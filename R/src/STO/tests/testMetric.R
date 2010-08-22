library("STO")
source(system.file("testHelper.r", package = "STO"))

testCurvesDir <- squish(system.file("testdata", package="STO"),'/TestCurves/')

testMetricConstructor <- function() {
    metric <- Metric$fetch("NetProfit")
    checkInherits(metric, "Metric")
    checkSame("NetProfit", as.character(metric))

    metric2 <- Metric$fetch("AverageDrawDown")
    map <- Map("Metric", "numeric")
    map$set(metric, 7)
    map$set(metric2, 9)

    checkSame(7, map$fetch(metric))
    checkSame(9, map$fetch(metric2))
    shouldBomb(map$fetch(Metric("foo")))

    allMetricsLength <- length(Metric$all())
    checkTrue(allMetricsLength > 20)
    Metric("asdfasdfasdf", metricNA)
    checkLength(Metric$all(), allMetricsLength+1)
}

curveFromEquityFile <- function(filename, columnName){
	benchZoos <- read.zoo(filename, sep=",", header=TRUE)
	return(ZooCurveLoader$fromEquity(benchZoos[,columnName],columnName))
}

testPsi <- function() {
	frozen <- curveFromEquityFile(squish(testCurvesDir,"RandomCurvesSamePnL.csv"),"B")	
	checkSame(0.502620542915471, frozen$metric(Psi))
}

testPsiNu <- function() {
	frozen <- curveFromEquityFile(squish(testCurvesDir, 'RandomCurvesSamePnL.csv'), 'B')
	checkSame(0.1638825846394343, frozen$metric(PsiNu))
}

testYearsInZoo <- function() {
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)	
	checkSame(5.02121834360027,yearsInZoo(curve$pnl()))
	
	sto <- STO(stoDirectory(), "IntradaySTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)	
	checkSame(0.242832154536467,yearsInZoo(curve$pnl()))
}

testAnnualizationFactor <- function() {
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)	
	checkSame(15.7590006478097,annualizationFactor(curve$pnl()))
	
	sto <- STO(stoDirectory(), "IntradaySTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)	
	checkSame(242.855944355369,annualizationFactor(curve$pnl()))
}

testMaxDrawDown <- function() {
    checkSame(-1, maxDrawDown(pnl(c(1, 0, 1))))
    checkSame(-1, maxDrawDown(pnl(c(1, 0, 0))))
    checkSame(0, maxDrawDown(pnl(c(1, 1, 1))))
    checkSame(0, maxDrawDown(pnl(c(1, 2, 3))))
    checkSame(-2, maxDrawDown(pnl(c(3, 2, 1))))
    checkSame(-2, maxDrawDown(pnl(c(3, 2, 1, 2))))

    checkSame(-13, maxDrawDown(pnl(c(1, 3, -1, 6, 5, 7, -3, 6, -6, 10))))
    checkSame(-13, maxDrawDown(pnl(c(1, 2, 3, 1, -1, 1, 2, 4, 5, 6, 5, 7, 4, 2, -1, -3, 0, 3, 6, 5, 2, -1, -3, -6, 0, 3, 5, 8, 10))))
}

testCanGenerateAllMetrics <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    cube$calculate(TSGeneratedMetrics, simpleMsivs)
    cube$calculate(NonTSGeneratedMetrics, simpleMsivs)
}

checkAverageDrawDown <- function(drawdown, days, recoveryDays, equity) {
    checkSame(drawdown, averageDrawDown(pnl(equity)))
    checkSame(days, averageDrawDownTime(pnl(equity)))
    checkSame(recoveryDays, averageDrawDownRecoveryTime(pnl(equity)))
}

# Outstanding questions
# - Units on recovery time (bars)
# - Drawdown at end of series - how to factor into average drawdown time (exclude from drawdown time)
# - If there's a "tie" for the trough in a drawdown, we use first? in recovery time calculation (true)

testAverageDrawDown <- function() { 
    checkAverageDrawDown(-0, 0, 0, c(1, 1, 1))
    checkAverageDrawDown(-0, 0, 0, c(1, 2, 3))
    checkAverageDrawDown(-2, 0, 0, c(3, 2, 1))
    checkAverageDrawDown(-1, 0, 0, c(0, 0, -1))
    checkAverageDrawDown(-1, 0, 0, c(1, 2, 1))
    checkAverageDrawDown(-1, 2, 1, c(2, 1, 2))
    checkAverageDrawDown(-1, 2, 1, c(2, 1, 2, 1))
    checkAverageDrawDown(-2.5, 3, 1, c(2, 1, 4, 2, 2, 0, 5))
    checkAverageDrawDown(-1.5, 2, 1, c(2, 1, 2, 0, 2))
    checkAverageDrawDown(-2.75, (11/3), (5/3), c(2, 1, 1, 0, 1, 2, 1, 2, 3, 4, 5, 6, 5, 3, 4, 7, 2)) # 5, 2, 4 / 2, 1, 2
    checkAverageDrawDown(-2.75, (11/3), (5/3), zoo(c(2, 1, 1, 0, 1, 2, 1, 2, 3, 4, 5, 6, 5, 3, 4, 7, 2), 1:17)) # 5, 2, 4 / 2, 1, 2
}

