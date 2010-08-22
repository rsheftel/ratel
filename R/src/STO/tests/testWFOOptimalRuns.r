library("STO")

source(system.file("testHelper.r", package = "STO"))

testNBestNetProfit <- function() {
	wfo <- createWFO()
	wfo$createSchedule("2003-09-27",365)
	wfo$calcPortfolioWeights(p.EqualWeighting)
	wfo$addPortfolios()
	optimalRuns <- wfo$calcOptimalRuns("BestNetProfit",or.nBestNetProfit,steps = 1:5,nBest = 2)
	checkSame(optimalRuns,data.frame(step = 1:5,optimalRuns = c('2,3','2,1','1,2','1,2','1,2'),stringsAsFactors = FALSE))
	destroyWFO()
}

testLiqInjETF <- function() {
	wfo <- createWFO()
	wfo$createSchedule("2003-09-27",365)
	wfo$calcPortfolioWeights(p.EqualWeighting)
	wfo$addPortfolios()
	optimalRuns <- wfo$calcOptimalRuns("LiqInjETF",or.LiqInjETF,steps = 1:5,nBest = 2, q = 0.5, optiMetric = NetProfit)
	checkSame(optimalRuns,data.frame(step = 1:5,optimalRuns = c('1','2,1','1,2','1,2','1,2'),stringsAsFactors = FALSE))
	destroyWFO()
}