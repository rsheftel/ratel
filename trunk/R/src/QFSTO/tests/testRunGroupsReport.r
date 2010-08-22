# RunGroupsReport class
# 
# Author: rsheftel
###############################################################################

library(QFSTO)
testSystemID <- 179030
testdata <- squish(system.file("testdata", package="QFSTO"),'/RunGroups/')

stoSetup <- function(){
	sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory=testdata, stoID="NewID")
	sto$strategyClass("Test")
	sto$systemQClassName("TestQClass.c")
	sto$addPortfolios(filename=squish(testdata,'TestPortfolios.csv'))
	sto$addParameters(name='Param1',start=1,end=3,step=1)
	sto$addParameters(name='Param2',start=2,end=8,step=2)
	return(sto)
}

test.constructor <- function(){
	checkInherits(RunGroupsReport(RunGroups(stoSetup())),"RunGroupsReport")
	shouldBomb(RunGroups())
}

test.individualReports <- function(){
	rg <- RunGroups(systemID=testSystemID)
	rg$addGroup('TestGroup',c('EquityFutures','BondFutures'),c(1,5))
	this <- RunGroupsReport(rg)
	
	#Set up internal variables, should not be done for real
	this$.setupAllPortfolios('TestGroup')
	
	#Portfolios level report
	this$plotAllPortfoliosMutlipleRuns()
	this$plotSumAllPortfoliosRuns()
	this$plotAllPortfoliosEachRun()
	this$plotAllPortfoliosCorrelationMsivs()
	this$plotAllPortfoliosTradePath()
	
	#Individual portfolio reports
	this$.setupSinglePortfolio('EnergyFutures')
	this$selectedRunTable()
	this$plotPortfolioMultipleRuns()
	this$plotMarketsAcrossRuns()
	this$plotMarketsEachRun()
	this$plotEachMarketMutlipleRuns()
	this$plotRunCorrelations()
	this$plotMarketCorrelations()
	this$plotPortfolioTradePath()
}

test.runAll <- function(){
	#rg <- RunGroups(systemID=testSystemID)
	#rg$addGroup('TestGroup1',c('EquityFutures','BondFutures'),c(1,5))
	#rg$addGroup('TestGroup2',c('EnergyFutures'),c(2,6))
	#RunGroupsReport(rg)$generateReports()
}
