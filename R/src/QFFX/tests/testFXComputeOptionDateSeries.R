library("QFFX")
testFXComputeOptionDateSeries <- function()
{
  fxcurr <- FXCurr$setByCross("usd/jpy")
  tenor <- "1m"
  tsdb <- TimeSeriesDB()
  dateList <- c("2007-11-28","2007-11-29")
  fxhist <- FXComputeOptionDateSeries(tsdb = tsdb,FXCurr = fxcurr,tenor = tenor,dateList = dateList)
  dates <- fxhist$computeOptionDateSeries(writeToTSDB=FALSE)
  res <- as.POSIXct(as.numeric(dates[as.POSIXct("2007-11-28"),"expiry_date_zoo"]))
  checkEquals(res,as.POSIXct("2007-12-27"))
  res <- as.POSIXct(as.numeric(dates[as.POSIXct("2007-11-29"),"settle_date_zoo"]))
  checkEquals(res,as.POSIXct("2008-01-04"))
  
}
    
