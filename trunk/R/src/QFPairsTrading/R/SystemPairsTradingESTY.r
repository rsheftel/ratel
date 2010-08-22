# Slippage estimate is 0.5 * (mean(hedge * tcES) + tcTY) where tcES = 12.5 and tcTY = 15.7 
constructor("SystemPairsTradingESTY", function(...){
    this <- extend(RObject(),"SystemPairsTradingESTY")
    
    if(!inStaticConstructor(this)){
        this$.systemHelper <- SystemPairsTrading(
            "ESTY",
            "rolling_regression",
            "1.0",
            "Market Systems/Linked Market Systems/Equities/StrategyESTY/"
        ,...)
    }
    this
})  

method("run","SystemPairsTradingESTY",function(this,startDate,
    window = 20,tcES = 0,tcTY = 0,updateTSDB = FALSE, updateASCII = FALSE, generatePDF = FALSE
,...){

    needs(window = "numeric",tcES = "numeric",tcTY = "numeric",updateTSDB ="logical",updateASCII = "logical",generatePDF = "logical")

    data <- this$getData(startDate)

	pair <- Pair(
        seriesY = data[,"treasuryFuturesMarketValue"],seriesX = data[,"equityFuturesMarketValue"],
        triY = data[,"treasuryFuturesMarketValue"],triX = data[,"equityFuturesMarketValue"],
        normalizeToY = TRUE,
        multiplierY = zoo(1,index(data[,"treasuryFuturesMarketValue"])),multiplierX = zoo(1,index(data[,"equityFuturesMarketValue"]))
	)
	
	pair$runChangesRollingRegression(
        window = window,
        constant = TRUE,
        storeIn = (this$.systemHelper)$.pairName,
        generatePDF = generatePDF,
        pathPDF = (this$.systemHelper)$.pdfPath,
        mfrowPDF = c(3,3)
	)
	
	pairResult <- pair$getModelResults((this$.systemHelper)$.pairName,window)
	
    m <- na.omit(merge(pairResult[,"residual"],pairResult[,"hedgeX"],pairResult[,"zScore"]))
	
	tc <- abs(m[,2] * tcES) + abs(tcTY)

    delta <- pairResult[,"hedgeX"]
    
    if(updateTSDB)this$updateTSDB(pair,pairResult,tc,delta)    	
    if(updateASCII)this$updateASCII(pair,pairResult,tc,delta)    
    return(list(pairResult = pairResult,tc = tc,delta = delta))
})

method("getBloombergBarData","SystemPairsTradingESTY",function(this,name,startDate = NULL,...){
	if(!is.null(startDate))range <- Range(as.POSIXct(startDate),Range$today()) else range <- Range(as.POSIXct('1990-01-01'),Range$today())
	if(name == 'ES.1C') .bbrgName <- 'ES1 Index' else .bbrgName <- 'TY1 Comdty'
	BloombergSecurity(.bbrgName)$bars('5minute',range)[,'close']
})

method("getData","SystemPairsTradingESTY",function(this,startDate,useDataDirectory = FALSE,...){
    if(useDataDirectory){    
        dataDir <- squish(dataDirectory(),"Futures/TradeStation/")
        equityFutures5Continuous <- importZooFromTradeStationFile(squish(dataDir,"ES.E.csv"))[,"Close"]    
        treasuryFuturesE5Continuous <- importZooFromTradeStationFile(squish(dataDir,"TY.E.csv"))[,"Close"]
        treasuryFuturesP5Continuous <- importZooFromTradeStationFile(squish(dataDir,"TY.P.csv"))[,"Close"]
        
        treasurySwitchDate <- as.POSIXct("2002-01-02 00:00:00")
        z1 <- treasuryFuturesP5Continuous[index(treasuryFuturesP5Continuous)<=treasurySwitchDate]
        z2 <- treasuryFuturesE5Continuous[index(treasuryFuturesE5Continuous)>=treasurySwitchDate]
        treasuryFutures5Continuous <- rbind(z1[-NROW(z1)],z2)
    }else{			
		equityFutures5Continuous <- this$getBloombergBarData('ES.1C',startDate)
		treasuryFutures5Continuous <- this$getBloombergBarData('TY.1C',startDate)        
    }
	data <- getTimeStampPriorityZoo(equityFutures5Continuous,treasuryFutures5Continuous,this$.systemHelper$.dataTimeStamp)    
	data[,1] <- JMarket$contractSize_by_String("ES.1C") * data[,1]
	data[,2] <- JMarket$contractSize_by_String("TY.1C") * data[,2]
    colnames(data) <- c("equityFuturesMarketValue","treasuryFuturesMarketValue")
    if(!is.null(startDate))data <- data[index(data) >= as.POSIXct(startDate)]
    return(data)
})

