setConstructorS3("FXComputeOptionDateSeries", function(tsdb = NULL, FXCurr=NULL, tenor=NULL, dateList=NULL, holidayList=NULL,...)
{
    this <- extend(RObject(), "FXComputeOptionDateSeries",
        .tsdb = tsdb,
        .FXCurr = FXCurr,
        .tenor = tenor,
        .dateList = dateList,
        .holidayList = holidayList
        )
    constructorNeeds(this,tsdb = "TimeSeriesDB") 
#We will assume that the dateList is ordered....    
    if(!inStaticConstructor(this))
    {
      startDate <- as.POSIXct(dateList[1])
      endDate <- as.POSIXct(dateList[length(dateList)])
      if (is.null(holidayList)) this$.holidayList <- FXHolidayDates$getHolidayDates(this$.FXCurr,startDate=startDate,endDate=endDate)
    }
    this
    
})

setMethodS3("computeOptionDateSeries", "FXComputeOptionDateSeries", function(this, writeToTSDB=TRUE, ...)
{
     func <- function(currDate) 
     { 
      return(as.list(c(list(tradeDate=currDate),FXSettleDates$getExpirySettleDate(this$.FXCurr, currDate, this$.tenor, this$.holidayList)))) 
     }
     dates.list <- matrix(unlist(lapply(as.character(this$.dateList),func)),ncol=3,byrow=TRUE)
     expiry.series.name=squish(this$.FXCurr$over(),this$.FXCurr$under(),"_",this$.tenor,"_expiry_date")
     settle.series.name=squish(this$.FXCurr$over(),this$.FXCurr$under(),"_",this$.tenor,"_settle_date")
     data.source <- "internal" 
     expiry_date_zoo=zoo(dates.list[,2],as.POSIXct(dates.list[,1]))
     settle_date_zoo=zoo(dates.list[,3],as.POSIXct(dates.list[,1]))
     if (!writeToTSDB) return(merge(expiry_date_zoo,settle_date_zoo))
#	 if (this$.tenor!="spot") this$.tsdb$writeOneTimeSeriesByName(ts=expiry_date_zoo,name=expiry.series.name,data.source=data.source)
	this$.tsdb$writeOneTimeSeriesByName(ts=expiry_date_zoo,name=expiry.series.name,data.source=data.source)
	this$.tsdb$writeOneTimeSeriesByName(ts=settle_date_zoo,name=settle.series.name,data.source=data.source)
     return()   
})
