constructor("MarketNeutralEquity", function(
	weighting = NULL,
	adjustRawBetas = FALSE,
	outputSource = 'internal',
	regLag = 1,
	hedgeCloseTsName = 'ivydb_109820_close_adj_price_mid',
	hedgeTriTsName = '109820_tri_vLibor'
,...){
	library(QFPairsTrading)
    this <- extend(RObject(), "MarketNeutralEquity")
    if(inStaticConstructor(this)) return(this)
	if(!is.null(weighting))assert(weighting %in% c('halfLife'))
	this$.weighting <- weighting
	this$.betaMin <- 0.1
	this$.regLag <- regLag
	this$.outputSource <- outputSource
	this$.adjustRawBetas <- adjustRawBetas
	this$.hedgeCloseTsName <- hedgeCloseTsName
	this$.hedgeTriTsName <- hedgeTriTsName
    return(this)
})

method("runRollingRegression", "MarketNeutralEquity", function(this, securityId, window,startDate = NULL,underlyingData = NULL,...){					
	needs(startDate = 'POSIXt?',window = 'numeric',securityId = 'numeric')				
	
	pair <- this$getPair(securityId,startDate,underlyingData)
	
	if(!this$.adjustRawBetas){
		pair$runPercentChangesRollingRegression(window = window,constant = TRUE,storeIn = 'result',lag = this$.regLag,weighting = this$.weighting)				
		beta <- na.omit(pair$getModelResults('result',window)[,"beta"])
	}else{
		rawBeta <- this$getEquityBetaZoo(securityId,window)
		if(NROW(rawBeta[as.numeric(rawBeta) == this$.betaMin]) > 0){
			pair$runPercentChangesRollingRegression(window = window,constant = TRUE,storeIn = 'result',lag = this$.regLag,weighting = this$.weighting)				
			rawBeta <- na.omit(pair$getModelResults('result',window)[,"beta"])
		}
		beta <- (2/3) * rawBeta + 1/3
	}
	
	for(i in 1:NROW(beta))		
		if(as.numeric(beta[i]) < this$.betaMin) beta[i] <- this$.betaMin
	
	this$.getHegdesAndTris(pair,beta)
})					

method("runDollarNeutral", "MarketNeutralEquity", function(this, securityId,startDate = NULL,underlyingData = NULL,...){
	needs(startDate = 'POSIXt?',securityId = 'numeric')
	
	pair <- this$getPair(securityId,startDate,underlyingData)	
	this$.getHegdesAndTris(pair,1)	
})

method(".getHegdesAndTris", "MarketNeutralEquity", function(this,pair,beta,...){
	m <- na.omit(merge(pair$.seriesY,pair$.seriesX,beta))
	hedgeRatios <- pair$getSlopeHedgeRatios((m[,1]/m[,2]) * m[,3])
	dailyTri <- getLinearCombinationDailyTri(zooDailyTris = diff(merge(pair$.triY,pair$.triX)),zooHedgeCoefficients = hedgeRatios,holdCoefficients = TRUE)
	list(beta = m[,3], hedge = hedgeRatios[,2], triDaily = dailyTri)
})

method("getEquityCloseZoo", "MarketNeutralEquity", function(this,securityId,...){
	TimeSeriesDB()$retrieveOneTimeSeriesByName(squish('ivydb_',securityId,'_close_adj_price_mid'),'internal')
})

method("getEquityTriZoo", "MarketNeutralEquity", function(this,securityId,...){
	TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(securityId,'_tri_vLibor'),'internal')
})

method("getHedgeTriZoo", "MarketNeutralEquity", function(this,...){
	TimeSeriesDB()$retrieveOneTimeSeriesByName(this$.hedgeTriTsName,'internal')
})

method("getHedgeCloseZoo", "MarketNeutralEquity", function(this,...){
	TimeSeriesDB()$retrieveOneTimeSeriesByName(this$.hedgeCloseTsName,'internal')
})

method("getEquityBetaZoo", "MarketNeutralEquity", function(this,securityId,source = 'internal',...){	
	TimeSeriesDB()$retrieveOneTimeSeriesByName(
		squish(toupper(EquityDataLoader$getTickerFromSecurityID(securityId)),'.SPY.TRI_close_beta'),
		source
	)
})

method("getPair","MarketNeutralEquity",function(this,securityId,startDate = NULL,underlyingData = NULL,...){
	if(is.null(underlyingData))data <- this$getData(securityId,startDate)
	else data <- underlyingData
	Pair(
		seriesY = data[,'equityClose'],seriesX = data[,'hedgeClose'],
		triY = data[,'equityTri'],triX = data[,'hedgeTri']
	)
})

method("getData","MarketNeutralEquity",function(this,securityId,startDate = NULL,...){	
	data <- merge(
		this$getEquityCloseZoo(securityId),this$getEquityTriZoo(securityId),
		this$getHedgeCloseZoo(),this$getHedgeTriZoo()
	)
	colnames(data) <- c('equityClose','equityTri','hedgeClose','hedgeTri')	
	if(!is.null(startDate))data <- data[index(data) >= as.POSIXct(startDate)]
	return(data)
})



