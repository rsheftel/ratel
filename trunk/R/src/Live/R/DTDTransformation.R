constructor("DTDTransformation", function(ticker = NULL,...) {
    this <- extend(Transformation(), "DTDTransformation")
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, ticker="character")
    this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
	this$.cds <- SingleNameCDS(ticker)	
	this$.equityTicker <- this$.cds$equityTicker()
    cat("DTDTransformation(",ticker,")\n")
    this
})

method("setDate",  "DTDTransformation", function(this, date, ...) {
    needs(date="POSIXct")
    this$.data <- CdsTransformationDailyData(this$.cds$.ticker, date)
	this$.yesterday <- this$.data$yesterday()
})

method("seriesDefinition", "DTDTransformation", function(this,field,...) {
	SeriesDefinition('MARKETDATA', toupper(squish(this$.cds$.ticker, "5Y")),field)
})

method("equitySeriesDefinition", "DTDTransformation", function(this,field,...) {
	SeriesDefinition('EQUITY', toupper(this$.equityTicker),field)
})

method("dtdSeriesDefinition", "DTDTransformation", function(this,field,...) {
	SeriesDefinition('MARKETDATA', toupper(squish('DTD.',this$.cds$.ticker)),field)
})

method(".inputSeries", "DTDTransformation", function(this, ...) {
    c(
        list(Spread = this$seriesDefinition('CDS','LastSpread')),
		list(TRI = this$seriesDefinition('CDS','LastTRI')),		
		list(Stock = this$equitySeriesDefinition('LastPrice')),
		list(SharesOutstanding = this$equitySeriesDefinition('LastSharesOutstanding')),
		list(StockVol = this$equitySeriesDefinition('Last91dImpliedVol')),
		list(IGTRI = SeriesDefinition('CDSTRI','CDXNAIG_snrfor_usd_xr_5y','LastTRI')),
		list(HVTRI = SeriesDefinition('CDSTRI','CDXNAIGHVOL_snrfor_usd_xr_5y','LastTRI')),
        list(Rate = SeriesDefinition('MARKETDATA','irs_usd_rate_5y','LastPrice'))
    )
})

method(".outputSeries", "DTDTransformation", function(this, ...) {   
    list(        
        LastDTD = this$seriesDefinition('CDS','LastDTD'),
		LastRichCheap = this$seriesDefinition('CDS','LastRichCheap'),
		LastDTDTRI = this$seriesDefinition('CDS','LastDTDTRI'),
        Timestamp = this$seriesDefinition('CDS',"Timestamp"),
        HighPrice = this$dtdSeriesDefinition('HighPrice'),
        LastPrice = this$dtdSeriesDefinition('LastPrice'),
        LastVolume = this$dtdSeriesDefinition('LastVolume'),
        LowPrice = this$dtdSeriesDefinition('LowPrice'),
        OpenPrice = this$dtdSeriesDefinition('OpenPrice'),
        MDTimestamp = this$dtdSeriesDefinition('Timestamp')
    )
})


method("loadData", "DTDTransformation", function(this,...) {
	read.table(
		squish(dataDirectory(),'Market Systems/Linked Market Systems/Credit/StrategyDTDRichCheap/PDF/',as.character(as.Date(this$.yesterday)),'.csv'),
		sep = ',',header = TRUE
	)
})

method("initialize", "DTDTransformation", function(this,...) {
	this$.seedSigma <- this$.cds$assetVols(startDate = this$.yesterday,endDate = this$.yesterday)
	this$.seedAsset <- this$.cds$assetValues(startDate = this$.yesterday,endDate = this$.yesterday)	
	if(is.null(this$.seedSigma))this$.seedSigma <- 0.5
	if(is.null(this$.seedAsset))this$.seedAsset <- 1
	this$.liability <- this$.cds$liabilities(startDate = this$.yesterday,endDate = this$.yesterday)
	this$.time <- 5
	data <- this$loadData()
	this$.oasData <- data[,'OAS']
	this$.dtdData <- data[,'DTD']	
	this$.richCheapCalculator <- DTDRichCheapCalculator()
	this$.richCheapCalculator$.time <- this$.time
	this$.richCheapCalculator$.matDate <- seq(from = this$.richCheapCalculator$.baseDate,by = paste(this$.richCheapCalculator$.time*12,"month"),length=2)[2]
	this$.richCheapCalculator$.loessfun <- loess(this$.oasData~this$.dtdData, span = 0.9, na.action = na.exclude)
	this$.loessCoeff <- SingleNameCDS$coefficientsOnDate(this$.yesterday)
})

method("skipUpdate", "DTDTransformation", function(this, inputs, ...) {
	# Add equity and vol filter...
    if(this$changed(inputs, "Spread")) return(FALSE)
    !SwapCache$needsUpdate()
})

method("outputValues", "DTDTransformation", function(this, inputs, ...) {
	rate <- this$value(inputs, "Rate")/100
	sharesOutstanding <- this$value(inputs, "SharesOutstanding")
	stock <- this$value(inputs, "Stock")
	oas <- this$value(inputs, "Spread")
	stockVol <- this$value(inputs, "StockVol")
	# DTD calculation
	currentDTD <- DTDCalculator$getDTD(
		this$.seedSigma,this$.seedAsset,this$.liability,
		sharesOutstanding, stock, stockVol, this$.time, rate,TRUE	
	)
	# Rich Cheap calc
	currentFairValue <- this$.richCheapCalculator$getLoessFV(currentDTD,this$.loessCoeff,rate)			
	currentRichCheap <- oas - currentFairValue	 
    list(        
        this$outputs()$LastDTD$valueString(currentDTD),
		this$outputs()$LastRichCheap$valueString(currentRichCheap),
		this$outputs()$LastDTDTRI$valueString(currentDTDTRI),
        this$outputs()$Timestamp$now(),
        this$outputs()$HighPrice$valueString(currentDTDTRI),
        this$outputs()$LastPrice$valueString(currentDTDTRI),
        this$outputs()$LastVolume$valueString(currentDTDTRI),
        this$outputs()$LowPrice$valueString(currentDTDTRI),
        this$outputs()$OpenPrice$valueString(currentDTDTRI),
        this$outputs()$MDTimestamp$now()
    )
})