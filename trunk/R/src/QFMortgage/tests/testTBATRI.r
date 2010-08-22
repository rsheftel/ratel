# testTBATRI.r
# Author: rsheftel
###############################################################################

library(QFMortgage)

fileMatches <- function(testFilename, benchFilename){
	testData <- read.csv(testFilename)
	benchmarkPath <- squish(system.file("testdata", package="QFMortgage"),'/TBATRI/')
	benchData <- read.csv(squish(benchmarkPath,benchFilename))
	file.remove(testFilename)	
	return(checkSame(benchData,testData))
}

tempDir <- squish(dataDirectory(),'temp_TSDB/')
testdataDir <- squish(system.file("testdata", package="QFMortgage"),'/TBATRI/') 
	
test.TBATRI <- function(){
	tbaTri <- TBATRI('fncl', seq(5,8,0.5))
	checkInherits(tbaTri,'TBATRI')
}

test.setTsdbSources <- function(){
	tbaTri <- TBATRI('fncl', seq(5,8,0.5))
	tbaTri$setTsdbSources(tbaPriceSource="internal", tbaDurationSource="internal", swapTRISource="internal")
	shouldBombMatching(tbaTri$setTsdbSources(swapTRISource=9),'swapTRISource is not character is numeric')
}

test.setDateRange <- function(){
	tbaTri <- TBATRI('fncl', seq(5,8,0.5))
	tbaTri$setTsdbSources(tbaPriceSource="internal", tbaDurationSource="internal", swapTRISource="internal")
	tbaTri$setDateRange(as.POSIXct('2008-01-01'), lookbackDays=9)
	tbaTri$setDateRange(as.POSIXct('2008-01-01'), startDate='2007-12-20')
	shouldBombMatching(tbaTri$setDateRange(as.POSIXct('2008-01-01'), startDate='2007-12-45'),'character string is not in a standard unambiguous format')
	shouldBombMatching(tbaTri$setDateRange(as.POSIXct('2008-01-01'), startDate='2008-12-31'),'startDate must be equal to or before endDate')
}

# Test removed as it is repeated below and is very slow
#test.setDataFromTsdb <- function(){
#	tbaTri <- TBATRI('fncl', seq(5,8,0.5))
#	tbaTri$setTsdbSources(tbaPriceSource="internal", tbaDurationSource="internal", swapTRISource="internal")
#	tbaTri$setDateRange(as.POSIXct('2008-01-01'), lookbackDays=9)
#	tbaTri$setTBADataFromTsdb()
#	tbaTri$setSwapDataFromTsdb()
#}

test.calculateTRI.vSwapPartials <- function(){
	tbaTri <- TBATRI("fncl",seq(5,6,0.5))
	tbaTri$setDateRange(dataDate=as.POSIXct('2008-01-01'), daysBack=7)
	tbaTri$setTsdbSources()
	tbaTri$setTBADataFromTsdb()
	tbaTri$setSwapDataFromTsdb()
	tbaTri$calculateTBAContinuousPriceChange()
	tbaTri$calculateTBAContinuousPriceLevel()
	
	print('vSwapPartials')
	tbaTri$generateTRI(hedgeBasket='vSwapPartials')
	checkSame(round(tbaTri$.TRI$vSwapPartials[['6']][[1]],8), -0.01446609)
	tbaTri$uploadTRItoTsdb(hedgeBasket='vSwapPartials',source='internal',uploadPath=tempDir,uploadMethod='file')
	uploadFilename <- 'fncl_6.0_1c_tri_daily_vSwapPartials.csv'
	fileMatches(squish(tempDir,uploadFilename),uploadFilename)
}

