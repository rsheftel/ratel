library("GSFCore")

aapl.test <- TimeSeriesDB()$retrieveOneTimeSeriesByName("aapl close", "yahoo", start = "1999-01-01", end = "1999-02-1")

getTestZoo <- function(){
	z <- zoo(c(100.5,100,101.5,105,102,103,104),as.POSIXct(
		c(
				'2009-01-01 14:00:00','2009-01-01 14:30:00','2009-01-01 15:00:00','2009-01-01 17:00:00',
				'2009-01-03 09:00:00','2009-01-03 15:00:00','2009-01-03 15:01:01')
		)
	)
}

testFilter.zoo.byTimeRange <- function() {	
	checkFunc <- function(z){
		checkSame(z,filter.zoo.byTimeRange(z))
		checkSame(z[-c(4,5)],filter.zoo.byTimeRange(z,timeEnd = '11:00:00'))
		checkSame(z[-5],filter.zoo.byTimeRange(z,timeEnd = '12:00:01'))
		checkSame(z[-c(1,2,5)],filter.zoo.byTimeRange(z,timeStart = '10:01:00',timeEnd = '12:00:01'))
		checkSame(z[5],filter.zoo.byTimeRange(z,timeStart = '12:00:01'))
		checkSame(filter.zoo.byTimeRange(z,timeStart = '16:00:00'),NULL)
	}
	z <- zoo(1:5,as.POSIXct(c('2009-01-01 10:00:00','2009-01-01 10:00:05','2009-01-01 11:00:00','2009-01-02 12:00:00','2009-01-03 15:00:00')))
	checkFunc(z)
	z <- strip.times.zoo(z)
	checkSame(z,filter.zoo.byTimeRange(z))
	index(z) <- c('2009-01-01 10:00:00','2009-01-01 10:00:05','2009-01-01 11:00:00','2009-01-02 12:00:00','2009-01-03 15:00:00')
	checkFunc(z)
	
	# Two columns zoo
	zz <- merge(z,z+1)
	checkSame(zz[-5,],filter.zoo.byTimeRange(zz,timeEnd = '12:00:01'))
}

testDaily.zoo.forTime <- function() {
	z <- getTestZoo()
	res <- daily.zoo.forTime(z,'15:00:00')
	target <- zoo(c(0,3.5,0.5,0,1),
		as.POSIXct(c('2009-01-01 15:00:00','2009-01-01 17:00:00','2009-01-03 09:00:00','2009-01-03 15:00:00','2009-01-03 15:01:01'))
	)	
	checkSame(res,target)	
	shouldBomb(daily.zoo.forTime(merge(z,z),'15:00:00'))
	checkSame(daily.zoo.forTime(z[1],'15:00:00'),NULL)
	checkSame(daily.zoo.forTime(z[1:2],'15:00:00'),NULL)
	checkSame(daily.zoo.forTime(z[3],'15:00:00'),zoo(0,as.POSIXct('2009-01-01 15:00:00')))
	checkSame(daily.zoo.forTime(z[3:4],'15:00:00'),zoo(c(0,3.5),as.POSIXct(c('2009-01-01 15:00:00','2009-01-01 17:00:00'))))
	
	# No time stamp zoo	
	checkSame(NULL,daily.zoo.forTime(make.zoo.daily(z,'15:00:00'),'15:00:00'))
}

testFilter.zoo.byDate <- function() {
	z <- getTestZoo()
	checkSame(filter.zoo.byDate(z,'2009-01-01'),z[1:4])
	checkSame(filter.zoo.byDate(z,as.POSIXct('2009-01-01')),z[1:4])
	checkSame(filter.zoo.byDate(z,'2009-01-03'),z[5:7])
	checkSame(NROW(filter.zoo.byDate(z,'2009-01-02')),0)
}

testFilterRemovesSomeValues <- function() {
    aapl <- aapl.test
    index(aapl)[1:10] <- index(aapl)[1:10] + 3600

    aapl.filter <- filter.zoo.byTime(aapl, "15:00:00")
    checkEquals(aapl[1:10, drop=FALSE], aapl.filter)

}

testSumZoos <- function() { 
    a <- zoo(1:3)
    b <- zoo(4:6)
    checkSame(zoo(c(5, 7, 9)), sumZoos(list(a, b)))
    checkSame(zoo(c(10, 14, 18)), sumZoos(list(a, b, a, b)))

}

