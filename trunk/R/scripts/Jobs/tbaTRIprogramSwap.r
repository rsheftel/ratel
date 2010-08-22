# tbaTRIProgramSwap
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)
dataDateTime <- dateTimeFromArguments(commandArgs(),hour=15)

#Dwarf / FNCL CC Swap
baseProgram <- 'fnci'
hedgeProgram <- 'fncl'
hedgeBaskets <- c('vSwapPartials','vTreasury10y','vNoHedge','vFuturesTY','vFuturesTU_TY','vFuturesTU_FV_TY_US')
sources <- c('internal','model_jpmorgan','qfmodel_smithBreedan_vector1.0')

print('Dwarf / FNCL CC Program Swap')
for (source in sources){
	for (hedgeBasket in hedgeBaskets){
		print(squish('Generating CC TRI for : ',hedgeBasket,' - Source: ',source))
		pSwap <- TBAProgramSwapTRI(baseProgram, hedgeProgram, TBA$couponVector(baseProgram,'all'), TBA$couponVector(hedgeProgram,'all'))
		pSwap$setSources(dv01=source, TRI=source, CC='internal', hedgeBasket=hedgeBasket)
		
		print('Loading data from tsdb...')
		pSwap$setStartEndDatesForCoupons()
		pSwap$setTRIs()
		pSwap$setDv01s()
		pSwap$setCurrentCouponData('base')
		
		print('Calculating and uploading TRI...')
		pSwap$generateCurrentCouponSwapTRI(hedgeCouponOffset=0.5)
		pSwap$uploadTRIs(uploadMethod='direct')
	}
}
print("Done.")
