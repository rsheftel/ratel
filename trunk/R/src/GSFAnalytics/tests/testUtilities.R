## Test file for the Utilities object
library("GSFAnalytics")

test.getFincadDateAdjust <- function()
{
  financialCenter <- "nyb"
  holidays <- HolidayDataLoader()
  holidayList <- holidays$getHolidays("financialcalendar",financialCenter)
  
  initialDate <- "2007/08/13"
  shouldBomb(getFincadDateAdjust("whatever","d",3,holidayList))
  shouldBomb(getFincadDateAdjust(initialDate,"blog",3,holidayList))
  shouldBomb(getFincadDateAdjust(initialDate,"d","yes",holidayList))
  shouldBomb(getFincadDateAdjust(initialDate,"d",3,"I like Holidays"))
  checkAdjust <- function(start, end, units, num, reverse = FALSE) {
    checkSame(as.POSIXct(end),getFincadDateAdjust(start,units,num,holidayList))
    if(reverse)
        checkSame(as.POSIXct(start),getFincadDateAdjust(end,units,-num,holidayList))
  }
  checkAdjust(initialDate, "2007/08/16", "d", 3, TRUE)
  checkAdjust("2007/08/31", "2007/09/06", "d", 3, TRUE)
  checkAdjust("2007/08/03", "2007/09/04", "m", 1, TRUE)
  checkAdjust("2007/08/20", "2007/09/04", "w", 2)
  checkAdjust("2006/12/29", "2008/01/02", "d", 252, TRUE)
  checkAdjust("2006/12/29", "2007/12/31", "y", 1)
  checkAdjust("2007/01/01", "2008/01/02", "y", 1)
  checkSame(as.POSIXct("2007/09/05"),getFincadDateAdjust("2007/08/31","d",3,NULL))
}

test.getDaysBetween <- function() {
    startDate <- "2007/07/13"
    endDate <- "2008/02/23"
    shouldBomb(getDaysBetween("BusinessTime",endDate))
    shouldBomb(getDaysBetween(startDate,"BusinessTime"))
    checkEquals(round(getDaysBetween(startDate,endDate),0),225)
}

test.getBusinessDaysBetween <- function(){
    checkSame(21, getBusinessDaysBetween("2007-12-01", "2008-01-02", "nyb"))
    checkSame(-21, getBusinessDaysBetween("2008-01-02","2007-12-01","nyb"))
    checkSame(0,getBusinessDaysBetween("2008-01-02","2008-01-02","nyb"))    
}



test.getSystemDBFXMarketName <- function()
{
  fxCurr <- FXCurr$setByCross("usd/jpy")
  
  shouldBomb(getSystemDBFXMarketName())
  shouldBomb(getSystemDBFXMarketName(fxCurr = "badmojo",tenor="1y",putCall="call",SystemNumber=1))
#  shouldBomb(getSystemDBFXMarketName(fxCurr = fxCurr,tenor="badmojo",putCall="call",SystemNumber=1))
  shouldBomb(getSystemDBFXMarketName(fxCurr = fxCurr,tenor="1y",putCall="businessTime",SystemNumber=1))
  shouldBomb(getSystemDBFXMarketName(fxCurr = fxCurr,tenor="1y",putCall="call",SystemNumber=-1))
  
  checkSame(getSystemDBFXMarketName(fxCurr = fxCurr,tenor="6m",putCall="call",SystemNumber=1),"USDJPY6MTRI.C1")
  checkSame(getSystemDBFXMarketName(fxCurr = fxCurr,tenor="1y",putCall="put",SystemNumber=1),"USDJPY1YTRI.P1")
  
}

test.getELDateFromRDate <- function()
{
  shouldBomb(getELDateFromRDate())
  shouldBomb(getELDateFromRDate(dates  = TRUE,timeStamps = 0))  
  shouldBomb(getELDateFromRDate(dates  = "2007-07-07",timeStamps = TRUE))
  
  checkEquals(getELDateFromRDate(dates  = "2007-07-07",timeStamps = 0),1070707)
  checkEquals(getELDateFromRDate(dates  = "2007-07-07",timeStamps = 1600),1070707.16)
  checkEquals(getELDateFromRDate(dates  = c("2007-07-07","2007-07-08"),timeStamps = 1600),c(1070707.16,1070708.16))
  checkEquals(getELDateFromRDate(dates  = c("2007-07-07","2007-07-08"),timeStamps = c(0,1600)),c(1070707,1070708.16))
}  

