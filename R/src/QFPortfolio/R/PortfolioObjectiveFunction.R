constructor("PortfolioObjectiveFunction", function(objectiveMetric = NULL, verbose = TRUE) {
    this <- extend(RObject(), "PortfolioObjectiveFunction", 
        .objectiveMetric = objectiveMetric, 
        .portfolioConstraints = list(),
        .constraintsForAll = list(),
        .constraints = list(),
        .weightedCurvesConstraints = list(),
		.curves = NULL,
		.verbose = verbose, 
		.penalty=NULL,
		.maxMin = c("max"),
		.count = 0
		
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, objectiveMetric="Metric")
    this
})

method('objectiveMetric', 'PortfolioObjectiveFunction', function(this,...){
	this$.objectiveMetric	
})

method("curves", "PortfolioObjectiveFunction", function(this, curves=NULL, ...) {
	needs(curves="list(PositionEquityCurve)?")
	if (is.null(curves)) return (this$.curves)
	this$.curves <- curves
})

method(".constraintDistance","PortfolioObjectiveFunction",function(static, curves, constraints, ...) {
	totalDistance <- 0
	for (curve in curves)
		for(constraint in constraints) {
			totalDistance <- totalDistance + constraint$distance(curve)
			
		}
	return(totalDistance)
})

method(".calcObjectiveWithPenalty","PortfolioObjectiveFunction", function(this, weights, ...) {
	wc <- WeightedCurves(this$.curves, weights)
	curve <- wc$curve()
	totalDistance <- 0
	totalDistance <- totalDistance + this$.constraintDistance(list(curve), this$.portfolioConstraints)
	for (constraint in this$.weightedCurvesConstraints) totalDistance <- totalDistance + constraint$distance(wc)
	scaled <- wc$scaledCurves()
	totalDistance <- totalDistance + this$.constraintDistance(scaled, this$.constraintsForAll)
	totalDistance <- totalDistance + this$.individualConstraintDistance(scaled, this$.constraints)
	penalty <- -1* totalDistance * this$.penalty
	return(curve$metric(this$.objectiveMetric) + penalty)
	})

method(".prettyWeights", "PortfolioObjectiveFunction", function(this, weights=NULL, ...) {
    squish("(", paste(collapse=", ", formatC(weights, format="f", digits=4)), ")")
})

method("objective", "PortfolioObjectiveFunction", function(this, weights=NULL, ...) {
	failIf(is.null(this$.curves),"Position Equity Curves Not Initialized")
	failIf(is.null(weights),"Weights must be entered")
	failIf(is.null(this$.penalty),"Need to initialize penalty amount")	
	this$.count <- this$.count + 1
    if(this$.verbose) cat(this$.count, ": ", this$.prettyWeights(weights), " ")
    
	res <- this$.calcObjectiveWithPenalty(weights)
	
	if(this$.verbose) {
        if(is.na(res))
            cat("=", attr(res, "message"), "\n")
        else
            cat("=", formatC(res, format="f", digits=2), "\n")
    }
	if (this$.maxMin=="min") res <- -1 * res
    res
})

method("constrainPortfolio", "PortfolioObjectiveFunction", function(this, metric, min, max, ...) {
    this$.portfolioConstraints <- appendSlowly(this$.portfolioConstraints, MetricConstraint(metric, min, max))
})

method('portfolioConstraints', 'PortfolioObjectiveFunction', function(this, ...){
	return(this$.portfolioConstraints)	
})

method("constrainAll", "PortfolioObjectiveFunction", function(this, metric, min, max, ...) {
    this$.constraintsForAll <- appendSlowly(this$.constraintsForAll, MetricConstraint(metric, min, max))
})

method("constrainWeightedCurves", "PortfolioObjectiveFunction", function(this, constraint, ...) {
    needs(constraint="WeightedCurvesConstraint")
    this$.weightedCurvesConstraints <- appendSlowly(this$.weightedCurvesConstraints, constraint)
})

method(".individualConstraintDistance", "PortfolioObjectiveFunction", function(this, curves, constraints, ...) {
	totalDistance <- 0
	for(constraintList in constraints)
		for(i in seq_along(curves)) {
			totalDistance <- totalDistance + constraintList[[i]]$distance(curves[[i]])	
		}
	return(totalDistance)
})

method("constrain", "PortfolioObjectiveFunction", function(this, metric, min, max, ...) {
    checkLength(min, length(this$.curves))
    checkLength(max, length(this$.curves))

    constraints <- mapply(function(lb, ub) MetricConstraint(metric, lb, ub), min, max, SIMPLIFY=FALSE)
    this$.constraints <- appendSlowly(this$.constraints, constraints)
})

method("penalty","PortfolioObjectiveFunction",function(this, penalty=NULL,...) {
	needs(penalty="numeric?")
	if (is.null(penalty)) return(this$.penalty)
	this$.penalty <- penalty
})

method("maxMin","PortfolioObjectiveFunction",function(this, maxMin=NULL,...) {
	needs(maxMin="character?")
	if (is.null(maxMin)) return(this$.maxMin)
	failUnless(any(maxMin==c("max","min")),"maxMin must be either max or min")
	this$.maxMin <- maxMin		
})
