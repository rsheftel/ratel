library(QFFutures)

testListConstructor <- function() {
    contract <- Contract("ed","Comdty")
    this <- List(contract)
    checkEquals(Map("POSIXt", "data.frame"),this$.lookup)
    checkEquals(class(contract),class(this$.contract))
}

testListLookupFunctions <- function() {
    this <- List(Contract("ed","Comdty"))
    this$addToLookupIfNeeded("2008-01-01",data.frame("test"))
    checkEquals(data.frame("test"),this$retrieveList("2008-01-01"))
	checkEquals(class(this$.bloombergDateCutOff),c("POSIXt","POSIXct"))
	checkSame(as.POSIXlt(this$.bloombergDateCutOff)$mday,1)
    checkEquals(NULL,this$retrieveList("2008-01-02"))
    shouldBomb(this$retrieveList("junk"))   
}

testRawList <- function() {
    this <- List(Contract("ed","Comdty",numCycles = 2))
    result <- this$raw("2006-07-02")
    checkShape(result,8,6,colnames = c("ticker","bbrg","futureYear","futureMonth","monthCode","expiryDate"))
    checkEquals(result,this$raw("2006-09-18"))
    result <- this$raw("2006-09-19")
    checkShape(result,8,6,colnames = c("ticker","bbrg","futureYear","futureMonth","monthCode","expiryDate"))
    result <- this$raw("2006-09-19")
    checkShape(result,8,6,colnames = c("ticker","bbrg","futureYear","futureMonth","monthCode","expiryDate"))
    this <- List(Contract("ed","Comdty",numCycles = 1))
    result <- this$raw("2006-07-02")
    checkShape(result,4,6,colnames = c("ticker","bbrg","futureYear","futureMonth","monthCode","expiryDate"))        
    checkEquals(result,this$raw("2006-07-02"))
    checkEquals(this$raw("2006-07-02 15:00:00"),this$raw("2006-07-02"))
    checkEquals(this$raw(as.POSIXct("2006-07-02 15:00:00")),this$raw("2006-07-02"))
    checkEquals(this$raw(as.POSIXct("2006-07-02")),this$raw("2006-07-02"))
	
	# Test different monthly cycle
	this <- List(Contract("cl","Comdty",numCycles = 1,monthlyCycle =  1:12))
	resList <- this$raw("2008-11-22")
	checkShape(resList,12,6,colnames = c("ticker","bbrg","futureYear","futureMonth","monthCode","expiryDate"))
	checkSame(resList[1,'expiryDate'],'2008-12-19')
	checkSame(resList[12,'expiryDate'],'2009-11-20')
	checkSame(resList[1,'bbrg'],'CLF9')
	checkSame(resList[12,'bbrg'],'CLZ9')
}

testAdjustedList <- function() {
    contract <- Contract("ed","Comdty")
    rollObj <- Roll(rollMethod = function(x)daysToExpiry(x,5))     
    this <- List(contract)
    result <- this$adjusted("2006-07-02",rollObj)
    checkShape(result,4,6,colnames = c("ticker","bbrg","futureYear","futureMonth","monthCode","expiryDate"))     
    checkSame(this$adjusted("2006-09-12",rollObj)[1,"expiryDate"],"2006-12-18")    
    checkSame(this$adjusted("2006-09-11",rollObj)[1,"expiryDate"],"2006-12-18")
    checkSame(this$adjusted("2006-09-10",rollObj)[1,"expiryDate"],"2006-09-18")    
    checkSame(this$adjusted(as.POSIXct("2006-09-10"),rollObj)[1,"expiryDate"],"2006-09-18")        
    checkSame(this$adjusted(as.POSIXct("2006-09-10 15:00:00"),rollObj)[1,"expiryDate"],"2006-09-18")            
}
testNextExpiryMonths <- function() {
    this <- List(Contract("ed","Comdty",numCycles = 2))
    target <- as.POSIXct(c("2008-03-01","2008-06-01","2008-09-01","2008-12-01","2009-03-01","2009-06-01","2009-09-01","2009-12-01","2010-03-01","2010-06-01","2010-09-01","2010-12-01"))
    target2 <- as.POSIXct(c("2007-12-01","2008-03-01","2008-06-01","2008-09-01","2008-12-01","2009-03-01","2009-06-01","2009-09-01","2009-12-01","2010-03-01","2010-06-01","2010-09-01"))
    target3 <- as.POSIXct(c("2008-06-01","2008-09-01","2008-12-01","2009-03-01","2009-06-01","2009-09-01","2009-12-01","2010-03-01","2010-06-01","2010-09-01","2010-12-01","2011-03-01"))    
    checkEquals(this$nextExpiryMonths("2008-03-01"),target)
    checkEquals(this$nextExpiryMonths("2008-03-31"),target) 
    checkEquals(this$nextExpiryMonths("2007-12-31"),target2)
    checkEquals(this$nextExpiryMonths("2008-06-01"),target3)   
    shouldBomb(this$nextExpiryMonths(TRUE))
}

testDateFromTicker <- function() {
	checkSame(List$dateFromTicker('cl200801'),as.POSIXct('2008-01-01'))
	checkSame(List$dateFromTicker('cl321200801'),as.POSIXct('2008-01-01'))
}

testTickerFromDate <- function() {	
	checkSame(List$tickerFromDate("ed",'2008-03-01'),'ed200803')
	checkSame(List$tickerFromDate("ed",'2008-03-01 15:00:00'),'ed200803')
	checkSame(List$tickerFromDate("ed",as.POSIXct('2008-03-01')),'ed200803')
}

testBloombergFromYearMonth <- function() {
	checkSame(List$bloombergFromYearMonth("ed",'200803'),'EDH8')
	checkSame(List$bloombergFromYearMonth("ed",'201003'),'EDH0')
	checkSame(List$bloombergFromYearMonth("ed",'201103'),'EDH1')
	checkSame(List$bloombergFromYearMonth("ed",'201106'),'EDM1')
	checkSame(List$bloombergFromYearMonth("ed",'201106'),'EDM1')
}