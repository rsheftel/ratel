constructor("SingleNameCDSTRI", function(cdsTicker = NULL,tenor = NULL,strike = NULL,internalSource = NULL,...)
{
	this <- extend(RObject(), "SingleNameCDSTRI")
	if(inStaticConstructor(this))return(this)
	constructorNeeds(this,cdsTicker = 'character',tenor = 'character')
	assert(strike %in% c('par',100,500)); assert(tenor %in% TermStructure$cds)
	strSplit <- strsplit(cdsTicker,'_')[[1]]
	this$.cds <- SingleNameCDS(ticker = strSplit[1],tenor = tenor,ccy = strSplit[3],tier = strSplit[2],dataSource = internalSource)
	this$.cdsTicker <- cdsTicker
	this$.strike <- strike	
	this$.docClause <- strSplit[4]
	this$.internalSource <- internalSource
	this$.isSNAC <- strike != 'par'
	print(squish("Working on ", cdsTicker,"/",tenor,"/",strike))
	this
})

method("setRange", "SingleNameCDSTRI", function(this,startDate,endDate,...) {
	this$.range <- Range(startDate,endDate)
})

method("loadHolidayData", "SingleNameCDSTRI", function(this,holidayData,...) {
	this$.holidayData <- holidayData
})

method("loadIrsData", "SingleNameCDSTRI", function(this,irsData,...) {
	this$.irsData <- strip.times.zoo(irsData)
})

method("loadSpreadSeries", "SingleNameCDSTRI", function(this,...) {				
	this$.cdsData <- this$.cds$specificCurves(quote_type = 'spread',strike = this$.strike,doc_clause = this$.docClause,startDate = this$.range$.start,endDate = this$.range$.end)
	if(is.null(this$.cdsData))return(NULL)
	this$.cdsData <- strip.times.zoo(this$.cdsData)
})

method("loadRecoveryRates", "SingleNameCDSTRI", function(this,...) {				
	this$.recoveryData <- this$.cds$specificSeries(quote_type = 'recovery',strike = this$.strike,doc_clause = this$.docClause,startDate = this$.range$.start,endDate = this$.range$.end,dataSource = 'internal')
	if(is.null(this$.recoveryData))return(NULL)
	this$.recoveryData <- strip.times.zoo(this$.recoveryData)
	if(!is.null(this$.cdsData)){
		this$.cdsData <- filter.zoo.byIntersection(this$.cdsData,this$.recoveryData)
		this$.recoveryData <- filter.zoo.byIntersection(this$.recoveryData,this$.cdsData)
	}
})

method("loadPriceSeries", "SingleNameCDSTRI", function(this,priceSeries = NULL,...) {
	if(!is.null(priceSeries))this$.cdsData <- priceSeries
	else this$.cdsData <- this$.cds$specificCurves(quote_type = 'price',strike = this$.strike,doc_clause = this$.docClause,startDate = this$.range$.start,endDate = this$.range$.end)
	this$.cdsData <- strip.times.zoo(this$.cdsData)
})

method("hasUnderlyingData", "SingleNameCDSTRI", function(this,...) {
	! (is.null(this$.cdsData) || is.null(this$.irsData)|| is.null(this$.recoveryData))
})

method("filterDataByIntersection", "SingleNameCDSTRI", function(this,...) {
	failIf(is.null(this$.cdsData) || is.null(this$.irsData),'Irs data or Cds data not loaded in SingleNameCDSTRI object')
	this$.cdsData <- subset(this$.cdsData,!is.na(this$.cdsData[,this$.cds$.tenor]))
	this$.cdsData <- this$.cdsData; this$.irsData <- this$.irsData;
	this$.cdsData <- filter.zoo.byIntersection(this$.cdsData,this$.irsData)
	this$.irsData <- filter.zoo.byIntersection(this$.irsData,this$.cdsData)
})

method("getStrike", "SingleNameCDSTRI", function(this,i,...) {
	if(this$.strike == 'par')
		return(as.numeric(this$.cdsData[i-1,this$.cds$.tenor]))
	else
		this$.strike/10000
})

method("getRecoveryRate", "SingleNameCDSTRI", function(this,i,...) {
	as.numeric(this$.recoveryData[i])	
})

method("getEffDate", "SingleNameCDSTRI", function(this,i,...) {
	if(!this$.isSNAC)
		return(index(this$.cdsData)[i])
	else
		Period('days',60)$rewind(index(this$.cdsData[i-1,]))
})

method("getValueDate", "SingleNameCDSTRI", function(this,i,...) {	
	if(!this$.isSNAC)
		getFincadDateAdjust(startDate = index(this$.cdsData)[i],unit = "d", NumUnits = 1,holidayList = this$.holidayData)
	else
		index(this$.cdsData)[i]
})

