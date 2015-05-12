library("QFFX")

testFXEuropeanOptionTriRun <- function()
{
    startDate <- "2007-06-04"
    endDate <- "2007-06-14"
    rebalPeriod <- "2w"
    rebalDelta <- 0.15
    quoteside = "mid"
    cross = "usd/jpy"
    tsdb <- TimeSeriesDB()
    data.source <- "internal"
    tenorList <- c("6m")
    optionList <- c("call")
# Test backtesting....
    fxhist <- FXEuropeanOptionTriRun(FXCurr=FXCurr$setByCross(cross),tenorList = tenorList, putCallList = optionList,
        startDate=startDate,endDate=endDate,tsdb=tsdb,writeToTSDB=FALSE,overUnder = "over", rebalPeriod=rebalPeriod, 
		rebalDelta=rebalDelta,BackPopulate=TRUE)
    checkDate <- as.POSIXct("2007-06-12")
    checkEquals(round(as.numeric(fxhist[checkDate,"pnl"]),6),0.213512)
    checkEquals(as.numeric(fxhist[checkDate,"rollDate"]),1180929600)
    
#test capability to load previous dates
   result <- FXOptionRollInfo$getLastEquityAndRollInfo(currencyPair=FXCurr$setByCross(cross), tenor=tenorList, putCall = optionList, obsDate = startDate, tsdb = tsdb, 
          rebalPeriod=rebalPeriod, rebalDelta=rebalDelta)
    checkEquals(as.numeric(result$lastRollDate),1180929600)       

#Test that we get same result as already performed backtest           
fxhist <- FXEuropeanOptionTriRun(FXCurr=FXCurr$setByCross(cross),tenorList = tenorList, putCallList = optionList,
        startDate=startDate,endDate=endDate,tsdb=tsdb,writeToTSDB=FALSE,overUnder = "over", rebalPeriod=rebalPeriod, rebalDelta=rebalDelta,BackPopulate=FALSE)    

previous.pnl <- tsdb$retrieveOneTimeSeriesByName("usdjpy_6m_call_tri_local_ccy_2w15d",
  data.source="internal",start=startDate,end=endDate)

previous.roll.dates <- tsdb$retrieveOneTimeSeriesByName("usdjpy_6m_call_tri_2w15d_lastrolldate",
  data.source="internal",start=startDate,end=endDate)  

checkEquals(round(as.numeric(fxhist[checkDate,"pnl"]),4),round(as.numeric(previous.pnl[checkDate,1]),4))
checkEquals(as.numeric(previous.roll.dates[checkDate]),as.numeric(fxhist[checkDate,"rollDate"]))


}
    
