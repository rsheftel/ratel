#Required Libraries
library(QFSTO)
library(SystemDB)
library(QFReports)

	
	

	###################    IN SAMPLE       #######################
	systemID.in <- 135766	
	
	#################################### Create New STO ###########################################
	
	library(SystemDB)
	library(QFReports)
	
#	Create the constructor
	sto <- NewSTO(system="NBarFade", interval="daily", version="1.0", stoDirectory="V:/Market Systems/General Market Systems/NBarFade", stoID="NBF_01_in")
	
#	Load the portfolio, markets and dates. Set a default dates if nothing in the file
	portFile = 'u:/market systems/general market systems/Portfolios/Futures_in.csv'
	sto$addPortfolios(filename=portFile)
	sto$startEndDates(start='19910101',end='20011231',filename=portFile)
	
	portFile = 'u:/market systems/general market systems/Portfolios/SectorETF_in.csv'
	sto$addPortfolios(filename=portFile)
	sto$startEndDates(start='19910101',end='20011231',filename=portFile)
	
	
#	Add the parameters (could also add one at time)
	sto$addParameters(name='LeadBars',start=30,end=30,step=1)
	sto$addParameters(name='ATRLen',start=10,end=50,step=5)
	sto$addParameters(name='nDays',start=5,end=50,step=5)
	sto$addParameters(name='nATRentry',start=0.5,end=2.5,step=0.25)
	sto$addParameters(name='exitATRmultiple',start=0.5,end=2.5,step=0.25)
	sto$addParameters(name='stopAfStep',start=0.02,end=0.05,step=0.01)
	sto$addParameters(name='stopAfMax',start=0.2,end=0.2,step=1)
	sto$addParameters(name='entryBarWindow',start=1,end=2,step=1)
	sto$addParameters(name='closeBetter',start=0,end=1,step=1)
	sto$addParameters(name='riskDollars',start=1000000,end=1000000,step=1)
	HWriterUtils$formatNumbers(sto$parameters())
	
#	Add the systemDB specific stuff
	sto$strategyClass("Trend")
	sto$systemQClassName("Q.Systems.NBarFade")
	
#	Before commiting, look at the report
	NewSTOReport(sto)$openReport(reportAll=TRUE)
	
#	Now create and commit the STO files and SystemDB
	sto$createSTOfiles()
	sto$uploadToSystemDB(commitToDB=FALSE)	#Set commitToDB to TRUE, left false here so no mistakes
	
#	And get back the systemID to do the sto...
	sto$systemID()
	
	#################################### Run Groups ###########################################
	rg <- RunGroups(systemID=systemID.in)
	
	rg$stoSetupObject()$parameters()
	
	#RunGroup = nATRentry_1.25

	rg$addFilter('ATRLen','>=','30')
	rg$addFilter('ATRLen','<=','35')
	rg$addFilter('nDays', '==', '10')
	rg$addFilter('nATRentry','==','1.25')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.04')
			
	rg$filters()
	length(rg$filtersRuns())
	
		rg$addGroup('nATRentry_1.25','EquityFutures')
	
	rg$clearFilters()
	
	#RunGroup = nATRentry_1.5
	
	rg$addFilter('ATRLen','>=','30')
	rg$addFilter('ATRLen','<=','35')
	rg$addFilter('nDays', '==', '10')
	rg$addFilter('nATRentry','==','1.5')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.04')
	
	rg$filters()
	length(rg$filtersRuns())
	
	rg$addGroup('nATRentry_1.5','EquityFutures')

	rg$clearFilters()
	
	#RunGroup = nATRentry_1.75
	
	rg$addFilter('ATRLen','>=','30')
	rg$addFilter('ATRLen','<=','35')
	rg$addFilter('nDays', '==', '10')
	rg$addFilter('nATRentry','==','1.75')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.04')
	
	rg$filters()
	length(rg$filtersRuns())
	
	rg$addGroup('nATRentry_1.75','EquityFutures')
	
	
	rg$clearFilters()
	
	#RunGroup = 1.25_10nDays
	
	#rg$addFilter('ATRLen','>=','35')
	rg$addFilter('nDays', '>=', '5')
	rg$addFilter('nDays', '==', '10')
	rg$addFilter('nATRentry','==','1.25')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.03')
	rg$addFilter('closeBetter','==','0')
	
	rg$filters()
	length(rg$filtersRuns())
	
	rg$addGroup('Test_1.25_10nDays','EquityFutures')

	rg$clearFilters()
	
	#RunGroup = 1.25_15nDays
	
	#rg$addFilter('ATRLen','>=','35')
	rg$addFilter('nDays', '>=', '5')
	rg$addFilter('nDays', '==', '15')
	rg$addFilter('nATRentry','==','1.25')
	rg$addFilter('exitATRmultiple','==','2.5')
	rg$addFilter('entryBarWindow','==','1')
	rg$addFilter('stopAfStep','>=','0.03')
	rg$addFilter('closeBetter','==','0')
	
	rg$filters()
	length(rg$filtersRuns())
	
	rg$addGroup('Test_1.25_15nDays','EquityFutures')
	
	rg$clearFilters()
	
	#RunGroup = 1.25_15nDays
	
	rg$addFilter('ATRLen','>=','30')
	rg$addFilter('nDays', '==', '5')
	#rg$addFilter('nDays', '==', '15')
	rg$addFilter('nATRentry','==','0.5')
	#rg$addFilter('nATRentry','<=','1.5')
	rg$addFilter('exitATRmultiple','==','0.5')
	#rg$addFilter('entryBarWindow','==','1')
	#rg$addFilter('stopAfStep','>=','0.03')
	rg$addFilter('closeBetter','==','0')
	
	rg$filters()
	length(rg$filtersRuns())
	
	rg$addGroup('FastTrade_0.5','EquityFutures')
		
			
	rg$saveToFile()
	
	
	
	
	#### Run the run Groups reports
	
	rg <- RunGroups(systemID=systemID.in)
	rg$loadFromFile()
	rg$groupNames()
				rgr <- RunGroupsReport(rg)
				rgr$generateReports()
				
		###################    OUT SAMPLE       #######################
		systemID.out <- 185802
		
		#################################### Create New STO ###########################################
		
		library(SystemDB)
		library(QFReports)
		
