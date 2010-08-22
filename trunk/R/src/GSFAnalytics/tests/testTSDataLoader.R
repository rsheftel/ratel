## Test file for the TSDataLoader object
library("GSFAnalytics")
library(QFCredit)

source(system.file("testHelpers.R", package="GSFCore"))
tempDir <- squish(dataDirectory(),'temp_TSDB/')
testDataPath <- squish(system.file("testdata", package="GSFAnalytics"),'/TSDataLoader/')


testTSDataLoader <- function()
{        
    loaderSample <- TSDataLoader()
                                      
    # init
    
    shouldBomb(loaderSample$init())
    shouldBomb(loaderSample$init(tsdb = CDS()))
    
    loaderSample <- TSDataLoader()
    loaderSample$init(TimeSeriesDB())
    
    checkEquals(TRUE,loaderSample$.isConnected)
    checkInherits(loaderSample$.tsdb,"TimeSeriesDB")
    
    # getDB()
    
    checkInherits(loaderSample$getDB(),"TimeSeriesDB")
    
    # getArrangeAttributes()
    
    checkEquals(loaderSample$getArrangeAttributes(),NULL)
                           
    # we can not use retrieveData, searchDate and matrixToZoo from TSDataLoader
    # those methods will be checked through child classes
}

testTSDataLoader.getMergedTimeSeries <- function()
{          
    tsdb <- TimeSeriesDB()
    tsName <- "irs_usd_rate_10y_mid"
    dataSource <- "internal"
    startDate <- "2007-01-01"
    endDate <- "2007-01-05"
    filter <- Close$NY.irs
    
    # one ts
    result <- getMergedTimeSeries(tsdb,tsName, dataSource, startDate, endDate, filter)
    target <- getZooDataFrame(zoo(c(5.16471,5.13072,5.07985,5.10473),as.POSIXct(c("2007-01-02","2007-01-03","2007-01-04","2007-01-05"))))
    colnames(target)<- tsName
    checkEquals(target,result)
    
    result <- getMergedTimeSeries(tsdb,tsName, dataSource, startDate, endDate)
    target <- getZooDataFrame(zoo(c(5.16471,5.13072,5.07985,5.10473),as.POSIXct(c("2007-01-02 15:00:00","2007-01-03 15:00:00","2007-01-04 15:00:00","2007-01-05 15:00:00"))))
    colnames(target)<- tsName  
    checkEquals(result,target)              
    
    # multiple ts
    tsName <- c("irs_usd_rate_5y_mid","irs_usd_rate_10y_mid")
    result <- getMergedTimeSeries(tsdb,tsName, dataSource, startDate, endDate)
    target <- getZooDataFrame(zoo(matrix(c(5.08503,5.04501,4.99124,5.02339,5.16471,5.13072,5.07985,5.10473),ncol = 2,nrow = 4),as.POSIXct(c("2007-01-02 15:00:00","2007-01-03 15:00:00","2007-01-04 15:00:00","2007-01-05 15:00:00"))))
    colnames(target)<- tsName  
    checkEquals(result,target)
    
    # multiple ts with one that is null
    tsName <- c("irs_usd_rate_5y_mid","irs_usd_rate_10y_mid","irs_usd_rate_10y_wrong")
    result <- getMergedTimeSeries(tsdb,tsName, dataSource, startDate, endDate)
    checkEquals(result,target)
    
    # should bombs and NULLs
    
    result <- getMergedTimeSeries(tsdb,"not_valid", dataSource, startDate, endDate, filter)
    checkEquals(result,NULL)
    result <- getMergedTimeSeries(tsdb,tsName, "markit", startDate, endDate, filter)
    checkEquals(result,NULL)
    result <- getMergedTimeSeries(tsdb,tsName, dataSource, startDate, endDate, "15:00:01")
    checkEquals(result,NULL)
    shouldBomb(getMergedTimeSeries(TRUE,tsName, dataSource, startDate, endDate, filter))
    shouldBomb(getMergedTimeSeries(tsdb,tsName, dataSource, TRUE, endDate, filter))    
    shouldBomb(getMergedTimeSeries(tsdb,tsName, dataSource, startDate, TRUE, filter))
}

