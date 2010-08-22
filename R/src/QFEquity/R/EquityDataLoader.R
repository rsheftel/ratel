constructor("EquityDataLoader", function(filter = Close$NY.equity,...)
{
    library(QFCredit)
    extend(TSDataLoader(filter = filter), "EquityDataLoader",
        .equityObj = NULL,
        .columnName = NULL,
        .quote_convention = NULL,
        .quote_type = NULL,
        .quote_side = NULL
    )
})

method("checkSource","EquityDataLoader",function(this,source,...)
{
    sourceList <- c("ivydb","internal","bloomberg","tradestation")
    assert(any(source == sourceList),paste(source,"is not a valid equity source"))
    this$.source <- source
})

method("checkFinObject","EquityDataLoader",function(this,equityObj,...)
{
    assert(any(class(equityObj)=="Equity"),paste(equityObj,"is not an equity object"))
    this$.equityObj <- equityObj
})

method("getFormat", "EquityDataLoader", function(this, result,...)
{   
    result <- this$matrixToZoo(result)
    if(!is.null(result)){
        result <- getZooDataFrame(result)
        colnames(result) <- this$.columnName
    }
    result
})

method("getAttributes","EquityDataLoader",function(this,equityObj,...)
{    
    attributeList <- new.env()
    attributeList$security_id = as.numeric(this$.equityObj$.securityId)
    attributeList$quote_type = this$.quote_type
    attributeList$instrument = "equity"
    
    if(!is.null(this$.quote_convention))attributeList$quote_convention = this$.quote_convention
    if(!is.null(this$.quote_side))attributeList$quote_side = this$.quote_side
    
    as.list(attributeList)
})

method("getPrices", "EquityDataLoader", function(this, equityObj,type,convention,source,startDate=NULL, endDate=NULL,...)
{     
	this$.quote_convention <- convention
	this$.quote_type <- type
	this$.quote_side <- "mid"
	this$.columnName <- squish(toupper(leftStr(type,1)),rightStr(type,nchar(type)-1)," Price")
	if(convention == "adj_price")this$.columnName <- squish("Adjusted ",this$.columnName)
	this$.data <- retrieveData(this,source = source,finObj=equityObj,
			retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate)
	if(!is.null(this$.data))this$.data <- abs(this$.data)
	if(this$.source == "ivydb")this$.data <- this$filterIfNeeded(this$.data)
	this$.data
})

method("getClosePrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"close","price",source,startDate, endDate)
})

method("getOpenPrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"open", "price",source,startDate, endDate)
})

method("getHighPrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"high", "price",source,startDate, endDate)
})

method("getLowPrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"low", "price",source,startDate, endDate)
})

method("getAdjClosePrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"close","adj_price",source,startDate, endDate)
})

method("getAdjOpenPrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"open", "adj_price",source,startDate, endDate)
})

method("getAdjHighPrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"high", "adj_price",source,startDate, endDate)
})

method("getAdjLowPrices", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
	this$getPrices(equityObj,"low", "adj_price",source,startDate, endDate)
})

method("getVolumes", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{    
    this$.quote_convention <- NULL
    this$.quote_side <- NULL
    this$.quote_type <- "volume"
    
    this$.columnName <- "Volume" 
    this$.data <- retrieveData(this,source = source,finObj=equityObj,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate)
    this$.data <- this$filterIfNeeded(this$.data)
    this$.data 
})

method("getTotalLiabilities", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{    
    this$.quote_convention <- NULL
    this$.quote_side <- NULL
    this$.quote_type <- "total_liabilities"
    
    this$.columnName <- "Total Liabilities" 
    this$.data <- retrieveData(this,source = source,finObj=equityObj,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate)
    this$.data <- this$filterIfNeeded(this$.data)
    this$.data
})

method("getSharesOutstanding", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{
    this$.quote_convention <- NULL
    this$.quote_side <- NULL
    this$.quote_type <- "shares_outstanding"
    
    this$.columnName <- "Shares Outstanding"  
    this$.data <- retrieveData(this,source = source,finObj=equityObj,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate)
    this$.data <- this$filterIfNeeded(this$.data)
    if(!is.null(this$.data))this$.data <- this$.data / 1000 # The unit will be one million
    this$.data
})

