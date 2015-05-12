constructor("SingleNameCDS", function(ticker = NULL,tenor = '5y',ccy = 'usd',tier = 'snrfor',dataSource = 'internal',eqTicker = NULL,...)
{
	library(QFEquity)
	this <- extend(RObject(), "SingleNameCDS")
	if(inStaticConstructor(this))return(this)
	constructorNeeds(this,ticker = 'character?',eqTicker = 'character?',tenor = 'character',ccy= 'character',tier = 'character',dataSource = 'character')	
	if(is.null(ticker)) ticker <- this$cdsTickerFromEquity(eqTicker)
	if(is.null(eqTicker)) eqTicker <- this$equityTicker(ticker)
	this$.ticker <- ticker
	this$.eqTicker <- eqTicker
	this$.tenor <- tenor
	this$.ccy <- ccy
	this$.tier <- tier
	this$.source <- dataSource
	this$.strikeThreshold <- 400
	this$.root <- paste(ticker,tier,ccy,sep = '_')
	print(squish(ticker,'/',tier,'/',ccy,'/',tenor))
	this
})

method("specificAttrList", "SingleNameCDS", function(this,quote_type,strike,doc_clause,ticker = this$.ticker,...)
{
	list(
		ticker = ticker,ccy = this$.ccy,cds_strike = strike,tier = this$.tier,
		quote_type = quote_type,doc_clause = doc_clause,tenor = this$.tenor,instrument = 'cds',
		cds_ticker = paste(ticker,this$.tier,this$.ccy,doc_clause,sep = '_')
	)
})

