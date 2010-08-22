library(SystemDB)

test.alreadyExists <- function(){
	checkSame(TRUE,SystemDB$alreadyExists('Class','Name','Test'))
	checkSame(FALSE,SystemDB$alreadyExists('Class','Name','Test Not In DB'))
}


testMarketsBySystemPV <- function() {
    markets <- SystemDB$marketsBySystemPV("TestSystem1", "daily", "1.0", "Fast")
    checkSame(c("TEST.SP.1C", "TEST.US.1C"), markets)
}

testBigPointValue <- function() {
    checkSame(50, SystemDB$bigPointValue("ES.1C"))
}

testSlippage <- function() {
	checkSame(0.015625, SystemDB$fixedSlippage("RE.TEST.TY.1C"))
}

test.histData <- function() {
	checkSame('ASCII',SystemDB$histDaily('RE.TEST.TY.1C'))
}

test.asciiFilename <- function(){
	if (isWindows()) checkSame(SystemDB$asciiFilename('RE.TEST.TY.1C'),'\\\\nyux51\\data\\TestData\\TY1C.full.csv')
	else checkSame(SystemDB$asciiFilename('RE.TEST.TY.1C'),'/data/TestData/TY1C.full.csv')
}

test.rebalanceFunction <- function(){
	checkSame('RebalanceMe',SystemDB$rebalanceFunction('RE.TEST.TY.1C'))
}

testBloombergRootFromMarket <- function(){
	checkSame(SystemDB$getBloombergRoot('S.1C'),'S ')
	checkSame(SystemDB$getBloombergRoot('doesnotexist'),character(0))
}

test.getContinuousContracts <- function(){
	checkResult <- function(strVector){
		assert(class(strVector) == 'character')
		assert(length(strVector) > 0)
	}	
	res <- SystemDB$getContinuousContracts()
	checkResult(res)	
}

test.getContinuousBloombergSecurity <- function(){
	checkSame(NROW(SystemDB$getContinuousBloombergSecurity('Does not exist')),0)
	checkSame(SystemDB$getContinuousBloombergSecurity('ES.GFUT.1C'),'ES1 COMB Index')
}

test.cleanAndDeleteMarketTicker <- function(){
	checkSame(NULL,unlist(SystemDB$deleteMarketTicker('TEST.US.1C')))	
	checkSame(NROW(SystemDB$getFrontBloombergTicker('TEST.US.1C')),0)
	checkSame(NROW(SystemDB$getFrontTSDBTicker('TEST.US.1C')),0)
	checkSame(NROW(SystemDB$getYellowKey('TEST.US.1C')),0)
	checkSame(NROW(SystemDB$getLastMarketUpdate('TEST.US.1C')),0)
	checkSame(NULL,unlist(SystemDB$insertMarketTicker('TEST.US.1C','USH9','Comdty','us200903','2009-02-23 15:00:00','us.root','us.ric')))
	checkSame(SystemDB$getFrontBloombergTicker('TEST.US.1C'),'USH9')
	checkSame(SystemDB$getFrontTSDBTicker('TEST.US.1C'),'us200903')
	checkSame(SystemDB$getYellowKey('TEST.US.1C'),'Comdty')
	checkSame(SystemDB$getRICRoot('TEST.US.1C'),'us.ric')
	checkSame(SystemDB$getBloombergRoot('TEST.US.1C'),'us.root')
	checkSame(SystemDB$getLastMarketUpdate('TEST.US.1C'),'2009-02-23 15:00:00.0')	
	checkSame(NULL,unlist(SystemDB$deleteMarketTicker('TEST.US.1C')))
	checkSame(NROW(SystemDB$getFrontBloombergTicker('TEST.US.1C')),0)
	checkSame(NROW(SystemDB$getFrontTSDBTicker('TEST.US.1C')),0)
	checkSame(NROW(SystemDB$getLastMarketUpdate('TEST.US.1C')),0)
	checkSame(NROW(SystemDB$getYellowKey('TEST.US.1C')),0)
}

test.systemID <- function(){
	checkSame(5191, SystemDB$systemID(system='TestSystem1',interval='daily',version='1.0'))
	checkSame(102206, SystemDB$systemID(system='TestSystem1',interval='daily',version='1.0',pvName="Fast"))
	checkSame(128579, SystemDB$systemID(system='TestSystem1',interval='daily',version='1.0',stoDir="v:/Temp/StoDir",stoID='StoID001'))
}

test.strategyClass <- function(){
	checkSame("Test", SystemDB$strategyClass(strategy='TestSystem5'))
}

test.systemQClass <- function(){
	checkSame("TestQClass.c", SystemDB$systemQClassName(system="TestSystem5"))
}

test.MSIVBacktestMarkets <- function(){
	checkSame(c('TEST1','TEST2','TEST3','TEST4'), SystemDB$msivBacktestMarkets(stoID="NewID"))
}

test.MSIVBacktest <- function(){
	expected <- data.frame(	market		=c('TEST1','TEST2','TEST3','TEST4'),
							startDate	=c('2000-01-01 00:00:00.0','2000-01-01 00:00:00.0','1999-06-30 00:00:00.0','1999-06-30 00:00:00.0'),
							endDate		=c('2009-01-30 00:00:00.0','2005-12-31 00:00:00.0','2009-01-30 00:00:00.0','2009-01-30 00:00:00.0'))
	checkSame(expected, SystemDB$msivBacktest(stoID="NewID"))
}

test.PortfolioBacktest <- function(){
	checkSame(list(Port1=c('TEST1','TEST2'), Port2=c('TEST3','TEST4')), SystemDB$backtestPortfolios(stoID="NewID"))
}

