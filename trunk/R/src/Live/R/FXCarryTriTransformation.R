constructor("FXCarryTriTransformation", function(currencyPair=NULL,putCall=NULL,...) {
    this <- extend(Transformation(), "FXCarryTriTransformation",.currencyPair=currencyPair)
    if(inStaticConstructor(this)) return(this)    
	constructorNeeds(this,currencyPair="character",putCall="character")
	this$.FXNotional <- FXNotional(0.0001,"over")
    this$.FXCurr <- FXCurr$setByCross(currencyPair)
	this$.putCall <- putCall
	this$.tenor <- "6m"
	this$.triMarketName <- squish(toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under()),".Tri.",toupper(this$.FXCurr$over()),
		toupper(this$.FXCurr$under()),"6MTRI.",toupper(substr(this$.putCall,0,1)),"1")
	this$.payoutRatioMarketName <- squish(toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under()),".Signal.",toupper(this$.FXCurr$over()),
		toupper(this$.FXCurr$under()),"6MPOUT.",toupper(substr(this$.putCall,0,1)))
	this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
	this$.option <- FXEuropeanOptionGeneric(FXForwardGeneric(this$.FXCurr,"mid",this$.tenor),expiry=this$.tenor, putCall=this$.putCall)
	this$.conversionCurrency <- NULL
	if (!(this$.FXCurr$over()=="usd")) this$.conversionCurrency <- ReturnCorrectFXPair(this$.FXCurr$over(),"usd")
	cat("FXCarryTriTransformation()\n")
    this
})

method("setDate",  "FXCarryTriTransformation", function(this, date, ...) {			
    needs(date="POSIXct")
	this$.date <- date
	this$.yesterday <- getFincadDateAdjust(date, "d",-1, this$holidays())
	this$.notInitialized <- TRUE
})

method("holidays", "FXCarryTriTransformation", function(this, ...) {
	lazy(this$.holidays, HolidayDataLoader$getHolidays(source = "financialcalendar", financialCenter = "nyb"), log=FALSE)
})

method("getCloseData", "FXCarryTriTransformation", function(this, ...) {
	
		lazy(
		this$.data,
		this$.data <- FXDataLoad(this$.FXCurr, this$.yesterday, this$.yesterday, TimeSeriesDB(), tenor="2y"))
		
		temp.zoo <- this$.data$.rateData
		index(temp.zoo) <- this$.date
		this$.data$.rateData <- rbind(this$.data$.rateData,temp.zoo)
		
		temp.zoo <- this$.data$.volData
		index(temp.zoo) <- this$.date
		this$.data$.volData <- rbind(this$.data$.volData,temp.zoo)
		
		temp.zoo <- this$.data$.overRepoData
		index(temp.zoo) <- this$.date
		this$.data$.overRepoData <- rbind(this$.data$.overRepoData,temp.zoo)
		
		temp.zoo <- this$.data$.underRepoData
		index(temp.zoo) <- this$.date
		this$.data$.underRepoData <- rbind(this$.data$.underRepoData,temp.zoo)
		
		temp.zoo <- this$.data$.settleData
		index(temp.zoo) <- this$.date
		this$.data$.settleData <- rbind(this$.data$.settleData,temp.zoo)
		
		temp.zoo <- this$.data$.expiryData
		index(temp.zoo) <- this$.date
		this$.data$.expiryData <- rbind(this$.data$.expiryData,temp.zoo)
})

method("getOptionData", "FXCarryTriTransformation", function(this,...) {
		lazy(this$.optionData,
			this$.optionData <- FXOptionRollInfo$getLastEquityAndRollInfo(currencyPair=this$.FXCurr,tenor="6m",putCall=this$.putCall,obsDate=this$.yesterday,tsdb=TimeSeriesDB(),rebalPeriod="2w",rebalDelta=0.15))
		this$.rollInfo <- data.frame(as.numeric(this$.optionData$lastRollDate),as.numeric(this$.optionData$strike),
				as.numeric(this$.optionData$expiryDate),as.numeric(this$.optionData$settleDate))
		colnames(this$.rollInfo) <- c("rollDate","strike","expiryDate","settleDate")
})

method("getConversionRateClose","FXCarryTriTransformation", function(this,...)
{
	this$.conversionRateClose <- 1
	if (!is.null(this$.conversionCurrency)) {
		this$.conversionRateClose <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(this$.conversionCurrency$over(),this$.conversionCurrency$under(),"_spot_rate_mid"),data.source = 'internal',this$.yesterday, this$.yesterday))
		if (this$.conversionCurrency$over()=="usd") this$.conversionRateClose <- (1/this$.conversionRateClose) 
	}
})

method("getPayoutRatio", "FXCarryTriTransformation", function(this,...)
	{
		lazy(
		this$.payoutRatio,
		this$.payoutRatio <- as.numeric(TimeSeriesDB()$retrieveOneTimeSeriesByName(squish(this$.FXCurr$over(),this$.FXCurr$under(),"_",this$.tenor,"_",this$.putCall,"_payoutratio_mid"),data.source = 'internal',this$.yesterday,this$.yesterday)),
	log=FALSE)
	})

