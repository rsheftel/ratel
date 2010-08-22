# PortfolioRun Class
# 
# Author: rsheftel
###############################################################################


constructor("PortfolioRun", function(name = NULL, groupName=NULL, curvesDirectory=NULL, curvesExtension="bin"){
	this <- extend(RObject(), "PortfolioRun", .name=name, .groupName=groupName, .curvesDirectory=curvesDirectory, .curvesExtension=curvesExtension)
	constructorNeeds(this, name="character", groupName="character", curvesDirectory="character", curvesExtension="character")
	if (inStaticConstructor(this)) return(this)
	this$.range = NULL
	this$.seeds = NULL
	this$.objFct = NULL
	this$.optRoutine = NULL
	this$.optimType = c("constrOptim")
	this$.lists$sources <- c('original','optimal')
	this$.lists$curves  <- c('raw','cut')
	this$.lists$slips   <- c('noslip','slipped')
	this$.penalty <- NULL
	this$.constraints <- list()
	this$.constraintsDF <- list()
	this$unloadCurves()
	this$removeResults()
	this$removeMetrics()
	return(this)
})

#################################################################################################################
#	Getter only
#################################################################################################################

method("name", "PortfolioRun", function(this, ...){
	return(this$.name)
})

method("curvesDirectory", "PortfolioRun", function(this, ...){
	return(this$.curvesDirectory)	
})

method("groupName", "PortfolioRun", function(this, ...){
	return(this$.groupName)	
})

method("startDate", "PortfolioRun", function(this, ...){
	if (is.null(range)) return(NULL)
	return(this$.startDate)
})

method("endDate", "PortfolioRun", function(this, ...){
	if (is.null(range)) return(NULL)
	return(this$.endDate)
})

method("optimalWeights", "PortfolioRun", function(this, ...){
	return(this$.weights$optimal)	
})

method("optimalSource", "PortfolioRun", function(this, ...){
	return(this$.optimalSource)	
})

method("constraintList", "PortfolioRun",function(this, ...){
	return(this$.constraints)	
})

method("constraintListDF", "PortfolioRun", function(this, ...){
	return(this$.constraintsDF)		
})

method("slippages", "PortfolioRun", function(this, ...){
	return(this$.slippages)	
})

#################################################################################################################
#	Setter / Getter
#################################################################################################################

method("objective", "PortfolioRun", function(this, objective=NULL, ...){
	needs(objective="Metric?")
	if (is.null(objective)) return(this$.objective)
	failIf(!is.null(this$.objective), "Objective can only be set once and not changed.")
	this$.objective <- objective
})

method("dateRange", "PortfolioRun", function(this, start=NULL, end=NULL, ...){
	needs(start='character|POSIXct', end='character|POSIXct')
	failIf(!this$.nullCurve('original','raw','noslip'), "Cannot change date range once curves are loaded.")
	this$.startDate <- as.POSIXct(start)
	this$.endDate <- as.POSIXct(end)
	this$.range <- Range(start, end)
	#this$.curves$cut <- cut with the withRange(), but only if optimization not run yet
	#then run the slipped method to slip them.		
})

method("minMaxWeights", "PortfolioRun", function(this, mins=NULL, maxs=NULL, ...){
	if(is.null(mins) && is.null(maxs)) return(list(mins=this$.weights$min, maxs=this$.weights$max))
	failIf(this$.optimized, "Portfolio already optimized, cannot change weights")
	failIf(!is.null(this$.optRoutine), "Weights cannot be changed after this$.optRoutine is set.")
	failIf(!is.null(this$.optimizer), "Optimization object already created, cannot change weights")
	if(!is.null(mins)) this$.weights$min <- this$.listToVector(mins, 0)
	if(!is.null(maxs)) this$.weights$max <- this$.listToVector(maxs, 1)
})

method("seeds", "PortfolioRun", function(this, seeds=NULL, ...){
	needs(seeds="list(numeric)?")
	if (is.null(seeds)) return(this$.seeds)
	failIf(this$.optimized, "Portfolio already optimized, cannot change seeds")
	this$.seeds <- this$.listToVector(seeds, NA)
})

method("diversityScore", "PortfolioRun", function(this, diversityScore=NULL, ...){
	needs(diversityScore="list(numeric)?")
	if (is.null(diversityScore)) return(this$.diversityScore)
	this$.diversityScore <- this$.listToVector(diversityScore, NA)
})

