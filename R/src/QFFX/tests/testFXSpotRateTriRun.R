library("QFFX")

testFXSpotRateTriRun <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-01-12"
    quoteside = "mid"
    cross = "usd/jpy"
    tsdb <- TimeSeriesDB()
	fxhist <- FXSpotRateTriRun(FXCurr=FXCurr$setByCross(cross), startDate=startDate,endDate=endDate,tsdb=tsdb,writeToTSDB=FALSE, overUnder = "over",BackPopulate=TRUE)
	checkDate <- as.POSIXct("2007-01-12")
	checkEquals(round(as.numeric(fxhist[checkDate]),6),201.022564)   

}
    
