# tbaTRIcouponSwaps.r
#
# Author: rsheftel
###############################################################################


library(QFMortgage)

knotPoints <- c(0)
halfLife = 60
maxPeriods = 252
lagPeriods = 5

#FNCL
	coupons <- seq(4.5,7.5,0.5)
	program <- 'fncl'
	
	moddur <- TBAModifiedOAD(program,coupons)
	moddur$setITMKnotPoints(knotPoints)
	moddur$setCouponDateRange()
	moddur$setTsdbSources(tbaOADSource='model_jpmorgan_bondStudio2008',smileModel='qfmodel_smileAdjustedOAD_bondStudio')
	
	moddur$setTBAOAD()
	moddur$setTBASpreadDv01()
	moddur$setCC()
	moddur$setOAS()
	print("This takes a minute - moddur$generateITMOASs()")
	moddur$generateITMOASs()
	dateRange <- index(moddur$.TBA$oas$itm[['0']][index(moddur$.TBA$oas$itm[['0']]) >= as.POSIXct('2008-01-01 15:00:00')])
	
	moddur$setSmileModelParameters(halfLife=halfLife, maxPeriods=maxPeriods, lagPeriods=lagPeriods)
	moddur$calculateDoasDccForITMs(dateRange=dateRange, knotPoints=knotPoints)
	moddur$generateDoasDccForCoupons(dOASdCC.min = -0.1, dOASdCC.max = 0.1)
	moddur$calculateSmileAdjustedDv01(minDv01=NULL)
	moddur$uploadSmileAdjustedDv01(uploadMethod='file')

#FNCI
	coupons <- seq(4,6.5,0.5)
	program <- 'fnci'
	
	moddur <- TBAModifiedOAD(program,coupons)
	moddur$setITMKnotPoints(knotPoints)
	moddur$setCouponDateRange()
	moddur$setTsdbSources(tbaOADSource='model_jpmorgan_bondStudio2008',smileModel='qfmodel_smileAdjustedOAD_bondStudio')
	
	moddur$setTBAOAD()
	moddur$setTBASpreadDv01()
	moddur$setCC()
	moddur$setOAS()
	print("This takes a minute - moddur$generateITMOASs()")
	moddur$generateITMOASs()
	dateRange <- index(moddur$.TBA$oas$itm[['0']][index(moddur$.TBA$oas$itm[['0']]) >= as.POSIXct('2008-01-01 15:00:00')])	
	
	moddur$setSmileModelParameters(halfLife=halfLife, maxPeriods=maxPeriods, lagPeriods=lagPeriods)
	moddur$calculateDoasDccForITMs(dateRange=dateRange, knotPoints=knotPoints)
	moddur$generateDoasDccForCoupons(dOASdCC.min = -0.1, dOASdCC.max = 0.1)
	moddur$calculateSmileAdjustedDv01(minDv01=NULL)
	moddur$uploadSmileAdjustedDv01(uploadMethod='file')

