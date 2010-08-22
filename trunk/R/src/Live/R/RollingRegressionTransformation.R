constructor("RollingRegressionTransformation", function(
    market = NULL,
    nameLead = NULL,nameLag = NULL,
    gissingSeriesLead = NULL,gissingSeriesLag = NULL,
    updateRuleLead = NULL,updateRuleLag = NULL,
    regressionWindow = NULL,
    timeStamp = NULL,
	dataSource = "internal"
,...) {
	library(SystemDB)
    this <- extend(Transformation(), "RollingRegressionTransformation",
        .market = market,
        .nameLead = nameLead,
        .nameLag = nameLag,
        .updateRuleLead = updateRuleLead,
        .updateRuleLag = updateRuleLag,
        .regressionWindow = regressionWindow,
        .date = NULL,
        .notInitialized = TRUE,
        .timeStamp = timeStamp,
		.dataSource = dataSource
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this,
         market="character",
         nameLead = "character",nameLag = "character",
         gissingSeriesLead = "character",gissingSeriesLag = "character",
         updateRuleLead = "character",updateRuleLag = "character",        
         regressionWindow = "numeric",
		 dataSource = "character"
    )
    
    strLead <- the(strsplit(gissingSeriesLead, ':||:', fixed=TRUE)) 
    strLag <- the(strsplit(gissingSeriesLag, ':||:', fixed=TRUE))
    
    this$.gissingSeriesLead = SeriesDefinition(strLead[1],strLead[2],strLead[3])
    this$.gissingSeriesLag = SeriesDefinition(strLag[1],strLag[2],strLag[3])
    
    this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
    
    cat("RollingRegressionTransformation(", commaSep(c(
        market,
        nameLead,nameLag,
        gissingSeriesLead,gissingSeriesLag,
        updateRuleLead,updateRuleLag,
        as.character(regressionWindow)
    )), ")\n")
    
    this
})

method("setDate",  "RollingRegressionTransformation", function(this, date, ...) {
    needs(date="POSIXct")
    this$.date <- date
    this$.lastCloseDate <- businessDaysAgo(1,date)

    dout("This Date:", this$.date)   
    dout("Last Close:", this$.lastCloseDate)
})

method(".inputSeries", "RollingRegressionTransformation", function(this, ...) {
    list(gissingSeriesLead = this$.gissingSeriesLead,gissingSeriesLag = this$.gissingSeriesLag)
})

method(".outputSeries", "RollingRegressionTransformation", function(this, ...) {
    defROLLINGREGRESSION <- function(field) SeriesDefinition("ROLLINGREGRESSION", this$.market, field)
    defMARKETDATA <- function(field) SeriesDefinition("MARKETDATA", this$.market, field)
    list(
        LastZScore = defROLLINGREGRESSION("LastZScore"),
        LastResidual = defROLLINGREGRESSION("LastResidual"),
        LastSd = defROLLINGREGRESSION("LastSd"),        
        LastR2Adj = defROLLINGREGRESSION("LastR2Adj"),                
        LastR2 = defROLLINGREGRESSION("LastR2"),                       
        LastAlphaPVal = defROLLINGREGRESSION("LastAlphaPVal"), 
        LastBetaPVal = defROLLINGREGRESSION("LastBetaPVal"),         
        LastAlpha = defROLLINGREGRESSION("LastAlpha"),                 
        LastBeta = defROLLINGREGRESSION("LastBeta"),                        
        LastSlope = defROLLINGREGRESSION("LastSlope"),                                
        LastHedgeLag = defROLLINGREGRESSION("LastHedgeLag"),                                       
        LastHedgeLead = defROLLINGREGRESSION("LastHedgeLead"),                                              
        LastDailyTri = defROLLINGREGRESSION("LastDailyTri"),                                                    
        LastTri = defROLLINGREGRESSION("LastTri"),
        LastAlphaFactorRank = defROLLINGREGRESSION("LastAlphaFactorRank"),
        LastBetaFactorRank = defROLLINGREGRESSION("LastBetaFactorRank"),        
        Timestamp = defROLLINGREGRESSION("Timestamp"),
        HighPrice = defMARKETDATA("HighPrice"),
        LastPrice = defMARKETDATA("LastPrice"),
        LastVolume = defMARKETDATA("LastVolume"),
        LowPrice = defMARKETDATA("LowPrice"),
        OpenPrice = defMARKETDATA("OpenPrice"),
        MDTimestamp = defMARKETDATA("Timestamp")  
    )
})

