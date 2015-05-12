library(QFCredit)

testConstructor <- function()
{
	# from cds ticker
	this <- SingleNameCDS(ticker = 'axl-inc')
	checkSame(this$.eqTicker,'axl')
	checkSame(this$.ticker,'axl-inc')
	checkSame(this$.tenor,'5y')
	checkSame(this$.ccy,'usd')
	checkSame(this$.tier,'snrfor')
	checkSame(this$.source,'internal')
	checkSame(this$.root,'axl-inc_snrfor_usd')
	checkSame(this$.strikeThreshold,400)
	
	# from equity ticker
	this <- SingleNameCDS(eqTicker = 'axl')
	checkSame(this$.eqTicker,'axl')
	checkSame(this$.ticker,'axl-inc')
	checkSame(this$.tenor,'5y')
	checkSame(this$.ccy,'usd')
	checkSame(this$.tier,'snrfor')
	checkSame(this$.source,'internal')
	checkSame(this$.root,'axl-inc_snrfor_usd')
	checkSame(this$.strikeThreshold,400)
}

testSpecificAttrList <- function()
{
	this <- SingleNameCDS('gm')
	resList <- this$specificAttrList('spread','par','mr')
	target <- list(
		ticker = 'gm',ccy = 'usd',cds_strike='par',
		tier = 'snrfor',quote_type = 'spread',doc_clause = 'mr',
		tenor = '5y',instrument = 'cds',cds_ticker = 'gm_snrfor_usd_mr'		
	)
	checkSame(resList,target)
	resList <- this$specificAttrList('spread',100,'mr','f')
	target$ticker <- 'f'
	target$cds_strike <- 100
	target$cds_ticker <- 'f_snrfor_usd_mr'
	checkSame(resList,target)
}

testUpdateTimeSeriesIDs <- function()
{
#	checkZoos <- function(z1,z2){
#		checkSame(as.character(z1),as.character(z2))
#		checkSame(as.character(index(z1)),as.character(index(z2)))
#	}	
#	this <- SingleNameCDS('cah')	
#	frame <- this$idFrame()
#	startDate <- '2009-04-16'
#	endDate <- '2009-04-24'
#	spreadSeries <- this$specificSeries('spread',100,'xr',startDate,endDate)
#	checkSame(this$updateTimeSeriesIDs(frame,spreadSeries,FALSE),NULL)
#	spreadSeries[3] <- 0.041
#	target <- zoo(c('cah_snrfor_usd_xr_500_spread_5y','cah_snrfor_usd_xr_100_spread_5y'),as.POSIXct(c('2009-04-20','2009-04-21')))
#	res <- this$updateTimeSeriesIDs(frame,spreadSeries,FALSE)	
#	checkZoos(res,target)
#	spreadSeries[4] <- 0.041
#	target <- zoo(c('cah_snrfor_usd_xr_500_spread_5y','cah_snrfor_usd_xr_100_spread_5y'),as.POSIXct(c('2009-04-20','2009-04-22')))
#	res <- this$updateTimeSeriesIDs(frame,spreadSeries,FALSE)	
#	checkZoos(res,target)
#	spreadSeries[6] <- 0.041
#	target <- zoo(c('cah_snrfor_usd_xr_500_spread_5y','cah_snrfor_usd_xr_100_spread_5y','cah_snrfor_usd_xr_500_spread_5y','cah_snrfor_usd_xr_100_spread_5y'),as.POSIXct(c('2009-04-20','2009-04-22','2009-04-23','2009-04-24')))
#	res <- this$updateTimeSeriesIDs(frame,spreadSeries,FALSE)
#	checkZoos(res,target)
#	
#	# One Date
#	checkSame(NULL,this$updateTimeSeriesIDs(frame,spreadSeries[1],FALSE))
#	res <- this$updateTimeSeriesIDs(frame,spreadSeries[3],FALSE)
#	target <- zoo(c('cah_snrfor_usd_xr_500_spread_5y'),as.POSIXct('2009-04-20'))
#	checkZoos(res,target)
}

testIsSNACTimeSeries <- function()
{
	checkSame(FALSE,SingleNameCDS$isSNACTimeSeries('xl_snrfor_usd_mr_spread_5y'))
	checkSame(TRUE,SingleNameCDS$isSNACTimeSeries('cah_snrfor_usd_xr_100_spread_5y'))
}