method("getTotalReturns", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{    
    this$.quote_type <- "close"   
    this$.quote_side <- "mid"
    this$.quote_convention <- "tri_daily_pct"
    
    this$.columnName <- "Total Return"
    this$.data <- retrieveData(this,source = source,finObj=equityObj,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate)
    this$.data <- this$filterIfNeeded(this$.data)
    this$.data
})

method("getCumulativeTotalReturnFactors", "EquityDataLoader", function(this, equityObj, source,startDate=NULL, endDate=NULL,...)
{     
    this$.quote_type <- "close"   
    this$.quote_side <- "mid"
    this$.quote_convention <- "total_return_factor"
        
    this$.columnName <- "Cumulative Total Return Factor"
    this$.data <- this$retrieveData(source = source,finObj=equityObj,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate)
        
    if(this$.source == "ivydb")this$.data <- this$filterIfNeeded(this$.data)
    
    if(!is.null(this$.data)){        
        badData <- as.POSIXct(c("2005-10-03","2007-03-06","2007-03-08","2007-03-09"))
        n <- as.numeric(na.omit(match(badData,index(this$.data))))
        if(NROW(n)>0)(this$.data <- this$.data[n,])
    }
    this$.data                                           
})

method("getSecurityIDFromTicker", "EquityDataLoader", function(this, ticker,...)
{
    assert(any(class(ticker)=="character"),paste(ticker,"is not a character"))
    result <- IvyDB()$lookupSecurityID(ticker = ticker)
    (this$.data <- result)
})

method("getCreditEquityMappingTable", "EquityDataLoader", function(this,...)
{      
	conn <- SQLConnection()
	conn$init()
	conn$select("select * from credit_ticker_lookup")
})

method("getSecurityIDsUniverse", "EquityDataLoader", function(this,subset = NULL,...)
{
    creditEquityMappingTable <- this$getCreditEquityMappingTable(); etfMappingTable <- this$getETFTable()
    cdsMarkitTickers <- SingleNameCDS$referenceUniverse()[,'ticker']    
    getUniqueAndNonZeroList <- function(x){as.numeric(unique(subset(x,x != 0)))}
    securityIDs.corporate <- creditEquityMappingTable$option_metrics_id[match(cdsMarkitTickers,creditEquityMappingTable$markit)]
    securityIDs.etf <- etfMappingTable$option_metrics_id
    securityIDs.all <- c(securityIDs.corporate,securityIDs.etf)        
    if(!is.null(subset)){
        if(subset == "etf")return(getUniqueAndNonZeroList(securityIDs.etf))
        if(subset == "corporate")return(getUniqueAndNonZeroList(securityIDs.corporate))
        fail(squish("The subset: ",subset," is not valid! It should be 'etf' or 'corporate'"))
    }
    return(getUniqueAndNonZeroList(securityIDs.all))
})

method("getETFTable", "EquityDataLoader", function(this,...)
{
    conn <- SQLConnection()
    conn$init()
    query <-  "select * from etf_ticker_lookup"
    return(conn$select(query))
})

method("getETFTRI", "EquityDataLoader", function(this,ticker,source = "internal",startDate = NULL,endDate = NULL,stitched = TRUE,...){			
	e <- Equity(ticker = ticker)
	etfData <- this$getAdjClosePrices(e,source,startDate,endDate)
	if(stitched){
		etfProxyTable <- data.frame(read.csv(system.file("etf","etf_proxy_mapping_table.csv", package = "QFEquity"), sep = ",",stringsAsFactors = FALSE))
		bbrgTicker <- squish(etfProxyTable[toupper(ticker) == etfProxyTable[,1],"bbrg_proxy_tri_ticker"]," Index")
		bbrgData <- BloombergSecurity(bbrgTicker)$observations(field = 'LAST_PRICE',range = Range(startDate,endDate))
		if(is.null(etfData))etfData <- bbrgData
		if(first(index(etfData)) > first(index(bbrgData))){		
			matchDate <- first(index(na.omit(merge(etfData,bbrgData))))			
			adjRatio <- as.numeric(etfData[matchDate])/as.numeric(bbrgData[matchDate])			
			etfData <- rbind(adjRatio * bbrgData[index(bbrgData) < matchDate],etfData[index(etfData) >= matchDate])
		}
	}
	if(!is.null(etfData)) getZooDataFrame(etfData,"TRI") else fail("No data!")
})

method("getETFFinancedTRI", "EquityDataLoader", function(this,ticker,source = "internal",startDate = NULL,endDate = NULL,stitched = TRUE,...){			
	tri <- this$getETFTRI(ticker,source,startDate,endDate,stitched)
	FinanceTRI()$usingONLibor(zooSeriesY = tri)
})

