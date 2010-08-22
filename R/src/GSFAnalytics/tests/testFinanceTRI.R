## Test file for the Pair object
library(GSFAnalytics)
library(GSFCore)

testDate <- as.POSIXct("2005-01-05")
testSeriesY <- "ivydb_102733_close_adj_price_mid"
testSeriesX <- "irs_usd_rate_2y_tri"

test.FinanceTRI..runPair <- function(){
    
    this <- FinanceTRI()
    
    result <- this$.runPair(testSeriesY, testSeriesX, 1.0, "internal_test", TRUE, TRUE, 100)
    target <- 131.06844
    checkSame(round(as.numeric(result[testDate,]),5), target)
    
    result <- this$.runPair(testSeriesY, testSeriesX, 0.5, "internal_test", TRUE, TRUE, 100)
    target <- 134.58317
    checkSame(round(as.numeric(result[testDate,]),5), target)
    
    result <- this$.runPair(testSeriesY, testSeriesX, 1.0, "internal_test", TRUE, FALSE, 100)
    target <-  125.1077
    checkSame(round(as.numeric(result[testDate,]),5), target)
    
    result <- this$.runPair(testSeriesY, testSeriesX, 0.5, "internal_test", TRUE, FALSE, 100)
    target <- 131.6028
    checkSame(round(as.numeric(result[testDate,]),5), target)
    
    shouldBombMatching(this$.runPair(1, testSeriesX, 1.0, "internal", TRUE, TRUE, 100), "Error: seriesY is not character is numeric")
    shouldBombMatching(this$.runPair(testSeriesY, 1, 1.0, "internal", TRUE, TRUE, 100), "Error: seriesX is not character is numeric")
    shouldBombMatching(this$.runPair(testSeriesY, testSeriesX, "test", "internal", TRUE, TRUE, 100), "Error: hedgeRatio is not numeric is character")
    shouldBombMatching(this$.runPair(testSeriesY, testSeriesX, 1.0, 1, TRUE, TRUE, 100), "Error: source is not character is numeric")
    shouldBombMatching(this$.runPair(testSeriesY, testSeriesX, 1.0, "internal", "test", TRUE, 100), "Error: stripTimes is not logical is character")
    shouldBombMatching(this$.runPair(testSeriesY, testSeriesX, 1.0, "internal", TRUE, "test", 100), "Error: marketValueHedge is not logical is character")
    shouldBombMatching(this$.runPair(testSeriesY, testSeriesX, 1.0, "internal", TRUE, TRUE, "test"), "Error: baseTri is not numeric is character")
    
    shouldBombMatching(this$.runPair(testSeriesY, testSeriesX, 1.0, "internal", FALSE, TRUE, 100), "subscript out of bounds")
}

test.FinanceTRI.pair <- function(){
    this <- FinanceTRI()
    
    result <- this$pair(testSeriesY, testSeriesX,source = 'internal_test')
    target <- 131.06844
    checkSame(round(as.numeric(result[testDate,]),5), target)
}

test.FinanceTRI.usingONLibor <- function(){
    this <- FinanceTRI()
    
    result <- this$usingONLibor(testSeriesY,source = 'internal_test')
    target <- 126.94722
    checkSame(round(as.numeric(result[testDate,]),5), target)
}

test.FinanceTRI.equity <- function(){
    this <- FinanceTRI()
    
    result <- this$equity(ticker = "cah",source = 'internal_test',adjustLive = FALSE)
    target <- 126.94722
    checkSame(round(as.numeric(result[testDate,]),5), target)
    
    result <- this$equity(ticker = "cah",source = 'internal_test', fundingCostName = testSeriesX,adjustLive = FALSE)
    target <- 131.06844
    checkSame(round(as.numeric(result[testDate,]),5), target)
	
	securityID <- IvyDB()$lookupSecurityID('cah')
	result <- this$equity(securityID = securityID,source = 'internal_test',adjustLive = FALSE)
	target <- 126.94722
	checkSame(round(as.numeric(result[testDate,]),5), target)
       
    shouldBombMatching(this$equity(1), "Error: ticker is not character is numeric")
	
	securityID <- IvyDB()$lookupSecurityID('cah')
	result <- this$equity(securityID = securityID,source = 'internal_test',adjustLive=TRUE)
	target <- 59.68898
	checkSame(round(as.numeric(result[testDate,]),5), target)
}

test.FinanceTRI.runOpenHighLowSeries <- function(){
	this <- FinanceTRI()
	result <- this$runOpenHighLowSeries(105175,'open','internal_test')
	target <- 91.63519
	checkSame(round(as.numeric(result[testDate,]),5), target)
	result <- this$runOpenHighLowSeries(105175,'high','internal_test')
	target <- 91.70653
	checkSame(round(as.numeric(result[testDate,]),5), target)
	result <- this$runOpenHighLowSeries(105175,'low','internal_test')
	target <- 91.06445
	checkSame(round(as.numeric(result[testDate,]),5), target)
	shouldBomb(this$runOpenHighLowSeries(105175,'none','internal_test'))
}