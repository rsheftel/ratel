# System tests
# 
# Author: RSheftel
###############################################################################

library(SystemDB)

testdata <- squish(system.file("testdata", package="SystemDB"),'/System/')

test.constructor <- function(){
	checkInherits(System('TestSystem1'), "System")
	shouldBomb(System())
	shouldBombMatching(System('Bad System'), 'System does not exist: Bad System')
}

test.pvNames <- function(){
	sys <- System('TestSystem1')
	checkSame(sys$pvNames(), c('Fast','Slow','TestPV1','TestPV2'))
}

test.parameters <- function(){
	sys <- System('TestSystem1')
	checkSame(sys$parameters(),c('DaysBack','LeadBars','TestParameter1','TestParameter2','TestParameter3'))	
}

test.addPV <- function(){
	sys <- System('TestSystem1')
	
	shouldBombMatching(sys$addPV(pvName='TestPV1', asOfDate='20090101', interval='daily', version='1.0'),"PVName already exists: TestPV1")
	shouldBombMatching(sys$addPV(	pvName='NewNewPV', parameterList=list(TestParameter1='1', BadParameter='99'), 
									asOfDate='19000101', interval='daily', version='1.0'),	"Invalid parameter name: BadParameter")
	
	sys$addPV(pvName='NewSystemClassPV', parameterList=	list(TestParameter1='1', TestParameter2='99'),
							asOfDate='19000101', interval='daily', version='1.0', commitToDB=FALSE )
	fileMatches(sys$.testfile, squish(testdata,'test.addPV.csv'))

	#The below test will not work because it needs to actually insert into the DB, but shows the bloombergTag input:
	#sys$addPV(pvName='NewSystemClassPV', parameterList=	list(TestParameter1=1, TestParameter2=99),
	#			asOfDate='19000101', interval='daily', version='1.0', bloombergTag='QF.NewTag', commitToDB=FALSE )
}