test.calculateTRI <- function(){
	
	checkTRI <- function(hedge,coupon){
		print(squish('Checking...',hedge))
		tbaTri$generateTRI(hedgeBasket=hedge)
		tbaTri$uploadTRItoTsdb(hedgeBasket=hedge,source='internal',uploadPath=tempDir,uploadMethod='file')
		uploadFilename <- squish('fncl_',coupon,'_1c_tri_',hedge,'.csv')
		fileMatches(squish(tempDir,uploadFilename),uploadFilename)
		uploadFilename <- squish('fncl_',coupon,'_1c_tri_daily_',hedge,'.csv')
		fileMatches(squish(tempDir,uploadFilename),uploadFilename)		
	}
		
	tbaTri <- TBATRI("fncl",seq(5,6,0.5))
	tbaTri$setDateRange(dataDate=as.POSIXct('2008-01-01'), startDate=as.POSIXct('2007-11-28'))
	tbaTri$setTsdbSources()
	tbaTri$setTBADataFromTsdb()
	tbaTri$calculateTBAContinuousPriceChange()
	tbaTri$calculateTBAContinuousPriceLevel()	
	
	print('vNoHedge')
	tbaTri$generateTRI(hedgeBasket='vNoHedge')
	checkSameLooking(tbaTri$.TBA$price.1c$change,tbaTri$.TRI$vNoHedge)
	tbaTri$uploadTRItoTsdb(hedgeBasket='vNoHedge',source='internal',uploadPath=tempDir,uploadMethod='file')
	uploadFilename <- 'fncl_5.0_1c_tri_vNoHedge.csv'
	fileMatches(squish(tempDir,uploadFilename),uploadFilename)
	uploadFilename <- 'fncl_5.0_1c_tri_daily_vNoHedge.csv'
	fileMatches(squish(tempDir,uploadFilename),uploadFilename)
	
	tbaTri$setCashTreasuryDataFromTsdb()
	checkTRI('vTreasury10y','5.0')
	
	tbaTri$setTreasuryFuturesData(container.TRI='systemdb',container.dv01=squish(testdataDir,'future_pvbp.csv'))
	checkTRI('vFuturesTY','5.0')
	checkTRI('vFuturesTU_TY','5.0')
	checkTRI('vFuturesTU_FV_TY_US','5.0')
}

test.missingUnneededFuturesData <- function(){
	
	tbaTri <- TBATRI("fncl",seq(6,6.5,0.5))
	tbaTri$setDateRange(dataDate=as.POSIXct('1993-10-06'), startDate=as.POSIXct('1993-09-02'))
	tbaTri$setTsdbSources()
	tbaTri$setTBADataFromTsdb()
	tbaTri$calculateTBAContinuousPriceChange()
	tbaTri$calculateTBAContinuousPriceLevel()	
	
	tbaTri$setTreasuryFuturesData(container.TRI='systemdb',container.dv01=squish(testdataDir,'future_pvbp.csv'))
	tbaTri$generateTRI(hedgeBasket='vFuturesTY')
	checkSame(-0.181678,round(sum(tbaTri$.TRI$vFuturesTY[['6']]),6))
	checkSame(-0.262285,round(sum(tbaTri$.TRI$vFuturesTY[['6.5']]),6))
}

