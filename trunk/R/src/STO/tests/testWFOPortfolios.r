library("STO")

source(system.file("testHelper.r", package = "STO"))

testEqualWeighting <- function() {
	wfo <- createWFO()
	shouldBomb(wfo$portfolioWeights())
	schedule <- wfo$createSchedule("2003-09-27",365)
	pw <- wfo$calcPortfolioWeights(p.EqualWeighting)
	pw.target <- data.frame(step = 1:5,CVE_10_Daily_CET10.AEP5M = 0.5,CVE_10_Daily_CET10.AES5X = 0.5)
	checkSame(pw,pw.target)
	checkSame(wfo$portfolioWeights(),pw.target)
	destroyWFO()
}

testPositiveMedianProfit <- function() {
	wfo <- createWFO()
	schedule <- wfo$createSchedule("2003-09-27",365)
	pw <- wfo$calcPortfolioWeights(p.PositiveMedianProfit,wfo$msivs())
	pw.target <- data.frame(step = 1:5,CVE_10_Daily_CET10.AEP5M = c(0,0,0,0,0.5),CVE_10_Daily_CET10.AES5X = c(0,1,1,1,0.5))
	checkSame(pw,pw.target)
	destroyWFO()
}