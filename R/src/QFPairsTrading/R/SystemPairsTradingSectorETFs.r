constructor("SystemPairsTradingSectorETFs", function(pairName = "XLEXLF",...){
    this <- extend(RObject(),"SystemPairsTradingSectorETFs")
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
			"1.1",
			squish("Market Systems/Linked Market Systems/Equities/StrategySectorETFs/"),
		,...)
	}
    this
})

method("getSecurityId","SystemPairsTradingSectorETFs",function(this,ticker,...){
	this$.securityIds[na.omit(match(ticker,this$.spdrTickers))]
})

method("permutations","SystemPairsTradingSectorETFs",function(this,...){
	spdrPermutations <- expand.grid(list(this$.spdrTickers,this$.spdrTickers))
	spdrPermutations <- spdrPermutations[spdrPermutations[,1]!=spdrPermutations[,2],]
	as.character(paste(spdrPermutations[,1],spdrPermutations[,2],sep = ""))
})

method("comb","SystemPairsTradingSectorETFs",function(this,...){
	comb <- combn(this$.spdrTickers,2)
	sapply(1:NCOL(comb),function(x)squish(comb[,x]))
})

method("getTC","SystemPairsTradingSectorETFs",function(this,...){
	0.02 # One tick for slippage, one tick for ticket cost 
})

method("runShortTerm","SystemPairsTradingSectorETFs",function(this,startDate,
    window = 20,updateTSDB = FALSE,generatePDF = FALSE
,...){
    needs(window = "numeric",updateTSDB ="logical",generatePDF = "logical")
	pair <- this$getPair(startDate = startDate)
	pair$runPercentChangesRollingRegression(
			window = window,
			constant = TRUE,
			storeIn = (this$.systemHelper)$.pairName,
			generatePDF = generatePDF,
			pathPDF = (this$.systemHelper)$.pdfPath,
			mfrowPDF = c(3,3)
	)
	pairResult <- pair$getModelResults((this$.systemHelper)$.pairName,window)	
	m <- na.omit(merge(lag(pair$.seriesY,-1),pairResult[,"residual"],pairResult[,"hedgeX"]))
	dollarResiduals <- m[,1] * m[,2]
	tc <- abs(this$getTC() * m[,3]) + abs(this$getTC())
	delta <- pairResult[,"hedgeX"]
	pScore <- abs(dollarResiduals/tc)
	
	if(updateTSDB)(this$.systemHelper)$updateTSDB(pair,pairResult,tc,delta,pScore,dollarResiduals)
	return(list(pairResult = pairResult,tc = tc,delta = delta,pScore = pScore,residual = dollarResiduals))
})

method("getPair","SystemPairsTradingSectorETFs",function(this,startDate,...){
	data <- this$getData(startDate)	
	Pair(
		seriesY = data[,squish(this$.lagMarket,".close")],seriesX = data[,squish(this$.leadMarket,".close")],
		triY = data[,squish(this$.lagMarket,".tri")],triX = data[,squish(this$.leadMarket,".tri")]
	)
})

method("runLongTerm","SystemPairsTradingSectorETFs",function(this,startDate,
	window = 130,betaBand = 0.2,updateTSDB = FALSE,generatePDF = FALSE
,...){
	needs(window = "numeric",updateTSDB ="logical",generatePDF = "logical")
	pair <- this$getPair(startDate = startDate)
	pair$runPercentChangesRollingRegression(
		window = window,
		constant = TRUE,
		storeIn = (this$.systemHelper)$.pairName,
		generatePDF = generatePDF,
		pathPDF = (this$.systemHelper)$.pdfPath,
		mfrowPDF = c(3,3)
	)

	combSeries <- this$getCombSeries(pair,window,betaBand)
	
	if(updateTSDB){
		spt <- SystemPairsTrading(
			this$.systemHelper$.pairName,
			this$.systemHelper$.transformationName,
			"1.2",
			squish("Market Systems/Linked Market Systems/Equities/StrategySectorETFs/")
		)
		spt$uploadTSDB(combSeries$combBeta,'beta')
		spt$uploadTSDB(combSeries$combTri,'tri')
		spt$uploadTSDB(combSeries$combDailyTri,'tri_daily')
		(this$.systemHelper)$uploadTSDB(combSeries$longBeta,"beta_long")	
	}
	return(combSeries)
})

