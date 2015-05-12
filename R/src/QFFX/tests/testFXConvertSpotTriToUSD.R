library("QFFX")

testFXConvertSpotTriToUSD <- function()
{
  startDate <- "1997-01-03"
  endDate <- "2007-10-31"
  
  fxCurr <- FXCurr$setByCross("usd/jpy")
  res <- FXConvertSpotTriToUSD$convertLocalCcyTri(fxCurr=fxCurr)
  expectedResult <- as.numeric(TimeSeriesDB$retrieveOneTimeSeriesByName(name="usdjpy_spot_tri_local_ccy",data.source="internal")[as.POSIXct("2007-09-28")])
  checkEquals(as.numeric(res[as.POSIXct("2007-09-28"),]),expectedResult)
  
  fxCurr <- FXCurr$setByCross("eur/jpy")
  res <- FXConvertSpotTriToUSD$convertLocalCcyTri(fxCurr=fxCurr)
  rawResult <- as.numeric(TimeSeriesDB$retrieveOneTimeSeriesByName(name="eurjpy_spot_tri_local_ccy",data.source="internal")[as.POSIXct("2007-09-28")])
  conversionRate <- as.numeric(TimeSeriesDB$retrieveOneTimeSeriesByName(name="eurusd_spot_rate_mid",data.source="internal")[as.POSIXct("2007-09-28 15:00:00")])	

  expectedResult <- rawResult * conversionRate
  checkEquals(as.numeric(res[as.POSIXct("2007-09-28"),]),expectedResult)
}