#	Create the constructor
		sto <- STOSetup(system="NBarFade", interval="daily", version="1.0", stoDirectory="V:/Market Systems/General Market Systems/NBarFade", stoID="NBF_01_out")
		
#	Load the portfolio, markets and dates. Set a default dates if nothing in the file
		portFile.out <- 'h:/Equity_out.csv'
		sto$addPortfolios(filename=portFile.out)
		sto$startEndDates(start='20020101',end='20090331',filename=portFile.out)
			
#	Add the parameters (could also add one at time)
		sto$addParameters(name='LeadBars',start=30,end=30,step=1)
		sto$addParameters(name='ATRLen',start=10,end=50,step=5)
		sto$addParameters(name='nDays',start=5,end=50,step=5)
		sto$addParameters(name='nATRentry',start=0.5,end=2.5,step=0.25)
		sto$addParameters(name='exitATRmultiple',start=0.5,end=2.5,step=0.25)
		sto$addParameters(name='stopAfStep',start=0.02,end=0.05,step=0.01)
		sto$addParameters(name='stopAfMax',start=0.2,end=0.2,step=1)
		sto$addParameters(name='entryBarWindow',start=1,end=2,step=1)
		sto$addParameters(name='closeBetter',start=0,end=1,step=1)
		sto$addParameters(name='riskDollars',start=1000000,end=1000000,step=1)
		HWriterUtils$formatNumbers(sto$parameters())
		
#	Add the systemDB specific stuff
		sto$strategyClass("Trend")
		sto$systemQClassName("Q.Systems.NBarFade")
		
#	Before commiting, look at the report
		STOSetupReport(sto)$openReport(reportAll=TRUE)
		
#	Now create and commit the STO files and SystemDB
		sto$createSTOfiles()
		sto$uploadToSystemDB(commitToDB=FALSE)	#Set commitToDB to TRUE, left false here so no mistakes
		
#	And get back the systemID to do the sto...
		sto$systemID()
		
		
		
		RUN THE STO!!!!	
			
	## Run the STOAnalysis
	
	sa <- STOAnalysis(systemID.out)
	sa$metricList(list(QAnnualizedNetProfit,QCalmarRatio,QKRatio))
	sa$runAll()
	
	rg <- RunGroups(systemID=systemID.out)
	rg$loadFromFile()
	rg$groupNames()

	for (x in rg$groupNames()){
		rg$groupPortfolios(x,'NewEquityFutures',replace=FALSE)
	}
	
		rgr <- RunGroupsReport(rg)
		rgr$generateReports(c("FastTrade_1.0" ,  "FastTrade_1.25" ,'FastTrade_1.5'))
		
		
		
		rg$addFilter('ATRLen','>=','30')
		rg$addFilter('ATRLen','<=','35')
		rg$addFilter('nDays', '==', '10')
		rg$addFilter('nATRentry','>=','1.0')
		rg$addFilter('nATRentry','<=','1.25')
		rg$addFilter('exitATRmultiple','==','2.5')
		rg$addFilter('entryBarWindow','==','1')
		rg$addFilter('stopAfStep','>=','0.04')
		rg$addFilter('closeBetter','==','0')
		
		rg$filters()
		length(rg$filtersRuns())
		
		rg$addGroup('FinalSlow','EquityFutures')
		