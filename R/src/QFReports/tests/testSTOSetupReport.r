# Test STOSetupReport Class
# 
# Author: RSheftel
###############################################################################

library(QFReports)
library(QFSTO)
testdata <- squish(system.file("testdata", package="QFReports"),'/STOSetupReport/')

makeSTOSetup <- function(){
	stoData <- squish(system.file("testdata", package="QFSTO"),'/STOSetup/')
	sto <- STOSetup(system='NewSystem', interval='daily', version='1.0', stoDir='h:/stoDir', stoID='stoID')
	sto$addPortfolios(filename=squish(stoData,'TestPortfolios.csv'))
	sto$startEndDates(start='19001010',end='20101231', filename=squish(stoData,'TestPortfolios.csv'))
	sto$addParameters(c('Param1','Param2'), start=c(1,10), end=c(10,100), step=c(1,10))
	sto$systemQClassName("TestQClass.c")
	sto$strategyClass("Carry")
	return(sto)
}

test.constructor <- function(){
	shouldBomb(STOSetupReport())
	report <- STOSetupReport(makeSTOSetup())
	checkInherits(report, "STOSetupReport")
	
	report <- STOSetupReport(makeSTOSetup(),squish(testdata,'newreport'))
	checkSame(squish(testdata,'newreport.html'),report$filename())
}

test.reportAll <- function(){
	report <- STOSetupReport(makeSTOSetup())
	report$reportAll()
	report$closeConnection()
	hWriterFileMatches(squish(report$filename()), squish(testdata,'ReportAll.html'),deleteFile=FALSE,startLine=6)
}

test.oneLine <- function(){
	#STOSetupReport(makeSTOSetup())$openReport(reportAll=TRUE)
}

test.settings <- function(){
	report <- STOSetupReport(makeSTOSetup())
	report$addSettings()
	report$closeConnection()
	hWriterFileMatches(squish(report$filename()), squish(testdata,'Settings.html'),deleteFile=FALSE,startLine=6)
}

test.markets <- function(){
	report <- STOSetupReport(makeSTOSetup())
	report$addMarkets()
	report$closeConnection()
	hWriterFileMatches(squish(report$filename()), squish(testdata,'Markets.html'),deleteFile=FALSE,startLine=6)
}

test.portfolios <- function(){
	report <- STOSetupReport(makeSTOSetup())
	report$addPortfolioList()
	report$closeConnection()
	hWriterFileMatches(squish(report$filename()), squish(testdata,'PortfolioList.html'),deleteFile=FALSE,startLine=6)
	
	report <- STOSetupReport(makeSTOSetup())
	report$addPortfolioGrid()
	report$closeConnection()
	hWriterFileMatches(squish(report$filename()), squish(testdata,'PortfolioGrid.html'),deleteFile=FALSE,startLine=6)
}

test.parameters <- function(){
	report <- STOSetupReport(makeSTOSetup())
	report$addParameters()
	report$closeConnection()
	hWriterFileMatches(squish(report$filename()), squish(testdata,'Parameters.html'),deleteFile=FALSE,startLine=6)
}
