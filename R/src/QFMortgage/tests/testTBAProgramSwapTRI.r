# tests for TBAProgramSwapTRI.R
# 
# Author: rsheftel
###############################################################################


library(QFMortgage)
tempDir <- squish(dataDirectory(),'temp_TSDB/')
testdataPath <- squish(system.file("testdata", package="QFMortgage"),'/TBAProgramSwapTRI/')

baseProgram <- 'fnci'
hedgeProgram <- 'fncl'
baseCoupons <- seq(4,7.5,0.5)
hedgeCoupons <- seq(4.5,8.0,0.5)

test.constructor <- function(){
	pSwap <- TBAProgramSwapTRI(baseProgram, hedgeProgram, baseCoupons, hedgeCoupons)
	checkInherits(pSwap,'TBAProgramSwapTRI')
}

test.setup <- function(){
	pSwap <- TBAProgramSwapTRI(baseProgram, hedgeProgram, baseCoupons, hedgeCoupons)
	pSwap$setSources(dv01='dv01_test', TRI='tri_test', hedgeBasket='vTreasuryTY')
	checkSame(pSwap$.sources$TRI,'tri_test')
	
	pSwap$setStartEndDatesForCoupons()
	checkSame(pSwap$.base$startDates[['6']],TBA$startEndDatesForCoupons(baseProgram,6)$startDates)
	checkSame(pSwap$.hedge$endDates[['5.5']],TBA$startEndDatesForCoupons(hedgeProgram,5.5)$endDates)
}

test.CurrentCouponSwapTRI <- function(){
	pSwap <- TBAProgramSwapTRI(baseProgram, hedgeProgram, c(4.5,5,5.5), c(5,5.5,6))
	pSwap$setSources(dv01='test', TRI='test', CC="test", hedgeBasket='vFuturesTY')
	pSwap$setStartEndDatesForCoupons()
	pSwap$setTRIs(container.base=squish(testdataPath,'TBATRI.base.csv'),container.hedge=squish(testdataPath,'TBATRI.hedge.csv'))
	pSwap$setDv01s(container.base=squish(testdataPath,'TBAdv01.base.csv'),container.hedge=squish(testdataPath,'TBAdv01.hedge.csv'))
	pSwap$setCurrentCouponData('base', container=squish(testdataPath,"CCCoupons.csv"))
	
	pSwap$generateCurrentCouponSwapTRI(hedgeCouponOffset=0.5)
	pSwap$uploadTRIs(uploadPath=tempDir,uploadMethod='file')

	fileMatches(squish(tempDir,'fnci_fncl_cc_1c_tri_vFuturesTY.csv'),squish(testdataPath,'fnci_fncl_cc_1c_tri_vFuturesTY.csv'),deleteFile=TRUE)
}
