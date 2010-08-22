
tenorList <- c("2y-1y","5y-2y","10y-5y","15y-10y","30y-15y")

  
createIRSlopePayoutRatioTimeSeries <- function(tsdb,currency,...)
{
    quote_type <- "close"
    ccy <- currency
    quote_convention <- "slope"
    instrument <- "irs"
    transformation <- "payout_ratio"
    
    for (tenor in tenorList) {
      time.series.name <- paste(instrument, currency, quote_convention,tenor,"6m",transformation,sep="_")
      if (!(tsdb$timeSeriesExists(time.series.name)))
      {
        attributes = list(
          quote_type = quote_type,
          ccy = currency,
          tenor = tenor,
          quote_convention = quote_convention,
          instrument = instrument,
          transformation =  transformation
          )
          cat("Creating:",time.series.name,"\n")
          tsdb$createTimeSeries(name = time.series.name, attributes = attributes)
      }
    }
}

purgeIRSlopePayoutRatioTimeSeries <- function(tsdb, currency, source, ...)
{
    quote_type <- "close"
    ccy <- currency
    quote_convention <- "slope"
    instrument <- "irs"
    transformation <- "payout_ratio"
    
    for (tenor in tenorList) {
      time.series.name <- paste(instrument, currency, quote_convention,tenor,"6m",transformation,sep="_")
      if(tsdb$timeSeriesExists(time.series.name))
        {
          tsdb$purgeTimeSeries(time.series.name,source=source)
        }
    }
}

deleteIRSSlopePayoutRatioTimeSeries <- function(currency,...)
{
    quote_type <- "close"
    ccy <- currency
    quote_convention <- "slope"
    instrument <- "irs"
    transformation <- "payout_ratio"
    
    for (tenor in tenorList) {
      time.series.name <- paste(instrument, currency, quote_convention,tenor,"6m",transformation,sep="_")
      if(tsdb$timeSeriesExists(time.series.name))
        {
          tsdb$.deleteTimeSeries(time.series.name,are.you.sure=TRUE)
        }
    }
}
