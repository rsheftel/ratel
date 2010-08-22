library(STO)

testCSVBinFiles <- function() {
    csvFile <- system.file("testdata/FuncTestCurves/curve_CASH.csv", package="QFPortfolio")
    binFile <- system.file("testdata/SimpleCurves/ABC_1_daily_mkt1/run_1.bin", package="STO")

    csvCurve <- PositionEquityCurve(CurveFileLoader(csvFile))
    checkLength(csvCurve$pnl(), 51)

    binCurve <- PositionEquityCurve(CurveFileLoader(binFile))
    checkLength(binCurve$pnl(), 3)

}
