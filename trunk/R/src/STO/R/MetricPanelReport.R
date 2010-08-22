constructor("MetricPanelReport", function(sto = NULL, metrics = NULL, msiv = NULL, metricCube = NULL, ...) {
    this <- extend(RObject(), "MetricPanelReport", .sto = sto, .metrics = metrics, .msiv = msiv, .metricCube = metricCube)
    if (inStaticConstructor(this)) return(this)
    constructorNeeds(this, sto = "STO", metrics = "list(Metric)", msiv = "MSIV", metricCube = "MetricCube?")
    if (is.null(this$.metricCube)) this$.metricCube <- sto$metrics(withCurves=FALSE)
    this
})

method("statistics", "MetricPanelReport", function(this, values, ...) {
    v <- quantile(values, c(0, 0.1, 0.25, 0.5, 0.75, 0.9, 1))
    c(v, mean = mean(values), stdev = sd(values), count = length(values))
})

method("writeOneFilter", "MetricPanelReport", function(this, file, filter, ...) {
    dfArgs <- lapply(this$.metrics, function(metric) this$statistics(this$.metricCube$values(metric, this$.msiv, filter, calculate=FALSE)))
    names(dfArgs) <- sapply(this$.metrics, as.character)
    df <- do.call(data.frame, dfArgs)
    write.csv(df, file, row.names=TRUE)
})

method("writeOneMetric", "MetricPanelReport", function(this, file, filters, metric, ...) {
    metricValues <- lapply(filters, function(filter) this$.metricCube$values(metric, this$.msiv, filter, calculate=FALSE))
    names(metricValues) <- sapply(filters, name)
    if(isWindows())
        this$buildPlot(squish(file, ".png"), metricValues)

    dfArgs <- lapply(metricValues, function(values) this$statistics(values))
    names(dfArgs) <- sapply(filters, name)
    dfArgs <- c(dfArgs, check.names=FALSE)
    df <- do.call(data.frame, dfArgs)
    write.csv(df, squish(file, ".csv"), row.names=TRUE)
})

method("buildPlot", "MetricPanelReport", function(this, file, values, ...) {
    min <- min(sapply(values, min))
    max <- max(sapply(values, max))
    step <- (max - min) / 100
    yTicks <- pretty(c(min, max))
    yLabels <- format(pretty(c(min, max)), big.mark=",", sci=FALSE)
    maxLenX <- max(sapply(names(values), nchar))
    maxLenY <- max(sapply(yLabels, nchar))
    
    breaks <- seq(min, max, step)
    if(length(breaks) == 1) {
        file.copy(system.file("blank.png", package="STO"), file, overwrite=TRUE)
        return()
    }
    
    png(file, width=480, height=320+7*maxLenX)
    on.exit(dev.off())
    color <- function(start, end, stats) {
        median <- stats[[3]]
        if(median >= start && median < end)
            return("black")
        if(end <= stats[[1]] || start >= stats[[5]])
            return("yellow")
        if(end <= stats[[2]] || start >= stats[[4]])
            return("orange")
        return("red")
    }


    plot.new()
    par(mar=c(2+maxLenX * 0.3, 2+maxLenY * 0.3,0,0), las=2, new=FALSE)
    plot.window(c(0.5,length(values)+0.5), c(min, max))
    axis(2, yTicks, yLabels, cex.axis=0.65)
    grid(NA, NULL)
    b <- boxplot(values, plot=FALSE)

    for(i in seq_along(values)) {
        par(new = TRUE)
        h <- hist(values[[i]], breaks=breaks, plot=FALSE)
        colors <- sapply(seq_along(h$counts), function(c) color(h$breaks[[c]], h$breaks[[c+1]], b$stats[,i]))
        barplot(
            h$counts/max(h$counts)*0.8, 
            horiz=TRUE, 
            axes=FALSE, 
            xlim=c(-1, length(values)), 
            ylim=c(0, max-min), 
            space=0,
            col=colors,
            width=h$breaks[2]-h$breaks[1],
            offset=i-1
        )
    }
    axis(1, seq_along(values)-1, names(values), cex.axis=0.65)
    box()
})

method(".setUpDirectory", "MetricPanelReport", function(this, name, ...) {
    dirname <- this$.sto$dirname("html")
    if(!file.exists(dirname)) {
        dir.create(dirname)
        runtimeDir <- system.file("runtime", package="STO")
        copyDirectory(runtimeDir, squish(dirname, "/runtime"))
    }
    dirname <- squish(dirname, "/", name)
    templateDir <- system.file("reportTemplate", package="STO")
    if(file.exists(dirname))
        destroyDirectory(dirname)
    copyDirectory(templateDir, dirname)
    dirname
})

method(".generateByFilterData", "MetricPanelReport", function(this, dirname, filters, ...) {
    fileNames <- paste("filter", 1:length(filters), ".csv", sep="")
    titles <- sapply(filters, function(f) f$name())
    main <- data.frame(name = fileNames, title = titles)
    write.csv(main, file=squish(dirname, "/data/byFilter.csv"), row.names = FALSE)
    for(i in 1:length(filters)) {
        fname <- squish(dirname, "/data/filter", i, ".csv")
        this$writeOneFilter(fname, filters[[i]])
    }
})

method(".generateByMetricData", "MetricPanelReport", function(this, dirname, filters, ...) {
    metrics <- this$.metrics
    fileNames <- paste("metric", 1:length(metrics), sep="")
    titles <- sapply(metrics, as.character)
    main <- data.frame(name = fileNames, title = titles)
    write.csv(main, file=squish(dirname, "/data/byMetric.csv"), row.names = FALSE)
    for(i in 1:length(metrics)) {
        fname <- squish(dirname, "/data/metric", i)
        this$writeOneMetric(fname, filters, metrics[[i]])
    }
})

method("runReport", "MetricPanelReport", function(this, name, filters, openInBrowser=TRUE, ...) {
    dirname <- this$.setUpDirectory(name)
    this$.generateByFilterData(dirname, filters)
    this$.generateByMetricData(dirname, filters)
    reportFile <- squish(dirname, "/index.html")
    cat("Report generated:\n", reportFile, "\n", sep="")
    if(openInBrowser && isWindows())
        browseURL(squish("file:///", reportFile))
})
