# 	tbaTRIcouponSwaps.r
###############################################################################
library(QFMortgage)

forwardDays <- '45d'
hedgeRatio.min <- 0
hedgeRatio.max <- 1

programs <- c('fncl','fnci')
hedgeBaskets <- c('vSwapPartials','vNoHedge','vFuturesTY','vFuturesTU_TY','vFuturesTU_FV_TY_US')


###############################################################################
#			Hedge Ratios from a each coupon dv01 saved in tsdb

print("Hedge Ratios from a each coupon dv01 saved in tsdb...")
HRSources <- c('internal','model_jpmorgan','qfmodel_smithBreedan_vector1.0')

for (couponDv01Source in HRSources){
	for (program in programs){
		for (TBATRIhedge in hedgeBaskets){
			print(squish('Program : ',program))
			print(squish('Coupon Swap TRI, hedge: ',TBATRIhedge,', source for DV01s:',couponDv01Source))
			coupons <- TBA$couponVector(program,'all')
			print('Coupons')
			print(coupons)
			
			csTRI <- TBACouponSwapTRI(program,coupons,TBATRIhedge=TBATRIhedge)
			csTRI$setTsdbSources(	couponDv01					=couponDv01Source,
				couponSwapHedgeRatioSource	=couponDv01Source,
				tbaCouponTRISource			=couponDv01Source)
			csTRI$setHedgeRatio(forwardDays=forwardDays)
			csTRI$setTBATRIDataFromTsdb()
			csTRI$setHedgeRatiosFromDv01s()
			csTRI$calculateTRIs(hedgeRatio.min=hedgeRatio.min,hedgeRatio.max=hedgeRatio.max)
			csTRI$uploadTRIs(uploadMethod='direct')
		}
		#Only need to do on the program/HRSources level, does not change with the TRI type
		csTRI$setTBAPrices()
		csTRI$calculateWeightedRolls()
		csTRI$uploadWeightedRolls(uploadMethod='direct')
	}
}

print('Done.')


#####! ! ! ! ! ! !  FOLLOWING IS DEPRICATED ! ! ! ! ! ! ! ! #############


###############################################################################
#			Hedge Ratios from a model and saved in tsdb

#print("Hedge ratios from a model that saves the HR in tsdb...")
#HRSources <- c("qfmodel_couponSwapModel_1.0","qfmodel_couponSwapModel_1.5")
#
#for (couponSwapHedgeRatioSource in HRSources){
#	for (program in programs){
#		for (TBATRIhedge in hedgeBaskets){
#			print(squish('Program : ',program))
#			print(squish('Coupon Swap TRI, hedge: ',TBATRIhedge,', source for HR:',couponSwapHedgeRatioSource))
#			coupons <- TBA$couponVector(program,'all')
#			print('Coupons')
#			print(coupons)
#		
#			csTRI <- TBACouponSwapTRI(program,coupons,TBATRIhedge=TBATRIhedge)
#			csTRI$setTsdbSources(couponSwapHedgeRatioSource=couponSwapHedgeRatioSource)
#			csTRI$setHedgeRatio(forwardDays=forwardDays)
#			csTRI$setTBATRIDataFromTsdb()
#			csTRI$setHedgeRatiosFromTsdb()
#			csTRI$calculateTRIs(hedgeRatio.min=hedgeRatio.min,hedgeRatio.max=hedgeRatio.max)
#			csTRI$uploadTRIs(uploadMethod='direct')
#		}
#		#Only need to do on the program/HRSources level, does not change with the TRI type
#		csTRI$setTBAPrices()
#		csTRI$calculateWeightedRolls()
#		csTRI$uploadWeightedRolls(uploadMethod='direct')
#	}
#}
#
#