test.missingDataInTheMiddle <- function(){
	#Check Missing data
	tbaTri <- TBATRI("fncl",seq(5,6,0.5))
	tbaTri$setDateRange(dataDate=as.POSIXct('2008-01-01'), startDate=as.POSIXct('2007-11-28'))
	tbaTri$setTsdbSources()
	tbaTri$setTBADataFromTsdb()
	tbaTri$calculateTBAContinuousPriceChange()
	tbaTri$calculateTBAContinuousPriceLevel()	
		
	tbaTri$setTreasuryFuturesData(container.TRI='systemdb',container.dv01=squish(testdataDir,'future_pvbp_missingData.csv'))
	
	#make some bad data points in the futures TRI
	tbaTri$.futuresTreasury$TRI$tu[index(tbaTri$.futuresTreasury$TRI$tu) == as.POSIXct('2007-12-04 15:00:00')] <- NA
	tbaTri$.futuresTreasury$TRI$fv[index(tbaTri$.futuresTreasury$TRI$fv) == as.POSIXct('2007-12-27 15:00:00')] <- NA
	tbaTri$.futuresTreasury$TRI$ty[index(tbaTri$.futuresTreasury$TRI$ty) == as.POSIXct('2007-12-17 15:00:00')] <- NA
	tbaTri$.futuresTreasury$TRI$ty[index(tbaTri$.futuresTreasury$TRI$ty) == as.POSIXct('2007-12-18 15:00:00')] <- NA
	
	tbaTri$.TBAzoos$partials[['2y']][['5']][index(tbaTri$.TBAzoos$partials[['2y']][['5']])== as.POSIXct('2007-11-28 15:00:00')] <- NA
	tbaTri$.TBAzoos$partials[['2y']][['5']][index(tbaTri$.TBAzoos$partials[['2y']][['5']])== as.POSIXct('2007-12-06 15:00:00')] <- NA
	tbaTri$.TBAzoos$partials[['2y']][['5.5']][index(tbaTri$.TBAzoos$partials[['2y']][['5.5']])== as.POSIXct('2007-12-28 15:00:00')] <- NA
	tbaTri$.TBAzoos$partials[['2y']][['5.5']][index(tbaTri$.TBAzoos$partials[['2y']][['5.5']])== as.POSIXct('2007-12-31 15:00:00')] <- NA
	tbaTri$.TBAzoos$partials[['2y']][['6']][index(tbaTri$.TBAzoos$partials[['2y']][['6']])== as.POSIXct('2007-12-28 15:00:00')] <- NA
	tbaTri$.TBAzoos$partials[['2y']][['6']][index(tbaTri$.TBAzoos$partials[['2y']][['6']])== as.POSIXct('2007-12-31 15:00:00')] <- NA	
	tbaTri$.TBAzoos$partials[['5y']][['5.5']][index(tbaTri$.TBAzoos$partials[['5y']][['5.5']])== as.POSIXct('2007-12-13 15:00:00')] <- NA
	tbaTri$.TBAzoos$partials[['10y']][['5']][index(tbaTri$.TBAzoos$partials[['10y']][['5']])== as.POSIXct('2007-12-20 15:00:00')] <- NA
	
	tbaTri$.TBAzoos$partials[['2y']][['5']] <- na.omit(tbaTri$.TBAzoos$partials[['2y']][['5']])
	tbaTri$.TBAzoos$partials[['2y']][['6']] <- na.omit(tbaTri$.TBAzoos$partials[['2y']][['6']])
	tbaTri$.TBAzoos$partials[['5y']][['5.5']] <- na.omit(tbaTri$.TBAzoos$partials[['5y']][['5.5']])
		
	tbaTri$generateTRI(hedgeBasket='vFuturesTU_FV_TY_US')
	expected <- read.csv(squish(testdataDir,'fn_5.0_AllFuts_MissingData.csv'))
	expected <- zoo(expected[,2],expected[,1])
	checkSameLooking(round(expected,6), round(tbaTri$.TRI$vFuturesTU_FV_TY_US[['5']],6))

	tbaTri$generateTRI(hedgeBasket='vFuturesTY')
	expected <- read.csv(squish(testdataDir,'fn_6.0_TYFuts_MissingData.csv'))
	expected <- zoo(expected[,2],expected[,1])
	checkSameLooking(round(expected,6), round(tbaTri$.TRI$vFuturesTY[['6']],6))

	tbaTri$generateTRI(hedgeBasket='vFuturesTU_TY')
	expected <- read.csv(squish(testdataDir,'fn_5.5_TUTYFuts_MissingData.csv'))
	expected <- zoo(expected[,2],expected[,1])
	checkSameLooking(round(expected,6), round(tbaTri$.TRI$vFuturesTU_TY[['5.5']],6))
		
}