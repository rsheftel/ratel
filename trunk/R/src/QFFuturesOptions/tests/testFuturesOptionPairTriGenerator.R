# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
library(QFMath)
library(GSFCore)
library(QFFixedIncome)
library(QFFutures)
library(QFFuturesOptions)

testFuturesOptionPairTriGeneratorConstructor <- function(){
	shouldBomb(FuturesOptionPairTriGenerator(NULL, 'straddle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2))
	shouldBomb(FuturesOptionPairTriGenerator('ty', 'junk', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2))
	shouldBomb(FuturesOptionPairTriGenerator('ty', 'straddle', 'junk', 'quarterly', 2))
	shouldBomb(FuturesOptionPairTriGenerator('ty', 'straddle', FuturesOptionRollLogic$straddleStrangle, 'junk', 2))
	shouldBomb(FuturesOptionPairTriGenerator('ty', 'straddle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 'junk'))
	
	this <- FuturesOptionPairTriGenerator('ty', 'straddle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2)
	checkSame('ty', this$underlying())
	checkSame(Contract('ty','Comdty'), this$underlyingContract())
	checkSame('straddle', this$type())
	checkSame(FuturesOptionRollLogic$straddleStrangle, this$rollLogic())
	checkSame('quarterly', this$optionFrequency())
	checkSame(2, this$width())	
}

testFuturesOptionPairTriGeneratorDateFunctions <- function(){
	this <- FuturesOptionPairTriGenerator('ty', 'straddle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2)
	
	checkSame(list(month = 1, year = 2008, day = 5), this$dateSplit('2008-01-05'))
	
	target <- as.POSIXct(c('2008-10-23','2008-10-24','2008-10-27', '2008-10-28'))
	result <- this$getAvailableDates(start = '2008-10-23', end = '2008-10-28')
	checkSame(target, result)
}

testFuturesOptionPairTriGeneratorAssembleOptionFunctions <- function(){
	this <- FuturesOptionPairTriGenerator('ty', 'straddle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2)
	target <- list(underlyingContract = SpecificContract('ty', 12, 2008), 
				   options = FuturesOptionPair('ty', 'straddle', 12, 2008, 12, 2008, 116, NULL),
				   centralStrike = 116					   	          
				   )
	result <- this$initializeUnderlyings('2008-10-23')
	checkSame(target,result)
	
	this <- FuturesOptionPairTriGenerator('ty', 'straddleStrangle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2)
	target <- list(underlyingContract = SpecificContract('ty', 12, 2008), 
				   options = list(FuturesOptionPair('ty', 'straddle', 12, 2008, 12, 2008, 116, NULL),
					   			  FuturesOptionPair('ty', 'strangle', 12, 2008, 12, 2008, 116, 2)),
				   centralStrike = 116					   	          
				   )
	result <- this$initializeUnderlyings('2008-10-23')
	checkSame(target,result)	
}

testFuturesOptionPairTriGeneratorCalculateHedgedReturnsStraddle <- function(){
	print('this test is slow')
	this <- FuturesOptionPairTriGenerator('ty', 'straddle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2)
	target <- zoo(c(NA, 0.052157118213387255,-0.15625), 
				as.POSIXct(c('2008-10-23', '2008-10-24', '2008-10-27')))
	result <- this$runDailyHedgedReturns('2008-10-23','2008-10-27')
	checkSame(target,result)
}
#
#testFuturesOptionPairTriGeneratorCalculateHedgedReturnsStraddleStrangle <- function(){
#	print('this test is even slower')
#	this <- FuturesOptionPairTriGenerator('ty', 'straddleStrangle', FuturesOptionRollLogic$straddleStrangle, 'quarterly', 2)
#	target <- zoo(c(NA, 0.0089426466001063,0,-0.00591952054223671,-0.06585347120729984,0.05297251179043816), 
#		as.POSIXct(c('2008-10-23', '2008-10-24', '2008-10-27', '2008-10-28','2008-10-29')))
#	result <- this$runDailyHedgedReturns('2008-10-23','2008-10-29')
#	checkSame(target,result)
#}


