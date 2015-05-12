constructor("FXSystemDataLoader", function(...)
{
    extend(TSDataLoader(),"FXSystemDataLoader",
        .market = NULL,
        .transformation = NULL,
        .transformation_output = NULL
    )
})


method("getFXCarry","FXSystemDataLoader",function(this,fxCurr,tenor,putCall,source,startDate=NULL, endDate = NULL,...)
{
    SystemNumber <- 1
    this$.market <- getSystemDBFXMarketName(fxCurr=fxCurr, tenor=tenor, putCall=putCall, SystemNumber=SystemNumber)
    this$.transformation <- "payout_ratio"
    this$.transformation_output <- "payout_ratio"
    (this$.data <- retrieveData(this,source = source,finObj=NULL,retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,
        startDate=startDate,endDate=endDate))   
})


method("getAttributes","FXSystemDataLoader",function(this,...)
{
    (attributeList <- list(
        market = this$.market,    
        transformation = this$.transformation,
        transformation_output = this$.transformation_output
    ))
})

method("checkSource","FXSystemDataLoader",function(this,source,...)
{
    sourceList <- c("internal")
    assert(any(source == sourceList),paste(source,"is not a valid system source"))
    this$.source <- source
})

method("getFormat", "FXSystemDataLoader", function(this, result,...)
{
    result <- this$matrixToZoo(result)
    if(!is.null(result)){
        result <- getZooDataFrame(result)
        colnames(result) <- this$.transformation_output
    }
    result
})