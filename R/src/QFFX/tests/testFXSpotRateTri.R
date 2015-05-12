library("QFFX")

testFXSpotRateTri <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-02-01"
    nextDate <- "2007-01-04"
    quoteside = "mid"
    cross = "usd/chf"
    
	notional <- 100
    notionalFlag <- "over"
    fxNotional <- FXNotional(notional,notionalFlag)

    usdchfccy <- FXCurr$setByCross(cross)
	shouldBomb(FXSpotRateTri(usdchfccy,fxNotional,"goodtimes",endDate,TimeSeriesDB(),overUnder="over",DataLoad=NULL,initialEquity=0))
	shouldBomb(FXSpotRateTri(usdchfccy,fxNotional,startDate,"badtimes",TimeSeriesDB(),overUnder="over",DataLoad=NULL,initialEquity=0))
    
	fxhist <- FXSpotRateTri(usdchfccy, fxNotional = fxNotional,
        startDate=startDate,endDate=endDate,tsdb=TimeSeriesDB(),overUnder="over")
 
	fxhist$computeEquityCurve()
    firstRate <- as.numeric(fxhist$.DataLoad$getRateData()[as.POSIXct(startDate),"spot"])
	nextRate <-  as.numeric(fxhist$.DataLoad$getRateData()[as.POSIXct(nextDate),"spot"])
	priceRes <- (1 - firstRate/nextRate)
	repo <- fxhist$getRepoData(as.POSIXct(startDate))
	effectiveRate <- as.numeric(repo["overRepoRate"]) - as.numeric(repo["underRepoRate"])
	accrualDays <- 3
	carry <- accrualDays / 360 * effectiveRate
	expectedResult <- notional * 1000000 * (priceRes + carry)
	realResult <- as.numeric(fxhist$.currPnL[as.POSIXct(nextDate)])
	checkEquals(round(expectedResult,0),round(realResult,0))
}
