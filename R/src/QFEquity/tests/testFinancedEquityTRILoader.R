
# Author: dhorowitz
###############################################################################
library(GSFAnalytics)
library(QFEquity)
tickList <- c("f","cah")
secIDList <- c(104939,102733)
testDate <- as.POSIXct("2005-01-05")

testFinancedEquityTRILoader <- function()
{
	# tests bad inputs
	shouldBomb(FinancedEquityTRILoader(tickerList = secIDList))
	shouldBomb(FinancedEquityTRILoader(securityIDList = tickList))
	shouldBomb(FinancedEquityTRILoader(tickerList = tickList,quoteType = "junk"))
	
	# tests good inputs
	FETL <- FinancedEquityTRILoader(tickerList = tickList)	
	checkSame(FETL$.tickerList, tickList)
	checkSame(FETL$.securityIDList, secIDList)
	checkSame(FETL$.quoteType, "close")
	
	FETL <- FinancedEquityTRILoader(securityIDList = secIDList)
	checkSame(FETL$.securityIDList, secIDList)
	
	output1 <- FinancedEquityTRILoader(tickerList = tickList)$financeEquitiesUsingTickers()
	output2 <- FinancedEquityTRILoader(tickerList = tickList)$financeEquitiesUsingSecurityID()	
	checkSame(round(as.numeric(output1[['cah']][testDate,]),5),round(as.numeric(output2[['102733']][testDate,]),5))
	checkSame(class(output1[['cah']]),'zoo')
	
	# test different quote type
	FETL <- FinancedEquityTRILoader(tickerList = tickList,quoteType = "open")
	checkSame(FETL$.quoteType,"open")
}