testFilterMaintainsZooStructure <- function() { 
    zoo.frame = zoo(data.frame(1), as.POSIXct("2005-01-01 15:00:00"))
    zoo.filtered = filter.zoo.byTime(zoo.frame, "15:00:00")
    checkEquals(zoo.filtered, zoo.frame)
}

testFilterFailsWithoutTZ <- function() {
    DEACTIVATED("time zone functionality is currently disabled.")
    shouldBomb(filter.zoo.byTime(aapl, "12:00:00"))
    shouldBomb(filter.zoo.byTime("12:00:00", "JST-9"))
    shouldBomb(filter.zoo.byTime(aapl, "12:00:00", "JST"))
}

testFilterByTimeZone <- function() {
    DEACTIVATED("time zone functionality is currently disabled.")
    aapl <- aapl.test[1:15]

    dates <- c("01-01", "01-02", "01-03", "07-01", "07-02")
    dates <- paste("2007-", dates, " 12:00:00", sep="")
    dates <- convert.timeZone(c(as.POSIXct(dates, "GMT"), as.POSIXct(dates, "EST5EDT"), as.POSIXct(dates, "JST-9")), "CST5CDT")
    index(aapl) <- dates

    ldn.noon <- filter.zoo.byTime(aapl, "12:00:00", "GMT")
    checkLength(ldn.noon, 5)
    checkEquals(timeZone(ldn.noon), "CST5CDT")
    nyc.noon <- filter.zoo.byTime(aapl, "12:00:00", "EST5EDT")
    checkLength(nyc.noon, 5)
    checkEquals(timeZone(nyc.noon), "CST5CDT")                                                                  
    jpy.noon <- filter.zoo.byTime(aapl, "12:00:00", "JST-9")
    checkLength(jpy.noon, 5)
    checkEquals(timeZone(jpy.noon), "CST5CDT")
}

testMakeZooDaily <- function() {
    aapl <- aapl.test[1:15]
    index(aapl)[1:10] <- index(aapl)[1:10] + 3600

    aapl.daily <- make.zoo.daily(aapl, "15:00:00")
    checkLength(aapl.daily, 10)

    aapl.lt.dates <- as.POSIXlt(index(aapl[1:10]))
    aapl.daily.lt.dates <- as.POSIXlt(index(aapl.daily))
    checkTrue(all(0 == aapl.daily.lt.dates$hour))
    checkTrue(all(0 == aapl.daily.lt.dates$min))
    checkTrue(all(0 == aapl.daily.lt.dates$sec))
    checkTrue(all(aapl.lt.dates$year == aapl.daily.lt.dates$year))
    checkTrue(all(aapl.lt.dates$mon == aapl.daily.lt.dates$mon))
    checkTrue(all(aapl.lt.dates$day == aapl.daily.lt.dates$day))
}

testMakeZooDailyDoesNotModifyZooDimensions <- function() { 
    zooDataFrame <- zoo(data.frame(1),as.POSIXct("2005-01-01 15:00:00"))
    zoo.daily = make.zoo.daily(zooDataFrame, "15:00:00")
    checkEquals(zoo.daily, zoo(data.frame(1), as.POSIXct("2005-01-01")))
}

testFilterByIntersection <- function() {
    aapl <- aapl.test[1:15]
    w <- aapl[c(1,3,5,7,9,11,13,15)]
    x <- aapl[c(1,4,7,10,13)]
    y <- aapl[c(1,5,9,13)]
    z <- aapl[c(1,6,11)]
    n <- aapl[c(2, 8, 12, 14)]

    checkEquals(filter.zoo.byIntersection(w, x, y), aapl[c(1,13)])
    checkEquals(filter.zoo.byIntersection(w, z), aapl[c(1,11)])
    checkEquals(filter.zoo.byIntersection(w, x, y, z), aapl[1])
    checkEquals(filter.zoo.byIntersection(w, y), y)
    checkLength(index(filter.zoo.byIntersection(w, n)), 0)
}

testAllTimeZoneFunctionalityDisabled <- function() {
    shouldBomb(filter.zoo.byTime(aapl.test, "12:00:00", "EST5EDT"))
    shouldBomb(strip.times.zoo(aapl.test, "EST5EDT"))
    shouldBomb(make.zoo.daily(aapl.test, "12:00:00", "EST5EDT"))
    shouldBomb(convert.timeZone(index(aapl.test), "EST5EDT"))
    shouldBomb(convert.timeZone(as.POSIXlt(index(aapl.test)), "EST5EDT"))
}

