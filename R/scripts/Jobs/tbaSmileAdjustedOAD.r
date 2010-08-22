# Smile adjusted OADs
#
# Author: rsheftel
###############################################################################
library(QFMortgage)
dataDateTime <- dateTimeFromArguments(commandArgs(),hour=15)

smileModels <- c('qfmodel_smileAdjustedOAD_bondStudio') #c('qfmodel_smileAdjustedOAD_jpmorgan', 'qfmodel_smileAdjustedOAD_bondStudio')
tbaOADSources <- c('model_jpmorgan_bondStudio2008') #c('model_jpmorgan_2008', 'model_jpmorgan_bondStudio2008')
programs <- c('fncl','fnci')

for (program in programs){
	for (count in seq_along(smileModels)){
		smileModel <- smileModels[count]
		tbaOADSource <- tbaOADSources[count]
		coupons <- TBA$couponVector(program,'all')
		dateRange <- seq(dataDateTime, length=7, by="-1 DST")
				
		print(squish('Smile model: ',smileModel))
		print(squish('OAD Source : ',tbaOADSource))
		print(squish('Program: ',program))
		print('Coupons:')
		print(coupons)
		print(dateRange)
		
		knotPoints <- c(0)
		halfLife = 60
		maxPeriods = 252
		lagPeriods = 5
	
		moddur <- TBAModifiedOAD(program,coupons)
		moddur$setITMKnotPoints(knotPoints)
		moddur$setCouponDateRange()
		moddur$setTsdbSources(tbaOADSource=tbaOADSource,smileModel=smileModel)
		moddur$setTBAOAD()
		moddur$setTBASpreadDv01()
		moddur$setCC()
		moddur$setOAS()
		print("This takes a minute - moddur$generateITMOASs()")
		moddur$generateITMOASs()
		moddur$setSmileModelParameters(halfLife=halfLife, maxPeriods=maxPeriods, lagPeriods=lagPeriods)
		moddur$calculateDoasDccForITMs(dateRange=dateRange, knotPoints=knotPoints)
		moddur$generateDoasDccForCoupons(dOASdCC.min = -0.1, dOASdCC.max = 0.1)
		moddur$calculateSmileAdjustedDv01(minDv01=NULL)
		moddur$uploadSmileAdjustedDv01(uploadMethod='direct')
	}
}
