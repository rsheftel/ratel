# Generates the pnl Report
# Author: RSheftel
###############################################################################

#################### CONSTANTS ##########################
pnl.source	<- 'GlobeOp'
tagPrefix	<- 'report_daily_'
#########################################################

library(QFReports)

runDate 	<- dateTimeFromArguments(commandArgs())
filename	<- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'),'/reports/','DailyPnlReport')

print('Set up groups...')
	groups <- PerformanceDB$groups()
	groups <- groups[grep(tagPrefix,groups)]

print('Set up pnl object...')
	pnl <- PnlAnalysis()
	for (group in groups) pnl$addPnlGroup(rightStr(group,nchar(group)-nchar(tagPrefix)),PerformanceDB$tagsForTagGroup(group))
	pnl$loadPnl(pnl.source)

print('Set up report...')
	report <- PnlReport(filename, pnl)
	
	report$underlying()$addTitle('Pnl Analysis ')
	report$underlying()$addPlotLink("Plots for Run")
	report$underlying()$addGroupTags(pnl)
	report$addDailyPnl(pnl)
	report$addDailyEquity(pnl)
	report$addPlotEquity(pnl)
	report$underlying()$closeConnection()
	report$underlying()$email('Pnl Report - ',runDate)