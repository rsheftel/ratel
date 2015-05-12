library("QFFX")

testFXEuropeanOptionPayoutRatioRun <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-01-14"
    
    quoteside = "mid"
    cross = "usd/jpy"
    putCall <- "call"
    tsdb <- TimeSeriesDB()
    data.source <- "goldman"
    tenorList <- c("6m")
    
    fxhist <- FXEuropeanOptionPayoutRatioRun(FXCurr=FXCurr$setByCross(cross),tenorList = tenorList,
        startDate=startDate,endDate=endDate,tsdb=tsdb, putCall = putCall, writeToTSDB=FALSE)
    checkDate <- as.POSIXct("2007-01-12")
    checkEquals(round(as.numeric(fxhist[checkDate,1]),3),1.259)   
    

}
    
