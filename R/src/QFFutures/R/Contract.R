constructor("Contract", function(
	name = NULL,
	yellowKey = NULL, bloomberRoot = NULL,monthlyCycle = NULL,numCycles = 1,
	bloombergExpirationField = 'FUT_LAST_TRADE_DT'
){
    this <- extend(RObject(), "Contract",
		.name = name,
		.jContract = NULL,
		.monthlyCycle = monthlyCycle,
		.numCycles = numCycles,
		.yellowKey = yellowKey,
		.bloomberRoot = bloomberRoot,
		.bloombergExpirationField = bloombergExpirationField,
		.list = NULL
	)
    constructorNeeds(this, name="character",bloomberRoot="character?",yellowKey="character?",monthlyCycle="numeric|integer?")
    if(inStaticConstructor(this)) return(this)
	this$.yellowKey <- this$yellowKey()	
	this$.bloombergRoot <- toupper(this$bloombergRoot())
	this$.monthlyCycle <- this$monthlyCycle()
    this$.jContract <- JContractCurrent$by_String_String(name,this$.yellowKey)
    class(this$.jContract) <- class(JContract())	
    this$.list <- List(this)
    this
})

############################################## Contract Definition ######################################

method("equals", "Contract", function(this, other, ...) {
	if (!this$.jContract$as(JContractCurrent())$equals_by_Object(other$.jContract))
		return(FALSE)
	return(all(
		equals(this$.name, other$.name),
		equals(this$.monthlyCycle, other$.monthlyCycle),
		equals(this$.numCycles, other$.numCycles),
		equals(this$.yellowKey, other$.yellowKey)
	))
})

method("bloombergSecurity", "Contract", function(this,bloombergTicker,...) {
	BloombergSecurity(squish(toupper(bloombergTicker),' ',this$.yellowKey))
})

method("name", "Contract", function(this,...) this$.name )

method("continuousName", "Contract", function(this,name = this$.name,...) squish(toupper(name),'.1C') )

method("gfutContinuousName", "Contract", function(this,name = this$.name,...) squish(toupper(name),'.GFUT.1C'))

method("tsContinuousName", "Contract", function(this,name = this$.name,...) squish(toupper(name),'.TS.1C'))

method("bloombergContinuousSecurity", "Contract", function(this,name = this$.name,...){
	SystemDB$getContinuousBloombergSecurity(this$gfutContinuousName(name))
})

method("nameFromMarket", "Contract", function(static,market,...) tolower(strsplit(market,"\\.")[[1]][1]))

method('yellowKey', 'Contract', function(this, ...){
	if(!is.null(this$.yellowKey))return(this$.yellowKey)	
	this$.yellowKey	 <- SystemDB$getYellowKey(this$continuousName())
	if(is.na(this$.yellowKey) || NROW(this$.yellowKey)==0)this$.yellowKey <- 'Comdty'
	assert(this$.yellowKey %in% this$yellowKeys(),squish(': ',this$.yellowKey,' is not a known bloomberg key (cf. Contract$yellowKeys())'))
	this$.yellowKey
})

method("bloombergRoot", "Contract", function(this,...) {
	if(!is.null(this$.bloombergRoot))return(this$.bloombergRoot)	
	this$.bloombergRoot	 <- SystemDB$getBloombergRoot(this$continuousName())
	if(is.na(this$.bloombergRoot) || NROW(this$.bloombergRoot)==0){
		if(nchar(this$.name)==1) this$.bloombergRoot <- squish(this$.name,' ') else this$.bloombergRoot <- this$.name		
	}	
	this$.bloombergRoot <- toupper(this$.bloombergRoot)
	this$.bloombergRoot
})

method("monthlyCycle", "Contract", function(this,...) {
	if(!is.null(this$.monthlyCycle))return(this$.monthlyCycle)	
	monthlyCycleCharacter <- this$bloombergSecurity(this$bloombergFrontContinuousRoot())$stringValue('FUT_GEN_MONTH')
	this$.monthlyCycle <- NULL
	for(i in 1:nchar(monthlyCycleCharacter))
		this$.monthlyCycle <- c(this$.monthlyCycle,MonthCode$number(substr(monthlyCycleCharacter,i,i)))
	this$.monthlyCycle
})

method("bloombergFrontContinuousRoot", "Contract", function(this,...) squish(this$.bloombergRoot,1))

method("numCycles", "Contract", function(this,...) this$.numCycles )

