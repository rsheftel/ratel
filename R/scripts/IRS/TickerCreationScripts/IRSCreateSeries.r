tenorList <- c("2y","5y","10y","30y")
currencyList <- c("eur","usd","sek","nzd","nok","jpy","gbp","eur","chf","cad","aud")
sourceList <- c("jpmorgan")

createIRSTimeSeriesIfNotExists <- function(tsdb,...)
{
  for(tenor in tenorList) {
      for (currency in currencyList) {
        quote_type <- c("close")
        quote_side <- c("mid")
        ccy <- currency
        instrument <- c("irs")
        quote_convention <- c("rate")
		transformation <- c("raw")
        time.series.name <- paste(instrument, currency, quote_convention,tenor,"mid",sep="_")
        if (!(tsdb$timeSeriesExists(time.series.name)))
        {
          attributes = list(
          quote_type = quote_type,
          ccy = currency,
          quote_side = quote_side,
          instrument = instrument,
          tenor = tenor,                                                                                                 
          quote_convention = quote_convention,
		  transformation = transformation
          )
          cat("Creating:",time.series.name,"\n")
          tsdb$createTimeSeries(name = time.series.name, attributes = attributes)       
     
        }
     }
   }
}

purgeIRSTimeSeries <- function(tsdb,...)
{
  for(tenor in tenorList) {
      for (currency in currencyList) {    
        quote_type <- "close"
        quote_side <- "mid"
        instrument <- "irs"
        ccy <- currency
        quote_convention <- c("rate")
        time.series.name <- paste(instrument, currency, quote_convention,tenor,"mid",sep="_")
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
      for (currency in currencyList) {    
        quote_type <- "close"
        quote_side <- "mid"
        instrument <- "irs"
        quote_convention <- c("rate") 
        time.series.name <- paste(instrument, currency, quote_convention, tenor, "mid",sep="_")
        if(tsdb$timeSeriesExists(time.series.name))
        {
          tsdb$.deleteTimeSeries(time.series.name,are.you.sure=TRUE)
        }
      }
    }
}