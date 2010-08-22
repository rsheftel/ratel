#
#	Test of PortfolioRunsReport class  (rsheftel)
#
#################################################################

library(QFPortfolio)
library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/PortfolioRunsReport/')

portfolioRunsObject <- function(){
	curvesPath <- squish(system.file("testdata", package="QFPortfolio"),'/PortfolioRun/testCurves/')
	mrun <- PortfolioRuns(name="ReportTest",groupName="TestPortfolio1", curvesDirectory=curvesPath, curvesExtension="csv")
	
	makeRun <- function(prun, weights){
		#Set up first run with some stuff
		slippages <- list(0.01, 0.02)
		names(slippages) <- rev(prun$curveNames())
		prun$slipCurves(slippages)	
		prun$objective(NetProfit)
		mins <- list(0,0)
		names(mins) <- prun$curveNames()
		maxs <- list(2,2)
		names(maxs) <- prun$curveNames()
		prun$minMaxWeights(mins, maxs)
		prun$addConstraint('Portfolio',MaxDrawDown,-200000,0)
		seeds <- list(1,1)
		names(seeds) <- prun$curveNames()
		prun$seeds(seeds)
		div <- c(1,2)
		names(div) <- prun$curveNames()
		prun$diversityScore(as.list(div))
		names(weights) <- prun$curveNames()
		prun$setOptimalWeights(weights)
		metrics <- list(NetProfit, MaxDrawDown)
		prun$calculateMetrics(metrics, verbose=TRUE)
	}
	
	mrun$addRun("First")
	prun <- mrun$currentRun()
	prun$dateRange(start='2007-01-01', end='2007-12-31')
	prun$loadCurves(mrun$childWeightedCurves())
	makeRun(prun, c(2,3))
	
	mrun$addRun("Second")
	prun <- mrun$currentRun()
	prun$dateRange(start='2006-01-01', end='2006-12-31')
	prun$loadCurves(mrun$childWeightedCurves())
	makeRun(prun, c(4,5))
	
	return(mrun)
}

test.addTitle <- function(){
	mrun <- portfolioRunsObject()  
	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
	checkInherits(report, "PortfolioRunsReport")
	checkSame(squish(filename,'.html'), report$filenames()$html)

	report$addTitle()
	report$closeConnection()
	hWriterFileMatches(squish(filename,'.html'), squish(testdata,'addTitle.html'),deleteFile=FALSE,startLine=6)
}

#test.addSettings <-function(){
#	mrun <- portfolioRunsObject() 
#	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
#	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
#	report$addSettings()
#	report$closeConnection()
#	hWriterFileMatches(squish(filename,'.html'), squish(testdata,'addSettings.html'),deleteFile=FALSE,startLine=6, endLineOffset=5)
#}

test.addChildItems <- function(){
	mrun <- portfolioRunsObject() 
	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
	report$addChildReports()
	report$addChildReportLinks()
	report$closeConnection()
}

test.graphs <- function(){
	mrun <- portfolioRunsObject() 
	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
	report$plotPortfolioEquity()
	report$addGraphLink()
	report$closeConnection()
}

test.addOptimizationSummary <- function(){
	mrun <- portfolioRunsObject() 
	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
	report$addOptimizationSummary()
	report$closeConnection()
	hWriterFileMatches(squish(filename,'.html'), squish(testdata,'addOptimizationSummary.html'),deleteFile=FALSE,startLine=6)
}

test.addWeights <- function(){
	mrun <- portfolioRunsObject() 
	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
	report$addWeights()
	report$closeConnection()
	hWriterFileMatches(squish(filename,'.html'), squish(testdata,'addWeights.html'),deleteFile=FALSE,startLine=6)
}

test.addMetrics <- function(){
	mrun <- portfolioRunsObject() 
	filename <- squish(testdata,'testPortfolioRunsReport_',round(abs(rnorm(1))*1000,4))
	report <- PortfolioRunsReport(mrun, filename, childReportDirectory=testdata)
	report$addMetrics()
	report$closeConnection()
	hWriterFileMatches(squish(filename,'.html'), squish(testdata,'addMetrics.html'),deleteFile=FALSE,startLine=6)
}

#Example usage, not tested
#report$reportAll(10000)
#report$openReport()