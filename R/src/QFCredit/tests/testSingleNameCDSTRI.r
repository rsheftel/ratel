library(QFCredit)

loadFrame <- function(frameName){
	target <- zoo(data.frame(read.csv(system.file("testdata",frameName, package = "QFCredit"), sep = ",", header = FALSE)))
	zoo(target[,-1],as.POSIXct(target[,1]))
}

getObject <- function(strike,cdsTicker = 'cah_snrfor_usd_xr'){
	tenor <- '5y'
	strike <- strike
	this <- SingleNameCDSTRI(cdsTicker,tenor,strike,'markit')
	checkSame(this$.cds$.ticker,'cah')	
	checkSame(this$.cds$.tenor,'5y')	
	checkSame(this$.cdsTicker,cdsTicker)
	checkSame(this$.strike,strike)	
	checkSame(this$.internalSource,'markit')
	this
}

getHolidayData <- function(){
	HolidayDataLoader$getHolidays(source = 'financialcalendar',financialCenter = 'nyb')
}

getIRSData <- function(startDate,endDate){
	getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",TermStructure$irs,'internal',startDate,endDate)
}

testConstructor <- function()
{
	this <- getObject('par')
	checkSame(this$.docClause,'xr')
	checkSame(this$.isSNAC,FALSE)
	shouldBomb(getObject('junk'))
	this <- getObject(100)
	checkSame(this$.isSNAC,TRUE)
	this <- getObject(500)
	checkSame(this$.isSNAC,TRUE)
}

testSetRange <- function()
{
	this <- getObject('par')
	this$setRange(as.POSIXct('2009-04-20'),as.POSIXct('2009-04-21'))
	checkSame(this$.range,Range(as.POSIXct('2009-04-20'),as.POSIXct('2009-04-21')))
}

testLoadSpreadSeries <- function()
{
	this <- getObject('par')
	this$setRange(as.POSIXct('2008-04-01'),as.POSIXct('2008-04-02'))
	this$loadSpreadSeries()
	checkShape(this$.cdsData,2,11)
	checkSame(as.numeric(this$.cdsData[,'5y']),c(0.00788965226381028,0.00759564426856431))
	
	this <- getObject(100)
	this$setRange(as.POSIXct('2009-04-08'),as.POSIXct('2009-04-09'))
	this$loadSpreadSeries()
	checkShape(this$.cdsData,2,11)
	checkSame(as.numeric(this$.cdsData[,'5y']),c(0.00697839958267068, 0.00728994139836801))
}

testLoadPriceSeries <- function()
{	
	this <- SingleNameCDSTRI('bzh_snrfor_usd_xr','5y',500,'markit')
	this$setRange(as.POSIXct('2009-04-08'),as.POSIXct('2009-04-09'))
	this$loadPriceSeries()
	checkShape(this$.cdsData,2,11)
	checkSame(as.numeric(this$.cdsData[,'3y']),c(0.669328864222735,0.68541431306121))
}

testLoadRecoveryRates <- function()
{	
	this <- SingleNameCDSTRI('aa_snrfor_usd_xr','5y',500,'internal')
	this$setRange(as.POSIXct('2009-04-08'),as.POSIXct('2009-04-09'))
	this$loadRecoveryRates()	
	checkSame(this$.recoveryData,zoo(matrix(c(0.4,0.4)),as.POSIXct(c('2009-04-08','2009-04-09'))))
}

testFilterDataByIntersection <- function()
{
	# Loading raw data
	this <- getObject('par')
	holidayData <- getHolidayData()
	this$loadHolidayData(holidayData)
	checkSame(this$.holidayData,holidayData)
	this$loadHolidayData(holidayData)
	irsData <- getIRSData('2009-04-08','2009-04-15')
	this$loadIrsData(irsData)
	checkSame(this$.irsData,strip.times.zoo(irsData))
	
	# Standard case
	this$setRange(as.POSIXct('2009-04-08'),as.POSIXct('2009-04-09'))
	this$loadSpreadSeries()
	this$filterDataByIntersection()
	checkShape(this$.irsData,2,16)
	checkShape(this$.cdsData,2,11)
	
	# Not enough data to run calc
	irsData <- getIRSData('2009-04-08','2009-04-08')
	this$loadIrsData(irsData)
}

