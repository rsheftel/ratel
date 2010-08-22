expiryList <- c("1w","1m","3m","6m","9m","1y","2y","5y","10y")
tenorList <- c("1y","2y","5y","7y","10y","15y","30y")
currencyList <- c("usd")
sourceList <- c("goldman")

createIRSOptionTimeSeriesIfNotExists <- function(tsdb,...)
{
    
    for(tenor in tenorList) {
       for(expiry in expiryList) {
        for (currency in currencyList) {
        quote_type <- "close"
        quote_side <- "mid"
        underlying <- paste("irs",currency,"rate",sep="_")
        instrument <- "swaption"
        quote_convention.list <- c("price","delta","gamma","vega","vol_ln","vol_bp","vol_bp_daily")
        option_type.list <- c("payer","receiver","straddle")
        strike.list <- c("atm","-25bp","+25bp")
        for (quote_convention in quote_convention.list) {
          for (option_type in option_type.list) {
            for (strike in strike.list) {
              time.series.name <- paste(instrument, currency, paste(expiry,tenor,sep=""), strike, option_type, quote_convention,"mid",sep="_")
              if (!(tsdb$timeSeriesExists(time.series.name)))
              {
                attributes = list(
                quote_type = quote_type,
                option_type = option_type,
                ccy = currency,
                strike = strike,
                quote_side = quote_side,
                instrument = instrument,
                underlying = underlying,
                tenor = tenor,
                expiry = expiry,
                quote_convention = quote_convention
                )
                cat("Creating:",time.series.name,"\n")
                tsdb$createTimeSeries(name = time.series.name, attributes = attributes)
              }
            }
          }
        }
       }
    }
  }
}

purgeIRSOptionTimeSeries <- function(tsdb,...)
{
  for(tenor in tenorList) {
    for(expiry in expiryList) {
      for (currency in currencyList) {    
        quote_type <- "close"
        quote_side <- "mid"
        underlying <- paste("irs",currency,"rate",sep="_")
        instrument <- "swaption"
        quote_convention.list <- c("price","delta","gamma","vega","vol_ln","vol_bp","vol_bp_daily")
        option_type.list <- c("payer","receiver","straddle")
        strike.list <- c("atm","-25bp","+25bp")
        for (quote_convention in quote_convention.list) {
          for (option_type in option_type.list) {
            for (strike in strike.list) {
              time.series.name <- paste(instrument, currency, paste(expiry,tenor,sep=""), strike, option_type, quote_convention,"mid",sep="_")
              if(tsdb$timeSeriesExists(time.series.name))
              {
                tsdb$purgeTimeSeries(time.series.name,source=source)
              }
            }
          }
        }
      }
    }
  }
}

deleteIRSOptionTimeSeries <- function(tsdb,...)
{
  for(tenor in tenorList) {
    for(expiry in expiryList) {
      for (currency in currencyList) {    
        quote_type <- "close"
        quote_side <- "mid"
        underlying <- paste("irs",currency,"rate",sep="_")
        instrument <- "swaption"
        quote_convention.list <- c("price","delta","gamma","vega","vol_ln","vol_bp","vol_bp_daily")
        option_type.list <- c("payer","receiver","straddle")
        strike.list <- c("atm","-25bp","+25bp")
        for (quote_convention in quote_convention.list) {
          for (option_type in option_type.list) {
            for (strike in strike.list) {
              time.series.name <- paste(instrument, currency, paste(expiry,tenor,sep=""), strike, option_type, quote_convention,"mid",sep="_")
              if(tsdb$timeSeriesExists(time.series.name))
              {
                tsdb$.deleteTimeSeries(time.series.name,are.you.sure=TRUE)
              }
            }
          }
        }
      }
    }
  }
}