method("initialize", "RollingRegressionTransformation", function(this, ...) {

    this$.dataClose <-  this$getCloseData()    
    this$.lastCloseTri <- this$getMarketLastCloseTri()
        
    this$.lastLead <- last(this$.dataClose[,"closeLead"])
    this$.lastLag <- last(this$.dataClose[,"closeLag"])
    
    cat("Last lead: ",this$.lastLead,"\n")
    cat("Last lag: ",this$.lastLag,"\n")    
    cat("Last Close Tri: ",this$.lastCloseTri,"\n")        
    
    "SUCCESS"
})

method("getTimeSeries", "RollingRegressionTransformation", function(this,name,...) {   
    parts <- the(strsplit(name, ":"))
    type <- first(parts)
    symbol <- second(parts)
    if(type == "tsdb"){
        resultData <- TimeSeriesDB()$retrieveOneTimeSeriesByName(symbol,data.source = this$.dataSource)
        resultData <- strip.times.zoo(resultData)
    }else if(type == "tsdb.f"){
        resultData <- TimeSeriesDB()$retrieveOneTimeSeriesByName(symbol,data.source = this$.dataSource)
		resultData <- resultData * SystemDB$bigPointValue(toupper(first(the(strsplit(symbol,"_")))))
    }else if(type == "csi"){
		s <- Symbol(symbol); resultData <- s$series()
		resultData <- resultData[,"close"] * SystemDB$bigPointValue(toupper(first(the(strsplit(symbol,"_")))))
	}else if(type == "bbg"){		
		resultData <- SystemPairsTradingESTY$getBloombergBarData(toupper(symbol))		
		resultData <- resultData * SystemDB$bigPointValue(toupper(symbol))		
    }else{
        fail("Don't know how to parse this lead/lag name: ", name)
    }
    return(resultData)
})

method("getMarketLastCloseTri", "RollingRegressionTransformation", function(this,...) {
    tsName <- paste(this$.market,"rolling_regression_1.0_tri",sep = "_") 
    lastCloseTriZoo <- TimeSeriesDB()$retrieveOneTimeSeriesByName(tsName,data.source = "internal",start = this$.lastCloseDate,end = this$.lastCloseDate)
    
    return(as.numeric(lastCloseTriZoo))
})

method("getCloseData", "RollingRegressionTransformation", function(this,...) {
    closeLead <- this$getTimeSeries(this$.nameLead) 
    closeLag <- this$getTimeSeries(this$.nameLag)	
    mergedData <- na.omit(merge(closeLead = closeLead,closeLag = closeLag))
    if(!is.null(this$.timeStamp))mergedData <- this$filterMergedData(mergedData)	
    mergedData <- mergedData[as.Date(index(mergedData)) < as.Date(this$.date),]
    n <- NROW(mergedData)
    failIf(n < this$.regressionWindow, "Not enough data to process transformation: ",n," is < than ",this$.regressionWindow)    
    mergedData <- mergedData[(n - this$.regressionWindow):n,]
    colnames(mergedData) <- c("closeLead","closeLag")
    return(mergedData)
})

method("filterMergedData", "RollingRegressionTransformation", function(this,mergedData,...) {       
    addThisZoo <- function(zooToAdd,zooFinal){
        bool <- !index(strip.times.zoo(zooToAdd)) %in% index(strip.times.zoo(zooFinal))
        rbind(zooToAdd[bool],zooFinal)
    }
    zooFinal <- filter.zoo.byTime(mergedData,this$.timeStamp)
    dates <- seq(as.POSIXct("2008-01-01 12:00:00"), as.POSIXct(squish("2008-01-01 ",this$.timeStamp)), 300)
    times <- strftime(as.POSIXlt(dates), "%H:%M:%S")
    timeStampPriorityOrder <- sort(times,TRUE)
    
    for (i in 1:NROW(timeStampPriorityOrder))zooFinal <- addThisZoo(filter.zoo.byTime(mergedData,timeStampPriorityOrder[i]),zooFinal)
    return(strip.times.zoo(zooFinal))
})

method("isBigChange", "RollingRegressionTransformation", function(this, oldValue,newValue,rule, ...) {
    needs(oldValue = "numeric", newValue="numeric", rule="character")
    parts <- the(strsplit(rule, ":"))
    rule <- first(parts)
    target <- as.numeric(second(parts))
    
    if(rule == "percent"){
        return(abs((newValue-oldValue)/oldValue) > target)
    }else if(rule == "absolute"){
        return(abs(newValue-oldValue) > target)
    }else{
        fail("Unknown update rule")
    }
})

