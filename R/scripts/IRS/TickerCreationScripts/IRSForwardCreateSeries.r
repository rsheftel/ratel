expiryList <- c("1w","1m","3m","6m","9m","1y","2y","5y","10y")
tenorList <- c("1y","2y","5y","7y","10y","15y","30y")
currencyList <- c("usd")
sourceList <- c("jpmorgan")

createIRSForwardTimeSeriesIfNotExists <- function(tsdb,...)
{
  for(tenor in tenorList) {
    for(forward in forwardList) {
      for (currency in currencyList) {
        quote_type <- "close"
        quote_side <- "mid"
        ccy <- currency
        instrument <- "irs"
        quote_convention <- c("fwd_rate")
        time.series.name <- paste(instrument, currency, quote_convention,paste(forward,tenor,sep=""),"mid",sep="_")
        if (!(tsdb$timeSeriesExists(time.series.name)))
        {
          attributes = list(
          quote_type = quote_type,
          ccy = currency,
          quote_side = quote_side,
          instrument = instrument,
          tenor = tenor,
          fwd_start = forward,                                                                                                        
          quote_convention = quote_convention
          )
          cat("Creating:",time.series.name,"\n")
          tsdb$createTimeSeries(name = time.series.name, attributes = attributes)       
     
        }
      }
    }
  }
}

purgeIRSForwardTimeSeries <- function(tsdb,...)
{
  for(tenor in tenorList) {
    for(forward in forwardList) {
      for (currency in currencyList) {    
        quote_type <- "close"
        quote_side <- "mid"
        instrument <- "irs"
        ccy <- currency
        quote_convention <- c("fwd_rate")
        time.series.name <- paste(instrument, currency, quote_convention,paste(forward,tenor,sep=""),"mid",sep="_")
        if(tsdb$timeSeriesExists(time.series.name))
        {
          tsdb$purgeTimeSeries(time.series.name,source=source)
        }
      }
    }
  }
}       

deleteIRSForwardTimeSeries <- function(tsdb,...)
{
  for(tenor in tenorList) {
    for(forward in forwardList) {
      for (currency in currencyList) {    
        quote_type <- "close"
        quote_side <- "mid"
        instrument <- "irs"
        quote_convention <- c("fwd_rate") 
        time.series.name <- paste(instrument, currency, quote_convention, paste(forward,tenor,sep=""), "mid",sep="_")
        if(tsdb$timeSeriesExists(time.series.name))
        {
          tsdb$.deleteTimeSeries(time.series.name,are.you.sure=TRUE)
        }
      }
    }
  }
}