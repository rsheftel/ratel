library(QFPortfolio)

testDir <- system.file("testdata/PortfolioEquityCurves", package="QFPortfolio")

testWFO <- function() {
    metric <- NetProfit

    range <- Range("2004/01/01", "2004/03/01")

    curves <- list(
        FV = PositionEquityCurve(CurveFileLoader(squish(testDir, "/NDayBreak_1.0_daily_BFBD20_FV.1C.csv"))),
        AD = PositionEquityCurve(CurveFileLoader(squish(testDir, "/NDayBreak_1.0_daily_FXBD20_AD.1C.csv"))),
        CL = PositionEquityCurve(CurveFileLoader(squish(testDir, "/SHORT_1.0_daily_NRGBD40_CL.1C.csv")))
    );

    groups <- names(curves)
    optimizer <- PretendOptimizer(curves)
    
    start <- as.POSIXct("2004-01-01")
    end <- as.POSIXct("2004-03-01")
    frequency <- Period$months(1)
    window <- RollingWindow(Period$months(2))
    
    wfo <- WalkForwardOptimizer(optimizer, window = window, frequency = frequency)
    weights <- wfo$optimize(startDate = start, endDate = end, seed = c(1,1,1), fastRestarts = 1, fastTol = 0.001, slowRestarts = 0, saveWeights = TRUE)
    checkLength(optimizer$.ranges, 3)
    expectedDates <- as.POSIXct(c("2004-01-01", "2004-02-01", "2004-03-01"))
    expected <- Weights(zoo(matrix(c(1,1,NA,3,3,NA,5,5,5), nrow=3, byrow=TRUE), order.by = expectedDates))
    checkSame(weights, expected)
	checkSame(wfo$savedWeights(), expected)
}


slowtestWFOSlowly <- function() {
    metric <- NetProfit

    range <- Range("2004/01/01", "2004/03/01")

    curves <- list(
        FV = PositionEquityCurve(CurveFileLoader(squish(testDir, "/NDayBreak_1.0_daily_BFBD20_FV.1C.csv"))),
        AD = PositionEquityCurve(CurveFileLoader(squish(testDir, "/NDayBreak_1.0_daily_FXBD20_AD.1C.csv"))),
        CL = PositionEquityCurve(CurveFileLoader(squish(testDir, "/SHORT_1.0_daily_NRGBD40_CL.1C.csv")))
    );

    groups <- names(curves)
    optimizer <- PortfolioOptimizer(metric, curves, lower = c(0,0,0), upper = c(2,2,2))
    
    start <- as.POSIXct("2004-01-01")
    end <- as.POSIXct("2004-03-01")
    frequency <- Period$months(1)
    window <- RollingWindow(Period$months(2))
    
    wfo <- WalkForwardOptimizer(optimizer, window = window, frequency = frequency)
    weights <- wfo$optimize(startDate = start, endDate = end, seed = c(1,1,1), fastRestarts = 1, fastTol = 0.001, slowRestarts = 0)
    weightsZoo <- weights$as.zoo()
    print(weightsZoo)
    expectedDates <- as.POSIXct(c("2004-01-01", "2004-02-01", "2004-03-01"))
    expectedUpper <- zoo(matrix(c(0.8, 2, NA, 2, 2, NA, 2, 0.8, 2), nrow=3, byrow=TRUE), order.by=expectedDates)
    expectedLower <- zoo(matrix(c(0, 1.4, NA, 1.4, 1.4, NA, 1.4, 0, 1.4), nrow=3, byrow=TRUE), order.by=expectedDates)
    checkValuesHigherOrNaMatch(weightsZoo, expectedLower)
    checkValuesHigherOrNaMatch(expectedUpper, weightsZoo)
    checkSame(expectedDates, index(weightsZoo))
}

checkValuesHigherOrNaMatch <- function(higher, lower) {
    higher <- as.vector(higher)
    lower <- as.vector(lower)
    checkTrue(all(is.na(higher) == is.na(lower)))
    checkTrue(all(higher[!is.na(higher)] > lower[!is.na(lower)]))
}