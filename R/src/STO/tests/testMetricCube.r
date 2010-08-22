library("STO")

source(system.file("testHelper.r", package = "STO"))

.setUp <- .tearDown <- function() {
    unlink(recursive=TRUE, squish(dataDir("SimpleCurvesRaggedDates"), "/ABC_1_daily_combined"))
    unlink(recursive=TRUE, squish(dataDir("SimpleCurves"), "/ABC_1_daily_combined"))
    unlink(recursive=TRUE, squish(dataDir("Curves"), "/NDayBreak_1_daily_PORTFOLIO"))
    STO$destroy(stoDirectory(), "SimpleSTO")
    copyDirectory(
        squish(stoDirectory(), "/SimpleSTOTemplate"),
        squish(stoDirectory(), "/SimpleSTO")
    )
}

testMetricCube <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    checkInherits(cube, "MetricCube")
    cube$load(dataDir("SimpleMetrics"))
    checkSame(cube$oneValue(TSGrossProfit, simpleMsivs[[1]], 1), 123.4)
    checkSame(cube$oneValue(TSAvgBarsEvenTrade, simpleMsivs[[2]], 3), 77)
    available <- cube$availableMetrics()
    checkLength(available, 21)
    checkInherits(first(available), "Metric")
    checkInherits(last(available), "Metric")
    cube$calculate(TSNetProfit, first(simpleMsivs))
    metricNamesFirstMsiv <- sapply(cube$availableMetrics(first(simpleMsivs)), as.character)
    metricNamesSecondMsiv <- sapply(cube$availableMetrics(second(simpleMsivs)), as.character)
    checkTrue("TSNetProfit" %in% metricNamesFirstMsiv)
    checkFalse("TSNetProfit" %in% metricNamesSecondMsiv)
}

testMetricCubeNetProfit <- function() {
    sto <- STO(stoDirectory(), "SimpleSTO")
    combined <- Portfolio("combined", simpleMsivs, c(1,1,2))
    sto$add(combined)

    cube <- sto$metrics()
    checkInherits(cube, "MetricCube")
    checkSame(cube$oneValue(NetProfit, simpleMsivs[[1]], 3), 45)
    checkSame(cube$oneValue(NetProfit, combined, 2), -21)
}

testMetricCubeMaxDrawDown <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    checkSame(cube$oneValue(MaxDrawDown, simpleMsivs[[1]], 3), 0)
    checkSame(cube$oneValue(MaxDrawDown, simpleMsivs[[3]], 1), -33)
}

testMetricCubePortfolio <- function() {
    curve <- simpleCube()
    combined <- Portfolio("combined", simpleMsivs, c(1,1,2))
    curve$addMarket(combined)

    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    cube$calculate(TSGeneratedMetrics)
    checkSame(cube$oneValue(TSGrossProfit, combined, 1), 346.5)
    checkSame(cube$oneValue(TSGrossLoss, combined, 1), -120.0)
    checkSame(cube$oneValue(TSGrossLoss, combined, 3), -2.0)
    checkTrue(is.na(cube$oneValue(TSMaxIDDrawDown, combined, 1)))
    checkSame(cube$oneValue(TSTotalBarsWinTrades, combined, 2), 19.0)
    checkSame(cube$oneValue(TSAvgBarsEvenTrade, combined, 2), 3.0)
    checkSame(cube$oneValue(TSLargestLosTrade, combined, 3), -10000.10)
    checkSame(cube$oneValue(TSLargestWinTrade, combined, 3), 19752)
    checkSame(cube$oneValue(TSNetProfit, simpleMsivs[[2]], 3), 400)
    checkSame(cube$oneValue(TSNetProfit, combined, 3), 1250)
}

testMetricCubeLargestWinLossTrade <- function() {
    curve <- simpleCube()
    combined <- Portfolio("combined", simpleMsivs, c(20000,1,2))
    curve$addMarket(combined)

    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    cube$calculate(TSGeneratedMetrics)
    checkSame(cube$oneValue(TSLargestLosTrade, combined, 3), -49000)
    checkSame(cube$oneValue(TSLargestWinTrade, combined, 3), 24680000)
}

testMetricCubeReal <- function() {
    curve <- realDataCube()
    cube <- curve$metrics()
    checkInherits(cube, "MetricCube")
    cube$load(dataDir("Metrics"))
    checkSame(cube$oneValue(TSMaxContractsHeld, msivFV, 5), 5391)
    checkSame(cube$oneValue(TSGrossProfit, msivTY, 1), 34831468.7500)
}

testMetricWriteCSV <- function() {
    curve <- simpleCube()
    combined <- Portfolio("combined", simpleMsivs, c(1,1,2))
    curve$addMarket(combined)
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    cube$calculate(TSGeneratedMetrics, combined)
    dir <- squish(tempdir(), "/metricWriteCSVTest")
    unlink(dir, recursive=TRUE)
    dir.create(dir)
    cube$writeCSV(dir)
    new.cube <- curve$metrics()
    new.cube$load(dir)
    checkSame(cube, new.cube)
   
    unlink(dir, recursive=TRUE)
    dir.create(dir)
    new.cube$writeCSV(dir, first(cube$msivs()))
    checkLength(list.files(dir), 1)
    cube$calculate(TSNetProfit, combined)
    new.cube$writeCSV(dir, combined)
    checkLength(list.files(dir), 2)
}

testMetricValuesRunFilter <- function() {
    curve <- simpleCube()
    cube <- curve$metrics()
    cube$load(dataDir("SimpleMetrics"))
    filter <- RunFilter$with("test", c(1,2))
    values <- cube$values(TSGrossLoss, first(simpleMsivs), filter)
    checkLength(values, 2)
    checkSame(cube$values(TSGrossLoss, first(simpleMsivs))[1:2], values)
    checkSame(cube$values(TSGrossLoss, first(simpleMsivs)), cube$values(TSGrossLoss, first(simpleMsivs), RunFilter$with("test", 1:3)))
    checkSame(cube$values(TSGrossLoss, first(simpleMsivs))[c(2,1)], cube$values(TSGrossLoss, first(simpleMsivs), RunFilter$with("test", c(2,1))))
}



