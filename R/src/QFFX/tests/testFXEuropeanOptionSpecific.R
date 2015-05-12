library("QFFX")

testFXEuropeanOptionSpecific <- function()
{
    
    spot <- 1.2041
    test_rate <- 1.20
    expiryDate <- "2008/07/22 "
    settleDate <- "2008/07/26"
    valueDate <- "2007/07/22"
    finalDate <- "2009/12/01"
    badvalueDate <- "2008/07/23"
    notional<-100
    notionalFlag <- "over"
    quoteside = "bid"
    direction = "buy"
    cross = "usd/chf"
    overRate <- 0.05398
    underRate<-0.03101
    optionType = "call"
    fxNotional <- FXNotional(notional,notionalFlag)
#   Set up forwards and calculate the at-the-money rate

    usdchf <- FXForwardSpecific(FXCurr$setByCross(cross),quoteside,test_rate,expiryDate,settleDate,fxNotional = fxNotional, direction)        
    usdchfpricer <-FXForwardPricer()
    usdchfpricer$setTerms(usdchf,valueDate,spot,overRate,underRate)
    fwdRate <- usdchfpricer$getFairForwardRate()
    usdchfpricer$getPrice("over")

   
# check for bad inputs    
    
    shouldBomb(FXEuropeanOptionSpecific(usdchf,-fwdRate,expiryDate,direction,optionType))
    shouldBomb(FXEuropeanOptionSpecific(usdchf,fwdRate,expiryDate,"none",optionType))
    shouldBomb(FXEuropeanOptionSpecific(usdchf,fwdRate,expiryDate,direction,"none"))
  
  
#check for good inputs
    
    usdchfoption <- FXEuropeanOptionSpecific(usdchf,fwdRate,expiryDate,direction,optionType)
    checkEquals(usdchfoption$getStrike(),fwdRate)
    checkEquals(usdchfoption$getExpiryDate(),as.POSIXlt(expiryDate))
    checkEquals(usdchfoption$getDirection(),direction)
    checkEquals(usdchfoption$getType(),optionType)
    
    eurusdFwdGeneric <- FXForwardGeneric(FXCurr$setByCross("eur/usd"),"mid","2y")
    
	putCall <- "call"
    eurusdOptionGeneric <- FXEuropeanOptionGeneric(eurusdFwdGeneric,expiry = "2y", putCall = optionType)
    
    tradeDate <- "2007/08/20"
    eurusdOptionSpecific <- FXEuropeanOptionSpecific$initSpecificFromGeneric(eurusdOptionGeneric,tradeDate)
    
    checkEquals(eurusdOptionSpecific$getStrike(),999)
    checkEquals(eurusdOptionSpecific$getExpiryDate(),as.POSIXlt("2009/08/20"))
    checkEquals(eurusdOptionSpecific$getDirection(),"buy")
    checkEquals(eurusdOptionSpecific$getType(),"call")
    checkEquals(eurusdOptionSpecific$getSettleDate(),as.POSIXct("2009/08/24"))
    

}
    