method("penalty", "PortfolioRun", function(this, penalty=NULL,...){
	needs(penalty="numeric?")
	if (is.null(penalty)) return(this$.penalty)
	failIf(!is.null(this$.optimizer), "Penatly cannot be changed after this$.optimizer is set.")
	this$.penalty <- penalty				
})

method("params","PortfolioRun", function(this, params=NULL,...) {
	needs(params="list?")
	if (is.null(params)) return(this$.params)
	failIf (!is.null(this$.optRoutine), "Params cannot be changed after this$.optRoutine is set.")
	this$.params <- params	
})

method("optimType","PortfolioRun", function(this, optimType=NULL,...) {
	needs (optimType="character?")
	if (is.null(optimType)) return (this$.optimType)
	failIf (!is.null(this$.optRoutine), "OptimType cannot be changed after this$.optRoutine is set.")
	failIf (!is.null(this$.optimizer), "OptimType cannot be changed after this$.optimizer is set.")
	this$.optimType <- optimType
})

method('optimizer', 'PortfolioRun', function(this, ...){
	this$.optimizer		
})

#################################################################################################################
#	Curve Methods
#################################################################################################################

method("loadCurves", "PortfolioRun", function(this, curves=NULL, rawRange=NULL, ...){
	needs(curves='list?')
	failIf(!this$.nullCurve('original','raw','noslip'), "Curves already loaded")
	failIf((!is.null(curves) && !inherits(first(curves),"PositionEquityCurve")), "Supplied curves must be WeightedCurve.")
	this$unloadCurves()
	rawCurves <- curves
	if(is.null(curves)) rawCurves <-CurveGroup(this$.groupName)$childCurves(dir=this$.curvesDirectory, extension=this$.curvesExtension, range=rawRange)
	#raw curves
	cuts <- 'raw'
	for (slips in this$.lists$slips)
		this$.makePortfolioCurve(source='original',curves=cuts,slip=slips, rawCurves)
	
	#cut curves
	cuts <- 'cut'
	if(!is.null(this$.range)){
		cutCurves <- lapply(names(rawCurves), function(x) rawCurves[[x]]$withRange(this$.range))
		names(cutCurves) <- names(rawCurves)
		rawCurves <- cutCurves
	}
	for (slips in this$.lists$slips)
		this$.makePortfolioCurve(source='original',curves=cuts,slip=slips, rawCurves)
})

method("unloadCurves", "PortfolioRun", function(this, ...){
	this$.curves <- NULL
	for (source in this$.lists$sources)
		for (curve in this$.lists$curves)
			for (slip in this$.lists$slips)
				this$.curves[[source]][[curve]][[slip]] <- list(NULL)
	this$removeResults()
})

method("childWeightedCurves", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	this$.validateInputs(source,curves,slip)
	if(this$.nullCurve(source, curves, slip)) return(NULL)
	return(this$.curves[[source]][[curves]][[slip]]$scaledCurves())
})

method("childCurvesEquity", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	this$.validateInputs(source,curves,slip)
	wc <- this$childWeightedCurves(source, curves, slip)
	equityCurves <- sapply(names(wc), function(x) wc[[x]]$equity(), simplify=FALSE)
	return(do.call(merge, equityCurves))		
})

method("portfolioCurve", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	this$.validateInputs(source,curves,slip)
	if(this$.nullCurve(source, curves, slip)) return(NULL)
	return(this$.curves[[source]][[curves]][[slip]]$curve())
})

method("portfolioCurveEquity", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	this$.validateInputs(source,curves,slip)
	return(this$portfolioCurve(source, curves, slip)$equity())
})

method("allCurvesEquity", "PortfolioRun",function(this, source='original', curves='raw', slip='noslip', ...){
	mergedZoo <- merge(getZooDataFrame(this$portfolioCurveEquity(source,curves,slip),"Portfolio"), this$childCurvesEquity(source,curves,slip))
	if (!is.null(names(index(mergedZoo)))) names(index(mergedZoo)) <- c()
	return(mergedZoo)				
})

method("curveNames", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	this$.validateInputs(source,curves,slip)		
	return(names(this$childWeightedCurves(source, curves, slip)))	
})

