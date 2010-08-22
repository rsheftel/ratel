# TODO: Add comment
# 
# Author: RSheftel
###############################################################################


constructor("TBAPerformance", function(program = NULL, couponVector = NULL){
	this <- extend(RObject(), "TBAPerformance", .program = program, .couponVector = couponVector)
	if (inStaticConstructor(this)) return(this)
	constructorNeeds (this, program = "character", couponVector = "numeric")
	
	#Set up initial variables
	this$.couponStringVector = format(this$.couponVector,nsmall=1)
	this$.couponStepSize = 0.5
	this$.settle$dv01 = "1n"
	this$.settle$price = "45d"
	
	this$setTsdbSources()
	this$setITMKnots(seq(-3,3,0.5))	
	this$.swapTenors <- c('2y','5y','10y','30y')
	#Default start and end dates, coupon start end
	#this$setStartEndDates(as.POSIXct('1900-01-01'), as.POSIXct('2100-01-01'))
	
	return(this)
})

method("setITMKnots", "TBAPerformance", function(this, ITMKnots, ...){
	needs(ITMKnots='numeric?')
	this$.ITMKnots = ITMKnots	
})

method("getData", "TBAPerformance", function(this, container='tsdb', attributeList, source, arrangeBy, ...){
		
	TSDataLoader$getDataByAttributeList(container, attributeList, source, arrangeBy)
})

method("setTsdbSources", "TBAPerformance", function(this, tbaPriceSource="internal", tbaDv01Source='internal', tbaPartialSource='internal',...){
#Set up the tsdb data sources for the various elements
	needs(tbaPriceSource="character?", tbaDv01Source='character?')
		
	this$.sources$dv01 <- tbaDv01Source
	this$.sources$partials <- tbaPartialSource
	this$.sources$price <- tbaPriceSource
	this$.sources$swapRate <- 'internal'
	this$.sources$currentCoupon <- 'internal'
})

method("setCouponDateRange", "TBAPerformance", function(this, startDates=NULL, endDates=NULL,...){		
	needs(startDates="POSIXct|character?", endDates="POSIXct|character?")

	startEndDates <- TBA$startEndDatesForCoupons(this$.program, this$.couponVector, startDates=startDates, endDates=endDates)
	
	startDates.list <- as.list(as.character(startEndDates$startDates))
	startDates.list <- lapply(startDates.list, function(x) as.POSIXct(x))
	names(startDates.list) <- as.character(this$.couponVector)
	this$.TBA$startDates <- startDates.list
	
	endDates.list <- as.list(as.character(startEndDates$endDates))
	endDates.list <- lapply(endDates.list, function(x) as.POSIXct(x))
	names(endDates.list) <- as.character(this$.couponVector)
	this$.TBA$endDates <- endDates.list		
})

method("filterByDateRange", "TBAPerformance", function(this, listToFilter, ...){
	result.list <- list()
	for (coupon in names(listToFilter)){
		if (coupon %in% names(this$.TBA$startDates)){
			rangeObj <- Range(this$.TBA$startDates[[coupon]], this$.TBA$endDates[[coupon]])
			result.list[[coupon]] <- rangeObj$cut(listToFilter[[coupon]])
		}
		else
			result.list[[coupon]] <- NA
	}
	return(result.list)
})

method("setTBADv01", "TBAPerformance", function(this, container='tsdb', ...){
#TBA$oad$[coupon]
		
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="dv01",
		quote_side="bid",
		settle=this$.settle$dv01,
		coupon=this$.couponStringVector
		)
	
	this$.TBA$dv01 <- this$getData(container, attributeList, source=this$.sources$dv01, arrangeBy="coupon")
	rownames(this$.TBA$dv01) <- as.numeric(rownames(this$.TBA$dv01))
	this$.TBA$dv01 <- this$filterByDateRange(this$.TBA$dv01)
})

method("setTBAPrice", "TBAPerformance", function(this, container='tsdb', ...){
#TBA$oad$[coupon]
		
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="price",
		quote_side="bid",
		settle=this$.settle$price,
		coupon=this$.couponStringVector
	)
		
	this$.TBA$price <- this$getData(container, attributeList, source=this$.sources$price, arrangeBy="coupon")
	rownames(this$.TBA$price) <- as.numeric(rownames(this$.TBA$price))
	this$.TBA$price <- this$filterByDateRange(this$.TBA$price)
})

