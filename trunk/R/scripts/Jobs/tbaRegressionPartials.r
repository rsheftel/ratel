# tbaResgressionPartials upload script
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

endDate <- dateTimeFromArguments(commandArgs(),hour=15)

startDate <- businessDaysAgo(5,endDate)

model <- 'qfmodel_smithBreedan_vector1.0'
print(squish("model: ",model))

for (program in c('fncl', 'fnci')){
	print(program)
	rp <- RegressionPartials(program,model)
	rp$setCurrentCoupon(source='internal')
	rp$setSwapSlope(source='internal')
	rp$setTBADv01(source=model)
	partialList <- rp$partials(TBA$couponVector(program), Range(startDate, endDate), verbose=TRUE)
	print('Uploading to tsdb...')
	rp$uploadPartials(partialList, uploadMethod='direct')
}

print('Done.')
