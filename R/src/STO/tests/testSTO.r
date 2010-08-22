library("STO")

source(system.file("testHelper.r", package = "STO"))

.tearDown <- .setUp <- function() {
    STO$destroy(stoDirectory(), "NewSTO")
    STO$destroy(stoDirectory(), "AnySTO")
    STO$destroy(stoDirectory(), "NonexistentSTO")
    STO$destroy(stoDirectory(), "Simple STO")
    STO$destroy(stoDirectory(), "Simple STORaggedDates")
    copyDirectory(
        squish(stoDirectory(), "/SimpleSTOTemplate"),
        squish(stoDirectory(), "/Simple STO")
    )
    copyDirectory(
        squish(stoDirectory(), "/SimpleSTORaggedDatesTemplate"),
        squish(stoDirectory(), "/Simple STORaggedDates")
    )
}

testSTOConstructor <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    checkInherits(sto, "STO")
    checkSame("Simple STO", sto$id())
    checkSame(sto$msivs(), simpleMsivs)
}

testConstructorBombsWhenBadDir <- function() { 
    shouldBombMatching(STO("/some dir that doesntexist", "Simple STO"), "invalid directory:.*/some dir that")
    shouldBombMatching(STO(stoDirectory(), "NonexistentSTO"), "invalid stoID: ")
}

testSTOCreate <- function() {
    sto <- STO$create(stoDirectory(), "NewSTO", simpleMsivs)
    checkInherits(sto, "STO")
    checkSame(sto$msivs(), simpleMsivs)
    checkTrue(file.exists(squish(stoDirectory(), "/NewSTO")))
    checkTrue(file.exists(squish(stoDirectory(), "/NewSTO/CurvesBin")))
    checkTrue(file.exists(squish(stoDirectory(), "/NewSTO/Metrics")))
    checkTrue(file.exists(squish(stoDirectory(), "/NewSTO/Trades")))
    checkTrue(file.exists(squish(stoDirectory(), "/NewSTO/Workspaces")))
}

testSTOCreateFailsInBadSituations <- function() { 
    shouldBombMatching(
        STO$create("/some bad dir", "AnySto", simpleMsivs), 
        "invalid directory.*/some bad dir"
    )
    shouldBombMatching(
        STO$create(stoDirectory(), "Simple STO", simpleMsivs), 
        "sto Simple STO already exists.  use constructor"
    )
    shouldBombMatching(
        STO$create(stoDirectory(), "NewSTO", list()),
        "no msivs"
    )
}

testSTOCanCreateParameterSpace <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    destroyDirectory(sto$dirname("Parameters"))
    shouldBombMatching(sto$parameters(), "no persisted ParameterSpace exists")
    paramSpace <- sto$parameters(ParameterSpace(p1 = c(1,2,1), p2 = c(3,5,2)))
    checkInherits(paramSpace, "ParameterSpace")    
    shouldBombMatching(
        sto$parameters(ParameterSpace(p1 = c(1,3,1))), 
        "cannot create a second parameter space over existing one"
    )
    paramSpace <- sto$parameters()
}

testSTOCanLoadCurveCubeBinary <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    curveCube <- sto$curves()
    checkInherits(curveCube, "CurveCube")
    curveCube$requireMSIVs(simpleMsivs)
    checkSame(curveCube$runNumbers(), c(1,2,3))
    firstCurve <- curveCube$curve(first(simpleMsivs), 1)
    checkPnl(firstCurve, c(1,0,-1))
    checkSame(firstCurve$dates(), c(as.POSIXct("2005/11/10"), as.POSIXct("2005/11/14"), as.POSIXct("2005/11/15")))
}

testNoTradesRun <- function() { 
    sto <- STO(stoDirectory(), "TestEmptyRunSto")
    curve <- sto$curves()
    run <- curve$curve(the(SIV("foo", "Daily", "bar")$m("blat")), 1)
    checkSame(0, kRatio(run$pnl()))
}

