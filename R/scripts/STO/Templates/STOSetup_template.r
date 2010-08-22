# Template on how to use the NewSTO Class
###############################################################################

library(QFSTO)

#	Create the constructor
sto <- STOSetup(system="NewSystem", interval="daily", version="1.0", stoDirectory="h:/temp", stoID="NewID")

#	Load the portfolio, markets and dates. Set a default dates if nothing in the file
sto$addPortfolios(filename='h:/portfolio-date.csv')
sto$startEndDates(start='19990101',end='20090130',filename='h:/portfolio-date.csv')

#	Add the parameters (could also add one at time)
sto$addParameters(name=c('Param1','Param2'),start=c(1,2),end=c(3,4),step=c(1,2))

#	Add the systemDB specific stuff
sto$strategyClass("Test")
sto$systemQClassName("TestQClass.c")

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
