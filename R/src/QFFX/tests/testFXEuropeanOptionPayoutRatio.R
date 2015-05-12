library("QFFX")

testFXEuropeanOptionPayoutRatio <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-02-01"
    quoteside = "mid"
    cross = "usd/chf"
    optionType = "call"
    expiry <-"2y"
    tenor <- "2y"
    notional <- 100
    
#   Set up forwards and calculate the at-the-money rate
    usdchfccy <- FXCurr$setByCross(cross)
    usdchf <- FXForwardGeneric(usdchfccy,quoteside,tenor)
    
    usdchfoption <- FXEuropeanOptionGeneric(usdchf,expiry,optionType)
    
    fxhistpayout <- FXEuropeanOptionPayoutRatio(FXOptionObj=usdchfoption,
        startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB())
    
    fxhistpayout$computePayoutRatio()
    PayOut <- fxhistpayout$getPayoutRatio()
    checkEquals(as.numeric(round(PayOut[as.POSIXct(endDate),1],3)),1.392)
    
    
    
}
    
