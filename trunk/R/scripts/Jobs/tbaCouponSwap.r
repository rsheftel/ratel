#Daily job to calculate the following for mortgage TBAs and save to tsdb:
#   Coupon Swap Model & Actual

library(QFMortgage)
dataDateTime <- dateTimeFromArguments(commandArgs(),hour=15)
endDate    <- as.POSIXct(format(dataDateTime,"%Y-%m-%d"))
startDate  <- seq(endDate,length=2,by='-1000 DSTday')[2]

models <- c("qfmodel_couponSwapModel_1.0", "qfmodel_couponSwapModel_1.5")
knots <- list(	"qfmodel_couponSwapModel_1.0"=seq(-3,3,1),
				"qfmodel_couponSwapModel_1.5"=seq(-3,3,0.5))

programs <- c('fncl','fnci')

print(squish('Running for date: ',format(endDate,"%Y-%m-%d")))
for(model in models){
	print(squish('Model: ',model))
	for (program in programs){
		
		print(squish('Running for ',program))
		print("Coupons - ")
		print(TBA$couponVector(program,'all'))
		cpnSwap <- TBACouponSwap(program=program, couponVector=TBA$couponVector(program,'all'), forwardDays=45, modelName=model)
		cpnSwap$setStartEndDates(startDate=startDate,endDate=endDate)
		
		print("Getting raw data.")
		cpnSwap$setRawDataFromTSDB()
		cpnSwap$setCouponDv01FromTsdb()
		cpnSwap$setRollDataFromTsdb()
		
		print("Calculating Actual values.")
		cpnSwap$generateActualData()
		
		print('Setting knot points:')
		print(knots[[model]])
		cpnSwap$setKnotPoints(knotPoints=knots[[model]])
		
		print("Fitting model...")
		cpnSwap$setStartEndDatesForCoupons()
		cpnSwap$generateWeights()
		cpnSwap$vectorizeMatrix()
		cpnSwap$filterVectors()
		cpnSwap$fitModel()
		
		print("Uploading to TSDB.")
		cpnSwap$writeTSDBuploadFile(uploadMethod='direct')
	}
}
print("Done.")
