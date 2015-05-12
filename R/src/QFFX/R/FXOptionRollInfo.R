setConstructorS3("FXOptionRollInfo", function(...)
{
    this <- extend(RObject(), "FXOptionRollInfo")
    this
    
})
method("getLastEquityAndRollInfo","FXOptionRollInfo", function(currencyPair = NULL, tenor = NULL, putCall=NULL,obsDate = NULL, 
  tsdb = NULL,rebalPeriod = "2w", rebalDelta=0.15,...)
{
  needs(currencyPair="FXCurr")
	quote.convention <- "tri_local_ccy"
  ccy.pair <- squish(currencyPair$over(), currencyPair$under())
  rebal_type <- squish(rebalPeriod,(rebalDelta*100),"d")
    
  time.series.name <- paste(ccy.pair, tenor, putCall, quote.convention,rebal_type,sep = "_")
  time.series.source <- "internal"
  tri <- tsdb$retrieveOneTimeSeriesByName(name=time.series.name,data.source=time.series.source,start=obsDate,end=obsDate)
  quote.convention <- "tri"
  tsdbfct <- function(type) {
	  time.series.name <- paste(ccy.pair, tenor, putCall, quote.convention, rebal_type, type,sep = "_")
	  tsdb$retrieveOneTimeSeriesByName(name=time.series.name,data.source=time.series.source,start=obsDate,end=obsDate)
  }
  lastRollDate <- tsdbfct("lastrolldate")
  strike <- tsdbfct("strike")
  expiryDate <- tsdbfct("expirydate")
  settleDate <- tsdbfct("settledate")
  return(list(tri=tri,lastRollDate=lastRollDate,strike=strike,expiryDate=expiryDate,settleDate=settleDate))
})
      