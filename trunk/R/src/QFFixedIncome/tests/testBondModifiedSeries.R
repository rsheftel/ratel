## Test file for the BondModifiedSeries object
library(QFFixedIncome)

test.BondModifiedSeries.constructor <- function(){
    this <- BondModifiedSeries(sector = "government",issuer = "us_treasury",ccy = "usd",data.source = "internal",timeStamp = "15:00:00")
    checkEquals(this$.instrument,"bond")
    checkEquals(this$.sector,"government")
    checkEquals(this$.issuer,"us_treasury")
    checkEquals(this$.ccy,"usd")
    checkEquals(this$.data.source,"internal")
    checkEquals(this$.timeStamp,"15:00:00")
    assert("TimeSeriesDB" %in% class(this$.tsdb))
}

test.BondModifiedSeries.updateContinuousYieldsSeriesInTSDB <- function(){
    this <- BondModifiedSeries()
    maturity = "10y"
    front = "otr"
    back = "1o"
    modified = "1c"
    adjustmentType = "ratio"
    result <- this$updateContinuousYieldsSeriesInTSDB(maturity,front,back,modified,adjustmentType)
    checkEquals(result,TRUE)
}