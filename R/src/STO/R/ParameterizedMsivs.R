constructor("ParameterizedMsivs", function(msivs = NULL, weights = NULL, runs = NULL) {
    this <- extend(RObject(), "ParameterizedMsivs", .msivs = msivs, .weights = weights, .runs = runs)
    constructorNeeds(this, msivs="list(MSIV)", weights = "numeric", runs="numeric")
    if (inStaticConstructor(this)) return(this)
    checkLength(runs, length(msivs))
    checkLength(msivs, length(weights))
    this
})

method("curve", "ParameterizedMsivs", function(this, curveCube, ...) {
    needs(curveCube = "CurveCube")
    curveCube$pmCurve(this)
})

method("scale", "ParameterizedMsivs", function(this, lookupFunction, ...) {
    needs(lookupFunction="function")
    # lookupFunction: function(msiv, run)
    lapply(1:length(this$.msivs), function(i) this$.weights[[i]] * lookupFunction(this$.msivs[[i]], this$.runs[[i]]))
})

method("metrics", "ParameterizedMsivs", function(this, metricCube, metricList, ...) {
    needs(metricCube="MetricCube", metricList="list(Metric)")
    result <- Map("Metric", "numeric")
    for(metric in metricList) {
        weightedMetric <- function(i) this$.weights[[i]] * this$.oneMetric(metricCube, metric, i)
        result$set(metric, sum(sapply(seq_along(this$.msivs), weightedMetric)))
    }
    result
})

method(".oneMetric", "ParameterizedMsivs", function(this, metricCube, metric, index, ...) {
    needs(metricCube="MetricCube", metric="Metric", index="numeric|integer")

    curveCube <- metricCube$curveCube()
    getMetricValues <- function(metric, msiv) {
        metricCube$oneValue(metric, this$.msivs[[index]], this$.runs[[index]])
    }
    getCurves <- function(msiv) {
        list(curveCube$curve(this$.msivs[[index]], this$.runs[[index]]))
    }
    metric$calculate(this$.msivs[[index]], getMetricValues, getCurves)
})

