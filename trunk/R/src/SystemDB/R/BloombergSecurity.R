constructor("BloombergSecurity", function(ticker=NULL) {
    this <- extend(RObject(), "BloombergSecurity", .ticker = ticker, .jSecurity = NULL)
    constructorNeeds(this, ticker = "character")
    if(inStaticConstructor(this)) return(this)
    this$.ticker = ticker
    this$.jSecurity = JBloombergSecurity$by_String(ticker)
    this
})


method("stringValue", "BloombergSecurity", function(this, name, ...) {
    needs(name = "character")
    this$.jSecurity$string_by_String(name)
})

method("numberValue", "BloombergSecurity", function(this, name, ...) {
    needs(name = "character")
    this$.jSecurity$numeric_by_String(name)
})

method("observations", "BloombergSecurity", function(this, field, range = Range("1861/01/01", "2099/12/31"), ...) {
    needs(field="character", range="Range")
    observations.zoo(this$.jSecurity$observations_by_String_Range(field, range$jRange())) 
})

method("bars", "BloombergSecurity", function(this, interval, range = Range("1970/01/01", "2038/01/17"), ...) {
	bars <- JBars$rBars_by_List(this$.jSecurity$bars_by_Range_Interval(range$jRange(), JInterval$lookup_by_String(interval)))
    Symbol$barsToDataFrame(bars)
})
