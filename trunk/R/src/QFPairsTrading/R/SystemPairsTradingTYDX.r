constructor("SystemPairsTradingTYDX", function(pairName = "TYDX",...){
    this <- extend(RObject(),"SystemPairsTradingTYDX")
    if(!inStaticConstructor(this)){
        assert(pairName %in% c("TYDX","DXTY"))
        this$.leadMarket <- ifelse(pairName == "TYDX","dollarFuturesMarketValue","treasuryFuturesMarketValue")
        this$.lagMarket <- ifelse(pairName == "TYDX","treasuryFuturesMarketValue","dollarFuturesMarketValue")
        this$.systemHelper <- SystemPairsTrading(
            pairName,
            "rolling_regression",
            "1.0",
            squish("Market Systems/Linked Market Systems/FX/StrategyTYDX/")
        ,...)
    }
    this
})

method("run","SystemPairsTradingTYDX",function(this,startDate,
    window = 20,tcLead = 0,tcLag = 0,updateTSDB = FALSE, updateASCII = FALSE, generatePDF = FALSE
,...){

    needs(window = "numeric",tcLag = "numeric",tcLead = "numeric",updateTSDB ="logical",updateASCII = "logical",generatePDF = "logical")

    data <- this$getData(startDate)

	pair <- Pair(
        seriesY = data[,this$.lagMarket],seriesX = data[,this$.leadMarket],
        triY = data[,this$.lagMarket],triX = data[,this$.leadMarket],
        normalizeToY = TRUE,
        multiplierY = zoo(1,index(data[,this$.lagMarket])),multiplierX = zoo(1,index(data[,this$.leadMarket]))
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
	
    m <- na.omit(merge(pairResult[,"residual"],pairResult[,"hedgeX"]))

    tc <- abs(tcLead * m[,2]) + abs(tcLag)
    delta <- pairResult[,"hedgeX"]

    if(updateTSDB)this$updateTSDB(pair,pairResult,tc,delta)
    if(updateASCII)this$updateASCII(pair,pairResult,tc,delta)    
    return(list(pairResult = pairResult,tc = tc,delta = delta))
})

method("getData","SystemPairsTradingTYDX",function(this,startDate,...){
    tyData <- getCSIData("TY",fields = "Close")
    dxData <- getCSIData("DX",fields = "Close")    
    zooFinal <- na.omit(merge(dxData,tyData)) 	    
    zooFinal[,1] <- getCSIFuturesMultiplier("DX") * zooFinal[,1]
    zooFinal[,2] <- getCSIFuturesMultiplier("TY") * zooFinal[,2]
    data <- strip.times.zoo(zooFinal)
    colnames(data) <- c("dollarFuturesMarketValue","treasuryFuturesMarketValue")
    if(!is.null(startDate))data <- data[index(data) >= as.POSIXct(startDate)]
    return(data)
})

method("updateTSDB","SystemPairsTradingTYDX",function(this,pair,pairResult,tc,delta,...){

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

method("updateASCII","SystemPairsTradingTYDX",function(this,pair,pairResult,tc,delta,...){

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