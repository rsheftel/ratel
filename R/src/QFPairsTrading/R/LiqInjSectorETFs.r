constructor("LiqInjSectorETFs", function(pairName = 'XLEXLF',...){
	library(SystemDB)
	this <- extend(RObject(),c("LiqInjSectorETFs","SporCorSectorETFs"))
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
				"liqinj",
				"1.0",
				squish("Market Systems/Linked Market Systems/Equities/LiqInjSectorETFs/")
				,...)
	}
	this
})

method("runShortTerm","LiqInjSectorETFs",function(this,startDate,
    window = 20,updateTSDB = FALSE,generatePDF = FALSE, underlyingData = NULL
,...){
    needs(window = "numeric",updateTSDB ="logical",generatePDF = "logical")
	pair <- this$getPair(startDate = startDate,underlyingData = underlyingData)
	pair$runPercentChangesRollingRegression(
			window = window,
			constant = FALSE,
			storeIn = (this$.systemHelper)$.pairName,
			generatePDF = generatePDF,
			pathPDF = (this$.systemHelper)$.pdfPath,
			mfrowPDF = c(3,3)
	)
	pairResult <- pair$getModelResults((this$.systemHelper)$.pairName,window)	
	m <- na.omit(merge(lag(pair$.seriesY,-1),pairResult[,"residual"]))
	dollarResiduals <- m[,1] * m[,2]	
	
	if(updateTSDB){
		(this$.systemHelper)$uploadTSDB(pairResult[,'beta'],'beta_short')
		(this$.systemHelper)$uploadTSDB(dollarResiduals,'residual')
		(this$.systemHelper)$uploadTSDB(pairResult[,'zScore'],'z_score')
	}
	return(list(beta = pairResult[,'beta'],residual = dollarResiduals,zScore = pairResult[,'zScore']))
})

method("longTermTransformation","LiqInjSectorETFs",function(this,pair,window,generatePDF,...){
	pair$runPercentChangesRollingRegression(
		window = window,
		constant = FALSE,
		storeIn = (this$.systemHelper)$.pairName,
		generatePDF = generatePDF,
		pathPDF = (this$.systemHelper)$.pdfPath,
		mfrowPDF = c(3,3)
	)		
	pair
})

method("getHedgeRatios","LiqInjSectorETFs",function(this,pair,beta,...){
	m <- na.omit(merge(beta,pair$.seriesY,pair$.seriesX))
	pair$getSlopeHedgeRatios(m[,1] * m[,2] / m[,3])
})

method("getData","LiqInjSectorETFs",function(this,startDate,...){

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