#Daily job to calculate the following for mortgage TBAs and save to tsdb:
#   TRI for individual coupons

library(QFMortgage)
dataDateTime <- dateTimeFromArguments(commandArgs())

programs <- c('fncl','fnci')
hedgeBaskets <- c('vSwapPartials','vNoHedge','vTreasury10y','vFuturesTY','vFuturesTU_TY','vFuturesTU_FV_TY_US')
sources <- c('internal','model_jpmorgan','qfmodel_smithBreedan_vector1.0')

for (program in programs){
	print(squish('Running for ',program,'...'))
	for (source in sources){
		print(squish('Source : ',source))
		tbaTri <- TBATRI(program,TBA$couponVector(program,'all'))
		tbaTri$setDateRange(dataDate=dataDateTime, daysBack=7)
		tbaTri$setTsdbSources(tbaDurationSource=source)
		print('Loading raw data from tsdb...')	
		tbaTri$setTBADataFromTsdb()
		tbaTri$setSwapDataFromTsdb()
		tbaTri$setCashTreasuryDataFromTsdb()
		tbaTri$setTreasuryFuturesData()
		tbaTri$calculateTBAContinuousPriceChange()
		tbaTri$calculateTBAContinuousPriceLevel()
		
		for (hedgeBasket in hedgeBaskets){
			print(hedgeBasket)
			tbaTri$generateTRI(hedgeBasket=hedgeBasket)
			tbaTri$uploadTRItoTsdb(hedgeBasket=hedgeBasket,source=source,uploadMethod='direct')		
		}
	}
}
print("Done.")
