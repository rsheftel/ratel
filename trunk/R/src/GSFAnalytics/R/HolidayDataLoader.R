setConstructorS3("HolidayDataLoader", function(...)
{
    extend(TSDataLoader(), "HolidayDataLoader",
        .financialCenter = NULL
    )        
})

setMethodS3("getHolidays", "HolidayDataLoader", function(this, source, financialCenter,startDate=NULL, endDate=NULL,...)
{
    (this$.data <- retrieveData(this,source = source,finObj=financialCenter,
        retrieveFunction = (this$.tsdb)$retrieveTimeSeriesByAttributeList,startDate=startDate,endDate=endDate))
})

method("getHolidaysToTenor","HolidayDataLoader",function(this,source,financialCenter,tenorNumeric,startDate,endDate,...){
    if(is.null(endDate)) endHolidayDate <- as.Date(Sys.Date())
    else endHolidayDate <- endDate
    endHolidayDate <- as.Date(endHolidayDate)+ (tenorNumeric + 1) * 365 # (tenorNumeric + 1) to be conservative
    return(this$getHolidays(source,financialCenter,startDate,endHolidayDate))
})

setMethodS3("getAttributes","HolidayDataLoader",function(this,...)
{    
    (attributeList <- list(instrument = "holiday",financial_center = this$.financialCenter))
})

 
setMethodS3("checkSource","HolidayDataLoader",function(this,source,...)
{
    sourceList <- c("financialcalendar")
    assert(any(source == sourceList),paste(source,"is not a valid holiday source"))
    this$.source <- source
})

setMethodS3("checkFinObject","HolidayDataLoader",function(this,financialCenter,...)
{
    centerList <- FXFinancialCenters$getListOfFinancialCenters()
    assert(all(financialCenter %in% centerList),paste(financialCenter,"is not a valid financial center"))
    this$.financialCenter <- financialCenter
})

setMethodS3("getFormat", "HolidayDataLoader", function(this, result,...)
{    
    if(!is.null(result)){  
        result <- this$matrixToZoo(result)     
        result <- as.POSIXct(unique(as.character(index(result))))    
    }
    result
})

setMethodS3("summary","HolidayDataLoader",function(this,...)
{
    cat("TS database:",as.character(this$.tsdb),"\n")
    cat("Connected:",this$.isConnected,"\n")
    cat("Source:",this$.source,"\n")
    cat("Financial center:",this$.financialCenter,"\n")
    cat("Data range:","\n","\n")
    if(!is.null(this$.data))as.character(range(this$.data))
})