method('yellowKeys', 'Contract', function(static, ...){
	c('Index','Comdty','Curncy','Govt')
})

method("attributes", "Contract", function(this,quote_type = "close",quote_convention = "price",quote_side = "mid",...) {
	list(	contract = this$name(),
		instrument = "futures",
		quote_type = quote_type,
		quote_convention = quote_convention,
		quote_side = quote_side,
		expiry="actual"
	)
})

############################################# Expirations ###################################################

method("bloombergExpiry", "Contract", function(this,yearMonth,...) {	
	bloombergTicker <- this$.list$bloombergFromExpiryMonth(yearMonth,yearMonth)		
	res <- try(this$bloombergSecurity(bloombergTicker)$stringValue(this$.bloombergExpirationField),TRUE)			
	if(class(res)=='try-error')return(NA)
	if(abs(as.numeric(as.Date(res)-as.Date(yearMonth), units="days"))>180)return(NA)
	res
})

method("expiry", "Contract", function(this,yearMonth,...) {
	result <- try(this$systemDBExpiry(yearMonth),TRUE)
	if('try-error' %in% class(result)) this$bloombergExpiry(yearMonth) else result
})

method("systemDBExpiry", "Contract", function(this,yearMonth,...) {
	needs(yearMonth="character|POSIXt")
	yearMonth <- as.JDate(as.POSIXct(yearMonth))
	as.POSIXct(this$.jContract$expiry()$expiration_by_YearMonth(JYearMonth$by_Date(yearMonth)))
})

method("expiryDates", "Contract", function(this,date,...){
	as.POSIXct(this$.list$raw(date)[,"expiryDate"])
})

################################################### Roll Logic #############################################

method("rawList", "Contract", function(this,date,...) this$.list$raw(date))

method("adjustedList", "Contract", function(this,rollObj,date,...) this$.list$adjusted(date,rollObj))

method("rollDatesAsOf", "Contract", function(this,rollObj,date,excludeOldDates = TRUE,...){
    rollDates <- rollObj$rollDates(this$expiryDates(date,...))
    if(excludeOldDates)rollDates[rollDates >= as.POSIXct(date)]
    else rollDates
})

method("rollDatesInWindow", "Contract", function(this,startDate,endDate,rollObj,...){
	rollDates <- NULL; newDate <- startDate
	while (newDate <= endDate){
		rollDates <- c(rollDates,as.character(rollObj$rollDates(this$expiryDates(newDate))))
		newDate <- rollDates[NROW(rollDates)]
	}
	result <- as.POSIXct(unique(rollDates[rollDates <= endDate]))
	if(NROW(result) == 0) NULL else result
})

method("nth", "Contract", function(this,attribute,nth,rollObj,date,...){
	adjustedList <- this$.list$adjusted(date,rollObj)
	if(nth > NROW(adjustedList))return(NULL)
	adjustedList[nth,attribute]
})

method("frontContracts", "Contract", function(this, dates, roll, ...){
	needs(dates="POSIXct|character")
	if (any(is(dates)=="POSIXct")) dates <- format(dates,"%Y-%m-%d")
	contracts <- sapply(dates,function(x) this$nth("ticker",1,roll,x))
	return(zoo(contracts, as.POSIXct(dates)))
})

method("frontYearMonths", "Contract", function(this, dates, roll, ...){
	needs(dates="POSIXct")
	
	dates <- sort(as.POSIXct(format(dates,"%Y-%m-%d")))
	numCycles <- ceiling(as.numeric(difftime(last(dates),first(dates))/365.25))
	contract <- Contract(this$name(), this$yellowKey(), numCycles=numCycles+1)
	
	rollDates <- contract$rollDatesAsOf(roll,first(dates))
	contractSpecs <- contract$rawList(first(dates))
	contractSpecs[,"futureYearMonth"] <- contractSpecs[,"futureYear"]*100 + contractSpecs[,"futureMonth"]
	
	rollDateLookup <- as.Date(c(as.POSIXct('1900-01-01'),rollDates))
	rollDateValues <- as.Date(c(rollDates,as.POSIXct('2900-12-31')))
	frontExpiry <- approx(rollDateLookup,rollDateValues,as.Date(dates),method='constant',rule=2,f=0)$y	
	frontExpiry <- as.POSIXct(format(as.Date(frontExpiry)))
	frontExpiry[frontExpiry > last(rollDates)] <- NA
	frontContractYearMonth <- (as.POSIXlt(frontExpiry)$year + 1900)*100 + (as.POSIXlt(frontExpiry)$mon+1)
	return(zoo(frontContractYearMonth,dates))
})