testSpecificSeries <- function()
{
	this <- SingleNameCDS('cah',dataSource = 'markit')
	res <- this$specificSeries(quote_type = 'spread',strike = 100,doc_clause = 'xr',startDate = '2009-04-24',endDate = '2009-04-24')
	checkSame(res,zoo(as.matrix(0.00601134749924568),as.POSIXct('2009-04-24 15:00:00')))
	res <- this$specificSeries(quote_type = 'spread',strike = 'par',doc_clause = 'mr',startDate = '2009-04-24',endDate = '2009-04-24')
	checkSame(res,zoo(as.matrix(0.006347231638),as.POSIXct('2009-04-24 15:00:00')))
}

testIdsAttrList <- function()	
{
	this <- SingleNameCDS('gm')
	resList <- this$idsAttrList()
	target <- list(
		ticker = 'gm',ccy = 'usd',cds_strike='market',
		tier = 'snrfor',quote_type = 'id',doc_clause = 'market',
		tenor = '5y',instrument = 'cds'		
	)
	checkSame(resList,target)
}

testTimeSeriesID <- function()	
{
	this <- SingleNameCDS('gm')
	res <- this$timeSeriesIDs()
	checkSame(res,zoo(as.matrix(c(354517,1925468)),as.POSIXct(c('2001-01-02','2009-04-09'))))	
}

testIdFrame <- function()	
{
	toDay <-as.character(Range$today())
	this <- SingleNameCDS('gm')
	res <- this$idFrame('spread')
	checkSame(res,
		data.frame(
			startDate = as.POSIXct(c('2001-01-02','2009-04-09')),
			endDate = as.POSIXct(c('2009-04-08',toDay)),
			tsName = c('gm_snrfor_usd_mr_spread_5y','gm_snrfor_usd_xr_500_spread_5y'),
			stringsAsFactors = FALSE
		)
	)
}

testBuildGeneric <- function()	
{
	this <- SingleNameCDS('cah',dataSource = 'markit')
	res <- this$buildGeneric('spread')
	checkSame(as.numeric(res[as.POSIXct('2009-04-08 15:00:00'),]),0.007395139166)
	checkSame(as.numeric(res[as.POSIXct('2009-04-09 15:00:00'),]),0.00728994139836801)
		
	this <- SingleNameCDS('aa',dataSource = 'internal')
	res <- this$buildGeneric('tri_daily')
	checkSame(as.numeric(res[as.POSIXct('2009-04-08'),]),-0.735279080611107)
	checkSame(as.numeric(res[as.POSIXct('2009-04-09'),]),-0.846635777777778)
	
	this <- SingleNameCDS('aa',dataSource = 'internal')
	res <- this$buildGeneric('dv01')
	checkSame(as.numeric(res[as.POSIXct('2009-04-08'),]),0.036372)
	checkSame(as.numeric(res[as.POSIXct('2009-04-09'),]),0.033876)
}

testGenericSeries <- function()	
{
	this <- SingleNameCDS('cah',dataSource = 'internal')
	res <- this$genericSeries('spread')
	checkSame(as.numeric(res[as.POSIXct('2009-04-08'),]),0.007395139166)
	checkSame(as.numeric(res[as.POSIXct('2009-04-09'),]),0.00728994139836801)
	this <- SingleNameCDS('aa',dataSource = 'internal')
	res <- this$genericSeries('tri_daily')
	checkSame(as.numeric(res[as.POSIXct('2009-04-08'),]),-0.735279080611107)
	checkSame(as.numeric(res[as.POSIXct('2009-04-09'),]),-0.846635777777778)	
	res <- this$genericSeries('dv01')
	checkSame(as.numeric(res[as.POSIXct('2009-04-08'),]),0.036372)
	checkSame(as.numeric(res[as.POSIXct('2009-04-09'),]),0.033876)
}

testReferenceUniverse <- function()	
{
	checkSame(SingleNameCDS$tickerFromIds(16),'abb')
	checkSame(SingleNameCDS$tickerFromIds(c(16,8418)),c('abb','abbey-treasserv'))
	checkSame(SingleNameCDS$tierFromIds(22),'snrfor')
	checkSame(SingleNameCDS$tierFromIds(c(22,20)),c('snrfor','preft1'))
	checkSame(SingleNameCDS$ccyFromIds(40),'usd')
	checkSame(SingleNameCDS$ccyFromIds(c(40,47)),c('usd','eur'))
	res <- SingleNameCDS$referenceUniverse()
	checkShape(res,cols = 3,colnames = c('ticker','tier','ccy'))
	assert(NROW(res) > 0)
}

