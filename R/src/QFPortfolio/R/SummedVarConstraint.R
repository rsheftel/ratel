constructor("SummedVarConstraint", function(categories = NULL, summedAlpha = 0.1,varBudget = NULL) {
    this <- extend(WeightedCurvesConstraint(), "SummedVarConstraint", 
        .categories = categories,
        .categoryLookup = Map("integer", "character"),
        .min = Map("character", "numeric"), 
        .max = Map("character", "numeric"),
        .summedAlpha = summedAlpha,
		.varBudget = varBudget
    )
    constructorNeeds(this, categories="character", summedAlpha="numeric",varBudget="numeric")
    if(inStaticConstructor(this)) return(this)
    for(i in seq_along(categories))
        this$.categoryLookup$set(i, categories[[i]])
    this
})

method("set", "SummedVarConstraint", function(this, category, min, max, ...) {
    failUnless(category %in% this$.categories, "category ", category, " not in ", humanString(this$.categories))
    this$.min$set(category, min)
    this$.max$set(category, max)
})



method("distance","SummedVarConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
	dist <- 0
	var <- this$.calcSummedVar(wc)
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
			res <- (min(tot-min,0)^2 + max(tot-max,0)^2)
		}
		else res <- (min(tot-min,0)^2 + max(tot-max,0)^2)/(max - min)^2 
		dist <- dist + res
	}
	return(dist)	
})


method(".calcSummedVar", "SummedVarConstraint", function(this, wc, varFunc=expectedShortFallVaR,...) {
	needs(wc="WeightedCurves",varFunc="function")
    z <- wc$oneZooUnweighted()
    failUnless(NROW(z)>= (1/this$.summedAlpha), "Not enough observations for confidence level")
	myVaR <- function(adj) { varFunc(z, wc$weights() * (adj), this$.summedAlpha) }
    varForColumn <- function(i) {
		adj <- rep(0,ncol(z))
        adj[[i]] <- 1
        return(myVaR(adj))
		}
    var <- sapply(seq_len(ncol(z)), varForColumn)
	names(var) <- colnames(z)
	varUsage <- this$.calcVar(wc,alpha=this$.summedAlpha)/this$.varBudget
	(var*varUsage / sum(var))
})

method(".calcVar", "SummedVarConstraint", function(this, wc, alpha, varFunc=expectedShortFallVaR,...) {
    needs(wc = "WeightedCurves",varFunc="function")
    z <- wc$oneZooUnweighted()
    failUnless(NROW(z)>= (1/alpha), "Not enough observations for confidence level")
    return(varFunc(z, wc$weights(), alpha))
	
})


method(".scaledCurveVars", "SummedVarConstraint", function(this, wc, ...) {
		var <- function(curve) this$.calcVar(WeightedCurves(list(curve)),alpha=this$.summedAlpha)
		vars <- sapply(wc$scaledCurves(), var)
		names(vars) <- colnames(wc$oneZooUnweighted())
		vars
	})
method("report", "SummedVarConstraint", function(this, wc, connection, ...) {
    needs(wc="WeightedCurves", connection="connection")
    output <- function(...) cat(..., "\n", file=connection)
    catVars <- function(vars) {
        for(curveName in names(vars))
            output(curveName, "Allocation,", vars[[curveName]])
    }

	output("Overall 1-day (1%) Hist VaR,", this$.calcVar(wc,alpha=0.01,varFunc=histVaR))
	output("Overall 1-day (5%) Hist VaR,", this$.calcVar(wc,alpha=0.05,varFunc=histVaR))
    output("Overall 1-day (1%) Short Fall VaR,", this$.calcVar(wc,alpha=0.01))
    output("Overall 1-day (", this$.summedAlpha * 100, "%) Short Fall VaR,", this$.calcVar(wc,alpha=this$.summedAlpha),sep="")
#    catVars(this$.scaledCurveVars(wc))
    output("Risk allocation at (", this$.summedAlpha * 100, "%)", sep="")
    catVars(this$.calcSummedVar(wc))
    
})

method("as.character","SummedVarConstraint",function(this,...) {
		out <- squish("Summed VaR Constraint","\n")
		categoryTotals <- Map("character", "numeric")
		for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
		for(i in this$.categoryLookup$keys()) {
			cat <- this$.categoryLookup$fetch(i)
			categoryTotals$set(cat, 0)
		}
		for(cat in categoryTotals$keys()) {
			min <- this$.min$fetch(cat)
			max <- this$.max$fetch(cat)
			out <- squish(out,"Summed Var:",cat," max= ",max," min=",min,"\n")
		}
		return(out)
	})

method('as.data.frame', 'SummedVarConstraint', function(this,...){
		output <- data.frame(SVC.SummedAlpha = this$.summedAlpha, SVC.VarBudget = this$.varBudget)
		nameList <- c('SVC.SummedAlpha','SVC.VarBudget' )
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
			nameList <- c(nameList, paste('SVC', cat, c('Min', 'Max'), sep = '.'))
		}
		names(output) <- nameList
		return(output)			
})