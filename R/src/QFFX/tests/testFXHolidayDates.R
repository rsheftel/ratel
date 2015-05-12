## Test file for the FXHolidayDates object
library("QFFX")

testFXHolidayDates <- function()
{
    gbpusd <- FXCurr$setByPairs("gbp","usd")
    shouldBomb(FXHolidayDates$getHolidayDates("BusinessTime"))
    holidayList <- FXHolidayDates$getHolidayDates(gbpusd)
    testOneDate <- as.POSIXct("2012-01-16")
    testTwoDate <- as.POSIXct("2012-01-17")
    
    checkEquals(any(holidayList==testOneDate),TRUE)
    checkEquals(any(holidayList==testTwoDate),FALSE)
  
    
}