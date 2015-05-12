library("QFFX")
testFXForwardSpecific <- function()
{
    
    tolerance = 0.001
    spot <- 1.2041
    test_rate <- 1.20
    settleDate <- "2008/07/22"
    expiryDate <- "2008/07/18"
    fxNotional <- FXNotional(100,"over")
    usdchf <- FXCurr$setByCross("usd/chf")

#check for bad inputs

    shouldBomb(FXForwardSpecific(usdchf,"mid",-test_rate,expiryDate,settleDate,fxNotional,"buy"))
    shouldBomb(FXForwardSpecific("usdchf","mid",test_rate,expiryDate,settleDate,fxNotional,"buy"))
    shouldBomb(FXForwardSpecific(usdchf,"mid",test_rate,expiryDate,settleDate,"fxNotional","buy"))
    shouldBomb(FXForwardSpecific(usdchf,"mid",test_rate,expiryDate,settleDate,fxNotional,"don't buy"))
    shouldBomb(FXForwardSpecific(usdchf,"mid",test_rate,settleDate,expiryDate,fxNotional,"buy"))
    
    
    
#check for good inputs

    usdjpy <- FXForwardSpecific(FXCurrency = FXCurr$setByCross("usd/jpy"),quoteSide = "bid",rate = test_rate,
      expiryDate = expiryDate,settleDate = settleDate,fxNotional = fxNotional,direction = "buy")
    
    checkEquals(usdjpy$getFXCurrency()$over(),"usd")
    checkEquals(usdjpy$getFXCurrency()$under(),"jpy")
    checkEquals(usdjpy$.quoteSide,"bid")
    checkEquals(usdjpy$getRate(),test_rate)
    checkEquals(usdjpy$getSettleDate(),as.POSIXct(settleDate))
    checkEquals(usdjpy$getExpiryDate(),as.POSIXct(expiryDate))
    checkEquals(usdjpy$getFXNotional(),fxNotional)
    checkEquals(usdjpy$getDirection(),"buy")
       
  
    tradeDate <- "2007/08/20"
    usdchf <- FXForwardGeneric(FXCurrency = FXCurr$setByCross("usd/chf"),quoteSide = "mid",tenor = "2m")
    usdchfSpecific <- FXForwardSpecific$initSpecificFromGeneric(usdchf,tradeDate)
    
    checkEquals(usdchfSpecific$getFXCurrency()$over(),"usd")
    checkEquals(usdchfSpecific$getFXCurrency()$under(),"chf")
    checkEquals(usdchfSpecific$.quoteSide,"mid")
    checkEquals(usdchfSpecific$getRate(),999)
    checkEquals(usdchfSpecific$getSettleDate(),as.POSIXct("2007/10/22"))
    checkEquals(usdchfSpecific$getExpiryDate(),as.POSIXct("2007/10/18"))
    
    
    
 
    
}
    
