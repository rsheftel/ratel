# Slippage estimate is mean(0.5 * hedge * dv01 * 1.25bps)
constructor("SystemPairsTradingESIG", function(...){
    this <- extend(RObject(),"SystemPairsTradingESIG")
    
    if(!inStaticConstructor(this)){
        this$.systemHelper <- SystemPairsTrading(
            "ESIG",
            "rolling_regression",
            "1.0",
            "Market Systems/Linked Market Systems/Credit/StrategyESIG/"
        ,...)
    }
    this
})

method("run","SystemPairsTradingESIG",function(this,startDate,
    window = 20,tcBps = 1.25,useCash = FALSE, updateTSDB = FALSE, updateASCII = FALSE, generatePDF = FALSE
,...){

    needs(window = "numeric",tcBps = "numeric",useCash = "logical",updateTSDB ="logical",updateASCII = "logical",generatePDF = "logical")

    data <- this$getData(startDate,useCash)

	pair <- Pair(
        seriesY = data[,"equityFuturesMarketValue"],seriesX = data[,"creditTri"],
        triY = data[,"equityFuturesMarketValue"],triX = data[,"creditTri"],
        normalizeToY = TRUE,
        multiplierY = zoo(1,index(data[,"equityFuturesMarketValue"])),multiplierX = zoo(1,index(data[,"creditTri"]))
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
	
    m <- na.omit(merge(pairResult[,"residual"],pairResult[,"hedgeX"],data[,"creditDv01"],pairResult[,"zScore"],data[,"equityFuturesMarketValue"]))
	pScore <- m[,1] / (abs(m[,2]) * m[,3] * tcBps)
    delta <- (m[,2] * 100) / m[,5]
    
    if(updateTSDB)this$updateTSDB(pair,pairResult,pScore,delta)    	
    if(updateASCII)this$updateASCII(pair,pairResult,pScore,delta)    
    return(list(pairResult = pairResult,pScore = pScore,delta = delta))
})

method("getData","SystemPairsTradingESIG",function(this,startDate,useCash,...){

    equityFuturesCloseContinuous <- getCSIData("ES",startDate = startDate,fields = "Close")
	equityFuturesMarketValue <- equityFuturesCloseContinuous * getCSIFuturesMultiplier("ES")
	
	# get Credit data
	
    if(!useCash){
    	ticker <- "cdx-na-ig"
    	tsNames <- c(squish(ticker,"_tri_daily_5y_otr"),squish(ticker,"_market_spread_5y_otr"),squish(ticker,"_dv01_5y_otr"))
    	dataCredit <- getMergedTimeSeries((this$.systemHelper)$.tsdb,tsNames,(this$.systemHelper)$.analyticsSource,startDate = startDate,filter = (this$.systemHelper)$.dataTimeStamp)
    	data <- na.omit(merge(dataCredit[,1],dataCredit[,2]*10000,dataCredit[,3],equityFuturesMarketValue))
	}else{
    	ticker <- "lehman_us_credit_intermediate"
        tsNames <- c(squish(ticker,"_adjusted_tri_daily"),squish(ticker,"_adjusted_spread"),squish(ticker,"_adjusted_dv01"))
        dataCredit <- getMergedTimeSeries((this$.systemHelper)$.tsdb,tsNames,(this$.systemHelper)$.analyticsSource,filter = (this$.systemHelper)$.dataTimeStamp)
        data <- na.omit(merge(getCumTriFromDailyTri(-dataCredit[,1]),dataCredit[,2]*100,dataCredit[,3],equityFuturesMarketValue))
    }
    colnames(data) <- c("creditTri","creditSpread","creditDv01","equityFuturesMarketValue")
 
    return(data)
})

method("updateTSDB","SystemPairsTradingESIG",function(this,pair,pairResult,pScore,delta,...){

    (this$.systemHelper)$uploadTSDB(pairResult[,"dailyTri"],"tri_daily")
    (this$.systemHelper)$uploadTSDB(pairResult[,"zScore"],"z_score")
	(this$.systemHelper)$uploadTSDB(pairResult[,"r2"],"r_square")
    (this$.systemHelper)$uploadTSDB(pairResult[,"hedgeX"],"hedge")
    (this$.systemHelper)$uploadTSDB(pScore,"p_score")
    (this$.systemHelper)$uploadTSDB(delta,"delta")
    (this$.systemHelper)$uploadTSDB(returnDates = index(na.omit(pairResult[,"dailyTri"])))
})

method("updateASCII","SystemPairsTradingESIG",function(this,pair,pairResult,pScore,delta,...){

    (this$.systemHelper)$exportASCII(pairResult[,"tri"],"tri")
    (this$.systemHelper)$exportASCII(pairResult[,"zScore"],"zScore")    
    (this$.systemHelper)$exportASCII(pairResult[,"r2"],"r2")    
    (this$.systemHelper)$exportASCII(pairResult[,"hedgeX"],"hedgeX")    
    (this$.systemHelper)$exportASCII(pair$.seriesY,"triy")    
    (this$.systemHelper)$exportASCII(pair$.seriesX,"trix")
    (this$.systemHelper)$exportASCII(pScore,"pscore")
    (this$.systemHelper)$exportASCII(delta,"delta")
})

