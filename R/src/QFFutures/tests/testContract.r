library(QFFutures)

testdataPath <- squish(system.file("testdata", package="QFFutures"),'/Contract/')
tempDir <- squish(dataDirectory(),'temp_TSDB/')

testExpiry <- function() {
	# Coming from SystemDB
    checkSameLooking(as.POSIXct("2008/09/15"), Contract("ed", "Comdty")$expiry("2008/09/01"))
	# Coming from Bloomberg
	checkSameLooking(as.POSIXct("2008/09/26"), Contract("gc", "Comdty")$expiry("2008/09/01"))
}

testName <- function() {
    checkSameLooking("ed", Contract("ed", "Comdty")$name())
}

testYellowKey <- function(){
	checkSame(Contract('ed','Comdty')$.yellowKey,'Comdty')
	checkSame(Contract('ed')$.yellowKey,'Comdty')
}

testBloombergRoot <- function(){
	checkSame(Contract('ed',bloomberRoot = 'ed')$.bloombergRoot,'ED')
	checkSame(Contract('ed')$.bloombergRoot,'ED')
}

testMonthlyCycle <- function() {
    checkSameLooking(c(3,6), Contract("ed", "Comdty",monthlyCycle = c(3,6))$monthlyCycle())
	checkSameLooking(c(3,6,9,12), Contract("ed", "Comdty")$monthlyCycle())
}

testnumCycles <- function() {
    checkSameLooking(1, Contract("ed", "Comdty")$numCycles())
}

testAttributes <- function() {
	checkEquals(
		list(contract = "ed",instrument = "futures",quote_type = "close",quote_convention = "price",quote_side = "mid", expiry="actual"),
		Contract("ed", "Comdty")$attributes()
	)
}

test.gfutYearMonthFrame <- function(){	
	res <- Contract$gfutYearMonthFrame()
	assert(all(as.character(sapply(res[,'marketName'],function(x){substr(x,4,7)})) == 'GFUT'))
	checkSame(colnames(res),c('marketName','field'))
}

testRollDatesAsOf <- function() {
    this <- Contract("ed","Comdty")
    rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5)) 
    result <- this$rollDatesAsOf(rollObj,"2006-09-10")
    checkEquals(result,as.POSIXct(c("2006-09-11","2006-12-11","2007-03-12","2007-06-11")))
    checkEquals(result,this$rollDatesAsOf(rollObj,"2006-09-10 15:00:00"))
    checkEquals(result,this$rollDatesAsOf(rollObj,as.POSIXct("2006-09-10")))
    result.2 <- this$rollDatesAsOf(rollObj,"2006-09-11",FALSE)    
    checkEquals(result,result.2)    
    result.3 <- this$rollDatesAsOf(rollObj,"2006-09-12",FALSE)        
    checkEquals(result.3,as.POSIXct(c("2006-09-11","2006-12-11","2007-03-12","2007-06-11")))
    result.4 <- this$rollDatesAsOf(rollObj,"2006-09-12",TRUE)        
    checkEquals(result.4,as.POSIXct(c("2006-12-11","2007-03-12","2007-06-11")))    
}

testRollDatesInWindow <- function() {
    this <- Contract("ed","Comdty")
    rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5)) 
    checkEquals(as.POSIXct(c("2007-09-10","2007-12-10","2008-03-10","2008-06-09")),this$rollDatesInWindow("2007-07-02","2008-07-02",rollObj))
    checkEquals(as.POSIXct(c("2007-09-10","2007-12-10","2008-03-10","2008-06-09")),this$rollDatesInWindow(as.POSIXct("2007-07-02"),as.POSIXct("2008-07-02"),rollObj))    
    checkEquals(as.POSIXct(c("2007-09-10","2007-12-10","2008-03-10","2008-06-09")),this$rollDatesInWindow("2007-07-02 15:00:00","2008-07-02 15:00:00",rollObj))        
    checkEquals(NULL,this$rollDatesInWindow("2007-07-02","2007-07-03",rollObj))
    shouldBomb(this$rollDatesInWindow("2007-07-02","2006-07-03",rollObj))
    shouldBomb(this$rollDatesInWindow("junk","2006-07-03",rollObj))    
}

testExpiryDates <- function() {
    this <- Contract("ed","Comdty")
    checkEquals(this$expiryDates("2007-07-02"),as.POSIXct(c("2007-09-17","2007-12-17","2008-03-17","2008-06-16")))
    checkEquals(this$expiryDates("2007-07-02 15:00:00"),as.POSIXct(c("2007-09-17","2007-12-17","2008-03-17","2008-06-16")))    
    checkEquals(this$expiryDates(as.POSIXct("2007-07-02")),as.POSIXct(c("2007-09-17","2007-12-17","2008-03-17","2008-06-16")))        
}

testWrongYellowKey <- function() {	
	shouldBomb(Contract("ed","Unknown"))
}

