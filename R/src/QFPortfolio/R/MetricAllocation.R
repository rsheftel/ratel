#
#	MetricAllocation class of type AbstractOptimizer
#
####################################################################################

constructor("MetricAllocation", function(verbose = TRUE) {
    this <- extend(AbstractOptimizer(), "MetricAllocation", 
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

method('objectiveMetric', 'MetricAllocation', function(this, ...){
	failIf(is.null(this$objectiveFunction()), 'ObjetiveFunction not set yet.')
	this$objectiveFunction()$objectiveMetric()	
})

method("objectiveFunction","MetricAllocation",function(this, objectiveFunction=NULL,...){
	needs(objectiveFunction="PortfolioObjectiveFunction?")
	if (is.null(objectiveFunction)) return(this$.objectiveFunction)
	this$.objectiveFunction <- objectiveFunction
})
	
method("optimizationRoutine","MetricAllocation", function(this, optimizationRoutine=NULL,...){
	needs(optimizationRoutine="OptimizationRoutine?")	
	if (is.null(optimizationRoutine)) return(this$.optimizationRoutine)
	this$.optimizationRoutine <- optimizationRoutine
})

method('curves', 'MetricAllocation', function(this, ...){
	this$.objectiveFunction$curves()	
})

method("optimize", "MetricAllocation", function(this, start=NULL, range = NULL,...) {
	needs(start="numeric|integer?", range="Range?")
	failIf(is.null(this$.objectiveFunction),"Objective Function not initialized")
	failIf(is.null(this$.optimizationRoutine),"Optimization Routine not initialized")
		
	curves <- this$curves()
	failIf(is.null(curves),"Curves not loaded.")
	curveNames <- names(curves)
	if(is.null(curveNames)) curveNames <- paste('curve',1:length(curves),sep="")	
	if(!is.null(range)) {
		if(this$.verbose) print(squish("Cutting curves for range: ",range))
		curves <- lapply(curves, function(curve) curve$withRange(range))
	} 
	nas <- rep(FALSE, length(curves))
	if(!is.null(start)){
		if(this$.verbose) print("Starting seed values are ignored for this optimization method.")
		failIf((length(start) != length(curves)), "Number of seeds does not match number of curves.")
		nas <- is.na(start)
	}
	#reduce the problem set if NAs are passed in start
	curves <- curves[!nas]
	curveNames <- curveNames[!nas]
	
	#Do the simple allocation
	rawRisk <- unlist(lapply(curves, function(x) x$metric(this$objectiveMetric())))
	names(rawRisk) <- curveNames
	if(this$.verbose) print(squish("rawRisk       : ",paste(format(rawRisk,nsmall=4),collapse=", ")))	
	
	diversityScore <- this$.diversityScore(curveNames, range)
	if(this$.verbose) print(squish("diversityScore: ",paste(format(diversityScore,nsmall=4),collpase=", ")))
	
	totalRawRisk <- sum(rawRisk)
	totalDiversity <- sum(diversityScore)
	riskPerDiversity <- totalRawRisk/totalDiversity
	weights.simple <- (diversityScore*riskPerDiversity) / rawRisk
	if(this$.verbose) print(squish("weights.simple: ",paste(format(weights.simple,nsmall=4),collapse=", ")))
	
	#Scale up to the constraint.
	weights.scaled <- weights.simple
	if(!is.empty(this$objectiveFunction()$portfolioConstraints())){
		wc <- WeightedCurves(curves, weights.simple)
		scale <- this$.scaleToConstraint(wc)
		weights.scaled <- weights.simple * scale
	}
	names(weights.scaled) <- curveNames
	
	#Return the results		
	if(this$.verbose) cat("OPTIMAL WEIGHTS:", this$.prettyWeights(weights.scaled), "\n")
	result <- rep(NA, length(nas))
	result[!nas] <- weights.scaled
	names(result) <- names(curves)
	return(result)
})

method('.diversityScore', 'MetricAllocation', function(this, curveNames, range, ...){
	needs(curveNames='character?', range="Range?")
	div <- this$optimizationRoutine()$params()$diversity
	if(is.null(div)){
		divScore <- rep(1,length(curveNames))
		names(divScore) <- curveNames
		return(divScore)
	}
	failIf(!(all(curveNames %in% div$childNames())), 'All Curves do not exist in diversity object.')
	if(is.null(range)){
		wc <- WeightedCurves(this$curves())
		range <- Range(first(index(wc$curve()$equity())),last(index(wc$curve()$equity())))
	}
	divScore <- div$score(this$optimizationRoutine()$params()$method, range, weights=this$optimizationRoutine()$params()$diversity.params$weights)
	return(divScore[curveNames])
})

method('.scaleToConstraint', 'MetricAllocation', function(this, wc, ...){
	needs(wc="WeightedCurves")
	if(this$.verbose) print('Scaling to constraints...')
	scales <- c()
	for (constraint in this$objectiveFunction()$portfolioConstraints()){
		if(this$.verbose) print(constraint)
		currentValue <- wc$curve()$metric(constraint$metric())
		if ((constraint$minimum()>0) || (constraint$maximum()>0)){ constraintValue <- constraint$maximum()}
		else{constraintValue <- constraint$minimum()}
		if (sign(currentValue) != sign(constraintValue)){
			if(this$.verbose) print('No feasable solution to constrant.')
			return(0)
		} 
		scale <- constraintValue / currentValue
		if(this$.verbose) print(squish('Scale: ',scale))
		scales <- c(scales,scale)
	}	
	return(min(scales))
})

method(".prettyWeights", "MetricAllocation", function(this, weights=NULL, ...) {
	squish("(", paste(collapse=", ", formatC(weights, format="f", digits=4)), ")")
})