testMakeZooDailyTimeZone <- function() {
    DEACTIVATED("time zone functionality is currently disabled.")
    aapl <- aapl.test[1:15]

    dates <- c("01-01", "01-02", "01-03", "07-01", "07-02")
    target.dates <- as.POSIXct(paste("2007-", dates, sep=""), "JST-9")
    dates <- paste("2007-", dates, " 12:00:00", sep="")
    dates <- c(as.POSIXct(dates, "GMT"), as.POSIXct(dates, "EST5EDT"), as.POSIXct(dates, "JST-9"))
    index(aapl) <- dates

    jpy.noon <- make.zoo.daily(aapl, "12:00:00", "JST-9")
    checkEquals(timeZone(jpy.noon), "JST-9")
    checkLength(jpy.noon, 5)
    checkEquals(index(jpy.noon), target.dates)
}

testConvertTimeZonePOSIXct <- function() {
    DEACTIVATED("time zone functionality is currently disabled.")
    gmt.dates <- as.POSIXct(c("2007-01-01 12:00:00", "2007-07-01 12:00:00"), "GMT")
    target.dates <- as.POSIXct(c("2007-01-01 07:00:00", "2007-07-01 08:00:00"), "EST5EDT")

    nyc.dates <- convert.timeZone(gmt.dates, "EST5EDT")
    checkEquals(nyc.dates, target.dates)

    checkEquals(convert.timeZone(nyc.dates, "EST5EDT"), nyc.dates)
}

testConvertTimeZonePOSIXlt <- function() {
    DEACTIVATED("time zone functionality is currently disabled.")
    gmt.dates <- as.POSIXlt(c("2007-01-01 12:00:00", "2007-07-01 12:00:00"), "GMT")
    target.dates <- as.POSIXlt(c("2007-01-01 07:00:00", "2007-07-01 08:00:00"), "EST5EDT")

    nyc.dates <- convert.timeZone(gmt.dates, "EST5EDT")
    checkEquals(nyc.dates, target.dates)

    checkEquals(convert.timeZone(nyc.dates, "EST5EDT"), nyc.dates)
}

testStripTimesFromIndex <- function() {
    aapl.noTimes <- strip.times.zoo(aapl.test)
    times <- as.POSIXlt(index(aapl.noTimes))
    checkTrue(all(0 == times$hour))
    checkTrue(all(0 == times$min))
    checkTrue(all(0 == times$sec))
}

testColumn <- function() {
    z <- zoo(data.frame(a = c(1,3,5), b = 2:4), order.by = 3:5)
    z2 <- zoo(data.frame(b = c(1,3,5), a = 2:4), order.by = 3:5)
    checkSame(column(z, "b"), column(z2, "a"))
    checkSame(column(z, 1), column(z2, 1))
    shouldBombMatching(column(z, "c"), "subscript out of bounds")
}

testStripTimesRowNames <- function() {
    aapl <- aapl.test
    rownames(aapl) <- as.character(index(aapl))

    stripped <- strip.times.zoo(aapl)

    checkEquals(rownames(stripped), as.character(index(stripped)))
    checkEquals(rownames(aapl)[1], "1999-01-04 14:00:00")
    checkEquals(rownames(stripped)[1], "1999-01-04")
}

