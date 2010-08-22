constructor("CdsTriTransformation", function(ticker = NULL,...) {
    this <- extend(Transformation(), "CdsTriTransformation")
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, ticker="character")
    this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
	this$.cds <- SingleNameCDS(ticker)	
    cat("CdsTriTransformation(",ticker,")\n")
    this
})

method("setDate",  "CdsTriTransformation", function(this, date, ...) {
    needs(date="POSIXct")
    this$.data <- CdsTransformationDailyData(this$.cds$.ticker, date)
})

method("seriesDefinition", "CdsTriTransformation", function(this,template,field,...) {	
	SeriesDefinition(template, toupper(squish(this$.cds$.ticker, ".5Y")),field)
})

method(".inputSeries", "CdsTriTransformation", function(this, ...) {
    c(
        list(Spread = this$seriesDefinition('CDS','LastSpread')),
        SwapCache$swapDefinitions()
    )
})

method(".outputSeries", "CdsTriTransformation", function(this, ...) {   
    list(        
        LastTRI = this$seriesDefinition('CDS','LastTRI'),
		LastDV01 = this$seriesDefinition('CDS','LastDV01'),
        Timestamp = this$seriesDefinition('CDS',"Timestamp"),
        HighPrice = this$seriesDefinition('MARKETDATA','HighPrice'),
        LastPrice = this$seriesDefinition('MARKETDATA','LastPrice'),
        LastVolume = this$seriesDefinition('MARKETDATA','LastVolume'),
        LowPrice = this$seriesDefinition('MARKETDATA','LowPrice'),
        OpenPrice = this$seriesDefinition('MARKETDATA','OpenPrice'),
        MDTimestamp = this$seriesDefinition('MARKETDATA','Timestamp')
    )
})

method("initialize", "CdsTriTransformation", function(this,idFrame = NULL,...) {
	if(is.null(idFrame))idFrame <- this$.cds$idFrame('spread')
	tsName <- idFrame[NROW(idFrame),'tsName']
	attrList <- TimeSeriesDB()$lookupAttributesForTimeSeries(tsName)
	this$.strike <- as.numeric(attrList[,'cds_strike'])
	this$.closeTri <- as.numeric(this$.cds$specificSeries('tri',this$.strike,'xr',startDate=this$.data$yesterday(),endDate=this$.data$yesterday()))
	
	this$.calculator <- SingleNameCDSTRI(cdsTicker = this$.cds$cdsTicker(),tenor = this$.cds$.tenor,strike = this$.strike,internalSource = 'internal')
	this$.calculator$setRange(startDate = this$.data$yesterday(),endDate = this$.data$yesterday())
	this$.calculator$loadIrsData(this$.data$closeIRS())
	this$.calculator$loadSpreadSeries()
	this$.calculator$loadRecoveryRates()
	this$.calculator$.irsDataSave <- this$.calculator$.irsData
	this$.calculator$.cdsDataSave <- this$.calculator$.cdsData
	this$.calculator$.recoveryDataSave <- this$.calculator$.recoveryData
    "SUCCESS"
})

method("skipUpdate", "CdsTriTransformation", function(this, inputs, ...) {	
    if(this$changed(inputs, "Spread")) return(FALSE)
    !SwapCache$needsUpdate()
})

method("getIRSZoo", "CdsTriTransformation", function(this,irsData,...) {
	zoo(matrix(irsData,ncol = NCOL(this$.calculator$.irsDataSave)),this$.data$date())	
})

method("getSpreadZoo", "CdsTriTransformation", function(this,spread,...) {
	zoo(matrix(spread/10000,ncol = NCOL(this$.calculator$.cdsDataSave)),this$.data$date())	
})

method("getRecoveryZoo", "CdsTriTransformation", function(this, ...) {
	zoo(as.numeric(this$.calculator$.recoveryDataSave),this$.data$date())
})

method("getResult", "CdsTriTransformation", function(this, spread, irsData, ...) {	
	this$.calculator$.irsData <- rbind(this$.calculator$.irsDataSave, this$getIRSZoo(irsData))
	this$.calculator$.cdsData <- rbind(this$.calculator$.cdsDataSave, this$getSpreadZoo(spread))
	this$.calculator$.recoveryData <- rbind(this$.calculator$.recoveryDataSave,this$getRecoveryZoo())
	this$.calculator$calcSpreadTRIs()
})

method("outputValues", "CdsTriTransformation", function(this, inputs, ...) {	
    irsData <- SwapCache$rates(inputs) * 100 
    currentSpread <- this$value(inputs, "Spread")
	res <- this$getResult(currentSpread,irsData)
	currentDV01 <- as.numeric(res$dv01Zoo)
	dailyTri <- as.numeric(res$dailyTriZoo)	
    currentTRI <- dailyTri + this$.closeTri
    list(        
        this$outputs()$LastTRI$valueString(currentTRI),
		this$outputs()$LastDV01$valueString(currentDV01),
        this$outputs()$Timestamp$now(),
        this$outputs()$HighPrice$valueString(currentTRI),
        this$outputs()$LastPrice$valueString(currentTRI),
        this$outputs()$LastVolume$valueString(currentTRI),
        this$outputs()$LowPrice$valueString(currentTRI),
        this$outputs()$OpenPrice$valueString(currentTRI),
        this$outputs()$MDTimestamp$now()
    )
})
