library(QFPortfolio)

testReport <- function() {
    testDir <- system.file("testdata/PortfolioEquityCurves", package="QFPortfolio")
    curves <- SystemCurveLoader$systemCurves(testDir, "PortfolioMS_test.csv")
    iVarConstraint <- IncrementalVarConstraint(names(curves), incrementalAlpha = 0.05, overallAlpha = 0.01, overallVarRange = c(0.0125, 0.020) * -100)

    report <- OptimizationReport(textConnection("output", "w", TRUE), curves, iVarConstraint)
    report$current(c(1,1,1))
    checkMatches(output[[2]], "Current Portfolio Weightings")
    checkMatches(output[[4]], "NDayBreak , 1")
    checkMatches(output[[8]], "AnnualizedNetProfit , 692594.7 ")
    checkMatches(output[[18]], "AnnualizedNetProfit , NDayBreak , 0.9892813 ")

    report$optimal(c(2,2,2), CalmarRatio)
    checkMatches(output[[58]], " Result of Optimization: CalmarRatio")
    checkMatches(output[[60]], "NDayBreak , 2 ")
    checkMatches(output[[65]], "CalmarRatio , 0.7151288")
    checkMatches(output[[106]], "NDayBreak  Value, -300802")

}
