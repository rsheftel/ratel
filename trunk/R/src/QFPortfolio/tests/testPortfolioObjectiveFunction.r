library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

sto <- STO(stoDirectory(), "SimpleSTOTemplate")
curve.cube <- sto$curves()
curve1 <- curve.cube$curve(first(simpleMsivs), 1) # 0, -1  (NetProfit, MaxDrawDown)
curve2 <- curve.cube$curve(first(simpleMsivs), 2) # 5, 0 
curve3 <- curve.cube$curve(first(simpleMsivs), 3) #45, 0
curves <- list(curve1, curve2, curve3)

testPortfolioObjectiveFunction <- function() {
    penalty=1000000
################
	opt <- PortfolioObjectiveFunction(NetProfit)
	checkSame(NetProfit, opt$objectiveMetric())
	opt$curves(curves=curves)
	opt$penalty(penalty=penalty)
    checkSame(120, opt$objective(c(8, 6, 2)))
################
	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
    opt$constrainPortfolio(MaxDrawDown, -5, 0)
	
	opt$penalty(penalty=penalty)
	checkTrue(as.numeric(opt$objective(c(8,0,0)))==(-.36*penalty))
	checkTrue(as.numeric(opt$objective(c(8,2,0))) == 10)
################	
	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
	opt$constrainAll(MaxDrawDown, -1, 0)

	opt$penalty(penalty=penalty)
	checkTrue(opt$objective(c(2,2,0))==(10-penalty))
    checkTrue(opt$objective(c(0,2,0))==10)
################	
	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
    opt$constrain(NetProfit, c(0, 0, 0), c(0,5,45))
	
	opt$penalty(penalty=penalty)
	checkTrue(opt$objective(c(1,1,1))==50)
	checkTrue(opt$objective(c(0, 2, 0))==(10-penalty))
	checkTrue(opt$objective(c(0,0,2))==(90-penalty))
	checkTrue(opt$objective(c(1, 1, 1.00001))==(50-penalty*(0.00001)^2+0.00001*45))
################
	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
	opt$penalty(penalty=penalty)
	ivc <- IncrementalVarConstraint(c('FX', 'FX', 'Directional'), incrementalAlpha = 0.5)
    ivc$set('FX', 0, .3)
    ivc$set('Directional', 0, .8)
    opt$constrainWeightedCurves(ivc)

	checkTrue(opt$objective(c(5, 1, 2))==95)
	checkTrue(opt$objective(c(6, 1, 2))==95)
	checkTrue(round(opt$objective(c(8, 2, 0)),0)==(10-5.444444*penalty))
################	
	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
	opt$constrainWeightedCurves(SimpleTestWCConstraint())
 
################

	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
	opt$penalty(penalty=penalty)
	wc <- WeightConstraint(c('FXCarry','DTD','NDayBreak'))
	wc$set('FXCarry',0,1)
	wc$set('DTD',0,5)
	wc$set('NDayBreak',0,3)
	opt$constrainWeightedCurves(wc)
	checkEquals(opt$objective(c(1,1,1)),50)
    checkEquals(opt$objective(c(2,1,1)),50-penalty)
	checkEquals(round(opt$objective(c(1,6,1)),2),75-penalty*(0.2)^2)
	checkEquals(round(opt$objective(c(1,1,4)),2),185-penalty*(1/3)^2)
}

test.constraintGetters <- function(){
	opt <- PortfolioObjectiveFunction(NetProfit)
	opt$curves(curves=curves)
	checkTrue(is.empty(opt$portfolioConstraints()))
	
	opt$constrainPortfolio(MaxDrawDown, -5, 0)
	expected <- list(MetricConstraint(MaxDrawDown,-5,0))
	checkSame(opt$portfolioConstraints(), expected)
	
	opt$constrainPortfolio(AverageDrawDown, -100, 100)
	expected <- c(expected, list(MetricConstraint(AverageDrawDown, -100, 100)))
	checkSame(opt$portfolioConstraints(), expected)
}
