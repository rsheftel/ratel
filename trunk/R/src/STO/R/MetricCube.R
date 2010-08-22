constructor("MetricCube", function(msivs = NULL, runs = NULL, curveCube = NULL, doCalculate = TRUE) {
    this <- extend(RObject(), "MetricCube", 
        .metrics = Map("Metric", "Array"), # Metric->Array(MSIV, run)
        .msivs = msivs,
        .runs = runs,
        .curveCube = curveCube,
        .doCalculate = doCalculate
    )
    constructorNeeds(this, msivs="list", runs="numeric", curveCube="CurveCube?")

    this
})

method("as.character", "MetricCube", function(this, ...) {
    squish("MSIVs:\n", paste(sapply(this$.msivs, as.character), collapse="\n"), "\nRuns: ", paste(this$.runs, collapse=", "), "\nMetrics:\n", as.character(this$.metrics))
})

method("load", "MetricCube", function(this, dir, ...) {
    needs(dir="character")
    for(msiv in this$msivs()) 
        if (msiv$hasMetricFile(dir))
            this$.readOneMetricFile(msiv, dir)
})

method("curveCube", "MetricCube", function(this, ...) {
    this$.curveCube
})

method("calculate", "MetricCube", function(this, metrics, msivs=this$msivs(), ...) {
    needs(msivs="list(MSIV)|MSIV", metrics="Metric|list(Metric)")
    msivs <- listify(msivs)
    metrics <- listify(metrics)
    for(msiv in msivs) {
        cat("\n\ncalculating for msiv ", as.character(msiv), "\n\n")
        this$requireMsiv(msiv)
        for(metric in metrics) {
            cat("\n\ncalculating for metric ", as.character(metric), "\n\n")
            this$.cacheValues(metric, msiv)
        }
    }
})

method(".emptyArray", "MetricCube", function(this, ...) {
    Array("MSIV", "numeric", "numeric", this$.msivs, this$.runs)
})

method(".readOneMetricFile", "MetricCube", function(this, msiv, dir, ...) {
    df <- MetricCube$metricsDataFrame(msiv, dir) 
    dfRuns <- column(df, "run")
    index <- match(this$.runs, dfRuns)
    bombMissing(index)
    df <- df[index,]
    metrics <- lapply(colnames(df)[-1], function(name) Metric$fetch(name))
    for(metric in metrics) {
        if(!this$.metrics$has(metric))
            this$.metrics$set(metric, this$.emptyArray())
        this$.metrics$fetch(metric)$setRow(msiv, as.numeric(df[,as.character(metric)]))
    }
})

method("metricsDataFrame", "MetricCube", function(static, msiv, dir, ...) {
    needs(msiv="MSIV", dir="character")
    fileName <- squish(dir, "/", msiv$fileName(".csv"))
    df <- read.csv(fileName, colClasses = "numeric")
    df <- df[!duplicated(df[1]), , drop=FALSE]
    df
})

method(".cacheValues", "MetricCube", function(this, metric, msiv, metricValues = NULL, ...) {
    if(!this$.doCalculate) return();
    if(!this$.metrics$has(metric))
        this$.metrics$set(metric, this$.emptyArray())
    runCurves <- this$.metrics$fetch(metric)
    if(any(is.na(runCurves$fetchRow(msiv)))) {
        if(is.null(metricValues)) {
            getMetricValues <- function(metric, msiv) this$values(metric, msiv)
            getCurves <- function(msiv) this$.curveCube$runCurves(msiv)
            metricValues <- metric$calculate(msiv, getMetricValues, getCurves)
        }
        runCurves$setRow(msiv, metricValues)
    }
})

method("msivs", "MetricCube", function(this, ...) {
    this$.msivs
})

method("runNumbers", "MetricCube", function(this, ...) {
    this$.runs
})

method("requireMsiv", "MetricCube", function(this, msiv, ...) {
    failUnless(any(sapply(this$msivs(), function(m) isTRUE(all.equal(m, msiv)))), as.character(msiv), " not in cube.  Has:\n", commaSep(this$msivs()))
})

method("values", "MetricCube", function(this, metric, msiv, runFilter = NULL, calculate=TRUE, ...) {
    needs(metric="Metric", msiv="MSIV", runFilter="RunFilter?", calculate="logical")
    this$requireMsiv(msiv)
    if(calculate)
        this$.cacheValues(metric, msiv)
    values <- as.numeric(this$.metrics$fetch(metric)$fetchRow(msiv))
    if(!is.null(runFilter))
        values <- values[runFilter$where(this$.runs)]
    values
})

method("oneValue", "MetricCube", function(this, metric, msiv, run, ...) {
    needs(metric="Metric", msiv="MSIV", run="numeric")
    this$.cacheValues(metric, msiv)
    this$.metrics$fetch(metric)$fetch(msiv, run)
})

method("availableMetrics", "MetricCube", function(this, msiv = NULL, ...) {
    needs(msiv = "MSIV?")
    allMetrics <- this$.metrics$keys()
    if (is.null(msiv))
        return(allMetrics)
    hasMetric <- sapply(allMetrics, function(metric) !any(is.na(this$.metrics$fetch(metric)$fetchRow(msiv))))
    allMetrics[hasMetric]
})

method("writeCSV", "MetricCube", function(this, dir, msivs = this$msivs(), ...) {
    needs(dir="character", msivs="MSIV|list(MSIV)")
    for(msiv in listify(msivs)) {
        metrics <- this$.metrics$keys()
        empty.array <- array(NA, c(length(this$.runs), length(metrics)))
        df <- as.data.frame(empty.array)

        for(metric.index in 1:length(metrics))
            df[, metric.index] <- this$values(metrics[[metric.index]], msiv, calculate=FALSE)

        df <- cbind(data.frame(run = this$.runs), df)
        colnames(df)[-1] <- sapply(metrics, as.character)
        write.csv(df, squish(dir, "/", msiv$fileName(".csv")), row.names=FALSE)
    }
})
