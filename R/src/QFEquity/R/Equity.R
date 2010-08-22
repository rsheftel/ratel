constructor("Equity", function(ticker = NULL,securityId = NULL,owner = 'Jerome Bourgeois',...)
{
	library(SystemDB)
	this <- extend(RObject(), "Equity")
	if(inStaticConstructor(this))return(this)	
	if(is.null(ticker)) ticker <- EquityDataLoader$getTickerFromSecurityID(securityId)
	if(is.null(securityId)) securityId <- EquityDataLoader$getSecurityIDFromTicker(ticker)
	this$.ticker <- ticker
	this$.bbrg <- squish(toupper(this$.ticker),' Equity')
	this$.bbgSec <- BloombergSecurity(this$.bbrg)
	this$.securityId <- securityId
	this$.owner <- owner
	this$.asOf <- as.character(Range$today())
	print(squish(ticker,'/',securityId))
	this
})

method("financedTris","Equity",function(this,dataSource = 'internal',ohlc = c('open','high','low','close'),...){
	getMergedTimeSeries(TimeSeriesDB(),sapply(ohlc,function(x)this$tsName(TRUE,x)),dataSource)
})

method("prices","Equity",function(this,dataSource = 'internal',ohlc = c('open','high','low','close'),...){	
	getMergedTimeSeries(TimeSeriesDB(),sapply(ohlc,this$tsNameRaw),dataSource)
})

method("bbgSec","Equity",function(this,...) this$.bbgSec)

method("bbgClose","Equity",function(this,...) this$.bbgSec$observations(...))

method("bbgStaticNumber","Equity",function(this,...) this$.bbgSec$numberValue(...))

method("bbgIntraday","Equity",function(this,...) this$.bbgSec$bars(...))

method("tsNameLiabilities","Equity",function(this,ohlc,...) squish('ivydb_',this$.securityId,'_total_liabilities'))

method("tsNameRaw","Equity",function(this,ohlc,...) squish('ivydb_',this$.securityId,'_',ohlc,'_price_mid'))

method("tsNameFactors","Equity",function(this,...) squish('ivydb_',this$.securityId,'_total_return_factor_mid'))

method("tsName","Equity",function(this,isFinanced,ohlc,...)
{
	if(isFinanced){
		if(ohlc == 'close') squish(this$.securityId,'_tri_vLibor')
		else squish(this$.securityId,'_',ohlc,'_tri_vLibor')
	}else{
		squish('ivydb_',this$.securityId,'_',ohlc,'_adj_price_mid')	
	}
})


############################################ SystemDB update #############################################

method("insertInSystemDB","Equity",function(this,subSector,...)
{
	this$insertTimeSeriesTable(TRUE,subSector)
	this$insertTimeSeriesTable(FALSE,subSector)
	this$insertMarketTable(TRUE)
	this$insertTSDBTable(FALSE,subSector)
	this$insertTSDBTable(TRUE,subSector)
	if(subSector == '') this$insertEquityLookupTable(subSector)
			else this$insertETFLookupTable(subSector)
})

method("updateSeries","Equity",function(this,...)
{
	# arbitrate close / factor data	
	data <- strip.times.zoo(TimeSeriesDB()$retrieveOneTimeSeriesByName(this$tsNameRaw(raw = TRUE),'ivydb'))
	SingleNameCDS$arbitrate(data,this$tsNameRaw(),'internal',useExistingData = FALSE)
	data <- strip.times.zoo(TimeSeriesDB()$retrieveOneTimeSeriesByName(this$tsNameFactors(raw = TRUE),'ivydb'))
	SingleNameCDS$arbitrate(data,this$tsNameFactors(),'internal',useExistingData = FALSE)
	data <- strip.times.zoo(TimeSeriesDB()$retrieveOneTimeSeriesByName(this$tsNameFactors(raw = TRUE),'ivydb'))
	SingleNameCDS$arbitrate(data,this$tsNameFactors(),'internal',useExistingData = FALSE)
	# calc adjusted series
	EquityDataLoader$calcAdjClosePrices(this$.securityID,1,1)
	EquityDataLoader$calcAdjOpenPrices(this$.securityID,1,1)
	EquityDataLoader$calcAdjHighPrices(this$.securityID,1,1)
	EquityDataLoader$calcAdjLowPrices(this$.securityID,1,1)	
	for(quoteType in c("close","open","high","low")){		
		FETL <- FinancedEquityTRILoader(quoteType = quoteType)
		FETL$financeAndUploadEquityTRIs(securityIDList = this$.securityID, uploadMethod = 'direct')	
	}
	"Done"
})