method("setSwapRates", "TBAPerformance", function(this, container='tsdb', ...){
#TBA$oad$[coupon]
		
	attributeList <- list(
		instrument="irs", 
		ccy="usd",
		quote_type="close",
		quote_convention="rate",
		quote_side="mid",
		transformation="raw",
		tenor=this$.swapTenors
	)
		
	this$.swap$rate <- this$getData(container, attributeList, source=this$.sources$swapRate, arrangeBy="tenor")
})

method("setPartialDurations", "TBAPerformance", function(this, container='tsdb',...){
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="partial_duration",
		quote_side="bid",
		settle=this$.settle$dv01,
		coupon=this$.couponStringVector
	)
	dataContainer <- container
	for (tenor in this$.swapTenors){
		attributeList$tenor <- tenor
		if(container != 'tsdb') dataContainer <- squish(container,tenor,'.csv')		
		this$.TBA$partialDuration[[tenor]] <- this$getData(dataContainer, attributeList, source=this$.sources$partial, arrangeBy="coupon")
		rownames(this$.TBA$partialDuration[[tenor]]) <- as.character(as.numeric(rownames(this$.TBA$partialDuration[[tenor]])))
	}
})

method("setCurrentCoupon", "TBAPerformance", function(this, container='tsdb',...){
#TBA$currentCoupon$yield
		
	if (container == 'tsdb'){
		this$.currentCoupon$yield = TimeSeriesDB$retrieveOneTimeSeriesByName('fncl_cc_30d_yield',this$.sources$currentCoupon)
	}
	else{
		this$.currentCoupon$yield <- the(this$getData(container, NULL, this$.sources$currentCoupon, arrangeBy="settle"))
	}	
})

method("calculateExpectedPriceChange", "TBAPerformance", function(this, coupon, ...){
	
	coupon <- as.character(coupon)
	#Merge the zoos to get consistent dates
	mergedZoo <- this$.TBA$dv01[[coupon]]
	for (tenor in this$.swapTenors){
		mergedZoo <- merge(mergedZoo, this$.TBA$partialDuration[[tenor]][[coupon]], all=FALSE)
	}
	for (tenor in this$.swapTenors){
		mergedZoo <- merge(mergedZoo, this$.swap$rate[[tenor]], all=FALSE)
	}
	
	#spin the values back out
	colCount <- 1
	dv01 <- mergedZoo[,colCount]
	colCount <- colCount + 1
	partialDuration.total <- zoo(0,index(mergedZoo))
	partialDuration <- list()
	for (tenor in this$.swapTenors){
		partialDuration[[tenor]] <- mergedZoo[,colCount]
		partialDuration.total <- partialDuration.total + partialDuration[[tenor]]
		colCount <- colCount + 1
	}
	partialPercent <- list()
	for (tenor in this$.swapTenors){
		partialPercent[[tenor]] <- partialDuration[[tenor]] / partialDuration.total
	}
	swapRate <- list()
	for (tenor in this$.swapTenors){
		swapRate[[tenor]] <- mergedZoo[,colCount]
		colCount <- colCount + 1
	}
	
	#Calculate expected price change
	#Equals the prior days dv01 * partials[i] * dSwapRate

	dPrice <- 0
	for (tenor in this$.swapTenors){
		dPrice <- dPrice + (-lag(dv01,-1) * lag(partialPercent[[tenor]],-1) * diff(swapRate[[tenor]], lag=1, differences=1))
	}
	this$.dPrice$expected[[coupon]] <- dPrice
})

method("calculateExpectedPriceChanges", "TBAPerformance", function(this, ...){
		
	for (coupon in this$.couponVector){
		print(squish("Calculating expected price change for coupon = ",coupon))
		if (length(this$.TBA$dv01[[as.character(coupon)]])!=0)
			this$calculateExpectedPriceChange(coupon=coupon)
	}
})

method("calculateActualPriceChanges", "TBAPerformance", function(this, ...){
		
	for (coupon in names(this$.dPrice$expected)){
		dates.expected <- index(this$.dPrice$expected[[coupon]])
		if (length(dates.expected) >0 ){
			price.actual <- zooForDates(this$.TBA$price[[coupon]], dates.expected)
			this$.dPrice$actual[[coupon]] <-diff(price.actual,lag=1,differences=1,arithmatic=TRUE,na.pad=FALSE)	
		}
	}		
})

method("calculateCouponPerformances", "TBAPerformance", function(this, ...){
	
	for (coupon in names(this$.dPrice$expected)){
		this$.performance$coupon[[coupon]] <- this$.dPrice$actual[[coupon]] - this$.dPrice$expected[[coupon]]
	}
})

