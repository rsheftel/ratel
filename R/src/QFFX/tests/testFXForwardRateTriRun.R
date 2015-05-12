library("QFFX")

testFXForwardRateTriRun <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-01-12"
    quoteside = "mid"
    cross = "usd/jpy"
    tsdb <- TimeSeriesDB()
	fxhist <- FXForwardRateTriRun(FXCurr=FXCurr$setByCross(cross),tenorList = c("spot"),
        startDate=startDate,endDate=endDate,tsdb=tsdb,writeToTSDB=FALSE, overUnder = "over",rebalPeriod = "1d",BackPopulate=TRUE)
	checkDate <- as.POSIXct("2007-01-12")
    checkEquals(round(as.numeric(fxhist[checkDate,1]),5),10.17641)   

}
    