method(".inputSeries", "FXCarryTriTransformation", function(this, ...) {
    if (is.null(this$.conversionCurrency)) return(list(rate = SeriesDefinition("MARKETDATA",squish("FX.",toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under()),".Rate.Spot"), "LastPrice")))
	return(list(rate = SeriesDefinition("MARKETDATA",squish("FX.",toupper(this$.FXCurr$over()),toupper(this$.FXCurr$under()),".Rate.Spot"), "LastPrice"),
		conversionRate = SeriesDefinition("MARKETDATA",squish("FX.",toupper(this$.conversionCurrency$over()),toupper(this$.conversionCurrency$under()),".Rate.Spot"), "LastPrice")))            
})

method(".outputSeries", "FXCarryTriTransformation", function(this, ...) {
    defTri <- function(field) SeriesDefinition("FXCARRY",this$.triMarketName, field)
    defPayoutRatio <- function(field) SeriesDefinition("FXCARRY", this$.payoutRatioMarketName, field)
	list(
		LastPrice = defTri("LastPrice"),
		OpenPrice = defTri("OpenPrice"),
		HighPrice = defTri("HighPrice"),
		LowPrice = defTri("LowPrice"),
		LastVolume = defTri("LastVolume"),
		Timestamp = defTri("Timestamp"),
		LastPayoutRatio = defPayoutRatio("LastPayoutRatio"),
		PayoutRatioTimestamp = defPayoutRatio("PayoutRatioTimestamp")
    )
})

method("initialize", "FXCarryTriTransformation", function(this, ...) {
    if(NROW(this$getCloseData()) == 0 || NROW(this$getPayoutRatio()) == 0 || NROW(this$getOptionData())==0
		|| NROW(this$getConversionRateClose()==0)) return("Missing close")
	this$.firstOfDay <- TRUE
    "SUCCESS"
})

method("skipUpdate", "FXCarryTriTransformation", function(this, inputs, ...) {	
	FALSE
})

method("outputValues", "FXCarryTriTransformation", function(this, inputs, ...) {	
	
	if (!(this$.firstOfDay)) {
		if (is.null(this$.conversionCurrency)) conversionRate <- 1
		else {
			if (this$.conversionCurrency$over()=="usd") conversionRate <- 1/(this$value(inputs,"conversionRate"))
			else conversionRate <- this$value(inputs,"conversionRate")
		}
		spotDiff <- this$value(inputs,"rate") - as.numeric(coredata(this$.data$.rateData[1,"spot"]))
		coredata(this$.data$.rateData[2,"spot"]) <- coredata(this$.data$.rateData[1,"spot"]) + spotDiff
		coredata(this$.data$.rateData[2,"6m"]) <- coredata(this$.data$.rateData[1,"6m"]) + spotDiff
		coredata(this$.data$.rateData[2,"3m"]) <- coredata(this$.data$.rateData[1,"3m"]) + spotDiff 
		fxhisttri <- FXEuropeanOptionTri(FXOptionObj=this$.option, fxNotional=this$.FXNotional, startDate=this$.yesterday, endDate=this$.date, tsdb=TimeSeriesDB(), 
			DataLoad=this$.data, overUnder="over",initialEquity=as.numeric(this$.optionData["tri"]), lastRollInfo=this$.rollInfo)
		fxhisttri$computeEquityCurve(rebalPeriod="2w",rebalDelta=0.15)
		pnl <- fxhisttri$shared()$getCurrPnL()  
		this$.notInitialized <- FALSE
		calc <- as.numeric(pnl[2])*conversionRate
		lastTri <- as.numeric(this$.optionData["tri"])*this$.conversionRateClose
		res <- calc
		list(
        	this$outputs()$LastPrice$valueString(res),
        	this$outputs()$OpenPrice$valueString(lastTri),
			this$outputs()$HighPrice$valueString(max(lastTri,res)),
			this$outputs()$LowPrice$valueString(min(lastTri,res)),
			this$outputs()$LastVolume$valueString(1),
			this$outputs()$Timestamp$now(),
			this$outputs()$LastPayoutRatio$valueString(this$getPayoutRatio()),
			this$outputs()$PayoutRatioTimestamp$now()
    	)
	}
	else {
		this$.firstOfDay <- FALSE
		res <- as.numeric(this$.optionData["tri"])*this$.conversionRateClose
		list (
			this$outputs()$LastPrice$valueString(res),
			this$outputs()$OpenPrice$valueString(res),
			this$outputs()$HighPrice$valueString(res),
			this$outputs()$LowPrice$valueString(res),
			this$outputs()$LastVolume$valueString(1),
			this$outputs()$Timestamp$now(),
			this$outputs()$LastPayoutRatio$valueString(this$getPayoutRatio()),
			this$outputs()$PayoutRatioTimestamp$now()
		)
	}
})