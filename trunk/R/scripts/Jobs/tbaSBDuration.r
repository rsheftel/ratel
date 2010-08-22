# tbaSBDuration upload script
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

endDate <- dateTimeFromArguments(commandArgs(),hour=15)

startDate <- businessDaysAgo(5,endDate)

for (program in c('fncl', 'fnci')){
	print(program)
	sb <- SBDuration(program)
	sb$setCurrentCoupon(source='internal')
	sb$setSwapSlope(source='internal')
	durations <- sb$couponDurations(coupons=TBA$couponVector(program), Range(startDate,endDate), verbose=TRUE)
	print('Uploading to tsdb...')
	sb$uploadDurations(durations, uploadMethod='direct')
}

print('Done.')