method("slipCurves", "PortfolioRun", function(this, slippages, ...){
	needs(slippages="list(numeric|interger)")
	this$.slippages <- this$.listToVector(slippages)
	for (curve in this$.lists$curves){
		slipCurves <- SlippageCurves(this$childWeightedCurves('original',curve,'noslip'), this$.slippages)$slippageAdjustedCurves()
		this$.curves$original[[curve]]$slipped <- this$.makePortfolioCurve(source='original', curves=curve, slip='slipped', slipCurves) 
	}
})

#################################################################################################################
#	Optimization Methods
#################################################################################################################

method("removeResults", "PortfolioRun", function(this, ...){
	this$.optimized = FALSE
	this$.optimizer = NULL
	this$.constraints = list()
	this$.weights$optimal = NULL
	this$minMaxWeights(NULL, NULL)
	return(invisible())		
})

method(".initializePortfolioObjectiveFunction", "PortfolioRun", function(this, ...){
	failIf(is.null(this$.objective),"Objective metric must be set")
	failIf(!inherits(this$.curves$original$cut$slipped,"WeightedCurves"), "Curves must be loaded")
	this$.objFct <- PortfolioObjectiveFunction(objectiveMetric=this$.objective)
	this$.objFct$curves(curves=this$childWeightedCurves(source='original',curves='cut',slip='slipped'))
})

method(".initializeOptimizationRoutine", "PortfolioRun", function(this, ...){
	this$.optRoutine <- OptimizationRoutine(type=this$.optimType)
	this$.optRoutine$upperWeights(upper= this$.weights$max)
	this$.optRoutine$lowerWeights(lower = this$.weights$min)
	if (!is.null(this$.params)) this$.optRoutine$params(listParams=this$.params)		
})

method(".initializeOptiObject", "PortfolioRun", function(this, ...){
	if (is.null(this$.objFct)) this$.initializePortfolioObjectiveFunction()
	if (is.null(this$.optRoutine)) this$.initializeOptimizationRoutine()
	if (this$.optimType=="nlminb") this$.objFct$maxMin(maxMin="min")
	if (!is.null(this$.penalty)) this$.objFct$penalty(penalty=this$.penalty)
	
	if (this$.optimType=='metricAllocation'){ this$.optimizer <- MetricAllocation()
	}else{	this$.optimizer <- PortfolioOptimizer() }
})

method("addConstraint", "PortfolioRun", function(this, constraint, metric, min, max,...){
	failIf(this$.optimized, "Portfolio already optimized, cannot add constraints.")
	if(is.null(this$.objFct)) this$.initializePortfolioObjectiveFunction()
	if(any(is(constraint)=="character")){
		switch (constraint,
			Portfolio 	= this$.objFct$constrainPortfolio(metric, min, max),
			AllChildren = this$.objFct$constrainAll(metric, min, max),
			EachChild 	= this$.objFct$constrain(metric, min, max),
			fail(squish("Not valid constraint type: ", constraint)))
		this$.constraints <- appendSlowly(this$.constraints, list(type=constraint, metric=metric, min=min, max=max))
		this$.constraintsDF <- appendSlowly(this$.constraintsDF, MetricConstraint(metric, min, max)$as.data.frame())
		return(invisible())
	}		
	if(inherits(constraint,"WeightedCurvesConstraint")){
		this$.objFct$constrainWeightedCurves(constraint)
		this$.constraints <- appendSlowly(this$.constraints, list(type='WeightedCurve', report=as.character(constraint)))
		this$.constraintsDF <- appendSlowly(this$.constraintsDF, constraint$as.data.frame())
		return(invisible())
	}
})

method("optimize", "PortfolioRun", function(this, ...){
	this$setupOptimizer()
	this$.weights$optimal <- this$.optimizer$optimize(start=this$.seeds,...)
	this$.optimized = TRUE
	this$.optimalSource <- 'optimizer'
	this$.makeResultCurves()
})

method('setupOptimizer', 'PortfolioRun', function(this,...){
	failIf(this$.optimized, 'Already optimized.')
	failIf(is.null(this$seeds()), 'Seeds are NULL, must be set to optimize')
	if(is.null(this$.optimizer)) this$.initializeOptiObject()	
	this$.optimizer$objectiveFunction(objectiveFunction=this$.objFct)
	this$.optimizer$optimizationRoutine(optimizationRoutine=this$.optRoutine)
	return(invisible())
})

method(".makeResultCurves", "PortfolioRun", function(this, ...){
	for (curve in this$.lists$curves)
		for (slip in this$.lists$slips){
			childCurves <- this$childWeightedCurves('original', curve, slip)
			this$.makePortfolioCurve('optimal', curve, slip, childCurves, this$.weights$optimal)
		}	
})

