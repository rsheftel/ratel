constructor("IncrementalVarConstraint", function(categories = NULL, incrementalAlpha = 0.1, overallAlpha = 0.1, overallVarRange = as.numeric(c(NA, NA))) {
    this <- extend(WeightedCurvesConstraint(), "IncrementalVarConstraint", 
        .categories = categories,
        .categoryLookup = Map("integer", "character"),
        .min = Map("character", "numeric"), 
        .max = Map("character", "numeric"),
        .incAlpha = incrementalAlpha,
        .minVaR = first(overallVarRange),
        .maxVaR = second(overallVarRange),
        .overallAlpha=overallAlpha
    )
    constructorNeeds(this, categories="character", incrementalAlpha="numeric", overallAlpha="numeric", overallVarRange="numeric")
    failUnless(length(overallVarRange) == 2, "overallVarRange should have 2 elements (min, max)")
    if(inStaticConstructor(this)) return(this)
    for(i in seq_along(categories))
        this$.categoryLookup$set(i, categories[[i]])
    this
})

method("set", "IncrementalVarConstraint", function(this, category, min, max, ...) {
    failUnless(category %in% this$.categories, "category ", category, " not in ", humanString(this$.categories))
    this$.min$set(category, min)
    this$.max$set(category, max)
})

method("rejects", "IncrementalVarConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
    
    if (!(is.na(this$.maxVaR)&&is.na(this$.minVaR))) {
        overallVaR <- this$.calcVar(wc, this$.overallAlpha)
        if (!(is.na(this$.maxVaR)))
            if (overallVaR < this$.maxVaR)
                return(this$rejected("Overall VaR: ",overallVaR," not in (",this$.minVaR,",",this$.maxVaR,")"))
            
        if (!(is.na(this$.minVaR)))
            if (overallVaR > this$.minVaR)
                return(this$rejected("Overall VaR: ",overallVaR," not in (",this$.minVaR,",",this$.maxVaR,")"))
    }        
    var <- this$.calcIncVar(wc, this$.incAlpha)
    categoryTotals <- Map("character", "numeric")
    for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
    for(i in this$.categoryLookup$keys()) {
        cat <- this$.categoryLookup$fetch(i)
        categoryTotals$set(cat, categoryTotals$fetch(cat) + var[[i]])
    }
# dout(categoryTotals)
    for(cat in categoryTotals$keys()) {
        tot <- categoryTotals$fetch(cat)
        min <- this$.min$fetch(cat)
        max <- this$.max$fetch(cat)
        if(tot < min || tot > max)
            return(this$rejected("IncVar: ", cat, " = ", tot, " not in (", min, ",", max, ")"))
    }
    return(FALSE)
})


method("distance", "IncrementalVarConstraint", function(this, wc, ...) {
		needs(wc="WeightedCurves")
		
		distance <- 0
		if (!(is.na(this$.maxVaR)&&is.na(this$.minVaR))) {
			overallVaR <- this$.calcVar(wc, this$.overallAlpha)
			if (!(is.na(this$.maxVaR))&&!(is.na(this$.minVaR))) {
				if ((this$.maxVaR-this$.minVaR)==0) {
					res <- (min(overallVaR-this$.minVaR,0)^2 + max(overallVaR-this$.maxVaR,0)^2)
				}
				else res <- (min(overallVaR-this$.minVaR,0)^2 + max(overallVaR-this$.maxVaR,0)^2)/(this$.maxVaR - this$.minVaR)^2 
				distance <- distance + res
			}
		}        
		var <- this$.calcIncVar(wc, this$.incAlpha)
		categoryTotals <- Map("character", "numeric")
		for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
		for(i in this$.categoryLookup$keys()) {
			cat <- this$.categoryLookup$fetch(i)
			categoryTotals$set(cat, categoryTotals$fetch(cat) + var[[i]])
		}
# dout(categoryTotals)
		for(cat in categoryTotals$keys()) {
			tot <- categoryTotals$fetch(cat)
			min <- this$.min$fetch(cat)
			max <- this$.max$fetch(cat)
			if ((max-min)==0) {
				res <- (min(tot-this$.min,0)^2 + max(tot-this$.max,0)^2)
			}
			else res <- (min(tot-min,0)^2 + max(tot-max,0)^2)/(max - min)^2 
			distance <- distance + res
		}
		return(distance)
	})


method(".calcIncVar", "IncrementalVarConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
    z <- wc$oneZooUnweighted()
    failUnless(NROW(z)>= (1/this$.incAlpha), "Not enough observations for confidence level")

    myVaR <- function(adj) { expectedShortFallVaR(z, wc$weights() * (1.0 + adj), this$.incAlpha) }
    
    small.num <- 1e-7
    iVarForColumn <- function(i) {
        weights_up <- rep(0,ncol(z))
        weights_dn <- rep(0,ncol(z))
        weights_up[[i]] <- +0.5 * small.num
        weights_dn[[i]] <- -0.5 * small.num
        iv <- (myVaR(weights_up) - myVaR(weights_dn)) / small.num
        return(iv)
    }
    ivar <- sapply(seq_len(ncol(z)), iVarForColumn)
    names(ivar) <- colnames(z)
    ivar / myVaR(0)
})

method(".calcVar", "IncrementalVarConstraint", function(this, wc, ...) {
    needs(wc = "WeightedCurves")
    z <- wc$oneZooUnweighted()
    failUnless(NROW(z)>= (1/this$.overallAlpha), "Not enough observations for confidence level")
    return(expectedShortFallVaR(z, wc$weights(), this$.overallAlpha))
})

method(".scaledCurveVars", "IncrementalVarConstraint", function(this, wc, ...) {
    var <- function(curve) this$.calcVar(WeightedCurves(list(curve)))
    vars <- sapply(wc$scaledCurves(), var)
    names(vars) <- colnames(wc$oneZooUnweighted())
    vars
})

method("report", "IncrementalVarConstraint", function(this, wc, connection, ...) {
    needs(wc="WeightedCurves", connection="connection")
    output <- function(...) cat(..., "\n", file=connection)
    catVars <- function(vars) {
        for(curveName in names(vars))
            output(curveName, " Value,", vars[[curveName]])
    }

    output("Overall 1-day 99% Short Fall VaR:", this$.calcVar(wc))
    output("Individual VaRs (", this$.overallAlpha * 100, "%) at weights:", sep="")
    catVars(this$.scaledCurveVars(wc))
    output("Incremental VaRs (", this$.incAlpha * 100, "%) at weights:", sep="")
    catVars(this$.calcIncVar(wc))
    
})

method("as.character","IncrementalVarConstraint",function(this,...) {
		out <- squish("Incremental VaR Constraint","\n")
		if (!is.na(this$.maxVaR)) out <- squish(out,"Max VaR Constraint:",this$.maxVaR,"\n")
		if (!is.na(this$minVaR)) out <- squish(out,"Min VaR Constraint:",this$.minVaR,"\n")
		categoryTotals <- Map("character", "numeric")
		for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
		for(i in this$.categoryLookup$keys()) {
			cat <- this$.categoryLookup$fetch(i)
			categoryTotals$set(cat, 0)
		}
		for(cat in categoryTotals$keys()) {
			min <- this$.min$fetch(cat)
			max <- this$.max$fetch(cat)
			out <- squish(out,"IncVar: ",cat," max= ",max," min=",min,"\n")
		}
	})
