constructor("CreditIndexTriStitcher", function(ticker,type,tenor,timeStamp,holidayCenter,dataSource = "internal",ccy = 'usd',...)
{
	this <- extend(RObject(), "CreditIndexTriStitcher")
	if(inStaticConstructor(this))return(this)
	this$.ticker = ticker
	this$.type = type
	this$.tenor = tenor
	this$.timeStamp = timeStamp
	this$.holidayCenter = holidayCenter
	this$.dataSource = dataSource
	this$.tsdb = TimeSeriesDB()	
	this$.base = 100
	this$.ccy = ccy
	print(squish("Working on ", ticker,", ",type,", ",tenor))
	this
})

method("validTypes", "CreditIndexTriStitcher", function(this,...){c("otr")})

method("overrideDetails", "CreditIndexTriStitcher", function(this,ticker,...) {
	details <- data.frame(
		read.csv(system.file("credit_data","credit_index_jobs.csv", package = "QFCredit"), sep = ",", header = TRUE)
	,stringsAsFactors = FALSE)			
	details <- details[details[,1] == this$getMarkitNameFromTickerName(ticker),]
	if(NROW(details) == 0)throw("No override data for these attributes")
	result <- data.frame(as.character(details[,1]),as.numeric(details[,2]),as.numeric(details[,3]),as.POSIXct(details[,4]),as.POSIXct(details[,5]),stringsAsFactors= FALSE)	
	colnames(result) <- colnames(details)
	result[order(result[,"start_date"]),]	
})

method("getMarkitNameFromTickerName", "CreditIndexTriStitcher", function(this,ticker,...) {
	conn <- SQLConnection()
	conn$init()
	details <- conn$select(squish("select markit_name from cds_index_ticker where ticker_name = '", ticker, "'"))[[1]]
	rm(conn)
	if(NROW(details) == 0)throw("No markit name for this ticker")
	as.character(details)
})

method("getTickerNameFromMarkitName", "CreditIndexTriStitcher", function(this,markit,...) {
	conn <- SQLConnection()
	conn$init()
	details <- conn$select(squish("select ticker_name from cds_index_ticker where markit_name = '", markit, "'"))[[1]]
	rm(conn)
	if(NROW(details) == 0)throw("No ticker name for this markit name")
	as.character(details)
})

method("calcStitchedTri", "CreditIndexTriStitcher", function(this,startDate,...){
			
	# Retrieve roll dates from TSDB here, delete override detail table
	
	overrideDetails <- this$overrideDetails(this$.ticker)
	startDates <- overrideDetails[,"start_date"]
	series <- overrideDetails[,"otr_series"]
	versions <- overrideDetails[,"otr_version"]
	dailyTriZoo <- NULL
	dv01Zoo <- NULL
	for (i in 2:NROW(startDates)){
		print(squish("From ",as.character(startDates[i-1])," to ",as.character(startDates[i]),": Series ",series[i-1]," / Version ",versions[i-1]))
		tsNames <- c(
			CreditIndexTriBuilder$adjustTsNameForCcy(paste(this$.ticker,"tri_daily",this$.tenor,series[i-1],versions[i-1],sep = "_"),this$.ccy),
			CreditIndexTriBuilder$adjustTsNameForCcy(paste(this$.ticker,"dv01",this$.tenor,series[i-1],versions[i-1],sep = "_"),this$.ccy)
		)		
		end <- if(is.na(startDates[i]))NULL else startDates[i]
		start <- startDates[i-1]
		if(!is.null(startDate) && NROW(startDates[i-1]) > 0){
			if(startDate > startDates[i-1]) start <- startDate
		}
		rawData <- getMergedTimeSeries(this$.tsdb,tsNames,this$.dataSource,start = start,end = end)
		rawData <- rawData[as.Date(index(rawData)) > as.Date(startDates[i-1]),]
		if(is.null(dailyTriZoo))dailyTriZoo <- rawData[,tsNames[1]] else dailyTriZoo <- rbind(dailyTriZoo,rawData[,tsNames[1]])
		if(is.null(dv01Zoo))dv01Zoo <- rawData[,tsNames[2]] else dv01Zoo <- rbind(dv01Zoo,rawData[,tsNames[2]])
	}
	return(list(dailyTriZoo = dailyTriZoo,dv01Zoo = dv01Zoo))	
})

