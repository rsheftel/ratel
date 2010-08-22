# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
library(QFReports)

#################### CONSTANTS ##########################
curveGroup <- 'AllSystemsQ'
metricList <- list(AnnualizedNetProfit, PsiNu, KRatio, ConditionalTwentyPercentileCalmarRatio, DownSideDeviation, WeeklyStandardDeviation)
metricList2 <- list(PsiNu, KRatio, ConditionalTwentyPercentileCalmarRatio, DownSideDeviation, WeeklyStandardDeviation)
perturbation <- 0.1
shifts <- c(-0.1, -0.05, 0, 0.05, 0.1)
#########################################################

runDate 	<- dateTimeFromArguments(commandArgs())

outputDir <- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'))
curvesDir <- squish(outputDir, '/curves')
outputDir <- squish(outputDir, '/reports')

print('Set up object...')
ps <- PortfolioSensitivity(curveGroup, curvesDir, outputDir)


print('shift Weights')
ps$shiftWeights(metricList, perturbation, saveFile = TRUE)

for(met in metricList2){
	print(squish('shift Risk for ', met$as.character()))
	ps$shiftRisk(met, shifts, changes = TRUE, saveFile = TRUE)
}

print('Emailing reports...')
email <- Mail$notification(	subject=squish('Portfolio Sensitvity Reports - ',format(runDate,'%Y-%m-%d')),
	content=squish('Report - available in ', outputDir))

email$sendTo('team')

