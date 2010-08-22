# test for the STOAnalysis class
# 
# Author: rsheftel
###############################################################################

library(QFSTO)

testStoID <- 179030

test.constructor <- function(){
	checkInherits(STOAnalysis(123),"STOAnalysis")
	shouldBomb(STOAnalysis('123'))
	shouldBomb(STOAnalysis())
}

test.loadStoItems <- function(){
	sa <- STOAnalysis(testStoID)
	sa$loadStoDetails()
	checkInherits(sa$stoDetails(),"STOSetup")
	checkSame(length(sa$.parameters), length(c(sa$.fixedParameters, sa$.variableParameters)))
	checkSame(c('ATRLen','nDays','nATRentry','exitATRmultiple'), sa$.variableParameters)
	
	sa$loadStoObject()
	checkInherits(sa$stoObject(), "STO")
}

test.runSTOSetupReport <- function(){
	sa <- STOAnalysis(testStoID)
	sa$loadStoDetails()
	sa$runSTOSetupReport()
}

test.portfolioReports <- function(){
	sa <- STOAnalysis(testStoID)
	sa$loadStoDetails()
	sa$loadStoObject()
	sa$metricList(list(QKRatio,QCalmarRatio,QAnnualizedNetProfit))
	checkSame(list(QKRatio,QCalmarRatio,QAnnualizedNetProfit), sa$metricList())
	portfolio <- 'BondFutures'
	sa$.makeMetricCube(portfolio)
	
	#Test the private methods that run on a single portfolio
	sa$.runSTOValidationTable(portfolio)
	sa$.runWiskograms(portfolio)
	sa$.runSurfaceSliceReports(portfolio)
	sa$.runReshapeMetrics(portfolio)
	sa$.runSortedRunsByKeyMetrics(portfolio)
	sa$.runSmashFile(portfolio)
}

test.runAll <- function(){
	#sa <- STOAnalysis(testStoID)
	#sa$runAll(list(QKatio, QCalmarRatio, QAnnualizedNetProfit))
}