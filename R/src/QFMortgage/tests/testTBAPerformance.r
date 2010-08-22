# test for TBAPerformance.R class
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

fileDir <- squish(system.file("testdata", package="QFMortgage"),'/TBAPerformance/')

fileMatches <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="QFMortgage"),'/TBAPerformance/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))	
	return(checkSame(benchData,testData))
}

test.AllFunctionality <- function(){
	
	program = 'fncl'
	coupons <- seq(4.5,8.5,0.5)
	
	perf <- TBAPerformance(program, coupons)
	checkInherits(perf,'TBAPerformance')
	
	perf$setTsdbSources(tbaPriceSource="internal", tbaDv01Source='internal')
	perf$setITMKnots(ITMKnots=seq(-3,3,0.5))
	perf$setCouponDateRange()
	
	perf$setTBADv01(squish(fileDir,"OAD.csv"))
	perf$setPartialDurations(squish(fileDir,'PartialDuration_'))
	perf$setTBAPrice(squish(fileDir,"TBAPx.csv"))
	perf$setSwapRates(squish(fileDir,"SwapRates.csv"))
	
	perf$calculateExpectedPriceChanges()
	perf$calculateActualPriceChanges()
	perf$calculateCouponPerformances()
	perf$setCurrentCoupon(squish(fileDir,"CurrentCoupon.csv"))
	perf$generateITMPerformances()
	perf$uploadCouponPerformances(uploadFilename="testTBAPerf_Coupon",uploadPath=tempDirectory(),uploadMethod='file')
	perf$uploadITMPerformances(uploadFilename="testTBAPerf_ITM",uploadPath=tempDirectory(),uploadMethod='file')
	
	fileMatches(squish(tempDirectory(),'testTBAPerf_Coupon.csv'),'testTBAPerf_Coupon.csv')
	fileMatches(squish(tempDirectory(),'testTBAPerf_ITM.csv'),'testTBAPerf_ITM.csv')
}

test.summaryStatistics <- function(){
	perf <- TBAPerformance('fncl', seq(4.5,8.5,0.5))
	perf$setITMKnots(ITMKnots=seq(-3,3,0.5))
	perf$setTsdbSources(tbaPriceSource="internal", tbaDv01Source='summaryTest')
	results <- perf$calculateStatistics(squish(fileDir,'testTBAPerf_ITM.csv'))
	
	if(isWindows()) checkSame(results, dget(squish(fileDir,'summaryStats.dput')))
}
