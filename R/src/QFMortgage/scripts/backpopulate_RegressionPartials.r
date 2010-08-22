# Back populate the SBDurations
#
# Author: rsheftel
###############################################################################


library(QFMortgage)

startDate <- as.POSIXct('1993-01-04')
endDate	  <- as.POSIXct('2009-05-03')

model <- 'qfmodel_smithBreedan_vector1.0'

program <- 'fnci'

	rp <- RegressionPartials(program,model)
	rp$setCurrentCoupon(source='internal')
	rp$setSwapSlope(source='internal')
	rp$setTBADv01(source=model)
	partialList <- rp$partials(TBA$couponVector(program), Range(startDate, endDate), verbose=TRUE)
	rp$uploadPartials(partialList, uploadMethod='file', uploadPath=tempDirectory(), uploadFilename=squish('batch_partials_',program))