################################################### Modified Methods #########################################

method("continuousSimple","Contract",function(this,nth,rollObj,...){
	this$callModifiedFuturesBuilder(nth,rollObj,"simple")
})

method("continuousRatio","Contract",function(this,nth,rollObj,...){
	this$callModifiedFuturesBuilder(nth,rollObj,"ratio")
})

method("unadjusted","Contract",function(this,nth,rollObj,...){
	this$callModifiedFuturesBuilder(nth,rollObj,"none")
})

method("callModifiedFuturesBuilder","Contract",function(this,nth,rollObj,adjustmentMethod,...){
	this$checkIfRawDataIsLoaded()
	cat("-------- Using Existing Data --------\n")
	cat(squish("StartDate: ",as.character(first(index(this$.rawData))),"\n"))
	cat(squish("EndDate: ",as.character(last(index(this$.rawData))),"\n"))
	MFB <- ModifiedFuturesBuilder(this,rollObj)
	modifiedSeries <- MFB$backAdjustedSeries(nth,adjustmentMethod)
	this$.modifiedRollSchedule <- MFB$rollSchedule()
	cat("--------- Roll Data -------\n")
	print(this$.modifiedRollSchedule)
	modifiedSeries
})

method("modifiedRollSchedule", "Contract", function(this,...){
	if(is.null(this$.modifiedRollSchedule))"Calculate Modified Series First!"
	else this$.modifiedRollSchedule
})

###########################################  Underlying data load #################################

method("checkIfRawDataIsLoaded", "Contract", function(this,...){
	failIf(is.null(this$.rawData),"You need to load raw data first! Use Contract$loadRawData(...)")
})

method("loadRawData","Contract",function(this,
	startDate = NULL,endDate = NULL,dataSource = "internal",timeStampFilter  = NULL,
	container = 'tsdb',rawData = NULL,quote_type = 'close'
,...){
	if(!is.null(rawData)){
		this$.rawData <- rawData
		return(this$.rawData)
	}
	this$.rawData <- do.call(merge, TSDataLoader$getDataByAttributeList(
		container=container, attributeList=this$attributes(quote_type = quote_type),
		source=dataSource,start=startDate, end=endDate, arrangeBy='ticker'
	))
	if(is.null(this$.rawData))fail("No Data Available!")
	names(index(this$.rawData)) <- c()
	if(!is.null(timeStampFilter))this$.rawData <- filter.zoo.byTime(this$.rawData,timeStampFilter)
	this$.rawData
})

method("gfutContinuousSeries", "Contract", function(this,
	startDate = NULL,endDate =  NULL, interval = NULL,field = 'LAST_PRICE',name = this$.name,...){
	ticker <- this$bloombergContinuousSecurity(name)
	TSDataLoader$getCloseDataFromBloomberg(ticker,startDate,endDate,interval,field)
})

method("bloombergCloses", "Contract", function(this, contractMonthNumber, contractYear, start = NULL, end = NULL, ...){
	needs(contractMonthNumber = 'numeric', contractYear = 'numeric')
	if(nchar(contractMonthNumber) < 2) monthCode <- paste('0', contractMonthNumber, sep = '') else monthCode <- contractMonthNumber
	dbName <- paste(this$name(), contractYear, monthCode, '_price_mid', sep = '')
	TimeSeriesDB$retrieveTimeSeriesByName(dbName, start = start, end = end, data.source = 'bloomberg')[[1]]			
})

method("continuousSeries", "Contract", function(this,name = this$.name,...){
	Symbol(this$continuousName(name))$series()	
})

method('gfutYearMonthFrame', 'Contract', function(static,...){
	dataFrame <- data.frame(marketName = SystemDB$getContinuousContracts('gfut'),field = NA,stringsAsFactors = FALSE)	
	for(i in 1:NROW(dataFrame)){
		security = BloombergSecurity(SystemDB$getContinuousBloombergSecurity(dataFrame[i,'marketName']))	
		dataFrame[i,'field'] <- as.character(as.Date(squish(1,security$stringValue("FUT_MONTH_YR")),"%d%b%y"))
		dataFrame[i,'field'] <- List$tickerFromDate(Contract$nameFromMarket(dataFrame[i,'marketName']),dataFrame[i,'field'])
	}
	dataFrame[order(dataFrame[,'marketName']),]
})