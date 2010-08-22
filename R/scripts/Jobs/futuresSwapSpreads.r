# Synthetic Swap Spread TRI using futures
# 
# Author: rsheftel
###############################################################################

library(QFPairsTrading)
dataDateTime <- dateTimeFromArguments(commandArgs(), hour=15)

bases <- c('ed_2y_bundle_1c','ed_5y_bundle_1c')
hedges <- c('tu.1c','fv.1c')
systemdb.bases 	<- c('ED.2Y.BUNDLE.1C','ED.5Y.BUNDLE.1C')
systemdb.hedges	<- c('TU.1C','FV.1C')

hedgeRatio <- 'pvbp'
hedgeSource <- 'internal'

print(squish('hedgeRatio: ',hedgeRatio))
print(squish('hedgeSource: ',hedgeSource))

for (count in 1:length(bases)){
	print(squish('Base: ',bases[[count]],', Hedge: ',hedges[[count]]))
	fp <- ModifiedFuturesPair(market.base = systemdb.bases[[count]], market.hedge = systemdb.hedges[[count]])
	fp$setUnderlyingTRIs(container='systemdb')
	fp$setHedgeRatio(specificNames=list(base=bases[count],hedge=hedges[count]), hedgeRatio.name = hedgeRatio, 
						hedgeRatio.source=hedgeSource,container='tsdb',calculatePvbp=FALSE)
	
	fp$generateTRI(hedgeRatio.offset = 0)
	tsdbName <- paste(bases[count],hedges[count],'tri',sep="_")
	fp$uploadTRI(tsdbName=tsdbName, tsdbSource='internal',uploadMethod='file', timeStamp=15)
	print('done.')
}

#10y Swap Future v 10y Cash
	print(squish('Base: DI.1C, Hedge: CT10.TRI'))
	fp <- ModifiedFuturesPair(market.base = 'DI.1C', market.hedge = 'CT10.TRI')
	fp$setUnderlyingTRIs(container='systemdb')
	fp$setHedgeRatioByName('base','di.1c_pvbp','internal')
	fp$setHedgeRatioByName('hedge','bond_government_usd_10y_otr_dv01','internal')
	fp$generateTRI(hedgeRatio.offset = 0)
	tsdbName <- 'di.1c_ct10.1c_tri'
	fp$uploadTRI(tsdbName=tsdbName, tsdbSource='internal',uploadMethod='file', timeStamp=15)
	print('done.')
	