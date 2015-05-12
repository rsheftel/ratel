setConstructorS3("FXDataLoad", function(FXCurr=NULL, startDate=NULL,endDate=NULL,tsdb=NULL,tenor=NULL,close=NULL,...)
{
        this <- extend(RObject(), "FXDataLoad",
        .FXCurr = FXCurr,
        .startDate = startDate,
        .endDate = endDate,
        .tsdb = tsdb,
        .holidayList = NULL,
        .volData = NULL,
        .rateData = NULL,
        .settleData = NULL,
        .expiryData = NULL,
        .overRepoData = NULL,
        .underRepoData = NULL,
		.close = close,
		.tenor = tenor
        )
        
        constructorNeeds(this,tsdb = "TimeSeriesDB", FXCurr = "FXCurr")
        
        if(!inStaticConstructor(this))
        {
		  if (!is.null(this$.close)) failIf(!any(this$.close == c("ldn")))
		  startDate <- as.POSIXct(startDate)
          endDate <- as.POSIXct(endDate)
          if(startDate > endDate) throw("FXTri: start date must be less than end date")
          tenor.list <- parseSimpleTenor(this$.tenor)
          unit <- tenor.list$unit
          NumUnits <- tenor.list$numUnits
          temp.holidayEnd <- getFincadDateAdjust(endDate,unit,NumUnits,holidayList=NULL)
          
          holidayEndDate <- seq(from = temp.holidayEnd,by = paste(1,"month"),length=2)[2]
          this$.holidayList <- FXHolidayDates$getHolidayDates(this$.FXCurr,startDate=startDate,endDate=holidayEndDate)

          this$loadRateVolRepoInfo(startDate = this$.startDate,endDate = this$.endDate)
          this$filterRateAndVol()
		  } 
        this
})


setMethodS3("loadRateVolRepoInfo","FXDataLoad",function(this,startDate,endDate,...)
{
	tenorList <- FXTenor$getTenors()
    temp.currency <- squish(this$.FXCurr$over(),this$.FXCurr$under())

	rateTickerName <- squish(temp.currency,"_tenor_rate_mid")
    volTickerName <- squish(temp.currency,"_tenor_vol_ln_mid")
    overDiscRateTickerName <- squish("fx_",this$.FXCurr$over(),"_tenor_disc_rate_mid")
    underDiscRateTickerName <- squish("fx_",this$.FXCurr$under(),"_tenor_disc_rate_mid")
	
	if (!is.null(this$.close)) {
		rateTickerName <- squish(rateTickerName,this$close)
		volTickerName <- squish(volTickerName,this$.close)
		overDiscRateTickerName <- squish(overDiscRateTickerName,this$.close)
		underDiscRateTickerName <- squish(underDiscRateTickerName,this$.close) 
	}
	
	settleDataTickerName <- squish(temp.currency,"_tenor_settle_date")
    expiryDataTickerName <- squish(temp.currency,"_tenor_expiry_date")
    
    this$.rateData <- getTermStructureForTimeSeries(rateTickerName,tenorList,source="internal",startDate=as.character(startDate), endDate=as.character(endDate))
    this$.volData <-  getTermStructureForTimeSeries(volTickerName,tenorList,source="internal",startDate=as.character(startDate), endDate=as.character(endDate))
    this$.settleData <-  getTermStructureForTimeSeries(settleDataTickerName,tenorList,source="internal",startDate=as.character(startDate), endDate=as.character(endDate))
    this$.expiryData <-  getTermStructureForTimeSeries(expiryDataTickerName,tenorList,source="internal",startDate=as.character(startDate), endDate=as.character(endDate))
    this$.overRepoData <- getTermStructureForTimeSeries(overDiscRateTickerName,tenorList,source="internal",startDate=as.character(startDate), endDate=as.character(endDate))
    this$.underRepoData <- getTermStructureForTimeSeries(underDiscRateTickerName,tenorList,source="internal",startDate=as.character(startDate), endDate=as.character(endDate))
   
    daily.slice <- function(time) {
		if (!is.null(this$.rateData)) this$.rateData <- make.zoo.daily(this$.rateData, time)
		if (!is.null(this$.volData)) this$.volData <- make.zoo.daily(this$.volData, time)
		if (!is.null(this$.overRepoData)) this$.overRepoData <- make.zoo.daily(this$.overRepoData, time)
		if (!is.null(this$.underRepoData)) this$.underRepoData <- make.zoo.daily(this$.underRepoData, time)
	}
	
	if (is.null(this$.close)) daily.slice("15:00:00")
	else if (this$.close == "ldn") daily.slice("11:00:00")

})

setMethodS3("getVolData","FXDataLoad",function(this,...)
{
  return(this$.volData)
})

setMethodS3("getRateData","FXDataLoad",function(this,...)
{
  return(this$.rateData)
})
    
setMethodS3("getOverRepoData","FXDataLoad",function(this,...)
{
  return(this$.overRepoData)
})

setMethodS3("getUnderRepoData","FXDataLoad",function(this,...)
{
  return(this$.underRepoData)
})

setMethodS3("getSettleData","FXDataLoad",function(this,...)
{
  return(this$.settleData)
})

setMethodS3("getExpiryData","FXDataLoad",function(this,...)
{
  return(this$.expiryData)
})

    
setMethodS3("getHolidayList","FXDataLoad",function(this,...)
{
  return(this$.holidayList)
})
      
setMethodS3("filterRateAndVol","FXDataLoad",function(this,...)
{
    this$.rateData <- filter.zoo.byIntersection(this$.rateData, this$.volData, this$.overRepoData, this$.underRepoData, this$.settleData,this$.expiryData)
    this$.volData <- filter.zoo.byIntersection(this$.volData, this$.rateData)
    this$.overRepoData <- filter.zoo.byIntersection(this$.overRepoData, this$.rateData)
    this$.underRepoData <- filter.zoo.byIntersection(this$.underRepoData, this$.rateData)
    
	this$.settleData <- filter.zoo.byIntersection(this$.settleData,this$.rateData)
    this$.expiryData <- filter.zoo.byIntersection(this$.expiryData,this$.rateData)
})

setMethodS3("filterRate","FXDataLoad",function(this,...)
{
  this$.rateData <- filter.zoo.byIntersection(this$.rateData, this$.overRepoData, this$.underRepoData)
  this$.overRepoData <- filter.zoo.byIntersection(this$.overRepoData, this$.rateData)
  this$.underRepoData <- filter.zoo.byIntersection(this$.underRepoData, this$.rateData)
})
  
  

   