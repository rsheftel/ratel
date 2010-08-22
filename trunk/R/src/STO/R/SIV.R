
constructor("SIV", function(system.name = NULL, interval = NULL, version = NULL, ...) {
    this <- extend(RObject(), "SIV", 
        .system = system.name,
        .interval = interval,
        .version = version
    )
    constructorNeeds(this, system.name = "character", interval = "character", version = "character")
    this
})

method("m", "SIV", function(this, markets, ...) {
    needs(markets = "character")
    markets <- c(markets, ...)
    lapply(markets, function(market) MSIV(this, market))
})

method("as.character", "SIV", function(this, ...) {
    paste(this$.system, this$.version, this$.interval, sep = "_")
})

method("toCode", "SIV", function(this, ...) {
	squish("SIV", paren(commaSep(singleQuote(c(this$.system, this$.interval, this$.version)))))
})

method("constructFromFilename", "SIV", function(this, filename, ...) {
    needs(filename = "character")
    parts <- strsplit(filename, "_", fixed = TRUE)
    lapply(parts, function(p) { 
        assert(length(p) == 3, squish("malformed SIV filename string: ", paste(p, collapse = ",")))
        SIV(p[1], p[3], p[2]) 
    })
})