test.getELDateFromRDate <- function()
{
  shouldBomb(getELDateFromRDate())
  shouldBomb(getELDateFromRDate(dates  = TRUE))
  
  checkEquals(getELDateFromRDate(dates  = "2007-07-07"),1070707)
  checkEquals(getELDateFromRDate(dates  = "2007-07-07 16:00:00"),1070707.16)
  checkEquals(getELDateFromRDate(dates  = c("2007-07-07 16:00:00","2007-07-08 16:00:00")),c(1070707.16,1070708.16))
  checkEquals(getELDateFromRDate(dates  = c("2007-07-07 00:00:00","2007-07-08 16:00:00")),c(1070707,1070708.16))
  checkEquals(getELDateFromRDate(dates  = c("2007-07-07","2007-07-08")),c(1070707,1070708))
}  

test.getELTimeFromPOSIXlt <- function()
{
  shouldBomb(getELTimeFromPOSIXlt())
  shouldBomb(getELTimeFromPOSIXlt(date  = TRUE))  
  
  checkEquals(getELTimeFromPOSIXlt(date  = as.POSIXlt("2007-07-07 15:00:00")),1500)
  checkEquals(getELTimeFromPOSIXlt(date  = "2007-07-07 15:00:00"),1500)
  checkEquals(getELTimeFromPOSIXlt(date  = "2007-07-07 01:00:00"),100)  
  checkEquals(getELTimeFromPOSIXlt(date  = "2007-07-07 16:00:00"),1600)  
  checkEquals(getELTimeFromPOSIXlt(date  = "2007-07-07"),0)
  checkEquals(getELTimeFromPOSIXlt(date  = c("2007-07-07","2007-07-08")),c(0,0))
  checkEquals(getELTimeFromPOSIXlt(date  = c("2007-07-07 16:00:00","2007-07-08 05:00:00")),c(1600,500))  
}  

testGetHistoricalVolatility <- function(){
    
    triExample <- zoo(1:70)
    
    shouldBomb(getHistoricalVolatility())
    shouldBomb(getHistoricalVolatility(as.numeric(triExample)))
    shouldBomb(getHistoricalVolatility(-triExample))
    shouldBomb(getHistoricalVolatility(triExample,window = 0))
    shouldBomb(getHistoricalVolatility(triExample,dt = 0))
    shouldBomb(getHistoricalVolatility(triExample,method = "not_implemented"))
       
    # test standard_deviation
    
    result <- getHistoricalVolatility(triExample, 60, 1/252,"standard_deviation")     
    checkEquals(0.3105343,last(result))
    
    result <- getHistoricalVolatility(triExample[1:60], 60, 1/252,"standard_deviation") 
    checkEquals(NULL,result)
    result <- getHistoricalVolatility(triExample[1:61], 60, 1/252,"standard_deviation") 
    checkEquals(1.703155421,last(result))
    
    # test EWMA
 
	result <- getHistoricalVolatility(triExample, 60, 1/252,"EWMA") 
	checkEquals(7.475219715,last(result))
	
	triExample <- zoo(rep(-5:5, 8))
	shouldBombMatching(getHistoricalVolatility(triExample, 60, 1/252, 'standard_deviation'), 'The TRI series should always be positive')
	result <- getHistoricalVolatility(triExample, 60, 1, 'standard_deviation', logChanges = FALSE)
	checkEquals(3.065895874638324, last(result))
	
	result <- getHistoricalVolatility(triExample, 60, 1, 'EWMA', logChanges = FALSE)
	checkEquals(2.449786723182203, last(result))
}

testGetEWMA <- function(){  
    shouldBomb(getEWMA())
    shouldBomb(getEWMA(c(1,5,"SD")))
    shouldBomb(getEWMA(c(1,5),0))

    checkEquals(getEWMA(c(1)),1)
    checkEquals(getEWMA(c(1,2,3,2),60),c(1,1.011485979647104,1.034326011212858,1.045417722993805))
}

testGetZScore <- function(){
    zooObj <- zoo(c(1,2,3,4),c("2007-01-01","2007-01-02","2007-01-03","2007-01-04"))
    checkEquals(
        getZScore(zooObj),
        zoo(c(-1.1618950038622251,-0.3872983346207417,0.3872983346207417,1.1618950038622251),c("2007-01-01","2007-01-02","2007-01-03","2007-01-04"))
    )
}

test.parseSimpleTenor <- function() {
  shouldBomb(parseSimpleTenor("No Way"))
  shouldBomb(parseSimpleTenor("1.5m"))
  
  test.list <- parseSimpleTenor("spot")
  checkSame(test.list$numUnits,0)
  checkSame(test.list$unit,"d")
  
  test.list <- parseSimpleTenor("18m")
  checkSame(test.list$numUnits,18)
  checkSame(test.list$unit,"m")
}


