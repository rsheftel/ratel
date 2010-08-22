# Modified Futures Risks
# 
# Author: rsheftel
###############################################################################

library(QFFutures)

dataDateTime <- dateTimeFromArguments(commandArgs())

contracts <- c('tu','fv','ty','us','di')

risk <- 'price_value_basis_point'
riskShortName <- 'pvbp'
riskSources <- list(	tu='model_jpmorgan_parallelYieldShift',
					fv='model_jpmorgan_parallelYieldShift',
					ty='model_jpmorgan_parallelYieldShift',
					us='model_jpmorgan_parallelYieldShift',
					di='model_jpmorgan_spotBPV')

print(squish('DateTime: ',as.character(dataDateTime)))
print(squish('Risk: ',risk))

for (contract in contracts){
	print(squish('Contract: ',contract))
	riskSource <- riskSources[[contract]]
	print(squish('RiskSource: ',riskSource))
	
	riskZoo <- ModifiedContract(squish(toupper(contract),".1C"), specificName=contract)$specificAttribute(quote_convention=risk, source=riskSource)
	tsdbName <- squish(contract,'.1c_',riskShortName)

	if (TimeSeriesDB()$timeSeriesExists(tsdbName))
		TimeSeriesDB()$purgeTimeSeries(tsdbName, riskSource)
	
	uploadZooToTsdb(riskZoo, tsdbNames=tsdbName, tsdbSources=riskSource, uploadMethod='direct', uploadFilename=tsdbName)			
	
	#Internal arbitration
	histDates <- seq(dataDateTime,length=7,by="-1 DSTday")
	uploadZooToTsdb(riskZoo[histDates]*100, tsdbNames=tsdbName, tsdbSources='internal', uploadMethod='direct')
}
