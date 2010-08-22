library(STO)

source(system.file("testHelper.r", package = "STO"))

testRange <- function() {
    range <- Range("2007/01/01", "2007/12/31")
    dates <- seq(as.POSIXct("2005-01-01 15:00:00"), as.POSIXct("2008-12-31 15:00:00"), 86400)
    z <- zoo(seq_along(dates), dates)
    checkLength(range$cut(z), 365)
    range <- Range("2007/01/01")
    checkLength(range$cut(z), 1)
    range <- Range$before("2007/01/01")
    checkLength(range$cut(z), 365*2)
    range <- Range$after("2006/12/31")
    checkLength(range$cut(z), 365*2+1) # leap year
    range <- Range$after("2008/12/31")
    checkLength(range$cut(z), 0)
    range <- Range$before("2005/01/01")
    checkLength(range$cut(z), 0)
}

testStaticRanges <- function() {		
	assert(class(Range$ytd()) %in% 'Range')
	assert(class(Range$mtd()) %in% 'Range')
	assert(class(Range$last20days()) %in% 'Range')
	assert(class(Range$lastMonth()) %in% 'Range')
	relativeTo <- as.POSIXct('2009-02-15')
	checkSame(Range$ytd(relativeTo),Range(as.POSIXct('2009-01-01'),relativeTo))
	checkSame(Range$mtd(relativeTo),Range(as.POSIXct('2009-02-01'),relativeTo))
	checkSame(Range$last20days(relativeTo),Range(as.POSIXct('2009-01-26'),relativeTo))
	checkSame(Range$lastNDays(5,relativeTo),Range(as.POSIXct('2009-02-10'),relativeTo))
	checkSame(Range$lastMonth(relativeTo),Range(as.POSIXct('2009-01-15'),relativeTo))
	checkSame(Range$lastNMonths(5,relativeTo),Range(as.POSIXct('2008-09-15'),relativeTo))
	shouldBomb(Range$lastNMonths('sd',relativeTo))
	shouldBomb(Range$lastNMonths(5,'2008-01-01'))
	shouldBomb(Range$lastNDays('sd',relativeTo))
	shouldBomb(Range$lastNDays(5,'2008-01-01'))
	relativeTo <- '2008-01-01'
	shouldBomb(Range$ytd(relativeTo))
	shouldBomb(Range$mtd(relativeTo))
	shouldBomb(Range$last20days(relativeTo))
}