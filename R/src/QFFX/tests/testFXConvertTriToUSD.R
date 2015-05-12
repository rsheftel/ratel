library("QFFX")

testFXConvertTriToUSD <- function()
{
  
  checkDate <- "2007-09-28"
  localTRI <- c("usdjpy_spot_tri_local_ccy")
  targetTRI <- c("usdjpy_spot_tri")
  result <- FXConvertTriToUSD$convertTriToUSD(localTRI, targetTRI, writeToTSDB=FALSE)
  expectedResult <- as.numeric(TimeSeriesDB$retrieveOneTimeSeriesByName(name=localTRI,data.source="internal")[as.POSIXct(checkDate)])
  checkEquals(as.numeric(result[as.POSIXct(checkDate)]),expectedResult)
  
  
  localTRI <- c("eurjpy_spot_tri_local_ccy")
  targetTRI <- c("eurjpy_spot_tri")
  result <- FXConvertTriToUSD$convertTriToUSD(localTRI, targetTRI, writeToTSDB=FALSE)
  rawResult <- as.numeric(TimeSeriesDB$retrieveOneTimeSeriesByName(name=localTRI,data.source="internal")[as.POSIXct(checkDate)])
  conversionRate <- as.numeric(TimeSeriesDB$retrieveOneTimeSeriesByName(name="eurusd_spot_rate_mid",data.source="internal")[as.POSIXct(squish(checkDate," 15:00:00"))])
  expectedResult <- rawResult * conversionRate
  checkEquals(as.numeric(result[as.POSIXct(checkDate)]),expectedResult)
  
}