method("updateStitchedTri", "CreditIndexTriStitcher", function(this,checkDate,resList,...){
	errorMessage <- squish(this$.ticker," type ",this$.type,": no update for ",as.character(as.Date(checkDate)))
	if(is.null(resList))return(errorMessage)
	if(!is.null(checkDate))
		if(!as.Date(checkDate) %in% as.Date(index(resList$dailyTriZoo)))
			return(errorMessage)
	creditBuilder <- CreditIndexTriBuilder(this$.ticker,this$.type,this$.type,this$.tenor,this$.timeStamp,this$.holidayCenter,ccy = this$.ccy)
	creditBuilder$updateTSDB(resList$dailyTriZoo,resList$dv01Zoo)
	"pass"
})

constructor("CreditIndexTriBuilder", function(ticker,series,version,tenor,timeStamp,holidayCenter,outputSource = "internal",ccy = 'usd',...)
{
	this <- extend(RObject(), "CreditIndexTriBuilder")
	if(inStaticConstructor(this))return(this)
	this$.ticker = ticker
	this$.series = series
	this$.version = version
	this$.tenor = tenor
	this$.timeStamp = timeStamp
	this$.cdsSource = "markit"
	this$.irsSource = "internal"
	this$.outputSource = outputSource
	this$.holidayCenter = holidayCenter
	this$.tsdb = TimeSeriesDB()	
	this$.base = 100
	this$.ccy = ccy
	print(squish("Working on ", ticker,", ",tenor,", ",series,", ",version))
	this
})