testKRatio <- function() {
    # Example from https://www.quadrafund.com/TWiki/pub/Research/PerformanceMetrics/K-Ratio.pdf
    checkSame(5.91039027990818, kRatio(pnl(c(
        0, -.15, 0.72, 1.23, 2.5, 2.33, 2.54, 3.63, 4.91, 6.25, 6.32, 6.60, 6.75, 7.92, 8.30, 9.00, 9.22, 9.36, 9.37, 10.17
    ))))
    shouldBombMatching(kRatio(c()), "must have at least one observation")
}

testSortinoRatio <- function(){
	shouldBomb(sortinoRatio(c()))
	checkSame(sortinoRatio(c(0,0,0)),0)
	checkSame(-11.2370878281316, sortinoRatio(-5:5,2))
	checkSame(6.05596339346192, sortinoRatio(c(1,6,9,-4,-2,0,0,7,8),2))
}

testOmegaRatio <- function(){
	shouldBomb(omegaRatio(c()))
	checkSame(1.4666666666, omegaRatio(c(1,6,9,-4,-2,0,0,7,8),2))
}

testUpsidePotentialRatio <- function(){
	shouldBomb(upsidePotentialRatio(c()))
	checkSame( 1.08916229727434, upsidePotentialRatio(10:-8,0))
	checkSame( 1.96061214930440, upsidePotentialRatio(c(1,6,9,-4,-2,0,0,7,8),0))
}

testRatios <- function(){
#Test multiple ratios from frozen curve, all at once for speed	
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	cube <- sto$metrics()
	checkSame(cube$oneValue(OmegaRatio,sto$msivs()[[1]], 1),  2.46153846153846)	
	checkSame(cube$oneValue(SortinoRatio,sto$msivs()[[1]], 1),1.39395317550843)
	checkSame(cube$oneValue(UpsidePotentialRatio,sto$msivs()[[1]], 1), 1.07066790449438)
}

testNetProfitLessTransactionCosts <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(lessTransactionCosts(NetProfit, 2), simpleMsivs[[1]], 3), 25)
    checkSame(cube$oneValue(lessTransactionCosts(NetProfit, 3), simpleMsivs[[1]], 3), 15)
    checkSame(cube$oneValue(lessTransactionCosts(TSNetProfit, 3), simpleMsivs[[1]], 3), 120)
}

testPercentile <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    pct <- percentile(TSNumWinTrades)
    checkSame(cube$oneValue(pct, first(simpleMsivs), 1), 100*(2/3))
    checkSame(cube$oneValue(pct, first(simpleMsivs), 2), 100)
    checkSame(cube$oneValue(pct, first(simpleMsivs), 3), 100*(1/3))
    revPct <- percentile(TSMaxIDDrawDown, smallerBetter=TRUE)
    checkSame(cube$oneValue(revPct, first(simpleMsivs), 1), 100*(2/3))
    checkSame(cube$oneValue(revPct, first(simpleMsivs), 2), 100)
    checkSame(cube$oneValue(revPct, first(simpleMsivs), 3), 100*(1/3))
}

testTSExpectancy <- function() {
	sto <- STO(stoDirectory(), "CurveAndMetricSTO")
	cube <- sto$metrics()
	checkSame(cube$oneValue(TSExpectancy,sto$msivs()[[1]], 1), -0.242104412361078)
	checkSame(cube$oneValue(TSExpectancyScore,sto$msivs()[[1]], 1), -0.0293909174948427)	
}

testTSWinLossRatio <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSWinLossRatio, simpleMsivs[[2]], 1), 0.358490566037736)
}

testTSAverageTrade <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSAverageTrade, simpleMsivs[[2]], 3), 30.76923077)
}

testStandardDeviationPnl <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    checkSame(cube$oneValue(StandardDeviationPnl, first(simpleMsivs), 2), 0.577350269189626)
}

testTSLargestLossMultAvgLoss <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSLargestLossMultAvgLoss, simpleMsivs[[2]], 1), 0.943396226)
}

testTSLargestLossPerGrossLoss <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSLargestLossPerGrossLoss, simpleMsivs[[2]], 1), 0.314465409)
}

testTSLargestWinMultAvgWin <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSLargestWinMultAvgWin, simpleMsivs[[2]], 1), 7.719298246)
}

testTSLargestWinPerGrossProfit <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSLargestWinPerGrossProfit, simpleMsivs[[2]], 1), 1.543859649)
}

testTSLargestWinPerNetProfit <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSLargestWinPerNetProfit, simpleMsivs[[2]], 3), 0.3075)
}

