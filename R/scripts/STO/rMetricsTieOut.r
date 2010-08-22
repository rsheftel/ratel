library(STO)



curve1 <- PositionEquityCurve(CurveFileLoader("C:/svn/R/src/QFPortfolio/inst/testdata/FuncTestCurves/curve_NDayBreak_1_daily_AllFutures.csv"))
curve2 <- PositionEquityCurve(CurveFileLoader("C:/svn/R/src/QFPortfolio/inst/testdata/PortfolioEquityCurves/NDayBreak_1.0_daily_BFBD30_FV.1C.csv"))
curve3 <- PositionEquityCurve(CurveFileLoader("C:/SVN/R/src/STO/inst/testdata/IntradaySTO/CurvesBin/CVE_10_Daily_CET10.AEP5M/run_1.bin"))



metrics <- c(
    KRatio =  1e-10,
    NetProfit =  1e-5,
    MaxDrawDown =  1e-5,
    AnnualizedNetProfit = NA,
    CalmarRatio =  1e-2,
    ConditionalTenPercentileCalmarRatio =  1e-2,
    ConditionalTwentyPercentileCalmarRatio =  1e-2,
    AverageDrawDown =  1e-5,
    AverageDrawDownTime =  1e-2,
    AverageDrawDownRecoveryTime =  1e-2,
    TenPercentileDrawDown =  1e-5,
    ConditionalTenPercentileDrawDown =  1e-5,
    TwentyPercentileDrawDown =  1e-5,
    ConditionalTwentyPercentileDrawDown =  1e-5,
    SortinoRatio =  1e-2,
    OmegaRatio =  1e-10,
    UpsidePotentialRatio =  1e-10,
    DownSideDeviation =  1e-8,
    DailyStandardDeviation =  1e-8,
    WeeklyStandardDeviation =  1e-8,
    MonthlyStandardDeviation =  1e-7,
    SharpeRatioDaily =  1e-2,            
    SharpeRatioWeekly =  1e-2,
    SharpeRatioMonthly =  1e-2
)

metricValues <- lapply(names(metrics), function(metric) {
    data.frame(
        metricName = metric, 
        value1 = curve1$metric(Metric$fetch(metric)), tol1 = metrics[[metric]],
        value2 = curve2$metric(Metric$fetch(metric)), tol2 = metrics[[metric]],
        value3 = curve3$metric(Metric$fetch(metric)), tol3 = metrics[[metric]]
    )
})

result <- first(metricValues)
for(row in 2:NROW(metricValues)) {
    result <- rbind(result,metricValues[[row]])
}
rownames(result) <- names(metrics)

result[["AnnualizedNetProfit", "tol1"]] <- 1e-5
result[["AnnualizedNetProfit", "tol2"]] <- 5
result[["AnnualizedNetProfit", "tol3"]] <- 120000

write.csv(result, file = "C:/svn/R/scripts/STO/rMetricsTieOut.csv", row.names=FALSE)