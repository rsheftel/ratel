# Template on how to use the NewSTO Class
###############################################################################

library(QFSTO)
library(QFReports)

#	Create the constructor
	sto <- STOSetup(system="NBarFade", interval="daily", version="1.0", stoDirectory="V:/Market Systems/General Market Systems/NBarFade", stoID="NBF_01_in")

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
	STOSetupReport(sto)$openReport(reportAll=TRUE)

#	Now create and commit the STO files and SystemDB
	sto$createSTOfiles()
	sto$uploadToSystemDB(commitToDB=FALSE)	#Set commitToDB to TRUE, left false here so no mistakes

#	And get back the systemID to do the sto...
	sto$systemID()

#-------------------------------------------------------------------------------------------------
#The format of the h:/porfolio-date.csv is this:
#
# Portfolio,Market,StartDate,EndDate
# Port1,Market1,20000101,20090130
# Port1,Market2,20000101,20090130
# Port2,Market3,19990630,20090130
# Port2,Market4,19990630,20090130
