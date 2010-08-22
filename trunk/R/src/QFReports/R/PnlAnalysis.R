# PnLAnalysis Class
# 
# Author: rsheftel
###############################################################################

constructor("PnlAnalysis", function(dateRange=NULL){
	this <- extend(RObject(), "PnlAnalysis", .dateRange = dateRange)
	constructorNeeds(this, dateRange="Range?")
	if (inStaticConstructor(this)) return(this)
	this$.groups <- list()
	this$.sources <- NULL
	this$.aum <- NULL
	return(this)
})

method("dateRange", "PnlAnalysis", function(this, ...){
	return(as.character(this$.dateRange))	
})

method("sources", "PnlAnalysis", function(this, ...){
	return(this$.sources)	
})

method("addPnlGroup", "PnlAnalysis", function(this, groupName=NULL, tags=NULL,...){
	needs(groupName="character", tags="character")
	failIf(!is.null(this$.sources),"Cannot add PnlGroup once Pnl has been loaded for any source.")
	newGroup <- list()
	newGroup[[groupName]] <- list(tags=tags)
	priorNames <- names(this$.groups)
	this$.groups <- appendSlowly(this$.groups, newGroup[[groupName]])
	names(this$.groups) <- c(priorNames,groupName)
})

method("groupNames", "PnlAnalysis", function(this, ...){
	return(names(this$.groups))
})

method("groupTags", "PnlAnalysis", function(this, groupName=NULL, ...){
	needs(groupName="character?")
	if (!is.null(groupName)) return(this$.groups[[groupName]]$tags)
	tagsList <- list()
	for (groupName in this$groupNames())
		tagsList <- appendSlowly(tagsList, as.vector(this$.groups[[groupName]]$tags))
	names(tagsList) <- this$groupNames()
	return(tagsList)	
})

method("tags", "PnlAnalysis", function(this, ...){
	return(unique(unlist(this$groupTags())))
})

method("loadPnl","PnlAnalysis", function(this, source=NULL, ...){
	needs(source='character')
	failIf(any(source %in% this$.sources),"Source already loaded")
	
	for (group in this$groupNames()){
		tags <- this$groupTags(group)
		if (is.null(this$.groups[[group]]$curves)) this$.groups[[group]]$curves <- list()
		pecList <- list()
		for (tag in tags)
			pecList <- appendSlowly(pecList, PerformanceDB$getPositionEquityCurve(source=source, tag=tag, range=this$.range))
		if(length(pecList)==0){
			this$.groups[[group]]$curves[[source]] <- ZooCurveLoader$fromEquity(zoo(NA,as.POSIXct('2007-11-01')),'empty')
		}else
			this$.groups[[group]]$curves[[source]] <- WeightedCurves(pecList)$curve()
	}
	this$.sources <- c(this$.sources, source)
})

method("groupCurve", "PnlAnalysis", function(this, groupName=NULL, source=NULL, ...){
	needs(groupName="character", source='character')
	return(this$.groups[[groupName]]$curves[[source]])	
})

method(".daily", "PnlAnalysis", function(this, source=NULL, range=NULL, target=NULL, ...){
	needs(source='character', range="Range?", target="function")
	failIf(!(source %in% this$sources()), squish("Source not loaded : ",source))
	pnlList <- list()
	for (group in this$groupNames()){
		priorNames <- names(pnlList)
		pnlList <- appendSlowly(pnlList, target(this$groupCurve(group, source)))
		names(pnlList) <- c(priorNames,group)
	}
	mergedZoo <- do.call(merge, pnlList)
	names(index(mergedZoo)) <- NULL
	if (!is.null(range)) mergedZoo <- range$cut(mergedZoo)
	return(mergedZoo)
})
	
method("dailyPnl", "PnlAnalysis", function(this, source=NULL, range=NULL, addTotal=TRUE, ...){
	needs(source='character', range="Range?", addTotal="logical")
	failIf(!(source %in% this$sources()), squish("Source not loaded : ",source))
				
	mergedZoo <- this$.daily(source,range,pnl.PositionEquityCurve)
	if (addTotal && (NCOL(mergedZoo) > 1)){
		failIf((length(mergedZoo)==0),'No valid dates in range for dailyPnl')
		mergedZoo <- cbind(mergedZoo,rowSums(mergedZoo,na.rm=TRUE))
		colnames(mergedZoo)[length(colnames(mergedZoo))] <- 'Total'
	}
	return(mergedZoo)
})

method("dailyEquity", "PnlAnalysis", function(this, source=NULL, range=NULL, addTotal=TRUE, ...){
	needs(source='character', range="Range?", addTotal="logical")
	failIf(!(source %in% this$sources()), squish("Source not loaded : ",source))
	
	mergedZoo <- this$.daily(source,range,equity.PositionEquityCurve)
	mergedZoo <- na.locf(mergedZoo, na.rm=FALSE)
	if (addTotal && (NCOL(mergedZoo) > 1)){
		mergedZoo <- cbind(mergedZoo,rowSums(mergedZoo,na.rm=TRUE))
		colnames(mergedZoo)[length(colnames(mergedZoo))] <- 'Total'
	}
	return(mergedZoo)
})


