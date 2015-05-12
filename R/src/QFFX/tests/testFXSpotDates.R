library("QFFX")

testFXSpotDates <- function()
{
    
    over = "usd"
    under = "chf"
    usdchf <- FXCurr$setByPairs(over,under)
    startDate <- "2007-01-03"
    endDate <- "2007-01-10"
    endForHolidays <- "2007-02-01"
    listOfDates <- c("2007-01-03","2007-01-04","2007-01-05","2007-01-08","2007-01-09","2007-01-10")
    
    
    holidayList <- FXHolidayDates(usdchf, startDate, endDate) 

    spotDates <- FXSpotDates$getSpotSettleDates(usdchf,listOfDates,holidayList)   
    checkEquals(as.vector(spotDates[as.POSIXct("2007-01-04")]),"2007-01-08")
    
}
    