method("getCombSeries","SystemPairsTradingSectorETFs",function(this,longPair,window,betaBand,...){
			
	longPairResult <- longPair$getModelResults((this$.systemHelper)$.pairName,window)
	longBeta <- longPairResult[,"beta"]
	shortBeta <- (this$.systemHelper)$.tsdb$retrieveOneTimeSeriesByName(this$.systemHelper$timeSeriesName('beta'),'internal')
	
	mBeta <- na.omit(merge(shortBeta,longBeta))
	betaRatio <- mBeta[,1]/mBeta[,2]
	 
	outBoundDatesUp <- index(betaRatio[betaRatio > (1 + betaBand)])
	outBoundDatesDown <- index(betaRatio[betaRatio < (1 - betaBand)])
	inBoundDates <- index(betaRatio[betaRatio <= (1 + betaBand) & betaRatio >= (1 - betaBand)])
	
	combBeta <- longBeta
	combBeta[match(outBoundDatesUp,index(combBeta))] <- longBeta[outBoundDatesUp] * (1 + betaBand)
	combBeta[match(outBoundDatesDown,index(combBeta))] <- longBeta[outBoundDatesDown] * (1 - betaBand)
	combBeta[match(inBoundDates,index(combBeta))] <- shortBeta[inBoundDates]
	
	m <- na.omit(merge(combBeta,longPair$.seriesY,longPair$.seriesX)) 
	hedgeCoefficients <- longPair$getSlopeHedgeRatios(m[,1] * m[,2] / m[,3])
	combDailyTri <- getLinearCombinationDailyTri(zooDailyTris = diff(merge(longPair$.triY,longPair$.triX)),zooHedgeCoefficients = hedgeCoefficients,holdCoefficients = TRUE)
	combTri <- getCumTriFromDailyTri(combDailyTri,baseTri = 0)
	
	return(list(combTri = combTri,combDailyTri = combDailyTri,combBeta = combBeta,longBeta = longPairResult[,"beta"]))
})

method("getData","SystemPairsTradingSectorETFs",function(this,startDate,...){

	tsNames <- c(
		squish(this$.leadId,"_tri_vLibor"),squish("ivydb_",this$.leadId,"_close_adj_price_mid"),
		squish(this$.lagId,"_tri_vLibor"),squish("ivydb_",this$.lagId,"_close_adj_price_mid")
	)
	data <- getMergedTimeSeries(
		(this$.systemHelper)$.tsdb,
		tsNames,
		"internal",
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

method("tcEstimate","SystemPairsTradingSectorETFs",function(this,...){
	tcFrame <- data.frame(Name = NULL,BetaMedian = NULL,BetaCombMedian = NULL,Slippage = NULL)
	version <- "1.1"
	pairs <- SystemPairsTradingSectorETFs$permutations()
	for (i in 1:NROW(pairs)){
		print(pairs[i])
		name <- SystemPairsTrading$getPairMarketName(pairs[i],version)
		data <- TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(name,"_rolling_regression_",version,"_hedge"))
		data <- data[as.numeric(data) < 0]
		dataBeta <- TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(name,"_rolling_regression_",version,"_beta"))
		dataBeta <- dataBeta[as.numeric(dataBeta) > 0]
		nameComb <- SystemPairsTrading$getPairMarketName(pairs[i],'1.2')
		dataBetaComb <- TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(nameComb,"_rolling_regression_",'1.2',"_beta"))
		dataBetaComb <- dataBetaComb[as.numeric(dataBetaComb) > 0]
		tcFrame <- rbind(tcFrame,
			data.frame(Name = name,
				BetaMedian = median(abs(as.numeric(dataBeta))),
				BetaCombMedian = median(abs(as.numeric(dataBetaComb))),
				Slippage = round(0.5 * SystemPairsTradingSectorETFs$getTC() * (1 + median(abs(as.numeric(data)))),4))
		)
	}
	tcFrame
})