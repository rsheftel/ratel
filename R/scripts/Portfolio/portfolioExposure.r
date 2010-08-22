# TODO: Add comment
# 
# Author: rsheftel
###############################################################################

library(QFPortfolio)
library(QFReports)

runDate <- dateTimeFromArguments(commandArgs())
portfolioDir <- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'))

ep <- Exposure('AllSystemsQ', verbose=TRUE)
ep$loadCurves(squish(portfolioDir,'/curves/'))

crosses <- list( c('sector'), c('system'), c('class'), c('system','sector'), c('class','sector'))
metrics <- list(NetProfit, DownSideDeviation, WeeklyStandardDeviation)


#Add sectors
print('Adding sectors factor...')
sectors <- SystemDB$sectors(as.character(ep$smash()$market))[c('Name','Sector')]
colnames(sectors) <- c('market','sector')
ep$aggregateCurvesObject()$addFactors('sector', sectors)

#Add system class
print('Adding strategy class factor...')
systems <- as.character(unique(ep$smash()$system))
systemClasses <- sapply(systems, SystemDB$strategyClass, simplify=TRUE)
classDF <- data.frame(system=names(systemClasses) , class=systemClasses)
rownames(classDF) <- NULL
ep$aggregateCurvesObject()$addFactors('class', classDF)


report <- ExposureReport(ep,squish(portfolioDir,'/reports/Exposure'))
report$addTitle()

print('Adding metric frames & correlations...')
for (cross in crosses){
	print(squish('Cross: ',paste(cross,collapse=',')))
	print('Adding metric frame...')
	report$addMetricFrame(aggregationLevels=cross, metrics=metrics)
	print('Adding correlations...')
	report$addCorrelations(aggregationLevels=cross)	
}
report$closeConnection()

#Email report
email <- Mail$notification(squish('Portfolio Exposure'),squish('Report - available in ', makeWindowsFilename(report$filenames()$html)))
email$attachFile(report$filenames()$html)
email$sendTo('team')
