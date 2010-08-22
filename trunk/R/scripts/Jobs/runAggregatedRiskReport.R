# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
#####################################
library(QFPortfolio)

curveGroup <- 'AllSystemsQ'

runDate 	<- dateTimeFromArguments(commandArgs())

outputDir <- squish(dataDirectory(),'/STProcess/RightEdge/Portfolio/',format(runDate,'%Y%m%d'))
curvesDir <- squish(outputDir, '/curves')
outputDir <- squish(outputDir, '/reports')
fileName <- squish(outputDir, '/AggregatedRiskReport.pdf')

pa <- PositionAggregator(curveDirectory = curvesDir, groupName = curveGroup)
output <- pa$riskBySector()
relOutput <- output / rowSums(output, na.rm = TRUE)
output <- cbind(output, rowSums(output, na.rm = TRUE))

colnames(output)[[NCOL(output)]] <- 'TOTAL'

lastOutput <- output[NROW(output),]
lastRelOutput <- relOutput[NROW(relOutput),]
absBarPlotX <- barplot(as.matrix(lastOutput))
relBarPlotX <- barplot(as.matrix(lastRelOutput))


pdf(fileName,paper="special",width=10,height=10)
plot(relOutput, main = 'Percentage of Total Risk')
barplot(as.matrix(lastRelOutput), main = 'Percentage of Total Risk', col = 'blue', ylim = c(0, max(lastRelOutput) + 0.05))
for(i in 1:length(relBarPlotX)) text(relBarPlotX[[i]], lastRelOutput[[i]] + 0.01, round(lastRelOutput[[i]],2)) 

plot(output, main = 'Absolute Total Risk')
barplot(as.matrix(lastOutput), main = 'Absolute Total Risk', col = 'blue', ylim = c(0, max(lastOutput) + 0.1 * max(lastOutput)))
for(i in 1:length(lastOutput)) text(absBarPlotX[[i]], lastOutput[[i]] + 0.05 * max(lastOutput), round(lastOutput[[i]], 0))
dev.off()


print('Emailing reports...')
email <- Mail$notification(	subject=squish('Aggregated Risk Reports - ',format(runDate,'%Y-%m-%d')),
	content=squish('Report - available in ', fileName))
email$attachFile(fileName)

email$sendTo('team')





