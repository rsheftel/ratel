library(QFPortfolio)

testCanCreateARollingWindowAndUseIt <- function() {
    w <- RollingWindow(Period$months(2))
    day <- as.POSIXct("2008/01/01")
    checkSameLooking("2008/01/01 to 2008/03/01 23:59:59", w$following(day))
    checkSameLooking("2007/11/01 to 2008/01/01 23:59:59", w$preceding(day))
}
