library("QFFX")

testFXDataLoad <- function()
{
    startDate <- "2007-01-03"
    endDate <- "2007-10-31"
    anotherDate <- "2007-02-23"
    quoteside = "mid"
    cross = "usd/jpy"
    optionType = "call"
    expiry <-"2y"
    tenor <- "2y"
    holidayList <- NULL
    close <- NULL
    usdchfccy <- FXCurr$setByCross(cross)
    usdchf <- FXForwardGeneric(usdchfccy,quoteside,tenor)
    
    usdchfoption <- FXEuropeanOptionGeneric(usdchf,expiry)
    
    shouldBomb(FXDataLoad("No Option",startDate,endDate,TimeSeriesDB(),close=close))
    shouldBomb(FXDataLoad(usdchfoption$getFXForwardGeneric(),endDate,startDate,TimeSeriesDB(),close=close))
    shouldBomb(FXDataLoad(usdchfoption$getFXForwardGeneric(),startDate,endDate,TimeSeriesDB(),close="bhj"))

    DataLoad <- FXDataLoad(usdchfoption$getFXCurrency(),startDate,endDate,TimeSeriesDB(),tenor=tenor,close=close)
    exchangeRate <- DataLoad$getRateData()[as.POSIXct(anotherDate),expiry][[1]]
    vol <- DataLoad$getVolData()[as.POSIXct(anotherDate),expiry][[1]]
    checkEquals(round(exchangeRate,3),111.45)
    checkEquals(round(vol,6),0.067171)
	
#	DataLoad <- FXDataLoad(usdchfoption$getFXCurrency(),startDate,endDate,TimeSeriesDB(),tenor=tenor,close="ldn")
#	exchangeRate <- DataLoad$getRateData()[as.POSIXct(anotherDate),expiry][[1]]
#	vol <- DataLoad$getVolData()[as.POSIXct(anotherDate),expiry][[1]]
#	checkEquals(round(exchangeRate,3),111.45)
#	checkEquals(round(vol,6),0.067171)
    
}
    