method("specificSeries", "SingleNameCDS", function(this,quote_type,strike,doc_clause,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{	
	TimeSeriesDB()$retrieveTimeSeriesByAttributeList(this$specificAttrList(quote_type,strike,doc_clause),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("specificCurves", "SingleNameCDS", function(this,quote_type,strike,doc_clause,startDate = NULL,endDate = NULL,...)
{
	resList <- this$specificAttrList(quote_type,strike,doc_clause)
	timeSeriesName <- TimeSeriesDB()$lookupTimeSeriesByAttributeList(resList)
	if(NROW(timeSeriesName)==0)return(NULL)
	timeSeriesName <- sub(this$.tenor,'tenor',timeSeriesName)	
	getTermStructureForTimeSeries(timeSeriesName,TermStructure$cds,source = this$.source,startDate = startDate,endDate = endDate,lookFor = "tenor")	
})

method("populateDefaultId", "SingleNameCDS", function(this,doc_clause = NULL,...)
{
	needs(doc_clause = 'character')			
	spreadSeries <- this$specificSeries('spread','par',doc_clause)	
	lastSpread <- last(as.numeric(spreadSeries))*10000
	strike <- ifelse(lastSpread < this$.strikeThreshold,100,500)
	values <- c(
		paste(this$.root,doc_clause,'spread_5y',sep = '_'),
		paste(this$.root,'xr',strike,'spread_5y',sep = '_')
	)
	dates <- c(first(index(spreadSeries)),as.POSIXct('2009-04-08'))	
	this$createTimeSeriesIDs(zoo(values,dates))
})

method("timeSeriesIDs", "SingleNameCDS", function(this,...)
{
	TimeSeriesDB()$retrieveOneTimeSeriesByAttributeList(this$idsAttrList(),data.source = 'internal')
})

method("idsAttrList", "SingleNameCDS", function(this,...)
{
	list(
		ticker = this$.ticker,ccy = this$.ccy,cds_strike = 'market',tier = this$.tier,
		quote_type = 'id',doc_clause = 'market',tenor = this$.tenor,instrument = 'cds'		
	)
})

method("createTimeSeriesIDs", "SingleNameCDS", function(this,timeSeriesNameZoo,...)
{
	needs(timeSeriesNameZoo = 'zoo')
	for(i in 1:NROW(timeSeriesNameZoo)){
		try(TimeSeriesDB()$createAndWriteOneTimeSeriesByName(
			zoo(
				TimeSeriesDB()$lookupSeriesIDs(as.character(timeSeriesNameZoo)[i]),
				index(timeSeriesNameZoo)[i]
			),
			paste(this$.root,'id',this$.tenor,sep = '_'),
			this$.source,this$idsAttrList()
		),TRUE)	
	}
})

method("updateTimeSeriesIDs", "SingleNameCDS", function(this,
	frame = this$idFrame(),spreadSeries,updateTSDB = FALSE
,...){
	failIf(max(frame[,'startDate']) >= min(index(spreadSeries)),'You can not back-populate CDS Ids!')		
	lastTsName <- last(frame[,'tsName'])
	lastUpdate <- last(frame[,'startDate'])
	if(!this$isSNACTimeSeries(lastTsName))return(NULL)
	.timeSeriesNameDates <- NULL
	.timeSeriesNameValues <- NULL
	for(i in 1:NROW(spreadSeries)){
		lastSpread <- as.numeric(spreadSeries[i])
		lastDate <- index(spreadSeries[i])		
		strike <- ifelse(lastSpread * 10000 < this$.strikeThreshold,100,500)		
		newSeries <- paste(this$.root,'xr',strike,'spread_5y',sep = '_')			
		if(newSeries != lastTsName){
			.timeSeriesNameValues <- c(.timeSeriesNameValues,newSeries)
			.timeSeriesNameDates <- as.POSIXct(c(.timeSeriesNameDates,lastDate))
			lastTsName <- newSeries
		}
	}
	if(is.null(.timeSeriesNameDates))return(NULL)
	z <- zoo(.timeSeriesNameValues,.timeSeriesNameDates)
	print(z)
	if(updateTSDB)this$createTimeSeriesIDs(z)
	z
})

method("isSNACTimeSeries", "SingleNameCDS", function(this,tsName,...)
{
	attrList <- TimeSeriesDB()$lookupAttributesForTimeSeries(tsName)
	as.character(attrList[,'cds_strike'])!='par'
})

method("idFrame", "SingleNameCDS", function(this,quote_type = 'spread',...)
{
	zooIDs <- this$timeSeriesIDs()	
	tsNames <- TimeSeriesDB()$lookupSeriesNames(as.numeric(zooIDs))
	frame <- data.frame(startDate = NULL,endDate = NULL,tsName = NULL)
	for(i in 1:NROW(zooIDs)){
		attrList <- TimeSeriesDB()$lookupAttributesForTimeSeries(tsNames[i])
		attrList <- as.list(data.frame(attrList),stringsAsFactors = FALSE)
		attrList$quote_type <- quote_type
		if(i < NROW(zooIDs)) endDate <- Period('days',1)$rewind(index(zooIDs[i+1]))
		else endDate <- Range$today()
		tsName <- TimeSeriesDB()$lookupTimeSeriesByAttributeList(attrList)
		if(!is.null(tsName)){
			frame <- rbind(frame,
				data.frame(
					startDate = index(zooIDs[i]),endDate = endDate,
					tsName = tsName,
				stringsAsFactors = FALSE)
			)
		}
	}
	if(!this$isSNACTimeSeries(frame[NROW(frame),'tsName'])) frame[NROW(frame),'endDate'] <- as.POSIXct('2009-04-08')
	frame
})

method("genericAttrList", "SingleNameCDS", function(this,quote_type,...)
{
	list(
		ticker = this$.ticker,ccy = this$.ccy,cds_strike = 'market',tier = this$.tier,
		quote_type = quote_type,doc_clause = 'market',tenor = this$.tenor,instrument = 'cds'		
	)
})

method("genericSeries", "SingleNameCDS", function(this,quote_type,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{	
	TimeSeriesDB()$retrieveTimeSeriesByAttributeList(this$genericAttrList(quote_type),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("genericTsName", "SingleNameCDS", function(this,quote_type,...)
{
	squish(this$.root,'_market_',quote_type,'_',this$.tenor)
})

method("buildGeneric", "SingleNameCDS", function(this,quote_type,...)
{
	lookupFrame <- this$idFrame(quote_type)
	if(is.null(lookupFrame))return(NULL)
	genericZoo <- NULL
	for(i in 1:NROW(lookupFrame)){
		z <- TimeSeriesDB()$retrieveOneTimeSeriesByName(lookupFrame[i,'tsName'],start=lookupFrame[i,'startDate'],end=lookupFrame[i,'endDate'],data.source = this$.source)
		if(NROW(z)==0)z <- NULL
		if(is.null(genericZoo))genericZoo <- z
		else genericZoo <- rbind(genericZoo,z)
	}
	genericZoo
})

method("uploadGeneric", "SingleNameCDS", function(this,quote_type,z,holidayData = NULL,purgeTimeSeries = TRUE,...)
{
	attrList <- this$genericAttrList(quote_type)
	tsName <- this$genericTsName(quote_type)
	if(TimeSeriesDB()$timeSeriesExists(tsName) && purgeTimeSeries)TimeSeriesDB()$purgeTimeSeries(tsName,this$.source)
	TimeSeriesDB()$createAndWriteOneTimeSeriesByName(z,tsName,this$.source,attrList)	
	if(quote_type == 'tri_daily'){
		returnDates <- index(z)
		firstDate <- as.POSIXct(getFincadDateAdjust(startDate = first(returnDates),unit = "d", NumUnits = -1,holidayList = holidayData))
		attrListTri <- this$genericAttrList('tri')
		tsNameTri <- squish(this$.root,'_market_tri_',this$.tenor)
		if(TimeSeriesDB()$timeSeriesExists(tsNameTri))TimeSeriesDB()$purgeTimeSeries(tsNameTri,this$.source)
		updateCumTriFromDailyTri(TimeSeriesDB(),firstDate,returnDates,tsName,tsNameTri,attrListTri,source = this$.source,100)
	}
})

method("referenceUniverse", "SingleNameCDS", function(static,...)
{
	conn <- SQLConnection()
	conn$init()
	res <- conn$select("select ticker_id,tier_id,ccy_id from cds_ticker_universe")	
	data <- data.frame(
		ticker = static$tickerFromIds(res[,'ticker_id']),
		tier = static$tierFromIds(res[,'tier_id']),
		ccy = static$tierFromIds(res[,'ccy_id']),
		stringsAsFactors = FALSE
	)
	data <-data[order(data[,'ticker']),]
	rownames(data) <- 1:NROW(data)
	data
})

method(".applySql", "SingleNameCDS", function(static,ids,sql,...)
{
	conn <- SQLConnection()
	conn$init()	
	sapply(ids,function(x){as.character(conn$select(squish(sql,x,"'"))[[1]])})	
})

method("tickerFromIds", "SingleNameCDS", function(static,ids,...)
{
	as.character(static$.applySql(ids,"select ticker_name from ticker where ticker_id = '"))
})

method("idsFromTickers", "SingleNameCDS", function(static,tickers,...)
{		
	as.numeric(static$.applySql(tickers,"select ticker_id from ticker where ticker_name = '"))
})

method("idsFromCcy", "SingleNameCDS", function(static,tickers,...)
{		
	as.numeric(static$.applySql(tickers,"select ccy_id from ccy where ccy_name = '"))
})

method("idsFromTier", "SingleNameCDS", function(static,tickers,...)
{		
	as.numeric(static$.applySql(tickers,"select attribute_value_id from general_attribute_value where attribute_value_name = '"))
})

method("tierFromIds", "SingleNameCDS", function(static,ids,...)
{
	as.character(static$.applySql(ids,"select attribute_value_name from general_attribute_value where attribute_value_id = '"))	
})

method("ccyFromIds", "SingleNameCDS", function(static,ids,...)
{
	as.character(static$.applySql(ids,"select ccy_name from ccy where ccy_id = '"))
})

method("systemDBName", "SingleNameCDS", function(this,attr,...) toupper(squish('CDS.',this$.ticker,'.',this$.tenor,'.',attr)))

method("topicName", "SingleNameCDS", function(this,...) toupper(squish('CDS.',this$.ticker,'.',this$.tenor)))

method("cdsTicker", "SingleNameCDS", function(static,doc_clause = 'xr',...) squish(static$.root,'_',doc_clause))

method("addToUniverse", "SingleNameCDS", function(this,...)
{
	tickerId <- this$idsFromTickers(this$.ticker)
	ccyId <- this$idsFromCcy(this$.ccy)
	tierId <- this$idsFromTier(this$.tier)
	conn <- SQLConnection()
	conn$init()	
	conn$query(squish("INSERT INTO cds_ticker_universe (ticker_id,tier_id,ccy_id) VALUES (",tickerId,",",tierId,",",ccyId,")"))	
})

method("multipleGenericSeries", "SingleNameCDS", function(static,tickerList,quote_type,startDate = NULL,endDate = NULL,...)
{	 
	static$.multipleSeries(
		function(x){SingleNameCDS(ticker = x)$genericSeries(quote_type,startDate = startDate,endDate = endDate)},
		tickerList,startDate,endDate
	)
})

method(".multipleSeries", "SingleNameCDS", function(static,func,tickerList,startDate = NULL,endDate = NULL,...)
{	
	f <- function(x){		
		error <- try(res <- func(x),TRUE)
		if(class(error)!='zoo')return(NULL) else res
	}
	resList <- list(); for(i in 1:NROW(tickerList)) resList[[i]] <- f(tickerList[i])
	if(length(resList)==0)return(NULL)
	validRows <- NULL; for (i in 1:length(resList))if(NROW(resList[[i]])>0)validRows <- c(validRows,i)
	if(is.null(validRows))return(NULL)	
	resList <- do.call(merge.zoo,resList[validRows]); colnames(resList) <- tickerList[validRows]	
	m <- match(tickerList,colnames(resList))	
	resList <- getZooDataFrame(zoo(matrix(resList[,m],nrow = length(index(resList)),ncol = length(m)),index(resList)))
	colnames(resList) <- tickerList
	resList
})

method("securityID", "SingleNameCDS", function(this,ticker = this$.ticker,...)			
{
	conn <- SQLConnection()
	conn$init()
	res <- conn$select(squish("select option_metrics_id from credit_ticker_lookup where markit = '",ticker,"'"))
	res[[1]]
})

method("oldCdsTickerID", "SingleNameCDS",function(this,...)
{
	sectorTab <- read.csv(system.file("credit_data","internal industry classifications.csv", package = "QFCredit"), sep = ",", header = TRUE)
	tickerList <- as.character(sectorTab[,"cds.ticker.ID"])
	.tickerList <- as.character(sapply(tickerList,function(x){leftStr(x,nchar(x)-3)}))
	tickerList[.tickerList %in% this$.root]
})

method("spreadCalcInfo", "SingleNameCDS",function(this,priceSeries = NULL,spreadSeries = NULL,...){	
	if(is.null(priceSeries))return(list(isNeeded = FALSE))
	if(!is.null(spreadSeries))
		priceSeries <- priceSeries[!index(priceSeries) %in% index(spreadSeries)]
	if(NROW(priceSeries)==0)return(list(isNeeded = FALSE))
	list(isNeeded = TRUE,priceSeries = priceSeries)	
})

######################################## Equity methods

method('multipleAssetValues', 'SingleNameCDS', function(static, tickerList, startDate = NULL, endDate = NULL, ...){
		static$.multipleSeries(
			function(x) {SingleNameCDS(ticker = x)$assetValues(startDate = startDate, endDate = endDate)},
			tickerList, startDate, endDate
		)
})

method("multipleClosingPrices", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(
			function(x){SingleNameCDS(ticker = x)$closePrices(startDate = startDate,endDate = endDate)},
			tickerList,startDate,endDate
	)
})

method("multipleAdjPriceSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(
			function(x){SingleNameCDS(ticker = x)$adjClosePrices(startDate = startDate,endDate = endDate,'internal')},
			tickerList,startDate,endDate
	)
})

method("multipleVolSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(
			function(x){SingleNameCDS(ticker = x)$equityVol(startDate = startDate,endDate = endDate,'internal')},
			tickerList,startDate,endDate
	)
})

method("multipleSharesOutstandingSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(
			function(x){SingleNameCDS(ticker = x)$sharesOutstanding(startDate = startDate,endDate = endDate)},
			tickerList,startDate,endDate
	)
})

method("multipleLiabilitySeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(
			function(x){SingleNameCDS(ticker = x)$liabilities(startDate = startDate,endDate = endDate)},
			tickerList,startDate,endDate
	)
})