testSTOAddPortfolioBinary <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    port <- Portfolio("combined", sto$msivs())
    sto$add(port)
    checkSame(sto$msivs(), c(simpleMsivs, list(port)))

    sto2 <- STO(stoDirectory(), "Simple STO")
    checkSame(sto2$msivs(), c(simpleMsivs, list(port)))

    curveCube <- sto$curves()
    combinedCurve <- curveCube$curve(port, 1)
    checkPnl(combinedCurve, c(112, 222, -334))
    checkEquity(combinedCurve, c(112, 334, 0))
    checkTrue(all(is.na(combinedCurve$position())))

    curveCube2 <- sto$curves()
    checkSame(curveCube2$msivs(), curveCube$msivs())
}

testSTOAddPortfolioRaggedDatesBinary <- function() {
    sto <- STO(stoDirectory(), "Simple STORaggedDates")
    port <- Portfolio("combined", sto$msivs())
    sto$add(port)
    curveCube <- sto$curves()
    combinedCurve <- curveCube$curve(port, 1)
    checkPnl(combinedCurve, c(1,0,50,110,169,-256,-33,22,-33))
    checkEquity(combinedCurve, c(1,1,51,161,330,74,41,63,30))
    checkTrue(all(is.na(combinedCurve$position())))

    curveCube2 <- sto$curves()
    checkSame(curveCube2$msivs(), curveCube$msivs())
    combinedCurve <- curveCube2$curve(port, 1)
    checkPnl(combinedCurve, c(1,0,50,110,169,-256,-33,22,-33))
    checkEquity(combinedCurve, c(1,1,51,161,330,74,41,63,30))
}

testSTOAddMsivs <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    sto$addMsivs(simpleSiv$m(c("mkt4", "mkt5")))
    checkLength(sto$msivs(), 5)
    sto2 <- STO(stoDirectory(), "Simple STO")
    checkSame(sto$msivs(), sto2$msivs())
    shouldBombMatching(sto$addMsivs(Portfolio("combined", sto$msivs())), "not support adding Portfolios")
    shouldBombMatching(sto$addMsivs(simpleSiv$m(c("mkt4"))), "cannot add duplicate")
}

testSTOCurveCubeMissing <- function() {
    unlink(squish(stoDirectory(), "/Simple STO/CurvesBin"), recursive=TRUE)
    sto <- STO(stoDirectory(), "Simple STO")
    shouldBombMatching(sto$curves(), "no MSIVs with raw data in CurveCube")
}

testSTOHasRunNumbers <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    checkTrue(sto$hasRunNumbers())
    checkSame(sto$runNumbers(), c(1,2,3))

    unlink(squish(stoDirectory(), "/Simple STO/CurvesBin"), recursive=TRUE)
    unlink(squish(stoDirectory(), "/Simple STO/Metrics"), recursive=TRUE)
    sto <- STO(stoDirectory(), "Simple STO")
    checkFalse(sto$hasRunNumbers())
    shouldBombMatching(sto$runNumbers(), "nowhere to get run numbers")
}

testSTOLoadsMetrics <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    metricCube <- sto$metrics()
    checkSame(metricCube$oneValue(TSGrossProfit, simpleMsivs[[1]], 1), 123.4)
}


testSTOCanLoadMsivsFromDirIfFileMissing <- function() { 
    msivsFile <- squish(stoDirectory(), "/Simple STO/", "MSIVs.csv")
    checkTrue(file.remove(msivsFile))
    sto <- STO(stoDirectory(), "Simple STO")
    checkSame(sto$msivs(), simpleMsivs)
    checkTrue(file.exists(msivsFile))
}

testSTOLookupMSIVByMarket <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    checkSame(first(simpleMsivs), sto$msiv(first(simpleMsivs)$market()))
    checkSame(third(simpleMsivs), sto$msiv(third(simpleMsivs)$market()))
}

testSTOJavaCombineMarkets <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    port <- Portfolio("combined", sto$msivs())
    sto$add(port)
    curveCube <- sto$curves()
    combinedCurve <- curveCube$curve(port, 1)
    checkPnl(combinedCurve, c(112, 222, -334))
}

testSTOCurvesWithMsivListPortfolio <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    port <- Portfolio("combined", sto$msivs())
    sto$add(port)
    sto <- STO(stoDirectory(), "Simple STO")
    curves <- sto$curves(port)
}

