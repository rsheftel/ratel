constructor("Portfolio", function(name = NULL, msivs=NULL, weights = rep(1, length(msivs))) {
    this <- extend(MSIV(), "Portfolio", .weights = Map("MSIV", "numeric"))
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, name="character", msivs="list", weights="numeric")

    requireAllMatchFirst(function(msiv) msiv$.siv, msivs)
    assert(length(msivs) == length(weights))
    assert(!(name %in% sapply(msivs, market)), "you cannot include a Portfolio in itself")
    this <- extend(MSIV(msivs[[1]]$.siv, name), "Portfolio", .weights = Map("MSIV", "numeric"))
    mapply(function(msiv, weight) this$.weights$set(msiv, weight), msivs, weights)
    this
})

method("constructFromFilename", "Portfolio", function(this, name, file, extra=NULL, ...) {
    needs(name="character", file="character|connection", extra="character?")
    df <- read.csv(file, header=FALSE, stringsAsFactors=FALSE, colClasses=c("character", "numeric"))
    subMsivs <- MSIV$constructFromFilename(column(df, 1))
    Portfolio(name, subMsivs, column(df, 2))
})

method("writeCSV", "Portfolio", function(this, file, ...) {
    needs(file="character|connection")
    df <- data.frame(msivs = sapply(this$.weights$keys(), as.character), weights = unlist(this$.weights$values()))
    write.table(df, file, sep=",", row.names=FALSE, col.names=FALSE)
})

method("pick", "Portfolio", function(this, lookupFunction, type, ...) {
    needs(lookupFunction="function", type="character")
    result <- Map("MSIV", type)
    values <- lapply(this$.weights$keys(), lookupFunction)
    result$setAll(this$.weights$keys(), values)
    result
})

method("scale", "Portfolio", function(this, lookupFunction, type, ...) {
    needs(lookupFunction="function", type="character")
    result <- this$pick(lookupFunction, type)
    for(msiv in result$keys())
        if(this$.weights$fetch(msiv) != 1) # performance optimization
            result$set(msiv, result$fetch(msiv) * this$.weights$fetch(msiv))
    result
})

method("hasRawData", "Portfolio", function(this, ...) {
    FALSE
})

method("withRuns", "Portfolio", function(this, runs, ...) {
    needs(runs = "numeric|integer")
    checkLength(runs, length(this$.weights$keys()))
    ParameterizedPortfolio(this, runs)
})

method("copy", "Portfolio", function(this, ...) {
    Portfolio(this$market(), this$.weights$keys(), unlist(this$.weights$values()))
})

method("toCode", "Portfolio", function(this, ...) {
    squish("Portfolio", paren(commaSep(c(
        singleQuote(this$.market), 
        squish("list", paren(commaSep(sapply(this$.weights$keys(), toCode)))),
        squish("c", paren(commaSep(this$.weights$values())))
    ))))
})
