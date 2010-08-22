constructor("PortfolioOptimizer", function(verbose = TRUE) {
    this <- extend(AbstractOptimizer(), "PortfolioOptimizer", 
        .objectiveFunction = NULL, 
        .verbose = verbose,
        .lower = NULL,
        .upper  = NULL,
		.optimizationRoutine = NULL
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, verbose="logical")
    this
})

method('objectiveMetric', 'PortfolioOptimizer', function(this, ...){
	this$objectiveFunction()$objectiveMetric()	
})

method("objectiveFunction","PortfolioOptimizer",function(this, objectiveFunction=NULL,...){
		needs(objectiveFunction="PortfolioObjectiveFunction?")
		if (is.null(objectiveFunction)) return(this$.objectiveFunction)
		this$.objectiveFunction <- objectiveFunction
})
	
method("optimizationRoutine","PortfolioOptimizer", function(this, optimizationRoutine=NULL,...){
		needs(optimizationRoutine="OptimizationRoutine?")	
		if (is.null(optimizationRoutine)) return(this$.optimizationRoutine)
		this$.optimizationRoutine <- optimizationRoutine
})

method('curves', 'PortfolioOptimizer', function(this, ...){
		this$.objectiveFunction$curves()	
})

method("optimize", "PortfolioOptimizer", function(this, start, range = NULL,...) {
		failIf(is.null(this$.objectiveFunction),"Objective Function Not initialized")
		this$.count <- 0
		oldCurves <- this$.objectiveFunction$curves()
		
		if(!is.null(range)) {
			cutCurves <- lapply(this$.objectiveFunction$curves(), function(curve) curve$withRange(range))
			this$.objectiveFunction$curves(curves=cutCurves)
		} 
		nas <- is.na(start)
		start <- start[!nas]
		this$.optimizationRoutine$upperWeights(upper=this$.optimizationRoutine$upperWeights()[!nas])
		this$.optimizationRoutine$lowerWeights(lower=this$.optimizationRoutine$lowerWeights()[!nas])
		this$.objectiveFunction$curves(curves=this$.objectiveFunction$curves()[!nas])
		
		res <- this$.optimizationRoutine$optimize(start, this$.objectiveFunction$objective)
			
		if(this$.verbose) cat("OPTIMAL WEIGHTS:", this$.prettyWeights(res$par), "\n")
		this$.objectiveFunction$curves(curves=oldCurves)
		result <- rep(NA, length(nas))
		result[!nas] <- res$par
		result
	})


method("optimizeLoop", "PortfolioOptimizer", function(this, start, fastRestarts = 16, fastTol = 10^-2, slowRestarts = 4, slowTol = 10^-4, range = NULL, ndeps=10^-3,...) {
		failIf(is.null(this$.objectiveFunction),"Objective Function Not initialized")
		this$.count <- 0
		oldCurves <- this$.objectiveFunction$curves()
		
		if(!is.null(range)) {
			cutCurves <- lapply(this$.objectiveFunction$curves(), function(curve) curve$withRange(range))
			this$.objectiveFunction$curves(curves=cutCurves)
		} 
		nas <- is.na(start)
		start <- start[!nas]
		this$.optimizationRoutine$upperWeights(upper=this$.optimizationRoutine$upperWeights()[!nas])
		this$.optimizationRoutine$lowerWeights(lower=this$.optimizationRoutine$lowerWeights()[!nas])
		this$.objectiveFunction$curves(curves=this$.objectiveFunction$curves()[!nas])
		
		for(i in seq_len(fastRestarts)) {
			this$.optimizationRoutine$params(listParams=list(reltol=slowTol, fnscale=-1,ndeps=ndeps))
			res <- this$.optimizationRoutine$optimize(start, this$.objectiveFunction$objective)
			start <- res$par
			if(this$.verbose) cat("Restarting optimizer with start = ", this$.prettyWeights(res$par), "\n")
		}
		for(i in seq_len(slowRestarts)) {
			this$.optimizationRoutine$params(listParams=list(reltol=slowTol, fnscale=-1,ndeps=ndeps))
			res <- this$.optimizationRoutine$optimize(start, this$.objectiveFunction$objective)
			start <- res$par
			if(this$.verbose) cat("Restarting optimizer with start = ", this$.prettyWeights(res$par), "\n")
		}
		if(this$.verbose) cat("OPTIMAL WEIGHTS:", this$.prettyWeights(res$par), "\n")
		this$.objectiveFunction$curves(curves=oldCurves)
		result <- rep(NA, length(nas))
		result[!nas] <- res$par
		result
	})



method(".prettyWeights", "PortfolioOptimizer", function(this, weights=NULL, ...) {
		squish("(", paste(collapse=", ", formatC(weights, format="f", digits=4)), ")")
	})


generatePortfolioMSCsv <- function(filename) { 
    needs(filename = "character")
    JPortfolioMSCsv$by()$csv_by_String(filename)
}
