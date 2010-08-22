constructor("FinanceTRI", function(...){
    library(QFEquity)
    library(QFPairsTrading)
    extend(RObject(), "FinanceTRI", 
    .placeHolder = NULL)
})

method(".runPair", "FinanceTRI", function(this, seriesY = NULL, seriesX, hedgeRatio, source, stripTimes, marketValueHedge, baseTri,zooSeriesY = NULL,adjustLive = FALSE,...)
{
    needs(seriesY = "character?")
    needs(seriesX = "character")
    needs(hedgeRatio = "numeric")
    needs(source = "character")
    needs(stripTimes = "logical")
    needs(marketValueHedge = "logical")
    needs(baseTri = "numeric")
	needs(zooSeriesY = "zoo?")
    
    inputY <- if(is.null(zooSeriesY))TimeSeriesDB()$retrieveTimeSeriesByName(seriesY, data.source = source)[[1]] else zooSeriesY	
    inputX <- TimeSeriesDB()$retrieveTimeSeriesByName(seriesX, data.source = source)[[1]]
	if(is.null(inputY)) return(NULL)
	if(is.null(inputX)) return(NULL)
    
    needs(inputY = "zoo")
    needs(inputX = "zoo")
    
    inputPair <- Pair(seriesY = inputY, seriesX = inputX)
	     
    if(marketValueHedge){
		res <- getCumTriFromDailyTri(inputPair$constantHedgeRatioOfMarketValues(hedgeRatio = hedgeRatio, stripTimes = stripTimes), baseTri = baseTri)		
	}else res <- getCumTriFromDailyTri(inputPair$constantHedgeRatioOfNotional(hedgeRatio = hedgeRatio, stripTimes, stripTimes), baseTri = baseTri)
	
	if(adjustLive){
		res <- res + last(inputY) - last(res)
	}
	return(res)
})

method("pair", "FinanceTRI", function(this, seriesY, seriesX, hedgeRatio = 1.0, source = "internal", stripTimes = TRUE, marketValueHedge = TRUE, baseTri = 100,adjustLive = FALSE,...)
{
    this$.runPair(seriesY, seriesX, hedgeRatio, source, stripTimes, marketValueHedge, baseTri,adjustLive = adjustLive)
})

method("usingONLibor", "FinanceTRI", function(this, seriesY = NULL, hedgeRatio = 1.0, source = "internal", stripTimes = TRUE, baseTri = 100,zooSeriesY = NULL,adjustLive = FALSE,...)
{
    this$.runPair(seriesY, "libor_usd_tri_on", hedgeRatio, source, stripTimes, marketValueHedge = TRUE, baseTri,zooSeriesY = zooSeriesY, adjustLive = adjustLive)
})

method("equity", "FinanceTRI", function(this, ticker=NULL, securityID = NULL, fundingCostName = NULL,
	hedgeRatio = 1.0, source = "internal", stripTimes = TRUE, equitySource = "ivydb", baseTri = 100,
	quoteType = "close",adjustLive = TRUE,...)
{
    needs(ticker = "character?")
	needs(securityID = "numeric?")
    needs(equitySource = "character")
    
    if(is.null(securityID)) securityID <- EquityDataLoader$getSecurityIDFromTicker(ticker)
	
	if(is.null(securityID)) return("Bad ticker or securityID in FinanceTRI$equity()")
    
    if(equitySource == "ivydb")
        databaseName <- paste("ivydb", securityID, quoteType,"adj_price_mid", sep = "_")
        
    # default funding cost is O/N LIBOR    
    if(is.null(fundingCostName)){
		if(quoteType == 'close')
	        return(this$usingONLibor(databaseName, hedgeRatio, source, stripTimes, baseTri,adjustLive=adjustLive))
		if(quoteType %in% c('open','high','low'))
			return(this$runOpenHighLowSeries(securityID,quoteType, source))
	}
    this$.runPair(databaseName, fundingCostName, hedgeRatio, source, stripTimes, TRUE, baseTri,adjustLive = adjustLive)
})

method("runOpenHighLowSeries", "FinanceTRI", function(this,securityID,quoteType,source,...)
{
	closeFinancedTriSeriesName <- paste(securityID,"tri_vLibor", sep = "_")
	closeAdjSeriesName <- paste("ivydb", securityID,"close_adj_price_mid", sep = "_")
	ohlAdjSeriesName <- paste("ivydb", securityID,quoteType,"adj_price_mid", sep = "_")
	mergedSeries <- na.omit(getMergedTimeSeries(
		TimeSeriesDB(),
		c(closeFinancedTriSeriesName,closeAdjSeriesName,ohlAdjSeriesName),
		source
	))
	failIf(NROW(mergedSeries) == 0 | NCOL(mergedSeries) < 3,squish('No Data To Calculate OHL Financed TRIs for security ID: ',securityID)) 
	mergedSeries[,1] - mergedSeries[,2] + mergedSeries[,3]
})