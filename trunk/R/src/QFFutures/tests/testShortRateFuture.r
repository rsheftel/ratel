# ShortRateFuture Class tests
# 
# Author: RSheftel
###############################################################################

library(QFFutures)
testdataPath <- squish(system.file("testdata", package="QFFutures"),'/ShortRateFuture/')
tempDir <- squish(dataDirectory(),'temp_TSDB/')

dates <- as.POSIXct(paste('2000','01',1:11,sep="-"))

test.constructor <- function(){    
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	checkInherits(ed, "ShortRateFuture")
}

test.contractType <- function(){
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	shouldBomb(ed$setContractType('xx'))
}

makeHypoDataObject <- function(){
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	#Make a fake series of ed futures (must use direct method and container file not available)
	for (expiry in 1:20) ed$.singleContracts[[expiry]] <- zoo((0:10)*expiry,dates)
	ed$.singleContracts[[1]][4] <- NA
	ed$.singleContracts[[10]][10] <- NA
	ed$.singleContracts[[20]][3] <- NA
	return(ed)
}

test.Packs <- function(){
	ed <- makeHypoDataObject()	
	ed$calculatePacks(packs=c('white','red','green','blue','gold'))
	expected <- na.omit(zoo(c(0,2.5,5,NA,10,12.5,15,17.5,20,22.5,25),dates))
	checkSameLooking(expected, ed$getPack('white'))
	
	expected <- na.omit(zoo(c(0,18.5,NA,55.5,74,92.5,111,129.5,148,166.5,185),dates))
	checkSameLooking(expected, ed$getPack('gold'))

	ed$uploadPacks(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'ed_white_pack_1c_price_mid.csv'),squish(testdataPath,'ed_white_pack.1c.csv'))	
	fileMatches(squish(tempDir,'ed_gold_pack_1c_price_mid.csv'),squish(testdataPath,'ed_gold_pack.1c.csv'))
}

test.Bundles <- function(){
	ed <- makeHypoDataObject()
	ed$calculateBundles()
	expected <- na.omit(zoo(c(0,4.5,9,NA,18,22.5,27,31.5,36,40.5,45),dates))
	checkSameLooking(expected, ed$getBundle('2y'))
	
	expected <- na.omit(zoo(c(0,10.5,NA,NA,42,52.5,63,73.5,84,NA,105),dates))
	checkSameLooking(expected, ed$getBundle('5y'))
	
	ed$uploadBundles(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'ed_2y_bundle_1c_price_mid.csv'),squish(testdataPath,'ed_2y_bundle.1c.csv'),deleteFile=TRUE)	
	fileMatches(squish(tempDir,'ed_5y_bundle_1c_price_mid.csv'),squish(testdataPath,'ed_5y_bundle.1c.csv'),deleteFile=TRUE)
}

