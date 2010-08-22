constructor("FXTriTransformation", function(currencyPair=NULL,...) {
    this <- extend(Transformation(), "FXTriTransformation",.currencyPair=currencyPair)
    if(inStaticConstructor(this)) return(this)    
	constructorNeeds(this,currencyPair="character")
	this$.FXNotional <- FXNotional(0.0001,"over")
    this$.FXCurr <- FXCurr$setByCross(currencyPair)
	this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
	this$.conversionCurrency <- NULL
	if (!(this$.FXCurr$over()=="usd")) this$.conversionCurrency <- ReturnCorrectFXPair(this$.FXCurr$over(),"usd")
    cat("FXTriTransformation()\n")
    this
})

method("setDate",  "FXTriTransformation", function(this, date, ...) {			
    needs(date="POSIXct")
	this$.date <- date
	this$.yesterday <- getFincadDateAdjust(date, "d",-1, this$holidays())
	this$.notInitialized <- TRUE
})

method("holidays", "FXTriTransformation", function(this, ...) {
	lazy(this$.holidays, HolidayDataLoader$getHolidays(source = "financialcalendar", financialCenter = "nyb"), log=FALSE)
})

method("getRateData", "FXTriTransformation", function(this, ...) {
	
		lazy(
		this$.data,
		this$.data <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(this$.FXCurr$over(),this$.FXCurr$under(),"_spot_rate_mid"),data.source = 'internal',this$.yesterday,this$.yesterday)),
	log=FALSE)
})

method("getCloseTri", "FXTriTransformation", function(this, ...) {
	lazy(
		this$.closeTri,
		this$.closeTri <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(this$.FXCurr$over(),this$.FXCurr$under(),"_spot_tri_local_ccy"),data.source = 'internal',this$.yesterday,this$.yesterday)),
	log=FALSE)
})


method("getConversionRateClose","FXTriTransformation", function(this,...)
{
	this$.conversionRateClose <- 1
	if (!is.null(this$.conversionCurrency)) {
		this$.conversionRateClose <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(this$.conversionCurrency$over(),this$.conversionCurrency$under(),"_spot_rate_mid"),data.source = 'internal',this$.yesterday, this$.yesterday))
		if (this$.conversionCurrency$over()=="usd") this$.conversionRateClose <- (1/this$.conversionRateClose) 
	}
})

method(".inputSeries", "FXTriTransformation", function(this, ...) {
	if (is.null(this$.conversionCurrency)) return(list(rate = SeriesDefinition("MARKETDATA",squish("FX.",toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under()),".Rate.Spot"), "LastPrice")))
	return(list(rate = SeriesDefinition("MARKETDATA",squish("FX.",toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under()),".Rate.Spot"), "LastPrice"),
			conversionRate = SeriesDefinition("MARKETDATA",squish("FX.",toupper(this$.conversionCurrency$over()),toupper(this$.conversionCurrency$under()),".Rate.Spot"), "LastPrice")))            
})

method(".outputSeries", "FXTriTransformation", function(this, ...) {
    def <- function(field) SeriesDefinition("FX",squish(toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under())), field)
    list(
        LastPrice = def("LastPrice"),
		OpenPrice = def("OpenPrice"),
		HighPrice = def("HighPrice"),
		LowPrice = def("LowPrice"),
        Timestamp = def("Timestamp"),
		LastVolume = def("LastVolume")
    )
})

method("initialize", "FXTriTransformation", function(this, ...) {
    if(NROW(this$getRateData()) == 0 || NROW(this$getCloseTri()) == 0 || NROW(this$getConversionRateClose()==0)) return("Missing close")
    "SUCCESS"
})

method("skipUpdate", "FXTriTransformation", function(this, inputs, ...) {	
	FALSE
})

method("outputValues", "FXTriTransformation", function(this, inputs, ...) {	
	conversionRate <- 1
	if (!is.null(this$.conversionCurrency)) conversionRate <- this$value(inputs,"conversionRate")
	calc <- (1 - this$.data/this$value(inputs,"rate")) * this$.FXNotional$getNotional()*1000000
	values <- c(this$.data,this$value(inputs,"rate"))
	dates <- as.POSIXct(paste(as.character(c(this$.yesterday,this$.date))," 15:00:00"))
	
	res <- (this$getCloseTri() + calc) * conversionRate
	
	this$.notInitialized <- FALSE
		
    list(
        this$outputs()$LastPrice$valueString(res),
        this$outputs()$OpenPrice$valueString(this$getCloseTri()*this$.conversionRateClose),
		this$outputs()$HighPrice$valueString(max(this$getCloseTri()*this$.conversionRateClose,res)),
		this$outputs()$LowPrice$valueString(min(this$getCloseTri()*this$.conversionRateClose,res)),
		this$outputs()$LastVolume$valueString(1),
		this$outputs()$Timestamp$now()
    )
})