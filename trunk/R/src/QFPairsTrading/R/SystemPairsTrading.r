constructor("SystemPairsTrading", function(
    pairName = NULL,
    transformationName = NULL,
    version = NULL,
    strategyPath = NULL,
    analyticsSource = "internal",
    dataTimeStamp = "15:00:00",
    principal = 0
,...){

    this <- extend(RObject(), "SystemPairsTrading",
        .tsdb = TimeSeriesDB(),
        .analyticsSource = analyticsSource,
        .dataTimeStamp = dataTimeStamp,
        .principal = principal,
        .version = version,
        .transformationName = transformationName,
        .pairName = pairName
    )
    
    if(inStaticConstructor(this))return(this)
    
    constructorNeeds(this,pairName = "character",transformationName = "character",version = "character",strategyPath = "character",
        analyticsSource = "character",dataTimeStamp = "character?",principal = "numeric")
    
    this$.strategyPath  <- squish(dataDirectory(),strategyPath); dir.create(this$.strategyPath,FALSE)
    this$.pairPath <- squish(this$.strategyPath,pairName,"/"); dir.create(this$.pairPath,FALSE)
    this$.pdfPath <- squish(this$.pairPath,"PDF/"); dir.create(this$.pdfPath,FALSE)
    this$.asciiPath <- squish(this$.pairPath,"ASCII/"); dir.create(this$.asciiPath,FALSE)    
    this$.stoPath <- squish(this$.pairPath,"STO/"); dir.create(this$.stoPath,FALSE)

    this
})

method("timeSeriesName", "SystemPairsTrading", function(this,transformation_output,...)
{
	paste(this$getPairMarketName(),this$.transformationName,this$.version,transformation_output,sep = "_")
})

method("uploadTSDB", "SystemPairsTrading", function(this,series = NULL,transformation_output = NULL,returnDates = NULL,...)
{
    market <- this$getPairMarketName(this$.pairName,this$.version)
    transformation <- paste(this$.transformationName,this$.version,sep = "_")
    
    if(is.null(returnDates)){
        series <- na.omit(series)
        tsName <- paste(market,transformation,transformation_output,sep = "_")    
        attributeList <- list(market = market,transformation = transformation,transformation_output = transformation_output)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(series,tsName,this$.analyticsSource,attributeList)
    }else{   
        firstDate <- businessDaysAgo(1,first(returnDates))
        updateCumTriFromDailyTri(
            this$.tsdb,firstDate,returnDates,
            paste(market,transformation,"tri_daily",sep = "_"),
            paste(market,transformation,"tri",sep = "_"),
            list(market = market,transformation = transformation,transformation_output = "tri"),
            source = this$.analyticsSource,this$.principal
        )
    }
})

method("exportASCII","SystemPairsTrading",function(this,series,fileName,...){
    exportZooInTradeStationASCII(na.omit(series),paste(this$.asciiPath,"/",fileName,".csv",sep = ""))
})

method("purgeTimeSeries", "SystemPairsTrading", function(this,transformation_output = NULL,...)
{
    market <- getPairMarketName(this$.pairName,this$.version)
    transformation <- paste(this$.transformationName,this$.version,sep = "_")
    
    tsName <- paste(market,transformation,transformation_output,sep = "_")    
    (this$.tsdb)$purgeTimeSeries(tsName,this$.analyticsSource)
})


method("purgeAllTimeSeries", "SystemPairsTrading", function(this,marketName,...)
{
	tsNames <- (this$.tsdb)$lookupTimeSeriesByAttributeList(list(market = marketName))    
	sapply(tsNames,function(x){(this$.tsdb)$purgeTimeSeries(x,this$.analyticsSource)})
})

method("getVersionNumeric", "SystemPairsTrading", function(this,versionString,...)			
{
	as.numeric(squish(unlist(strsplit(versionString,"\\."))))
})
			
