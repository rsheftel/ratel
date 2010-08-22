constructor("WeightConstraint", function(categories = NULL) {
    this <- extend(WeightedCurvesConstraint(), "WeightConstraint", 
        .categories = categories,
        .categoryLookup = Map("integer", "character"),
        .min = Map("character", "numeric"), 
        .max = Map("character", "numeric")
    )
    constructorNeeds(this, categories="character")
    if(inStaticConstructor(this)) return(this)
    for(i in seq_along(categories))
        this$.categoryLookup$set(i, categories[[i]])
    this
})

method("set", "WeightConstraint", function(this, category, min, max, ...) {
    failUnless(category %in% this$.categories, "category ", category, " not in ", humanString(this$.categories))
    this$.min$set(category, min)
    this$.max$set(category, max)
})


method("distance","WeightConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
	dist <- 0
	weights <- wc$weights()
	categoryTotals <- Map("character", "numeric")
	for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
	for(i in this$.categoryLookup$keys()) {
		cat <- this$.categoryLookup$fetch(i)
		categoryTotals$set(cat, weights[[i]])
	}
# dout(categoryTotals)
	for(cat in categoryTotals$keys()) {
		tot <- categoryTotals$fetch(cat)
		min <- this$.min$fetch(cat)
		max <- this$.max$fetch(cat)
		if ((max-min)==0) {
			res <- (min(tot-min,0)^2 + max(tot-max,0)^2)
		}
		else res <- (min(tot-min,0)^2 + max(tot-max,0)^2)/(max - min)^2 
		dist <- dist + res
	}
	return(dist)	
})


method("as.character","WeightConstraint",function(this,...) {
		out <- squish("Weight Constraint","\n")
		categoryTotals <- Map("character", "numeric")
		for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
		for(i in this$.categoryLookup$keys()) {
			cat <- this$.categoryLookup$fetch(i)
			categoryTotals$set(cat, 0)
		}
		for(cat in categoryTotals$keys()) {
			min <- this$.min$fetch(cat)
			max <- this$.max$fetch(cat)
			out <- squish(out,"Weights:",cat," max= ",max," min=",min,"\n")
		}
		return(out)
	})


method('as.data.frame', 'WeightConstraint', function(this,...){
		output <- NULL
		nameList <- NULL
		categoryTotals <- Map("character", "numeric")
		for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
		for(i in this$.categoryLookup$keys()) {
			cat <- this$.categoryLookup$fetch(i)
			categoryTotals$set(cat, 0)
		}
		for(cat in categoryTotals$keys()) {
			min <- this$.min$fetch(cat)
			max <- this$.max$fetch(cat)
			output <- data.frame(cbind(output, min, max))
			nameList <- c(nameList, paste('WC', cat, c('Min', 'Max'), sep = '.'))
		}
		names(output) <- nameList
		return(output)			
	})