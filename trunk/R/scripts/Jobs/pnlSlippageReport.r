# Generate the hypo v actual pnl report
# 
# Author: RSheftel
###############################################################################

#################### CONSTANTS ##########################
	source.hypo <- 'Hypo_Daily'
	source.actual <- 'GlobeOp'
	sources <- c(source.actual, source.hypo)
	tagPrefix	<- 'report_daily_'
#########################################################

library(QFReports)

runDate 	<- dateTimeFromArguments(commandArgs())
filename	<- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'),'/reports/','DailySlippageReport')

print('Set up groups...')
	groups <- PerformanceDB$groups()
	groups <- groups[grep(tagPrefix,groups)]

print('Set up pnl object...')
	pnl <- PnlAnalysis()
	for (group in groups) pnl$addPnlGroup(rightStr(group,nchar(group)-nchar(tagPrefix)),PerformanceDB$tagsForTagGroup(group))
	for (source in sources)	pnl$loadPnl(source)	

print('Set up report...')
	report <- PnlDifferenceReport(filename, pnl, sources)
	report$addTitle()
	report$addPlotLink()
	report$addGroupTags()	
	#report$addDifferenceGrid(Range(runDate,runDate))			#Line commented out because it fails on satuday runs
	report$addDifferenceGrid(Range$lastNDays(7,runDate))
	report$addDifferenceGrid(Range$lastNDays(30,runDate))
	report$addDifferenceGrid(Range$ytd(runDate))
	report$addDailyDifference()	
	report$addPlotSlippageCurves(range = Range$lastNDays(30,runDate),mainTitle = '30 days')
	report$addPlotSlippageCurves(range = Range$ytd(runDate),mainTitle = 'YTD')
	report$addPlotSlippageCurves(mainTitle = 'All Period')
	report$closeConnection()

print('Emailing reports...')
	email <- Mail$notification(	subject=squish('Pnl Slippage Report - ',format(runDate,'%Y-%m-%d')),
		content=squish('Report - available in ', makeWindowsFilename(report$filenames()$html)))
	email$attachFile(report$filenames()$html)
	email$sendTo('team')