test.systemDetails <- function(){
	expected <- list(	system='TestSystem5',
						version='1.0',
						interval='daily',
						pvName="NA",
						stoDirectory='StoDir',
						stoID='NewID')
	checkSame(expected, SystemDB$systemDetails(150163))
	
	if(isWindows()){checkSame('V:/TestData/TestSTO',SystemDB$systemDetails(179030, makeFilenameNatve=TRUE)$stoDirectory)}
	else{ 			
					checkSame('/data/TestData/TestSTO',SystemDB$systemDetails(179030, makeFilenameNative=TRUE)$stoDirectory)
					checkSame('V:/TestData/TestSTO',SystemDB$systemDetails(179030)$stoDirectory)
	}			
}

testTradingRangeForLiveMSIV <- function() {
	# End date is NULL
	range <- Range('2007-12-01',as.POSIXct(as.character(Sys.Date())))
	checkSame(range,SystemDB$tradingRangeForLiveMSIVPV('TEST.SP.1C_TestSystem1_daily_1.0','Slow'))
	# End date is not NULL
	range <- Range('2007-12-01',as.POSIXct('2029-12-01'))
	checkSame(range,SystemDB$tradingRangeForLiveMSIVPV('TEST.US.1C_TestSystem1_daily_1.0','Fast'))
	# MSIV/PV does not exist
	checkSame(NULL,SystemDB$tradingRangeForLiveMSIVPV('TEST.US.1C_TestSystem1_dail_1.0','Fast'))
	checkSame(NULL,SystemDB$tradingRangeForLiveMSIVPV('TEST.US.1C_TestSystem1_daily_1.0','Fat'))
	# Wrong input
	shouldBomb(SystemDB$tradingRangeForLiveMSIVPV(TRUE,'Fat'))
	shouldBomb(SystemDB$tradingRangeForLiveMSIVPV('TEST.US.1C_TestSystem1_daily_1.0',TRUE))
}

testMemberGroupNamesForGroup <- function() {
	res <- SystemDB()$memberGroupNamesForGroup('TestAllSystemsJB')
	checkSame(res,c("BuySellAndHold","CouponSwapFNCI","CouponSwapFNCL","FXCarry","FadeMonthEndPush","FaderClose","LiqInj","NDayBreak","NDayBreakCloseMinVol"))
	checkSame(SystemDB()$memberGroupNamesForGroup('Junk'),NULL)
	shouldBomb(SystemDB()$memberGroupNamesForGroup(TRUE))
}

test.memberMSIVPVsForGroup <- function(){
	checkSame(SystemDB$memberGroupMSIVPVsForGroup('TestSubGroup1','TestPV1'), c('TEST4_TestSystem1_daily_1.0','TEST5_TestSystem1_daily_1.0'))
}

test.pvNames <- function(){
	checkSame(SystemDB$pvNamesForSystem('TestSystem1'), c('Fast','Slow','TestPV1','TestPV2'))
}

test.parameters <- function(){
	checkSame(SystemDB$parameterNames('TestSystem1'),c('DaysBack','LeadBars','TestParameter1','TestParameter2','TestParameter3'))
}

test.msivsForPV <- function(){
	checkSame(SystemDB$msivsForPV('TestPV1'), paste(c('TEST1','TEST4','TEST5'),'TestSystem1','daily','1.0',sep='_'))
}

test.bloombergTag <- function(){
	checkSame(SystemDB$bloombergTag(102206), 'QF.Example')
	checkSame(SystemDB$bloombergTag(5191), NULL)
}

test.liveHistoryStartEndDates <- function(){
	expected <- data.frame(Start_trading=c('2007-12-01 00:00:00.0', '2007-12-29 00:00:00.0'), End_trading=c('2007-12-07 00:00:00.0', NA))
	checkSame(SystemDB$liveHistoryStartEndDates('TEST.SP.1C_TestSystem1_daily_1.0', 'Fast'), expected)
}

test.marketHistory <- function(){
	expected <- data.frame(StartDate=NA, EndDate='2003-03-31 00:00:00.0')
	checkSameLooking(SystemDB$marketHistory('SP.1C'), expected)
	
	expected <- data.frame(StartDate=c('2003-06-01 00:00:00.0','2008-12-01 00:00:00.0'), EndDate=c('2006-12-31 00:00:00.0', NA))
	checkSame(SystemDB$marketHistory('F5045TRS'),expected)
}

test.parameterValues <- function(){
	checkSame(SystemDB$parameterValues('TestSystem1', 'Slow', 'TestParameter1'), 
							data.frame(ParameterValue=c("1",'15'), AsOfDate=c('2007-01-02 00:00:00.0', '2007-10-01 00:00:00.0')))
	checkSame(SystemDB$lastParameterValue('TestSystem1', 'Slow', 'TestParameter1'), 15)
	shouldBomb(SystemDB$lastParameterValue('TestSystem1', 'Slow', 'TestParameter10'))
	checkSame(20, SystemDB$lastParameterValue("NDayBreak", "BFBD20", "BreakDays"))
}

test.sizingParameter <- function(){
	checkSame(SystemDB$sizingParameter('TestSystem1'), 'TestParameter1')
	shouldBombMatching(SystemDB$sizingParameter('BadSystem'), 'expected single result but got 0')
}

test.sectors <- function(){
	expected <- data.frame(	Name=c('RE.TEST.TU.1C','TEST1','TEST2'),
							SubSector=c('Bond Future','TestSubSector','TestSubSector'),
							Sector=c('Interest Rate','Volatility','Volatility'))
	checkSame(SystemDB$sectors(markets=c('TEST1','TEST2','RE.TEST.TU.1C')), expected)
	checkSame(as.character(SystemDB$sectors('FNCL.1.5.1.0.TEST')$Sector),'Mortgage')
}
