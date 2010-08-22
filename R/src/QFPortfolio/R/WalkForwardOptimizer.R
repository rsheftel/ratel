constructor("WalkForwardOptimizer", function(optimizer = NULL, window = NULL, frequency = NULL) {
    this <- extend(RObject(), "WalkForwardOptimizer", 
        .optimizer = optimizer, 
        .window = window, 
        .frequency = frequency,
		.savedWeights = NULL
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, optimizer="AbstractOptimizer", window = "RollingWindow", frequency="Period")
    this
})

# passing through to PortfolioOptimizer$optimize(...)
#     start, fastRestarts, fastTol, slowRestarts, slowTol
method("optimizeOnce", "WalkForwardOptimizer", function(this, startDate, seed, originalSeed, ...) {
   print('in OptimizeOnce')
   needs(startDate="POSIXct", seed="numeric")
   range <- this$.window$preceding(startDate)
   curves <- this$.optimizer$curves()
   seed <- this$assignSeeds(curves, range, seed, originalSeed)
   
   print(seed)
   print(range)
 
   trialWeights <- this$.optimizer$optimize(start = seed, range = range, ...)
 		
   nas <- is.na(seed)
   trialObjectiveFunction <- this$.optimizer$objectiveFunction()
   cutCurves <- lapply(curves, function(curve) curve$withRange(range))
   trialObjectiveFunction$curves(curves=cutCurves[!nas])
   trialObjectiveFunctionOutput <- trialObjectiveFunction$objective(trialWeights[!nas])
   print(squish('trialObjectiveFunctionOutput = ', -trialObjectiveFunctionOutput))
   trialObjectiveFunction$curves(curves = curves)
   if(-trialObjectiveFunctionOutput > 0) return(trialWeights)		#### ADJUST --- FOR NLMINB ONLY 
   print('Bad ObjectiveFunction, retrying with original Seed')
   seed <- this$assignSeeds(curves, range, originalSeed, originalSeed)
   this$.optimizer$optimize(start = seed, range = range, newRange = FALSE, ...)
})

method("optimize", "WalkForwardOptimizer", function(this, startDate, endDate, seed, saveWeights = TRUE, ...) { 
    needs(startDate="POSIXt", endDate="POSIXt", seed="numeric")
    dates <- c()
    while (startDate <= endDate) {
        dates <- c(dates, as.character(startDate))
        startDate <- this$.frequency$advance(startDate)
    }
    dates <- as.POSIXct(dates)
    z <- zoo(matrix(NA, nrow=length(dates), ncol=length(seed)), order.by=dates)
    nextSeed <- seed
    for(i in seq_along(dates)) {    
        nextSeed <- this$optimizeOnce(dates[[i]], nextSeed, seed, ...)
        z[i,] <- nextSeed
    }
	if(saveWeights) this$.savedWeights <- Weights(z)
    Weights(z)
})

method('assignSeeds', 'WalkForwardOptimizer', function(this, curves, range, seed, originalSeed, ...){
	for(i in seq_along(curves)) {
		if(!curves[[i]]$covers(range)) {
			seed[[i]] <- NA
			next;
		}
		if(is.na(seed[[i]])) seed[[i]] <- originalSeed[[i]]
	}		
	seed
})

method('savedWeights', 'WalkForwardOptimizer', function(this, ...){
	this$.savedWeights		
})