testNth <- function() {
    this <- Contract("ed","Comdty")
    rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5)) 
    checkEquals(this$nth("ticker",1,rollObj,"2006-09-10"),"ed200609")
    checkEquals(this$nth("ticker",1,rollObj,"2006-09-11"),"ed200612")
    checkEquals(this$nth("ticker",1,rollObj,"2006-09-12"),"ed200612")          
}

test.ContractBloombergCloses <- function(){
	this <- Contract('fv', 'Comdty')
	checkEquals(110.484375, as.numeric(this$bloombergCloses(9,2008,start = '2008-07-18', end = '2008-07-18')))
	checkEquals(103.390625, as.numeric(this$bloombergCloses(9,2006,start = '2006-07-18', end = '2006-07-18')))
	checkEquals(109.703125, as.numeric(this$bloombergCloses(9,1998,start = '1998-07-20', end = '1998-07-20')))
}

test.Contract.equals <- function(){
	a <- Contract('ty','Index')
	b <- Contract('ty', 'Comdty')
	shouldBombMatching(checkSame(a,b), "Error:  Contract" )
	
	a <- Contract('us', 'Comdty')
	b <- Contract('ty', 'Comdty')
	shouldBombMatching(checkSame(a,b), "Error:  Contract")
	
	a <- Contract('ty','Comdty')
	b <- Contract('ty','Comdty')
	checkSame(a,b)
}

test.loadRawData <- function(){
	this <- Contract("ed","Comdty") 
	result <- this$loadRawData("2008-03-15","2008-04-01","internal",NULL)
	checkShape(result,11,41)
	shouldBomb(this$loadRawData(TRUE,"2008-04-01","internal",NULL))
	shouldBomb(this$loadRawData("2008-03-15",TRUE,"internal",NULL))
	shouldBomb(this$loadRawData("2008-03-15","2008-04-01","junk",NULL))
	shouldBomb(this$loadRawData("2008-03-15","2008-04-01","internal",TRUE))
	# rawData given as an input
	rawData <- this$loadRawData("2008-05-01","2008-06-01","internal",NULL)
	result <- this$loadRawData(rawData = rawData)
	checkSame(rawData,this$.rawData)
	# no data to load
	shouldBomb(this$loadRawData(startDate = "2005-01-01",endDate = "2005-01-01"))
	# container tests
	result <- this$loadRawData('2008-01-15','2008-02-19','test',container=squish(testdataPath,'multipleTimeSeries.csv'))
	checkSame(c('ed200809','ed200812'), colnames(result))
	checkSame(1851.67, sum(na.omit(result[,'ed200812'])))
	checkSame(2340.395, sum(na.omit(result[,'ed200809'])))
	# data load with time stamps
	this <- Contract("es","Index")
	result <- this$loadRawData(startDate = '2008-08-01',endDate = '2008-09-01',timeStampFilter = '16:15:00',dataSource = 'bloomberg')
	checkSame(filter.zoo.byTime(result,'16:15:00'),result)
	checkSame(NROW(filter.zoo.byTime(result,'16:15:00')),NROW(result))
}

test.frontContract <- function(){
	roll <- Roll(function(x) daysToExpiry(x,5))
	dates <- as.POSIXct(c('2008-10-29','2000-03-09','2005-06-01'))
	expected <- zoo(c('ed200812','ed200006','ed200506'), dates)
	checkSameLooking(expected, Contract('ed','Comdty')$frontContracts(dates,roll)) 
}

test.frontYearMonths <- function(){
	roll <- Roll(function(x) daysToExpiry(x,5))
	dates <- as.POSIXct(c('2008-10-29','2000-03-09','2005-06-01'))
	expected <- zoo(c(200812,200006,200506), dates)
	checkSameLooking(expected, Contract('ed','Comdty')$frontYearMonths(dates,roll)) 
}

test.names <- function(){
	checkSame(Contract$continuousName('es'),'ES.1C')
	checkSame(Contract$gfutContinuousName('es'),'ES.GFUT.1C')
	checkSame(Contract$tsContinuousName('es'),'ES.TS.1C')	
	contract <- Contract('es','Index')
	checkSame(contract$tsContinuousName(),'ES.TS.1C')
	checkSame(contract$gfutContinuousName(),'ES.GFUT.1C')
	checkSame(contract$tsContinuousName(),'ES.TS.1C')	
}

test.bloombergContinuousSecurity <- function(){
	contract <- Contract('es','Index')
	checkSame(contract$bloombergContinuousSecurity(),'ES1 COMB Index')
	checkSame(Contract$bloombergContinuousSecurity('es'),'ES1 COMB Index')
	checkSame(NROW(Contract$bloombergContinuousSecurity('junk')),0)
}

test.nameFromMarket <- function(){	
	checkSame(Contract$nameFromMarket('AD.1C'),'ad')
	checkSame(Contract$nameFromMarket('AD.GFUT.1C'),'ad')
}

test.yellowKeys <- function(){
	assert('Comdty' %in% Contract$yellowKeys())
}