method("getAccruedPnl", "SingleNameCDSTRI", function(this,lastValueDate,valueDate,strike,notional,...) {
	if(this$.isSNAC)
		CDSPricer$accruedSNAC(lastValueDate,valueDate,strike,notional)
	else 
		CDSPricer$accrued(lastValueDate,valueDate,strike,notional)						
})

method("getSingleNameCDSTable", "SingleNameCDSTRI", function(this,i,curveDate,matDate,...) {
	if(this$.isSNAC){
		cdsCurve <- as.numeric(this$.cdsData[i,this$.cds$.tenor])
		tenors <- this$.cds$.tenor
	}else{ 
		cdsCurve <- as.numeric(this$.cdsData[i,]) 
		tenors <- TermStructure$cds
	}
	cdsTable <- CDSPricer$getSingleNameCDSTable(curveDate,cdsCurve,characterToNumericTenor(tenors),isSNAC = this$.isSNAC)[,-1]	
	CDSPricer$flatCurveAdjustment(cdsTable,matDate,flatCurve = TRUE)	
})

method("getDiscountFactorTable", "SingleNameCDSTRI", function(this,i,curveDate,...) {
	CDSPricer$getDiscountFactorTable(curveDate,dfCurve = NULL,swapRates = as.numeric(this$.irsData[i,])/100,swapTenors = colnames(this$.irsData),cashRates = NULL,cashTenors = NULL)
})

method("getMTMChangeAndDV01", "SingleNameCDSTRI", function(this,
	strike,recoveryRate,effDate,matDate,
	lastValueDate,valueDate,lastCdsTable,cdsTable,lastDfTable,dfTable
,...) {
	returnDateRun <- CDSPricer$getFincadPrice(
		direction = "buy",strike = strike,notional = 100,recovery = recoveryRate,
		valueDate = valueDate,effDate = effDate,matDate = matDate,
		cdsTable = cdsTable,dfTable = dfTable
	)	
	if(!this$.isSNAC){
		mtmChange <- returnDateRun["mtm",]
		dv01 <- returnDateRun["cds spread DVOI",]
	}else{
		tradeDateRun <- CDSPricer$getFincadPrice(
			direction = "buy",strike = strike,notional = 100,recovery = recoveryRate,
			valueDate = lastValueDate,effDate = effDate,matDate = matDate,
			cdsTable = lastCdsTable,dfTable = lastDfTable
		)			
		mtmChange <- returnDateRun["mtm",] - tradeDateRun["mtm",]
		dv01 <- returnDateRun["cds spread DVOI",]
	}
	list(mtmChange = mtmChange,dv01 = dv01)
})

method("calcSpreadsFromUpFronts", "SingleNameCDSTRI", function(this,...) {
	this$filterDataByIntersection()	
	priceSeries <- na.omit(this$.cdsData[,this$.cds$.tenor])
	if(NROW(priceSeries)==0)return(NULL)
	spreadSeries <- zoo(NA,index(priceSeries))
	for(i in 1:NROW(spreadSeries)){
		effDate <- Period('days',61)$rewind(index(this$.cdsData[i,]))
		spread <- CDSPricer$upfrontToSpread(
			as.numeric(priceSeries[i]),
			strike = this$getStrike(i),
			this$getRecoveryRate(i),
			this$getValueDate(i),			
			effDate,			
			IMMDates()$maturityFromEffective(effDate,characterToNumericTenor(this$.cds$.tenor),this$.isSNAC),
			this$getDiscountFactorTable(i,index(this$.cdsData)[i]),
			tenor = this$.cds$.tenor
		)
		spreadSeries[i] <- spread
	}
	return(spreadSeries)
})

method("calcSpreadTRIs", "SingleNameCDSTRI", function(this,...) {
	this$filterDataByIntersection()
	failIf(NROW(this$.cdsData) < 2, 'Not enough data to run TRI calculation!')
	templateZoo <- zoo(0,index(this$.cdsData))
	dailyTriZoo <- templateZoo
	dv01Zoo <- templateZoo
	
	lastMatDate <- IMMDates()$maturityFromEffective(this$getEffDate(2),characterToNumericTenor(this$.cds$.tenor),this$.isSNAC)
	lastValueDate <- this$getValueDate(1)
	lastCdsTable <- this$getSingleNameCDSTable(1,index(this$.cdsData)[1],lastMatDate)	
	lastDfTable <- this$getDiscountFactorTable(1,index(this$.cdsData)[1])
	for(i in 2:NROW(this$.cdsData)){
		returnDate <- index(this$.cdsData)[i]
		effDate <- this$getEffDate(i)
		matDate <- IMMDates()$maturityFromEffective(effDate,characterToNumericTenor(this$.cds$.tenor),this$.isSNAC)
		strike <- this$getStrike(i)
		recoveryRate <- this$getRecoveryRate(i)
		valueDate <- this$getValueDate(i)		
		cdsTable <- this$getSingleNameCDSTable(i,returnDate,matDate)			
		dfTable <- this$getDiscountFactorTable(i,returnDate)		 
		pricerOutput <- CDSPricer$getFincadPrice(
			direction = "buy",strike = strike,notional = 100,
			recovery = recoveryRate,valueDate = valueDate,effDate = effDate,
			matDate = matDate,cdsTable = cdsTable,dfTable = dfTable
		)
		mtmChangeAndDV01 <- this$getMTMChangeAndDV01(
			strike,recoveryRate,effDate,matDate,
			lastValueDate,valueDate,lastCdsTable,cdsTable,lastDfTable,dfTable
		)		
		accruedPnl <- this$getAccruedPnl(lastValueDate,valueDate,strike,100)
		dailyTriZoo[i] <- as.numeric(mtmChangeAndDV01$mtmChange) - as.numeric(accruedPnl)		
		dv01Zoo[i] <- as.numeric(mtmChangeAndDV01$dv01)
		
		lastMatDate <- matDate
		lastValueDate <- valueDate
		lastCdsTable <- cdsTable
		lastDfTable <- dfTable	
	}		
	list(dv01Zoo = dv01Zoo[-1],dailyTriZoo = dailyTriZoo[-1])
})