testChangeZooFrequency <- function(){
    zooObj <- zoo(1:3,c("2007-12-30","2007-12-31","2008-01-01"))
    checkEquals(changeZooFrequency(zooObj,"monthly"), zoo(c(2,3),c("2007-12-31","2008-01-01")))
    zooObjDate <- zoo(1:3,as.Date(c("2007-12-30","2007-12-31","2008-01-01")))
    checkEquals(changeZooFrequency(zooObjDate,"monthly"),zoo(c(2,3),as.Date(c("2007-12-31","2008-01-01"))))
    
    zooObj <- zoo(1:4,c("2007-12-30","2007-12-31","2008-01-01","2008-01-02"))
    checkEquals(changeZooFrequency(zooObj,"monthly"), zoo(c(2,4),c("2007-12-31","2008-01-02")))
    
    zooObj <- zoo(1:5,c("2007-12-30","2007-12-31","2008-01-01","2008-01-02","2009-01-01"))
    zooObjDate <- zoo(1:5,as.Date(c("2007-12-30","2007-12-31","2008-01-01","2008-01-02","2009-01-01")))
    checkEquals(changeZooFrequency(zooObj,"yearly"), zoo(c(2,4,5),c("2007-12-31","2008-01-02","2009-01-01")))
    checkEquals(changeZooFrequency(zooObjDate,"yearly"), zoo(c(2,4,5),as.Date(c("2007-12-31","2008-01-02","2009-01-01"))))
    
    zooObj <- zoo(1:5,c("2007-12-30","2007-12-31","2008-01-01","2008-01-02","2009-01-01"))
    zooObjDate <- zoo(1:5,as.Date(c("2007-12-30","2007-12-31","2008-01-01","2008-01-02","2009-01-01")))
    checkEquals(changeZooFrequency(zooObj,"monthly"), zoo(c(2,4,5),c("2007-12-31","2008-01-02","2009-01-01")))
    checkEquals(changeZooFrequency(zooObjDate,"monthly"), zoo(c(2,4,5),as.Date(c("2007-12-31","2008-01-02","2009-01-01"))))

    zooObj <- zoo(1:7,c("2007-12-28","2007-12-29","2007-12-30","2007-12-31","2008-01-01","2008-01-02","2008-01-03"))
    checkEquals(changeZooFrequency(zooObj,"weekly"), zoo(c(1,7),c("2007-12-28","2008-01-03")))
    zooObj <- zoo(1:7,as.Date(c("2007-12-28","2007-12-29","2007-12-30","2007-12-31","2008-01-01","2008-01-02","2008-01-03")))
    checkEquals(changeZooFrequency(zooObj,"weekly"), zoo(c(1,7),as.Date(c("2007-12-28","2008-01-03"))))
    
    checkEquals(changeZooFrequency(zooObj[-1],"weekly"), NULL)
    zooObj <- zoo(1:7,c("2007-12-28","2007-12-29","2007-12-30","2007-12-31","2008-01-01","2008-01-02","2008-01-03","2008-01-04"))
    checkEquals(changeZooFrequency(zooObj,"weekly"), zoo(1,c("2007-12-28","2008-01-04")))

    shouldBomb(changeZooFrequency(TRUE,"monthly"))
    shouldBomb(changeZooFrequency(TRUE,"error"))
    shouldBomb(changeZooFrequency(1,"monthly"))
    shouldBomb(changeZooFrequency(zoo(1,"2008-01-01"),"monthly"))
}

testCheckSameWithZooDataFrame <- function() {
    df1 <- getZooDataFrame(zoo(1, "2001-01-01"))
    df2 <- getZooDataFrame(zoo(1, "2001-01-01"))
    colnames(df2) <- "foobar"
    colnames(df1) <- "zooObj"

    shouldBombMatching(
        checkSame(df1, df2),
        "colnames:zooObj did not match colnames:foobar"
    )
}

test.getZooDataFrame <- function()
{
    shouldBombMatching(getZooDataFrame(), 'argument "zooObj" is missing')
    shouldBombMatching(getZooDataFrame(1), '1 should be a zoo')

    # test dimension = 1

    target <- zoo(1,"2001-01-01")
    target <- zoo(data.frame(target),order.by = as.POSIXct(rownames(data.frame(target))))

    result <- getZooDataFrame(zoo(1,"2001-01-01"))
    colnames(target) <- c()
    colnames(result) <- c()
    checkShape(result, 1, 1, rownames(target), colnames(target))
    checkSame(result,target)    

    # test dimension = 2

    target <- zoo(c(1,2),c("2001-01-01","2001-01-02"))
    target <- zoo(data.frame(target),order.by = as.POSIXct(rownames(data.frame(target))))
    result <- getZooDataFrame(zoo(c(1,2),c("2001-01-01","2001-01-02")))
    colnames(target) <- c()
    colnames(result) <- c()
    checkSame(result,target)    

    # is it preserving column names? (especially "-" characters)

    target <- zoo(1,"2001-01-01")
    target <- zoo(data.frame(target),order.by = as.POSIXct(rownames(data.frame(target))))
    colnames(target) <- "zoo-column"

    checkSame(getZooDataFrame(target),target)    
}

test.merge.zoo.null <- function()
{
    zoo1 <- zoo(1,"2001-01-01")
    zoo2 <- zoo(2,"2001-01-01")   
    zoo.merged <- merge.zoo(zoo1, zoo2)
    checkEquals(zoo1,merge.zoo.null(zoo1,NULL))  
    checkEquals(zoo2,merge.zoo.null(NULL, zoo2))  
    checkEquals(getZooDataFrame(zoo.merged),merge.zoo.null(zoo1,zoo2))  

    shouldBomb(merge.zoo.null(1, zoo1))
    shouldBomb(merge.zoo.null(zoo2, "c"))
    shouldBomb(merge.zoo.null(1, NULL))
}
  
