# Exposure Class
# 
# Author: rsheftel
###############################################################################

constructor("Exposure", function(groupName='AllSystemsQ', verbose=TRUE){
	this <- extend(RObject(), "Exposure", .groupName=groupName, .verbose=verbose)
	constructorNeeds(this, groupName="character", verbose='logical')
	if (inStaticConstructor(this)) return(this)
	this$range(Range$before('2900-01-01')$after('1800-01-01'))
	this$.initializeCurveGroup()
	this$.initializeData()
	this$.initializeWeights()
	return(this)
})

method('range', 'Exposure', function(this, range=NULL, ...){
	needs(range='Range?')
	if(is.null(range)) return (this$.range)
	this$.range <- range
})

method('.initializeCurveGroup', 'Exposure', function(this, ...){
	this$.parentGroup <- CurveGroup(this$.groupName)	
	this$.childNames <- this$parentGroup()$childNames()
})

method('.initializeWeights', 'Exposure', function(this, ...){
	weights <- rep(1, length(this$.childNames))
	names(weights) <- this$.childNames
	this$.weights <- weights
	this$calculateSmash()
})

method('parentGroup', 'Exposure', function(this, ...){
	return(this$.parentGroup)	
})

method('weights', 'Exposure', function(this, weights=NULL, ...){
	needs(weights='list|numeric|integer?')
	if(is.null(weights)) return(this$.weights)
	if(is.list(weights)) weights <- unlist(weights)
	for (name in names(weights))
		failIf(!(name %in% this$.childNames), squish('Not a child group name: ',name))
	this$.weights[names(weights)] <- weights
	this$calculateSmash()
})

method('groupName', 'Exposure', function(this, ...){
	return(this$.groupName)	
})

method('.initializeData', 'Exposure', function(this, ...){
	this$.smash <- NULL
	this$.loadMSIVPVs()
	this$.idVars <- c('group','system','pv','market')
})

method('.loadMSIVPVs', 'Exposure', function(this, ...){
	for (child in this$.childNames){
		childGroup <- CurveGroup(child)
		msivPVs <- data.frame(	group=child, market=childGroup$markets(unique=FALSE), system=childGroup$systems(unique=FALSE), 
								pv=childGroup$pvs(unique=FALSE), weight.base=childGroup$totalWeights())
		this$.smash <- rbind(this$.smash, msivPVs)
	}
})

method('addSizingParameter', 'Exposure', function(this, ...){
	sizingParams <- NULL
	for (system in as.vector(unique(this$.smash$system))){
		sizingParam <- SystemDB$sizingParameter(system)
		for (pvName in as.vector(unique(this$.smash$pv[this$.smash$system==system]))){
			paramValue <- SystemDB$lastParameterValue(system,pvName,sizingParam)
			sizingParams <- rbind(sizingParams, data.frame(system=system, pv=pvName, 
												sizingParameter=sizingParam, sizingParameter.orig=paramValue))
		}
	}
	this$.smash <- merge(this$.smash, sizingParams, by=c('system','pv'))
	this$.idVars <- c(this$.idVars,'sizingParameter')
	this$calculateSmash()
})

method('smash', 'Exposure', function(this, ...){
	return(this$.smash)	
})

method('calculateSmash', 'Exposure', function(this, ...){
	weightDF <- data.frame(group=names(this$.weights),weight.user=this$.weights)
	this$.smash$weight.user <- NULL
	this$.smash <- merge(this$.smash, weightDF, by='group')
	this$.smash$weight <- this$.smash$weight.base * this$.smash$weight.user
	this$.calculateSizingParameter()
	this$.calculateRiskDollars()
})

method('.calculateSizingParameter', 'Exposure', function(this, ...){
	if('sizingParameter.orig' %in% colnames(this$.smash)) 
		this$.smash$sizingParameter.weighted <- (this$.smash$sizingParameter.orig * this$.smash$weight)	
})

method('.calculateRiskDollars', 'Exposure', function(this, ...){
	if('riskDollars.orig' %in% colnames(this$.smash)) 
		this$.smash$riskDollars.weighted <- (this$.smash$riskDollars.orig * this$.smash$weight)	
})

method('sizing', 'Exposure', function(this, ...){
	if(!('sizingParameter' %in% colnames(this$smash()))) this$addSizingParameter()
	molt <- melt(this$smash(), id=this$.idVars)
	res <- cast(molt, system + pv + sizingParameter ~ variable, min)
	failIf((!all(res == cast(molt, system + pv + sizingParameter ~ variable, max))), 'Error in casting data.')
	res <- res[,c('system','pv','sizingParameter','sizingParameter.orig','sizingParameter.weighted','weight','weight.user','weight.base')]
	names(res) <- c('system','pv','parameter','original','weighted','weight.final','weight.user','weight.base')
	return(res)
})

method('writeSizingCsv', 'Exposure', function(this, filename=NULL, asOfDate=NULL, ...){
	needs(filename='character?', asOfDate='character')
	if (is.null(filename)) filename <- squish(tempDirectory(),'sizing_',format(Sys.time(),'%Y%m%d_%H%M%S'),'.csv')
	data <- this$sizing()
	newData <- data[c('system','system','pv','parameter','weighted')]
	newData <- cbind(newData,data.frame(asOfDate=asOfDate))
	colnames(newData) <- c('System','Strategy','Name','ParameterName','ParameterValue','AsOfDate')
	write.csv(newData,filename,row.names=FALSE,quote=FALSE)
	print(filename)
})

