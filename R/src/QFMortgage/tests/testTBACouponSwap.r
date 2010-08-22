
library(QFMortgage)

tempDir <- squish(dataDirectory(),'temp_TSDB/')

fileMatches <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="QFMortgage"),'/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))	
	return(checkSame(benchData,testData))
}

test.TBACouponSwap <- function(){

    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(4.5,8.5,0.5), forwardDays=45)
    checkInherits(cpnSwap,'TBACouponSwap')
}

test.setRawDataFromTSDB <- function(){

    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(5,7,0.5), forwardDays=45)
    cpnSwap$setStartEndDates(startDate='2007-03-03',endDate='2008-03-05')
    cpnSwap$setRawDataFromTSDB()
    
    checkSame(round(cpnSwap$.priceZoos[[1]],digits=5), 97.00953)
}

test.generateActualData <- function(){

    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(5,7,0.5), forwardDays=45)
    cpnSwap$setStartEndDates(startDate='2007-03-03',endDate='2008-03-05')
    cpnSwap$setRawDataFromTSDB()
    cpnSwap$generateActualData()
    
    checkSame(round(cpnSwap$.currentCoupon.vector[1],digits=5), 5.62668)
    checkSame(dim(cpnSwap$.couponSwap.actual.matrix), c(253,4))

}

test.prepareDataForFit <- function(){

    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(5,7,0.5), forwardDays=45)
    cpnSwap$setStartEndDates(startDate='2007-03-03',endDate='2008-03-05')
    cpnSwap$setRawDataFromTSDB()
    cpnSwap$generateActualData()
    cpnSwap$setKnotPoints()
    cpnSwap$setStartEndDatesForCoupons()
    cpnSwap$generateWeights()
    cpnSwap$vectorizeMatrix()
    cpnSwap$filterVectors()
    
    checkSame(round(cpnSwap$.couponSwap.actual.vector[1],digits=5), 2.18456)
    checkSame(round(cpnSwap$.itmHigh.vector[100],digits=5), -0.70161)
    checkSame(round(cpnSwap$.currentCoupon.repeatVector[500],digits=5),5.71799)
    checkSame(round(cpnSwap$.slope.repeatVector[205],digits=5),0.83477)
    checkSame(length(cpnSwap$.weights.vector), length(cpnSwap$.couponSwap.actual.vector))
}

test.fitModel <- function(){

    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(5,7,0.5), forwardDays=45)
    cpnSwap$setStartEndDates(startDate='2007-03-03',endDate='2008-03-05')
    cpnSwap$setRawDataFromTSDB()
    cpnSwap$generateActualData()
    cpnSwap$setKnotPoints()
    cpnSwap$setStartEndDatesForCoupons()
    cpnSwap$generateWeights()
    cpnSwap$vectorizeMatrix()
    cpnSwap$filterVectors()
    cpnSwap$fitModel()
    checkSame(round(cpnSwap$getModelValues(seq(5,7,0.5), 5.5, 1.10),digits=6), c( 2.968113, 2.304759, 1.817608, 1.330456, 1.264489))
    checkSame(round(cpnSwap$getModelDurations(seq(5,7,0.5), 5.5, 1.10),digits=6), c(-1.326707, -1.150505, -0.974303, -0.553119, -0.131934))
    cpnSwap$setCouponDv01FromTsdb()
    checkSame(round(cpnSwap$getModelHedgeRatios(seq(5.5,7,0.5), 5.5, 1.10),digits=6), c(0.817727, 0.817067, 0.859257, 0.955993))
	cpnSwap$setRollDataFromTsdb()
	checkSame(round(cpnSwap$getModelWeightedRolls(seq(5.5,7,0.5), 5.5, 1.10),digits=6), c(0.052995, 0.033252, 0.051476, 0.033236))
	
	#Using the dv01.source for the weighted rolls
	checkSame(round(cpnSwap$getModelHedgeRatios(seq(5.5,7,0.5), 5.5, 9, method='dv01.source'),digits=7), 
													c(0.8437896,0.7378896,0.7628499,0.6854570))
	checkSame(round(cpnSwap$getModelWeightedRolls(seq(5.5,7,0.5), 5.5, 1.10, method='dv01.source'),digits=8), 
													c(0.04821050,0.04933493,0.07068225,0.09347247))
	
}

