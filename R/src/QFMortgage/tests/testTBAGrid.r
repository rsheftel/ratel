
library(QFMortgage)

tempDir <- squish(dataDirectory(),'temp_TSDB/')

fileMatches <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="QFMortgage"),'/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))	
	return(checkSame(benchData,testData))
}

test.TBAGrid <- function(){

    tbagrid <- TBAGrid('fncl',c(5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))
    checkInherits(tbagrid,'TBAGrid')
}

test.setSettleDatesFromTSDB <- function(){
  
    tbagrid <- TBAGrid('fncl',c(5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))
    tbagrid$setSettleDatesFromTSDB()
    expected <- as.Date(c("2007-02-12","2007-03-13","2007-04-12","2007-05-14"))
    checkSame(expected,tbagrid$.settleDates)

    #include 9s, should fail should still work
    tbagrid <- TBAGrid('fncl',c(9,5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))
    tbagrid$setSettleDatesFromTSDB()
    expected <- as.Date(c("2007-02-12","2007-03-13","2007-04-12","2007-05-14"))
    checkSame(expected,tbagrid$.settleDates)
}

test.setPricesFromTSDB <- function (){

    tbagrid <- TBAGrid('fncl',c(5,5.5,6,6.5),as.POSIXct('2007-01-10 15:00:00'))
    tbagrid$setPricesFromTSDB()

    #Set up the expected grid
    expected <- c(96.88281,96.85546625,96.83984125,96.83202875)
    expected <- rbind(expected, c(99.07031,99.0156225,98.9843725,98.94921625))
    expected <- rbind(expected, c(100.75,100.6796875,100.6328125,100.578125))
    expected <- rbind(expected, c(101.86719,101.80469,101.77344,101.7187525))
    
    checkSameLooking(round(expected,digits=6),round(tbagrid$.prices,digits=6))

    #include 9s, should still work
    tbagrid <- TBAGrid('fncl',c(9,5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))
    tbagrid$setPricesFromTSDB()

    #Set up the expected grid
    expected <- rep(NA,4)
    expected <- rbind(expected, c(96.88281,96.85546625,96.83984125,96.83202875))
    expected <- rbind(expected, c(99.07031,99.0156225,98.9843725,98.94921625))
    expected <- rbind(expected, c(100.75,100.6796875,100.6328125,100.578125))

    checkSameLooking(round(expected,digits=6),round(tbagrid$.prices,digits=6))
}

test.getNDayForwardPrice <- function() {

    tbagrid <- TBAGrid('fncl',c(5,5.5,9),as.POSIXct('2007-01-10 15:00:00'))
    tbagrid$setPricesFromTSDB()
    tbagrid$setSettleDatesFromTSDB()

    expected <- c(96.87150, 99.04768, NA)
    checkSameLooking(expected, round(tbagrid$getNDayForwardPrice(45),digits=5))
}

test.getCurrentCoupon <- function(){
  
    tbagrid <- TBAGrid('fncl',c(5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))
    tbagrid$setPricesFromTSDB()
    tbagrid$setSettleDatesFromTSDB()
  
    checkSame(5.664336,round(tbagrid$currentCoupon, digits=6))
}

test.setPricesFromMatrix <- function(){

    tbagrid <- TBAGrid('fncl',c(5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))

    #Complete matrix
    priceMatrix <- matrix(c(1,2,3,4,5,6,7,8,9,10,11,12),3,4)
    tbagrid$setPricesFromMatrix(priceMatrix, c(5,5.5,6), c('1n','2n','3n','4n'))
    checkSame(priceMatrix, tbagrid$.prices)
    
    #Mismatch coupon
    priceMatrix <- matrix(c(NA,2,3,NA,5,6,NA,8,9,NA,11,12),3,4)
    tbagrid$setPricesFromMatrix(priceMatrix, c(4,5.5,6), c('1n','2n','3n','4n'))
    checkSame(priceMatrix, tbagrid$.prices)

    #Settle too few
    priceMatrix <- matrix(c(1,2,3,4,5,6,7,8,9,NA,NA,NA),3,4)
    tbagrid$setPricesFromMatrix(priceMatrix, c(5,5.5,6), c('1n','2n','3n'))
    checkSame(priceMatrix, tbagrid$.prices)
    
    #Mismatch settles
    priceMatrix <- matrix(c(NA,NA,NA,4,5,6,7,8,9,10,11,12),3,4)
    tbagrid$setPricesFromMatrix(priceMatrix, c(5,5.5,6), c('5n','2n','3n','4n'))
    checkSame(priceMatrix, tbagrid$.prices)

}

