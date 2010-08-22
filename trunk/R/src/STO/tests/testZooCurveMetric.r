library(STO)

testMetricsFromEquityCurve <- function() {
    equity <- zoo(c(2, 1, 1, 0, 1, 2, 1, 2, 3, 4, 5, 6, 5, 3, 4, 7, 2), 1:17)
    curve <- ZooCurveLoader$fromEquity(equity, "test")
    shouldBombMatching(curve$metric(TSNetProfit), "msiv is not Portfolio")
    checkEquals(2, curve$metric(NetProfit))
    checkEquals(-2.75, curve$metric(AverageDrawDown))
    checkEquals(11/3, curve$metric(AverageDrawDownTime))
    checkEquals(5/3, curve$metric(AverageDrawDownRecoveryTime))
	
	#NOTE: the $fromEquity() assumes that prior to the first entry in the zoo, the equity was zero
	#So this means that the $pnl() for the first date will be the same as the $equity() for the first date
	#This may require you to adjust the entire zoo to get the "silent" equity entry to zero. 
}
