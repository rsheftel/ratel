constructor("Symbol", function(name=NULL) {
    this <- extend(RObject(), "Symbol", .name = name)
    constructorNeeds(this, name = "character")
    if(inStaticConstructor(this)) return(this)

    this$.symbol = JSymbol$by_String_double(name, 1.0)
    this
})


method("barCount", "Symbol", function(this, ...) {
    this$.symbol$barCount()
})

method("series", "Symbol", function(this, ...) {
    bars <- this$.symbol$rBars()
    Symbol$barsToDataFrame(bars)
})

method("barsToDataFrame", "Symbol", function(static, bars, ...) {
    getBarsField <- function(bars, sig, func) { .jevalArray(.jfield(bars$.jobj, sig, func, convert=FALSE)) }
    convertNA <- function(v) { v[v == JBar$na()] <- NA; v } # we have to use a sentinel value for NA
    dates <- getBarsField(bars, "[J", "dates")
    
    index <- POSIXct.from.millis(dates)
    df <- data.frame(
    	open = getBarsField(bars, "[D", "open"),
    	high = getBarsField(bars, "[D", "high"),
    	low = getBarsField(bars, "[D", "low"),
    	close = getBarsField(bars, "[D", "close"),
    	openInterest = convertNA(getBarsField(bars, "[J", "openInterest")),
    	volume = convertNA(getBarsField(bars, "[J", "volume"))
    )
    zoo(df, order.by=index)
})

method("firstBarDate", "Symbol", function(this, ...) { 
    as.POSIXct(this$.symbol$firstBarDate())
})

method("lastBarDate", "Symbol", function(this, ...) { 
    as.POSIXct(this$.symbol$lastBarDate())
})