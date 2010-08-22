library(STO)
source(system.file("testHelper.r", package = "STO"))

test.constructor <- function(){
	sto <- MetricFrame()
	checkInherits(sto, "MetricFrame")
}

test.reportMultipleMsivsSingleRun <- function(){
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	reportFrame <- MetricFrame$multipleMsivsSingleRun(sto$curves(),sto$msivs(), 1, list(CalmarRatio,AnnualizedNetProfit))
	
	curve <- sto$curves()$curve(first(sto$msivs()), 1)
	expected <- curve$metric(CalmarRatio)
	checkSame(reportFrame[1,1],expected)	
	
	curve <- sto$curves()$curve(sto$msivs()[[2]], 1)
	expected <- curve$metric(AnnualizedNetProfit)
	checkSame(reportFrame[2,2],expected)
	
	checkSame(round(reportFrame[3,2],3), 7139.701)
}

test.reportSingleMsivMultipleRuns <- function(){
	sto <- STO(stoDirectory(), "ManyDatesSTO")
	reportFrame <- MetricFrame$singleMsivMultipleRuns(sto$curves(),sto$msivs()[[2]], c(1,3), list(CalmarRatio,NetProfit))
	
	curve <- sto$curves()$curve(sto$msivs()[[2]], 1)
	expected <- curve$metric(CalmarRatio)
	checkSame(reportFrame[1,1],expected)	
	
	curve <- sto$curves()$curve(sto$msivs()[[2]], 3)
	expected <- curve$metric(NetProfit)
	checkSame(reportFrame[2,2],expected)
	
	checkSame(round(reportFrame[3,2],3), 62550)
}
