FXEuropeanOptionPayoutRatioRun <- function(FXCurr = NULL, tenorList = NULL, startDate = NULL, endDate = NULL, tsdb = NULL,putCall = NULL, writeToTSDB=FALSE,...)
{
   quoteside <- "mid"
   dataLoad <- FXDataLoad(FXCurr,startDate,endDate,tsdb,tenor="2y")
   
   list.dates <- index(dataLoad$getRateData())
   newStartDate <- list.dates[1]
   
   quote.convention <- "payoutratio"
   quoteside <- "mid"
    for (tenor in tenorList){
      fwd.generic <- FXForwardGeneric(FXCurr,quoteside,tenor)
      option.generic <- FXEuropeanOptionGeneric(fwd.generic,expiry = tenor,putCall = putCall)
      fxhist <- FXEuropeanOptionPayoutRatio(FXOptionObj=option.generic, startDate=newStartDate, endDate=endDate, tsdb = tsdb, putCall = putCall, DataLoad = dataLoad)
      fxhist$computePayoutRatio()
      pnl <- fxhist$getPayoutRatio()
  
      ccy.pair <- paste(FXCurr$over(), FXCurr$under(), sep = "")
      time.series.name <- paste(ccy.pair, tenor, putCall, quote.convention, quoteside, sep = "_")
      time.series.source <- "internal"
    
      if (writeToTSDB){
        cat("Writing to DB:",time.series.name,"\n")
        tsdb <- TimeSeriesDB()
        tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(time.series.name, time.series.source))
        tsMatrix[[1,1]] <- pnl
        fx.data <- tsdb$writeTimeSeries(tsMatrix)
      }
    }
  return(pnl)
}
