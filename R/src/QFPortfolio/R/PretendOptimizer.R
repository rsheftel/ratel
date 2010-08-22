constructor("PretendOptimizer", function(curves = NULL) {
    this <- extend(AbstractOptimizer(), "PretendOptimizer", .curves = curves, .count = 0, .ranges = list())
    if(inStaticConstructor(this)) return(this)
    this
})

method("curves", "PretendOptimizer", function(this, ...) {
    this$.curves
})

method("optimize", "PretendOptimizer", function(this, start, fastRestarts = 8, fastTol = 10^-2, slowRestarts = 1, slowTol = 10^-6, range = NULL, newRange = TRUE, ...) {
    if(newRange) this$.ranges <- appendSlowly(this$.ranges, range)
    this$.count <- this$.count + 1
    result <- rep(this$.count, length(this$curves()))
    result[is.na(start)] <- NA
    result
})

method('objectiveMetric', 'PretendOptimizer', function(this, ...){
	NetProfit	
})

method('objectiveFunction', 'PretendOptimizer', function(this, ...){
	objFct <- PortfolioObjectiveFunction(NetProfit)
	objFct$curves(curves = this$curves())
	objFct$penalty(penalty = -1000000)
	objFct	
})

method('optimizationRoutine', 'PretendOptimizer', function(this, ...){
	OptimizationRoutine(type="constrOptim")	
})

