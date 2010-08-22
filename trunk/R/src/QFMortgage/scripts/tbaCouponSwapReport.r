#Daily job to calculate the following for mortgage TBAs and save to tsdb:
#   Coupon Swap Model & Actual

library(QFMortgage)

dataDate <- "2008-06-23"

dataDate <- as.POSIXlt(dataDate)
dataDate$hour <- 15
dataDateTime <- as.POSIXct(dataDate)


#FNCL
    coupons30y <- seq(4.5,8.5,0.5)
    endDate    <- as.POSIXct(format(dataDateTime,"%Y-%m-%d"))
    startDate  <- seq(endDate,length=2,by='-1000 DSTday')[2]
    
    cpnSwap <- TBACouponSwap(program='fncl', couponVector=coupons30y, forwardDays=45)
    cpnSwap$setStartEndDates(startDate=startDate,endDate=endDate)
    cpnSwap$setRawDataFromTSDB()
	cpnSwap$setCouponDv01FromTsdb()
	cpnSwap$setRollDataFromTsdb()
    cpnSwap$generateActualData()
    cpnSwap$setKnotPoints()
    cpnSwap$setStartEndDatesForCoupons()
    cpnSwap$generateWeights()
    cpnSwap$vectorizeMatrix()
    cpnSwap$filterVectors()
    cpnSwap$fitModel()
	
	#get Data range from 
	couponSwap.actual <- cpnSwap$.couponSwap.actual.vector
	couponSwap.model <- cpnSwap$.couponSwapModel.lm$fitted
	plot(couponSwap.actual, couponSwap.model)


#FNCI
    coupons15y <- seq(4,8,0.5)
    endDate    <- as.POSIXct(format(dataDateTime,"%Y-%m-%d"))
    startDate  <- seq(endDate,length=2,by='-1000 DSTday')[2]

    cpnSwap <- TBACouponSwap(program='fnci', couponVector=coupons15y, forwardDays=45)
    cpnSwap$setStartEndDates(startDate=startDate,endDate=endDate)
    cpnSwap$setRawDataFromTSDB()
	cpnSwap$setCouponDv01FromTsdb()
	cpnSwap$setRollDataFromTsdb()	
    cpnSwap$generateActualData()
    cpnSwap$setKnotPoints()
    cpnSwap$setStartEndDatesForCoupons()
    cpnSwap$generateWeights()
    cpnSwap$vectorizeMatrix()
    cpnSwap$filterVectors()
    cpnSwap$fitModel()
    
