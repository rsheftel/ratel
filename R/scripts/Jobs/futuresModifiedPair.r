# Modified Futures Pair TRI
# 
# Author: rsheftel
###############################################################################

library(QFPairsTrading)

#Get date from command line, or use logic if not available
arguments <- commandArgs()
arguments <- arguments[-(1:match("--args", arguments))]

if(NROW(arguments)==0){
	dataDate <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb", switchTime="15:00:00")$lastBusinessDate
}else
{
	dataDate <- arguments
}

dataDate <- as.POSIXlt(dataDate)
dataDate$hour <- 15
dataDateTime <- as.POSIXct(dataDate)

bases 	<- c('ty','fv','ty','us')
hedges	<- c('tu','tu','fv','ty')

hedgeRatio <- 'price_value_basis_point'
hedgeRatioShortName <- 'pvbp'
hedgeSource <- 'model_jpmorgan_parallelYieldShift'
hedgeSourceShortName <- 'jpmorgan'

print(squish('hedgeRatio: ',hedgeRatio))
print(squish('hedgeSource: ',hedgeSource))

for (count in 1:length(bases)){
	print(squish('Base: ',bases[[count]],', Hedge: ',hedges[[count]]))
	fp <- ModifiedFuturesPair(market.base = squish(toupper(bases[[count]]),'.1C'), market.hedge = squish(toupper(hedges[[count]]),'.1C'))
	fp$setUnderlyingTRIs(container='systemdb')
	fp$setHedgeRatio(specificNames=list(base=bases[count],hedge=hedges[count]), hedgeRatio.name = hedgeRatio, hedgeRatio.source=hedgeSource,container='tsdb')
	
	fp$generateTRI(hedgeRatio.offset = 0)
	tsdbName <- squish(bases[count],'.1c_',hedges[count],'.1c_',hedgeRatioShortName,'_',hedgeSourceShortName,'_tri')
	fp$uploadTRI(tsdbName=tsdbName, tsdbSource='internal',uploadMethod='direct')
	print('done.')
}
