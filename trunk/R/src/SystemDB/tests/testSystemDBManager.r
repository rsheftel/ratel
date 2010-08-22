library(SystemDB)

checkSysDBManagerFile <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="SystemDB"),'/SystemDBManager/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))	
	return(checkSame(benchData,testData))
}

test.constructor <- function(){
	sto <- SystemDBManager()
	checkInherits(sto, "SystemDBManager")
}

test.alreadyExists <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	checkSame(TRUE,sto$alreadyExists('Class','Name','Test'))
	checkSame(FALSE,sto$alreadyExists('Class','Name','Test Not In DB'))
}

test.insertToSystemDB <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	tempFilename <- sto$insertToSystemDB('System','Name','Test1')
	checkSysDBManagerFile(tempFilename,'test.insertToSystemDB.csv')
}

test.updateInSystemDB <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	tempFilename <- sto$updateInSystemDB('System',c('Name','Field2','Field3'),c('Name','Field2'),c('Test1','Test2','Test3'))
	checkSysDBManagerFile(tempFilename,'test.updateInSystemDB.csv')
}

test.insertStrategy <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	testfile <- sto$insertStrategy('NewStrategy','Test','TestDescription','TestOwner',c('TestParam1','TestParam2'), c(1,2))
	checkSysDBManagerFile(testfile,'test.insertStrategy.csv')
	checkSame('Strategy already exists in SystemDB..Strategy table: TestSystem1', sto$insertStrategy('TestSystem1','Test','TestDescription','TestOwner',c('TestParam1','TestParam2'), c(1,2)))
	checkSame('Class does not exist in SystemDB..Class table: Test Class Does not Exist', sto$insertStrategy('Test','Test Class Does not Exist','TestDescription','TestOwner',c('TestParam1','TestParam2'), c(1,2)))
}

test.insertStrategyParameterNames <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	checkSame(FALSE,sto$insertStrategyParameterNames('TestStrategy',c('TestParam1','TestParam2'), 1))
	checkSame('Strategy does not exist in SystemDB..Strategy table: Bad Strategy',sto$insertStrategyParameterNames('Bad Strategy',c('TestParam1','TestParam2'), c(1,2)))
	testfile <- sto$insertStrategyParameterNames('TestSystem1',c('TestParam1','TestParam2'), c(1,2))
	checkSysDBManagerFile(testfile,'test.insertStrategyParameterNames.csv')	
}

test.insertSystemTable <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	testfile <- sto$insertSystemTable('TestSystem','TestDescription','TestDocumentation','TestOwner','TestQClass')
	checkSysDBManagerFile(testfile,'test.insertSystemTable.csv')
	checkSame('System already exists in SystemDB..System table: TestSystem1', sto$insertSystemTable('TestSystem1','TestDescription','TestDocumentation','TestOwner','TestStrategy'))		
}

test.insertSystemStrategiesTable <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	testfile <- sto$insertSystemStrategiesTable('TestSystem1',c('TestSystem1'))
	checkSysDBManagerFile(testfile,'test.insertSystemStrategiesTable.csv')
	checkSame('Strategy does not exist in SystemDB..Strategy table: NoStrategy', sto$insertSystemStrategiesTable('TestSystem1',c('TestSystem1','NoStrategy'),owner='John Doe'))
	checkSame('System does not exist in SystemDB..System table: TestSystem', sto$insertSystemStrategiesTable('TestSystem',c('TestSystem1','NoStrategy'),owner='John Doe'))				
}

test.insertSystemDetailsTable <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	testfile <- sto$insertSystemDetailsTable(	systemName='TestSystem1',
												version='1.0',
												interval='daily',
												stoDir='TestDir',
												stoID='TestID',
												pvName='Fast')
	checkSysDBManagerFile(testfile,'test.insertSystemDetailsTable.csv')
	checkSame('System does not exist in SystemDB..System table: NoSystem', 
		sto$insertSystemDetailsTable('NoSystem',version='1.0',interval='daily'))
	checkSame('Version does not exist in SystemDB..Version table: NoVersion', 
		sto$insertSystemDetailsTable('TestSystem1',version='NoVersion',interval='daily'))
	checkSame('Interval does not exist in SystemDB..Interval table: NoInterval', 
		sto$insertSystemDetailsTable('TestSystem1',version='1.0',interval='NoInterval'))
	checkSame('Record already exists in SystemDB..SystemDetails',
		sto$insertSystemDetailsTable('TestSystem1',version='1.0',interval='daily'))
}