method("updateTSDB","SystemPairsTradingESTY",function(this,pair,pairResult,tc,delta,...){

    (this$.systemHelper)$uploadTSDB(pairResult[,"dailyTri"],"tri_daily")
    (this$.systemHelper)$uploadTSDB(pairResult[,"zScore"],"z_score")
	(this$.systemHelper)$uploadTSDB(pairResult[,"r2"],"r_square")
    (this$.systemHelper)$uploadTSDB(pairResult[,"beta.factorRank"],"scale")	
    (this$.systemHelper)$uploadTSDB(pairResult[,"hedgeX"],"hedge")
    (this$.systemHelper)$uploadTSDB(pairResult[,"alpha"],"intercept")
    (this$.systemHelper)$uploadTSDB(pairResult[,"residual"],"residual")    
    (this$.systemHelper)$uploadTSDB(tc,"transaction_cost")
    (this$.systemHelper)$uploadTSDB(delta,"delta")
    (this$.systemHelper)$uploadTSDB(returnDates = index(na.omit(pairResult[,"dailyTri"])))
})

method("updateASCII","SystemPairsTradingESTY",function(this,pair,pairResult,tc,delta,...){

    (this$.systemHelper)$exportASCII(pairResult[,"tri"],"tri")
    (this$.systemHelper)$exportASCII(pairResult[,"zScore"],"zScore")    
    (this$.systemHelper)$exportASCII(pairResult[,"r2"],"r2")    
    (this$.systemHelper)$exportASCII(pairResult[,"residual"],"residual")    
    (this$.systemHelper)$exportASCII(pairResult[,"alpha"],"intercept")        
    (this$.systemHelper)$exportASCII(pairResult[,"hedgeX"],"hedgeX")    
    (this$.systemHelper)$exportASCII(pair$.seriesY,"triy")    
    (this$.systemHelper)$exportASCII(pairResult[,"beta.factorRank"],"scale")
    (this$.systemHelper)$exportASCII(pair$.seriesX,"trix")
    (this$.systemHelper)$exportASCII(tc,"tc")
    (this$.systemHelper)$exportASCII(delta,"delta")
})

method("createModifiedFuturesTimeSeries","SystemPairsTradingESTY",function(this,contract = "es",expiry = "1c",...){
    ticker <- paste(contract,expiry,sep = ".")
    attributeList <- list(
        ticker = ticker,contract = contract,quote_convention = "price",quote_type = "last",
        expiry = expiry,instrument = "futures"
    )    
    tsName <- paste(ticker,"price","last",sep = "_")
    TimeSeriesDB$createTimeSeries(tsName,attributeList)
})

method("createActualFuturesTimeSeries","SystemPairsTradingESTY",function(this,
    contract = "es",future_year = "2008",future_month = "09",expiry_date = "2008-09-19 00:00:00",future_month_letter = "u"
,...){
    contract = "es"
    future_year = "2008"
    future_month = "09"
    expiry_date = "2008-09-19 00:00:00"
    future_month_letter = "u"
    ticker <- paste(contract,future_year,future_month,sep = "")
    attributeList <- list(
        ticker = ticker,contract = contract,quote_convention = "price",quote_type = "last",future_month = as.numeric(future_month),expiry = "actual",
        expiry_date = expiry_date,future_year = as.numeric(future_year),instrument = "futures",future_month_letter = future_month_letter
    )
    tsName <- paste(ticker,"price","last",sep = "_")
    TimeSeriesDB$createTimeSeries(tsName,attributeList)
})