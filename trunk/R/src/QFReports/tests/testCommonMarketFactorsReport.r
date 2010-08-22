# test for CommonMarketFactorsReport Class
# 
# Author: RSheftel
###############################################################################

library(QFReports)
library(QFPortfolio)

filenamePrefix <- function() squish('testCMFreport_',round(abs(rnorm(1))*1000,4))
testCurvesDir <- squish(system.file("testdata", package="STO"),'/TestCurves/')
testDir <- squish(system.file("testdata", package="QFReports"),'/CommonMarketFactorsReport/')

test.constructor <- function(){
	noNames <- read.zoo(squish(testCurvesDir,"RandomCurvesSamePnL.csv"), sep=",", header=TRUE)[,"B"]
	prefix <- filenamePrefix()	
	shouldBomb(CommonMarketFactorsReport())
	shouldBomb(CommonMarketFactorsReport(outputDirectory=tempDirectory(), filenamePrefix=prefix, analysisZoo= noNames))
	
	singleZoo <- getZooDataFrame(noNames,"TestZoo")
	report <- CommonMarketFactorsReport(outputDirectory=tempDirectory(), filenamePrefix=prefix, analysisZoo= singleZoo)
	checkInherits(report,"CommonMarketFactorsReport")
	checkSame(squish(tempDirectory(),prefix,'.html'), report$filename())
	report$closeConnection()
}

test.badCMFReport <- function(){
	fprefix <- filenamePrefix()
	multiZoo <- read.zoo(squish(testCurvesDir,"RandomCurvesSamePnL.csv"), sep=",", header=TRUE)[,1:3]
	badrept <- CommonMarketFactorsReport(outputDirectory=tempDirectory(), filenamePrefix=fprefix, analysisZoo= multiZoo)
	shouldBomb(badrept$generateReport())
	badrept$closeConnection()
}

test.generateCMFReport <- function(){
	multiZoo <- read.zoo(squish(testCurvesDir,"RandomCurvesSamePnL.csv"), sep=",", header=TRUE)[,1:3]	
	fprefix <- "testCMFreportTest"
	rept <- CommonMarketFactorsReport(outputDirectory=tempDirectory(), filenamePrefix=fprefix, analysisZoo= multiZoo)
	cmf <- rept$commonMarketFactorsObject()
	
	fileDir <- squish(system.file("testdata", package="QFPortfolio"),'/CommonMarketFactors/')
	standard.names <- c('10ySwapRate','CDX.IG5Y','5ySwapSpread','3m5yBpVolDaily','2y10yBpVolDaily')
	standard.tickers <- c('irs_usd_rate_10y_mid','cdx-na-ig_market_spread_5y_otr','irs_usd_spread_5y_1n','swaption_usd_3m5y_atm_payer_vol_bp_daily_mid','swaption_usd_2y10y_atm_payer_vol_bp_daily_mid')
	standard.sources <- c('internal','internal','internal','jpmorgan','jpmorgan')
	standard.containers <- rep(squish(fileDir,'standardFactors.csv'),length(standard.names))
	cmf$addFactors(standard.names, standard.tickers, standard.sources, standard.containers, standard.containers)	
	cmf$loadFactors()
	
	rept$generateReport()
	rept$closeConnection()
	#rept$openReport()
	hWriterFileMatches(squish(tempDirectory(),fprefix,".html"), squish(testDir,"testCMFreport.html"),deleteFile=FALSE,startLine=7,endLineOffset=31)	
	hWriterFileMatches(squish(tempDirectory(),fprefix,".html"), squish(testDir,"testCMFreport.html"),deleteFile=FALSE,startLine=21,endLineOffset=17)
	hWriterFileMatches(squish(tempDirectory(),fprefix,".html"), squish(testDir,"testCMFreport.html"),deleteFile=FALSE,startLine=35,endLineOffset=2)
}
