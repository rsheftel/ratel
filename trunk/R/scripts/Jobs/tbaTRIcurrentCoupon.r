# tbsTRIcurrentCoupon
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

programs 	 <- c('fncl','fnci')
hedgeBaskets <- list(	fncl = c('vSwapPartials','vTreasury10y','vFuturesTU_TY','vFuturesTU_FV_TY_US','vFuturesTY','vNoHedge'),
						fnci = c('vFuturesTU_TY','vFuturesTU_FV_TY_US','vFuturesTY'))

sources <- c('model_jpmorgan','qfmodel_smithBreedan_vector1.0','internal')
					
for (program in programs){					
	print(program)
	for (source in sources){
		print(source)
		for (hedgeBasket in hedgeBaskets[[program]]){
			print(squish('Generating CC TRI for : ',hedgeBasket))
			ccTRI <- TBACurrentCouponTRI(program, TBA$couponVector(program), hedgeBasket, 98, 101)
			ccTRI$setSources(tbaCouponTRISource=source)
			ccTRI$setStartEndDatesForCoupons()
			ccTRI$setTBATRI()
			ccTRI$setTBAPrice()
			ccTRI$generateTRI()
			
			ccTRI$uploadTRIs(uploadMethod='direct')
		}
	}
	#Only need to upload these once
	print("Uploading attributes..")
	ccTRI$uploadAttributes(uploadMethod='direct')
}
print("Done.")
