#
#	Test of PortfolioReport class  (rsheftel)
#
#################################################################

library(QFPortfolio)
library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/PortfolioRunReport/')

portfolioRunObject <- function(){
	curvesPath <- squish(system.file("testdata", package="QFPortfolio"),'/PortfolioRun/testCurves/')
	prun <- PortfolioRun(name="TestRun", groupName="TestPortfolio1", curvesDirectory=curvesPath, curvesExtension="csv")
	prun$dateRange(start='2007-01-01', end='2007-12-31')
	prun$loadCurves()
	
	slippages <- list(0.01, 0.02)
	names(slippages) <- rev(prun$curveNames())
	prun$slipCurves(slippages)
	
	prun$objective(NetProfit)
	prun$penalty(penalty=10000000000)
	
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

	prun$optimize(fastRestarts=1, slowRestrarts=0, slowTol=10^-3)
	metrics <- list(NetProfit, CalmarRatio)
	prun$calculateMetrics(metrics, verbose=TRUE)
	return(prun)
}

test.reportOptimization <- function(){
	prun <- portfolioRunObject()  
	filename <- squish(testdata,'testPortfolioRunReport_',round(abs(rnorm(1))*1000,4))
	
	report <- PortfolioRunReport(prun, filename)
	checkInherits(report, "PortfolioRunReport")
	checkSame(squish(filename,'.html'), report$filenames()$html)
	
	report$reportOptimization()
	report$reportReturns(1000000)
	report$graphOptimization()
	report$graphReturns()
	report$reportCMF()
	report$closeConnection()
	#report$openReport()
	hWriterFileMatches(squish(filename,'.html'), squish(testdata,'testPortfolioRunReport.html'),deleteFile=FALSE,startLine=12,endLineOffset=4)
}