test.uploadZooToTsdb.File <- function()
{
	if (isWindows()) uploadPath <- "V:/temp_TSDB/"
		else uploadPath <- "/data/temp_TSDB/"
	
	testdataPath <- squish(system.file("testdata", package="GSFAnalytics"),'/')
	
	#Next Test
	testZoo <- zoo(c(1,2,3),as.POSIXct(c('2008-01-01','2008-01-02','2008-01-03')))
	tsdbNames <- c('aapl close')
	tsdbSource <- 'test'
	
	uploadFilename='test.uploadDataToTsdb'
	uploadZooToTsdb(testZoo,tsdbNames,tsdbSource,uploadMethod='file',uploadFilename=uploadFilename,uploadPath=uploadPath)
	
	benchFile <- read.csv(squish(testdataPath,uploadFilename,'.csv'))
	outfile <- read.csv(squish(uploadPath,uploadFilename,'.csv'))
	checkSame(benchFile, outfile)
	file.remove(squish(uploadPath,uploadFilename,'.csv'))
	
	#Next test
	testZoo <- zoo(matrix(c(1,2,3,4,5,6),ncol=2),as.POSIXct(c('2008-01-01','2008-01-02','2008-01-03')))
	tsdbNames <- c('aapl close')
	tsdbSource <- 'test'
	
	uploadFilename='test.bombTsdbFile'
	shouldBombMatching(
		uploadZooToTsdb(testZoo,tsdbNames,tsdbSource,uploadMethod='file',uploadFilename=uploadFilename,uploadPath=uploadPath),
		'tsdbNames must be same length as zoo columns'
	)
	
	#Next test
	testZoo <- zoo(matrix(c(1,2,3,4,5,6),ncol=2),as.POSIXct(c('2008-01-01','2008-01-02','2008-01-03')))
	tsdbNames <- c('aapl close','intc close')
	tsdbSource <- c('test','test','test')
	shouldBombMatching(
		uploadZooToTsdb(testZoo,tsdbNames,tsdbSource,uploadMethod='file',uploadFilename=uploadFilename,uploadPath=uploadPath),
		'tsdbSources must of length 1 or same length as tsdbNames'
	)
	
	#Next test
	testZoo <- zoo(matrix(c(1,2,3,4,5,6),ncol=2),as.POSIXct(c('2008-01-01','2008-01-02','2008-01-03')))
	tsdbNames <- c('aapl close','intc close')
	tsdbSource <- c('test','test')
	uploadFilename='test.uploadDataToTsdb.multiple'
	uploadZooToTsdb(testZoo,tsdbNames,tsdbSource,uploadMethod='file',uploadFilename=uploadFilename,uploadPath=uploadPath)
	
	benchFile <- read.csv(squish(testdataPath,uploadFilename,'.csv'))
	outfile <- read.csv(squish(uploadPath,uploadFilename,'.csv'))
	checkSame(benchFile, outfile)
	file.remove(squish(uploadPath,uploadFilename,'.csv'))
	
}

test.uploadZooToTsdb.Direct <- function(){
	#Make sure commits to the DB are erased at the end
	purgeTestTimeSeries()
	on.exit(purgeTestTimeSeries())
	tsdb<-TimeSeriesDB()
	
	aapl.source <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
			ticker = "aapl"
		), data.source = "yahoo", start = "1999-01-01", end = "1999-03-01")
	
	colnames(aapl.source) <- "test"
	
	tsdbValues 	<- TSDataLoader$matrixToZoo(aapl.source)
	tsdbNames	<- colnames(tsdbValues)
	tsdbSources	<- 'test'
	
	uploadZooToTsdb(tsdbValues,tsdbNames,tsdbSources,uploadMethod='direct')
	
	aapl.target <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
			ticker = "aapl"
		), data.source = "test", start = "1999-01-01", end = "1999-03-01")
	
	#The odd [] are because sometimes the return values come from the cache so the order of the arrays needs to be forces
	checkSame(aapl.source[,], aapl.target[rownames(aapl.source),])
}

