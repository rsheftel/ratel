library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

testWeightConstraint <- function() {

    c <- WeightConstraint(c('FXCarry', 'NDayBreak', 'LiqInj'))
    checkInherits(c, c("WeightConstraint", "WeightedCurvesConstraint"))
    c$set('FXCarry', 0, 1.20)
    c$set('NDayBreak', 0, 0.97)
	c$set('LiqInj',0,5.67)
	
	dir <- system.file("testdata/IVarCurves", package="QFPortfolio")
	curve1 <- ZooCurveLoader$fromFile(squish(dir, "/a.csv"))
	curve2 <- ZooCurveLoader$fromFile(squish(dir, "/b.csv"))
	curve3 <- ZooCurveLoader$fromFile(squish(dir, "/c.csv"))
	curves <- list(curve1, curve2, curve3)
	
	checkTrue(round(c$distance(WeightedCurves(curves,c(1.3,0.5,3))),6)==round((.1/1.2)^2,6))
	checkTrue(round(c$distance(WeightedCurves(curves,c(1,0.98,3))),6)==round((.01/.97)^2,6))
	checkTrue(round(c$distance(WeightedCurves(curves,c(1,0.5,-0.22))),6)==round((0.22/5.67)^2,6))
	
	target <- 'Weight Constraint\nWeights:FXCarry max= 1.2 min=0\nWeights:LiqInj max= 5.67 min=0\nWeights:NDayBreak max= 0.97 min=0\n'
	checkSame(target, c$as.character())
	
	target <- data.frame(WC.FXCarry.Min = 0, WC.FXCarry.Max = 1.2, WC.LiqInj.Min = 0, WC.LiqInj.Max = 5.67, WC.NDayBreak.Min = 0, WC.NDayBreak.Max = 0.97)
	checkSame(target, c$as.data.frame())
	
 }
 
 