method("indexDetails", "CreditIndexTriBuilder", function(this,ticker,tenor,series,...) {
	conn <- SQLConnection()
	conn$init()
	details <- conn$select(squish("
		select * 
		from cds_index_details a
		where 
		ticker_name = '", ticker, "' and 
		tenor = '",tenor,"' and
		series = '",series,"'
	"))
	rm(conn)
	if(NROW(details) == 0)throw("No index data for these attributes")
	list(strike = details[,"strike_bp"]/10000,effDate = as.POSIXct(details[,"effective_date"]),matDate = as.POSIXct(details[,"maturity_date"]))
})

method("getIrsTermStructure", "CreditIndexTriBuilder", function(this,...) {
	if(this$.ccy == 'usd') TermStructure$irs else TermStructure$irs_eur
})

method("calcRawTri", "CreditIndexTriBuilder", function(this,startDate,...) {
			
	details <- this$indexDetails(this$.ticker,this$.tenor,this$.series)
	effDate <- details$effDate
	matDate <- details$matDate
	strike <- details$strike
	
	holiday <- HolidayDataLoader(); holidayData <- holiday$getHolidays(source = "financialcalendar",financialCenter = this$.holidayCenter)
	dataCds <- getTermStructureForTimeSeries(squish(this$.ticker,"_market_spread_tenor_",this$.series,"_",this$.version),TermStructure$cds,this$.cdsSource,startDate)	
	dataCds <- subset(dataCds,!is.na(dataCds[,this$.tenor]))	
	dataIrs <- getTermStructureForTimeSeries(squish("irs_",this$.ccy,"_rate_tenor_mid"),this$getIrsTermStructure(),this$.irsSource,startDate)
	if(is.null(dataCds) || is.null(dataIrs))return(NULL)
	dataCds <- strip.times.zoo(dataCds); dataIrs <- strip.times.zoo(dataIrs);  
	dataCds <- filter.zoo.byIntersection(dataCds,dataIrs)
	dataIrs <- filter.zoo.byIntersection(dataIrs,dataCds)
 	
	returnDates <- index(dataCds)
	templateZoo <- zoo(0,as.POSIXct(paste(as.Date(index(dataCds[,1])),this$.timeStamp,sep = " ")))
	pnlZoo <- templateZoo; dv01Zoo = templateZoo
	for(i in 1:NROW(returnDates)){
		nextBusinessDate <- getFincadDateAdjust(startDate = returnDates[i],unit = "d", NumUnits = 1,holidayList = holidayData)
		cdsTable <- data.frame(effDate = as.POSIXlt(effDate),matDate = as.POSIXlt(matDate),spread = as.numeric(dataCds[i,this$.tenor])) 
		dfTable <- CDSPricer$getDiscountFactorTable(returnDates[i],dfCurve = NULL,swapRates = as.numeric(dataIrs[i,])/100,swapTenors = this$getIrsTermStructure(),cashRates = NULL,cashTenors = NULL)
		pricerOutput <- CDSPricer$getFincadPrice(
			direction = "buy",
			strike = strike,
			notional = this$.base,
			recovery = 0.4,
			valueDate = nextBusinessDate,
			effDate = effDate,
			matDate = matDate,
			cdsTable = cdsTable,
			dfTable = dfTable
		)
		pnlZoo[i] <- pricerOutput["pnl",] 
		dv01Zoo[i] <- pricerOutput["cds spread DVOI",]
	}
	return(list(dailyTriZoo = diff(pnlZoo),dv01Zoo = dv01Zoo))			
})

method("updateRawTri", "CreditIndexTriBuilder", function(this,checkDate,resList,...){
	errorMessage <- squish(this$.ticker," series ",this$.series," version ",this$.version,": no update for ",as.character(as.Date(checkDate)))
	if(is.null(resList))return(errorMessage)
	if(!is.null(checkDate))
		if(!as.Date(checkDate) %in% as.Date(index(resList$dailyTriZoo)))
			return(errorMessage)
	this$updateTSDB(resList$dailyTriZoo,resList$dv01Zoo)
	"pass"
})

method("adjustTsNameForCcy", "CreditIndexTriBuilder", function(this,tsName,ccy = this$.ccy,...) {
	if(ccy == 'eur')paste(tsName,ccy,sep = '_')
	else tsName
})

method("updateTSDB", "CreditIndexTriBuilder", function(this,dailyTriZoo,dv01Zoo,...) {
	dailyTriZoo <- na.omit(dailyTriZoo)
	dv01Zoo <- na.omit(dv01Zoo)
	isStitched <- this$.version %in% CreditIndexTriStitcher$validTypes()
		
	attL <- list(ccy = this$.ccy,ticker = this$.ticker,index_version = this$.version,index_series = this$.series,instrument = "cds_index",tenor = this$.tenor)
	# update daily TRI
	attL$quote_type <- "tri_daily"
	tsNameTriDaily <- paste(attL$ticker,attL$quote_type,this$.tenor,attL$index_series,attL$index_version,sep = "_")
	
	if(isStitched){
		attL$quote_type <- "tri"
		tsNameTriDaily <- paste(attL$ticker,attL$quote_type,this$.tenor,attL$index_series,sep = "_")
	}
	(this$.tsdb)$createAndWriteOneTimeSeriesByName(dailyTriZoo,this$adjustTsNameForCcy(tsNameTriDaily),this$.outputSource,attL)	
	# update DV01s
	attL$quote_type <- "dv01"
	tsNameDv01Daily <- paste(attL$ticker,attL$quote_type,this$.tenor,attL$index_series,attL$index_version,sep = "_")
	if(isStitched){
		tsNameDv01Daily <- paste(attL$ticker,attL$quote_type,this$.tenor,attL$index_series,sep = "_")
	}
	(this$.tsdb)$createAndWriteOneTimeSeriesByName(dv01Zoo,this$adjustTsNameForCcy(tsNameDv01Daily),this$.outputSource,attL)
	# update TRI
	returnDates <- index(dailyTriZoo)
	firstDate <- as.POSIXct(paste(as.Date(businessDaysAgo(1,first(returnDates),center = this$.holidayCenter)),this$.timeStamp,sep = " "))
	attL$quote_type <- "tri"
	tsNameTri <- paste(attL$ticker,attL$quote_type,this$.tenor,attL$index_series,attL$index_version,sep = "_")
	if(isStitched){
		attL$quote_type <- "tri_daily"
		tsNameTri <- paste(attL$ticker,attL$quote_type,this$.tenor,attL$index_series,sep = "_")
	}		
	updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,this$adjustTsNameForCcy(tsNameTriDaily),this$adjustTsNameForCcy(tsNameTri),attL,source = this$.outputSource,this$.base)
})