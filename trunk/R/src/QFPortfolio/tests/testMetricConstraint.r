library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

sto <- STO(stoDirectory(), "SimpleSTOTemplate")
curve.cube <- sto$curves()
curve1 <- curve.cube$curve(first(simpleMsivs), 1)
curve2 <- curve.cube$curve(first(simpleMsivs), 2)
curve3 <- curve.cube$curve(first(simpleMsivs), 3)

testMetricConstraint <- function() {

    c <- MetricConstraint(NetProfit, 5, 10)
    checkFalse(c$isViable(curve1))
    checkTrue(c$isViable(curve2))
    checkFalse(c$isViable(curve3))
	
	checkTrue(c$distance(curve1)>0)
	checkTrue(c$distance(curve2)==0)
	checkTrue(c$distance(curve3)>0)
	
	
    c <- MetricConstraint(NetProfit, 20, 100)
    checkFalse(c$isViable(curve1))
    checkFalse(c$isViable(curve2))
    checkTrue(c$isViable(curve3))

    checkTrue(c$distance(curve1)>0)
	checkTrue(c$distance(curve2)>0)
	checkTrue(c$distance(curve3)==0)
	
	shouldBombMatching(MetricConstraint(NetProfit, 100, 1), ":must conform to min <= max")
}

testOneBound <- function() {
    c <- MetricConstraint$lessThanOrEqual(NetProfit, 20)
    checkTrue(c$isViable(curve1))
    checkTrue(c$isViable(curve2))
    checkFalse(c$isViable(curve3))

    c <- MetricConstraint$greaterThanOrEqual(NetProfit, 20)
    checkFalse(c$isViable(curve1))
    checkFalse(c$isViable(curve2))
    checkTrue(c$isViable(curve3))
}

testOutputFunctions <- function() {
	c <- MetricConstraint(NetProfit, 5, 10)
	target <- 'NetProfit: (5, 10)'
	checkSame(target, c$as.character())
	target <- data.frame(MC.MetricName = 'NetProfit', MC.Min = 5, MC.Max = 10)
	checkSame(target, c$as.data.frame())
	
	checkSame(c$metric(), NetProfit)
	checkSame(c$minimum(), 5)
	checkSame(c$maximum(), 10) 
}
