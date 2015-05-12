library("QFFX")
testFXForwardPricer <- function()
{
    
    spot <- 1.2041
    test_rate <- 1.20
    settleDate<- "2008/07/22"
    expiryDate <- "2008/07/18"
    
    valueDate <- "2007/07/22"
    modifiedValueDate <- "2007/07/24"
    badvalueDate <- "2008/07/23"
    notional <- 100
    notionalFlag <- "over"
    quoteside = "bid"
    direction = "buy"
    cross = "usd/chf"
    overRate <- 0.05398
    underRate <- 0.03101
    FXCurrency <- FXCurr$setByCross(cross)
    fxNotional <- FXNotional(notional,notionalFlag)
    usdchf <- FXForwardSpecific(FXCurrency = FXCurrency,quoteSide = quoteside,test_rate,expiryDate,settleDate,fxNotional = fxNotional,direction)
    
    usdjpy <- FXForward(FXCurr$setByCross(cross),quoteside)
    usdchfpricer <-FXForwardPricer()
    shouldBomb(FXHolidayDates$getHolidayDates("BusinessTime"))
    holidayList <- FXHolidayDates$getHolidayDates(FXCurrency)
#check for bad inputs

    shouldBomb(usdchfpricer$setTerms(usdchf,valueDate,-spot,overRate,underRate,holidayList))
    shouldBomb(usdchfpricer$setTerms(usdchf,valueDate,spot,-overRate,underRate,holidayList))
    shouldBomb(usdchfpricer$setTerms(usdchf,valueDate,spot,overRate,-underRate,holidayList))
    shouldBomb(usdchfpricer$setTerms(usdjpy,valueDate,spot,overRate,underRate,holidayList))
    
#check for good inputs

    usdchfpricer$setTerms(usdchf,valueDate,spot,overRate,underRate,holidayList)
    
    checkEquals(usdchfpricer$.valueDate,as.POSIXct(modifiedValueDate))
    checkEquals(usdchfpricer$.spotRate,spot)
    checkEquals(usdchfpricer$.overRepoRate,overRate)
    checkEquals(usdchfpricer$.underRepoRate,underRate)
    checkEquals(usdchfpricer$.isDefined,TRUE)
    
#test Pricing Mechanism. If we pass forward rate, price should be zero.

 fwdRate <- getFairForwardRate(usdchfpricer)
 usdchfnew<-FXForwardSpecific(FXCurr$setByCross(cross),quoteside,fwdRate,expiryDate,settleDate,fxNotional = fxNotional,direction)
 usdchfpricer$setTerms(usdchfnew,valueDate,spot,overRate,underRate,holidayList)
 overPrice <- usdchfpricer$getPrice("over")
 checkEquals(round(overPrice,3),0)
    
#test for one known example. 
 valueDate <- "2008-12-01"
 expiryDate <- "2008-12-03"
 notional <- 100
 notionalFlag <- "over"
 quoteside = "bid"
 direction = "buy"
 cross = "usd/chf"
 
	
}
    