method("monthlyPnl", "PnlAnalysis", function(this, source=NULL, range=NULL, addTotal=TRUE, ...){
	needs(source='character', range="Range?")
	pnl.daily <- this$dailyPnl(source,range,addTotal)
	pnl.monthly <- Interval$MONTHLY$collapse(pnl.daily,sum)
	index(pnl.monthly) <- format(index(pnl.monthly),"%Y %b")
	return(pnl.monthly)
})

method("loadAUM", "PnlAnalysis", function(this, name=NULL,...){
	needs(name='character')
	this$.aum <- PerformanceDB$investorData(name=name, range=this$.range)
})

method("aum", "PnlAnalysis", function(this, endOfMonth=FALSE, ...){
	needs(endOfMonth='logical')
	if(endOfMonth){
		aum <- Interval$MONTHLY$collapse(this$.aum,last)
		index(aum) <- format(index(aum),"%Y %b")
		return(aum)
	}
	return(this$.aum)	
})

method("dailyReturns", "PnlAnalysis", function(this, source=NULL, range=NULL, addTotal=TRUE, ...){
	needs(source='character', range='Range?', addTotal='logical')
	failIf(is.null(this$aum()),"Cannot calculate returns before AUM loaded.")
	pnl <- this$dailyPnl(source=source, range=range, addTotal=addTotal)
	aum <- this$aum()
	aum <- zooForDates(aum,index(pnl),remove.na=FALSE)		#use the pnl dates for the aum
	aum <- lag(na.locf(aum),-1) 							#remove NAs and shift forward one
	return(pnl/aum)
})

method("monthlyReturns", "PnlAnalysis", function(this, source=NULL, range=NULL, addTotal=TRUE, ...){
	needs(source='character', range='Range?', addTotal='logical')
	failIf(is.null(this$aum()),"Cannot calculate returns before AUM loaded.")
	pnl <- this$monthlyPnl(source=source, range=range, addTotal=addTotal)
	aum <- this$aum(endOfMonth=TRUE)
	aum <- merge(pnl,aum)[,'aum']							#use the pnl dates for the aum
	aum <- lag(na.locf(aum),-1)		 						#remove NAs and shift forward one
	returns <- pnl/aum
	rownames(returns) <- NULL
	return(returns)
})

method("pnlDifference", "PnlAnalysis", function(this, groups=NULL, sources=NULL, range=NULL, ...){
	needs(groups='character?', sources='character', range='Range?')
	failIf((length(sources)!=2),'Sources input must be a vector of 2 sources.')
	if(is.null(groups)) groups <- this$groupNames()
	pnl.source1 <- this$dailyPnl(source=sources[1],range=range,addTotal=FALSE)
	pnl.source2 <- this$dailyPnl(source=sources[2],range=range,addTotal=FALSE)
	failIf((length(pnl.source1)==0),'No valid data for date range for source: ',sources[1])
	failIf((length(pnl.source1)==0),'No valid data for date range for source: ',sources[2])
	
	data <- data.frame(t(rep(NA,4)))
	names(data) <- c('Group',sources[1],sources[2],'Diff')
	df <- data.frame()
	.sum <- function(z){if(all(is.na(z)))NA else sum(na.omit(z))}
	for(group in groups){
		data['Group'] <- group
		data[sources[1]] <- .sum(pnl.source1[,group])
		data[sources[2]] <- .sum(pnl.source2[,group])
		data['Diff'] <- data[sources[1]] - data[sources[2]]
		df <- rbind(df,data)
	}
	data['Group'] <- 'TOTAL'
	data[sources[1]] <- .sum(df[sources[1]])
	data[sources[2]] <- .sum(df[sources[2]])
	data['Diff'] <- .sum(df['Diff'])
	df <- rbind(df,as.data.frame(data))
	return(df)
})

method("pnlDifferenceZoo", "PnlAnalysis", function(this, groups=NULL, sources=NULL, range=NULL, replaceNA=NA, ...){
	needs(groups='character?', sources='character', range='Range?')
	failIf((length(sources)!=2),'Sources input must be a vector of 2 sources.')
	if(is.null(groups)) groups <- this$groupNames()
	pnl.source1 <- this$dailyPnl(source=sources[1],range=range,addTotal=FALSE)
	pnl.source2 <- this$dailyPnl(source=sources[2],range=range,addTotal=FALSE)
	
	result <- list()
	zooColNames <- c(sources[1],sources[2],'Diff','CumDiff')
	for (group in groups){
		diffZoo <- merge.zoo(pnl.source1[,group], pnl.source2[,group], all=TRUE)
		diffZoo[is.na(diffZoo)] <- replaceNA
		diffZoo <- merge(diffZoo, diffZoo[,1] - diffZoo[,2])
		diffZoo <- merge(diffZoo, cumsum(na.omit(diffZoo[,3])))
		colnames(diffZoo) <- zooColNames
		result <- appendSlowly(result, diffZoo) 
	}
	names(result) <- groups
	if (length(groups)==1) return(diffZoo)
	return(result)		
})

method("plotEquity", "PnlAnalysis", function(this, source=NULL, range=NULL, addTotal=TRUE, ...){
	needs(source='character', range='Range?', addTotal='logical')
	equity <- this$dailyEquity(source=source, range=range, addTotal=addTotal)
	chart.TimeSeries(equity, ylab="Equity", main=squish("Equity"), lwd=2, legend.loc='topleft',colorset=1:20)
})