test.fitModelAgain <- function(){	
    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(4.5,8,0.5), forwardDays=45)
    cpnSwap$setStartEndDates(startDate='2005-12-31',endDate='2008-03-05')
    cpnSwap$setRawDataFromTSDB()
    cpnSwap$generateActualData()
    cpnSwap$setKnotPoints()
    cpnSwap$setStartEndDatesForCoupons()
    cpnSwap$generateWeights()
    cpnSwap$vectorizeMatrix()
    cpnSwap$filterVectors()
    cpnSwap$fitModel()
    checkSame(round(cpnSwap$getModelValues(seq(5,7.5,0.5), 5.75238, 1.80456),digits=4), c(3.1967, 2.6644, 2.1517, 1.6588, 1.4982, 1.6762))
    checkSame(round(cpnSwap$getModelDurations(seq(5,7.5,0.5), 5.75238, 1.80456),digits=7), c(-1.0645266, -1.0645266, -0.9857382, -0.9857382, 0.3560338, 0.3560338))
    cpnSwap$setCouponDv01FromTsdb()
    checkSame(round(cpnSwap$getModelHedgeRatios(seq(5,7.5,0.5), 5.75238, 1.80456),digits=7), c(0.8409017, 0.8313488, 0.8149196, 0.749176, 1.1187571, 1.1732525))
	cpnSwap$setRollDataFromTsdb()
	checkSame(round(cpnSwap$getModelWeightedRolls(seq(5,7.5,0.5), 5.75238, 1.80456),digits=9), c(0.052202857, 0.050494564, 0.033688206, 0.073406338, -0.003004515, -0.132480102))
}

