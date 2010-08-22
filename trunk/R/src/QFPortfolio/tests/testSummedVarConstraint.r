library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

testSummedVarConstraint <- function() {

    c <- SummedVarConstraint(c('FX', 'FX', 'Directional'),summedAlpha=0.1,varBudget=16.065)
    checkInherits(c, c("SummedVarConstraint", "WeightedCurvesConstraint"))
    c$set('FX', 0, .70)
    c$set('Directional', 0, .35)

    dir <- system.file("testdata/IVarCurves", package="QFPortfolio")
    curve1 <- ZooCurveLoader$fromFile(squish(dir, "/a.csv"))
    curve2 <- ZooCurveLoader$fromFile(squish(dir, "/b.csv"))
    curve3 <- ZooCurveLoader$fromFile(squish(dir, "/c.csv"))
    curves <- list(curve1, curve2, curve3)
	
	checkSame(c$distance(WeightedCurves(curves, c(1,1,1))),0)
	checkSame(round(c$distance(WeightedCurves(curves, c(7,4,3))),6),1.946605)
	
	target <- 'Summed VaR Constraint\nSummed Var:Directional max= 0.35 min=0\nSummed Var:FX max= 0.7 min=0\n'
	checkSame(target, c$as.character())
	
	target <- data.frame(SVC.SummedAlpha = 0.1, SVC.VarBudget = 16.065, SVC.Directional.Min = 0, SVC.Directional.Max = 0.35, SVC.FX.Min = 0, SVC.FX.Max = 0.7)
	checkSame(target, c$as.data.frame())
 }
 
testSummedVarCalc <- function() {   
    summedVaRCon <- SummedVarConstraint(c('FX', 'FX', 'Directional'),summedAlpha=0.1,varBudget=16.065)
    summedVaRCon$set('FX', 0, .25)
    summedVaRCon$set('Directional', 0, .8)
    list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10"))

    list.values.a <- c(2.1,2.5,3.3,4.7,5.0,6.1,7.3,8.3,9.2,1.2)
    list.values.b <- c(5.2,6.4,7.7,8.5,9.3,10.3,11.2,12.05,13.1,3.3)
    list.values.c <- c(3.005,4.8,5.2,6.3,7.0,8.9,9.9,10.6,11.6005,2.01)

    weights <- c(3.7,2,2.5)
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

    results <- summedVaRCon$.calcSummedVar(wc)
    checkEquals(results[[1]]+results[[2]]+results[[3]],1)

    checkEquals(round(results[[1]],5),0.27638)
    checkEquals(round(results[[2]],5),0.41083)
    checkEquals(round(results[[3]],5),0.31279)   
 }
 