test.insertMSIVTable <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	
	errorMsg <- 'Interval does not exist in SystemDB..Interval table: Interval Does Not Exist'
	checkSame(errorMsg, sto$insertMSIVTable(c('market1','market2'),'TestSystem','Interval Does Not Exist','1.0', ''))
	errorMsg <- 'Version does not exist in SystemDB..Version table: Version Does Not Exist'
	checkSame(errorMsg, sto$insertMSIVTable(c('market1','market2'),'TestSystem','daily','Version Does Not Exist', ''))
	errorMsg <- 'System does not exist in SystemDB..System table: Bad System'
	checkSame(errorMsg, sto$insertMSIVTable(c('market1','market2'),'Bad System','daily','1.0', ''))
	errorMsg <- 'Market does not exist in SystemDB..Markets table: Bad Market'
	checkSame(errorMsg, sto$insertMSIVTable('Bad Market','TestSystem1','daily','1.0', ''))
	
	testfile <- sto$insertMSIVTable(c('TEST2','TEST3'), 'TestSystem1','daily','1.0','')
	checkSysDBManagerFile(testfile,'test.insertMSIVTable.csv')
	
	testfile <- sto$insertMSIVTable(c('TEST1','TEST2','TEST3','TEST4','TEST5'), 'TestSystem1','daily','1.0','')
	checkSysDBManagerFile(testfile,'test.insertMSIVTable.csv')
}

test.uploadMSIVTable <- function(){
	sto <- SystemDBManager()
	sto$commitToDB(FALSE)
	
	errorMsg <- 'MSIV does not exist in SystemDB..MSIV table: TEST2_TestSystem1_daily_1.0'
	shouldBombMatching(sto$updateMSIVTable('TEST2','TestSystem1','daily','1.0',inSampleSTOid='123',outSampleSTOid='456'), errorMsg)
	
	testfile <- sto$updateMSIVTable('TEST1','TestSystem1','daily','1.0',inSampleSTOid='123',outSampleSTOid='456')
	checkSysDBManagerFile(testfile,'test.updateMSIVTable.csv')	
}

test.insertMSIVBacktestTable <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'MSIV does not exist in SystemDB..MSIV table: Bad MSIV'
	checkSame(errorMsg, stodir$insertMSIVBacktestTable('Bad MSIV','20080101','STOID1'))
	testfile <- stodir$insertMSIVBacktestTable('TEST1_TestSystem1_daily_1.0','20080101','STOID1')
	checkSysDBManagerFile(testfile,'test.insertMSIVBackTestTable.csv')
	
	msivs <- paste('TEST',c(1,4,5),'_TestSystem1_daily_1.0',sep="")
	testfile <- stodir$insertMSIVBacktestTable(msivs,stoID="stoID",stoDir="stoDir",
		startDate=c(NA,"12345678","87654321"),endDate=c("12345678",NA,"87654321"))
	checkSysDBManagerFile(testfile,'test.insertMSIVBackTestTableWithNAs.csv')
	
	shouldBombMatching(stodir$insertMSIVBacktestTable(msivs,startDate="BadDate"),"StartDate in bad format, must be character YYYYMMDD")
	shouldBombMatching(stodir$insertMSIVBacktestTable(msivs,endDate="BadDate"),"EndDate in bad format, must be character YYYYMMDD")
}

test.insertPortfolioBacktest <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'MSIV does not exist in SystemDB..MSIV table: Bad MSIV'
	checkSame(errorMsg, stodir$insertPortfolioBacktest('Bad MSIV',1,'stoDir','stoID','PortfolioName'))
	
	msivs <- paste('TEST',c(1,4,5),'_TestSystem1_daily_1.0',sep="")
	testfile <- stodir$insertPortfolioBacktest(msivs,1,'stoDir','stoID','PortfolioName')
	checkSysDBManagerFile(testfile,'test.insertPortfolioBacktest.csv')
	testfile <- stodir$insertPortfolioBacktest(msivs,c(0.5,0.6,0.7),'stoDir','stoID','PortfolioName')
	checkSysDBManagerFile(testfile,'test.insertPortfolioBacktestWithWeights.csv')
	shouldBombMatching(stodir$insertPortfolioBacktest(msivs,c(0.5,0.6),'stoDir','stoID','PortfolioName'),
												"Weights vector must be same length as msivs or length of one.")
}

test.updateMSIVBacktestTableResults <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'MSIV does not exist in SystemDB..MSIV table: BadMarket_BadSystem_daily_1.0'
	checkSame(errorMsg, stodir$updateMSIVBacktestTableResults('BadMarket','BadSystem','daily','1.0','STOID1','20080101'))
	testfile <- stodir$updateMSIVBacktestTableResults('TEST1','TestSystem1','daily','1.0',runDate='20080101',stoID='STOID1',resultFile='resultFile',validationAccept=TRUE)
	checkSysDBManagerFile(testfile,'test.updateMSIVBacktestTableResults.csv')
}

