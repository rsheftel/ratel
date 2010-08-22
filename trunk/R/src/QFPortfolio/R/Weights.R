constructor("Weights", function(weights = NULL) {
    this <- extend(RObject(), "Weights", .weights = weights)
    constructorNeeds(this, weights="zoo|numeric")
    if (inherits(weights, "numeric")) 
        this$.weights <- zoo(matrix(weights, nrow=1), order.by = c(as.POSIXct("1900/01/01")))
    if(inStaticConstructor(this)) return(this)
    this
})

method("as.zoo", "Weights", function(this, ...) {
    this$.weights        
})

method("as.character", "Weights", function(this, ...) {
    humanString(this$.weights)        
})

method("scale", "Weights", function(this, toBeScaled, ...) {
    needs(toBeScaled="list(zoo)")
    weightsDates <- index(this$.weights)
    for(zooIndex in seq_along(toBeScaled)) {
        targetDates <- index(toBeScaled[[zooIndex]])
        weightsZoo <- zoo(NA, targetDates)
        for(timeIndex in seq_along(weightsDates)) {
            if (timeIndex + 1 > length(weightsDates)) break;
            time <- weightsDates[[timeIndex]]
            nextTime <- weightsDates[[timeIndex + 1]]
            thisScaleDates <- targetDates >= time & targetDates < nextTime
            weightsZoo[thisScaleDates] <- this$.weights[timeIndex, zooIndex]
        }
        weightsZoo[targetDates >= last(weightsDates)] <- this$.weights[NROW(this$.weights), zooIndex]
        toBeScaled[[zooIndex]] <- toBeScaled[[zooIndex]] * weightsZoo
    }
    toBeScaled
})

method("weights", "Weights", function(this, ...) {
    checkSame(1, nrow(this$.weights))
    as.vector(this$.weights[nrow(this$.weights), ])
})

method("requireParallelTo", "Weights", function(this, items, ...) {
    checkLength(items, NCOL(this$.weights)) 
})