testSystemDBNames <- function()	
{
	this <- SingleNameCDS('gm')
	checkSame(this$systemDBName('tri'),'CDS.GM.5Y.TRI')	
	checkSame(this$systemDBName('dv01'),'CDS.GM.5Y.DV01')
	checkSame(this$systemDBName('spread'),'CDS.GM.5Y.SPREAD')
}

testSpecificCurves <- function()
{
	this <- SingleNameCDS('cah')
	res <- this$specificCurves(quote_type = 'spread',strike = 'par',doc_clause = 'mr',startDate = '2009-01-02',endDate= '2009-01-05')
	checkShape(res,rows = 2,cols = 11,colnames =  TermStructure$cds)
	checkSame(as.numeric(res[1,'5y']),0.0074160625)
	checkSame(as.numeric(res[1,'4y']),0.006950790425)
	
	res <- this$specificCurves(quote_type = 'spread',strike = 100,doc_clause = 'xr',startDate = '2009-04-09',endDate= '2009-04-11')
	checkShape(res,rows = 2,cols = 11,colnames =  TermStructure$cds)
	checkSame(as.numeric(res[1,'5y']),0.00728994139836801)
	checkSame(as.numeric(res[1,'4y']),0.00706806695678445)
}

testCdsTicker <- function()	
{
	this <- SingleNameCDS('cah')
	checkSame(this$cdsTicker(),'cah_snrfor_usd_xr')
}

testCdsTickerFromEquity <- function(){
	checkSame(SingleNameCDS$cdsTickerFromEquity('axl'),'axl-inc')
	checkSame(SingleNameCDS$cdsTickerFromEquity('gm'),'gm')
}

testIdsFromTickers <- function()
{
	checkSame(SingleNameCDS$idsFromTickers('abb'),16)
	checkSame(SingleNameCDS$idsFromTickers(c('abb','abbey-treasserv')),c(16,8418))
}

testIdsFromTier <- function()
{
	checkSame(SingleNameCDS$idsFromTier('snrfor'),22)
	checkSame(SingleNameCDS$idsFromTier(c('snrfor','preft1')),c(22,20))
}

testTopicName <- function()
{
	checkSame(SingleNameCDS('cah')$topicName(),'CDS.CAH.5Y')
}


testIdsFromCcy <- function()
{
	checkSame(SingleNameCDS$idsFromCcy('usd'),40)
	checkSame(SingleNameCDS$idsFromCcy(c('usd','eur')),c(40,47))
}

testMultipleSeries <- function()
{
	# To be changed to take fon_snrfor_usd inputs instead + speed up
	tickerList <- c("gm","cargil","f","s")
	res <- SingleNameCDS$multipleGenericSeries(tickerList,'spread')
	checkShape(res,colnames = tickerList)

	res <- SingleNameCDS$multipleAssetVolSeries(tickerList)
	checkShape(res,colnames = tickerList)
	
	res <- SingleNameCDS$multipleAssetSeries(tickerList)
	checkShape(res,colnames = tickerList)
	
	res <- SingleNameCDS$multipleAdjPriceSeries(tickerList,startDate ="2007-06-10",endDate = "2007-06-12")
	target <- getZooDataFrame(
		zoo(matrix(c(NA,NA,NA,NA,NA,NA,NA,NA),nrow = 2, ncol = 4),order.by = c("2007-06-11","2007-06-12"))
	)
	colnames(target) <- tickerList
	checkEquals(dim(res),c(2,4))
	checkEquals(index(res),index(target))
	checkEquals(colnames(res),colnames(target))
		
	res <- SingleNameCDS$multipleSharesOutstandingSeries(tickerList,startDate ="2007-06-10",endDate = "2007-06-12")
	target <- getZooDataFrame(
		zoo(matrix(c(565.743,565.743,NA,NA,1809.296,1809.296,2813.799,2813.799),nrow = 2, ncol = 4),order.by = c("2007-06-11","2007-06-12"))
	)
	colnames(target) <- tickerList
	checkSame(res,target)
	
	res <- SingleNameCDS$multipleVolSeries(tickerList,,startDate ="2007-06-10",endDate = "2007-06-12")
	target <- getZooDataFrame(
		zoo(matrix(c(0.390654,0.386283,NA,NA,0.327036,0.324270,0.316122,0.301985),nrow = 2, ncol = 4),order.by = c("2007-06-11","2007-06-12"))
	)
	colnames(target) <- tickerList
	checkSame(res,target)
	
	res <- SingleNameCDS$multipleLiabilitySeries(tickerList,startDate ="2007-06-10",endDate = "2007-06-12")
	target <- getZooDataFrame(
		zoo(matrix(c(188400,188400,NA,NA,284938,284938,42617,42617),nrow = 2, ncol = 4),order.by = c("2007-06-11","2007-06-12"))
	)
	colnames(target) <- tickerList
	checkSame(res,target)
	
	res <- SingleNameCDS$multipleClosingPrices(tickerList, startDate = '2007-06-10', endDate = '2007-06-12')
	target <- getZooDataFrame(
		zoo(matrix(c(31.77,31.43,NA,NA,8.4,8.32,22,21.6),nrow=2,ncol=4), order.by = c('2007-06-11', '2007-06-12'))
	)
	colnames(target) <- tickerList
	checkSame(res, target)
	
	res <- SingleNameCDS$multipleFairValuesSeries(tickerList, startDate = '2007-06-10', endDate = '2007-06-12')
	target <- getZooDataFrame(
		zoo(matrix(c(290.3858, 278.7717, NA,NA,210.959, 199.7855, 99.4468, 85.635), nrow = 2, ncol = 4), order.by = c('2007-06-11', '2007-06-12'))
	)
	colnames(target) <- tickerList
	checkSame(round(res,4),target)
	
}

