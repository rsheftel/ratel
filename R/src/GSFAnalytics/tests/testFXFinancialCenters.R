## Test file for the FXHolidayDates object
library("GSFAnalytics")

testFXFinancialCenters <- function()
{
    tsdb <- TimeSeriesDB()
    ccy.list <- GetCurrencies(tsdb)
    badTest <- "BusinessTime"
    financialCenterList <- FXFinancialCenters$getListOfFinancialCenters()
    for (i in ccy.list)
    {
       FXFinancialCenters$getFinancialCenterGivenCurrency(i)
    }
    
    checkEquals(any(financialCenterList==badTest),FALSE)
    
    checkEquals(FXFinancialCenters$getFinancialCenterGivenCurrency("usd"),"nyb")
    checkEquals(FXFinancialCenters$getFinancialCenterGivenCurrency("hkd"),"hkb")
    checkEquals(FXFinancialCenters$getFinancialCenterGivenCurrency("ars"),"bab")
    checkEquals(FXFinancialCenters$getFinancialCenterGivenCurrency("kzt"),"ayb")
    
    shouldBomb(FXFinancialCenters$getFinancialCenterGivenCurrency(badTest))
    
    
}