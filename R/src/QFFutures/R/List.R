constructor("List", function(contract = NULL) {
    this <- extend(RObject(), "List", .contract = contract)
    constructorNeeds(this,contract = "Contract")
    this$.lookup <- Map("POSIXt", "data.frame")
	this$.today <- as.POSIXlt(Sys.Date())
	this$.today$mday <- 1
	this$.today <- as.POSIXct(as.character(this$.today))
	this$.bloombergDateCutOff <- Period('months',11)$rewind(as.POSIXct(as.character(this$.today)))
    this
})

method("addToLookupIfNeeded", "List", function(this,date,rawList,...) {
    if(!this$isStoredList(date)) this$.lookup$set(as.POSIXct(date),rawList)
})

method("isStoredList", "List", function(this,date,...) {
    this$.lookup$has(as.POSIXct(date))
})

method("retrieveList", "List", function(this,date,...) {
    if(this$isStoredList(date))this$.lookup$fetch(as.POSIXct(date))
    else NULL    
})

method("adjusted", "List", function(this,date,rollObj,...) {
    rollDates <- this$.contract$rollDatesAsOf(rollObj,date,FALSE,...)
    this$raw(date)[date < rollDates,]
})

method("raw", "List", function(this,date,...){
    needs(date = "character|POSIXt")
    if(this$isStoredList(date))return(this$retrieveList(date))
	nextExpiryMonths <- this$nextExpiryMonths(date)	
    futureYears <- as.POSIXlt(nextExpiryMonths)$year + 1900; futureMonthNumerics <- as.POSIXlt(nextExpiryMonths)$mon + 1
    futureMonthStrings <- sapply(futureMonthNumerics,function(x){ifelse(nchar(x)==1,squish("0",x),x)})
    monthCodes <- sapply(futureMonthNumerics,function(x){MonthCode$letter(x)})
    tickers <- paste(this$.contract$name(),futureYears,futureMonthStrings,sep ="")		   
	nextExpiries <- unlist(sapply(as.list(as.character(nextExpiryMonths)),function(x){as.character(this$.contract$expiry(x))}))
	bbrgTickers <- as.character(mapply(function(x,y){this$bloombergFromExpiryMonth(x,y)},as.character(nextExpiryMonths),as.character(nextExpiries)))
    rawList <- data.frame(ticker = tickers,bbrg = bbrgTickers,futureYear = futureYears,futureMonth = futureMonthNumerics, monthCode = monthCodes,expiryDate = nextExpiries,stringsAsFactors = FALSE)	
    rawList <- rawList[rawList[,"expiryDate"]>=date,][1:(this$.contract$numCycles()*NROW(this$.contract$monthlyCycle())),]
	rawList <- rawList[!is.na(rawList[,'expiryDate']),]
    this$addToLookupIfNeeded(date,rawList)
    rawList
})

method("bloombergFromExpiryMonth", "List", function(this,expiryMonth,expiryDate,...){
	if(is.na(expiryDate))return(NA)
	futureYear <- as.POSIXlt(expiryMonth)$year + 1900
	futureMonthNumeric <- as.POSIXlt(expiryMonth)$mon + 1
	expiryDate <- as.POSIXlt(expiryDate); expiryDate$mday <- 1	
	if(expiryDate <= this$.bloombergDateCutOff)yearNum <- substr(as.character(futureYear),3,4)
	else yearNum <- substr(as.character(futureYear),4,4)
	toupper(squish(this$.contract$bloombergRoot(),MonthCode$letter(futureMonthNumeric),yearNum))	
})

method("nextExpiryMonths", "List", function(this,date,...){
    date <- as.Date(date); year <- as.numeric(substr(date,1,4)); mon <- as.numeric(substr(date,6,7))
    startDate <- as.Date(squish(year,"-",mon,"-1"))
	numCycles <- this$.contract$numCycles()+1 # We return one additional cycle to make sure we include enough expiries
    dateList <- NULL; for(i in 0:numCycles){dateList <- c(dateList,paste(year + i,"-",this$.contract$monthlyCycle(),"-",1,sep = ""))}
    as.POSIXct(dateList[dateList>=startDate][1:(numCycles*NROW(this$.contract$monthlyCycle()))])
})

method("dateFromTicker", "List", function(this,ticker,...){
	needs(ticker = 'character')
	as.POSIXct(squish(substr(ticker,nchar(ticker)-5,nchar(ticker)-2),"-",rightStr(ticker,2),"-1"))
})

method("tickerFromDate", "List", function(this,contractName,date,...){
	needs(date = 'character|POSIXt',contractName = 'character')
	newMon <- as.POSIXlt(date)$mon+1
	newMon <- ifelse(nchar(newMon)==1,squish("0",newMon),newMon)
	squish(contractName,as.POSIXlt(date)$year+1900,newMon)
})

method("bloombergFromYearMonth", "List", function(this,bloombergRoot,yearMonth,...){
	needs(bloombergRoot = 'character',yearMonth = 'character')
	squish(toupper(bloombergRoot),toupper(MonthCode$letter(rightStr(yearMonth,2))),as.numeric(substr(yearMonth,4,4)))
})