library(QFEquity)

source(system.file("testHelper.r", package = "QFEquity"))

testMarketNeutralEquity <- function()
{
	builder <- MarketNeutralEquity(hedgeTriTsName = 'ivydb_109820_close_adj_price_mid')
	underlyingData <- Range('2000-01-01','2009-02-12')$cut(getTestData('marketNeutralEquity.data.csv'))
	underlyingData[,4] <- underlyingData[,3]
	testResult <- getTestData('testMarketNeutralEquity.rolling20.daily.csv')
	resList <- builder$runRollingRegression(securityId = 110011,window = 20,startDate = NULL,underlyingData = underlyingData)	
	checkZoos(resList$beta,testResult[,1])
	checkZoos(resList$hedge,testResult[,2])
	checkZoos(resList$triDaily,na.omit(testResult[,3]))
}

testMarketNeutralEquityWeekly <- function()
{
	builder <- MarketNeutralEquity(regLag = 5)
	underlyingData <- getTestData('marketNeutralEquity.data.csv')	
	testResult <- getTestData('testMarketNeutralEquity.rolling20.csv')
	resList <- builder$runRollingRegression(securityId = 110011,window = 20,startDate = NULL,underlyingData = underlyingData)	
	checkZoos(resList$beta,testResult[,1])
	checkZoos(resList$hedge,testResult[,2])
	checkZoos(resList$triDaily,na.omit(testResult[,3]))
	testResult <- getTestData('testMarketNeutralEquity.dollarNeutral.csv')
	resList <- builder$runDollarNeutral(securityId = 110011,startDate = NULL,underlyingData = underlyingData)
	checkZoos(resList$beta,testResult[,1])
	checkZoos(resList$hedge,testResult[,2])
	checkZoos(resList$triDaily,na.omit(testResult[,3]))
}

builder <- MarketNeutralEquity(regLife = 5)
data <- builder$getData(110007)