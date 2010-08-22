# TODO: Add comment
# 
# Author: rsheftel
###############################################################################


test.AllFunctionality <- function(){
program = 'fncl'
coupons <- seq(4.5,8.5,0.5)
fileDir <- 'h:/Smile OAD/'


perf <- TBAPerformance(program, coupons)
perf$setTsdbSources()
perf$setCouponDateRange()
perf$setTBADv01()
perf$setPartialDurations()
perf$setTBAPrice()
perf$setSwapRates()
perf$calculateExpectedPriceChange(coupon=6)
perf$calculateExpectedPriceChanges()
perf$calculateActualPriceChanges()

program = 'fncl'
coupons <- seq(4.5,8.5,0.5)
fileDir <- 'h:/Smile OAD/'


perf <- TBAPerformance(program, coupons)
perf$setTsdbSources()
perf$setCouponDateRange()
perf$setTBADv01()
perf$setPartialDurations()
perf$setTBAPrice()
perf$setSwapRates()
perf$calculateExpectedPriceChange(coupon=6)
perf$calculateExpectedPriceChanges()
perf$calculateActualPriceChanges()
perf$calculateCouponPerformances()
perf$setCurrentCoupon()
perf$generateITMPerformances(ITMKnots=seq(-3,3,0.5))
perf$uploadCouponPerformances(uploadFilename="testTBAPerf_Coupon",uploadPath=fileDir,uploadMethod='file')
perf$uploadITMPerformances(uploadFilename="testTBAPerf_ITM",uploadPath=fileDir,uploadMethod='file')