test.exportZooInTradeStationASCII <- function()
{
    badZoo <- zoo(matrix(c(6518500,1532138,25522,12838),nrow = 2,ncol = 2),order.by = c("2005-01-03","2005-01-04"))
    goodZoo <- zoo(c(25522,12838),order.by = c("2005-01-03","2005-01-04"))

    shouldBomb(exportZooInTradeStationASCII())
    shouldBomb(exportZooInTradeStationASCII(zooObj = TRUE,filePath = filePath))
    shouldBomb(exportZooInTradeStationASCII(zooObj = badZoo,filePath = filePath))
    shouldBomb(exportZooInTradeStationASCII(zooObj = goodZoo,filePath = FALSE))

    exportZooInTradeStationASCII(zooObj = goodZoo,filePath = textConnection("testData", open = "w", local = TRUE))
    
    table <- read.csv(textConnection(testData))
    
    checkEquals(table,
	data.frame(
	    Date = index(goodZoo),
            Open = c(25522,12838),
            High = c(25522,12838),
            Low = c(25522,12838),
            Close = c(25522,12838)
        )
    )
    rm(testData)
}

test.zooForDates <- function()
{
	earlyZoo <- zoo(c(1,2,3,4,5), order.by = as.POSIXct(c('2008-01-01','2008-01-02','2008-01-03','2008-01-04','2008-01-05')))
	lateZoo  <- zoo(c(6,7,8,9,10), order.by = as.POSIXct(c('2008-01-04','2008-01-05','2008-01-06','2008-01-07','2008-01-08')))
	
	lateDates <- index(lateZoo)
	
	checkSameLooking(zooForDates(earlyZoo,lateDates), zoo(c(4,5), as.POSIXct(c('2008-01-04','2008-01-05'))))
	
	outerZoo <- zoo(c(1,2,3,4,5,6,7,8), order.by = as.POSIXct(c('2007-12-20','2007-12-30','2008-01-01','2008-01-02','2008-01-05','2008-01-06','2008-01-07','2008-01-08','2008-01-09')))
			
	checkSameLooking(zooForDates(outerZoo, index(earlyZoo)), zoo(c(3,4,5), order.by=as.POSIXct(c('2008-01-01','2008-01-02','2008-01-05'))))
	checkSameLooking(zooForDates(earlyZoo, index(outerZoo),remove.na=FALSE), zoo(c(NA,NA,1,2,5,NA,NA,NA,NA),index(outerZoo)))		
}


test.importZooFromTradeStationFile <- function()
{
    fileName <- system.file("testdata","test.importZooFromTradeStationFile.csv", package = "GSFCore")

    shouldBomb(importZooFromTradeStationFile())
    shouldBomb(importZooFromTradeStationFile(TRUE))

    result <- importZooFromTradeStationFile(fileName)
    checkShape(result,rows = 5,cols = 6,colnames = c("Open","High","Low","Close","Up","Down"))
}

test.chronToPosixZoo <- function()
{
    #z <- zoo(1,chron("02/27/92"))
    #checkSame(chronToPosixZoo(z),zoo(1,as.POSIXct("1992-02-27")))
    #z <- zoo(1,chron("02/27/92","12:45:00"))
    #checkSame(chronToPosixZoo(z),zoo(1,as.POSIXct("1992-02-27 12:45:00")))
    #shouldBomb(chronToPosixZoo(zoo(1,"2008-01-01")))
    #shouldBomb(chronToPosixZoo(zoo(1,as.POSIXct("2008-01-01"))))    
}

test.indiciesOverlap <- function(){
	
	dates.1 <- as.POSIXct(c('2001-01-01','2001-01-02','2001-01-03'))
	zoo.1 <- zoo(c(1:3),dates.1)
	
	dates.2 <- as.POSIXct(c('2001-02-01','2001-02-02','2001-02-03'))
	zoo.2 <- zoo(c(1:3),dates.2)
	
	checkSame(FALSE,indiciesOverlap(zoo.1,zoo.2))
	
	dates.3 <- as.POSIXct(c('2001-01-03','2001-04-02','2001-05-03'))
	zoo.3 <- zoo(c(1:3),dates.3)
	
	checkSame(TRUE,indiciesOverlap(zoo.1,zoo.3)) 
}

