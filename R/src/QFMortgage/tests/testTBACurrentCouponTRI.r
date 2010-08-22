# tests for TBACurrentCoupon.R
# 
# Author: rsheftel
###############################################################################


library(QFMortgage)
tempDir <- squish(dataDirectory(),'temp_TSDB/')
testDataPath <- squish(system.file("testdata", package="QFMortgage"),'/TBACurrentCouponTRI/')

test.constructor <- function(){
	program <- 'fncl'
	couponVector <- seq(6,8,0.5)
	hedgeBasket <- 'vTreasury10y'
	
	ccTRI <- TBACurrentCouponTRI(program, couponVector, hedgeBasket, 98, 101)
	checkInherits(ccTRI,'TBACurrentCouponTRI')
}

test.setup <- function(){
	program <- 'fncl'
	couponVector <- seq(6,8,0.5)
	hedgeBasket <- 'vTreasury10y'
	
	ccTRI <- TBACurrentCouponTRI(program, couponVector, hedgeBasket, 98, 102)
	ccTRI$setSources()
	checkSame(ccTRI$.sources$tbaCouponTRI,'internal')
	
	ccTRI$setStartEndDatesForCoupons()
	checkSame(ccTRI$.TBA$startDates[['6']],TBA$startEndDatesForCoupons(program,6)$startDates)
}

test.allFunctionality <- function(){
	program <- 'fncl'
	couponVector <- seq(4.5,8.5,0.5)
	hedgeBasket <- 'vSwapPartials'
	
	ccTRI <- TBACurrentCouponTRI(program, couponVector, hedgeBasket, 98, 101)
	ccTRI$setSources()
	ccTRI$setStartEndDatesForCoupons()
	ccTRI$setTBATRI(squish(testDataPath,'rawdata.tba.tri.csv'))
	ccTRI$setTBAPrice(squish(testDataPath,'rawdata.tba.price.csv'))
	ccTRI$generateTRI()
	
	ccTRI$uploadTRIs(tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'fncl_cc_1c_tri_vSwapPartials.csv'),squish(testDataPath,'fncl_cc_1c_tri_vSwapPartials.csv'))
	file.remove(squish(tempDir,'fncl_cc_1c_tri_vSwapPartials.csv'))
	fileMatches(squish(tempDir,'fncl_cc_1c_tri_daily_vSwapPartials.csv'),squish(testDataPath,'fncl_cc_1c_tri_daily_vSwapPartials.csv'))
	file.remove(squish(tempDir,'fncl_cc_1c_tri_daily_vSwapPartials.csv'))
	
	
	ccTRI$uploadAttributes(tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'fncl_cc_1c_coupon.csv'),squish(testDataPath,'fncl_cc_1c_coupon.csv'))
	file.remove(squish(tempDir,'fncl_cc_1c_coupon.csv'))
	fileMatches(squish(tempDir,'fncl_cc_1c_price.csv'),squish(testDataPath,'fncl_cc_1c_price.csv'))
	file.remove(squish(tempDir,'fncl_cc_1c_price.csv'))
}
