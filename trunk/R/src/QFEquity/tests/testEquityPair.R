library(QFEquity)

source(system.file("testHelper.r", package = "QFEquity"))

testMarketNeutralEquity <- function()
{
	builder <- EquityPair()
	underlyingData <- getTestData('EquityPair.data.csv')	
	testResult <- getTestData('testEquityPair.rolling20.csv')
	resList <- builder$runFactorBased(securityIdLag = 110011,securityIdLead = 110012,window = 20,startDate = NULL,underlyingData = underlyingData[,1:4],underlyingData[,5],underlyingData[,6])		
	checkZoos(resList$hedge,testResult[,1])
	checkZoos(resList$triDaily,na.omit(testResult[,2]))
	testResult <- getTestData('testEquityPair.dollarNeutral.csv')
	resList <- builder$runDollarNeutral(securityIdLag = 110011,securityIdLead = 110012,startDate = NULL,underlyingData = underlyingData)
	checkZoos(resList$hedge,testResult[,1])
	checkZoos(resList$triDaily,na.omit(testResult[,2]))
}