library("QFFX")

testFXEuropeanOptionTri <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-02-01"
    anotherDate <- "2007-01-23"
    quoteside = "mid"
    cross = "usd/chf"
    optionType = "call"
    expiry <-"2y"
    tenor <- "2y"
    overUnder <- "over"
    fxNotional <- FXNotional(100,overUnder)
    
    
#   Set up forwards and calculate the at-the-money rate
    usdchfccy <- FXCurr$setByCross(cross)
    usdchf <- FXForwardGeneric(usdchfccy,quoteside,tenor)
    usdchfoption <- FXEuropeanOptionGeneric(usdchf,expiry,optionType)
    shouldBomb(FXEuropeanOptionTri(usdchfoption,"goodtimes",endDate,TimeSeriesDB(),overUnder = overUnder))
    shouldBomb(FXEuropeanOptionTri(usdchfoption,startDate,"badtimes",TimeSeriesDB(),overUnder = overUnder))
    shouldBomb(FXEuropeanOptionTri(usdchfoption,endDate,startDate,TimeSeriesDB(),overUnder = overUnder))
    shouldBomb(FXEuropeanOptionTri(usdchfoption,startDate,endDate,"Totally Unknown",overUnder = overUnder))
    shouldBomb(FXEuropeanOptionTri(usdchfoption,startDate,endDate,TimeSeriesDB(),overUnder = "BusinessTime"))

# Check creating a new option    
    fxhist <- FXEuropeanOptionTri(FXOptionObj=usdchfoption, fxNotional = fxNotional,startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB(),overUnder = overUnder)
	shouldBomb(fxhist$setCurrentDate("2007/06/01"))
    fxhist$.FXCalcSetup$setCurrentDate(startDate)
	newOption <- fxhist$createNewOption()
    checkEquals(round(newOption$.strike,6),1.1664)
    
# Check test for rebalancing in time
	price <- fxhist$priceCurrent(newOption)
	fxhist$.FXCalcSetup$setCurrentDate(anotherDate)
	checkTrue(fxhist$checkNeedToRebalance(price,newOption,rebalPeriod="2w",rebalDelta=0.15))
	fxhist$.FXCalcSetup$setCurrentDate(startDate)
	price <- fxhist$priceCurrent(newOption)
    checkTrue(!fxhist$checkNeedToRebalance(price,newOption,rebalPeriod="2w",rebalDelta=0.15))

    # Check test for rebalancing in delta 
    price[2] <- 0.70
    checkTrue(fxhist$checkNeedToRebalance(price,newOption,rebalPeriod="2w",rebalDelta=0.15))
    price[2] <- 0.30
    checkTrue(fxhist$checkNeedToRebalance(price,newOption,rebalPeriod="2w",rebalDelta=0.15))
    
# check Whole thing
    fxhist$computeEquityCurve(rebalPeriod="2w",rebalDelta=0.15)
    checkEquals(as.numeric(round(fxhist$.FXCalcSetup$getCurrPnL()[as.POSIXct(endDate),1],1)),371611.2)
    lastRollInfo <- fxhist$.FXCalcSetup$getRollHistory()[as.POSIXct(endDate),]
	checkEquals(as.POSIXct(as.numeric(lastRollInfo[,"rollDate"])),as.POSIXct("2007-01-31"))       
#check Interpolation scheme.    
    oldFXOption <- FXEuropeanOptionSpecific$initSpecificFromGeneric(fxhist$.FXOptionObj,startDate,fxhist$.holidayList)
    fxhist$.FXCalcSetup$setCurrentDate(anotherDate)
    dateList <- fxhist$.FXCalcSetup$determineInterpolationScheme(expiry = oldFXOption$getExpiryDate(),maxTenor = which(FXTenor$getTenors()==fxhist$.FXOptionObj$getExpiry()))
    checkEquals(dateList$daysOne,342)
    checkEquals(dateList$daysTwo,23)
#check non-backtest Mode
	lastRollInfo <- data.frame(as.POSIXct("2006-12-26"),1.1645,as.POSIXct("2008-12-23"),as.POSIXct("2008-12-29"))
	colnames(lastRollInfo) <- c("rollDate","strike","expiryDate","settleDate")  
	fxhist <- FXEuropeanOptionTri(FXOptionObj=usdchfoption,putCall = optionType, fxNotional = fxNotional,
        startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB(),overUnder = overUnder, initialEquity = 100000,lastRollInfo = lastRollInfo)
  fxhist$computeEquityCurve(rebalPeriod="2w",rebalDelta=0.15)
  checkEquals(as.numeric(round(fxhist$.FXCalcSetup$getCurrPnL()[as.POSIXct(endDate),1],1)),467531.1)
  lastRollInfo <- fxhist$.FXCalcSetup$getRollHistory()[as.POSIXct(endDate),]
  checkEquals(as.POSIXct(as.numeric(lastRollInfo[,"rollDate"])),as.POSIXct("2007-01-23"))
  
  fxhist <- FXEuropeanOptionTri(FXOptionObj=usdchfoption,putCall = optionType, fxNotional = fxNotional,
        startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB(),overUnder = overUnder, initialEquity = 100000)
  fxhist$computeEquityCurve(rebalPeriod="2w",rebalDelta=0.15)
  checkEquals(as.numeric(round(fxhist$.FXCalcSetup$getCurrPnL()[as.POSIXct(endDate),1],1)),471611.2)
  lastRollInfo <- fxhist$.FXCalcSetup$getRollHistory()[as.POSIXct(endDate),]
  checkEquals(as.POSIXct(as.numeric(lastRollInfo[,"rollDate"])),as.POSIXct("2007-01-31"))

}