test.insertMSIVParameterValuesTable <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'PVName does not exist in SystemDB..ParameterValues table: BadPV'
	checkSame(errorMsg, stodir$insertMSIVParameterValuesTable('BadMarket','BadSystem','daily','1.0','BadPV'))	

	errorMsg <- 'MSIV does not exist in SystemDB..MSIV table: BadMarket_BadSystem_daily_1.0'
	checkSame(errorMsg, stodir$insertMSIVParameterValuesTable('BadMarket','BadSystem','daily','1.0','TestPV1'))	

	errorMsg <- 'MSIV already exists in PVName: TEST1_TestSystem1_daily_1.0'
	shouldBombMatching(stodir$insertMSIVParameterValuesTable('TEST1','TestSystem1','daily','1.0','TestPV1'), errorMsg)	
		
	testfile <- stodir$insertMSIVParameterValuesTable(c('TEST.SP.1C','TEST.US.1C'),'TestSystem1','daily','1.0','TestPV1')
	checkSysDBManagerFile(testfile,'test.insertMSIVParameterValuesTable.csv')
}

test.insertParameterValues <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'Parameter names not valid for this system.'
	shouldBombMatching(stodir$insertParameterValuesTable('TestSystem1', 'TestPV1', c('TestParameter1', 'BadParameter'), c('1','2'), asOfDate='20090101'), errorMsg)
	
	errorMsg <- 'Length of parameter names and values must be the same.'		
	shouldBombMatching(stodir$insertParameterValuesTable('TestSystem1', 'TestPV1', c('TestParameter1', 'TestParameter2'), c('1'), asOfDate='20090101'), errorMsg)
	shouldBombMatching(stodir$insertParameterValuesTable('TestSystem1', 'TestPV1', c('TestParameter1'), c('1','2'), asOfDate='20090101'), errorMsg)
	
	errorMsg <- 'System name not valid: BadSystem'
	shouldBombMatching(stodir$insertParameterValuesTable('BadSystem', 'TestPV1', 'TestParameter1', '1', asOfDate='20090101'), errorMsg)
	
	testfile <- stodir$insertParameterValuesTable('TestSystem1', 'TestPV1', 
													parameterNames=	c('TestParameter1', 'TestParameter2'), 
													parameterValues=c('1','2'), 
													asOfDate='20090101')
	checkSysDBManagerFile(testfile,'test.insertParameterValuesTable.csv')												
}

test.insertGroupMemberMSIVPVs <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'No group: BadGroup'
	shouldBombMatching(stodir$insertGroupMemberMSIVPVs('BadGroup', markets=c('TEST1','TEST2'), 
							system='TestSystem1', interval='daily', version='1.0', pvName='TestPV1', weight=0.5) ,errorMsg)
					
	errorMsg <- 'MSIV already exists in group: TEST4_TestSystem1_daily_1.0'
	shouldBombMatching(stodir$insertGroupMemberMSIVPVs('TestSubGroup1', markets=c('TEST4','TEST2'), 
							system='TestSystem1', interval='daily', version='1.0', pvName='TestPV1', weight=0.5) ,errorMsg)
	
	errorMsg <- 'MSIV does not exist in SystemDB..MSIV table: BadMSIV_TestSystem1_daily_1.0'
	shouldBombMatching(stodir$insertGroupMemberMSIVPVs('TestSubGroup1', markets=c('BadMSIV','TEST2'), 
							system='TestSystem1', interval='daily', version='1.0', pvName='TestPV1', weight=0.5) ,errorMsg)
		
	testfile <- stodir$insertGroupMemberMSIVPVs('TestSubGroup1', markets=c('TEST1','TEST.SP.1C'), system='TestSystem1', 
													interval='daily', version='1.0', pvName='TestPV1', weight=0.5)
	checkSysDBManagerFile(testfile,'test.insertGroupMemberMSIVPVs.csv')
}

test.insertMSIVLiveHistory <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'Not valid MSIV: TEST2_TestSystem1_daily_1.0'
	shouldBombMatching(stodir$insertMSIVLiveHistory(system='TestSystem1', interval='daily', version='1.0', pvName='Fast', 
													markets=c('TEST2'), startDate='21000101'), errorMsg)
	
	errorMsg <- 'MSIV already in on state: TEST.SP.1C_TestSystem1_daily_1.0'
	shouldBombMatching(stodir$insertMSIVLiveHistory(system='TestSystem1', interval='daily', version='1.0', pvName='Fast', 
													markets=c('TEST.SP.1C'), startDate='21000101'), errorMsg)
	
	testfile <- stodir$insertMSIVLiveHistory(system='TestSystem1', interval='daily', version='1.0', pvName='Fast', 
													markets=c('TEST.US.1C'), startDate='21000101')
												
	checkSysDBManagerFile(testfile,'test.insertMSIVLiveHistory.csv')
}

test.bloombergTag <- function(){
	stodir <- SystemDBManager()
	stodir$commitToDB(FALSE)
	
	errorMsg <- 'SystemID already has a bloomberg tag: 102206'
	shouldBombMatching(stodir$insertBloombergTag(102206, 'QF.Example'), errorMsg)
	
	testfile <- stodir$insertBloombergTag(259863,'QF.Example')
	checkSysDBManagerFile(testfile,'test.insertBloombergTag.csv')
}
