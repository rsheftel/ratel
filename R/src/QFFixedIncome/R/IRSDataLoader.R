
setConstructorS3("IRSDataLoader", function(filter = Close$NY.irs, ...)
{
    extend(TSDataLoader(filter = filter), "IRSDataLoader",
        .irsObj = NULL,
        .tenor  = NULL,
        .checkGeneric = NULL
    )
})

# Methods to retrieve time series data

setMethodS3("getSpreads","IRSDataLoader",function(this,source,irsObj,startDate=NULL, endDate = NULL,...)
{
    this$.tenor <- irsObj$.tenor
    this$.checkGeneric <- TRUE
    this$.data <- retrieveData(this,source = source,finObj=irsObj,retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,
        startDate=startDate,endDate=endDate)
    this$.data <- this$filterIfNeeded(this$.data)
})

setMethodS3("getCurves","IRSDataLoader",function(this,source,irsObj,startDate=NULL, endDate = NULL,...)
{
    this$.tenor <- this$getTenors()
    this$.checkGeneric <- FALSE
    this$.data <- retrieveData(this,source = source,finObj=irsObj,retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,
        startDate=startDate,endDate=endDate)
    this$.data <- this$filterIfNeeded(this$.data)
    this$.data
})

setMethodS3("getOneDateCurve","IRSDataLoader",function(this,source,irsObj,myDate,defaultLimit,...)
{
    this$.data <- searchDate(this,source = source,finObj=irsObj,baseFunction = this$getCurves,myDate=myDate,defaultLimit=defaultLimit)
})

setMethodS3("getOneDateSpread","IRSDataLoader",function(this,source,irsObj,myDate,defaultLimit,...)
{
    this$.data <- searchDate(this,source = source,finObj=irsObj,baseFunction = this$getSpreads,myDate=myDate,defaultLimit=defaultLimit)
})

# secondary methods

setMethodS3("getTenors","IRSDataLoader",function(this,...)
{
   (tenorList <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y"))
})
                                                
setMethodS3("getAttributes","IRSDataLoader",function(this,irsObj,...)
{
    (attributeList <- list(
        instrument = irsObj$.instrument,    
        tenor = this$.tenor,
        quote_convention = irsObj$.quoteConvention,
        ccy = irsObj$.currency,
        quote_side = irsObj$.quoteSide,
    quote_type = irsObj$.quoteType))
})
 
setMethodS3("checkSource","IRSDataLoader",function(this,source,...)
{
    sourceList <- c("internal","bloomberg_CMN3")
    assert(any(source == sourceList),paste(source,"is not a valid IRS source"))
    this$.source <- source
})

setMethodS3("checkFinObject","IRSDataLoader",function(this,irsObj,...)
{
    if(this$.checkGeneric){
        assert(any(class(irsObj)=="GenericIRS"),paste(irsObj,"is not a GenericIRS"))
        assert(irsObj$.isDefinedGeneric,paste(irsObj,"is not properly defined"))
    }else{
        assert(any(class(irsObj)=="IRS"),paste(irsObj,"is not an IRS"))
        assert(irsObj$.isDefined,paste(irsObj,"is not properly defined"))
    }
    this$.irsObj <- irsObj
})

setMethodS3("getFormat", "IRSDataLoader", function(this, result,...)
{
    result <- this$matrixToZoo(result)

    if(is.null(result))return(result)
    
    if(this$.checkGeneric){
        result <- getZooDataFrame(result)
      	colnames(result) <- (this$.irsObj)$.tenor
    }
    if(!this$.checkGeneric){
    
        # set column names and leave NA columns for tenors that are empty    
        
        if((!all(this$getTenors()%in%colnames(result)))){
        	tenors <-  (this$.tsdb)$lookupAttributesForTimeSeries(colnames(result), attributes = "tenor")
        	sorted.tenors <- sapply(colnames(result), function(time.series.name) as.character(tenors[time.series.name, "tenor"]))
        	result <- result[,match(this$getTenors(),sorted.tenors)]
       	
           	result <- getZooDataFrame(result)
          	colnames(result) <- this$getTenors()
        }
    }
    result
})

setMethodS3("getIRS", "IRSDataLoader", function(this,startDate = NULL,endDate = NULL,...)
{
	print("getting irs data")
	irs <- IRS()
	irs$setDefault()
	this$getCurves(irsObj = irs,source = 'internal',startDate = startDate,endDate = endDate)/100
})