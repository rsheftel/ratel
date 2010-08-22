# tests for RegressionPartials class
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

testdata <- squish(system.file("testdata", package="QFMortgage"),'/RegressionPartials/')

test.constructor <- function(){
	checkInherits(RegressionPartials('fncl','model'),'RegressionPartials')
}

test.baseFunctions <- function(){
	rp <- RegressionPartials('fnci','qfmodel_smithBreedan_vector1.0')
	checkSameLooking(c(0.2916477, 0.2917229, 0.3988466, 0.0177828), rp$partial(itm=0,slope=1))
	checkSameLooking(c(0.4180432, 0.1304766, 0.4065838, 0.0448964), round(rp$partial(itm=1,slope=2),7))
	
	rp <- RegressionPartials('fncl','qfmodel_smithBreedan_vector1.0')
	checkSameLooking(c(0.0824929, 0.3100514, 0.4885887, 0.118867), rp$partial(itm=-2,slope=-1))
	checkSameLooking(c(0.786145375, 1.1770005, 2.017370375, 1.01948375), rp$partial(itm=-0.5,slope=1.5, dv01=5))
}

test.partialZoos <- function(){
	rp <- RegressionPartials('fncl','qfmodel_smithBreedan_vector1.0')
	shouldBombMatching(rp$partials(coupons=c(4,5,6)),'Current coupon not set.')
	rp$setCurrentCoupon(container=squish(testdata,'CurrentCoupon.csv'), source='internal')
	
	shouldBombMatching(rp$partials(coupons=c(4,5,6)),'Swap slope not set.')
	rp$setSwapSlope(container=squish(testdata,'SwapRates.csv'), source='internal')
	
	shouldBombMatching(rp$partials(coupons=c(4)), 'TBA durations not set.')
	rp$setTBADv01(container=squish(testdata,'TBADurations.csv'), source='test')
	
	actuals <- rp$partials(coupons=c(4,5),Range('2007-01-07','2007-02-07'),verbose=TRUE)
	checkSame(actuals, dget(squish(testdata,'bench_dateRange.dput')))
	
	actuals <- rp$partials(coupons=c(4,6),verbose=TRUE)
	checkSame(actuals, dget(squish(testdata,'bench_allDates.dput')))
		
	rp$uploadPartials(actuals, uploadMethod='file', uploadFilename='test_uploadFile', uploadPath=testdata)
	fileMatches(squish(testdata,'test_uploadFile.csv'),squish(testdata,'bench_uploadFile.csv'))
}
