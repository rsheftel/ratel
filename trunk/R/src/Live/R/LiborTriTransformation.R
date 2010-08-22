constructor("LiborTriTransformation", function(...) {
    this <- extend(Transformation(), "LiborTriTransformation")
    if(inStaticConstructor(this)) return(this)    

    this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
    cat("LiborTriTransformation()\n")
    this
})

method("setDate",  "LiborTriTransformation", function(this, date, ...) {			
    needs(date="POSIXct")
	this$.date <- date
	this$.yesterday <- getFincadDateAdjust(date, "d",-1, this$holidays())
	this$.notInitialized <- TRUE
})

method("holidays", "LiborTriTransformation", function(this, ...) {
	lazy(this$.holidays, HolidayDataLoader$getHolidays(source = "financialcalendar", financialCenter = "nyb"), log=FALSE)
})

method("getRateData", "LiborTriTransformation", function(this, ...) {
	lazy(
		this$.data,
		this$.data <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName('libor_usd_rate_on',data.source = 'internal',this$.yesterday,this$.yesterday)),
	log=FALSE)
})

method("getCloseTri", "LiborTriTransformation", function(this, ...) {
	lazy(
		this$.closeTri,
		this$.closeTri <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName('libor_usd_tri_on',data.source = 'internal',this$.yesterday,this$.yesterday)),
	log=FALSE)
})

method(".inputSeries", "LiborTriTransformation", function(this, ...) {
    list(onRate = SeriesDefinition("MARKETDATA","libor_usd_rate_on", "LastPrice"))            
})

method(".outputSeries", "LiborTriTransformation", function(this, ...) {
    def <- function(field) SeriesDefinition("LIBORTRI",'ON', field)
    list(
        LastTRI = def("LastTRI"),
		LastDailyTRI = def("LastDailyTRI"),
        Timestamp = def("Timestamp")
    )
})

method("initialize", "LiborTriTransformation", function(this, ...) {
    if(NROW(this$getRateData()) == 0 || NROW(this$getCloseTri()) == 0) return("Missing close")
    "SUCCESS"
})

method("skipUpdate", "LiborTriTransformation", function(this, inputs, ...) {	
    if(this$.notInitialized) return(FALSE)	
	TRUE
})

method("outputValues", "LiborTriTransformation", function(this, inputs, ...) {	
	calc <- LiborTri(ccy = "usd",dataTimeStamp = "15:00:00",unitSett = "d",numUnitsSett = 1,acc = 2)	
	values <- c(this$.data,this$value(inputs,"onRate"))
	dates <- as.POSIXct(paste(as.character(c(this$.yesterday,this$.date))," 15:00:00"))
	
	print(values)
	
	res <- calc$getDailyZooSeries(zoo(values,dates)	,'on',this$.holidays)

	print(res)
	
	this$.notInitialized <- FALSE
	this$.lastRate <- this$value(inputs,"onRate")
		
    list(
        this$outputs()$LastTRI$valueString(this$getCloseTri() + last(res)),
        this$outputs()$LastDailyTRI$valueString(last(res)),
        this$outputs()$Timestamp$now()
    )
})