test.stitchZoos <- function(){
	
	dates.1 <- as.POSIXct(paste('2000','01',c(1:30),sep="-"))
	zoo.1 <- zoo(1:30,dates.1)

	dates.2 <- as.POSIXct(paste('2000','02',c(1:20),sep="-"))
	zoo.2 <- zoo(1:20,dates.2)
	
	shouldBomb(stitchZoos(zoo.1))
	checkSame('#Error : Must provide at least 2 zoos', stitchZoos(list(zoo.1)))
	checkSame(rbind(zoo.1,zoo.2), stitchZoos(list(zoo.1,zoo.2),backAdjusted=FALSE,overlapRule='notAllowed')$stitchedZoo)
	checkSame(rbind(zoo.1,(zoo.2+29)), stitchZoos(list(zoo.1,zoo.2),backAdjusted=TRUE,overlapRule='notAllowed')$stitchedZoo)
	
	dates.3 <- as.POSIXct(paste('2000','01',c(15:20),sep="-"))
	zoo.3 <- zoo(100:105,dates.3)
	
	checkSame('#Error : Zoos cannot have overlapping dates', stitchZoos(list(zoo.1,zoo.3),overlapRule='notAllowed'))
	
	checkSameLooking(zoo.1,stitchZoos(list(zoo.1,zoo.3),overlapRule='usePrior')$stitchedZoo)
	
	dates.4 <- as.POSIXct(paste('2000','02',c(10:28),sep="-"))
	zoo.4 <- zoo(c(100:118),dates.4)
	
	dates.5 <- as.POSIXct(paste('2000','02',c(1:28),sep="-"))
	zoo.5 <- zoo(c(1:28),dates.5)
	checkSameLooking(zoo.5, stitchZoos(list(zoo.2,zoo.4),overlapRule='usePrior',backAdjusted=TRUE)$stitchedZoo)
	checkSameLooking(rbind(zoo.1,(zoo.2+29)), stitchZoos(list(zoo.1,zoo.2),overlapRule='usePrior',backAdjusted=TRUE)$stitchedZoo)
	
	dates.6 <- as.POSIXct(paste('2000','01',c(1:20),sep="-"))
	zoo.6 <- zoo(c(86:105),dates.6)
	
	checkSameLooking(zoo.6, stitchZoos(list(zoo.1,zoo.3),overlapRule='useLatter',backAdjust=TRUE)$stitchedZoo)
	checkSameLooking(rbind((zoo.1-29),zoo.2), stitchZoos(list(zoo.1,zoo.2),overlapRule='useLatter',backAdjust=TRUE)$stitchedZoo)
	
}

test.is.empty <- function(){
	shouldBomb(is.empty(), 'is missing, with no default')
	testZoo <- zoo(c(1,2), c('2008-10-02','2008-10-03'))
	checkSame(FALSE, is.empty(testZoo))
	checkSame(TRUE, is.empty(testZoo['2008-10-01',]))
	checkSame(TRUE, is.empty(NULL))
}

test.setZooTimes <- function(){
	startZoo <- zoo(1:10,as.POSIXct(paste('2008','01',11:20,sep="-")))
	compareZoo <- zoo(1:10,as.POSIXct(paste(paste('2008','01',11:20,sep="-"),"15:00:00")))
	checkSame(compareZoo, setZooTimes(startZoo))
	checkSame(compareZoo, setZooTimes(startZoo, hour=15))
	
	compareZoo <- zoo(1:10,as.POSIXct(paste(paste('2008','01',11:20,sep="-"),"10:12:13")))
	checkSame(compareZoo, setZooTimes(startZoo, hour=10, minute=12, second=13))
}


zTest <- zoo(1:5, c('2008-10-02','2008-10-03','2008-10-04','2008-10-05','2008-10-06'))
test.firstN <- function(){
	checkSame(firstN(zTest,1),zoo(1,'2008-10-02'))
	checkSame(firstN(zTest,2),zoo(1:2,c('2008-10-02','2008-10-03')))
	checkSame(firstN(zTest,5),zTest)
	shouldBomb(firstN(zTest,0))
	shouldBomb(firstN(zTest,6))
	shouldBomb(firstN(1:5,1))
	shouldBomb(firstN(zTest,"SD"))
}
test.lastN <- function(){
	checkSame(lastN(zTest,1),zoo(5,'2008-10-06'))
	checkSame(lastN(zTest,2),zoo(4:5,c('2008-10-05','2008-10-06')))
	checkSame(lastN(zTest,5),zTest)
	shouldBomb(lastN(zTest,0))
	shouldBomb(lastN(zTest,6))
	shouldBomb(lastN(1:5,1))
	shouldBomb(lastN(zTest,"SD"))
}