test.fitModelAndWriteOutput <- function(){
    cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(4.5,8,0.5), forwardDays=45)
    cpnSwap$setStartEndDates(startDate='2001-01-01',endDate='2003-06-26')
    cpnSwap$setRawDataFromTSDB()
    cpnSwap$generateActualData()
    cpnSwap$setKnotPoints()
    cpnSwap$setStartEndDatesForCoupons()
    cpnSwap$generateWeights()
    cpnSwap$vectorizeMatrix()
    cpnSwap$filterVectors()
    cpnSwap$fitModel()
	checkSame(round(cpnSwap$getActualValues(seq(5,7.5,0.5)), digits=7), c(2.2663813,1.8476562,0.7844250,0.4265375,1.0187750,0.9968250))
	checkSame(round(cpnSwap$getModelValues(seq(5,7.5,0.5), 4.6166, 2.28207),digits=4), c(2.2832, 1.5988, 1.2329, 0.9639, 1.0433, 1.2285))
    checkSame(round(cpnSwap$getModelDurations(seq(5,7.5,0.5), 4.6166, 2.28207),digits=4), c( -1.3688, -1.3688, -0.538, -0.538, 0.3706, 0.3706))
    cpnSwap$setCouponDv01FromTsdb()
    checkSame(round(cpnSwap$getModelHedgeRatios(seq(5,7.5,0.5), 4.6166, 2.28207),digits=4), c(0.7534, 0.6569, 0.7691, 0.4336, 1.6501, 1.5147))
	cpnSwap$setRollDataFromTsdb()
	checkSame(round(cpnSwap$getModelWeightedRolls(seq(5,7.5,0.5), 4.6166, 2.28207),digits=4), c(0.0453, 0.0976, -0.1983, -0.0651, 0.0516, -0.0312))

	dataDate <- as.POSIXlt(cpnSwap$.endDate)
	dataDate$hour <- 15
	dataDateTime <- as.POSIXct(dataDate)
	
	cpnSwap$writeTSDBuploadFile(uploadMethod='file',path=tempDir)
	filename <- squish('TBA_CouponSwap_Price_Actual_',cpnSwap$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv')
	fileMatches(squish(tempDir,filename),filename)
	filename <- squish('TBA_CouponSwap_Price_Model_',cpnSwap$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv')
	fileMatches(squish(tempDir,filename),filename)
	filename <- squish('TBA_CouponSwap_DV01_Model_',cpnSwap$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv')
	fileMatches(squish(tempDir,filename),filename)
	filename <- squish('TBA_CouponSwap_HedgeRatio_Model_',cpnSwap$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv')	
	fileMatches(squish(tempDir,filename),filename)
	
	#	Weighted Roll funtionality moved to TBATRICouponSwap.R class
	#filename <- squish('TBA_CouponSwap_WgtRoll_Model_',cpnSwap$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv')
	#fileMatches(squish(tempDir,filename),filename)
}

test.liveData <- function(){
	cpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(4.5,8,0.5), forwardDays=45)
	cpnSwap$setStartEndDates(startDate='2001-01-01',endDate='2003-06-26')
	cpnSwap$setRawDataFromTSDB()
	cpnSwap$generateActualData()
	cpnSwap$setKnotPoints()
	cpnSwap$setStartEndDatesForCoupons()
	cpnSwap$generateWeights()
	cpnSwap$vectorizeMatrix()
	cpnSwap$filterVectors()
	cpnSwap$fitModel()
	checkSame(round(cpnSwap$getActualValues(seq(5,7.5,0.5)), digits=7), c(2.2663813,1.8476562,0.7844250,0.4265375,1.0187750,0.9968250))
	checkSame(round(cpnSwap$getModelValues(seq(5,7.5,0.5), 4.6166, 2.28207),digits=4), c(2.2832, 1.5988, 1.2329, 0.9639, 1.0433, 1.2285))
	
	#Now add the live information
	newDate <- as.POSIXct('2003-06-27 15:00:00')
	cpnSwap$addNewDate(newDate)
	cpnSwap$prepareAndFitModel()
	
	#Check that the actuals are unchanged
	checkSame(round(cpnSwap$getActualValues(seq(5,7.5,0.5)), digits=7), c(2.2663813,1.8476562,0.7844250,0.4265375,1.0187750,0.9968250))
	
	#Add in the values
	couponVector=seq(4.5,8,0.5)
	tickers <- paste('fncl',format(couponVector,nsmall=1),'45d_price',sep="_")
	priceVector <- as.vector(TSDataLoader$matrixToZoo(TimeSeriesDB$retrieveTimeSeriesByName(tickers,'internal',newDate,newDate)))
	cpnSwap$updateLastTBAPrices(priceVector, couponVector)
	
	cpnSwap$updateLastSwapRates(TimeSeriesDB$retrieveOneTimeSeriesByName('irs_usd_rate_2y_mid','internal',newDate,newDate)[[1]], TimeSeriesDB$retrieveOneTimeSeriesByName('irs_usd_rate_10y_mid','internal',newDate,newDate)[[1]])
	cpnSwap$updateLastCurrentCoupon(TimeSeriesDB$retrieveOneTimeSeriesByName('fncl_cc_30d_yield','internal',newDate,newDate)[[1]])
	cpnSwap$prepareAndFitModel()
	
	newCpnSwap <- TBACouponSwap(program='fncl', couponVector=seq(4.5,8,0.5), forwardDays=45)
	newCpnSwap$setStartEndDates(startDate='2001-01-01',endDate=newDate)
	newCpnSwap$setRawDataFromTSDB()
	newCpnSwap$generateActualData()
	newCpnSwap$setKnotPoints()
	newCpnSwap$setStartEndDatesForCoupons()
	newCpnSwap$generateWeights()
	newCpnSwap$vectorizeMatrix()
	newCpnSwap$filterVectors()
	newCpnSwap$fitModel()
	
	checkSame(cpnSwap$getActualValues(couponVector), newCpnSwap$getActualValues(couponVector))
	checkSame(cpnSwap$getModelValues(couponVector, 2.33905, 4.656756), newCpnSwap$getModelValues(couponVector, 2.33905,4.656756))
	checkSame(cpnSwap$getModelDurations(couponVector, 2.33905, 4.656756), newCpnSwap$getModelDurations(couponVector, 2.33905,4.656756))
	
	tickers.1n <- paste('fncl',format(couponVector,nsmall=1),'1n_price',sep="_")
	prices.1n <- as.vector(TSDataLoader$matrixToZoo(TimeSeriesDB$retrieveTimeSeriesByName(tickers.1n,'internal',newDate,newDate)))
	tickers.2n <- paste('fncl',format(couponVector,nsmall=1),'2n_price',sep="_")
	prices.2n <- as.vector(TSDataLoader$matrixToZoo(TimeSeriesDB$retrieveTimeSeriesByName(tickers.2n,'internal',newDate,newDate)))
	
	cpnSwap$setRollData(prices.1n - prices.2n, couponVector)
	newCpnSwap$setRollDataFromTsdb()
	
	tickers <- paste('fncl',format(couponVector,nsmall=1),'1n','dv01',sep="_")
	dv01s <- as.vector(TSDataLoader$matrixToZoo(TimeSeriesDB$retrieveTimeSeriesByName(tickers,'internal',newDate,newDate)))
	cpnSwap$setCouponDv01s(dv01s, couponVector)
	newCpnSwap$setCouponDv01FromTsdb()
	
	checkSame(cpnSwap$getModelDurations(couponVector, 2.33905, 4.656756), newCpnSwap$getModelDurations(couponVector, 2.33905, 4.656756))
	checkSame(cpnSwap$getModelHedgeRatios(couponVector, 2.33905, 4.656756), newCpnSwap$getModelHedgeRatios(couponVector, 2.33905, 4.656756))
	checkSame(cpnSwap$getModelWeightedRolls(couponVector, 2.33905, 4.656756), newCpnSwap$getModelWeightedRolls(couponVector, 2.33905, 4.656756))
	checkSame(cpnSwap$getModelWeightedRolls(couponVector, 2.33905, 4.656756, method='dv01.source'),
				newCpnSwap$getModelWeightedRolls(couponVector, 2.33905, 4.656756, method='dv01.source'))
}