method('addRiskDollars', 'Exposure', function(this, ...){
	if(!('sizingParameter.orig' %in% colnames(this$smash()))) this$addSizingParameter()
	riskDollarsForSystem <- function(system, parameterValue){
		switch(system,
					NDayBreak 			= parameterValue * 0.02,
					FadeMonthEndPush 	= parameterValue * 0.01,
					FadeWeekEndPush		= parameterValue * 0.01,
					NDayBreakCloseMinVol= parameterValue * 0.02,
					CouponSwap			= parameterValue * 312.5 * 15,
					BuySellAndHold		= 30000,
					TestSystem1			= parameterValue * 100,
					TestSystem2			= parameterValue / 100,
		parameterValue)
	}
	riskDollars <- mapply(riskDollarsForSystem, as.character(this$.smash$system), as.numeric(this$.smash$sizingParameter.orig))
	this$.smash$riskDollars.orig <- riskDollars
	this$calculateSmash()
})

method('riskDollars', 'Exposure', function(this, aggregationLevels='system', margins=FALSE, ...){
	resCols <- gsub(' ','',c(unlist(strsplit(aggregationLevels,'\\+'))))
	assert(all(resCols %in% c('group','system','pv','market')),squish('Invalid aggregationLevels: ',aggregationLevels))
	failIf((length(resCols)>2),'Only works for 2 or less aggregationLevels.')
	if(!('riskDollars.weighted' %in% colnames(this$smash()))) this$addRiskDollars()
	molt <- melt(this$smash(), id=this$.idVars)
	res <- cast(molt, squish(aggregationLevels,' ~ variable'), sum, margins=margins)
	res <- res[,c(resCols,'riskDollars.weighted')]
	names(res) <- c(resCols,'riskDollars')
	return(res)
})

method('loadCurves', 'Exposure', function(this, curvesDirectory, curvesExtension='bin', ...){
	needs(curvesDirectory='character', curvesExtension='character')
	if(rightStr(curvesDirectory,1)!='/') curvesDirectory <- squish(curvesDirectory,'/')
	this$.aggCurves <- AggregateCurves(this$.groupName, verbose=this$.verbose)
	this$.aggCurves$loadCurves(curvesDirectory=curvesDirectory, curvesExtension=curvesExtension)
})

method('aggregateCurvesObject', 'Exposure', function(this, ...){
	return(this$.aggCurves)	
})

method('aggregateCurvesWeights', 'Exposure', function(this, weights='weight', ...){
	needs(weights='numeric|logical|character')
	if(is.numeric(weights)) this$.aggCurves$weights(weights)
	if(is.character(weights)){
		assert((weights %in% colnames(this$.smash)), 'Not a valid weight name.')
		weightDF <- this$.smash[c('market','system','pv',weights)]
		colnames(weightDF) <- c('market','system','pv','weight')
		this$.aggCurves$weights(weightDF)
	 }	
})

method('aggregateCurves', 'Exposure', function(this, aggregationLevels=NULL, weights=NULL,...){
	needs(aggregationLevels='character?', weights='numeric|logical|character?')
	if (!is.null(weights)) this$aggregateCurvesWeights(weights)
	this$.aggCurves$weightedCurves(aggregationLevels=aggregationLevels)	
})


method('metricFrame', 'Exposure', function(this, aggregationLevels, metrics, weights='weight', percentages=TRUE, allRow=TRUE, ...){
	needs(aggregationLevels='character', metrics='list(Metric)', weights='character?', percentages='logical', allRow='logical')
			
	collapse <- this$aggregateCurves(aggregationLevels, weights)
	if(allRow) collapse$ALL <- this$.aggCurves$weightedCurves()
	summaryDF <- data.frame(id=names(collapse))
	for (metric in metrics){
		res <- NULL
		for (id in as.character(summaryDF$id)){
			if(this$.verbose) print(squish('Metric= ',as.character(metric),' : Curve= ',id))
			res <- c(res, collapse[[id]]$curve()$withRange(this$.range)$metric(metric))
		}
		summaryDF <- cbind(summaryDF, res)
		colnames(summaryDF)[NCOL(summaryDF)] <- as.character(metric)
		if(percentages){
			metricValues <- as.numeric(summaryDF[summaryDF$id!='ALL',NCOL(summaryDF)]) 
			res <- metricValues / sum(metricValues)
			if(allRow) res <- c(res,1)
			summaryDF <- cbind(summaryDF, res)
			colnames(summaryDF)[NCOL(summaryDF)] <- squish(as.character(metric),'.percent')
		}
	}
	if(allRow){
		nonAllRows <- summaryDF$id!='ALL'
		divBenefit <- NULL
		for (metricCol in 2:NCOL(summaryDF)){
			divBenefit <- c(divBenefit, 1 - (summaryDF[NROW(summaryDF),metricCol] / sum(summaryDF[nonAllRows,metricCol])))
		}
		divBenefitDF <- data.frame(id='DiversityBenefit', t(divBenefit))
		colnames(divBenefitDF) <- colnames(summaryDF)
		summaryDF <- rbind(summaryDF, divBenefitDF)
	}
	return(summaryDF)
})

method('correlations', 'Exposure', function(this, aggregationLevels, weights='weight', ...){
	needs(aggregationLevels='character', weights='character?')

	collapse <- this$aggregateCurves(aggregationLevels, weights)
	collapse$ALL <- this$.aggCurves$weightedCurves()
	equities <- do.call(merge, lapply(collapse, function(x) x$curve()$withRange(this$.range)$equity()))
	equities.diff <- diff(equities,lag=5) 
	return(cor(equities.diff, use="pairwise.complete.obs", method="pearson"))
})
