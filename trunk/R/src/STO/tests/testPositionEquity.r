library("STO")

source(system.file("testHelper.r", package = "STO"))
loader <- CurveFileLoader(CurveCube$filename(dataDir("Curves"), msivTY, 1))

testInterval <- function() {
    curve <- PositionEquityCurve(loader)
    checkSameLooking(last(curve$dates()), "2007-08-31")
    checkSame(last(curve$pnl()), -2961718.75)

    monthly <- PositionEquityCurve(loader, interval=Interval$MONTHLY)
    checkSame(22, length(monthly$dates()))
    checkSameLooking(last(monthly$dates()), "2007-08-01")
    checkSame(last(monthly$pnl()), 6160374.99)
    checkSameLooking(c(0, 4092, -4552, -4552, -4520, -4520, 0, 0, 4665, 5078, 5005, 0, 4567, -4559, -4559, 5159, 0, 0, -5428, 0, 3791, 3791), monthly$position())

    withMonthly <- curve$withInterval(Interval$MONTHLY)
    checkSame(last(withMonthly$pnl()), 6160374.99)
    checkSameLooking(c(0, 4092, -4552, -4552, -4520, -4520, 0, 0, 4665, 5078, 5005, 0, 4567, -4559, -4559, 5159, 0, 0, -5428, 0, 3791, 3791), withMonthly$position())
    
}

testRange <- function() {
    curve <- PositionEquityCurve(loader, range=Range("2007-03-02", "2007-03-09"))
    checkSameLooking(first(curve$dates()), "2007-03-02")
    checkSameLooking(last(curve$dates()), "2007-03-09")
    checkLength(curve$dates(), 6)
}

testCanDetermineIfWasPopulatedForEntireRange <- function() {
    curve <- PositionEquityCurve(loader)
    checkTrue(curve$covers(Range("2005-11-11", "2007-08-30"))) # exact
    checkTrue(curve$covers(Range("2006-11-11", "2007-08-01"))) # inside
    checkFalse(curve$covers(Range("2005-11-10", "2007-08-30"))) # left edge out
    checkFalse(curve$covers(Range("2005-11-11", "2007-08-31"))) # right edge out
}
