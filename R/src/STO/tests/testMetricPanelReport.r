library(STO)

source(system.file("testHelper.r", package = "STO"))

.tearDown <- .setUp <- function() {
    STO$destroy(stoDirectory(), "SurfaceSTOInstance")
    copyDirectory(
        squish(stoDirectory(), "/SurfaceSTO"),
        squish(stoDirectory(), "/SurfaceSTOInstance")
    )
}


testReportByFilter <- function() {
    sto <- STO(stoDirectory(), "SurfaceSTOInstance")
    generator <- MetricPanelReport(sto, list(NetProfit), first(sto$msivs()))
    parameters <- sto$parameters()
    filters <- parameters$filtersExact(a = c(1,2,3), b = c(6,7))
    checkLength(filters, 6)
    generator$runReport("metricsByAB", filters, openInBrowser=FALSE)
    checkTrue(file.exists(sto$dirname("html/metricsByAB/reportByFilter.html")))
    checkTrue(file.exists(sto$dirname("html/metricsByAB/data/byFilter.csv")))
    # shouldn't bomb
    generator$runReport("metricsByAB", filters, openInBrowser=FALSE)
#    filters <- parameters$filtersRange(c = c(-5,10,10))
#    checkLength(filters, 2)
#    generator$runReport("metricsByCRange", filters)
}

testReportByMetric <- function() {
    sto <- STO(stoDirectory(), "SurfaceSTOInstance")
    generator <- MetricPanelReport(sto, list(NetProfit), first(sto$msivs()))
    parameters <- sto$parameters()
    filters <- parameters$filtersExact(a = c(1,2,3), b = c(6,7))
    checkLength(filters, 6)
    generator$runReport("metricsByAB", filters, openInBrowser=FALSE)
    checkTrue(file.exists(sto$dirname("html/metricsByAB/reportByMetric.html")))
    checkTrue(file.exists(sto$dirname("html/metricsByAB/data/byMetric.csv")))
}

testCanSupplyMetricCube <- function() {
    sto <- STO(stoDirectory(), "SurfaceSTOInstance")
    metric.cube <- sto$metrics()
    generator <- MetricPanelReport(sto, list(NetProfit), first(sto$msivs()))
    checkFalse(getInternalAddress(generator$.metricCube) == getInternalAddress(metric.cube))
    generator <- MetricPanelReport(sto, list(NetProfit), first(sto$msivs()), metric.cube)
    checkTrue(getInternalAddress(generator$.metricCube) == getInternalAddress(metric.cube))
}
