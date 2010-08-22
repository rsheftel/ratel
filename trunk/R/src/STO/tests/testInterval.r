library("STO")

testInterval <- function() {
    daily <- Interval("daily")
    checkInherits(daily, "Interval")
    checkSame(daily, Interval("daily"))
    shouldBomb(Interval("foo"))
    Interval("weekly")
    Interval("monthly")
    min20 <- Interval("20minute")
    min1 <- Interval("minute")
    checkFalse(equals(min1, min20))
}

testIntervalGreaterEqual <- function() {
    checkTrue(Interval("daily")$greaterEqual(Interval("daily")))
    checkTrue(Interval("weekly")$greaterEqual(Interval("daily")))
    checkTrue(Interval("monthly")$greaterEqual(Interval("daily")))
    checkFalse(Interval("tick")$greaterEqual(Interval("daily")))
    checkFalse(Interval("minute")$greaterEqual(Interval("daily")))
    checkFalse(Interval("20minute")$greaterEqual(Interval("daily")))
    checkTrue(Interval("20minute")$greaterEqual(Interval("19minute")))
    checkTrue(Interval("20minute")$greaterEqual(Interval("1minute")))
    checkTrue(Interval("20minute")$greaterEqual(Interval("20minute")))
    checkFalse(Interval("minute")$greaterEqual(Interval("20minute")))
    checkTrue(Interval("monthly")$greaterEqual(Interval("weekly")))
    checkTrue(Interval("minute")$greaterEqual(Interval("tick")))
}

checkAsCharacter <- function(name) {
    checkSame(name, as.character(Interval(name)))
}

testIntervalAsCharacter <- function() {
    checkAsCharacter("daily")
    checkAsCharacter("weekly")
    checkAsCharacter("monthly")
    checkAsCharacter("tick")
    checkAsCharacter("minute")
    checkAsCharacter("1minute")
    checkAsCharacter("10minute")
}

testCollapse <- function() {
    dates <- seq(as.POSIXct("2005-01-01 01:00:00"), as.POSIXct("2007-12-31 01:00:00"), 86400)
    z <- zoo(seq_along(dates), dates)
    monthly <- Interval$MONTHLY$collapse(z, sum)
    checkLength(index(monthly), 36)
    checkSame(first(monthly), sum(1:31))
    checkSame(last(monthly), sum((length(dates)-30):length(dates)))
    checkSame(as.numeric(as.POSIXct("2005-01-01")), as.numeric(first(index(monthly))))
    checkSame(as.numeric(as.POSIXct("2007-09-01")), as.numeric(index(monthly)[[33]]))

    weekly <- Interval$WEEKLY$collapse(z, sum)
    checkLength(index(weekly), 52*3+2)
    checkSame(first(weekly), 1)
    checkSame(second(weekly), sum(2:8))
    checkSameLooking(second(index(weekly)), "2005-01-03")
}
