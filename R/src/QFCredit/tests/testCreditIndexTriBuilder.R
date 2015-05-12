## Test file for the CreditIndexTriBuilder object

library(QFCredit)

test.indexDetails <- function()
{
    result <- CreditIndexTriBuilder$indexDetails("cdx-na-ig","5y",10)
	checkSame(result$strike,0.0155)
	checkSame(result$effDate,as.POSIXct("2008-03-21"))
	checkSame(result$matDate,as.POSIXct("2013-06-20"))
	shouldBomb(CreditIndexTriBuilder$indexDetails("junk","5y",10))
	shouldBomb(CreditIndexTriBuilder$indexDetails("cdx-na-ig","80y",10))
	shouldBomb(CreditIndexTriBuilder$indexDetails("cdx-na-ig","5y",0))	
}

test.getMarkitNameFromTickerName <- function()
{
	result <- CreditIndexTriStitcher$getMarkitNameFromTickerName(ticker = "cdx-na-ig")
	checkSame(result,"CDXNAIG")
	shouldBomb(CreditIndexTriStitcher$getMarkitNameFromTickerName(ticker = "junk"))
}

test.getTickerNameFromMarkitName <- function()
{
	result <- CreditIndexTriStitcher$getTickerNameFromMarkitName(markit = "CDXNAIG")
	checkSame(result,"cdx-na-ig")
	shouldBomb(CreditIndexTriStitcher$getTickerNameFromMarkitName(ticker = "junk"))
}

test.overrideDetails <- function()
{
	result <- CreditIndexTriStitcher$overrideDetails(ticker = "cdx-na-ig")
	checkShape(result,colnames = c("markit_name","otr_series","otr_version","start_date","end_date"))
	shouldBomb(CreditIndexTriStitcher$overrideDetails(ticker = "junk"))
}

test.constructor <- function()
{
	this <- CreditIndexTriBuilder(ticker = "cdx-na-ig",series = 10,version = 1,tenor = "5y",timeStamp = "15:00:00",holidayCenter = "nyb")
	checkSame(this$.ticker,"cdx-na-ig")
	checkSame(this$.version,1)
	checkSame(this$.series,10)
	checkSame(this$.tenor,"5y")
	checkSame(this$.timeStamp,"15:00:00")
	checkSame(this$.cdsSource,"markit")
	checkSame(this$.irsSource,"internal")
	checkSame(this$.holidayCenter,"nyb")
	checkSame(this$.base,100)
}

test.calcRawTri <- function()
{
	this <- CreditIndexTriBuilder(ticker = "cdx-na-ig",series = 10,version = 1,tenor = "5y",timeStamp = "15:00:00",holidayCenter = "nyb")
	result <- this$calcRawTri("2008-09-01")
	assert(NROW(result$dv01Zoo) > 0)
	assert(NROW(result$dailyTriZoo) > 0)
	
	this <- CreditIndexTriBuilder(ticker = "junk",series = 10,version = 1,tenor = "5y",timeStamp = "15:00:00",holidayCenter = "nyb")
	shouldBomb(this$calcRawTri("2008-09-01"))
	this <- CreditIndexTriBuilder(ticker = "junk",series = 3,version = 1,tenor = "5y",timeStamp = "15:00:00",holidayCenter = "nyb")
}

test.calcStitchedTri <- function()
{
	startDate <- "2008-09-29"
	this <- CreditIndexTriStitcher("itraxx-eur","otr","5y","11:00:00","nyb")
	resList <- this$calcStitchedTri(startDate)
}