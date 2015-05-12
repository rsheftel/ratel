## Test file for the FXHolidayDates object
library("QFFX")

testFXOptionRollInfo <- function()
{
	gbpusd <- FXCurr$setByPairs("gbp","usd")
	rollInfo <- FXOptionRollInfo$getLastEquityAndRollInfo(currencyPair=gbpusd,tenor="6m",putCall="call",obsDate=as.POSIXct("1997-01-03"),tsdb=TimeSeriesDB(),
		rebalPeriod="2w", rebalDelta=0.15)
	checkSame(as.numeric(rollInfo$tri),0)
	checkSame(as.numeric(rollInfo$lastRollDate),852267600)
	checkSame(as.numeric(rollInfo$strike),1.6809)
	checkSame(as.numeric(rollInfo$expiryDate),867816000)
	checkSame(as.numeric(rollInfo$settleDate),868248000)
	
	
}