method("skipUpdate", "RollingRegressionTransformation", function(this, inputs, ...) {

    leadChangedBasedOnRule <- (
        this$changed(inputs, "gissingSeriesLead")
        & 
        this$isBigChange(this$.lastLead,this$value(inputs, "gissingSeriesLead"),this$.updateRuleLead)
    )
    
    lagChangedBasedOnRule <- (
        this$changed(inputs, "gissingSeriesLag")
        & 
        this$isBigChange(this$.lastLag,this$value(inputs, "gissingSeriesLag"),this$.updateRuleLag)
    )
    
    if (any(c(leadChangedBasedOnRule,lagChangedBasedOnRule)) || this$.notInitialized)return(FALSE)
    TRUE
})

method("getNewTriZoo", "RollingRegressionTransformation", function(this,inputs,gissingSeriesName,name,...) {    
    result <- zoo(this$value(inputs,gissingSeriesName),this$.date)    
    parts <- the(strsplit(name, ":"))
	if(first(parts) == "csi")result <- result * SystemDB$bigPointValue(second(parts))
	if(first(parts) == "tsdb.f")result <- result * SystemDB$bigPointValue(toupper(first(the(strsplit(second(parts),"\_")))))			
	if(first(parts) == "bbg")result <- result * SystemDB$bigPointValue(toupper(second(parts)))
    return(result)
})

method("outputValues", "RollingRegressionTransformation", function(this, inputs, ...) {
    
    triLead <- c(this$.dataClose[,"closeLead"],this$getNewTriZoo(inputs,"gissingSeriesLead",this$.nameLead))
    triLag <- c(this$.dataClose[,"closeLag"],this$getNewTriZoo(inputs,"gissingSeriesLag",this$.nameLag))
    storeIn <- "liveResult"

   	pair <- Pair(
        seriesY = triLag,
        seriesX = triLead,
        triY = triLag,
        triX = triLead,
        normalizeToY = TRUE,
        multiplierY = zoo(1,index(triLag)),
        multiplierX = zoo(1,index(triLead))
	)
	
	pair$runChangesRollingRegression(window = this$.regressionWindow,constant = TRUE,storeIn = storeIn)
	pairResult <- pair$getModelResults(storeIn,this$.regressionWindow)
	                                        
    this$.lastLead <- last(triLead)
    this$.lastLag <- last(triLag)

    this$.notInitialized <- FALSE
    
    liveTri <- last(pairResult[,"tri"]) + this$.lastCloseTri
    
    list(
        this$outputs()$HighPrice$valueString(liveTri),
        this$outputs()$LastPrice$valueString(liveTri),
        this$outputs()$LastVolume$valueString(liveTri),
        this$outputs()$LowPrice$valueString(liveTri),
        this$outputs()$OpenPrice$valueString(liveTri),
        this$outputs()$MDTimestamp$now(),
        this$outputs()$LastZScore$valueString(last(pairResult[,"zScore"])),
        this$outputs()$LastResidual$valueString(last(pairResult[,"residual"])),
        this$outputs()$LastSd$valueString(last(pairResult[,"sd"])),
        this$outputs()$LastR2Adj$valueString(last(pairResult[,"r2Adj"])),
        this$outputs()$LastR2$valueString(last(pairResult[,"r2"])),
        this$outputs()$LastAlphaPVal$valueString(last(pairResult[,"alpha.pVal"])),
        this$outputs()$LastBetaPVal$valueString(last(pairResult[,"beta.pVal"])),
        this$outputs()$LastAlpha$valueString(last(pairResult[,"alpha"])),    
        this$outputs()$LastBeta$valueString(last(pairResult[,"beta"])),
        this$outputs()$LastSlope$valueString(last(pairResult[,"slope"])),
        this$outputs()$LastHedgeLag$valueString(last(pairResult[,"hedgeY"])),
        this$outputs()$LastHedgeLead$valueString(last(pairResult[,"hedgeX"])),
        this$outputs()$LastDailyTri$valueString(last(pairResult[,"dailyTri"])),                                                                           
        this$outputs()$LastTri$valueString(liveTri),
        this$outputs()$LastAlphaFactorRank$valueString(last(pairResult[,"alpha.factorRank"])),    
        this$outputs()$LastBetaFactorRank$valueString(last(pairResult[,"beta.factorRank"])),
        this$outputs()$Timestamp$now()
    )
})