testGetFunctions <- function()
{
	# Par spreads
	irsData <- getIRSData('2009-04-08','2009-04-15')
	this <- getObject('par')	
	this$setRange(as.POSIXct('2009-04-08'),as.POSIXct('2009-04-09'))
	this$loadSpreadSeries()	
	this$loadIrsData(irsData)
	this$filterDataByIntersection()
	checkSame(this$getEffDate(2),as.POSIXct('2009-04-09'))
	checkSame(this$getStrike(2),0.006985032615)
	checkSame(this$getValueDate(2),as.POSIXct('2009-04-10'))
	checkSame(this$getAccruedPnl(as.POSIXct('2008-09-01'),as.POSIXct('2008-09-02'),0.01,100),0.00277777777777778)
	curveTable <- this$getSingleNameCDSTable(2,as.POSIXct('2009-04-09'),as.POSIXct('2014-06-20'))
	checkSame(data.frame(effDate = as.POSIXct('2009-04-10'),matDate = as.POSIXct('2009-12-20'),spread = 0.00729659986740741),curveTable[1,])
	checkShape(curveTable,11,3)	
	
	# Fixed spreads
	this <- getObject(100)	
	this$setRange(as.POSIXct('2009-04-08'),as.POSIXct('2009-04-09'))
	this$loadSpreadSeries()	
	this$loadIrsData(irsData)
	this$filterDataByIntersection()
	checkSame(this$getEffDate(2),as.POSIXct('2009-02-07'))
	checkSame(this$getStrike(2),0.01)
	checkSame(this$getValueDate(2),as.POSIXct('2009-04-09'))
	checkSame(this$getAccruedPnl(as.POSIXct('2008-09-01'),as.POSIXct('2008-09-02'),0.01,100),0.00555555555555556)
	curveTable <- this$getSingleNameCDSTable(2,as.POSIXct('2009-04-09'),as.POSIXct('2014-06-20'))
	checkSame(data.frame(effDate = as.POSIXct('2009-02-08'),matDate = as.POSIXct('2014-06-20'),spread = 0.00728994139836801),curveTable)
	checkShape(curveTable,1,3)
}

checkZoos <- function(z1,z2){
	checkSame(as.POSIXct(as.character(index(z1))),as.POSIXct(as.character(index(z2))))
	checkSame(as.numeric(z1[,1]),as.numeric(z2[,1]))
	checkSame(as.numeric(z1[,2]),as.numeric(z2[,2]))
}

testTRI <- function()
{
	# Non SNAC
	
	irsData <- getIRSData('2009-03-11','2009-03-25')
	obj <- SingleNameCDSTRI('cah_snrfor_usd_mr','5y','par','markit')	
	checkSame(obj$.docClause,'mr')
	obj$setRange(as.POSIXct('2009-03-10'),as.POSIXct('2009-03-25'))
	obj$loadSpreadSeries()
	obj$loadRecoveryRates()
	cdsData <- obj$.cdsData
	obj$loadIrsData(irsData)
	obj$filterDataByIntersection()
	res <- obj$calcSpreadTRIs()
	
	m <- do.call(merge,res)
	index(m) <- as.POSIXct(as.character(index(m)))
	target <- loadFrame('testfileSingleNameTRI.NonSNAC.csv')
	checkZoos(target[1:10],m)
	
	# SNAC
		
	this <- getObject(100,'cah_snrfor_usd_xr')	
	this$.cdsData <- cdsData
	this$loadIrsData(irsData)
	this$.recoveryData <- zoo(0.4,index(this$.cdsData))
	this$filterDataByIntersection()
	res100 <- this$calcSpreadTRIs()
	
	m100 <- do.call(merge,res100)
	index(m100) <- as.POSIXct(as.character(index(m100)))
	target <- loadFrame('testfileSingleNameTRI.SNAC.csv')	
	checkZoos(target[1:10],m100)	
	
	# On the fly checks
	
	checkOnTheFly <- function(strike,startDate,endDate){
		this <- getObject(strike,'cah_snrfor_usd_xr')	
		this$.cdsData <- Range(as.POSIXct(startDate),as.POSIXct(endDate))$cut(cdsData)
		this$.recoveryData <- zoo(0.4,index(this$.cdsData))
		this$loadIrsData(Range(as.POSIXct(startDate),as.POSIXct(endDate))$cut(irsData))
		this$filterDataByIntersection()
		this$calcSpreadTRIs()
	}
	
		# SNAC
		
		res <- checkOnTheFly(100,'2009-03-19','2009-03-20')
		checkSame(res$dv01Zoo,zoo(0.047191,as.POSIXct('2009-03-20')))
		checkSame(res$dailyTriZoo,zoo(0.0397924444444444,as.POSIXct('2009-03-20')))
		
		res <- checkOnTheFly(100,'2009-03-20','2009-03-23')
		checkSame(res$dv01Zoo,zoo(0.049295,as.POSIXct('2009-03-23')))
		checkSame(res$dailyTriZoo,zoo(-0.0142521111111112,as.POSIXct('2009-03-23')))
		
		res <- checkOnTheFly(100,'2009-03-23','2009-03-24')
		checkSame(res$dv01Zoo,zoo(0.049334,as.POSIXct('2009-03-24')))
		checkSame(res$dailyTriZoo,zoo(-0.0744505555555553 ,as.POSIXct('2009-03-24')))
		
		# Non SNAC
		
		res <- checkOnTheFly('par','2009-03-19','2009-03-20')
		checkSame(res$dv01Zoo,zoo(0.046689,as.POSIXct('2009-03-20')))
		checkSame(res$dailyTriZoo,zoo(-0.0162287705226190,as.POSIXct('2009-03-20')))
		
		res <- checkOnTheFly('par','2009-03-20','2009-03-23')
		checkSame(res$dv01Zoo,zoo(0.048774,as.POSIXct('2009-03-23')))
		checkSame(res$dailyTriZoo,zoo(-0.00640334404861111,as.POSIXct('2009-03-23')))
		
		res <- checkOnTheFly('par','2009-03-23','2009-03-24')
		checkSame(res$dv01Zoo,zoo(0.048811,as.POSIXct('2009-03-24')))
		checkSame(res$dailyTriZoo,zoo(-0.0707469006944444,as.POSIXct('2009-03-24')))			
}

