# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

library(QFFutures)
library(QFFuturesOptions)
library(QFFixedIncome)
library(QFMath)

testFuturesOptionPairConstructor <- function(){
	shouldBomb(FuturesOptionPair(NULL,'straddle',3,2008,3,2008,111,2))
	shouldBomb(FuturesOptionPair('ty',NULL,3,2008,3,2008,111,2))
	shouldBomb(FuturesOptionPair('ty','straddle',NULL,2008,3,2008,111,2))
	shouldBomb(FuturesOptionPair('ty','straddle',3,NULL,3,2008,111,2))
	shouldBomb(FuturesOptionPair('ty','straddle',3,2008,NULL,2008,111,2))
	shouldBomb(FuturesOptionPair('ty','straddle',3,2008,3,NULL,111,2))
	shouldBomb(FuturesOptionPair('ty','straddle',3,2008,3,2008,NULL,2))
	shouldBomb(FuturesOptionPair('ty','straddle',3,2008,3,2008,111,'junk'))
	
	this <- FuturesOptionPair('ty','straddle',3,2008,3,2008,111,2)
	
	checkEquals('ty', this$underlying())
	checkEquals('straddle', this$type())
	checkSame(Contract('ty','Comdty'),this$underlyingContract())
	checkEquals(111,this$strike('lower'))
	checkEquals(111, this$strike('upper'))
	checkEquals(3,this$optionMonth())
	checkEquals(2008, this$optionYear())
	checkEquals(3, this$underlyingMonth())
	checkEquals(2008, this$underlyingYear())
	checkSame(FuturesOption(Contract('ty','Comdty'),'put',3,2008,3,2008,111), this$optionLeg('lower'))
	checkSame(FuturesOption(Contract('ty','Comdty'),'call',3,2008,3,2008,111), this$optionLeg('upper'))
	
	this <- FuturesOptionPair('ty','strangle',3,2008,3,2008,111,2)
	
	checkEquals(109, this$strike('lower'))	
	checkEquals(113, this$strike('upper'))
}

# Functionality of the FuturesOptionPair is fully tested in FuturesOptionPairTriGeneratorCalculateHedgedReturnsStraddle
# These tests are therefore depricated, but I left them here for unit testing if there is a problem
# DSH 6/10/2009
#
#testFuturesOptionPairMergedPrices <- function(){
#	targetDates <- c('2007-11-01 15:00:00', '2007-11-02 15:00:00', '2007-11-05 15:00:00')
#	targetValues <- data.frame(c(1.625,1.40625,1.46875),c(1.046875, 1.359375,1.234375))
#	target <- zoo(targetValues, targetDates)
#	
#	this <- FuturesOptionPair('ty', 'straddle', 3,2008,3,2008,111,2)
#	result <- this$mergedPrices('2007-11-01', '2007-11-05')
#	colnames(target) <- colnames(result)
#	rownames(target) <- NULL
#	index(target) <- as.POSIXct(index(target))
#	
#	checkSameLooking(target,result)
#	
#	targetDates <- c('2007-11-28 15:00:00', '2007-11-30 15:00:00', '2007-12-03 15:00:00')
#	targetValues <- data.frame(c(0.796875,0.65625,0.53125),c(0.28125,0.375,0.484375))
#	target <- zoo(targetValues, targetDates)
#	
#	this <- FuturesOptionPair('fv','strangle',3,2008,3,2008,111,2)
#	result <- this$mergedPrices('2007-11-28', '2007-12-03')
#	colnames(target) <- colnames(result)
#	rownames(target) <- NULL
#	index(target) <- as.POSIXct(index(target))
#	
#	checkSameLooking(target,result)
#}
#
#testFuturesOptionPairMergedDeltaHedgedReturns <- function(){
#	liborInterpolator <- LiborInterpolator()
#	
#	targetDates <- c('2007-12-04 15:00:00', '2007-12-05 15:00:00','2007-12-06 15:00:00')
#	targetValues <- data.frame(c(0.0109044429,0.0009219243,-0.056503815),
#							   c(-0.0195073112,0.0169168624,-0.0606627066))
#	target <- zoo(targetValues, targetDates)
#	
#	this <- FuturesOptionPair('fv','straddle',3,2008,3,2008,111,2)
#	result <- this$mergedDeltaHedgedReturns('2007-12-01', '2007-12-06', liborInterpolator)
#	result <- round(result,10)
#	colnames(target) <- colnames(result)
#	rownames(target) <- NULL
#	index(target) <- as.POSIXct(index(target))
#	
#	checkSameLooking(target,result)		
#	
#	targetDates <- c('2007-12-04 15:00:00', '2007-12-05 15:00:00','2007-12-06 15:00:00')
#	targetValues <- data.frame(c(.0066250746,0.0086598508,-0.056349854),
#							   c(0.0122060476,-0.0079191469,-0.0450911362))
#	target <- zoo(targetValues, targetDates)
#	
#	this <- FuturesOptionPair('fv','strangle',3,2008,3,2008,111,2)
#	result <- this$mergedDeltaHedgedReturns('2007-12-01', '2007-12-06', liborInterpolator)
#	result <- round(result, 10)
#	colnames(target) <- colnames(result)
#	rownames(target) <- NULL
#	index(target) <- as.POSIXct(index(target))
#	
#	checkSameLooking(target,result)		
#}
#
#testFuturesOptionPairPairDeltaHedgedReturns <- function(){
#	liborInterpolator <- LiborInterpolator()
#	
#	targetDates <- c('2007-12-06 15:00:00','2007-12-07 15:00:00', '2007-12-10 15:00:00')
#	targetValues <- c(-0.1171665217,0.0164310748,0.0139135694)
#	target <- zoo(targetValues, targetDates)
#	
#	this <- FuturesOptionPair('fv','straddle',3,2008,3,2008,111,2)
#	result <- this$pairDeltaHedgedReturns('2007-12-05','2007-12-10',liborInterpolator)	
#	result <- round(result, 10)
#	colnames(target) <- colnames(result)
#	rownames(target) <- NULL
#	index(target) <- as.POSIXct(index(target))
#	
#	
#	checkSameLooking(target,result)		
#	
#	targetDates <- c('2007-12-06 15:00:00','2007-12-07 15:00:00', '2007-12-12 15:00:00', '2007-12-20 15:00:00','2007-12-21 15:00:00')
#	targetValues <- c(-0.1014409902,0.0217826683, 0.0887282554,0.022617697,-0.0487206202)
#	target <- zoo(targetValues, targetDates)
#
#	this <- FuturesOptionPair('fv','strangle',3,2008,3,2008,111,2)
#	result <- this$pairDeltaHedgedReturns('2007-12-05','2007-12-31',liborInterpolator)	
#	result <- round(result,10)
#	colnames(target) <- colnames(result)
#	rownames(target) <- NULL
#	index(target) <- as.POSIXct(index(target))
#	
#	checkSameLooking(target,result)		
#}
