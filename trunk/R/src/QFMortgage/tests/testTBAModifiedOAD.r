# Tests for the TBAModifiedOAD.R class
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

tempDir <- squish(dataDirectory(),'temp_TSDB/')

fileMatches <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="QFMortgage"),'/TBAModifiedOAD/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))	
	return(checkSame(benchData,testData))
}

test.AllFunctionality <- function(){
	program = 'fncl'
	coupons <- seq(4.5,8.5,0.5)
	knotPoints <- seq(-3,3,0.5)
	fileDir <-  squish(system.file("testdata", package="QFMortgage"),'/TBAModifiedOAD/')

	halfLife = 30
	maxPeriods = 120
	lagPeriods = 5
	dateRange <- as.POSIXct(c('2007-05-01 15:00:00','2007-05-02 15:00:00','2007-05-03 15:00:00'))

	moddur <- TBAModifiedOAD(program,coupons)
	checkInherits(moddur,'TBAModifiedOAD')
	
	moddur$setITMKnotPoints(knotPoints)
	moddur$setCouponDateRange()
	moddur$setTBAOAD(squish(fileDir,'OAD.csv'))
	moddur$setTBASpreadDv01(squish(fileDir,'SpreadDuration.csv'))
	moddur$setCC(squish(fileDir,'CurrentCoupon.csv'))
	moddur$setOAS(squish(fileDir,'OAS.csv'))
	print("This takes a minute - moddur$generateITMOASs()")
	moddur$generateITMOASs()
	moddur$setSmileModelParameters(halfLife=halfLife, maxPeriods=maxPeriods, lagPeriods=lagPeriods)
	moddur$calculateDoasDccForITMs(dateRange=dateRange, knotPoints=c(-1, -0.5, 0, 0.5, 1))
	moddur$generateDoasDccForCoupons(dOASdCC.min = -100, dOASdCC.max = 100)
	moddur$calculateSmileAdjustedDv01(minDv01=NULL)
	uploadFilename="TBAModifiedOAD.AllFunctionality"
	moddur$uploadSmileAdjustedDv01(uploadPath=tempDir,uploadFilename=uploadFilename, uploadMethod='file')
	
	fileMatches(squish(tempDir,uploadFilename,'.csv'),squish(uploadFilename,'.csv'))
}
