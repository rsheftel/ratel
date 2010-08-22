# coalates Runs created with runPortfolioWFORun.r
# currently assumes 6 runs (as of 5/13/09)
# 
# Author: dhorowitz
###############################################################################



library(QFPortfolio)
library(QFReports)

args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
	dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else
{
	dateList <- getRefLastNextBusinessDates(lastBusinessDate = args[1], holidaySource = "financialcalendar", financialCenter = "nyb")
}

dataDateTime <- as.POSIXct(dateList$lastBusinessDate)


#Test metrics
metricPsiNu <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, PsiNu))
	first <- approx(x=c(2,8),y=c(0,1),xout=getMetricValues(KRatio,msiv),rule=2)$y
	second <- approx(x=c(1.5,4),y=c(0,1),xout=getMetricValues(ConditionalTwentyPercentileCalmarRatio,msiv),rule=2)$y
	third <- approx(x=c(1.1,1.3),y=c(0,1),xout=getMetricValues(OmegaRatio,msiv),rule=2)$y
	fourth <- approx(x=c(1.75,4),y=c(0,1),xout=getMetricValues(SortinoRatio,msiv),rule=2)$y
	pnl <- getMetricValues(AnnualizedNetProfit,msiv)
	return(pnl*(0.3*first + 0.3*second+ 0.2*third+ 0.2*fourth))
}
PsiNu <- Metric("PsiNu", metricPsiNu) 

outputDirectory <- squish(dataDirectory(),"STProcess/RightEdge/Portfolio/",format(dataDateTime,"%Y%m%d"),"/reports/")


start1 <- as.POSIXct('2005-12-31')
start2 <- as.POSIXct('2006-12-31')
startList <- c(start1,start1,start1,start2,start2,start2)
monthList <- c(rep(36,6), rep(48,6))
periodList <- c(12,12,3,3,1,1,12,12,3,3,1,1)

monthListLabel <- c(rep(36,3), rep(48,3))
periodListLabel <- c(12,3,1,12,3,1)


fileNameRuns <- function(x) squish(outputDirectory,'run',x,'_', as.character(startList[x]),'.csv')
fileNameWeights <- function(x) squish(outputDirectory, 'weights', x, '_' , as.character(startList[x]), '.csv')

runList <- list()
for(i in 1:length(startList)){
	currentRun <- read.csv(fileNameRuns(i))
	runList[[i]] <- zoo(currentRun[,-1], order.by = currentRun[,1])
	index(runList[[i]]) <- as.POSIXct(index(runList[[i]]))
}

weightsList <- list()
for(i in 1:length(startList)){
	currentWeights <- read.csv(fileNameWeights(i))
	weightsList[[i]] <- zoo(currentWeights[,-1], order.by = currentWeights[,1])
	index(weightsList[[i]]) <- as.POSIXct(index(weightsList[[i]]))
}

pdfFilename <- squish(outputDirectory, 'WFO vs non-WFO EquityPlots.pdf')
pdf(file = pdfFilename, paper = 'special', width = 10, height = 10)

fullOutput <- NULL
for(runNumber in 1:length(startList)){
	mainLabel = squish('Run = ', runNumber, ' , Window = ', monthListLabel[runNumber]/12, 'years , Rebal every ', periodListLabel[runNumber], ' months')
	plot(runList[[runNumber]], plot.type = 'single', col = c(1,2), main = mainLabel)
	legend('topleft', c('WFO','Non-WFO'),lty = c(1,1), col = c(1,2))
	
	pecWFO <- ZooCurveLoader$fromEquity(runList[[runNumber]][,1], 'WFO')
	pecNWFO <- ZooCurveLoader$fromEquity(runList[[runNumber]][,2], 'non-WFO')
	
	metricList <- list(AnnualizedNetProfit, KRatio, CalmarRatio, ConditionalTenPercentileCalmarRatio, ConditionalTenPercentileDrawDown, MaxDrawDown, PsiNu, OmegaRatio, SortinoRatio, ConditionalTwentyPercentileCalmarRatio)
	WFOOutput <- sapply(metricList, function(x) pecWFO$metric(x))
	names(WFOOutput) <- sapply(metricList, function(x) x$as.character())
	nonWFOOutput <- sapply(metricList, function(x) pecNWFO$metric(x))
	names(nonWFOOutput) <- sapply(metricList, function(x) x$as.character())
	output <- cbind(WFOOutput, nonWFOOutput)
	colnames(output) <- paste(colnames(output), runNumber, sep = '-')
	fullOutput <- cbind(fullOutput, output)
}


for(runNumber in 1:length(startList)){
	mainLabel = squish('WEIGHTS: Run = ', runNumber, ' , Window = ', monthListLabel[runNumber]/12, 'years , Rebal every ', periodListLabel[runNumber], ' months')
	plot(weightsList[[runNumber]], plot.type = 'single', col = seq(1, NCOL(weightsList[[runNumber]])), main = mainLabel)
	legend ('topleft', colnames(weightsList[[runNumber]]), lty = rep(1, NCOL(weightsList[[runNumber]])), col = seq(1, NCOL(weightsList[[runNumber]])))
}

dev.off()

fullOutput <- HWriterUtils$formatNumbers(fullOutput, numDigits = 2)

rowLabels <- c('WFO/non-WFO', 'Window Size', 'Rebal Period', row.names(fullOutput))
fullOutput <- rbind(c('WFO','non-WFO', 'WFO', 'non-WFO', 'WFO', 'non-WFO'),
				   paste(monthList/12, 'y win', sep = ''),
				   paste('rebal every ', periodList,'m', sep = ''),
				   fullOutput)
row.names(fullOutput) <- rowLabels
htmlFilename <- squish(outputDirectory, 'WFOoutput.html')
write.csv(fullOutput, squish(outputDirectory, 'WFOoutput.csv'), row.names = TRUE)
hwu <- HWriterUtils(htmlFilename)
hwrite(hwu$dataTable(fullOutput), hwu$connection())
hwu$closeConnection()


email <- Mail$notification(	subject=squish('WFO Run Report ',format(dataDateTime,'%Y-%m-%d')),
	content=squish('Report - available in ', makeWindowsFilename(htmlFilename)))
email$attachFile(htmlFilename)
email$attachFile(pdfFilename)
email$sendTo('team')

rm(list = ls())

