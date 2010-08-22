constructor("SporCorSectorETFs", function(pairName = "XLEXLF",...){
    this <- extend(RObject(),"SporCorSectorETFs")
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
			"sporcor",
			"1.0",
			squish("Market Systems/General Market Systems/SporCor/"),
		,...)
	}
    this
})

method("getSecurityId","SporCorSectorETFs",function(this,ticker,...){
	this$.securityIds[na.omit(match(ticker,this$.spdrTickers))]
})

method("permutations","SporCorSectorETFs",function(this,...){
	spdrPermutations <- expand.grid(list(this$.spdrTickers,this$.spdrTickers))
	spdrPermutations <- spdrPermutations[spdrPermutations[,1]!=spdrPermutations[,2],]
	as.character(paste(spdrPermutations[,1],spdrPermutations[,2],sep = ""))
})

method("comb","SporCorSectorETFs",function(this,...){
	comb <- combn(this$.spdrTickers,2)
	sapply(1:NCOL(comb),function(x)squish(comb[,x]))
})

method("getBidAsk","SporCorSectorETFs",function(this,...){
	0.02 # One tick for slippage, one tick for ticket cost 
})

method("runShortTerm","SporCorSectorETFs",function(this,startDate,
    window = 20,updateTSDB = FALSE,generatePDF = FALSE, underlyingData = NULL
,...){
    needs(window = "numeric",updateTSDB ="logical",generatePDF = "logical")
	pair <- this$getPair(startDate = startDate, underlyingData = underlyingData)
	pair$runChangesRollingRegression(
		window = window,
		constant = FALSE,
		storeIn = (this$.systemHelper)$.pairName,
		generatePDF = generatePDF,
		pathPDF = (this$.systemHelper)$.pdfPath,
		mfrowPDF = c(3,3)
	)
	pairResult <- pair$getModelResults((this$.systemHelper)$.pairName,window)
	
	if(updateTSDB)(this$.systemHelper)$uploadTSDB(pairResult[,"beta"],"beta_short")    
	return(list(beta = pairResult[,"beta"]))
})

method("getPair","SporCorSectorETFs",function(this,startDate,underlyingData = NULL,...){
	if(is.null(underlyingData)) data <- this$getData(startDate)
	else data <- underlyingData
	Pair(
		seriesY = data[,squish(this$.lagMarket,".close")],seriesX = data[,squish(this$.leadMarket,".close")],
		triY = data[,squish(this$.lagMarket,".tri")],triX = data[,squish(this$.leadMarket,".tri")]
	)
})

method("longTermTransformation","SporCorSectorETFs",function(this,pair,window,generatePDF,...){
	pair$runChangesRollingRegression(
		window = window,
		constant = FALSE,
		storeIn = (this$.systemHelper)$.pairName,
		generatePDF = generatePDF,
		pathPDF = (this$.systemHelper)$.pdfPath,
		mfrowPDF = c(3,3)
	)		
	pair
})

method("runLongTerm","SporCorSectorETFs",function(this,startDate,
	window = 130,betaBand = 0.2,updateTSDB = FALSE,generatePDF = FALSE, underlyingData = NULL, shortBeta = NULL
,...){
	needs(window = "numeric",updateTSDB ="logical",generatePDF = "logical")
	pair <- this$getPair(startDate = startDate,underlyingData = underlyingData)
	pair <- this$longTermTransformation(pair,window,FALSE)
	combSeries <- this$getCombSeries(pair,window,betaBand,shortBeta)
	hedge <- this$getHedgeRatios(pair,combSeries$combBeta)[,2]	
	tc <- abs(this$getBidAsk(this$.leadMarket) * hedge) + abs(this$getBidAsk(this$.lagMarket))
	
	if(updateTSDB){		
		(this$.systemHelper)$uploadTSDB(combSeries$combBeta,"beta")
		(this$.systemHelper)$uploadTSDB(tc,"transaction_cost")
		(this$.systemHelper)$uploadTSDB(hedge,"hedge")		
		(this$.systemHelper)$uploadTSDB(combSeries$combDailyTri,"tri_daily")
		(this$.systemHelper)$uploadTSDB(returnDates = index(na.omit(combSeries$combDailyTri)))
	}	
	return(list(
		combBeta = combSeries$combBeta, hedge = hedge, tc = tc, tri = combSeries$combTri, dailyTri = combSeries$combDailyTri
	))
})

method("getHedgeRatios","SporCorSectorETFs",function(this,pair,beta,...){
	pair$getSlopeHedgeRatios(beta)	
})

method("getCombSeries","SporCorSectorETFs",function(this,longPair,window,betaBand,shortBeta = NULL,...){
			
	longPairResult <- longPair$getModelResults((this$.systemHelper)$.pairName,window)
	longBeta <- longPairResult[,"beta"]
	if(is.null(shortBeta)){
		shortBeta <- (this$.systemHelper)$.tsdb$retrieveOneTimeSeriesByName((this$.systemHelper)$timeSeriesName('beta_short'),'internal')
	}
	
	mBeta <- na.omit(merge(shortBeta,longBeta))
	betaRatio <- mBeta[,1]/mBeta[,2]
	
	outBoundDatesUp <- index(betaRatio[betaRatio > (1 + betaBand)])
	outBoundDatesDown <- index(betaRatio[betaRatio < (1 - betaBand)])
	inBoundDates <- index(betaRatio[betaRatio <= (1 + betaBand) & betaRatio >= (1 - betaBand)])
	
	combBeta <- longBeta
	combBeta[match(outBoundDatesUp,index(combBeta))] <- longBeta[outBoundDatesUp] * (1 + betaBand)
	combBeta[match(outBoundDatesDown,index(combBeta))] <- longBeta[outBoundDatesDown] * (1 - betaBand)
	combBeta[match(inBoundDates,index(combBeta))] <- shortBeta[inBoundDates]
		
	hedgeCoefficients <- this$getHedgeRatios(longPair,combBeta)
	combDailyTri <- getLinearCombinationDailyTri(zooDailyTris = diff(merge(longPair$.triY,longPair$.triX)),zooHedgeCoefficients = hedgeCoefficients,holdCoefficients = TRUE)
	combTri <- getCumTriFromDailyTri(combDailyTri,baseTri = 0)
			
	return(list(combTri = combTri,combDailyTri = combDailyTri,combBeta = combBeta,longBeta = longPairResult[,"beta"]))
})

method("getData","SporCorSectorETFs",function(this,startDate,...){

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

method("tcEstimate","SporCorSectorETFs",function(this,pairs,mustBeNegativeHedge,version = "1.0",...){
	tcFrame <- data.frame(Name = NULL,Slippage = NULL)
	for (i in 1:NROW(pairs)){
		print(pairs[i])
		name <- (this$.systemHelper)$getPairMarketName(pairName = pairs[i],versionString = version)
		data <- TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(name,"_sporcor_",version,"_hedge"))
		if(mustBeNegativeHedge) data <- data[as.numeric(data) < 0]		
		tcFrame <- rbind(tcFrame,
			data.frame(
				Name = name,		
				Slippage = round(0.5 *
					(this$getBidAsk(this$.leadMarket) * median(abs(as.numeric(data))) + abs(this$getBidAsk(this$.lagMarket)))												
				,4)
			)
		)
	}
	tcFrame
})