test.PackSpreads <- function(){
	ed <- makeHypoDataObject()	
	ed$calculatePacks(packs=c('white','red','green','blue','gold'))
	
	ed$makePackSpread(basePack='red',hedgePack='green')
	ed$makePackSpread(basePack='red',hedgePack='blue')
	ed$uploadPackSpreads(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	#fileMatches(squish(tempDir,'ed_red_blue_pack_spread_1c.csv'),squish(testdataPath,'ed_red_blue_pack_spread.1c.csv'))
	fileMatches(squish(tempDir,'ed_red_blue_pack_spread_1c_tri.csv'),squish(testdataPath,'ed_red_blue_pack_spread.1c_tri.csv'),deleteFile=TRUE)
}

test.PackFlys <- function(){
	ed <- makeHypoDataObject()	
	ed$calculatePacks(packs=c('white','red','green','blue','gold'))
	
	ed$makePackFly(nearWing='red',middle='green',farWing='gold')
	ed$uploadPackCombinations(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	#fileMatches(squish(tempDir,'ed_red_green_gold_pack_fly_1c.csv'),squish(testdataPath,'ed_red_green_gold_pack_fly.1c.csv'))
	fileMatches(squish(tempDir,'ed_red_green_gold_pack_fly_1c_tri.csv'),squish(testdataPath,'ed_red_green_gold_pack_fly.1c_tri.csv'),deleteFile=TRUE)	
}

test.BundleSpreads <- function(){
	ed <- makeHypoDataObject()	
	ed$calculateBundles()
	
	ed$makeBundleSpread(baseBundle='2y',hedgeBundle='5y')
	ed$makeBundleSpread(baseBundle='1y',hedgeBundle='4y')
	ed$uploadBundleSpreads(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	#fileMatches(squish(tempDir,'ed_1y_4y_bundle_spread_1c.csv'),squish(testdataPath,'ed_1y_4y_bundle_spread.1c.csv'))
	fileMatches(squish(tempDir,'ed_1y_4y_bundle_spread_1c_tri.csv'),squish(testdataPath,'ed_1y_4y_bundle_spread.1c_tri.csv'),deleteFile=TRUE)
}

test.SingleSpreads <- function(){
	ed <- makeHypoDataObject()	
	
	ed$makeSingleSpread(1,5)
	ed$makeSingleSpread(5,10)
	
	ed$uploadSingleCombinations(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	#fileMatches(squish(tempDir,'ed_5c_10c_spread.csv'),squish(testdataPath,'ed_5c_10c_spread.csv'),deleteFile=TRUE)
	fileMatches(squish(tempDir,'ed_5c_10c_spread_tri.csv'),squish(testdataPath,'ed_5c_10c_spread_tri.csv'),deleteFile=TRUE)				
}

test.SingleButterflys <- function(){
	ed <- makeHypoDataObject()	
	
	ed$makeSingleButterfly(1,5,20)
	ed$makeSingleButterfly(8,10,18)
	
	ed$uploadSingleCombinations(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	#fileMatches(squish(tempDir,'ed_8c_10c_18c_fly.csv'),squish(testdataPath,'ed_8c_10c_18c_fly.csv'))
	fileMatches(squish(tempDir,'ed_8c_10c_18c_fly_tri.csv'),squish(testdataPath,'ed_8c_10c_18c_fly_tri.csv'),deleteFile=TRUE)						
}

test.realData <- function(){
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	ed$setContractType('continuous')
	ed$setDateRange('2005-01-01','2005-12-31')
	ed$setSources(contract='test')
	
	ed$calculateSingleContracts(numberContracts=8, container=squish(testdataPath,'ED_all.csv'))
	ed$uploadSingleContracts(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'ed.1c_price_mid.csv'),squish(testdataPath,'realData_ed.1c.csv'),deleteFile=TRUE)
	fileMatches(squish(tempDir,'ed.2c_price_mid.csv'),squish(testdataPath,'realData_ed.2c.csv'),deleteFile=TRUE)
	fileMatches(squish(tempDir,'ed.8c_price_mid.csv'),squish(testdataPath,'realData_ed.8c.csv'),deleteFile=TRUE)
		
	ed$calculatePacks(packs=c('white','red'))
	ed$uploadPacks(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'ed_white_pack_1c_price_mid.csv'),squish(testdataPath,'realData_ed_white_pack.1c.csv'),deleteFile=TRUE)
	fileMatches(squish(tempDir,'ed_red_pack_1c_price_mid.csv'),squish(testdataPath,'realData_ed_red_pack.1c.csv'),deleteFile=TRUE)
		
	ed$calculateBundles(bundles=c('1y','2y'))
	ed$uploadBundles(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'ed_2y_bundle_1c_price_mid.csv'),squish(testdataPath,'realData_ed_2y_bundle.1c.csv'),deleteFile=TRUE)	
}

test.stubDates <- function(){
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	ed$setContractType('continuous')
	ed$setDateRange('2008-01-01','2009-02-12')
	
	methodData <- ed$daysToEndOfStub()
	filedata <- read.zoo(squish(testdataPath,"testDaysToEndOfStub.csv"),header=FALSE,sep=",", FUN=as.POSIXct)
	checkSame(filedata, methodData)
}

test.bundlePvbp <- function(){
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	ed$setContractType('continuous')
	ed$setDateRange('2005-01-01','2005-12-31')
	ed$setSources(contract='test')	
	ed$calculateSingleContracts(numberContracts=8, container=squish(testdataPath,'ED_all.csv'))
	ed$calculateBundles(c('1y','2y'))
	
	ed$calculateBundlePvbps(c('1y','2y'))
	ed$uploadBundlePvbps(tsdbSource='test',uploadPath=tempDir,uploadMethod='file')
	fileMatches(squish(tempDir,'ed_2y_bundle_1c_pvbp.csv'),squish(testdataPath,'ed_2y_bundle_1c_pvbp.csv'))#,deleteFile=TRUE)
}

test.markets <- function(){
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	ed$setContractType('continuous')
	markets <- ed$markets()
	assert(class(markets) == 'character')
	assert(length(markets) > 0)
	assert(all(as.character(sapply(markets,function(x){leftStr(x,3)})) == 'ED.'))
}

testfrontDataForMarket <- function(){
	date <- '2009-05-27'
	ed <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
	adjustedList <- ed$adjustedList(5,date)
	checkSame('ed200906',ed$frontDataForMarket('ED.1C',adjustedList))
	checkSame('ed200909',ed$frontDataForMarket('ED.2C',adjustedList))
	checkSame('ed200912',ed$frontDataForMarket('ED.3N',adjustedList))
	checkSame('ed200906',ed$frontDataForMarket('ED.WHITE.PACK.1C',adjustedList))
	checkSame('ed201006',ed$frontDataForMarket('ED.RED.PACK.1C',adjustedList))
	checkSame('ed201106',ed$frontDataForMarket('ED.GREEN.PACK.1C',adjustedList))
	checkSame('ed200906',ed$frontDataForMarket('ED.1C.3C.5C.FLY',adjustedList))
}