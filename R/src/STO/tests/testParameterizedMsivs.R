library(STO)

source(system.file("testHelper.r", package = "STO"))

.tearDown <- .setUp <- function() {
    STO$destroy(stoDirectory(), "SimpleSTO")
    copyDirectory(
        squish(stoDirectory(), "/SimpleSTOTemplate"),
        squish(stoDirectory(), "/SimpleSTO")
    )
}

testParameterizedMsivs <- function() {
    sto <- STO(stoDirectory(), "SimpleSTO")
    shouldBombMatching(ParameterizedMsivs(simpleMsivs, weights = c(1,1,2), runs = c(3, 2)), "expected length of runs was 3 but got 2")
    pm <- ParameterizedMsivs(simpleMsivs, weights = c(1,1,2), runs = c(3,2,1))
    checkInherits(pm, "ParameterizedMsivs")
    curve.cube <- sto$curves()
    curve <- pm$curve(curve.cube)
    checkInherits(curve, "PositionEquityCurve")
    checkSameLooking(curve$equity(), c(39,106,69))
    metric.cube <- sto$metrics()
    metrics <- pm$metrics(metric.cube, list(NetProfit, TSNetProfit))
    checkTrue(isType(metrics, "Map(Metric, numeric)"))
    checkSame(metrics$fetch(NetProfit), 69)
    checkSame(metrics$fetch(TSNetProfit), 150)

    # Other idea...
    # pp <- port$withRun(2)  # Shortcut to set all markets to same run
}

testDuplicatedMarketInParameterizedMsivs <- function() {
    sto <- STO(stoDirectory(), "SimpleSTO")
    msiv <- first(simpleMsivs)
    pm <- ParameterizedMsivs(list(msiv, msiv, msiv), weights = c(1,1,2), runs = c(3,3,1))
    curve.cube <- sto$curves()
    curve <- pm$curve(curve.cube)
    checkSameLooking(curve$equity(), c(22,52,90))
    metric.cube <- sto$metrics()
    metrics <- pm$metrics(metric.cube, list(NetProfit, TSNetProfit))
    checkSame(metrics$fetch(TSNetProfit), 300)
    checkSame(metrics$fetch(NetProfit), 90)
}
