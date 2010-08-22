## Test file for the EquityDataLoader object
library("QFEquity")

testEquityDataLoader <- function()
{          
    # test lookup functions

    loaderSample <- EquityDataLoader()     
    equitySample <- Equity(securityId = 102733)
                     
    checkEquals(102733,loaderSample$getSecurityIDFromTicker("cah"))
    checkEquals("cah",loaderSample$getTickerFromSecurityID(102733))
    checkEquals("16:00:00",loaderSample$.filter)
    
    # test close price

	equitySample <- Equity(securityId = 102733)

    target <- getZooDataFrame(zoo(c(57.18),order.by = c("2005-01-03")))    
    colnames(target) <- "Close Price"
   
    checkEquals(target,
        loaderSample$getClosePrices(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
    
    checkEquals(target,
        loaderSample$getClosePrices(equityObj = equitySample,source = "internal",startDate = "2005-01-01",endDate = "2005-01-03")
    )

    # test volume

    target <- getZooDataFrame(zoo(c(1495200),order.by = c("2005-01-03")))    
    colnames(target) <- "Volume"
   
    checkEquals(target,
        loaderSample$getVolumes(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
    
    # shares outstanding
    
    target <- getZooDataFrame(zoo(c(432.076),order.by = c("2005-01-03")))    
    colnames(target) <- "Shares Outstanding"    
   
    checkEquals(target,
        loaderSample$getSharesOutstanding(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
    
    # total return
    
    target <- getZooDataFrame(zoo(c(-0.016681),order.by = c("2005-01-03")))    
    colnames(target) <- "Total Return"  

    checkEquals(target,
        loaderSample$getTotalReturns(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )
    
    # cumulative total returns

    target <- getZooDataFrame(zoo(c( 3.425733),order.by = c("2005-01-03")))    
    colnames(target) <- "Cumulative Total Return Factor"
    
    checkEquals(target,
        loaderSample$getCumulativeTotalReturnFactors(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2005-01-03")
    )    
}

testAdjClosePrice <- function()
{
    loaderSample <- EquityDataLoader()
    equitySample <- Equity(securityId = 110433)
    
    shouldBomb(loaderSample$getAdjClosePrices(equityObj = Equity(),source = "ivydb",startDate = "2005-01-01",endDate = "2006-01-01"))
    shouldBomb(loaderSample$getAdjClosePrices(equityObj = equitySample,source = "db",startDate = "2005-01-01",endDate = "2006-01-01"))
    shouldBomb(loaderSample$getAdjClosePrices(equityObj = equitySample,source = "ivydb",startDate = TRUE,endDate = "2006-01-01"))
    shouldBomb(loaderSample$getAdjClosePrices(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = TRUE))
    shouldBomb(loaderSample$getAdjClosePrices(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2000-01-01"))
}

testAdjOpenPrice <- function()
{
	loaderSample <- EquityDataLoader()
	equitySample <- Equity(securityId = 110433)
	
	shouldBomb(loaderSample$getAdjOpenPrices(equityObj = Equity(),source = "ivydb",startDate = "2005-01-01",endDate = "2006-01-01"))
	shouldBomb(loaderSample$getAdjOpenPrices(equityObj = equitySample,source = "db",startDate = "2005-01-01",endDate = "2006-01-01"))
	shouldBomb(loaderSample$getAdjOpenPrices(equityObj = equitySample,source = "ivydb",startDate = TRUE,endDate = "2006-01-01"))
	shouldBomb(loaderSample$getAdjOpenPrices(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = TRUE))
	shouldBomb(loaderSample$getAdjOpenPrices(equityObj = equitySample,source = "ivydb",startDate = "2005-01-01",endDate = "2000-01-01"))
}

test.createEquityTimeSeriesIfNotExists <- function(){

    loaderSample <- EquityDataLoader()
    (loaderSample$init(loaderSample$getDB()))
    
    loaderSample$.tsdb$.deleteTimeSeries("ivydb_105175_test_value", are.you.sure = TRUE)
    checkTrue(!loaderSample$.tsdb$timeSeriesExists(name = "ivydb_105175_test_value"))

    loaderSample$createEquityTimeSeriesIfNotExists(securityID = 105175, quote_type = "test_value")
    checkTrue(loaderSample$.tsdb$timeSeriesExists("ivydb_105175_test_value"))
    
    test.attributes <- loaderSample$.tsdb$lookupAttributesForTimeSeries(time.series = "ivydb_105175_test_value")
    checkEquals(as.numeric(test.attributes["ivydb_105175_test_value", "security_id"]), 105175)
    checkEquals(as.character(test.attributes["ivydb_105175_test_value", "instrument"]), "equity")
    checkEquals(as.character(test.attributes["ivydb_105175_test_value", "quote_type"]), "test_value")
    
    # This should succeed, but not do anything
    
    checkEquals(NULL,loaderSample$createEquityTimeSeriesIfNotExists(securityID = 105175, quote_type = "test_value"))  
    loaderSample$.tsdb$.deleteTimeSeries("ivydb_105175_test_value", are.you.sure = TRUE)
}

test.TotalLiabilities <- function()
{
    loaderSample <- EquityDataLoader()
	equitySample <- Equity(securityId = 102296)
    
    shouldBomb(loaderSample$getTotalLiabilities(equityObj = Equity(),source = "bloomberg",startDate = "2005-01-01",endDate = "2006-01-01"))
    shouldBomb(loaderSample$getTotalLiabilities(equityObj = equitySample,source = "db",startDate = "2005-01-01",endDate = "2006-01-01"))
    shouldBomb(loaderSample$getTotalLiabilities(equityObj = equitySample,source = "bloomberg",startDate = TRUE,endDate = "2006-01-01"))
    shouldBomb(loaderSample$getTotalLiabilities(equityObj = equitySample,source = "bloomberg",startDate = "2005-01-01",endDate = TRUE))
    shouldBomb(loaderSample$getTotalLiabilities(equityObj = equitySample,source = "bloomberg",startDate = "2005-01-01",endDate = "2000-01-01"))
    
    target <- getZooDataFrame(zoo(c(4145),order.by = c("2005-01-03")))    
    colnames(target) <- "Total Liabilities"
   
    checkEquals(target,
        loaderSample$getTotalLiabilities(equityObj = equitySample,source = "bloomberg",startDate = "2005-01-01",endDate = "2005-01-03")
    )
}

test.getETFTable <- function()
{
    result <- EquityDataLoader$getETFTable()
    needs(result = "data.frame")
    checkShape(result,cols = 2,colnames = c("bloomberg","option_metrics_id"))
}


test.getETFTRI <- function()
{
	library(SystemDB)
	ticker <- "xle"; source <- "internal"; startDate <- "2008-01-01"; endDate <- "2008-01-10"
	result <- EquityDataLoader$getETFTRI(ticker,source,startDate,endDate,stitched = TRUE)	
	checkShape(result,7,1,colnames = "TRI")
	startDate <- "1998-12-15"; endDate <- "1998-12-30"
	result <- EquityDataLoader$getETFTRI(ticker,source,startDate,endDate,stitched = TRUE)
	checkShape(result,cols = 1,colnames = "TRI")	
	startDate <- "1998-12-22"; endDate <- "1998-12-22"
	result <- EquityDataLoader$getETFTRI(ticker,source,startDate,endDate,stitched = TRUE)
	checkShape(result,rows = 1,cols = 1,colnames = "TRI")
	shouldBomb(EquityDataLoader$getETFTRI("badticker",source,startDate,endDate,stitched = TRUE))
	shouldBomb(EquityDataLoader$getETFTRI(ticker,"badsource",startDate,endDate,stitched = TRUE))
	shouldBomb(EquityDataLoader$getETFTRI(ticker,source,TRUE,endDate,stitched = TRUE))	
	shouldBomb(EquityDataLoader$getETFTRI(ticker,source,startDate,TRUE,stitched = TRUE))	
	shouldBomb(EquityDataLoader$getETFTRI(ticker,source,"2008-01-01","2007-01-01",stitched = TRUE))
	shouldBomb(EquityDataLoader$getETFTRI(ticker,source,startDate,endDate,stitched = "junk"))
	shouldBomb(EquityDataLoader$getETFTRI(ticker,endDate = "1980-01-01",stitched = TRUE))
	shouldBomb(EquityDataLoader$getETFTRI(ticker,endDate = "1998-01-01",stitched = FALSE))
	# Financed Tri
	startDate <- "1998-12-15"; endDate <- "1998-12-30"
	result <- EquityDataLoader$getETFFinancedTRI(ticker,source,startDate,endDate,stitched = TRUE)
	checkShape(result,rows = NULL,cols = 1,colnames = "TRI")
}

test.getOpenPrices <- function()
{
	e <- Equity(securityId = 101475)
	result <- EquityDataLoader$getOpenPrices(e,"ivydb","2008-11-20","2008-11-25")
	target <- zoo(c(29.47,27.54,29.5,30.8),as.POSIXct(c("2008-11-20","2008-11-21","2008-11-24","2008-11-25")))
	checkSame(result,getZooDataFrame(target,"Open Price"))
	shouldBomb(EquityDataLoader$getOpenPrices(e,"junk","2008-11-20","2008-11-25"))
	shouldBomb(EquityDataLoader$getOpenPrices(e,"ivydb",TRUE))
	shouldBomb(EquityDataLoader$getOpenPrices(1,"ivydb"))
}

test.getHighPrices <- function()
{
	e <- Equity(securityId = 101475)
	result <- EquityDataLoader$getHighPrices(e,"ivydb","2008-11-20","2008-11-25")
	target <- zoo(c(29.77,29.82,30.38,30.8),as.POSIXct(c("2008-11-20","2008-11-21","2008-11-24","2008-11-25")))
	checkSame(result,getZooDataFrame(target,"High Price"))
}

test.getLowPrices <- function()
{
	e <- Equity(securityId = 101475)
	result <- EquityDataLoader$getLowPrices(e,"ivydb","2008-11-20","2008-11-25")
	target <- zoo(c(27.28,26.66,29.27,29.19),as.POSIXct(c("2008-11-20","2008-11-21","2008-11-24","2008-11-25")))
	checkSame(result,getZooDataFrame(target,"Low Price"))
}

test.getCreditEquityMappingTable <- function()
{	
    res <- EquityDataLoader$getCreditEquityMappingTable()
	checkShape(res,cols =3,colnames = c('markit','bloomberg','option_metrics_id'))	
}

test.getSecurityIDsUniverse <- function()
{
	res <- EquityDataLoader$getSecurityIDsUniverse()
	checkSame(TRUE,all(class(res) == 'numeric'))
	checkSame(TRUE,NROW(res)>0)
}