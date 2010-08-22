# Code to support the excel TBACouponSwap transformation engine
# 
# Author: rsheftel
###############################################################################


library(QFMortgage)


cs.initialize <- function(){
	cs.couponVector <- seq(cs.startCoupon,cs.endCoupon,0.5)
	cpnSwapObject <- TBACouponSwap(program=cs.program, couponVector=cs.couponVector, forwardDays=cs.forwardDays)
	return(cpnSwapObject)
}

cs.setDateRange <- function(oCS){
	cs.endDate <- as.POSIXct(cs.endDate)	
	startDate <- seq(cs.endDate,length=2,by='-1000 DSTday')[2]
	oCS$setStartEndDates(startDate=startDate,endDate=cs.endDate)
	
	cs.knotPoints <- seq(cs.knotPoints.low,cs.knotPoints.high,cs.knotPoints.step)
	oCS$setKnotPoints(knotPoints=cs.knotPoints)
	oCS$setStartEndDatesForCoupons()
} 

cs.loadHistoricalData <- function(oCS){
	oCS$setRawDataFromTSDB()
	oCS$setCouponDv01FromTsdb()
	oCS$setRollDataFromTsdb()
}

cs.fitModel <- function(oCS){
	oCS$generateActualData()
	oCS$generateWeights()	
	oCS$vectorizeMatrix()
	oCS$filterVectors()
	oCS$fitModel()
}

cs.getModelValues <- function(oCS, cs.currentCoupon, cs.slope){
	return(oCS$getModelValues(oCS$.couponVector[2:length(oCS$.couponVector)], cs.currentCoupon, cs.slope))
}

cs.getModelHedgeRatios <- function(oCS, cs.currentCoupon, cs.slope){
	return(oCS$getModelHedgeRatios(oCS$.couponVector[2:length(oCS$.couponVector)], cs.currentCoupon, cs.slope))
}

cs.getModelWeightedRolls <- function(oCS, cs.currentCoupon, cs.slope){
	return(oCS$getModelWeightedRolls(oCS$.couponVector[2:length(oCS$.couponVector)], cs.currentCoupon, cs.slope))
}

cs.getActualValues <- function(oCS){
	return(oCS$getActualValues(oCS$.couponVector[2:length(oCS$.couponVector)]))		
}

############# Live Functions #################################
cs.addNewDate <- function(oCS, newDate){
	newDate <- as.POSIXlt(newDate)
	newDate$hour <- 15
	oCS$addNewDate(as.POSIXct(newDate))
	cs.fitModel(oCS)
}

cs.addLiveValues <- function(oCS, priceMatrix, settleDatesMatrix, couponVector, settleVector, swap2y, swap10y){
	
	#Get forwardPrices and CC
	nowDate <- as.POSIXct('2008-10-27')
	couponVector <- as.numeric(couponVector)
	
	tbaGrid <- TBAGrid(oCS$.program,couponVector, nowDate)
	tbaGrid$setPricesFromMatrix(priceMatrix,couponVector,settleVector)
	tbaGrid$setSettleDatesFromMatrix(settleDatesMatrix,settleVector)
	cc <- tbaGrid$getCurrentCoupon()
	forwardPrices <- tbaGrid$getNDayForwardPrice(oCS$.forwardDays)
	
	oCS$updateLastTBAPrices(forwardPrices, couponVector)
	oCS$updateLastCurrentCoupon(cc)
	oCS$updateLastSwapRates(swap2y, swap10y)
	
	rollVector <- priceMatrix[,match('1n',settleVector)] - priceMatrix[,match('2n',settleVector)]
	goodCols <- match(oCS$.couponVector,couponVector)[!is.na(match(oCS$.couponVector,couponVector))]
	oCS$setRollData(rollVector[goodCols], couponVector[goodCols])
	
	return(cc)
}
