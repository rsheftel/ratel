library("QFFX")

testFXEuropeanOptionPricer <- function()
{
    
    spot <- 115.84
    test_rate <- 114
    settleDate <- "2008/01/08"
    expiryDate <- "2007/12/28"  
    valueDate <- "2007/10/03"
    spotSettleDate <- "2007/10/05"
    
    badvalueDate <- "2008/07/23"
    
    quoteside = "bid"
    direction = "buy"
    cross = "usd/jpy"
    
    overRate <- 0.050931
    underRate<-0.010037
    
    vol = 0.0864
    notional <- 100
    fxNotional <- FXNotional(notional,"over")
#   Set up forwards and calculate the at-the-money rate

    usdchf <- FXForwardSpecific(FXCurrency=FXCurr$setByCross(cross),quoteSide=quoteside,rate=test_rate,
      expiryDate=expiryDate, settleDate=settleDate,fxNotional=fxNotional, direction=direction)
        
    usdchfpricer <-FXForwardPricer()
    usdchfpricer$setTerms(usdchf,valueDate,spot,overRate,underRate)
    fwdRate <- usdchfpricer$getFairForwardRate()
    usdchfpricer$getPrice("over")

#   Set up European Option
    usdchfcalloption <- FXEuropeanOptionSpecific(usdchf,fwdRate,expiryDate,direction,"call")

# Set up Pricer
    usdchfoptionpricer <- FXEuropeanOptionPricer()
    

# check for bad inputs    
    
    shouldBomb(usdchfoptionpricer$setTerms(usdchfcalloption,valueDate,spotSettleDate,-spot,vol,overRate,underRate))
    shouldBomb(usdchfoptionpricer$setTerms(usdchfcalloption,valueDate,spotSettleDate,spot,-vol,overRate,underRate))
    shouldBomb(usdchfoptionpricer$setTerms(usdchfcalloption,valueDate,spotSettleDate,spot,vol,-overRate,underRate))
    shouldBomb(usdchfoptionpricer$setTerms(usdchfcalloption,valueDate,spotSettleDate,spot,vol,overRate,-underRate))
    shouldBomb(usdchfoptionpricer$setTerms(usdchfcalloption,badvalueDate,spotSettledate,spot,vol,overRate,-underRate))
    
    eurusdoptionpricer <- FXEuropeanOptionPricer()
    checkEquals(eurusdoptionpricer$.isDefined,FALSE)
  
#check for good inputs
    
    usdchfoptionpricer$setTerms(usdchfcalloption,valueDate,spotSettleDate,spot,vol,overRate,underRate)
    
    checkEquals(usdchfoptionpricer$.valueDate,as.POSIXct(valueDate))
    checkEquals(usdchfoptionpricer$.spotSettleDate,as.POSIXct(spotSettleDate))
    checkEquals(usdchfoptionpricer$.spotRate,spot)
    checkEquals(usdchfoptionpricer$.volLn,vol)
    checkEquals(usdchfoptionpricer$.overRepoRate,overRate)
    checkEquals(usdchfoptionpricer$.underRepoRate,underRate)
    
#check put/call parity for atm    
    
    output <- usdchfoptionpricer$getPrice("over")
    call_price <- output[1]
    checkEquals(round(call_price,6),0.016513)
    
    usdchfputoption <- FXEuropeanOptionSpecific(usdchf,fwdRate,expiryDate,direction,"put")
    usdchfoptionpricer$setTerms(usdchfputoption,valueDate,spotSettleDate,spot,vol,overRate,underRate)
    output <- usdchfoptionpricer$getPrice("over")
    put_price <- output[1]
    if ((call_price>0)&&(put_price>0)){
        checkEquals((round((call_price-put_price)/(call_price+put_price),6)),0)
        }
    
    
}
    
