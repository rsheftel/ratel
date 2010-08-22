
# Post STO Analysis to upload and save the results of the STO

library(QFSTO)

#Get the systemID of the in and out sample STOs, use the SystemDB class methods if you don't know them

	systemID.in <- SystemDB$systemID(system='MySystem', interval='daily', version='1.0', stoDir='h:/Temp', stoID='InSample')
	systemID.out <- 123456
	
#Construct the object

	sr <- STOResults(systemID.in, systemID.out)
	
#Set the runDate, stored in systemDB

	sr$runDate(runDate.in='21001231', runDate.out='21010105')

#Chose which portfolios were accepted in the validation.

	sr$stoSetupObject('in')$portfolios()	#Shows what the portfolios are
	sr$stoSetupObject('out')$portfolios()	#Shows what the portfolios are

	sr$acceptedPortfolios(c('EquityFutures','Portfolioo2'))

#Confirm the portfolios and markets
	sr$acceptedPortfolios()	
	sr$acceptedMarkets()
		
#Upload the information to the DBs

	sr$uploadToSystemDB(commitToDB=FALSE)	#Set commitToDB=TRUE for real use...
	