testTransformedTsName <- function()
{
	this <- getObject('par','cah_snrfor_usd_xr')
	checkSame(this$transformedTsName('tri'),'cah_snrfor_usd_xr_tri_5y')
	checkSame(this$transformedTsName('dv01'),'cah_snrfor_usd_xr_dv01_5y')
	
	this <- getObject(100,'cah_snrfor_usd_xr')
	checkSame(this$transformedTsName('tri'),'cah_snrfor_usd_xr_100_tri_5y')
	checkSame(this$transformedTsName('dv01'),'cah_snrfor_usd_xr_100_dv01_5y')
}

testTransformedAttributeList <- function()
{
	this <- getObject('par','cah_snrfor_usd_xr')
	target <- list(
		ticker = 'cah',
		ccy = 'usd',
		cds_strike = 'par',
		tier = 'snrfor',
		doc_clause = 'xr',
		instrument = 'cds',		
		cds_ticker = 'cah_snrfor_usd_xr',
		tenor = '5y',
		quote_type = 'tri'		
	)
	checkSame(this$transformedAttributeList('tri'),target)	
	this <- getObject(100,'cah_snrfor_usd_xr')
	target$cds_strike <- 100
	checkSame(this$transformedAttributeList('tri'),target)
}

testCalcSpreadsFromUpFronts <- function()
{
	irsData <- getIRSData('2009-04-22','2009-04-22')
	this <- SingleNameCDSTRI('rad_snrfor_usd_xr','5y',500,'markit',recovery = 0.1)	
	this$loadPriceSeries()
	this$loadRecoveryRates()
	cdsData <- this$.cdsData
	this$loadIrsData(irsData)	
	res <- this$calcSpreadsFromUpFronts()
	checkSame(zoo(0.3842392,as.POSIXct('2009-04-22')),res)
}

testGetRecovery <- function()
{
	checkSame(SingleNameCDSTRI$getRecovery(0.2),0.4)
	checkSame(SingleNameCDSTRI$getRecovery(0.5),0.4)
	checkSame(SingleNameCDSTRI$getRecovery(0.6),0.2)
	checkSame(SingleNameCDSTRI$getRecovery(0.7),0.2)
	checkSame(SingleNameCDSTRI$getRecovery(0.8),0.1)
}

testRecoveryRates <- function()
{
	this <- SingleNameCDSTRI('rad_snrfor_usd_xr','5y',500,'internal')
	this$setRange(as.POSIXct('2009-04-14'),as.POSIXct('2009-04-15'))
	data <- this$recoveryRates()
	checkZoos(data,zoo(0.2,as.POSIXct(c('2009-04-14','2009-04-15'))))
	
	this <- SingleNameCDSTRI('rad_snrfor_usd_xr','5y',100,'internal')
	this$setRange(as.POSIXct('2009-02-14'),as.POSIXct('2009-02-15'))
	data <- this$recoveryRates()
	checkSame(data,NULL)
}