testSTOJavaCombineMarketsPortfolioOfPortfolios <- function() {
    sto <- STO(stoDirectory(), "Simple STORaggedDates")
    port <- Portfolio("combined", sto$msivs())
    sto$add(port)
    bigger <- Portfolio("bigger", sto$msivs())
    sto$add(bigger)
    curveCube <- sto$curves()
    combinedCurve <- curveCube$curve(bigger, 1)
    checkPnl(combinedCurve, 2 * c(1,0,50,110,169,-256,-33,22,-33))
}


testSTOMetricsWithMsivList <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    metrics <- sto$metrics()
    checkSame(metrics$msivs(), simpleMsivs)
    checkSame(metrics$curveCube()$msivs(), simpleMsivs)
    msiv <- first(simpleMsivs)
    metrics <- sto$metrics(msiv)
    checkSame(metrics$msivs(), list(msiv))
    checkSame(metrics$curveCube()$msivs(), list(msiv))
    shouldBombMatching(metrics$values(TSNetProfit, second(simpleMsivs)), "ABC_1_daily_mkt2 not in cube")
    msivList <- simpleMsivs[c(1,2)]
    metrics <- sto$metrics(msivList)
    checkSame(metrics$msivs(), msivList)
    checkSame(metrics$curveCube()$msivs(), msivList)
}

testSTOCurvesWithMsivList <- function() {
    sto <- STO(stoDirectory(), "Simple STO")
    curves <- sto$curves()
    checkSame(curves$msivs(), simpleMsivs)
    msiv <- first(simpleMsivs)
    curves <- sto$curves(msiv)
    checkSame(curves$msivs(), list(msiv))
    shouldBombMatching(curves$curve(second(simpleMsivs), 1), "ABC_1_daily_mkt2 not in cube")
    msivList <- simpleMsivs[c(1,2)]
    curves <- sto$curves(msivList)
    checkSame(curves$msivs(), msivList)
}

testSTOMetricIntervals <- function() {
    sto <- STO(stoDirectory(), "SimpleManyDatesSTO")
    metrics <- sto$metrics()
    checkSame(metrics$oneValue(NetProfit, first(simpleMsivs), 1), 950)
    checkSame(metrics$oneValue(MaxDrawDown, first(simpleMsivs), 1), 500 * -10)
    checkSame(metrics$oneValue(AverageDrawDownTime, first(simpleMsivs), 1), 1000)
    checkSame(metrics$oneValue(AverageDrawDownRecoveryTime, first(simpleMsivs), 1), 500)

    monthlyMetrics <- sto$metrics(interval=Interval$MONTHLY)
    checkSame(monthlyMetrics$oneValue(NetProfit, first(simpleMsivs), 1), 950)
    checkSame(monthlyMetrics$oneValue(MaxDrawDown, first(simpleMsivs), 1), -4730)
    checkSame(monthlyMetrics$oneValue(AverageDrawDownTime, first(simpleMsivs), 1), 32)
    checkSame(monthlyMetrics$oneValue(AverageDrawDownRecoveryTime, first(simpleMsivs), 1), 16)
}

testSTOMetricRange <- function() {
    sto <- STO(stoDirectory(), "SimpleManyDatesSTO")
    metrics <- sto$metrics(range=Range("2005/01/01", "2005/01/20"))
    checkSame(metrics$oneValue(NetProfit, first(simpleMsivs), 1), 0)
    checkSame(metrics$oneValue(MaxDrawDown, first(simpleMsivs), 1), -100)
}

testSTOMetricIntervalAndRange <- function() {
    sto <- STO(stoDirectory(), "SimpleManyDatesSTO")
    metrics <- sto$metrics(interval=Interval$MONTHLY, range=Range$after("2005/01/10"))
    checkSame(metrics$oneValue(NetProfit, first(simpleMsivs), 1), 850)
    checkSame(metrics$oneValue(MaxDrawDown, first(simpleMsivs), 1), -4730)
}

testSTOWithPartialCurveCube <- function() {
    sto <- STO(stoDirectory(), "Simple STO", calculateMetrics = FALSE)
    file.remove(paste(sto$.curveDir(), "/ABC_1_daily_mkt" , c(1,2,3), "/run_1.bin", sep=""))
    metrics <- sto$metrics()
    checkSame(metrics$runNumbers(), c(1,2,3))
    curves <- sto$curves()
    checkSame(curves$runNumbers(), c(2,3))
}