method("getTickerFromSecurityID", "EquityDataLoader", function(this, securityID,...)
{
    assert(any(class(securityID)=="numeric"),paste(securityID,"is not numeric"))
    result <- IvyDB()$lookupTicker(security.id = securityID)
    (this$.data <- result)
})

method("createEquityTimeSeriesIfNotExists", "EquityDataLoader", function(this, securityID, quote_type,...)
{
    if(!this$.isConnected)(this$init(this$getDB()))
    
    time.series.name <- paste("ivydb",securityID,quote_type, sep = "_")

    if(this$.tsdb$timeSeriesExists(time.series.name))
        return()

    attributes = list(
        quote_type = quote_type,
        instrument = "equity",
        security_id = securityID
    )
    
    this$.tsdb$createTimeSeries(name = time.series.name, attributes = attributes)
})

method("calcAdjPrices", "EquityDataLoader", function(this,type,securityIds,nbSteps = NULL,step = NULL,
		analyticsSource = "internal",typeSource = "internal",factorSource = "internal",...)
{
	if(!this$.isConnected)(this$init(this$getDB()))
	getTSName <- function(x){paste("ivydb",x,type,"adj_price","mid", sep = "_")}
	purgeOrCreateTS <- function(x){
		if(!(this$.tsdb)$timeSeriesExists(getTSName(x))){
			(this$.tsdb)$createTimeSeries(
					getTSName(x),
					attributes = list(quote_type = type,quote_side = "mid", quote_convention = "adj_price",instrument = "equity",security_id = x)
			)
		}else{
			(this$.tsdb)$purgeTimeSeries(getTSName(x), data.source = analyticsSource)
		}
	}
	listToCalc <- securityIds
	if(!is.null(nbSteps) && !is.null(step))
		listToCalc <- getSubList(securityIds,nbSteps,step)
	tsNames <- sapply(listToCalc,getTSName)
	sapply(listToCalc,purgeOrCreateTS)
	tsArray <- array(list(NULL),dim = c(NROW(listToCalc),1),dimnames = list(tsNames, analyticsSource))
	for(i in 1:NROW(listToCalc)){
		p <- EquityListDataLoader$getPrices(securityIDList = listToCalc[i],type,"price",source = typeSource)
		f <- EquityListDataLoader$getCumulativeTotalReturnFactors(securityIDList = listToCalc[i], source = factorSource)
		if(!is.null(p) && !is.null(f)){
			merge.series <- na.omit(merge.zoo(f,p))
			if(NROW(merge.series)>0){
				result <- merge.series[,2] * merge.series[,1] / as.numeric(merge.series[NROW(merge.series[,1]),1])
				result = getZooDataFrame(result)
				colnames(result) <- listToCalc[i]
				if(!all(result == "Inf",na.rm = TRUE)){
					tsArray[[i,1]] <- result
				}
			}
		}
	}
	(this$.tsdb)$writeTimeSeries(tsArray)
	return(TRUE)
})

method("calcAdjClosePrices", "EquityDataLoader", function(this,securityIds,nbSteps = NULL,step = NULL,
	analyticsSource = "internal",closeSource = "internal",factorSource = "internal",...)
{
	this$calcAdjPrices("close",securityIds,nbSteps,step,analyticsSource,closeSource,factorSource)
})

method("calcAdjOpenPrices", "EquityDataLoader", function(this,securityIds,nbSteps = NULL,step = NULL,
	analyticsSource = "internal",openSource = "internal",factorSource = "internal",...)
{
	this$calcAdjPrices("open",securityIds,nbSteps,step,analyticsSource,openSource,factorSource)
})

method("calcAdjHighPrices", "EquityDataLoader", function(this,securityIds,nbSteps = NULL,step = NULL,
	analyticsSource = "internal",highSource = "internal",factorSource = "internal",...)
{
	this$calcAdjPrices("high",securityIds,nbSteps,step,analyticsSource,highSource,factorSource)
})

method("calcAdjLowPrices", "EquityDataLoader", function(this,securityIds,nbSteps = NULL,step = NULL,
	analyticsSource = "internal",lowSource = "internal",factorSource = "internal",...)
{
	this$calcAdjPrices("low",securityIds,nbSteps,step,analyticsSource,lowSource,factorSource)
})