test.setSettleDatesFromMatrix <- function() {

    tbagrid <- TBAGrid('fncl',c(5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))

    #Complete Matrix
    settleMatrix <- matrix(c(20070212,20070212,20070212,20070313,20070313,20070313,20070412,20070412,20070412,20070514,20070514,20070514),3,4)
    tbagrid$setSettleDatesFromMatrix(settleMatrix, c('1n','2n','3n','4n'))
    expected <- c(as.Date('2007-02-12'), as.Date('2007-03-13'), as.Date('2007-04-12'), as.Date('2007-05-14'))
    checkSame(expected, tbagrid$.settleDates)
    
    #Missing Dates
    settleMatrix <- matrix(c(NA,20070212,20070212,20070313,NA,20070313,20070412,20070412,NA,NA,20070514,20070514),3,4)
    tbagrid$setSettleDatesFromMatrix(settleMatrix, c('1n','2n','3n','4n'))
    expected <- c(as.Date('2007-02-12'), as.Date('2007-03-13'), as.Date('2007-04-12'), as.Date('2007-05-14'))
    checkSame(expected, tbagrid$.settleDates)

    #Missmatch settleVector
    settleMatrix <- matrix(c(NA,20070212,20070212,20070313,NA,20070313,20070412,20070412,NA,NA,20070514,20070514),3,4)
    tbagrid$setSettleDatesFromMatrix(settleMatrix, c('1n','2n','3n'))
    expected <- c(as.Date('2007-02-12'), as.Date('2007-03-13'), as.Date('2007-04-12'), NA)
    checkSame(expected, tbagrid$.settleDates)

    #Missmatch settleVector
    settleMatrix <- matrix(c(NA,20070212,20070212,20070313,NA,20070313,20070412,20070412,NA,NA,20070514,20070514),3,4)
    tbagrid$setSettleDatesFromMatrix(settleMatrix, c('1n','2n','5n','4n'))
    expected <- c(as.Date('2007-02-12'), as.Date('2007-03-13'), NA, as.Date('2007-05-14'))
    checkSame(expected, tbagrid$.settleDates)
}

test.writeTSDBuploadFile <- function(){
	
	tbagrid <- TBAGrid('fncl',c(5,5.5,6),as.POSIXct('2007-01-10 15:00:00'))
	tbagrid$setPricesFromTSDB()
	tbagrid$setSettleDatesFromTSDB()
	tbagrid$writeTSDBuploadFile(uploadMethod='file',path=tempDir)
	
	checkSame(5.664336,round(tbagrid$currentCoupon, digits=6))
	fileName <- squish('TBA_45d_fwdPx_',tbagrid$.program,'_',format(tbagrid$.gridDateTime,'%Y%m%d'),'.csv') 
	fileMatches(squish(tempDir,fileName),fileName)
	fileName <- squish('TBA_30d_fwdPx_',tbagrid$.program,'_',format(tbagrid$.gridDateTime,'%Y%m%d'),'.csv') 
	fileMatches(squish(tempDir,fileName),fileName)
	fileName <- squish('TBA_CC_',tbagrid$.program,'_',format(tbagrid$.gridDateTime,'%Y%m%d'),'.csv') 
	fileMatches(squish(tempDir,fileName),fileName)
}
