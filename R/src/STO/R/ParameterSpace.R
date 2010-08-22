constructor("ParameterSpace", function(...) {
    this <- extend(RObject(), "ParameterSpace", .data = NULL) 
    params <- data.frame(...)
    if (length(params) == 0) return(this) 
    params <- sapply(params, function(x) { 
        if(class(x) != "logical") 
            do.call(seq, as.list(x)) 
        else 
            unique(x[c(1,2)]) 
    }, simplify = FALSE)

    df <- expand.grid(params)
    df <- cbind(data.frame(run = 1:nrow(df)), df)
    this$.data = df
    this
})

method("paramDefinition", "ParameterSpace", function(this, param, ...) {
    unique <- this$uniqueValues(param)
    c(this$rawParamDefinition(param), length(unique))
})

method("rawParamDefinition", "ParameterSpace", function(this, param, ...) {
    unique <- this$uniqueValues(param)
    if(is.logical(unique)) {
        # reversed to sort TRUE, FALSE when both are present
        return(c(last(unique), first(unique), NA))
    }
    if(length(unique) == 1)
        return(c(the(unique), the(unique), 0))
    c(first(unique), last(unique), second(unique) - first(unique))
})

method("toCode", "ParameterSpace", function(this, ...) {
    defs <- sapply(this$colNames()[-1], function(param.name) {
        squish("\n\t", param.name, " = c(", commaSep(this$rawParamDefinition(param.name)), ")")
    }) 
	squish("ParameterSpace(", commaSep(defs), "\n)")
})

method("definition", "ParameterSpace", function(this, ...) {
    sapply(this$colNames()[-1], this$paramDefinition, simplify=FALSE)
})

method("as.character", "ParameterSpace", function(this, ...) {
    squish("\nparams: ", commaSep(this$colNames()), "\nruns:", commaSep(this$runs()), "\n")
})

method("colNames", "ParameterSpace", function(this, ...) {
    colnames(this$.data)
})

method("rowCount", "ParameterSpace", function(this, ...) {
    nrow(this$.data)
})

method("colCount", "ParameterSpace", function(this, ...) {
    ncol(this$.data)
})

method("subSet", "ParameterSpace", function(this, runs, colNames, ...) { 
    needs(runs = "numeric|integer", colNames = "character")
    colNames <- c("run", colNames)
    df <- this$.data[runs, colNames]
    ParameterSpace$fromData(df)
})

method("data", "ParameterSpace", function(this, ...) { 
    data.frame(this$.data)
})

method("runs", "ParameterSpace", function(this, ...) { 
    column(this$.data, "run")
})

method("writeCSV", "ParameterSpace", function(this, file, ...) {
    write.csv(this$.data, file, row.names = FALSE)
})

method("readCSV", "ParameterSpace", function(unused, file, ...) {
    data = read.csv(file, stringsAsFactors=FALSE)
    for(col in colnames(data))
        if(col != "run" && is.integer(data[[col]]))
            data[[col]] <- as.numeric(data[[col]])
    ParameterSpace$fromData(data)
})

method("fromData", "ParameterSpace", function(unused, data, ...) {
    needs(data = "data.frame")
    result <- ParameterSpace()
    result$.data = data
    result
})

method("uniqueValues", "ParameterSpace", function(this, paramName, ...) {
    needs(paramName="character")
    sort(the(unique(this$.data[paramName])))
})

method("filter", "ParameterSpace", function(this, expr, ...) { # uuuuugly
    df <- this$data()
    runs <- df$run[eval(eval(substitute(expression(expr))), df)]
    RunFilter$with(deparse(substitute(expr)), runs)
})

method("filtersExact", "ParameterSpace", function(this, ...) {
    filterParams <- list(...)
    failUnless(all(names(filterParams) %in% colnames(this$.data)))
    filtersForParam <- function(paramName, paramValues) {
        expressionStrings <- sapply(paramValues, function(value) squish(paramName, " == ", value))
        lapply(expressionStrings, function(str) eval(parse(text=squish("this$filter(", str, ")"))))
    }
    filters <- lapply(names(filterParams), function(param) filtersForParam(param, filterParams[[param]]))
    result <- RunFilter$cross(filters)
    for(filter in result)
        failIf(filter$isEmpty(), "parameters with no runs passed to filtersExact:\n", filter$name())
    result
})

method("filtersAll", "ParameterSpace", function(this, ...) {
    paramNames <- list(...)
    needs(paramNames="list(character)")
    filters <- lapply(paramNames, function(name) {
        values <- list(this$uniqueValues(name))
        names(values) <- name
        do.call(this$filtersExact, values)
    })
    RunFilter$cross(filters)
})