method("generateITMPerformances", "TBAPerformance", function(this, ...){
	performanceZoo <- do.call(merge.zoo, this$.performance$coupon)
	itmZoo <- merge(this$.currentCoupon$yield, performanceZoo, all=FALSE)
	currentCouponZoo <- itmZoo[,1]
	couponZoo <- zoo(matrix(as.numeric(colnames(performanceZoo)),ncol=length(colnames(performanceZoo))), index(itmZoo))
	itmMatrix <- couponZoo - currentCouponZoo
	
	itmPerformance.matrix <- matrix(NA,ncol=length(this$.ITMKnots),nrow=length(index(couponZoo)))  
	
	for (zooIndex in seq_along(index(itmMatrix))){
		zooDate <- index(couponZoo)[zooIndex]
		itmPerformance.matrix[zooIndex,] <- approx(xout=this$.ITMKnots, x=as.vector(itmMatrix[zooDate,]), y=as.vector(performanceZoo[zooDate,]), rule=1, method="linear")$y
	}
		
	for (knotCount in seq_along(this$.ITMKnots)){
		knotPoint <- as.character(this$.ITMKnots)[knotCount]
		this$.performance$itm[[knotPoint]] <- zoo(itmPerformance.matrix[,knotCount], index(itmMatrix))
	}	
})

method("uploadCouponPerformances", "TBAPerformance", function(this, uploadFilename=NULL, uploadPath=NULL, uploadMethod='file',...){
		
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
		
	tsdbNames <- c()	
	mergedZoo <- zoo(NA,as.POSIXct('1900-01-01 15:00:00'))
	for (coupon in names(this$.performance$coupon)){
		couponZoo <- this$.performance$coupon[[as.character(coupon)]]
		if (!all(is.na(couponZoo))){
			mergedZoo <- merge(mergedZoo,this$.performance$coupon[[as.character(coupon)]], all=TRUE)
			tsdbNames <- c(tsdbNames,paste(this$.program, format(as.numeric(coupon),nsmall=1), this$.settle$price, 'performance', sep="_"))
		}
	}
	tsdbValues <- mergedZoo[2:length(index(mergedZoo)),2:NCOL(mergedZoo)]
	uploadZooToTsdb(tsdbValues, tsdbNames=tsdbNames, tsdbSources='TBAPerformance', uploadMethod=uploadMethod, uploadFilename=uploadFilename, uploadPath=uploadPath)			
})

method("uploadITMPerformances", "TBAPerformance", function(this, uploadFilename=NULL, uploadPath=NULL, uploadMethod='file',...){
		
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		

	tsdbNames <- paste(this$.program, names(this$.performance$itm), this$.settle$price, 'performance', sep="_")
	tsdbValues <- do.call(merge, this$.performance$itm)
	uploadZooToTsdb(tsdbValues, tsdbNames=tsdbNames, tsdbSources='TBAPerformance', uploadMethod=uploadMethod, uploadFilename=uploadFilename, uploadPath=uploadPath)			
})

method("calculateStatistics", "TBAPerformance", function(this, ITMPerformanceFile, ...){
	needs(ITMPerformanceFile='character')
	
	quantile10 <- function(x) quantile(x,0.10)[[1]]
	quantile25 <- function(x) quantile(x,0.25)[[1]]
	quantile75 <- function(x) quantile(x,0.75)[[1]]
	quantile90 <- function(x) quantile(x,0.90)[[1]]
	MAE <- function(x) mean(abs(x))
	
	result.matrix <- c('tbaDv01Source','itm','mean','stddev','MAE','skew','kurtosis','quantile10','quantile25','quantile75','quantile90')
	function.list <- c(mean,sd,MAE,skewness,kurtosis,quantile10,quantile25,quantile75,quantile90)
	
	df <- read.csv(ITMPerformanceFile)
	for (itm in seq_along(this$.ITMKnots)){
		itm.vector <- na.omit(df[,itm+1])*32
		if(length(itm.vector) > 0){
			result.vector <- sapply(function.list, function(func) func(itm.vector))
			parameter.vector <- c(this$.sources$dv01,this$.ITMKnots[itm])
			result.matrix <- rbind(result.matrix, t(c(parameter.vector, result.vector)))	
		}
	}
	
	result.df <- data.frame(result.matrix[2:nrow(result.matrix),])
	colnames(result.df) <- result.matrix[1,]
	
	this$statisticsCube <- result.df
	return(this$statisticsCube)
})
