library(Live)

testClosingData <- function() {
    data <- CdsTransformationDailyData("abc_snrfor_usd_xr", as.POSIXct("2007/11/30"))
    checkSame(52.7242561537261, data$closingSpread())    
}

testHolidays <- function() {
    data <- CdsTransformationDailyData("abc_snrfor_usd_xr", as.POSIXct("2007/11/30"))
    checkTrue(as.POSIXct("2008/01/01") %in% data$holidays())
}