test.getSubList <- function()
{
    DEACTIVATED("CDS_TICKER_UNIVERSE")
    library(QFCredit)
    tickerList <- CDSDataLoader$getScrubbedNames()
    shouldBomb(getSubList())
    shouldBomb(getSubList(tickerList,0,1))
    shouldBomb(getSubList(tickerList,2,0))
    shouldBomb(getSubList(tickerList,2,4))    

    a = getSubList(tickerList[1:100],4,1)
    b = getSubList(tickerList[1:100],4,2)
    c = getSubList(tickerList[1:100],4,3)
    d = getSubList(tickerList[1:100],4,4)

    checkEquals(c(a,b,c,d),tickerList[1:100])
    checkEquals(a,tickerList[1:25])
    checkEquals(b,tickerList[26:50])
    checkEquals(c,tickerList[51:75])
    checkEquals(d,tickerList[76:100])
    
    a = getSubList(tickerList[1:105],4,1)
    b = getSubList(tickerList[1:105],4,2)
    c = getSubList(tickerList[1:105],4,3)
    d = getSubList(tickerList[1:105],4,4)
       
    checkEquals(c(a,b,c,d),tickerList[1:105])
    checkEquals(a,tickerList[1:26])
    checkEquals(b,tickerList[27:52])
    checkEquals(c,tickerList[53:78])
    checkEquals(d,tickerList[79:105])
}

test.removeCommasFromDataFrame <- function()
{
	shouldBomb(removeCommasFromDataFrame('junk'))
	shouldBomb(removeCommasFromDataFrame(1))
	
	
	testData <- data.frame(col1 = c('497,190,785','3.73'), col2 = c('-100,000.100','2.11'), row.names = c('a','b'))
	output <- removeCommasFromDataFrame(testData, returnAsNumeric = TRUE)
	target <- data.frame(col1 = c(497190785, 3.73), col2 = c(-100000.100,2.11), stringsAsFactors = FALSE, row.names = c('a','b'))
	checkSame(output, target)
	
	testData <- data.frame(col1 = c('497,190,785','3.73'), col2 = c('-100,000.100','2.11'), row.names = c('a','b'))
	output <- removeCommasFromDataFrame(testData, returnAsNumeric = FALSE)
	target <- data.frame(col1 = c('497190785', '3.73'), col2 = c('-100000.100','2.11'), stringsAsFactors = FALSE, row.names = c('a','b'))
	checkSame(output, target)
}

test.mergeDataFrames <- function()
{
	res <- mergeDataFrames(data.frame(a = '1'),b = data.frame(a = '5'))
	checkSame(res,data.frame(a = c('1','5')))
	res <- mergeDataFrames(data.frame(a = '1',b= 5),b = data.frame(a = '5'))
	checkSame(res,data.frame(a = c('1','5'),b = c(5,NA)))
	res <- mergeDataFrames(data.frame(a = '1'),b = data.frame(a = '5',b = 5))
	checkSame(res,data.frame(a = c('1','5'),b = c(NA,5)))
	res <- mergeDataFrames(data.frame(a = '1',e = 8),b = data.frame(a = '5',b = 5))
	checkSame(res,data.frame(a = c('1','5'),e = c(8,NA),b = c(NA,5)))
	res <- mergeDataFrames(data.frame(e = 8),b = data.frame(b = 5))
	checkSame(res,data.frame(e = c(8,NA),b = c(NA,5)))
	res <- mergeDataFrames(data.frame(a = '1',e = 8,k = TRUE),b = data.frame(a = '5',b = 5))
	checkSame(res,data.frame(a = c('1','5'),e = c(8,NA),k = c(TRUE,NA),b = c(NA,5)))
	shouldBomb(mergeDataFrames(1,data.frame(a = 5)))
	shouldBomb(mergeDataFrames(data.frame(a = 5),5))
	res <- mergeDataFrames(data.frame(a = c('1','5'),e = 7:8,k = c(TRUE,FALSE)),b = data.frame(a = '5',b = 5))
	checkSame(res,data.frame(a = c('1','5','5'),e = c(7,8,NA),k = c(TRUE,FALSE,NA),b = c(NA,NA,5)))	
	res <- mergeDataFrames(data.frame(a = c('1','5'),e = 7:8,k = c(TRUE,FALSE),stringsAsFactors = FALSE),b = data.frame(a = c('5','10'),b = 5:6,stringsAsFactors = FALSE))
	checkSame(res,data.frame(a = c('1','5','5','10'),e = c(7,8,NA,NA),k = c(TRUE,FALSE,NA,NA),b = c(NA,NA,5,6),stringsAsFactors = FALSE))
	res <- mergeDataFrames(data.frame(a = '5',b = 5,stringsAsFactors = FALSE),data.frame(a = c('1','5'),e = 7:8,k = c(TRUE,FALSE),stringsAsFactors = FALSE))
	checkSame(res,data.frame(a = c('5','1','5'),b = c(5,NA,NA),e = c(NA,7,8),k = c(NA,TRUE,FALSE),stringsAsFactors = FALSE))
}



testAddSlashToDirectory <- function() {
	shouldBomb(addSlashToDirectory(1))
	checkSame(addSlashToDirectory('c:'), 'c:/')
	checkSame(addSlashToDirectory('c:/'), 'c:/')
}