testSecurityID <- function()
{
	checkSame(105175,SingleNameCDS$securityID('gm'))
	cds <- SingleNameCDS('gm')
	checkSame(105175,cds$securityID())
}

testEquityVol <- function()
{
	cds <- SingleNameCDS('gm')
	res <- cds$equityVol(startDate = '2009-03-10',endDate = '2009-03-25')
	checkSame(res[as.POSIXct('2009-03-20')],zoo(2.804130,as.POSIXct('2009-03-20')))
}

testAdjClosePrices <- function()
{
	cds <- SingleNameCDS('gm')
	res <- cds$adjClosePrices(startDate = '2009-03-10',endDate = '2009-03-25')
	checkSame(res[as.POSIXct('2009-03-20')],zoo(3.18,as.POSIXct('2009-03-20')))
}

testLiabilities <- function()
{
	cds <- SingleNameCDS('gm')
	res <- cds$liabilities(startDate = '2009-03-10',endDate = '2009-03-25')
	checkSame(res[as.POSIXct('2009-03-20')],zoo(176387,as.POSIXct('2009-03-20')))
}

testSharesOutstanding <- function()
{
	cds <- SingleNameCDS('gm')
	res <- cds$sharesOutstanding(startDate = '2009-03-10',endDate = '2009-03-25')
	checkSame(res[as.POSIXct('2009-03-20')],zoo(610.502,as.POSIXct('2009-03-20')))
}

testDTDInputData <- function()
{
	cds <- SingleNameCDS('gm')
	res <- cds$dtdInputData(startDate = '2009-03-10',endDate = '2009-03-25')
	checkShape(res,cols = 5,rows = 14,colnames = c('spreads','volatility','adjClosePrices','liabilities','sharesOutstanding'))
}

testDTDOutputData <- function()
{
	cds <- SingleNameCDS('gm')
	res <- cds$dtdOutputData(startDate = '2009-03-10',endDate = '2009-03-25')
	checkShape(res,colnames = c('dtds','richCheaps','adjTri','delta','a','sigma'))
}

testOldCdsTickerID <- function()
{
	cds <- SingleNameCDS('gm')
	checkSame(cds$oldCdsTickerID(),'gm_snrfor_usd_mr')
}

testEquityTicker <- function()
{
	checkSame('mccc',SingleNameCDS('mccc-medcomllc')$equityTicker())
}

testSpreadCalcInfo <- function()
{
	priceSeries <- zoo(c(0.68,0.4,0.9),as.POSIXct(c('2009-04-13','2009-04-15','2009-04-17')))
	spreadSeries <- zoo(c(0.68,0.9),as.POSIXct(c('2009-04-13','2009-04-17')))
	this <- SingleNameCDS('bzh')
	res <- this$spreadCalcInfo(priceSeries,spreadSeries)
	checkSame(res$isNeeded,TRUE)
	checkSame(res$priceSeries,priceSeries[2])	
	res <- this$spreadCalcInfo(priceSeries,NULL)
	checkSame(res$priceSeries,priceSeries)
	res <- this$spreadCalcInfo(NULL,spreadSeries)
	checkSame(res$isNeeded,FALSE)
	res <- this$spreadCalcInfo(NULL,NULL)
	checkSame(res$isNeeded,FALSE)
}

testCoefficientsOnDate <- function()
{
	res <- SingleNameCDS$dtdCoefficients('2008-07-01')
	checkShape(res,rows = 1,cols = 5,colnames = c("alpha","beta","strike","adjStrike","vol"))
}