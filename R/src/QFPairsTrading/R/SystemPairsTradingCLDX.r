# Slippage estimate is 0.5 * (mean(hedge * tcCL) + tcDX) where tcCL = 10 and tcDX = 5 
constructor("SystemPairsTradingCLDX", function(pairName = "CLDX",testPair = NULL,...){
	library(SystemDB)
    this <- extend(RObject(),"SystemPairsTradingCLDX")
    if(!inStaticConstructor(this)){
        assert(pairName %in% c("CLDX","DXCL"))
        this$.leadMarket <- ifelse(pairName == "CLDX","dollarFuturesMarketValue","crudeFuturesMarketValue")
        this$.lagMarket <- ifelse(pairName == "CLDX","crudeFuturesMarketValue","dollarFuturesMarketValue")
        this$.systemHelper <- SystemPairsTrading(
            ifelse(!is.null(testPair),testPair,pairName),
            "rolling_regression",
            "1.0",
            squish("Market Systems/Linked Market Systems/FX/StrategyCLDX/")
        ,...)
    }
    this
})

method("run","SystemPairsTradingCLDX",function(this,startDate,
    window = 20,tcLead = 0,tcLag = 0,updateTSDB = FALSE,generatePDF = FALSE
,...){

    needs(window = "numeric",tcLag = "numeric",tcLead = "numeric",updateTSDB ="logical",generatePDF = "logical")

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
	pScore <- abs(m[,1]/tc)

    if(updateTSDB)this$updateTSDB(pair,pairResult,tc,delta,pScore)    
    return(list(pairResult = pairResult,tc = tc,delta = delta,pScore = pScore))
})

method("getData","SystemPairsTradingCLDX",function(this,startDate,...){
	dxContinuous <- (this$.systemHelper)$.tsdb$retrieveOneTimeSeriesByName("dx.1c_price_last",data.source = (this$.systemHelper)$.analyticsSource)
	clContinuous <- (this$.systemHelper)$.tsdb$retrieveOneTimeSeriesByName("cl.1c_price_last",data.source = (this$.systemHelper)$.analyticsSource)
	data <- getTimeStampPriorityZoo(dxContinuous,clContinuous,this$.systemHelper$.dataTimeStamp)
	data[,1] <- SystemDB$bigPointValue("DX.1C") * data[,1]
	data[,2] <- SystemDB$bigPointValue("CL.1C") * data[,2]
	
	colnames(data) <- c("dollarFuturesMarketValue","crudeFuturesMarketValue")
	if(!is.null(startDate))data <- data[index(data) >= as.POSIXct(startDate)]
	return(data)
})

method("updateTSDB","SystemPairsTradingCLDX",function(this,pair,pairResult,tc,delta,pScore,...){
    (this$.systemHelper)$uploadTSDB(pairResult[,"dailyTri"],"tri_daily")
    (this$.systemHelper)$uploadTSDB(pairResult[,"zScore"],"z_score")
	(this$.systemHelper)$uploadTSDB(pairResult[,"r2"],"r_square")
    (this$.systemHelper)$uploadTSDB(pairResult[,"beta.factorRank"],"scale")	
    (this$.systemHelper)$uploadTSDB(pairResult[,"hedgeX"],"hedge")
    (this$.systemHelper)$uploadTSDB(pairResult[,"alpha"],"intercept")
	(this$.systemHelper)$exportASCII(pairResult[,"beta.factorRank"],"scale")	
	(this$.systemHelper)$uploadTSDB(pairResult[,"alpha"],"intercept")
    (this$.systemHelper)$uploadTSDB(pairResult[,"residual"],"residual")    
    (this$.systemHelper)$uploadTSDB(tc,"transaction_cost")
	(this$.systemHelper)$uploadTSDB(pScore,"p_score")
    (this$.systemHelper)$uploadTSDB(delta,"delta")
    (this$.systemHelper)$uploadTSDB(returnDates = index(na.omit(pairResult[,"dailyTri"])))
})

