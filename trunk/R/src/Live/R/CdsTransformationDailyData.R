constructor("CdsTransformationDailyData", function(tsdb.ticker = NULL, date = NULL, ...) {
    this <- extend(RObject(), "CdsTransformationDailyData", .tsdb.ticker = tsdb.ticker, .date = date)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, tsdb.ticker="character?", date="POSIXct")
    this$.yesterday <- this$date(-1)
    this
})

method("date", "CdsTransformationDailyData", function(this, days = NULL, ...) {
    needs(days = "integer|numeric?")
    if (is.null(days)) return(this$.date)
    getFincadDateAdjust(this$.date, "d", days, this$holidays())
})

method("yesterday", "CdsTransformationDailyData", function(this,...) {
    this$.yesterday
})

method("holidays", "CdsTransformationDailyData", function(this, ...) {
    lazy(this$.holidays, HolidayDataLoader$getHolidays(source = "financialcalendar", financialCenter = "nyb"), log=FALSE)
})

method("closingSpread", "CdsTransformationDailyData", function(this, ...) {
    lazy(this$.closing.spread, this$oneClose(squish(this$.tsdb.ticker, "_spread_5y")) * 10000)
})

method("oneClose", "CdsTransformationDailyData", function(this, tsName, ...) {
    needs(tsName = "character") 
    res <- TimeSeriesDB()$retrieveOneTimeSeriesByName(tsName, 'internal', start=this$.yesterday, end=this$.yesterday)
    if(length(res) == 0) return(NA)
    the(res)
})

method("closeIRS", "CdsTransformationDailyData", function(this, ...) {
	getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",SwapCache$.live.swap.string.tenors,'internal',startDate = this$yesterday(),endDate = this$yesterday())
})