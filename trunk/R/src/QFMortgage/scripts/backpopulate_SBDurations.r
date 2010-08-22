# Back populate the SBDurations
#
# Author: rsheftel
###############################################################################


library(QFMortgage)

startDate <- as.POSIXct('1993-01-04')
endDate	  <- as.POSIXct('2009-05-03')

for (program in c('fncl', 'fnci')){
	sb <- SBDuration(program)
	sb$setCurrentCoupon(source='internal')
	sb$setSwapSlope(source='internal')
	durations <- sb$couponDurations(coupons=TBA$couponVector(program), Range(startDate,endDate), verbose=TRUE)
	sb$uploadDurations(durations, uploadMethod='direct')
}
