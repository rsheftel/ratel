constructor("SimpleTestTransformation", function(ticker = NULL, ...) {
    this <- extend(Transformation(), "SimpleTestTransformation", .ticker = ticker)
    constructorNeeds(this, ticker="character")
    if(inStaticConstructor(this)) return(this)
    cat("In constructor (", this$.ticker, ")\n")
    this
})

method(".inputSeries", "SimpleTestTransformation", function(this, ...) {
    list(SeriesDefinition("MARKETDATA", this$.ticker, "LastPrice"))
})

method(".outputSeries", "SimpleTestTransformation", function(this, ...) {
    list(SeriesDefinition("TEST", this$.ticker, "BID"))
})

method("update", "SimpleTestTransformation", function(this, seriesWithValues, ...) {
    needs(seriesWithValues="character")    
    inputs <- SeriesDefinition$withValues(seriesWithValues)
    value <- inputs$fetch(the(this$inputs()))$value()
    cat(squish("\nInputs:\n", as.character(inputs), "\n"))
    list(the(this$outputs())$valueString(value))
})


