constructor("Surface", function(paramSpace = NULL, metrics = NULL, aggregationFunction = NULL) {
    this <- extend(RObject(), "Surface", 
        .paramSpace = paramSpace,
        .metrics = metrics,
        .aggregationFunction = aggregationFunction,
        .surfaceData = NULL

    )
    constructorNeeds(this, paramSpace = "ParameterSpace", metrics = "numeric", aggregationFunction = "function")
    if(inStaticConstructor(this)) return(this)

    failUnless(
        paramSpace$colCount() == 3, 
        "must have 3 columns in paramSpace: (run, param1, param2).  Have: ", commaSep(paramSpace$colNames())
    )
    failUnless(first(paramSpace$colNames()) == "run", "first column in paramSpace must be run.  Have: ", first(colnames(paramSpace)))
    failUnless(paramSpace$rowCount() == length(metrics), 
        "mismatched parameters/metric lengths. ", 
        paramSpace$rowCount(), 
        " != ", 
        length(metrics), 
        "\nParameter Space:\n",
        paramSpace,
        "\nMetrics:\n",
        commaSep(metrics)
    )

    this$.populate()
    this
})

method(".populate", "Surface", function(this, ...) {
    params <- this$.paramSpace
    rowNames <- params$uniqueValues(second(params$colNames()))
    colNames <- params$uniqueValues(third(params$colNames()))
    this$.surfaceData <- Array("numeric", "numeric", "numeric", # param1, param2, metricValue
        rowNames, 
        colNames
    )

    for(row in rowNames) {
        for(col in colNames) {
            runIndices <- which(params$.data[2] == row & params$.data[3] == col)
            aggregateMetric <- this$.aggregationFunction(this$.metrics[runIndices])
            this$.surfaceData$set(row, col, aggregateMetric)
        }
    }

    this$.surfaceData$requireFull()
})

method("rownames", "Surface", function(this, ...) {
    this$.surfaceData$rownames()
})
method("colnames", "Surface", function(this, ...) {
    this$.surfaceData$colnames()
})
method("row", "Surface", function(this, rowValue, ...) {
    needs(rowValue="numeric")
    this$.surfaceData$fetchRow(rowValue)
})
method("column", "Surface", function(this, colValue, ...) {
    needs(colValue="numeric")
    this$.surfaceData$fetchColumn(colValue)
})

method("point", "Surface", function(this, rowValue, colValue, ...) {
    needs(rowValue="numeric", colValue="numeric")
    this$.surfaceData$fetch(rowValue, colValue)
})

method("plot3d", "Surface", function(this, ...) {
    data <- this$.surfaceData
    x <- data$rownames()
    y <- data$colnames()
    z <- data$.data
    
    names <- this$.paramSpace$colNames()
    simpleSurface3d(x,y,z, second(names), third(names))
})

method("as.data.frame", "Surface", function(this, ...){
	data <- data.frame(this$.surfaceData$.data)
	rownames(data) <- this$rownames()
	colnames(data) <- this$colnames()	
	return(data)
})

method("plotContour", "Surface", function(this, title = "", zRange = NULL, ...) {
    data <- this$.surfaceData
    x <- data$rownames()
    y <- data$colnames()
    z <- data$.data
    
    names <- this$.paramSpace$colNames()
    simpleContourPlot(x,y,z, second(names), third(names), title, zRange)
})


method("plotMultiLine", "Surface", function(this, viewAlong, ...) {
    data <- this$.surfaceData
    x <- data$rownames()
    y <- data$colnames()
    z <- data$.data
    
    names <- this$.paramSpace$colNames()
    if(viewAlong == second(names))
        viewAlongDim <- 'x'
    else if (viewAlong == third(names))
        viewAlongDim <- 'y'
    else
        fail(
            "viewAlong parameter must be one of the parameter names in Surface.\nIs: ", 
            viewAlong, 
            "\nOptions: ", 
            names[c(2,3)]
        )
    simpleMultiLinePlot(x,y,z, viewAlongDim, second(names), third(names))
})