testTSProfitFactor <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSProfitFactor, simpleMsivs[[2]], 1), 0.597484277)
	checkSame(cube$oneValue(TSProfitFactor, simpleMsivs[[2]], 2), 0)
}


testTSPercentProfit <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSPercentProfit, simpleMsivs[[2]], 1), 0.625)
}

testCalmarRatio <- function() {
    sto <- STO(stoDirectory(), "ManyDatesSTO")
    cube <- sto$metrics()
    checkSame(cube$oneValue(CalmarRatio,sto$msivs()[[1]], 1), 0.540563171833619)
}

testAnnualizedNetProfit <- function() {
    sto <- STO(stoDirectory(), "ManyDatesSTO")
    curve <- sto$curves()$curve(first(sto$msivs()), 1)
    checkSame(1135.1826608506, annualizedNetProfit(curve))
    shouldBomb(annualizedNetProfit())

    sto <- STO(stoDirectory(), "TestEmptyRunSto")
    curve <- sto$curves()$curve(first(sto$msivs()), 1)    
    checkSame(0, annualizedNetProfit(curve))
}

testDailyStandardDeviation <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(dailyStandardDeviation(curve),2),33187.38)
	
	# Intraday data
	
	sto <- STO(stoDirectory(), "IntradaySTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)	
	checkSame(round(dailyStandardDeviation(curve),2),2159777.78)
}

testMonthlylyStandardDeviation <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(monthlyStandardDeviation(curve),2),151376.93)
}

testWeeklyStandardDeviation <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
        dout("retrieved curve...")
	checkSame(round(weeklyStandardDeviation(curve),2),74072.49)
}

testMaxDrawDownMonthly <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	dout("retrieved curve...")
	checkSame(round(maxDrawDownMonthly(curve),2),-935015.62)
}

testMaxDrawDownWeekly <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	dout("retrieved curve...")
	checkSame(round(maxDrawDownWeekly(curve),2),-1076109.38)
}

testTenPercentileDrawDown <- function() {
	testCurve <- c(0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,-19,19,-20,20)
	testDrawDown <- tenPercentileDrawDown(testCurve)
	checkSame(testDrawDown,-19)	
}

testConditionalTenPercentileDrawDown <- function() {
	testCurve <- c(0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,-19,19,-20,20)	
	testCondDrawDown <- conditionalTenPercentileDrawDown(testCurve)
	checkSame(testCondDrawDown,-19.5)
}

testTwentyPercentileDrawDown <- function() {
	testCurve <- c(0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,-19,19,-20,20)
	testDrawDown <- twentyPercentileDrawDown(testCurve)
	checkSame(testDrawDown,-17)	
}

testConditionalTwentyPercentileDrawDown <- function() {
	testCurve <- c(0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,-19,19,-20,20)	
	testCondDrawDown <- conditionalTwentyPercentileDrawDown(testCurve)
	checkSame(testCondDrawDown,-18.5)
}
testSharpeRatioDaily <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(SharpeRatioDaily),4),0.3751)	
}

testSharpeRatioWeekly <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(SharpeRatioWeekly),4),0.3699)	
}

testSharpeRatioMonthly <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(SharpeRatioMonthly),4),0.3768)	
}

testDownSideDeviation <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(DownSideDeviation),2),38715.00)
}

testCalmarRatioMonthly <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(CalmarRatioMonthly),4),0.2113)	
}

testCalmarRatioWeekly <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(CalmarRatioWeekly),4),0.1836)	
}

testConditionalTwentyPercentileCalmarRatiofunction <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(ConditionalTwentyPercentileCalmarRatio),4),0.3081)
	
	# Intraday data
	
	sto <- STO(stoDirectory(), "IntradaySTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)
	checkSame(round(curve$metric(ConditionalTwentyPercentileCalmarRatio),4),-7.1215)
}

testConditionalTenPercentileCalmarRatiofunction <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(ConditionalTenPercentileCalmarRatio),4),0.2237)	
}

testConditionalTenPercentileDailyVaRfunction <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(ConditionalTenPercentileDailyVaR),4),-60135.1024)
	
	# Intraday data
	
	sto <- STO(stoDirectory(), "IntradaySTO")
	curve <- sto$curves()$curve(first(sto$msivs()), 1)	
	checkSame(round(curve$metric(ConditionalTenPercentileDailyVaR),4),-4833883.2202)
}

testConditionalFivePercentileDailyVaRfunction <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(ConditionalFivePercentileDailyVaR),4),-74275.8387)	
}

testConditionalOnePercentileCalmarRatiofunction <- function() {
	testFile <- squish(stoDirectory(),"/CsvCurves/PnLHistory.csv")
	curve <- PositionEquityCurve(CurveFileLoader(testFile))
	checkSame(round(curve$metric(ConditionalOnePercentileDailyVaR),4),-104975.6944)	
}

