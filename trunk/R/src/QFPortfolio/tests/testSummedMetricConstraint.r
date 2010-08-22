library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

testSummedMetricConstraintWithBudget <- function() {

	metric <- Metric$fetch("DailyStandardDeviation")
    c <- SummedMetricConstraint(c('FX', 'FX', 'Directional'),summedMetric=metric,metricBudget=10)
    checkInherits(c, c("SummedMetricConstraint", "WeightedCurvesConstraint"))
    c$set('FX', 0, 0.65)
    c$set('Directional', 0, 0.36)

    dir <- system.file("testdata/IVarCurves", package="QFPortfolio")
    curve1 <- ZooCurveLoader$fromFile(squish(dir, "/a.csv"))
    curve2 <- ZooCurveLoader$fromFile(squish(dir, "/b.csv"))
    curve3 <- ZooCurveLoader$fromFile(squish(dir, "/c.csv"))
    curves <- list(curve1, curve2, curve3)
	
	checkSame(c$distance(WeightedCurves(curves, c(1,1,1))),0)
	checkSame(round(c$distance(WeightedCurves(curves, c(2,3,3))),6),4.474934)
	
	target <- 'Summed Metric Constraint\nSummed Metric:Directional max= 0.36 min=0\nSummed Metric:FX max= 0.65 min=0\n'
	checkSame(target, c$as.character())
	
	target <- data.frame(SMC.Metric = "DailyStandardDeviation", SMC.MetricBudget = 10, SMC.Directional.Min = 0, SMC.Directional.Max = 0.36, SMC.FX.Min = 0, SMC.FX.Max = 0.65)
	checkSame(target, c$as.data.frame())
 }
 
testSummedMetricCalc <- function() {   
    metric <- Metric$fetch("DailyStandardDeviation")
	summedMetricCon <- SummedMetricConstraint(c('FX', 'FX', 'Directional'),summedMetric=metric,metricBudget=24.10463)
    summedMetricCon$set('FX', 0, .25)
    summedMetricCon$set('Directional', 0, .8)
    list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10"))

    list.values.a <- c(2.1,2.5,3.3,4.7,5.0,6.1,7.3,8.3,9.2,1.2)
    list.values.b <- c(5.2,6.4,7.7,8.5,9.3,10.3,11.2,12.05,13.1,3.3)
    list.values.c <- c(3.005,4.8,5.2,6.3,7.0,8.9,9.9,10.6,11.6005,2.01)

    weights <- c(3,2,3)
    valuesToCurve <- function(values, name) { 
        pos <- zoo(NA, list.dates)
        pnl <- cbind(zoo(values, list.dates), pos)
        colnames(pnl) <- c("pnl", "position")
        ZooCurveLoader$fromPnl(pnl, name)
    }
    curves <- list(
        valuesToCurve(list.values.a, "a"),
        valuesToCurve(list.values.b, "b"),
        valuesToCurve(list.values.c, "c")
    )

    wc <- WeightedCurves(curves, weights)

    results <- summedMetricCon$.calcSummedMetric(wc)
    checkEquals(round(results[[1]]+results[[2]]+results[[3]],6),1)

    checkEquals(round(results[[1]],5),0.33848)
    checkEquals(round(results[[2]],5),0.25767)
    checkEquals(round(results[[3]],5),0.40386)   
 }
 

