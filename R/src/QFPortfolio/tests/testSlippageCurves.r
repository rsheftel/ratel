library(QFPortfolio)


testSlippageCurves <- function() {
    testDir <- system.file("testdata/PortfolioEquityCurves", package="QFPortfolio")
    curves <- SystemCurveLoader$systemCurves(testDir, "PortfolioMS_test.csv")

	testCurveOne <- curves[[1]]
	testPnLOne <- testCurveOne$pnl()
	
	Slippage <- SlippageCurves(curves=curves, slippage=c(0.5,0.5,0.5))
	newCurves <- Slippage$slippageAdjustedCurves()
	
	test.date <- as.POSIXct("2007-09-04")
	
	testCurveTwo <- newCurves[[1]]
	testPnLTwo <- testCurveTwo$pnl()
	
	checkSame(testPnLOne[test.date]-0.5*abs(testPnLOne[test.date]),testPnLTwo[test.date])
    

}
