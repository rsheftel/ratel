library(QFPortfolio)


testPnLReport <- function() {
    testDir <- system.file("testdata/PortfolioEquityCurves", package="QFPortfolio")
    curves <- SystemCurveLoader$systemCurves(testDir, "PortfolioMS_test.csv")

    report <- PortfolioPnLReport(textConnection("output", "w", TRUE), curves, start="2007-07-01",end="2008-05-30")
    weights <- c(1,1,1)
    report$report(weights)
    checkSame(output[2],"NDayBreak,1 ")
    checkSame(output[10],"2007-09-01,276011.25,0,7323.47")
    checkSame(output[18],"2008-05-01,-15163.75,0,0")

}