method("insertEquityLookupTable","Equity",function(this,...)
{
	sqlString <- squish(
			"insert into TSDB..credit_ticker_lookup
					(markit,
					bloomberg,
					option_metrics_id)
					values ('",
			tolower(this$.ticker),"','",
			tolower(this$.ticker),"','",
			this$.securityID,"')"
			)
			SystemDB$sqlInsert(sqlString)
})

method("insertETFLookupTable","Equity",function(this,...)
{
	sqlString <- squish(
			"insert into TSDB..etf_ticker_lookup
					(bloomberg,					
					option_metrics_id)
					values ('",
			tolower(this$.ticker),"','",			
			this$.securityID,"')"
	)
	SystemDB$sqlInsert(sqlString)
})

method("insertMarketTable","Equity",function(this,isFinanced,...)
{
	sqlString <- squish(
			"insert into SystemDB..Market
					(Name,
					Weighting_function,
					Rebalance_function,
					ID,
					BigPointValue,
					Slippage,
					SlippageCalculator)
					values ('",
			this$name(isFinanced),"','",
			'',"','",
			'',"','",
			'',"','",
			'',"','",
			'',"','",
			'',"')"
	)
	SystemDB$sqlInsert(sqlString)
})

method("insertTSDBTable","Equity",function(this,isFinanced,subSector,...)
{
	sqlString <- squish(
			"insert into SystemDB..TSDB
					(Name,
					Data_source,
					Name_open,
					Name_high,
					Name_low,
					Name_close,
					Name_volume,
					Name_open_interest,
					Calculate_method,
					StartDate,
					VerifyBy,
					VerifyDate,
					Template,
					Comments)
					values ('",
			this$name(isFinanced),"','",
			'internal',"','",
			this$tsName(isFinanced,'open'),"','",
			this$tsName(isFinanced,'high'),"','",
			this$tsName(isFinanced,'low'),"','",
			this$tsName(isFinanced,'close'),"','",			
			'',"','",
			'',"','",
			'OmitIncomplete',"','",
			'',"','",			
			this$.owner,"','",
			this$.asOf,"','",
			'FALSE',"','",
			'',"')"
	)
	SystemDB$sqlInsert(sqlString)
})

method("insertTimeSeriesTable","Equity",function(this,isFinanced,subSector,...)
{
	sqlString <- squish(
			"insert into SystemDB..Time_series_data
					(Name,
					Long_name,
					SubSector,
					Country,
					Type,
					Exchange,
					Expiry,
					OptionFlag,
					TemplateFlag,
					HistDaily,
					HistIntraday,
					HistTick,
					TodayTick,
					Live,
					UseTSDBAdjustment,
					Description,
					Owner,
					AsOf)
					values ('",
			this$name(isFinanced),"','",
			this$longName(isFinanced),"','",
			subSector,"','",
			'USD',"','",
			'Index',"','",
			this$exchange(),"','",
			'ReturnIndex',"','",
			'FALSE',"','",
			'TSDB',"','",
			'',"','",
			'',"','",
			'',"','",
			'ActiveMQ',"','",
			'FALSE',"','",
			this$longName(isFinanced),"','",
			this$.owner,"','",
			this$.asOf,"')"
	)
	SystemDB$sqlInsert(sqlString)
})


method("name","Equity",function(this,isFinanced,...)
{
	if(!isFinanced)toupper(this$.ticker) else squish(toupper(this$.ticker),'.TRI')
})

method("exchange","Equity",function(this,...)
{
	exch <- this$.bbgSec$stringValue('ID_MIC_PRIM_EXCH')
	if(exch == 'XNYS')return('NYSE')
	else throw('Exchange not defined')
})

method("longName","Equity",function(this,isFinanced,...)
{
	if(!isFinanced)
		squish(this$.ticker,' split and dividend adjusted') 
	else 
		squish(this$.ticker,' financed TRI') 
})