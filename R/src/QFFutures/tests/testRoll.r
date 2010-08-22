library(QFFutures)

testConstructor <- function() {    
    this <- Roll(rollMethod = function(x)daysToExpiry(x,5))
    shouldBomb(Roll(rollMethod = "not a function"))
}

test.rollDates <- function() {
    expiryDates <- c("2008-01-01","2008-03-01")
    this <- Roll(rollMethod = function(x)daysToExpiry(x,5))
    checkSame(this$rollDates(expiryDates),as.POSIXct(c("2007-12-24","2008-02-25")))
    expiryDates <- as.POSIXct(c("2008-01-01","2008-03-01"))
    this <- Roll(rollMethod = function(x)daysToExpiry(x,5))
    checkSame(this$rollDates(expiryDates),as.POSIXct(c("2007-12-24","2008-02-25")))
    expiryDates <- as.POSIXct(c("2008-01-01 15:00:00","2008-03-01 15:00:00"))
    this <- Roll(rollMethod = function(x)daysToExpiry(x,5))
    checkSame(this$rollDates(expiryDates),as.POSIXct(c("2007-12-24","2008-02-25")))    
}

test.daysToExpiry <- function() {
    expiryDates <- c("2008-01-01","2008-03-01")
    checkSame(daysToExpiry(expiryDates,5,"nyb"),as.POSIXct(c("2007-12-24","2008-02-25")))
    checkSame(daysToExpiry(expiryDates[1],5,"nyb"),as.POSIXct(c("2007-12-24")))
    shouldBomb(daysToExpiry(TRUE))
}


test.calendarDaysToExpiry <- function() {
	expiryDates <- c("2008-01-01","2008-03-01")
	checkSame(calendarDaysToExpiry(expiryDates,1),as.POSIXct(c("2007-12-31","2008-02-29")))
	checkSame(calendarDaysToExpiry(expiryDates[1],5),as.POSIXct(c("2007-12-27")))
	shouldBomb(calendarDaysToExpiry(TRUE))
}
