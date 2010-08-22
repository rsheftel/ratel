# STOResults class test
# 
# Author: rsheftel
###############################################################################

library(QFSTO)
systemID <- 179030
testdata <- squish(system.file("testdata", package="QFSTO"),'/STOResults/')

test.constructor <- function(){
	checkInherits(STOResults(systemID),"STOResults")
	shouldBomb(STOResults())
	sr <- STOResults(systemID)
	checkInherits(sr$stoSetupObject(),"STOSetup")
}

test.updateSystemDB <- function(){
	sr <- STOResults(systemID)
	shouldBombMatching(sr$uploadToSystemDB(commitToDB=FALSE),'No run date set, cannot upload!')
	sr$runDate('20090405')
	
	shouldBombMatching(sr$acceptedPortfolios('BadPort'),"Portfolio names not valid.")
	sr$acceptedPortfolios(c('BondFutures','EquityFutures'))
	checkSame(sr$acceptedPortfolios(),c('BondFutures','EquityFutures'))
	checkSame(sr$runDate()[['in']],'20090405')
	
	testfiles <- sr$uploadToSystemDB(commitToDB=FALSE)
	fileMatches(testfiles[['in']]$bad, squish(testdata,'uploadBothPorts_bad.csv'))
	fileMatches(testfiles[['in']]$good, squish(testdata,'uploadBothPorts_good.csv'))
	
	sr$acceptedPortfolios('EquityFutures')
	checkSame(sr$acceptedPortfolios(),'EquityFutures')
	testfiles <- sr$uploadToSystemDB(commitToDB=FALSE)
	fileMatches(testfiles[['in']]$bad, squish(testdata,'uploadOnePort_bad.csv'))
	fileMatches(testfiles[['in']]$good, squish(testdata,'uploadOnePort_good.csv'))
	fileMatches(testfiles$msivStoIDs, squish(testdata,'uploadMISVStoIDs.csv'))
}

test.updateWithInOutSample <- function(){
	sr <- STOResults(systemID.in=systemID, systemID.out=179030)
	sr$acceptedPortfolios(c('BondFutures','EquityFutures'))
	checkSame(sr$acceptedMarkets()$out$good, c('FV.1C','TU.1C','TY.1C','US.1C','SP.1C'))
	sr$runDate('20090405','21001212')
	checkSame('21001212',sr$runDate()$out)
	testfiles <- sr$uploadToSystemDB(commitToDB=FALSE)
	fileMatches(testfiles$msivStoIDs, squish(testdata,'uploadMISVStoIDsWithOutSample.csv'))
}

