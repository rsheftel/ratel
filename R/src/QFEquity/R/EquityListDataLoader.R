setConstructorS3("EquityListDataLoader", function(filter = Close$NY.equity,...)
{
    extend(TSDataLoader(filter = filter), "EquityListDataLoader",
        .securityIDList =NULL,
        .quote_convention = NULL,
        .quote_type = NULL,
        .quote_side = NULL
    )    
})

setMethodS3("getPrices", "EquityListDataLoader", function(this,securityIDList,type,convention,source,startDate=NULL, endDate=NULL,...)
{
	this$.securityIDList <- as.numeric(unlist(securityIDList))
	
	this$.quote_convention <- convention
	this$.quote_type <- type
	this$.quote_side <- "mid"
	
	this$.data <- retrieveData(this,startDate=startDate,endDate = endDate,source = source,finObj=NULL,
			retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList
	)
	if(this$.source == "ivydb")this$.data <- this$filterIfNeeded(this$.data)
	this$.data
})

setMethodS3("getAdjClosePrices", "EquityListDataLoader", function(this,securityIDList,source,startDate=NULL, endDate=NULL,...)
{
	this$getPrices(securityIDList,"close","adj_price",source,startDate, endDate)
})

setMethodS3("getClosePrices", "EquityListDataLoader", function(this,securityIDList,source,startDate=NULL, endDate=NULL,...)
{
    this$getPrices(securityIDList,"close","price",source,startDate, endDate)
})

setMethodS3("getCumulativeTotalReturnFactors", "EquityListDataLoader", function(this,securityIDList,source,startDate=NULL, endDate=NULL,...)
{
    this$.securityIDList <- as.numeric(unlist(securityIDList))
    
    this$.quote_convention <- "total_return_factor"
    this$.quote_type <- "close"
    this$.quote_side <- "mid"
    
    this$.data <- retrieveData(this,startDate=startDate,endDate = endDate,source = source,finObj=NULL,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList
    )
    if(this$.source == "ivydb")this$.data <- this$filterIfNeeded(this$.data)
    
    if(!is.null(this$.data)){        
        # bad data points in the OptionMetrics files
        badData <- as.POSIXct(c("2005-10-03","2007-03-06","2007-03-08","2007-03-09"))
        n <- as.numeric(na.omit(match(badData,index(this$.data))))
        if(NROW(n)>0)(this$.data<-this$.data[-n,])
    }
    this$.data
})

setMethodS3("getVolumes", "EquityListDataLoader", function(this,securityIDList,source,startDate=NULL, endDate=NULL,...)
{
    this$.securityIDList <- as.numeric(unlist(securityIDList))
    
    this$.quote_convention <- NULL
    this$.quote_side <- NULL
    this$.quote_type <- "volume"
    
    this$.data <- retrieveData(this,startDate=startDate,endDate = endDate,source = source,finObj=NULL,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList
    )
    this$.data <- this$filterIfNeeded(this$.data)
    this$.data
})

setMethodS3("getTotalLiabilities", "EquityListDataLoader", function(this,securityIDList,source,startDate=NULL, endDate=NULL,...)
{
    this$.securityIDList <- as.numeric(unlist(securityIDList))
    
    this$.quote_convention <- NULL
    this$.quote_side <- NULL
    this$.quote_type <- "total_liabilities"
    
    this$.data <- retrieveData(this,startDate=startDate,endDate = endDate,source = source,finObj=NULL,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList
    )
    this$.data <- this$filterIfNeeded(this$.data)
    this$.data
})

setMethodS3("getSharesOutstanding", "EquityListDataLoader", function(this, securityIDList, source,startDate=NULL, endDate=NULL,...)
{
    this$.securityIDList <- as.numeric(unlist(securityIDList))
    
    this$.quote_convention <- NULL
    this$.quote_side <- NULL
    this$.quote_type <- "shares_outstanding"
    
    this$.data <- retrieveData(this,startDate=startDate,endDate = endDate,source = source,finObj=NULL,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList
    )
    this$.data <- this$filterIfNeeded(this$.data)
    if(!is.null(this$.data))this$.data <- this$.data / 1000 # The unit will be one million
    this$.data
})

setMethodS3("checkSource","EquityListDataLoader",function(this,source,...)
{
    sourceList <- c("ivydb","internal","bloomberg","tradestation")
    assert(any(source == sourceList),paste(source,"is not a valid equity source"))
    this$.source <- source
})

setMethodS3("getAttributes","EquityListDataLoader",function(this,...)
{
    attributeList <- new.env()
    attributeList$security_id = subset(this$.securityIDList,this$.securityIDList !=0)
    attributeList$quote_type = this$.quote_type
    attributeList$instrument = "equity"
    
    if(!is.null(this$.quote_convention))attributeList$quote_convention = this$.quote_convention
    if(!is.null(this$.quote_side))attributeList$quote_side = this$.quote_side
    
    as.list(attributeList)
})

setMethodS3("getArrangeAttributes","EquityListDataLoader",function(this,...)
{
    result <- c("security_id","quote_type")
})
       
setMethodS3("getFormat", "EquityListDataLoader", function(this, result,...)
{ 
    if(is.null(result))return(result)
    
    result <- this$matrixToZoo(result)    
    result <- getZooDataFrame(result[,match(this$.securityIDList,colnames(result))])
    colnames(result) <- this$.securityIDList
    result
})