test.loadMatrixInTSDB <- function()
{
    this <- TSDataLoader()
    this$init(this$getDB())
    lastBusinessDate = "2007-09-24"
       
    (this$.tsdb)$.deleteTimeSeries("test-quantys_snrfor_usd_mr_tri_daily", are.you.sure = TRUE)	
	attributes = list(
		quote_type = 'tri_daily',
		ticker = 'test-quantys',
		tier = 'snrfor',
		ccy = 'usd',
		doc_clause = 'mr',
		cds_ticker = 'test-quantys_snrfor_usd_mr',
		instrument = "cds"
	)
	this$.tsdb$createTimeSeries(name = "test-quantys_snrfor_usd_mr_tri_daily", attributes = attributes)
	
    (this$.tsdb)$crear# create  test-quantys_snrfor_usd_mr_tri_daily ts

    checkTrue((this$.tsdb)$timeSeriesExists("test-quantys_snrfor_usd_mr_tri_daily"))
     
    tsArray <- array(list(NULL),dim = c(1,1),dimnames = list(
            "test-quantys_snrfor_usd_mr_tri_daily",
            "ivydb"
    ))
    tsArray[[1,1]] <- na.omit(zoo(data.frame(1),order.by = as.POSIXct("2007-01-01")))
    
    checkEquals("there is no data to copy from",this$loadMatrixInTSDB(matrix = NULL,timeFilter = NULL, source = NULL))

    shouldBomb(this$loadMatrixInTSDB(matrix = tsArray,timeFilter = NULL, source = "junk source"))
    shouldBomb(this$loadMatrixInTSDB(matrix = tsArray,timeFilter = TRUE, source = "internal"))
    shouldBomb(this$loadMatrixInTSDB(matrix = 1,timeFilter = NULL, source = "internal"))

    checkTrue(this$loadMatrixInTSDB(matrix = tsArray,timeFilter = NULL, source = "internal"))    

    (this$.tsdb)$.deleteTimeSeries("test-quantys_snrfor_usd_mr_tri_daily", are.you.sure = TRUE)
    
    checkTrue(!(this$.tsdb)$timeSeriesExists("test-quantys_snrfor_usd_mr_tri_daily"))  
}

test.getDataByName <- function(){
	tsdbData.direct <- TimeSeriesDB$retrieveOneTimeSeriesByName("aapl close","yahoo")
	tsdbData.function <- TSDataLoader$getDataByName('tsdb',"aapl close",'yahoo') 
	checkSame(tsdbData.direct,tsdbData.function)

	tsdbData.function <- TSDataLoader$getDataByName('tsdb',"aapl close",'yahoo','2007-02-01','2007-02-28') 
	checkSame(19,length(tsdbData.function))	
		
	uploadFilename <- tempfile('test.getDataByName.',tmpdir='')
	uploadFilename <- rightStr(uploadFilename,nchar(uploadFilename)-1)
	uploadZooToTsdb(tsdbData.direct,"aapl close","yahoo",uploadMethod='file',uploadFilename=uploadFilename, uploadPath=tempDir)
	tsdbData.file <- TSDataLoader$getDataByName(container=squish(tempDir,uploadFilename,'.csv'),'aapl close','yahoo')
	checkSame(tsdbData.direct,tsdbData.file)
	
	tsdbData.file <- TSDataLoader$getDataByName(container=squish(tempDir,uploadFilename,'.csv'),'aapl close','yahoo',start='2007-02-01',end='2007-02-28')
	checkSame(19,length(tsdbData.file))	
	checkSame(as.POSIXct('2007-02-01 14:00:00'), first(index(tsdbData.file)))
	checkSame(as.POSIXct('2007-02-28 14:00:00'), last(index(tsdbData.file)))
	
	tsdbData.file <- TSDataLoader$getDataByName(container=squish(tempDir,uploadFilename,'.csv'),'aapl close','yahoo',start='2007-02-01')	
	checkSame(37,length(tsdbData.file))	
	checkSame(as.POSIXct('2007-02-01 14:00:00'), first(index(tsdbData.file)))
	checkSame(as.POSIXct('2007-03-26 14:00:00'), last(index(tsdbData.file)))

	tsdbData.file <- TSDataLoader$getDataByName(container=squish(tempDir,uploadFilename,'.csv'),'aapl close','yahoo',end='2007-02-20')		
	checkSame(as.POSIXct('1984-09-07 14:00:00'), first(index(tsdbData.file)))
	checkSame(as.POSIXct('2007-02-20 14:00:00'), last(index(tsdbData.file)))
		
	checkSame(first(TSDataLoader$getDataByName('systemdb','RE.TEST.TY.1C',field='close')),38.859375)
}

