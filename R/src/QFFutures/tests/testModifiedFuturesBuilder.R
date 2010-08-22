## Test file for the ModifiedFuturesBuilder object

library(QFFutures)

source(system.file("testHelper.r", package = "QFFutures"))

test.ModifiedFuturesBuilder.constructor <- function(){
    contract <- Contract("ed","Comdty") 
    rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))    
    this <- ModifiedFuturesBuilder(contract,rollObj)
    assert("Contract" %in% class(this$.contract))
    assert("Roll" %in% class(this$.rollObj))
	checkSame(this$.contract,contract)
	checkSame(this$.rollObj,rollObj)
	checkSame(this$.initialized,FALSE)
	checkSame(this$.rollSchedule,data.frame(rollDate = NULL,oldTicker = NULL,newTicker = NULL))	
}

test.backAdjustment <- function(){
    this <- Contract("ed","Comdty")
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))
	# Bombs
	shouldBomb(this$continuousSimple(1,rollObj))
	this$loadRawData(rawData = getRawDataZoo("testBackAdjustmentData.csv"))
	shouldBomb(this$continuousSimple("junk",rollObj))
	shouldBomb(this$continuousSimple(1,TRUE))
    # Simple 1c
    res <- this$continuousSimple(1,rollObj)    
    checkEquals(res,getTestZoo("testContinuousSimple.csv"))
	# Simple 3c
	res <- this$continuousSimple(3,rollObj)
	checkEquals(res,getTestZoo("testSimpleBackAdjustment3C.csv"))
    # Ratio
    res <- this$continuousRatio(1,rollObj)
    checkEquals(res,getTestZoo("testContinuousRatio.csv"))
    # None
    res <- this$unadjusted(1,rollObj)
    checkEquals(res,getTestZoo("testNth.csv"))
}

test.backAdjustmentAroundRollDate <- function(){
	this <- Contract("ed","Comdty")
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))
	rawData <- getRawDataZoo("testAdjustmentAroundRollDateData.csv")
	this$loadRawData(rawData = rawData)
	res <- this$continuousSimple(1,rollObj)    
	checkEquals(res,getTestZoo("testAdjustmentOnRollDate.csv"))
	this$loadRawData(rawData = firstN(rawData,NROW(rawData)-1))
	res <- this$continuousSimple(1,rollObj)    
	checkEquals(res,getTestZoo("testAdjustmentBeforeRollDate.csv"))
	this$loadRawData(rawData = rawData[index(rawData) < as.POSIXct("2008-09-10")])
	res <- this$continuousSimple(1,rollObj)    
	checkEquals(res,getTestZoo("testAdjustmentAfterRollDate.csv"))
}

test.simpleBackAdjustmentWithKnownProblems <- function(){
	rawDataStatic <- getRawDataZoo("testStaticDataWithProblems.csv")
	this <- Contract("ed","Comdty")
	rawDataStatic <- this$loadRawData(rawData = rawDataStatic)
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,4))
	res <- this$continuousSimple(1,rollObj)
	checkEquals(res,getTestZoo("testBackAdjustmentWithKnownProblems.csv"))
	rollDataTarget <- data.frame(
		rollDate = c("2008-09-08 15:00:00","2008-06-09 15:00:00","2008-03-10 15:00:00","2007-12-11 15:00:00","2007-09-07 15:00:00","2007-06-04 15:00:00"),
		oldTicker = c("ed200809","ed200806","ed200803","ed200712","ed200709","ed200706"),
		newTicker = c("ed200812","ed200809","ed200806","ed200803","ed200712","ed200709"),
		stringsAsFactors = FALSE
	) 
	checkSame(this$modifiedRollSchedule(),rollDataTarget)
}

test.failsBecauseTickerNotAvailable <- function(){
	rawDataStatic <- getRawDataZoo("testStaticDataWithProblems.csv")
	this <- Contract("ed","Comdty")
	rawDataStatic <- this$loadRawData(rawData = rawDataStatic[,-13])
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,4))
	shouldBomb(this$continuousSimple(1,rollObj))
}

test.backAdjustment3C <- function(){
	rawDataStatic <- getRawDataZoo("testDataBackAdjustment3C.csv")
	this <- Contract("ed","Comdty")
	rawDataStatic <- this$loadRawData(rawData = rawDataStatic)
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,4))
	res <- this$continuousSimple(3,rollObj)
	checkEquals(res,getTestZoo("testBackAdjustment3C.csv"))
}

test.simpleBackAdjustmentOneDate <- function(){
	this <- Contract("ed","Comdty")
	this$loadRawData(rawData = getRawDataZoo("testOneDateData.csv"))
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))
	res <- this$continuousSimple(1,rollObj)
	checkSame(res,getZooDataFrame(zoo(97.09,as.POSIXct("2005-01-03 15:00:00"))))
}

test.noRollNeeded <- function(){
	this <- Contract("ed","Comdty")
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))
	this$loadRawData(rawData = getRawDataZoo("testNoRollNeededData.csv"))
	res <- this$continuousSimple(1,rollObj)
	checkEquals(res,getTestZoo("testNoRollNeeded.csv"))
}

test.isValidTicker <- function(){
	this <- Contract("ed","Comdty")
	this$loadRawData(rawData = getRawDataZoo("testNoRollNeededData.csv"))
	rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))
	ModifiedFuturesBuilder(this,rollObj)$isValidTicker("ed200809")
	shouldBomb(ModifiedFuturesBuilder(this,rollObj)$isValidTicker("junk"))
	shouldBomb(ModifiedFuturesBuilder(this,rollObj)$isValidTicker(NULL))
}


test.MonthlyCyle <- function(){
	cl <- Contract('cl','Comdty',monthlyCycle = 1:12)
	clRoll <- Roll(rollMethod = function(x)daysToExpiry(x,2))
	cl$loadRawData(rawData = getRawDataZoo("testCLData.csv"))
	clModified <- cl$continuousRatio(1,clRoll)
	checkEquals(clModified,getTestZoo("testCL.csv"))
}