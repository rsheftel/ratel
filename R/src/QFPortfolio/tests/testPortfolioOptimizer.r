library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

sto <- STO(stoDirectory(), "SimpleSTOTemplate")
curve.cube <- sto$curves()
curve1 <- curve.cube$curve(first(simpleMsivs), 1) # 0, -1  (NetProfit, MaxDrawDown)
curve2 <- curve.cube$curve(first(simpleMsivs), 2) # 5, 0 
curve3 <- curve.cube$curve(first(simpleMsivs), 3) #45, 0
curves <- list(curve1, curve2, curve3)

testPortfolioOptimizer <- function() {
	
	penalty=10000000
    objFct <- PortfolioObjectiveFunction(NetProfit)
	objFct$curves(curves=curves)
	objFct$penalty(penalty=penalty)
	optRoutine <- OptimizationRoutine(type="constrOptim")
	optRoutine$lowerWeights(c(0,0,0))
	optRoutine$upperWeights(c(3,2,1))
	
	opt <- PortfolioOptimizer(verbose=FALSE)
	opt$objectiveFunction(objectiveFunction=objFct)
	opt$optimizationRoutine(optimizationRoutine=optRoutine)
	
	checkSame(opt$objectiveMetric(), NetProfit)
	checkSame(opt$curves(), curves)
	
	optimalWeights <- opt$optimize(c(NA, 1, 0.5))
    checkSame(round(optimalWeights, 2)[2:3], c(1.99,1))
    checkTrue(is.na(first(optimalWeights)))
    
    
	objFct <- PortfolioObjectiveFunction(NetProfit)
	objFct$curves(curves=curves)
	objFct$penalty(penalty=penalty)
	optRoutine <- OptimizationRoutine(type="constrOptim")
	optRoutine$lowerWeights(c(0,0,0))
	optRoutine$upperWeights(c(3,2,1))
	opt <- PortfolioOptimizer(verbose=FALSE)
	opt$objectiveFunction(objectiveFunction=objFct)
	opt$optimizationRoutine(optimizationRoutine=optRoutine)
    optimalWeights <- opt$optimize(c(1.5, 1, 0.5))
    checkSame(round(optimalWeights, 2)[2:3], c(2,1))
    
}
