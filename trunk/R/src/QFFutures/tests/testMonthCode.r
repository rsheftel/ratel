library(QFFutures)

testMonthCodes <- function() {
    checkSame("h", MonthCode$letter(3))
    checkSame(3, MonthCode$number("h"))
    checkSame(3, MonthCode$number("H"))
}