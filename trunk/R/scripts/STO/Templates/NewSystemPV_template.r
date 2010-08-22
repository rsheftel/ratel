
#Example of using the classes to a new System-PV

library(QFSTO)
library(SystemDB)

#First construct the System class with the system name:

	sys <- System('TestSystem')
	
#Check for existing pv Names

	sys$pvNames()
	
#See what the parameters are in the db:

	sys$parameters()
	
#Now add a new PV

	parameters <- list(ParameterA='1', ParameterB='2')
	
	sys$addPV(	pvName = 		'NewPV', 
				parameterList=	parameters, 
				asOfDate = 		'21001231', 
				interval = 		'daily', 
				version = 		'1.0', 
				bloombergTag=	'QF.NewPVSys', 
				commitToDB=FALSE)				#set this to TRUE to actually commit to DB 

#Get the systemID for this new PV

	SystemID <- SystemDB$systemID(system='TestSystem', interval='daily', version='1.0', pvName='NewPV')
	
#Once the new PV is created in the DB, we need to add the markets to the PV and the portfolioGroup:

	sysPv <- SystemPV(SystemID)
	
	sysPv$commitToDB(FALSE)
	sysPv$addMarkets(c('TEST1','TEST2'), portfolioGroup='TestSubGroup99')
	
#Now make sure to manually check that the portfolioGroup is in AllSystemsQ

#  STOP here and optimize the portfolio, and update the optimal sizing parameter...
	
#Now turn the markets ON:

	markets <- sysPv$markets()
	sysPv$turnMarketsOn(markets=markets, startDate='21000101', portfolioGroup='TestSubGroup3')

#At this point the System-PV-Markets are live, and in the nightly portfolio run.

#Now add the required information to the PerformanceDB, SystemDB items are auto-generated for Hypo Pnl

	pnl <- PnlSetup()
	pnl$bloombergTag('QF.NewPVSys')
	pnl$reportGroup('report_daily_new')
	pnl$commitToDB(FALSE)  ##Make true for real
	pnl$insertPnlSetup()
	