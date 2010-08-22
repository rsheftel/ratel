testCanCreateAMonthsPeriodAndUseIt <- function() {
    p <- Period$months(2)
    day <- as.POSIXct("2008/01/01")
    checkSame(as.POSIXct("2008/03/01"), p$advance(day))
    checkSame(as.POSIXct("2007/11/01"), p$rewind(day))
}

testCanCreateADaysPeriodAndUseIt <- function() {
    p <- Period$days(3)
    day <- as.POSIXct("2008/01/01")
    checkSame(as.POSIXct("2008/01/04"), p$advance(day))
    checkSame(as.POSIXct("2007/12/29"), p$rewind(day))
}