method("setOptimalWeights", "PortfolioRun", function(this, weights, ...){
	failIf(this$.optimized, "Already optimized.")
	this$removeResults()
	this$.weights$optimal <- this$.listToVector(weights)
	this$.optimized <- TRUE
	this$.optimalSource <- 'manual'
	this$.makeResultCurves()
})

#################################################################################################################
#	Metric Methods
#################################################################################################################

method("calculateMetrics", "PortfolioRun", function (this, metrics, verbose = FALSE, ...){
	needs(metrics='list(Metric)')
	for (metric in metrics)
		this$calculateMetric(metric, verbose)
})

method("calculateMetric", "PortfolioRun", function (this, metric, verbose = FALSE, ...){
	needs(metric="Metric")
	metric.name <- as.character(metric)
	for (source in this$.lists$sources)
		for (curves in this$.lists$curves)
			for (slip in this$.lists$slips){
				if (this$.nullCurve(source, curves, slip)) break()
				if(verbose) cat(squish("Calculating metric: ",metric.name,",  source: ",source,", curves: ",curves,", slip: ",slip,"\nCalculating Portfolio...\n"))
				this$.addMetric(source, curves, slip, 'Portfolio', metric.name, this$portfolioCurve(source, curves, slip)$metric(metric))
				childCurves <- this$childWeightedCurves(source, curves, slip)
				for (childCount in seq_along(childCurves)){
					childName <- names(childCurves)[childCount]
					if(verbose) cat(squish("Calulating child curve: ",childName,'\n'))
					this$.addMetric(source, curves, slip, childName, metric.name, childCurves[[childCount]]$metric(metric))
				}
			}
})

method(".addMetric", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', curveName=NULL, metric.name=NULL, metric.value=NULL, ...){
	needs(source='character', curves='character', slip='character', curveName='character', metric.name='character', metric.value='numeric')
		this$.metrics[[source]][[curves]][[slip]][[curveName]][[metric.name]] <- metric.value
})

method("metric", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', curveName=NULL, metric=NULL, ...){
	needs(source='character', curves='character', slip='character', curveName='character', metric='character|Metric?')
	if(is.null(curveName)) return('Not implemented yet')
	if(is.null(metric)) return(this$.metrics[[source]][[curves]][[slip]][[curveName]])
	return(this$.metrics[[source]][[curves]][[slip]][[curveName]][[metric]])
})

method("removeMetrics", "PortfolioRun", function(this, ...){
	this$.metrics <- NULL
	for (source in this$.lists$sources)
		for (curve in this$.lists$curves)
			for (slip in this$.lists$slips)
				this$.metrics[[source]][[curve]][[slip]] <- list(NULL)
})

#################################################################################################################
#	Helper Methods
#################################################################################################################

method(".listToVector", "PortfolioRun", function(this, inputList, naValue=NULL, ...){
	outputVector <- unlist(inputList)
	goodorder <- match(this$curveNames(), names(outputVector))
	failIf(is.null(naValue) && any(is.na(goodorder)),"All values must be supplied for each curve")
	resultVector <- as.numeric(outputVector[goodorder])
	if (any(is.na(goodorder))) resultVector[is.na(resultVector)] <- naValue
	return(resultVector)
})

method(".makePortfolioCurve", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', childCurves=NULL, weights=NULL, ...){
	needs(source='character', curves='character', slip='character')
	if(is.null(weights)) weights <- rep(1.0, length(childCurves))
	this$.curves[[source]][[curves]][[slip]] <- WeightedCurves(childCurves, weights)    	
})

method(".validateInputs", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	good.source <- any(source %in% this$.lists$sources)
	good.curves <- any(curves %in% this$.lists$curves)
	good.slip   <- any(slip %in% this$.lists$slips)
	failIf(!all(c(good.source, good.curves, good.slip)), "Inputs for curve type not valid.")
})

method(".nullCurve", "PortfolioRun", function(this, source='original', curves='raw', slip='noslip', ...){
	needs(source='character', curves='character', slip='character')
	this$.validateInputs(source, curves, slip)
	if(is.null(this$.curves[[source]][[curves]][[slip]])) return(TRUE)
	return(is.null(first(this$.curves[[source]][[curves]][[slip]][1])))
})