method("equityTicker", "SingleNameCDS", function(this,ticker = this$.ticker,...)			
{
	conn <- SQLConnection()
	conn$init()
	res <- conn$select(squish("select bloomberg from credit_ticker_lookup where markit = '",ticker,"'"))
	as.character(res[[1]])
})

method("cdsTickerFromEquity", "SingleNameCDS", function(static,ticker,...)			
{
	conn <- SQLConnection()
	conn$init()
	res <- conn$select(squish("select markit from credit_ticker_lookup where bloomberg = '",ticker,"'"))
	as.character(sort(res[[1]])[1])
})

method("bloombergField", "SingleNameCDS",function(this,field = NULL,...)this$bloombergSecurity()$stringValue(field))

method("bloombergSecurity", "SingleNameCDS",function(this,...)BloombergSecurity(squish(this$.eqTicker,' Equity')))

method("equityVolTsName", "SingleNameCDS", function(this,...) squish("ivydb_",this$securityID(),"_91d_atm_put_vol_ln_mid"))

method("equityVol", "SingleNameCDS", function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$equityVolTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("closePrices", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	EquityListDataLoader()$getClosePrices(securityIDList = this$securityID(),source = dataSource,startDate = startDate,endDate = endDate)
})

method("adjClosePrices", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	EquityListDataLoader()$getAdjClosePrices(securityIDList = this$securityID(),source = dataSource,startDate = startDate,endDate = endDate)
})

