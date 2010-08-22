# Get 3rd Friday of month maturities

constructor("EquityATMImpliedVol", function(ticker = NULL,daysToExpiration = NULL,strikeIncrement = NULL,...) {
	this <- extend(Transformation(), "EquityATMImpliedVol")
	if(inStaticConstructor(this)) return(this)	
	this$.ticker <- ticker
	this$.daysToExpiration <- daysToExpiration	
	this$.strikeIncrement <- strikeIncrement
	this$.minVolPercent <- 0.75
	this$.maxVolPercent <- 1.25
	this$.minDataPoints <- 3
	this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
	this
})

method("setDate",  "EquityATMImpliedVol", function(this, date, ...) {
	needs(date="POSIXct")
	this$.date <- date
	this$.yesterday <- businessDaysAgo(1,this$.date,'nyb')
	this$.targetExpirationDate <- Period('days',this$.daysToExpiration)$advance(as.POSIXct(this$.date))
})

method("equitySeriesDefinition", "EquityATMImpliedVol", function(this,field,...) {
	SeriesDefinition('EQUITY', toupper(this$.ticker),field)
})

method(".inputSeries", "EquityATMImpliedVol", function(this, ...) {
	c(
		list(Stock = this$equitySeriesDefinition('LastPrice'))		
	)
})

method("initialize", "EquityATMImpliedVol", function(this,...) {
	cds <- SingleNameCDS(eqTicker = this$.ticker)
	this$.closeStock <- as.numeric(cds$adjClosePrices(startDate = this$.yesterday,endDate = this$.yesterday))
	this$.strikes <- this$getStrikes() 
	this$.yearMonths <- this$yearMonths()
	this$.optionTickers <- this$optionTickers(this$.strikes,this$.yearMonths,'p')
	this$.expirations <- this$expirations(this$.optionTickers)
	this$.yearMonths <- this$.yearMonths[!is.na(this$.expirations)]
	this$.optionTickers <- this$.optionTickers[,!is.na(this$.expirations)]
	this$.expirations <- this$.expirations[!is.na(this$.expirations)]
	colnames(this$.optionTickers) <- this$.expirations
	"SUCCESS"
})

method("yearMonths",  "EquityATMImpliedVol", function(this,...) {
	todayRef <- as.POSIXlt(this$.date)
	limitDate <- Period('months',12)$advance(as.POSIXct(todayRef))
	todayRef$mday <- 1
	year <- todayRef$year + 1900	
	next12Months <- sapply(as.numeric(c(1:12)),function(x){as.character(Period('months',x)$advance(as.POSIXct(todayRef)))})
	yearMonths <- unique(as.character(c(todayRef,next12Months)))
	yearMonths[yearMonths>=todayRef & yearMonths < limitDate]	
})

method("getOptionTicker",  "EquityATMImpliedVol", function(this,yearMonth,strike,optionType,...) {
	optionTicker <- toupper(squish(this$.ticker,' ',yearMonth,' ',optionType,strike))
	optionTicker <- BloombergSecurity(squish(optionTicker,' Equity'))$stringValue('Ticker')
	optionTicker <- strsplit(optionTicker,'\\+')[[1]][1]
	squish(optionTicker,squish(' ',yearMonth,' ',toupper(optionType),toupper(strike),' Equity'))
})

method("optionTickers",  "EquityATMImpliedVol", function(this,strikes,yearMonths,optionType,...) {			
	surface <- matrix(NA,nrow = NROW(strikes),ncol = NROW(yearMonths),dimnames = list(strikes,yearMonths))	
	for(mat in 1:NCOL(surface)){
		for(strike in 1:NROW(surface)){
			try({
				bloombergTicker <- this$getOptionTicker(format(as.POSIXct(yearMonths[mat]),'%m/%y'),strikes[strike],optionType);			
				surface[strike,mat] <- bloombergTicker
			},TRUE)
		}
	}	
	surface
})

