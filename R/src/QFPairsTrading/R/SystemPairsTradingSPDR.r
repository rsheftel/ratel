constructor("SystemPairsTradingSPDR", function(pairName = "XLEXLF",...){
    this <- extend(RObject(),"SystemPairsTradingSPDR")
	this$.spdrTickers <- c("XLE","XLF","XLV","XLY","XLK","XLP","XLB","XLI","XLU")
	this$.securityIds <- c(110011,110012,110008,110010,110014,110009,110007,110013,110015)
	if(!inStaticConstructor(this)){
		assert(pairName %in% this$permutations())
		this$.leadMarket <- substr(pairName,4,6)
		this$.lagMarket <- substr(pairName,1,3)
		this$.leadId <- this$getSecurityId(this$.leadMarket)
		this$.lagId <- this$getSecurityId(this$.lagMarket)
		this$.systemHelper <- SystemPairsTrading(
				pairName,
				"rolling_regression",
				"1.0",
				squish("Market Systems/Linked Market Systems/Equities/StrategySPDR/"),
		,...)
	}
    this
})

method("getSecurityId","SystemPairsTradingSPDR",function(this,ticker,...){
	this$.securityIds[na.omit(match(ticker,this$.spdrTickers))]
})

method("permutations","SystemPairsTradingSPDR",function(this,...){
	spdrPermutations <- expand.grid(list(this$.spdrTickers,this$.spdrTickers))
	spdrPermutations <- spdrPermutations[spdrPermutations[,1]!=spdrPermutations[,2],]
	as.character(paste(spdrPermutations[,1],spdrPermutations[,2],sep = ""))
})

method("comb","SystemPairsTradingSPDR",function(this,...){
	comb <- combn(this$.spdrTickers,2)
	sapply(1:NCOL(comb),function(x)squish(comb[,x]))
})

method("getTC","SystemPairsTradingSPDR",function(this,...){
	0.01
})

method("run","SystemPairsTradingSPDR",function(this,startDate,
    window = 20,updateTSDB = FALSE,generatePDF = FALSE
,...){

    needs(window = "numeric",updateTSDB ="logical",generatePDF = "logical")

    data <- this$getData(startDate)

	pair <- Pair(
        seriesY = data[,squish(this$.lagMarket,".close")],seriesX = data[,squish(this$.leadMarket,".close")],
        triY = data[,squish(this$.lagMarket,".tri")],triX = data[,squish(this$.leadMarket,".tri")]
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

    tc <- abs(this$getTC(this$.leadMarket) * m[,2]) + abs(this$getTC(this$.lagMarket))
    delta <- pairResult[,"hedgeX"]
	pScore <- abs(m[,1]/tc)

    if(updateTSDB)(this$.systemHelper)$updateTSDB(pair,pairResult,tc,delta,pScore,pairResult[,"residual"])    
    return(list(pairResult = pairResult,tc = tc,delta = delta,pScore = pScore,residual = pairResult[,"residual"]))
})

method("getData","SystemPairsTradingSPDR",function(this,startDate,...){

	tsNames <- c(
		squish(this$.leadId,"_tri_vLibor"),squish("ivydb_",this$.leadId,"_close_adj_price_mid"),
		squish(this$.lagId,"_tri_vLibor"),squish("ivydb_",this$.lagId,"_close_adj_price_mid")
	)
	data <- getMergedTimeSeries(
		(this$.systemHelper)$.tsdb,
		tsNames,
		"bloomberg",
		filter = NULL,
		startDate = startDate
	)

	colnames(data) <- c(
		squish(this$.leadMarket,".tri"),squish(this$.leadMarket,".close"),
		squish(this$.lagMarket,".tri"),squish(this$.lagMarket,".close")
	)
	if(!is.null(startDate))data <- data[index(data) >= as.POSIXct(startDate)]
	return(data)
})

method("dataLoader","SystemPairsTradingSPDR",function(this,...){
	print("Update Bloomberg Based Transformation Data")
	for (i in 1:NROW(this$.spdrTickers)){
		print(this$.spdrTickers[i])
		securityId <- this$getSecurityId(this$.spdrTickers[i]) 
		closeAdjTsName <- squish("ivydb_",securityId,"_close_adj_price_mid")
		financedTriTsName <- squish(securityId,"_tri_vLibor")
		# Calculate adjusted prices
		EquityDataLoader$calcAdjClosePrices(securityId,1,1,"bloomberg","bloomberg","internal")
		# Calculate financed adjusted TRIs
		adjustedTriData <- TimeSeriesDB()$retrieveOneTimeSeriesByName(closeAdjTsName,"bloomberg")
		financedAdjTri <- FinanceTRI()$usingONLibor(zooSeriesY = adjustedTriData)
		TimeSeriesDB()$writeOneTimeSeriesByName(financedAdjTri,financedTriTsName,"bloomberg")
	}
})

method("pScoreAverage","SystemPairsTradingSPDR",function(this,...){
	pairs <- SystemPairsTradingSPDR$permutations()
	pScoreList <- list()
	for (i in 1:NROW(pairs)){
		print(pairs[i])
		pScoreList[[i]] <- TimeSeriesDB()$retrieveOneTimeSeriesByName(squish("PTT10.",pairs[i],"_rolling_regression_1.0_p_score"),"internal")
	}
	pScoreSum = accumulate('+',pScoreList)/NROW(pScoreList)
})


method("tcEstimate","SystemPairsTradingSPDR",function(this,...){
	tcFrame <- data.frame(Name = NULL,Slippage = NULL)
	version <- "1.0"
	pairs <- SystemPairsTradingSPDR$permutations()
	for (i in 1:NROW(pairs)){
		print(pairs[i])
		name <- SystemPairsTrading$getPairMarketName(pairs[i],version)
		data <- TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(name,"_rolling_regression_",version,"_hedge"))
		tcFrame <- rbind(tcFrame,
				data.frame(Name = name,Slippage = round(0.5 * SystemPairsTradingSPDR$getTC() * (1 + median(abs(as.numeric(data)))),4))
		)
	}
	tcFrame
})