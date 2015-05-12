setConstructorS3("FXHolidayDates", function(...)
{
    this <- extend(RObject(), "FXHolidayDates",
        .nativeCurr = "usd"
        )
      
      this
    
})


setMethodS3("getHolidayDates","FXHolidayDates",function(this,FXCurr = NULL,startDate=NULL, endDate=NULL,...)
{      
#
#This will return the holiday calendar which is a union of the two currencies, plus the native currency.  
#  
  assert(any(class(FXCurr)=="FXCurr"),paste("Not appropriate FX Object in getHolidayDates"))
  
  currDateInfo <- FXHolidayDates()
  holidays <- HolidayDataLoader()
  
  finCenterOver <- FXFinancialCenters$getFinancialCenterGivenCurrency(FXCurr$over())
  finCenterUnder <- FXFinancialCenters$getFinancialCenterGivenCurrency(FXCurr$under())
  finCenterNative <- FXFinancialCenters$getFinancialCenterGivenCurrency(currDateInfo$.nativeCurr)
#Until we get all financial calendars correct....
  dateOver <- holidays$getHolidays(source = "financialcalendar",financialCenter = finCenterOver,startDate=startDate, endDate=endDate)
  dateUnder <- holidays$getHolidays(source = "financialcalendar",financialCenter = finCenterUnder,startDate=startDate, endDate=endDate)
  dateNative <- holidays$getHolidays(source = "financialcalendar",financialCenter = finCenterNative, startDate=startDate, endDate=endDate)

  totalHolidays <- sort(unique(c(dateOver,dateUnder,dateNative)))
  
  return(totalHolidays)   

})



      