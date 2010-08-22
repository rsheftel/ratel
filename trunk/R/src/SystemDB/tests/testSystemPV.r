# SystemPV tests
# 
# Author: RSheftel
###############################################################################

library(SystemDB)

testdata <- squish(system.file("testdata", package="SystemDB"),'/SystemPV/')
testPVid <- 102206

test.constructor <- function(){
	checkInherits(SystemPV(systemID=testPVid), 'SystemPV')
	
	shouldBomb(SystemPV())
	shouldBombMatching(SystemPV(systemID=0),'SystemID does not exist: 0')
	shouldBombMatching(SystemPV(systemID=150163), 'SystemID does not have a PVName: 150163')
}

test.addMarkets <- function(){
	pv <- SystemPV(testPVid)
	pv$commitToDB(FALSE)
	
	shouldBombMatching(pv$addMarkets('BadMarket'), 'Not valid market: BadMarket')
	testres <- pv$addMarkets(c('TEST1','TEST4'))
	fileMatches(testres$MSIVParamValues, squish(testdata,'test.addMarkets_1.csv'))
	
	testres <- pv$addMarkets(c('TEST1'), portfolioGroup='TestSubGroup1')
	fileMatches(testres$MSIVParamValues, squish(testdata,'test.addMarkets_2.csv'))
	fileMatches(testres$groupMemberMSIVPVs, squish(testdata,'test.addMarketsToPortfolioGroup.csv'))
}

test.getters <- function(){
	pv <- SystemPV(testPVid)
	pv$commitToDB(FALSE)
	checkSame(pv$commitToDB(), FALSE)
	checkSame(pv$markets(), c('TEST.SP.1C','TEST.US.1C'))
}

test.turnMarketsOn <- function(){
	pv <- SystemPV(259863)
	pv$commitToDB(FALSE)
	shouldBombMatching(pv$turnMarketsOn('TEST1', startDate='21000101', portfolioGroup='TestSubGroup1'),'Must have bloomberg tag to make live.')
	
	pv <- SystemPV(102206)
	pv$commitToDB(FALSE)
	shouldBombMatching(pv$turnMarketsOn('TEST1', startDate='21000101', portfolioGroup='TestSubGroup1'),
										'Cannot make an MSIV live until it is the portfolioGroup: TEST1_TestSystem1_daily_1.0')
	
	pv <- SystemPV(102206)
	pv$commitToDB(FALSE)
	testfile <- pv$turnMarketsOn(markets='TEST.US.1C', startDate='21000101', portfolioGroup='TestSubGroup3')
	fileMatches(testfile, squish(testdata,'test.turnMarketsOn.csv'))										
}
