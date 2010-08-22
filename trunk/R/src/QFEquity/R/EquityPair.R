constructor("EquityPair", function(inputSource = 'internal',outputSource = 'internal',...){	
	library(QFPairsTrading)
	this <- extend(RObject(), "EquityPair")	
	if(inStaticConstructor(this)) return(this)
	this$.inputSource <- inputSource
	this$.weighting <- 'halfLife'
	this$.outputSource <- outputSource
	return(this)		
})

method("runOneFactorDistance", "EquityPair", function(this, securityIdLag,securityIdLead, window,startDate = NULL,underlyingData = NULL,...){					
	needs(startDate = 'POSIXt?',securityIdLag = 'numeric',securityIdLead = 'numeric')
	
	betaLag <- MarketNeutralEquity$getEquityBetaZoo(securityIdLag,this$.inputSource)
	betaLead <- MarketNeutralEquity$getEquityBetaZoo(securityIdLead,this$.inputSource)
	
	getBeta <- function(pair){
		pair$runRollingRegression(window = window,constant = TRUE,storeIn = 'result',weighting = this$.weighting)						
		na.omit(pair$getModelResults('result',window)[,"beta"])		
	}
	
	betaOne <- getBeta(Pair(seriesY = betaLag,seriesX = betaLead))
	betaTwo <- getBeta(Pair(seriesY = betaLead,seriesX = betaLag))

	sqrt(abs(betaOne) * abs(betaTwo)) * sign(betaOne)
})

method("getMarketCombinations","EquityPair",function(this,securityIds,includeSpy = FALSE,...){
	tickers <- sort(as.character(sapply(securityIds,function(x){this$getTickerFromID(x)})))
	if(includeSpy)tickers <- c(tickers,'SPY')
	comb <- combn(tickers,2)
	sapply(1:NCOL(comb),function(x)squish(comb[1,x],'.',comb[2,x]))
})

method("getIdCombinations","EquityPair",function(this,securityIds,...){
	tickers <- sort(as.character(sapply(securityIds,function(x){this$getTickerFromID(x)})))
	ids <- as.numeric(sapply(tickers,function(x){EquityDataLoader$getSecurityIDFromTicker(x)}))
	combn(ids,2)	
})

method("runFactorBased", "EquityPair", function(this, securityIdLag,securityIdLead, startDate = NULL,underlyingData = NULL,betaLag = NULL,betaLead = NULL,...){		
	needs(startDate = 'POSIXt?',securityIdLag = 'numeric',securityIdLead = 'numeric')	
	pair <- this$getPair(securityIdLag,securityIdLead,startDate,underlyingData)
	
	if(is.null(betaLag))betaLag <- MarketNeutralEquity$getEquityBetaZoo(securityIdLag,this$.inputSource)
	if(is.null(betaLead))betaLead <- MarketNeutralEquity$getEquityBetaZoo(securityIdLead,this$.inputSource)
	
	m <- na.omit(merge(pair$.seriesY,pair$.seriesX,betaLag,betaLead))	
	hedgeRatios <- pair$getSlopeHedgeRatios((m[,3] / m[,4]) * (m[,1]/m[,2]))	
	dailyTri <- getLinearCombinationDailyTri(zooDailyTris = diff(merge(pair$.triY,pair$.triX)),zooHedgeCoefficients = hedgeRatios,holdCoefficients = TRUE)	
	
	list(hedge = hedgeRatios[,2], triDaily = dailyTri)	
})	

method("runDollarNeutral", "EquityPair", function(this,securityIdLag,securityIdLead,startDate = NULL,underlyingData = NULL,...){	
	needs(startDate = 'POSIXt?',securityIdLag = 'numeric',securityIdLead = 'numeric')
	pair <- this$getPair(securityIdLag,securityIdLead,startDate,underlyingData)	
	m <- na.omit(merge(pair$.seriesY,pair$.seriesX))
	hedgeRatios <- pair$getSlopeHedgeRatios(m[,1]/m[,2])
	dailyTri <- getLinearCombinationDailyTri(zooDailyTris = diff(merge(pair$.triY,pair$.triX)),zooHedgeCoefficients = hedgeRatios,holdCoefficients = TRUE)
	
	list(hedge = hedgeRatios[,2], triDaily = dailyTri)
})	

method("getPair","EquityPair",function(this,securityIdLag,securityIdLead,startDate = NULL,underlyingData = NULL,...){	
	if(is.null(underlyingData))data <- this$getData(securityIdLag,securityIdLead,startDate)
	else data <- underlyingData
	Pair(	
		seriesY = data[,'equityCloseLag'],seriesX = data[,'equityCloseLead'],
		triY = data[,'equityTriLag'],triX = data[,'equityTriLead']
	)
})		


method("getTickerFromID", "EquityPair", function(static,id,...){
	toupper(EquityDataLoader$getTickerFromSecurityID(id))
})

method("updateTSDB", "EquityPair", function(this,securityIdLag,securityIdLead,attr,transformation,z = NULL,returnDates = NULL,...){
	assert(transformation %in% c('EquityPair.R','MarketNeutralEquity.R'))
	
	market <- squish(this$getTickerFromID(securityIdLag),'.',this$getTickerFromID(securityIdLead),'.TRI')
	tsName <- squish(market,'_close_',attr)
	
	attributes = list(quote_type = 'close',
		quote_side = 'mid',
		instrument = 'equity',
		transformation_output = attr,
		transformation = transformation,
		market = market
	)
	if (TimeSeriesDB()$timeSeriesExists(tsName)) TimeSeriesDB()$purgeTimeSeries(tsName,this$.outputSource)
	if(attr != 'tri'){
		TimeSeriesDB()$createAndWriteOneTimeSeriesByName(z,tsName,this$.outputSource,attributes)
	}else{
		firstDate <- businessDaysAgo(1,first(returnDates))
		updateCumTriFromDailyTri(
			TimeSeriesDB(),firstDate,returnDates,
			squish(market,'_close_tri_daily'),
			tsName,
			attributes,
			source = this$.outputSource,
			100
		)
	}
	TRUE
})

method("getData","EquityPair",function(this,securityIdLag,securityIdLead,startDate = NULL,...){		
	data <- merge(	
			MarketNeutralEquity$getEquityCloseZoo(securityIdLag),MarketNeutralEquity$getEquityTriZoo(securityIdLag),
			MarketNeutralEquity$getEquityCloseZoo(securityIdLead),MarketNeutralEquity$getEquityTriZoo(securityIdLead)
	)	
	colnames(data) <- c('equityCloseLag','equityTriLag','equityCloseLead','equityTriLead')	
	if(!is.null(startDate))data <- data[index(data) >= as.POSIXct(startDate)]	
	return(data)	
})

method("slippageEstimate","EquityPair",function(this,pairs,...){
	tcFrame <- data.frame(Name = NULL,Slippage = NULL,StartDate = NULL)
	for (i in 1:NROW(pairs)){
		print(pairs[i])
		strSplit <- strsplit(pairs[i],'\\.')
		lagTicker <- squish(strSplit[[1]][1],'.TRI')
		leadTicker <- squish(strSplit[[1]][2],'.TRI')
					
		name <- squish(pairs[i],'.TRI')
		data <- abs(TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(name,"_close_hedge")))		
		tcFrame <- rbind(tcFrame,
			data.frame(
				Name = name,		
				Slippage = round(
					SystemDB$slippage(lagTicker)
					+ SystemDB$slippage(leadTicker) * median(as.numeric(data))												
				,4),
				StartDate = as.character(first(index(data)))
			)
		)
	}
	tcFrame
})