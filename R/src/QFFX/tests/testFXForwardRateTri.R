library("QFFX")

testFXForwardRateTri <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-02-01"
    anotherDate <- "2007-01-23"
    quoteside = "mid"
    cross = "usd/chf"
    expiry <-"2y"
    tenor <- "2y"
    notional <- 100
    notionalFlag <- "over"
    fxNotional <- FXNotional(notional,notionalFlag)
#    
##   Set up forwards and calculate the at-the-money rate
    usdchfccy <- FXCurr$setByCross(cross)
    usdchf <- FXForwardGeneric(usdchfccy,quoteside,tenor)
	shouldBomb(FXForwardRateTri(usdchf,fxNotional,"goodtimes",endDate,TimeSeriesDB(),overUnder="over",DataLoad=NULL,initialEquity=0,lastRollInfo=NULL))
	shouldBomb(FXForwardRateTri(usdchf,fxNotional,startDate,"badtimes",TimeSeriesDB(),overUnder="over",DataLoad=NULL,initialEquity=0,lastRollInfo=NULL))
#    
## Check creating a new forward    
    fxhist <- FXForwardRateTri(FXForwardObj=usdchf, fxNotional = fxNotional,
        startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB(),overUnder="over")
    shouldBomb(fxhist$shared()$setCurrentDate("2007/06/01"))
    fxhist$shared()$setCurrentDate(startDate)
    newForward <- fxhist$createNewForward()
    checkEquals(round(newForward$getRate(),6),1.1664)

## Check test for rebalancing in time
	fxhist$shared()$setCurrentDate(anotherDate)
    price <- fxhist$priceCurrent(newForward)
    checkTrue(fxhist$checkNeedToRebalance(newForward,rebalPeriod="2w"))
    
    newForward <- fxhist$createNewForward()
    price <- fxhist$priceCurrent(newForward)
    checkTrue(!fxhist$checkNeedToRebalance(newForward,rebalPeriod="2w"))
    
## check Whole thing
	fxhist <- FXForwardRateTri(FXForwardObj=usdchf, fxNotional = fxNotional,
		startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB(),overUnder="over")
	fxhist$computeEquityCurve(rebalPeriod="2w")
    checkEquals(as.numeric(round(fxhist$shared()$getCurrPnL()[as.POSIXct(endDate),1],0)),1205914)
    
       

}
