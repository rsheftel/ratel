library("QFFX")

testFXSettleDates <- function()
{
    
    over = "usd"
    under = "chf"
    usdchf <- FXCurr$setByPairs(over,under)
    tradeDate = "2007/07/27"
    endForHolidays <- "2008/01/01"
    
    regsettleDate = "2007/07/31"
    cadsettleDate = "2007/07/30"
    onemonth = list(expiryDate = as.POSIXct("2007/08/29"),settleDate = as.POSIXct("2007/08/31"))
    twomonth = list(expiryDate = as.POSIXct("2007/09/27"),settleDate = as.POSIXct("2007/10/01"))
    holidayList <- FXHolidayDates(usdchf, tradeDate, endForHolidays) 
#check for bad inputs

    shouldBomb(FXSettleDates$getExpirySettleDate("",tradeDate,"spot"))
    shouldBomb(FXSettleDates$getExpirySettleDate(usdchf,"BusinessTime","spot"))
    shouldBomb(FXSettleDates$getExpirySettleDate(usdchf,tradeDate,"not spot"))
    
#check for good inputs
    
    regDates <- FXSettleDates$getExpirySettleDate(usdchf,tradeDate,"spot",holidayList)
    checkEquals(regDates,list(expiryDate = as.POSIXct(tradeDate), settleDate = as.POSIXct(regsettleDate)))
    
    usdcad <- FXCurr$setByPairs("usd","cad")
    cadHolidayList <- FXHolidayDates(usdcad, tradeDate, endForHolidays) 
    expirysettleDate <- FXSettleDates$getExpirySettleDate(usdcad,tradeDate,"spot",cadHolidayList)
    
    checkEquals(expirysettleDate,list(expiryDate = as.POSIXct(tradeDate),settleDate = as.POSIXct(cadsettleDate)))
    
    
    date_one <- FXSettleDates$getExpirySettleDate(usdchf,tradeDate,"1m",holidayList)
    date_two <- FXSettleDates$getExpirySettleDate(usdchf,tradeDate,"2m",holidayList)
    
    checkEquals(date_one,onemonth)
    checkEquals(date_two,twomonth)
    
    

}
    
