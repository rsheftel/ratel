constructor("EmptyTestTransformation", function(ticker = NULL, ...) {
    this <- extend(Transformation(), "EmptyTestTransformation", .ticker = ticker)
    constructorNeeds(this, ticker="character")
    if(inStaticConstructor(this)) return(this)
    cat("In constructor (", this$.ticker, ")\n")
    this
})

method(".inputSeries", "EmptyTestTransformation", function(this, ...) {
    list(SeriesDefinition("MARKETDATA", this$.ticker, "LastPrice"))
})

method(".outputSeries", "EmptyTestTransformation", function(this, ...) {
    list(SeriesDefinition("TEST", this$.ticker, "BID"))
})

method("update", "EmptyTestTransformation", function(this, seriesWithValues, ...) {
    needs(seriesWithValues="character")    
    list()
})


