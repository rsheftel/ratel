library(QFPortfolio)

slowtestIansTest <- function() {
    dirName <- squish(dataDirectory(), "/STProcess/Portfolio/Curves/20080527")
    outputFileName <- squish(homeDirectory(), "/OptResults.csv")
    metric <- CalmarRatio
    aum <- 113000000

    range <- Range("2004-01-03", "2008-05-30")
#    systemCurves <- CurveGroup("AllSystems")$childCurves(dirName, extension = "csv", range = range)
    systems <- c("NDayBreak", "DTDRichCheap", "FXCarry", "CVE", "NDayBreakCloseMinVol")
    systemCurves <-sapply(systems, function(s) CurveGroup(s)$curve(dirName, extension = "csv", range = range), simplify=FALSE)
    couponSwap <- PositionEquityCurve(CurveFileLoader(squish(dirName, "/MortgageCouponSwap_1.0_daily_FNCLALL_FNCL.csv")), range = range)
    systemCurves <- appendSlowly(systemCurves, couponSwap)
    names(systemCurves)[6] <- "MortgageCouponSwap"

    systems <- names(systemCurves)
    optimizer <- PortfolioOptimizer(metric, systemCurves, lower = c(0,0,0,0,0,0), upper = c(10,1.6667,100,1,0.5,10))
    iVarConstraint <- IncrementalVarConstraint(systems, incrementalAlpha = 0.05, overallAlpha = 0.01, overallVarRange = c(0.0125, 0.020) * -aum)
    for(system in systems) iVarConstraint$set(system, -0.40, 0.40)
    optimizer$constrainWeightedCurves(iVarConstraint)

    report <- OptimizationReport(file(outputFileName, "w"), systemCurves, iVarConstraint)
    report$current(c(1,1.667,20,1,0.025,1))

    optimalWeights <- optimizer$optimize(start = c(2.4,0.9,65,0.95,0.1,1.8))

    report$optimal(optimalWeights, metric)
}

slowtestWFO <- function() {
    dirName <- squish(dataDirectory(), "/STProcess/Portfolio/Curves/20080527")
    outputFileName <- squish(homeDirectory(), "/OptResults.csv")
    metric <- CalmarRatio
    aum <- 113000000

    start <- as.POSIXct("2006-05-30")
    end <- as.POSIXct("2008-05-30")
#    systemCurves <- CurveGroup("AllSystems")$childCurves(dirName, extension = "csv", range = range)
    systems <- c("NDayBreak", "DTDRichCheap", "FXCarry", "CVE", "NDayBreakCloseMinVol")
    systemCurves <-sapply(systems, function(s) CurveGroup(s)$curve(dirName, extension = "csv"), simplify=FALSE)
    couponSwap <- PositionEquityCurve(CurveFileLoader(squish(dirName, "/MortgageCouponSwap_1.0_daily_FNCLALL_FNCL.csv")))
    systemCurves <- appendSlowly(systemCurves, couponSwap)
    names(systemCurves)[6] <- "MortgageCouponSwap"

    systems <- names(systemCurves)
    optimizer <- PortfolioOptimizer(metric, systemCurves, lower = c(0,0,0,0,0,0), upper = c(10,1.6667,100,1,0.5,10))
    iVarConstraint <- IncrementalVarConstraint(systems, incrementalAlpha = 0.05, overallAlpha = 0.01, overallVarRange = c(0.0125, 0.020) * -aum)
    for(system in systems) iVarConstraint$set(system, -0.40, 0.40)
    optimizer$constrainWeightedCurves(iVarConstraint)

#    report <- OptimizationReport(file(outputFileName, "w"), systemCurves, iVarConstraint)
#    report$current(c(1,1.667,20,1,0.025,1))

    wfo <- WalkForwardOptimizer(optimizer, RollingWindow(Period$months(24)), Period$months(12))
    optimalWeights <- wfo$optimize(start = start, end = end, seed = c(2.4,0.9,65,0.95,0.1,1.7))
    print(optimalWeights$as.zoo())
#    report$optimal(optimalWeights, metric)
}

slowtestEricsTest <- function() {
    dirName <- squish(dataDirectory(), "/STProcess/Portfolio/Curves/20080527")
    outputFileName <- squish(homeDirectory(), "/OptResults.NDayBreak.csv")
    metric <- CalmarRatio
    aum <- 113000000

    range <- Range("2004-01-03", "2008-05-30")
    systemCurves <- CurveGroup("NDayBreak")$childCurves(dirName, extension = "csv", range = range)
    systems <- names(systemCurves)

    optimizer <- PortfolioOptimizer(metric, systemCurves, lower = rep(0, length(systems)), upper = rep(10, length(systems)))
    iVarConstraint <- IncrementalVarConstraint(systems, incrementalAlpha = 0.05, overallAlpha = 0.01, overallVarRange = c(0, 0.020) * -aum)
    for(system in systems) iVarConstraint$set(system, -0.40, 0.40)
    optimizer$constrainWeightedCurves(iVarConstraint)

    optimizer$optimize(start = rep(1, length(systems)))

}

