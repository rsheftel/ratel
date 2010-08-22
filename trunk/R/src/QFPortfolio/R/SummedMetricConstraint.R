constructor("SummedMetricConstraint", function(categories = NULL, summedMetric = NULL, metricBudget = NULL) {
    this <- extend(WeightedCurvesConstraint(), "SummedMetricConstraint", 
        .categories = categories,
        .categoryLookup = Map("integer", "character"),
        .min = Map("character", "numeric"), 
        .max = Map("character", "numeric"),
        .summedMetric = summedMetric,
		.metricBudget = metricBudget
    )
    constructorNeeds(this, categories="character", summedMetric="Metric",metricBudget="numeric")
    if(inStaticConstructor(this)) return(this)
    for(i in seq_along(categories))
        this$.categoryLookup$set(i, categories[[i]])
    this
})

method("set", "SummedMetricConstraint", function(this, category, min, max, ...) {
    failUnless(category %in% this$.categories, "category ", category, " not in ", humanString(this$.categories))
    this$.min$set(category, min)
    this$.max$set(category, max)
})

method("distance","SummedMetricConstraint", function(this, wc, ...) {
    needs(wc="WeightedCurves")
	dist <- 0
	metric <- this$.calcSummedMetric(wc)
	categoryTotals <- Map("character", "numeric")
	for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
	for(i in this$.categoryLookup$keys()) {
		cat <- this$.categoryLookup$fetch(i)
		categoryTotals$set(cat, categoryTotals$fetch(cat) + metric[[i]])
	}
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

method(".calcSummedMetric", "SummedMetricConstraint", function(this, wc,...) {
	needs(wc="WeightedCurves")
    z <- wc$scaledCurves()
	metricForColumn <- function(i) {z[[i]]$metric(this$.summedMetric)}
    metricObj <- sapply(seq_len(length(z)), metricForColumn)
	names(metricObj) <- colnames(z)
	metricUsage <- this$.calcMetric(wc)/this$.metricBudget
	(metricObj*metricUsage / sum(metricObj))
})

method(".calcMetric", "SummedMetricConstraint", function(this, wc,...) {
    needs(wc = "WeightedCurves")
    return(wc$curve()$metric(this$.summedMetric))
})

method(".scaledCurveMetrics", "SummedMetricConstraint", function(this, wc, ...) {
		metric <- function(curve) this$.calcMetric(WeightedCurves(list(curve)))
		metrics <- sapply(wc$scaledCurves(), metric)
		names(metrics) <- colnames(wc$oneZooUnweighted())
		vars
	})

method("report", "SummedMetricConstraint", function(this, wc, connection, ...) {
    needs(wc="WeightedCurves", connection="connection")
    output <- function(...) cat(..., "\n", file=connection)
    catMetrics <- function(metrics) {
        for(curveName in names(metrics))
            output(curveName, "Allocation,", vars[[curveName]])
    }
	output("Total Metric,", this$.calcMetric(wc))
    output("Metric Budget,", this$.metricBudget)
    catMetrics(this$.calcSummedMetric(wc))
})

method("as.character","SummedMetricConstraint",function(this,...) {
		out <- squish("Summed Metric Constraint","\n")
		categoryTotals <- Map("character", "numeric")
		for(cat in this$.min$keys()) categoryTotals$set(cat, 0)
		for(i in this$.categoryLookup$keys()) {
			cat <- this$.categoryLookup$fetch(i)
			categoryTotals$set(cat, 0)
		}
		for(cat in categoryTotals$keys()) {
			min <- this$.min$fetch(cat)
			max <- this$.max$fetch(cat)
			out <- squish(out,"Summed Metric:",cat," max= ",max," min=",min,"\n")
		}
		return(out)
	})

method('as.data.frame', 'SummedMetricConstraint', function(this,...){
		output <- data.frame(SMC.Metric = as.character(this$.summedMetric), SMC.Budget = this$.metricBudget)
		nameList <- c('SMC.Metric','SMC.MetricBudget' )
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
			nameList <- c(nameList, paste('SMC', cat, c('Min', 'Max'), sep = '.'))
		}
		names(output) <- nameList
		return(output)			
})