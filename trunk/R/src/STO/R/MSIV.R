constructor("MSIV", function(siv=NULL, market.name = NULL) {
    this <- extend(RObject(), "MSIV", 
        .siv = siv,
        .market = market.name,
        ...toString = NULL
    )
    # TODO: This line should be as follows, but cannot due to problem with inheritance and static constructor
    # constructorNeeds(this, market.name = "character", siv = "SIV")
    constructorNeeds(this, market.name = "character?", siv = "SIV?")
    this
})

method("fileName", "MSIV", function(this, extra = "", ...) { 
    needs(extra = "character")
    squish(as.character(this), extra)
})

method("hasMetricFile", "MSIV", function(this, dir, ...) {
    filename <- squish(dir, "/", this$fileName(".csv"))
    file.exists(filename)
})

method("as.character", "MSIV", function(this, ...) {
    if(is.null(this$...toString))
        this$...toString <- squish(this$.siv$as.character(), "_", this$.market)
    this$...toString
})

method("market", "MSIV", function(this, ...) {
    this$.market
})

method("siv", "MSIV", function(this, ...) {
    this$.siv
})

method("constructFromFilename", "MSIV", function(this, filename, extra="", ...) {
    needs(filename = "character")
    if (extra != "") {
        extraRegex <- squish(extra, "$")
        assert(length(filename) == length(grep(extraRegex, filename)), squish("some filename did not match ", extra, "\nfiles: \n", filename))
        filename <- sub(extraRegex, "", filename)
    }
    parts <- strsplit(filename, "_", fixed = TRUE)
    markets <- sapply(parts, function(x) x[[length(x)]])
    siv.strings <- sapply(parts, function(x) paste(x[1:(length(x)-1)], collapse = "_"))
    mapply(function(siv, market) SIV$constructFromFilename(siv)[[1]]$m(market), siv.strings, markets, USE.NAMES=FALSE)
})

method("indicesOfAInB", "MSIV", function(this, a, b, ...) { # NOT indexesOfAInB - OED says 'indexes' is not a word
    needs(a="list|MSIV", b="list")
    if(inherits(a, "MSIV")) a <- list(a)
    result <- match(sapply(a, as.character), sapply(b, as.character))
    assert(!any(is.na(result)))
    result
})

method("hasRawData", "MSIV", function(this, ...) {
    TRUE
})

method("fromDir", "MSIV", function(class, dir, extra = "", ...) {
    requireDirectory(dir)
    files <- list.files(dir)
    assert(length(files) > 0, squish(dir, " does not have any files in it (or is not a valid directory)!"))

    MSIV$constructFromFilename(files, extra)
})

method("toCode", "MSIV", function(this, ...) {
  	squish("MSIV", paren(commaSep(c(this$.siv$toCode(), singleQuote(this$.market)))))
})