method("getPairMarketName", "SystemPairsTrading", function(this,pairName = this$.pairName,versionString = this$.version,...)
{
	assert(this$.transformationName %in% c('rolling_regression','sporcor','liqinj'))
	if(this$.transformationName == 'rolling_regression')prefix <- 'PTT'
	if(this$.transformationName == 'sporcor')prefix <- 'SCT'
	if(this$.transformationName == 'liqinj')prefix <- 'LIT'
    paste(prefix,as.numeric(squish(unlist(strsplit(versionString,split="\\.")))),".",pairName,sep = "")
})

method("getData", "SystemPairsTrading", function(this,...){
    fail("The getData() method should be defined in underlying class")
})

method("updateTSDB","SystemPairsTrading",function(this,pair,pairResult,tc,delta,pScore,residual,loadIntercept = TRUE,...){
	this$uploadTSDB(pairResult[,"dailyTri"],"tri_daily")
	this$uploadTSDB(pairResult[,"zScore"],"z_score")
	this$uploadTSDB(pairResult[,"r2"],"r_square")
	this$uploadTSDB(pairResult[,"beta"],"beta")
	this$uploadTSDB(pairResult[,"beta.factorRank"],"scale")	
	this$uploadTSDB(pairResult[,"hedgeX"],"hedge")
	if(loadIntercept)this$uploadTSDB(pairResult[,"alpha"],"intercept")
	this$uploadTSDB(residual,"residual")    
	this$uploadTSDB(tc,"transaction_cost")
	this$uploadTSDB(pScore,"p_score")
	this$uploadTSDB(delta,"delta")
	this$uploadTSDB(returnDates = index(na.omit(pairResult[,"dailyTri"])))
})

method("updateASCII", "SystemPairsTrading", function(this,...){
    fail("The updateASCII() method should be defined in underlying class")
})

method("run", "SystemPairsTrading", function(this,...){
    fail("The run() method should be defined in underlying class")
})

getTimeStampPriorityZoo <- function(zooA,zooB,timeStamp){
	addThisZoo <- function(zooToAdd,zooFinal){
		bool <- !index(strip.times.zoo(zooToAdd)) %in% index(strip.times.zoo(zooFinal))
		rbind(zooToAdd[bool],zooFinal)
	}
	m <- na.omit(merge(zooA,zooB))
	zooFinal <- filter.zoo.byTime(m,timeStamp)
	dates <- seq(as.POSIXct("2008-01-01 12:00:00"), as.POSIXct(squish("2008-01-01 ",timeStamp)), 300)
	times <- strftime(as.POSIXlt(dates), "%H:%M:%S")
	timeStampPriorityOrder <- sort(times,TRUE)
	for (i in 1:NROW(timeStampPriorityOrder))zooFinal <- addThisZoo(filter.zoo.byTime(m,timeStampPriorityOrder[i]),zooFinal)
	strip.times.zoo(zooFinal)	
}

method("createRawFuturesContractTimeSeries","SystemPairsTrading",function(this,contract = "cl",year = "2009",month = "01",quote_type = 'close',...){
	ticker <- paste(contract,year,month,sep = "")
	attributeList <- list(
			ticker = ticker,contract = contract,quote_convention = "price",quote_type = quote_type,
			future_month = as.numeric(month),expiry = "actual",future_year = year,quote_side = 'mid',
			instrument = "futures",future_month_letter = MonthCode$letter(as.numeric(month))
	)
	if(quote_type == 'close')tsName <- paste(ticker,"price_mid",sep = "_")
	if(quote_type == 'last')tsName <- paste(ticker,"price_last_mid",sep = "_")
	TimeSeriesDB$createTimeSeries(tsName,attributeList)
})

method("createModifiedFuturesTimeSeries","SystemPairsTrading",function(this,contract = "cl",expiry = "1c",...){
	ticker <- paste(contract,expiry,sep = ".")
	attributeList <- list(
			ticker = ticker,contract = contract,quote_convention = "price",quote_type = "last",
			expiry = expiry,instrument = "futures"
	)
	tsName <- paste(ticker,"price","last",sep = "_")
	TimeSeriesDB$createTimeSeries(tsName,attributeList)
})