method("expirations",  "EquityATMImpliedVol", function(this,optionTickers,...) {
	expirationDates <- rep(NA,NCOL(optionTickers))
	baseStrikeIndex <- 2
	for(yearMonth in 1:NCOL(optionTickers))		
		try(expirationDates[yearMonth] <- this$getOptionLiveData(optionTickers[baseStrikeIndex,yearMonth],'OPT_EXPIRE_DT'),TRUE) 
	expirationDates
})

method("skipUpdate", "EquityATMImpliedVol", function(this, inputs, ...) {
	if(is.null(this$.lastUpdate)) return(FALSE)
	currentTime <- this$time()
	diffTime <- as.POSIXct(currentTime) - as.POSIXct(this$.lastUpdate)
	diffMinutes <- as.numeric(diffTime,units = 'mins')	
	if(diffMinutes > 5) return(FALSE)
	TRUE
})

method(".outputSeries", "EquityATMImpliedVol", function(this, ...) {   
	list(        
		LastImpliedVol = this$equitySeriesDefinition('Last91dImpliedVol'),		
		TimestampVol = this$equitySeriesDefinition('TimestampVol')
	)
})

method("time", "EquityATMImpliedVol", function(this,...) {
	format(Sys.time(), "%Y/%m/%d %H:%M:%S")
})

method("outputValues", "EquityATMImpliedVol", function(this, inputs, ...) {
	currentStock <- this$value(inputs, "Stock")
	lastImpliedVol <- this$getInterpolatedVol(currentStock)
	this$.lastUpdate <- this$time()
	list(
		this$outputs()$LastImpliedVol$valueString(lastImpliedVol),
		this$outputs()$TimestampVol$now()			
	)
})

method("getStrikes",  "EquityATMImpliedVol", function(this,currentStock = this$.closeStock,strikeIncrement = this$.strikeIncrement,...) {
	baseStike <- roundToNearest(currentStock,strikeIncrement)
	baseStike + strikeIncrement * seq(-1,1)	
})

method("getOptionLiveData",  "EquityATMImpliedVol", function(this,bloombergTicker,field,...) {
	BloombergSecurity(bloombergTicker)$stringValue(field)
})

method("getCurrentIVolSurface",  "EquityATMImpliedVol", function(this,optionTickers,field,...) {			
	surface <- matrix(NA,nrow = NROW(optionTickers),ncol = NCOL(optionTickers),dimnames = list(rownames(optionTickers),colnames(optionTickers)))	
	for(mat in 1:NCOL(surface)){
		for(strike in 1:NROW(surface)){
			try(surface[strike,mat] <- as.numeric(this$getOptionLiveData(optionTickers[strike,mat],field)),TRUE)
		}
	}
	surface
})

method("getCleanSurface",  "EquityATMImpliedVol", function(this,surface,...) {
	newSurface <- surface
	# Interpolate when possible with y = alpha exp(beta x))
	x <- as.numeric(as.POSIXct(colnames(newSurface)))	
	for(i in 1:NROW(newSurface)){		
		y <- surface[i,]
		if(NROW(y[!is.na(y)])>=this$.minDataPoints){
			lm.model <- lm(log(y)~x)
			minVol <- this$.minVolPercent *  min(y,na.rm = TRUE)
			maxVol <- this$.maxVolPercent *  max(y,na.rm = TRUE)
			newSurface[i,] <- exp(predict(lm.model,newdata = data.frame(x)))
		}		
	}
	newSurface
})

method("getInterpolatedVol",  "EquityATMImpliedVol", function(this,currentStock,...) {
	putVolSurface <- this$getCurrentIVolSurface(this$.optionTickers,'OPT_IMPLIED_VOLATILITY_MID')	
	cleanSurface <- this$getCleanSurface(putVolSurface)	
	qf.interpolateXY(
		currentStock,as.numeric(this$.targetExpirationDate),
		as.numeric(rownames(cleanSurface)),
		as.numeric(as.POSIXct(colnames(cleanSurface))),
		cleanSurface
	)/100
})