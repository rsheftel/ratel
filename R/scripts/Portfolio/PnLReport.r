rm(list=ls())
library(QFPortfolio)
PnLFunction <- function(systems,systemCurves,slippedCurves,currentWeights,optimalWeights)
{
	pnlreport <- PortfolioPnLReport(connection=file("H:/SlippedSystemPnL-Current.csv","w"),slippedCurves,start="2003-12-31",end="2008-11-01")
	pnlreport$report(weights=currentWeights,systemNames=systems)
	close(pnlreport$.connection)

	pnlreport <- PortfolioPnLReport(connection=file("H:/NonSlippedSystemPnL-Current.csv","w"),systemCurves,start="2003-12-31",end="2008-11-01")
	pnlreport$report(weights=currentWeights,systemNames=systems)
	close(pnlreport$.connection)

	pnlreport <- PortfolioPnLReport(connection=file("H:/SlippedSystemPnL-Optim.csv","w"),slippedCurves,start="2003-12-31",end="2008-11-01")
	pnlreport$report(weights=optimalWeights,systemNames=systems)
	close(pnlreport$.connection)

	pnlreport <- PortfolioPnLReport(connection=file("H:/NonSlippedSystemPnL-Optim.csv","w"),systemCurves,start="2003-12-31",end="2008-11-01")
	pnlreport$report(weights=optimalWeights,systemNames=systems)
	close(pnlreport$.connection)
}