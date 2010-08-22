# testTBACouponSwapTRI.r.R
# 
# TODO: Add comment
#
# Author: rsheftel
###############################################################################

library(QFMortgage)

tempDir <- squish(dataDirectory(),'temp_TSDB/')
testdata <- squish(system.file("testdata", package="QFMortgage"),'/TBACouponSwapTRI/')

fileMatches <- function(testFilename, benchFilename){
	testfile  <- read.csv(testFilename)
	benchfile <- read.csv(squish(testdata,benchFilename))	
	return(checkSame(benchfile,testfile))
}

test.TBACouponSwapTRI <- function(){
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	checkInherits(csTRI,'TBACouponSwapTRI')
}

test.TBACouponSwapTRI.setTsdbSources <- function(){
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setTsdbSources(couponSwapHedgeRatioSource="qfmodel_couponSwapModel_1.0")
	shouldBombMatching(csTRI$setTsdbSources(couponSwapHedgeRatioSource=9),'couponSwapHedgeRatioSource is not character is numeric')
}

test.TBACouponSwapTRI.setHedgeRatio <- function(){
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setHedgeRatio(forwardDays="45d")
	shouldBombMatching(csTRI$setHedgeRatio(forwardDays=45),'forwardDays is not character is numeric')
}

test.TBACouponSwapTRI.calculateZooFromHedgeRatios <- function(){
	
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setTsdbSources(couponSwapHedgeRatioSource="qfmodel_couponSwapModel_1.0")
	csTRI$setHedgeRatio(forwardDays="45d")
	csTRI$setTBATRIDataFromTsdb()
	csTRI$setHedgeRatiosFromTsdb()
	triCalc <- csTRI$calculateTRI(5.5)
	
	checkSame(triCalc[[1]],100)
	checkSame(round(triCalc[[100]],5),99.80354)
	
	csTRI$calculateTRIs(hedgeRatio.min=0,hedgeRatio.max=1)
	checkSame(csTRI$.couponSwap$TRI[['5.5']][[1]],100)
	checkSame(round(csTRI$.couponSwap$TRI[['5.5']][[1000]],4),100.0330)
}

test.TBACouponSwapTRI.uploadTRIs <- function(){
	
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setTsdbSources(couponSwapHedgeRatioSource="qfmodel_couponSwapModel_1.0")
	csTRI$setHedgeRatio(forwardDays="45d")
	csTRI$setTBATRIDataFromTsdb()
	csTRI$setHedgeRatiosFromTsdb()
	csTRI$calculateTRIs(hedgeRatio.min=0,hedgeRatio.max=1)
	csTRI$uploadTRIs(uploadMethod='file',uploadPath=tempDir)

	fileName <- paste('fncl_5.5_5.0_1c_45d_tri_vSwapPartials.csv')
	testData <- read.csv(squish(tempDir,fileName))
	benchData <- read.csv(squish(testdata,fileName))
	checkSameLooking(benchData[1:100,1],testData[1:100,1])	
	checkSame(round(benchData[1:100,2],8),round(testData[1:100,2],8))
}

test.setHedgeRatioFromCouponDv01s <- function(){
	
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setTsdbSources(couponDv01='testdv01')
	csTRI$setHedgeRatio(forwardDays="45d")
	csTRI$setHedgeRatiosFromDv01s(container=squish(testdata,'couponSwapTRI_dv01s.csv'))

	checkSame(csTRI$.couponSwap$hedgeRatio[['5']], NULL)
	checkSame(csTRI$.couponSwap$hedgeRatio[['5.5']], NULL)

	dates <- as.POSIXct(paste('2008-01-',1:20,' 15:00:00', sep=""))
	expected <- na.omit(zoo(c(2.285,1.376,31.182,3.780,10.891,0.282,NA,0.736,0.902,0.325,6.516,1.830,0.671,0.108,NA,0.899,1.976,0.805,35.280,4.378),dates))
	checkSameLooking(expected, round(csTRI$.couponSwap$hedgeRatio[["6"]],3))
}

test.calculateTRIsfromCouponDv01s <- function(){
	
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setTsdbSources(couponDv01='testdv01',tbaCouponTRISource='test')
	csTRI$setHedgeRatio(forwardDays="45d")
	csTRI$setHedgeRatiosFromDv01s(container=squish(testdata,'couponSwapTRI_dv01s.csv'))
	csTRI$setTBATRIDataFromTsdb(container=squish(testdata,'couponSwapTRI_CouponTRIs.csv'))

	triCalc <- csTRI$calculateTRI(6)	
	dates <- as.POSIXct(paste('2008-01-',1:20,' 15:00:00', sep=""))	
	expected <- na.omit(zoo(c(100.0000,98.7216,99.5900,47.4120,53.5340,73.3851,NA,72.0828,72.0734,73.8310,72.8418,62.1424,65.2952,65.4977,NA,63.9234,63.6087,63.9259,61.7687,122.3744),dates))
	checkSameLooking(expected,round(triCalc,4))

	triCalc <- csTRI$calculateTRI(6.5)	
	dates <- as.POSIXct(paste('2008-01-',1:20,' 15:00:00', sep=""))	
	expected <- na.omit(zoo(c(100.0000,100.7053,101.0477,101.5574,99.6584,NA,104.2346,103.9913,105.9645,106.5431,110.9255,NA,109.3315,108.3393,NA,114.3155,110.4329,103.1272,105.4395,105.6903),dates))
	checkSameLooking(expected,round(triCalc,4))		
}

test.weightedRolls <- function(){
	csTRI <- TBACouponSwapTRI('fncl',seq(4.5,7,0.5),TBATRIhedge="vSwapPartials")
	csTRI$setTsdbSources(couponDv01='testdv01',tbaPriceSource='internal',couponSwapHedgeRatioSource='testdv01')
	csTRI$setHedgeRatio(forwardDays="45d")
	csTRI$setHedgeRatiosFromDv01s(container=squish(testdata,'couponSwapTRI_dv01s.csv'))
	
	csTRI$setTBAPrices(container='tsdb')
	dates <- as.POSIXct(paste('2008-01-',1:20,' 15:00:00', sep=""))
	expected <- na.omit(zoo(c(NA,0.0004,-1.517,-0.1216,NA,NA,NA,0.0251,0.0392,0.0896,-0.6778,NA,NA,0.1159,NA,0.02,-0.1143,0.0315,NA,NA),dates))
	checkSameLooking(expected, round(csTRI$calculateWeightedRoll(6),4))

	csTRI$calculateWeightedRolls()
	csTRI$uploadWeightedRolls(uploadPath=testdata, uploadMethod='file')
	testfile <- getZooDataFrame(read.zoo(squish(testdata,'fncl_6.0_5.5_1n_weighted_roll.csv'),FUN=as.POSIXct,header=TRUE,sep=","))
	checkSameLooking(expected, round(testfile,4))	
}
