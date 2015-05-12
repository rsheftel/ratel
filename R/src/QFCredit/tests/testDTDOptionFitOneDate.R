library(QFCredit)

tickerListDTD <- SystemDTD$tickerUniverse()[1:20]
cons <- DTDOptionFitOneDate('2009-04-07',tickerListDTD)
cons$loadDataFromTSDB()
dtds <- cons$.dtds
oas <- cons$.oas
rate <- cons$.rate
strike <- cons$.startStrike
impliedVol <- cons$.startVol

getConstructor <- function(){
	this <- DTDOptionFitOneDate('2009-04-07',tickerListDTD)
	this$importData(dtds,oas,rate,strike,impliedVol)
	this
}

testConstructor <- function(){
	this <- getConstructor()
	checkSame(this$.dividendAnn,0)
	checkSame(this$.baseDate,as.POSIXct('2009-04-07'))
	checkSame(this$.settleDate,as.POSIXct('2009-04-07'))
	checkSame(this$.expiryDate,as.POSIXct('2014-04-07'))
	checkSame(this$.tickerListDTD,tickerListDTD)
	checkSame(this$.refDateBuffer,as.POSIXct('2009-03-31'))
}

testImportData <- function(){
	this <- getConstructor()		
	checkSame(class(this$.dtds),'numeric')
	checkSame(class(this$.oas),'numeric')
	checkSame(NROW(this$.oas),NROW(this$.dtds))
}

testCalcFastOptionPrice <- function(){
	this <- getConstructor()
	checkSame(0.551659236,this$calcFastOptionPrice(50,10,0.5)[1])
}

testLoadFitData <- function(){
	this <- getConstructor()
	resList <- this$loadFitData(this$.dtds)	
	checkSame(NROW(resList$oasFit),NROW(resList$dtdsFit))
	assert(all(resList$oasFit > 0)); assert(all(resList$dtdsFit > 0))
	assert(all(!is.na(resList$oasFit))); assert(all(!is.na(resList$dtdsFit)))
}

testOptiFit <- function(){
	this <- getConstructor()
	this$loadFitData()	
	parameters <- c(this$.startStrike,this$.startVol)
	res <- this$optiFit(parameters)
	checkSame(res, 0.152452608593216)
}

testFit <- function(){
	this <- getConstructor()
	this$loadFitData()
	resList <- this$fit(strike,impliedVol,dtds,oas)
	checkSame(resList$r2,0.716945366512098)
}

testCalcImpliedShiftedDTDFromOptionPrice <- function(){
	this <- getConstructor()
	this$loadFitData()
	optionPrice <- (0.38 - -0.3577007)/6.119541
	res <- this$calcImpliedShiftedDTDFromOptionPrice(optionPrice,strike = 1,vol =0.5)
	checkSame(res,2.89223829040929)
}

testGetPriceDelta <- function(){
	this <- getConstructor()
	this$loadFitData()
	res <- this$getPriceDelta(oas = 0.38,alpha = -0.3577007,beta= 6.119541,strike = 1,vol =0.5)
	checkSame(res,-0.323707680183268)
}