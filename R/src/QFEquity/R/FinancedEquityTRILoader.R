constructor("FinancedEquityTRILoader", function(tickerList = NULL, securityIDList = NULL,quoteType = "close"){
    this <- extend(RObject(), "FinancedEquityTRILoader")
    if(inStaticConstructor(this)) return(this)
    
    this$.tickerList <- NULL
	this$.securityIDList <- NULL
	assert(quoteType %in% c("open","close","high","low"))
	this$.quoteType <- quoteType
    
    if(!is.null(tickerList)) this$addTickerList(tickerList)
	if(!is.null(securityIDList)) this$addSecurityIDList(securityIDList)
    return(this)
})

method("addTickerList", "FinancedEquityTRILoader", function(this, tickerList, ...){
    needs(tickerList = 'character')
    this$.tickerList <- tickerList
	this$.securityIDList <- as.numeric(lapply(this$.tickerList, IvyDB()$lookupSecurityID))
})

method("addSecurityIDList", "FinancedEquityTRILoader",function(this, securityIDList, ...){
	needs(securityIDList = 'numeric')
	this$.securityIDList <- securityIDList
})

method("financeEquitiesUsingTickers", "FinancedEquityTRILoader", function(this, ...){
    if(is.null(this$.tickerList)) return("No equities loaded in FinancedEquityTRILoader$financeEquities()")
    output <- lapply(this$.tickerList, function(x) FinanceTRI()$equity(ticker = x, quoteType = this$.quoteType))
    names(output) <- this$.tickerList
    output
})

method("financeEquitiesUsingSecurityID", "FinancedEquityTRILoader", function(this, ...){
	if(is.null(this$.securityIDList)) return("No securityIDs loaded in FinancedEquityTRILoader$financeEquities()")
	output <- lapply(this$.securityIDList, function(x) FinanceTRI()$equity(securityID = x, quoteType = this$.quoteType))
	names(output) <- this$.securityIDList
	output
})

method("uploadOneFinancedEquityTRI", "FinancedEquityTRILoader", function(this, marketName, financedTRI, uploadPath = NULL, uploadMethod = 'file', source = 'internal', ...){
	if(is.null(financedTRI)) return()
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()
	
	if(this$.quoteType == "close") tsdbName <- paste(marketName, 'tri', 'vLibor', sep = "_")
	else tsdbName <- paste(marketName, this$.quoteType,'tri', 'vLibor', sep = "_")
	
	tsExists <- TimeSeriesDB()$timeSeriesExists(tsdbName)

	#PURGE
	if(uploadMethod == 'direct'){	
		if (tsExists) TimeSeriesDB()$purgeTimeSeries(tsdbName, source)
	}
	
	if(!tsExists){
		TimeSeriesDB()$createTimeSeries(
			tsdbName,
			attributes = list(
				hedge = "libor_usd_tri_on",
				quote_type = this$.quoteType,
				security_id = marketName,
				quote_side = "mid",
				instrument = "equity",
				transformation_output = "tri",
				transformation = "FinanceTRI.R"
			)
		)
	}
	
	uploadZooToTsdb(financedTRI, tsdbNames = tsdbName, tsdbSources = source,
		uploadMethod = uploadMethod, uploadFilename = tsdbName,
		uploadPath = uploadPath)
})

method("uploadFinancedEquityTRIs", "FinancedEquityTRILoader", function(this, financedTRIs, uploadPath = NULL, uploadMethod = 'file', source = 'internal', ...){
    if(is.null(financedTRIs)) return("No equities loaded in FinancedEquityTRILoader$uploadFinancedEquityTRIs()")
    
    for(market in names(financedTRIs))
		this$uploadOneFinancedEquityTRI(market, financedTRIs[[market]], uploadPath = uploadPath, uploadMethod = uploadMethod, source = source)
})

method("financeAndUploadEquityTRIs", "FinancedEquityTRILoader", function(this, tickerList = NULL, securityIDList = NULL, uploadPath = NULL, uploadMethod = 'file', ...){
    if(!is.null(tickerList)) this$addTickerList(tickerList)
	if(!is.null(securityIDList)) this$addSecurityIDList(securityIDList)
		    
    outputData <- this$financeEquitiesUsingSecurityID()
    this$uploadFinancedEquityTRIs(outputData, uploadPath, uploadMethod)
})    