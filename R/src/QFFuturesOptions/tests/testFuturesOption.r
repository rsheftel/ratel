# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

#Test functions for the FuturesOption class
library(QFMath)
library(QFFutures)
library(QFFixedIncome)
library(QFFuturesOptions)

test.FuturesOptionConstructor <- function(){
	### Bad inputs
	shouldBomb(FuturesOption(underlying = 'junk', 'call', 3,2008,3,2008))
	testContract <- Contract('fv', 'Comdty')
	shouldBomb(FuturesOption(testContract, 'junk', 3,2008,3,2008))
	shouldBomb(FuturesOption(testContact, 'call', 'junk', 2008, 3, 2008))
	shouldBomb(FuturesOption(testContract, 'call', 3, 'junk', 3, 2008))
	shouldBomb(FuturesOption(testContract, 'call', 3, 2008, 3, 'junk'))
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 2, 2008, NULL, 2009, 116)
	checkEquals('fv', futOpt$.underlyingName)
	checkEquals(3, futOpt$.underlyingCycle[[1]])
	checkEquals('call', futOpt$.optionType)
	checkEquals(2, futOpt$.optionExpiryMonth)
	checkEquals(2008, futOpt$.optionExpiryYear)
	checkEquals(3, futOpt$.underlyingExpiryMonth)
	checkEquals(2009, futOpt$.underlyingExpiryYear)	
	checkEquals(116, futOpt$.strike)
}

test.FuturesOptionValueFunctions <- function(){
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 2, 2008, 3, 2008)
	checkEquals('fv', futOpt$underlyingName())
	checkEquals('call', futOpt$optionType())
	checkEquals(c(3,6,9,12), futOpt$underlyingCycle())
	checkSame(testContract$identity(), futOpt$underlying()$identity())
	checkEquals(3, futOpt$underlyingExpiryMonth())
	checkEquals(2008, futOpt$underlyingExpiryYear())
	checkEquals(2, futOpt$optionExpiryMonth())
	checkEquals(2008, futOpt$optionExpiryYear())
	
	futOpt <- FuturesOption(testContract, 'call', 2,2008,3,2008,112)
	checkEquals(112, futOpt$strike())
}

test.FuturesOptionOptionExpiryMonth <- function(){
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 6,2008,9,2008)
	checkEquals(6, futOpt$optionExpiryMonth())
	futOpt <- FuturesOption(testContract, 'call', 5,2008,6,2008)
	checkEquals(5, futOpt$optionExpiryMonth())
}

test.FuturesOptionAttributeList <- function(){
	target <- list(option_month = 2,
				   contract = 'fv',
				   quote_convention = 'price',
				   quote_type = 'close',
				   future_month = 3,
				   expiry = 'actual',
				   option_contract = 'fv',
				   option_type = 'call',
				   quote_side = 'mid',
				   option_year = 2005,
				   instrument = 'futures_option')
			   
	testContract <- Contract('fv', 'Comdty')
    futOpt <- FuturesOption(testContract, 'call', 2, 2005, 3, 2005)
	checkSame(target, futOpt$attributeList())
}

test.FuturesOptionAvailableStrikes <- function(){
	target <- c(108,108.5,109,109.5,110,110.5,111, 111.5, 112)
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 2, 2005, 3, 2005)
	
	checkSame(target, futOpt$availableStrikes())
}

test.FuturesOptionExpiryDates <- function(){
	target <- as.Date('2008-02-22')
	testContract <- Contract('ty', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 3, 2008, 3, 2008)
	
	checkSame(target, futOpt$expiryDates())
}

test.FuturesOptionHistoricalPrices <- function(){		
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract,  'call', 3, 2005, 3, 2005)
	result <- futOpt$historicalPrices(strike = 109)
	checkSame(1.34375, first(result))
	checkSame(0.015625, last(result))
	checkSame(as.POSIXct('2004-10-07 15:00:00'), index(result[1]))
	checkSame(as.POSIXct('2005-02-17 15:00:00'), index(result[91]))		
}

test.FuturesOptionHistoricalChanges <- function(){
	testContract <- Contract('ty', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 3,2005,3,2005, 109)
	result <- futOpt$historicalChanges()
	checkSame(0.21875, first(result))
	checkSame(-0.515625, last(result))
	checkSame(as.POSIXct('2004-05-25 15:00:00'), index(result[1]))
	checkSame(as.POSIXct('2004-09-30 15:00:00'), index(result[90]))	
}

test.FuturesOptionatTheMoneyStrike <- function(){
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 3,2008,3,2008)
	checkEquals(113.5,futOpt$atTheMoneyStrike('2008-02-04'))	
}

test.FuturesOptionHistoricalDeltas <- function(){
	targetDates <- c(as.POSIXct('2008-02-04 15:00:00'), as.POSIXct('2008-02-05 15:00:00')) 
	targetResults <- c(0.1379164259592519, NA)
	target <- zoo(targetResults, targetDates)
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 3,2008,3,2008,115)
	result <- futOpt$historicalDeltas('2008-02-04','2008-02-05')
	checkSame(target, result)
}

test.FuturesOptionDeltaHedgedReturns <- function(){
	targetDates <- c(as.POSIXct('2008-02-05 15:00:00'), as.POSIXct('2008-02-06 15:00:00'))
	targetResults <- c(0.04903656364282731, -0.0589825458352613)
	target <- zoo(targetResults, targetDates)
	testContract <- Contract('fv', 'Comdty')
	futOpt <- FuturesOption(testContract, 'call', 3,2008,3,2008,115)
	result <- futOpt$deltaHedgedReturns('2008-02-04','2008-02-06')
	checkSame(target, result)
}