# tests for SBDuration class
# 
# Author: rsheftel
###############################################################################

library(QFMortgage)

testdata <- squish(system.file("testdata", package="QFMortgage"),'/SBDuration/')

test.constructor <- function(){
	checkInherits(SBDuration(),'SBDuration')
	checkInherits(SBDuration('fncl'),'SBDuration')
}

test.baseFunctions <- function(){
	checkSame(5.34076117448886, sbDuration(1,5.75,2.25,-0.5,0))
	checkSame(6.72933678515683, sbDuration(1,5.75,2.25,-0.5,2))
	checkSame(1.54825942316988, sbDuration(1,5.75,2.25,-0.5,-1.5))
	checkSame(c(5.34076117448886, 6.72933678515683), sbDuration(1,5.75,2.25,-0.5,c(0,2)))
	
	x <- vector()
	x[1] <- 1
	x[2] <- 5.75
	x[3] <- 2.25
	x[4] <- -0.5
	checkSame(6^2, sbObjective(x,2,0.72933678515683))
}

test.optimize <- function(){
	res <- nlm(sbObjective,c(1,5,2,0),r=seq(-2,2,1),actuals=c(1.19,2.41,5.34,6.56,6.73))
	expected <-  c(0.9990838, 5.7517502, 2.2477686, -0.5000198)
	checkSame(expected,res$estimate)
	
	checkSame(expected, SBDuration$solver(seeds=c(1,5,2,0), r=seq(-2,2,1), actuals=c(1.19,2.41,5.34,6.56,6.73))$estimate)
	shouldBomb(SBDuration$solver(seeds=c(1,5,2,0), r=seq(-2,2,1), actuals=c(6.73)))
	shouldBomb(SBDuration$solver(seeds=c(1), r=seq(-2,2,1), actuals=c(1.19,2.41,5.34,6.56,6.73)))
}

test.durations.fncl <- function(){
	checkSame(c(5.4451, 3.0000, 0.5549), round(SBDuration('fncl')$durations(slope=0,otm=c(2,0,-2)),4))
	checkSame(c(5.4180, 3.8481, 1.3834), round(SBDuration('fncl')$durations(slope=0.5,otm=c(1,0,-1)),4))
	checkSame(c(5.9167, 4.6300, 0.6667), round(SBDuration('fncl')$durations(slope=1.0,otm=c(10,0,-10)),4))
	checkSame(c(5.8089, 4.9854, 3.5833), round(SBDuration('fncl')$durations(slope=2,otm=c(0.5,0,-0.5)),4))
	checkSame(c(6.7293, 5.3408, 1.1902), round(SBDuration('fncl')$durations(slope=3,otm=c(2,0,-2)),4))
	
	checkSame(c(5.4451, 3, 0.5549), round(SBDuration('fncl')$durations(slope=-1,otm=c(2,0,-2)),4))
	checkSame(c(6.7293, 5.3408, 1.1902), round(SBDuration('fncl')$durations(slope=5,otm=c(2,0,-2)),4))
	
	shouldBomb(SBDuration('fncl')$durations(slope=300,otm=c(2,0,-2)))
	shouldBombMatching(SBDuration$durations(slope=3,otm=c(2,0,-2)), "No program defined.")
}

test.durations.fnci <- function(){
	checkSame(c(4.6781, 2.7500, 0.8219), round(SBDuration('fnci')$durations(slope=0,otm=c(2,0,-2)),4))
	checkSame(c(4.5652, 3.3334, 1.5366), round(SBDuration('fnci')$durations(slope=0.5,otm=c(1,0,-1)),4))
	checkSame(c(5.0000, 3.8794, 0.8333), round(SBDuration('fnci')$durations(slope=1.0,otm=c(10,0,-10)),4))
	checkSame(c(4.7335, 4.0846, 3.0833), round(SBDuration('fnci')$durations(slope=2,otm=c(0.5,0,-0.5)),4))
	checkSame(c(5.4699, 4.2898, 1.2134), round(SBDuration('fnci')$durations(slope=3,otm=c(2,0,-2)),4))
	
	checkSame(c(4.6781, 2.7500, 0.8219), round(SBDuration('fnci')$durations(slope=-1,otm=c(2,0,-2)),4))
	checkSame(c(5.4699, 4.2898, 1.2134), round(SBDuration('fnci')$durations(slope=5,otm=c(2,0,-2)),4))
}

test.couponDurations <- function(){
	sb <- SBDuration('fncl')
	shouldBombMatching(sb$couponDurations(coupons=c(4,5,6)),'Current coupon not set.')
	sb$setCurrentCoupon(container=squish(testdata,'CurrentCoupon.csv'), source='internal')
	
	shouldBombMatching(sb$couponDurations(coupons=c(4,5,6)),'Swap slope not set.')
	sb$setSwapSlope(container=squish(testdata,'SwapRates.csv'), source='internal')
	
	shouldBombMatching(sb$couponDurations(coupons=c(4,5,6),Range('1900-01-01','1900-01-10')),'No dates for range.')
	
	actuals <- sb$couponDurations(coupons=c(4,5,6),Range('2007-01-07','2007-02-07'))
	actuals <- sb$couponDurations(coupons=c(4,5,6),verbose=TRUE)
	expected <- read.zoo(squish(testdata,'DurationZoo.csv'),header=TRUE,sep=",", FUN=as.POSIXct)
	colnames(expected) <- c(4,5,6)
	checkSameLooking(round(actuals,4) , round(expected,4))
	
	sb$uploadDurations(expected, uploadMethod='file', uploadFilename='test_uploadFile', uploadPath=testdata)
	fileMatches(squish(testdata,'test_uploadFile.csv'),squish(testdata,'bench_uploadFile.csv'))
}