method("transformedAttributeList", "SingleNameCDSTRI", function(this,name,...) {
	resList <- list(
		ticker = this$.cds$.ticker,ccy = this$.cds$.ccy,cds_strike = this$.strike,tier = this$.cds$.tier,doc_clause = this$.docClause, 
		instrument = 'cds',cds_ticker = this$.cdsTicker,tenor = this$.cds$.tenor,
		quote_type = name		
	)
	if(this$.isSNAC)resList$cds_strike <- this$.strike
	resList
})

method("transformedTsName", "SingleNameCDSTRI", function(this,name,...) {
	if(!this$.isSNAC) squish(this$.cdsTicker,'_',name,'_',this$.cds$.tenor)
	else squish(this$.cdsTicker,'_',this$.strike,'_',name,'_',this$.cds$.tenor)
})

method("uploadSpreads", "SingleNameCDSTRI", function(this,spreadZoo,outputSource = this$.internalSource,...) {	
	TimeSeriesDB()$writeOneTimeSeriesByName(spreadZoo,this$transformedTsName('spread'),outputSource)
})

method("upload", "SingleNameCDSTRI", function(this,dv01Zoo,dailyTriZoo,outputSource = this$.internalSource,...) {	
	# update daily TRI
	attL <- this$transformedAttributeList('tri_daily')
	tsNameTriDaily <- this$transformedTsName('tri_daily')	
	TimeSeriesDB()$createAndWriteOneTimeSeriesByName(dailyTriZoo,tsNameTriDaily,outputSource,attL)	
	# update DV01s
	attL <- this$transformedAttributeList('dv01')
	tsName <- this$transformedTsName('dv01')
	TimeSeriesDB()$createAndWriteOneTimeSeriesByName(dv01Zoo,tsName,outputSource,attL)
	# update TRI
	returnDates <- index(dailyTriZoo)
	firstDate <- as.POSIXct(getFincadDateAdjust(startDate = first(returnDates),unit = "d", NumUnits = -1,holidayList = this$.holidayData))
	attL <- this$transformedAttributeList('tri')
	tsNameTri <- this$transformedTsName('tri')
	updateCumTriFromDailyTri(TimeSeriesDB(),firstDate,returnDates,tsNameTriDaily,tsNameTri,attL,source = outputSource,100)
})

method("getRecovery", "SingleNameCDSTRI", function(this,price,...) {
	needs(price = 'numeric')
	if(price > 0.7)return(0.1)
	if(price > 0.5)return(0.2)
	return(0.4)
})

method("recoveryRates", "SingleNameCDSTRI", function(this,...) {	
	spreadSeries <- this$.cds$specificSeries('spread',this$.strike,this$.docClause,startDate = this$.range$.start,endDate = this$.range$.end)
	priceSeries <- this$.cds$specificSeries('price',this$.strike,this$.docClause,startDate = this$.range$.start,endDate = this$.range$.end)
	recoveryZoo <- zoo(0.4,as.POSIXct(unique(c(index(spreadSeries),index(priceSeries)))))
	for(i in 1:NROW(recoveryZoo)){
		price <- as.numeric(priceSeries[index(recoveryZoo)[i]])
		if(NROW(price)>0)recoveryZoo[i] <- this$getRecovery(price)
	}
	if(NROW(recoveryZoo)==0)return(NULL)
	recoveryZoo
})

method("uploadRecoveryRates", "SingleNameCDSTRI", function(this,recoveryZoo,outputSource = this$.internalSource,...) {
	attL <- this$transformedAttributeList('recovery')
	tsName <- this$transformedTsName('recovery')	
	TimeSeriesDB()$createAndWriteOneTimeSeriesByName(recoveryZoo,tsName,outputSource,attL)
})