method("liabilities", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{	
	data <- TimeSeriesDB()$retrieveTimeSeriesByName(this$liabilitiesTsName(),data.source = 'bloomberg',start = startDate,end = endDate)[[1]]
	if(!is.null(data))make.zoo.daily(data,'16:00:00')
})

method("sharesOutstanding", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = 'ivydb',...)
{
	EquityListDataLoader()$getSharesOutstanding(securityIDList = this$securityID(),source = dataSource,startDate = startDate,endDate = endDate)
})

method("equityFactors", "SingleNameCDS",function(this,dataSource = this$.source,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$equityFactorsTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("equityFactorsTsName", "SingleNameCDS",function(this,...)
{
	squish("ivydb_",this$securityID(),"_total_return_factor_mid")
})

method("liabilitiesTsName", "SingleNameCDS",function(this,...)
{
	squish("ivydb_",this$securityID(),"_total_liabilities")
})

######################################## Arbitration methods

method("filterPositiveVol", "SingleNameCDS",function(this,...)
{
	data <- this$equityVol()
	TimeSeriesDB()$purgeTimeSeries(this$equityVolTsName(),'internal')
	TimeSeriesDB()$writeOneTimeSeriesByName(data[as.numeric(data)>0],this$equityVolTsName(),'internal')
})

method("arbitrateDTD", "SingleNameCDS",function(this,z,...)this$arbitrate(z,this$dtdTsName()))

method("arbitrateAssetValues", "SingleNameCDS",function(this,z,...)this$arbitrate(z,this$assetValueTsName()))

method("arbitrateAssetVol", "SingleNameCDS",function(this,z,...)this$arbitrate(z,this$assetVolTsName()))

method("arbitrateRichCheap", "SingleNameCDS",function(this,z,...)this$arbitrate(z,this$richCheapTsName()))

method("arbitrateVol", "SingleNameCDS",function(this,z,useExistingData = FALSE,...)this$arbitrate(z,this$equityVolTsName(),useExistingData = useExistingData))

method("arbitrateVolFromIvyDB", "SingleNameCDS",function(this,...){
	dataIvyDB <- make.zoo.daily(this$equityVol(dataSource = 'ivydb'),'16:00:00')
	this$arbitrateVol(dataIvyDB)
	this$filterPositiveVol()
})

method("arbitrateLiabilities", "SingleNameCDS",function(this,z,...)this$arbitrate(z,this$liabilitiesTsName(),'bloomberg'))

method("arbitrateLiabilitiesFromBloomberg", "SingleNameCDS",function(this,...){
	range <- Range(as.POSIXct('1996-01-01'),Range$today())	
	addTimeStamp <- function(z){index(z) <- as.POSIXct(paste(index(z),' 16:00:00'));z}
	liabilities <- addTimeStamp(this$bloombergSecurity()$observations('BS_TOT_LIAB2',range))
	baseZoo <- addTimeStamp(this$adjClosePrices())
	res <- na.locf(merge(liabilities,baseZoo))[,1]
	this$createLiabilityTimeSeries()
	this$arbitrateLiabilities(res)
})

method("calcEquityAdjPriceFromScratch", "SingleNameCDS",function(this,...){
	dataIvyDB <- this$closePrices(dataSource = 'ivydb')
	this$arbitrate(dataIvyDB,squish('ivydb_',this$securityID(),'_close_price_mid'))
	dataIvyDB <- strip.times.zoo(this$equityFactors(dataSource = 'ivydb'))
	this$arbitrate(dataIvyDB,squish('ivydb_',this$securityID(),'_total_return_factor_mid'))
	result <- EquityDataLoader$calcAdjClosePrices(this$securityID())
	result <- EquityDataLoader$calcAdjOpenPrices(this$securityID())
	result <- EquityDataLoader$calcAdjHighPrices(this$securityID())
	result <- EquityDataLoader$calcAdjLowPrices(this$securityID())
})

method("createLiabilityTimeSeries", "SingleNameCDS",function(this,...){
	tsName <- this$liabilitiesTsName()
	if(!TimeSeriesDB()$timeSeriesExists(tsName)){
		attr <- list(quote_type = 'total_liabilities',security_id = this$securityID(),instrument = 'equity')
		TimeSeriesDB()$createTimeSeries(tsName,attr)
	}
})

method("arbitrateEquityFactors", "SingleNameCDS",function(this,z,useExistingData = FALSE,...)this$arbitrate(z,this$equityFactorsTsName(),useExistingData = useExistingData))

method("arbitrate", "SingleNameCDS",function(this,z,tsName,dataSource = 'internal',useExistingData = FALSE,...)			
{
	needs(z = 'zoo')
	data <- TimeSeriesDB()$retrieveOneTimeSeriesByName(tsName,dataSource)
	data <- data[!index(data) %in% index(z)]
	TimeSeriesDB()$purgeTimeSeries(tsName,dataSource)
	if(useExistingData)TimeSeriesDB()$writeOneTimeSeriesByName(data,tsName,dataSource)
	if(NROW(na.omit(z))>0) TimeSeriesDB()$writeOneTimeSeriesByName(z,tsName,dataSource)
})


######################################## DTD methods
method("fairValuesTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_option_fit_fair_values'))

method("fairValue", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
		TimeSeriesDB()$retrieveTimeSeriesByName(this$fairValuesTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
)

method("multipleFairValuesSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,dataSource = 'internal',...)
		static$.multipleSeries(function(x){SingleNameCDS(ticker = x)$fairValue(startDate = startDate,endDate = endDate,dataSource = dataSource)},tickerList,startDate,endDate)
)

method("assetValueTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_asset'))
method("assetVolTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_sigma'))
method("dtdTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd'))
method("richCheapTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_option_fit_rich_cheaps'))
method("fairValuesTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_option_fit_fair_values'))
method("deltaTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_option_fit_delta'))
method("dtdTriTsName", "SingleNameCDS",function(this,...) this$genericTsName('dtd_option_fit_acb'))
method("dtdVolTsName", "SingleNameCDS",function(this,...) 'dtd_option_fit_vol')
method("dtdStrikeTsName", "SingleNameCDS",function(this,...) 'dtd_option_fit_strike')
method("dtdAlphaTsName", "SingleNameCDS",function(this,...) 'dtd_option_fit_alpha')
method("dtdShiftTsName", "SingleNameCDS",function(this,...) 'dtd_option_fit_shift')
method("dtdBetaTsName", "SingleNameCDS",function(this,...) 'dtd_option_fit_beta')

method("purgeDTDOutputs", "SingleNameCDS",function(this,dataSource = 'internal',...)			
{
	TimeSeriesDB()$purgeTimeSeries(this$dtdTsName(),dataSource)
	TimeSeriesDB()$purgeTimeSeries(this$assetVolTsName(),dataSource)
	TimeSeriesDB()$purgeTimeSeries(this$assetValueTsName(),dataSource)
})

method("purgeOptionFitSeries", "SingleNameCDS",function(this,dataSource = 'internal',...)			
{
	data <- TimeSeriesDB()$lookupTimeSeriesByAttributeList(list(transformation = 'dtd_option_fit'))
	sapply(data,function(x){TimeSeriesDB()$purgeTimeSeries(x,dataSource)})
	data <- TimeSeriesDB()$lookupTimeSeriesByAttributeList(list(
	quote_type = c(thisdtdTriTsName(),'dtd_option_fit_aig','dtd_option_fit_ahv',this$deltaTsName(),this$richCheapTsName(),'dtd_option_fit_fair_values')))
	sapply(data,function(x){TimeSeriesDB()$purgeTimeSeries(x,dataSource)})
})

method("calcDTD", "SingleNameCDS",function(this,refDate = NULL, startDate = NULL,endDate = NULL,...)			
{
	dtd <- SystemDTD()
	dtd$runDTD(transformationName = "dtd_1.0",volExpiry = "91d",optionType = "put",
		tickerList = this$.ticker,tenor = "5y",refDate = refDate,startDate = startDate, endDate = endDate,	
		useConstrOptim = TRUE,updateTSDB = TRUE)
	dtd
})

method('dtdCoefficients', 'SingleNameCDS', function(static,myDate,dataSource = 'internal',...){
	data <- data.frame(
		static$dtdAlpha(myDate,myDate),static$dtdBeta(myDate,myDate),
		static$dtdStrike(myDate,myDate),static$dtdAdjStrike(myDate,myDate),
		static$dtdVol(myDate,myDate)
	)
	if(NROW(data)>0)colnames(data) <- c("alpha","beta","strike","adjStrike","vol")
	data
})

method("dtdBeta", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdBetaTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("dtdAlpha", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdAlphaTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("dtdStrike", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdStrikeTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("dtdAdjStrike", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	this$dtdStrike(startDate = startDate,endDate = endDate) - this$dtdShift(startDate = startDate,endDate = endDate)
})

method("dtdShift", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdShiftTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("dtdVol", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdVolTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("dtd", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("fairValue", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$fairValuesTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("richCheap", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$richCheapTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("delta", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$deltaTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("dtdTri", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,dataSource = this$.source,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$dtdTriTsName(),data.source = dataSource,start = startDate,end = endDate)[[1]]
})

method("assetValues", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$assetValueTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("assetVols", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	TimeSeriesDB()$retrieveTimeSeriesByName(this$assetVolTsName(),'internal',start = startDate,end = endDate)[[1]]
})

method("dtdOutputData", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	dataList <- list();validElement <- NULL
	dataList$dtds <- this$dtd()
	dataList$richCheaps <- this$richCheap()
	dataList$adjTri <- this$dtdTri()
	dataList$delta <- this$delta()
	dataList$a <- this$assetValues()
	dataList$sigma <- this$assetVols()
	for(i in 1:length(dataList))if(NROW(dataList[[i]])>0)validElement <- c(validElement,i)
	m <- do.call(merge,dataList[validElement]); colnames(m) <- names(dataList)[validElement]
	m
})

method("dtdInputData", "SingleNameCDS",function(this,startDate = NULL,endDate = NULL,...)
{
	dataList <- list(); validElement <- NULL
	dataList$spreads <- this$genericSeries('spread',startDate = startDate,endDate = endDate)
	dataList$volatility <- this$equityVol(startDate = startDate,endDate = endDate)
	dataList$adjClosePrices <- this$adjClosePrices(startDate = startDate,endDate = endDate)
	dataList$liabilities <- this$liabilities(startDate = startDate,endDate = endDate)
	dataList$sharesOutstanding <- this$sharesOutstanding(startDate = startDate,endDate = endDate)	
	for(i in 1:length(dataList))if(NROW(dataList[[i]])>0)validElement <- c(validElement,i)
	m <- do.call(merge,dataList[validElement]); colnames(m) <- names(dataList)[validElement]
	m
})

method("multipleAssetSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(function(x){SingleNameCDS(ticker = x)$assetValues(startDate = startDate,endDate = endDate)},tickerList,startDate,endDate)
})

method("multipleAssetVolSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,...)
{
	static$.multipleSeries(function(x){SingleNameCDS(ticker = x)$assetVols(startDate = startDate,endDate = endDate)},tickerList,startDate,endDate)
})

method("multipleDTDSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,dataSource = 'internal',...)
{
	static$.multipleSeries(function(x){SingleNameCDS(ticker = x)$dtd(startDate = startDate,endDate = endDate,dataSource = dataSource)},tickerList,startDate,endDate)
})

method("multipleFairValuesSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,dataSource = 'internal',...)
{
	static$.multipleSeries(function(x){SingleNameCDS(ticker = x)$fairValue(startDate = startDate,endDate = endDate,dataSource = dataSource)},tickerList,startDate,endDate)
})

method("multipleRichCheapSeries", "SingleNameCDS", function(static,tickerList,startDate = NULL,endDate = NULL,dataSource = 'internal',...)
{
	static$.multipleSeries(function(x){SingleNameCDS(ticker = x)$richCheap(startDate = startDate,endDate = endDate,dataSource = dataSource)},tickerList,startDate,endDate)
})