test.getDataByAttributeList <- function(){
	aapl.source <- TimeSeriesDB$retrieveTimeSeriesByAttributeList(attributes = list(ticker = "aapl", quote_type="close"), data.source = "yahoo", arrange.by=NULL)
	tsdbData.function <- TSDataLoader$getDataByAttributeList(container='tsdb',attributeList = list(ticker = "aapl", quote_type="close"), source='yahoo',arrangeBy=NULL)
	checkSame(aapl.source,tsdbData.function)
		
	uploadFilename <- tempfile('test.getDataByName.',tmpdir=tempDir)
	TimeSeriesFile$writeTimeSeries(aapl.source,file = uploadFilename)
	tsdbData.file <- TSDataLoader$getDataByAttributeList(container=uploadFilename, attributeList = list(ticker = "aapl", quote_type="close"), source='yahoo',arrangeBy=NULL)
	checkSame(aapl.source, tsdbData.file)
}

test.getDataByAttributeListWithDates <- function(){
	#Test getting from file with dates
	aapl.source <- TimeSeriesDB$retrieveTimeSeriesByAttributeList(attributes = list(ticker = "aapl"), data.source = "yahoo", arrange.by=NULL)	
	uploadFilename <- tempfile('test.getDataByName.',tmpdir=tempDir)
	TimeSeriesFile$writeTimeSeries(aapl.source,file = uploadFilename)
	aapl.file <- TSDataLoader$getDataByAttributeList(container=uploadFilename, attributeList = list(ticker = "aapl"), source='yahoo',arrangeBy=NULL)
	checkSame(aapl.file, aapl.source) 
	
	subset.source <- TSDataLoader$getDataByAttributeList(container='tsdb', attributeList = list(ticker = "aapl"), source = "yahoo",
		arrangeBy='quote_type', start='2007-01-01', end='2007-02-28')	
	subset.file <- TSDataLoader$getDataByAttributeList(container=uploadFilename, attributeList = list(ticker = "aapl"), source='yahoo',
		arrangeBy='quote_type',start='2007-01-01', end='2007-02-28')
	checkSameLooking(subset.source, subset.file)
	
	#Must be arrangeby with start or end date
	shouldBomb(TSDataLoader$getDataByAttributeList(container=uploadFilename, attributeList = list(ticker = "aapl"), source='yahoo',arrangeBy=NULL,start='2007-01-01'))
	shouldBomb(TSDataLoader$getDataByAttributeList(container=uploadFilename, attributeList = list(ticker = "aapl"), source='yahoo',arrangeBy=NULL,end='2007-01-01'))
	
	#Check start/end date
	testdata <- TSDataLoader$getDataByAttributeList(container=squish(testDataPath,'multipleTimeSeries.csv'), attributeList=attributeList, source="test",
		start='2008-01-15', end='2008-02-22', arrangeBy='ticker')
	checkSame(2632.85, sum(testdata[['ed200809']]))
}
