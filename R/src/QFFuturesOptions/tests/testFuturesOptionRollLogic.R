# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


library(QFFutures)
library(QFFuturesOptions)

test.FuturesOptionRollLogicJPMorgan <- function(){
	#test Bad Inputs
	shouldBomb(FuturesOptionRollLogic$jpMorgan('junk', '2008-08-21', '2008-08-19', 'quarterly', 115, NULL))
	shouldBomb(FuturesOptionRollLogic$jpMorgan('2008-08-20', 'junk', '2008-08-19', 'quarterly', 115, NULL))
	shouldBomb(FuturesOptionRollLogic$jpMorgan('2008-08-20', '2008-08-21', '2008-08-19', 'junk', 115, NULL))
	shouldBomb(FuturesOptionRollLogic$jpMorgan('2008-08-20', '2008-08-21', '2008-08-19', 'monthly', 115, NULL))
	shouldBomb(FuturesOptionRollLogic$jpMorgan('2008-08-20', '2008-08-21', '2008-08-19', 'quarterly', 'junk', NULL))
	shouldBomb(FuturesOptionRollLogic$jpMorgan('2008-08-20', '2008-08-21', '2008-08-19', 'quarterly', 115, 'junk'))
	
	target <- zoo(data.frame(optionMonth = 12, optionYear = 2008, underlyingMonth = 12, underlyingYear = 2008), 
				  order.by = '2008-08-20')
	result <- FuturesOptionRollLogic$jpMorgan('2008-08-20', '2008-08-21', '2008-08-19', 'quarterly', 115, NULL)
	checkSame(target, result)
	
	
	target <- zoo(data.frame(optionMonth = 3, optionYear = 2008, underlyingMonth = 3, underlyingYear = 2008), 
				  order.by = '2007-11-14')
	result <- FuturesOptionRollLogic$jpMorgan('2007-11-14', '2007-11-15', '2007-11-13', 'quarterly', 115, NULL)
}

test.FuturesOptionRollLogicUnderlyingDatesQuarterly <- function(){
	target <- list(underlyingMonth = 6, underlyingYear = 2008, optionMonth = 6, optionYear = 2008) 
		
	result <- FuturesOptionRollLogic()$underlyingDatesQuarterly('2008-03-12')
	checkSame(result, target)
	
	target <- list(underlyingMonth = 3, underlyingYear = 2009, optionMonth = 3, optionYear = 2009)
	
	result <- FuturesOptionRollLogic()$underlyingDatesQuarterly('2008-11-12')
	
	checkSame(result, target)	
}

test.FuturesOptionRollLogicNextStraddleStrangleData <- function(){
	target <- list(optionMonth = 6, optionYear = 2008, underlyingMonth = 6, underlyingYear = 2008, strike = 116)
	testFuture <- Contract('ty','Comdty')
	
	result <- FuturesOptionRollLogic()$nextStraddleStrangleData('2008-02-04', 'quarterly', testFuture)
	checkSame(target, result)	
}

test.FuturesOptionRollLogicStraddleStrangle <- function(){
	#test Bad Inputs
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('junk', '2007-11-15', '2007-11-13','quarterly',111,NULL, SpecificContract('ty',3,2008),2,0.8))
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', '2007-11-13','junk',111,NULL, SpecificContract('ty',3,2008),2,0.8))
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', '2007-11-13','quarterly','junk',NULL, SpecificContract('ty',3,2008),2,0.8))
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', '2007-11-13','quarterly',111,'junk', SpecificContract('ty',3,2008),2,0.8))
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', '2007-11-13','quarterly',111,NULL, SpecificContract('ty',3,2008),NULL,0.8))
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', '2007-11-13','quarterly',111,NULL, SpecificContract('ty',3,2008),2,NULL))
	
	# if lastDate does NOT equal NULL, then must use Specific Contract
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', '2007-11-13','quarterly',111,NULL, Contract('ty','Comdty'),2,0.8))
	# similarly, if lastDate DOES equal NULL, then must use Generic Contract
	shouldBomb(FuturesOptionRollLogic$straddleStrangle('2007-11-14', '2007-11-15', NULL,'quarterly',111,NULL, SpecificContract('ty',3,2008),2,0.8))
	
	target <- list(optionMonth = 3, optionYear = 2008, underlyingMonth = 3, underlyingYear = 2008, strike = 111)
	result <- FuturesOptionRollLogic$straddleStrangle('2007-11-14', NULL,NULL, 'quarterly',111,NULL, Contract('ty','Comdty'),2,0.8)
	checkSame(target, result)
	
	# result is NULL if no roll is  necessary
	result <- FuturesOptionRollLogic$straddleStrangle('2007-11-14', NULL,'2007-11-13', 'quarterly',110,NULL, SpecificContract('ty',3,2008),2,0.8)
	checkSame(NULL, result)
	
	target <- list(optionMonth = 3, optionYear = 2008, underlyingMonth = 3, underlyingYear = 2008, strike = 111)
	result <- FuturesOptionRollLogic$straddleStrangle('2007-11-14', NULL,'2007-11-13', 'quarterly',108,NULL, SpecificContract('ty',3,2008),2,0.8)
	checkSame(target, result)
	
	
}

