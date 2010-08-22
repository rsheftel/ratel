
constructor("CouponSwapTransformation", function(program=NULL, coupons.all=NULL, coupons.live=NULL,	dv01.source='internal', modelVersion=NULL, ...) {
    this <- extend(Transformation(), "CouponSwapTransformation", .program=program, .coupons.all=coupons.all, .coupons.live=coupons.live, 
																		.dv01.source=dv01.source, .modelVersion=modelVersion)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, program = "character", coupons.all = 'numeric', coupons.live='numeric', dv01.source='character', modelVersion='character')
	this$.coupon.vector <- format(coupons.all,nsmall=1)
	this$.modelSetup()
	this$setDates(as.POSIXct(strptime(Sys.time(),'%Y-%m-%d')))
	this$.testMode = FALSE
    return(this)
})

method('.modelSetup', 'CouponSwapTransformation', function(this, ...){
	this$.modelName <- squish('qfmodel_couponSwapModel_',this$.modelVersion)
	this$.settle.vector <- c('1n','2n','3n')
	this$.forwardDays <- 45
	if (this$.modelVersion=='1.0'){ this$.knotPoints <- seq(-3,3,1)}
	if (this$.modelVersion=='1.5'){ this$.knotPoints <- seq(-3,3,0.5)}
})

method(".outputSeries", "CouponSwapTransformation", function(this, ...) {
	coupon.swap.tickers <- this$couponSwapTickers()
	
	ticker.actual <- lapply(coupon.swap.tickers, function(ticker) SeriesDefinition("CouponSwap",ticker,squish(this$.forwardDays,"d_price_actual")))
	ticker.model  <- lapply(coupon.swap.tickers, function(ticker) SeriesDefinition("CouponSwap",ticker,squish(this$.forwardDays,"d_price_model")))
	ticker.roll   <- lapply(coupon.swap.tickers, function(ticker) SeriesDefinition("CouponSwap",ticker,"1n_weighted_roll"))
	ticker.timestamp <- lapply(coupon.swap.tickers, function(ticker) SeriesDefinition("CouponSwap",ticker,"Timestamp"))
	
	return(c(ticker.actual, ticker.model, ticker.roll, ticker.timestamp))
})

method("couponSwapTickers", "CouponSwapTransformation", function(this, ...){
	coupons.low <- this$.coupon.vector[1:length(this$.coupon.vector)-1]
	coupons.high<- this$.coupon.vector[-1]
	return(paste(this$.program,coupons.high,coupons.low,sep="_"))
})

method(".inputSeries", "CouponSwapTransformation", function(this, ...) {
	swap.rates <- list(
		swap.rate.2y = SeriesDefinition("MARKETDATA", "irs_usd_rate_2y","LastPrice"),
		swap.rate.10y =SeriesDefinition("MARKETDATA", "irs_usd_rate_10y","LastPrice")
	)
	
	tba.price.tickers <- sapply(this$.settle.vector, function(settle) paste(this$.program, format(this$.coupons.live,nsmall=1),settle,'price.internal',sep="_"))
	tba.settle.tickers <- sapply(this$.settle.vector, function(settle) paste(this$.program, format(this$.coupons.live,nsmall=1),settle,'settle_date.internal',sep="_"))
	
	tba.prices <- lapply(tba.price.tickers, function(ticker) SeriesDefinition("TBA",ticker,"LastValue"))
	tba.settles <- lapply(tba.settle.tickers, function(ticker) SeriesDefinition("TBA",ticker,"LastValue"))
	this$.tba.timeStamps <- lapply(tba.price.tickers, function(ticker) SeriesDefinition("TBA",ticker,"timeStamp"))
	
	return(c(swap.rates, tba.prices, tba.settles, this$.tba.timeStamps))
})

######################################################################################################
#			Standard Methods
######################################################################################################

method("initialize", "CouponSwapTransformation", function(this, ...) {
 	
	this$.couponSwap <- TBACouponSwap(program=this$.program, couponVector=as.numeric(this$.coupon.vector), forwardDays=this$.forwardDays,
										modelName=this$.modelName)
	this$.couponSwap$setStartEndDates(startDate=this$.startDate, endDate=this$.endDate)
	this$setParameters()
	this$loadHistoricalData()
	this$fitModel()
	if (!this$.testMode) this$validateInitialize()
	this$addNewDate(this$.liveDate)
	this$.firstOfDay = TRUE
	this$.lastUpdateTime = Sys.time()
	return("SUCCESS")
})

method("skipUpdate", "CouponSwapTransformation", function(this, inputs, ...) {
	if(this$.firstOfDay) return(FALSE)
	return(any(sapply(this$.tba.timeStamps, function(sd) inputs$fetch(sd)$value() < this$.lastUpdateTime)))
})

method("outputValues", "CouponSwapTransformation", function(this, inputs, ...) {
	
	if(!this$.firstOfDay){ 	#In here use the FirstOfDay to set first tick for the day
		
		# Get the raw data from the inputs
		priceMatrix <- this$matrixFromInputs(inputs, 'price.internal')
		settleDatesMatrix <- this$matrixFromInputs(inputs, 'settle_date.internal')
		swap.rate.2y = inputs$fetch(SeriesDefinition("MARKETDATA", "irs_usd_rate_2y","LastPrice"))$value()
		swap.rate.10y = inputs$fetch(SeriesDefinition("MARKETDATA", "irs_usd_rate_10y","LastPrice"))$value()
		slope <- swap.rate.10y - swap.rate.2y
		
		#Calculate the CC and forward prices
		tbaGrid <- TBAGrid(this$.program,this$.coupons.live, this$.liveDate)
		tbaGrid$setPricesFromMatrix(priceMatrix,this$.coupons.live,this$.settle.vector)
		tbaGrid$setSettleDatesFromMatrix(settleDatesMatrix,this$.settle.vector)
		cc <- tbaGrid$getCurrentCoupon()
		forwardPrices <- tbaGrid$getNDayForwardPrice(this$.forwardDays)
		
		#Update CpnSwap Object
		this$.couponSwap$updateLastTBAPrices(forwardPrices, this$.coupons.live)
		this$.couponSwap$updateLastCurrentCoupon(cc)
		this$.couponSwap$updateLastSwapRates(swap.rate.2y, swap.rate.10y)
		rollVector <- priceMatrix[,match('1n',this$.settle.vector)] - priceMatrix[,match('2n',this$.settle.vector)]
		goodCols <- match(this$.coupons.live,this$.coupons.live)[!is.na(match(this$.coupons.live,this$.coupons.live))]
		this$.couponSwap$setRollData(rollVector[goodCols], this$.coupons.live[goodCols])
	}
	else {
		cc <- this$.couponSwap$getLastCurrentCoupon()
		slope <- this$.couponSwap$getLastSlope()
		this$.firstOfDay <- FALSE
	}
		
	#calculate and publish the outputs
	this$fitModel()
	output.actual <- this$publishOutput(squish(this$.forwardDays,"d_price_actual"),this$getActualValues())
	output.model  <- this$publishOutput(squish(this$.forwardDays,"d_price_model"),this$getModelValues(cc, slope))
	output.roll   <- this$publishOutput("1n_weighted_roll",this$getModelWeightedRolls(cc, slope))
	
	tickers <- this$couponSwapTickers()
	output.timestamp <- lapply(tickers, function(ticker) SeriesDefinition("CouponSwap",ticker,"Timestamp")$now())
	
	this$.lastUpdateTime = Sys.time()
	return(c(output.actual, output.model, output.roll, output.timestamp))
})

######################################################################################################
#			Output helper methods
######################################################################################################

method("publishOutput", "CouponSwapTransformation", function(this, fieldName, outputValues,...){
	tickers <- this$couponSwapTickers()
	outputList <- vector("list", length(outputValues)) 
	for (count in seq_along(outputValues)){
		outputList[[count]] <- SeriesDefinition("CouponSwap",tickers[count],fieldName)$valueString(outputValues[count])
	}
	return(outputList)
})

method("matrixFromInputs", "CouponSwapTransformation", function(this, inputs, inputTag, ...){
	priceMatrix <- matrix(NA,length(this$.coupons.live),length(this$.settle.vector))
	for (coupon in seq_along(this$.coupons.live)){
		for (settle in seq_along(this$.settle.vector)){
			sd <- SeriesDefinition("TBA",paste(this$.program, format(this$.coupons.live[coupon],nsmall=1), this$.settle.vector[settle],inputTag,sep="_"),"LastValue")
			priceMatrix[coupon,settle] <- inputs$fetch(sd)$value() 
		}
	}
	return(priceMatrix) 
})

######################################################################################################
#			Access to the TBACouponSwap object
######################################################################################################

method("setDates", "CouponSwapTransformation", function(this, liveDate){
	this$.liveDate <- liveDate
	this$.endDate <- businessDaysAgo(1,liveDate,'nyb')
	this$.startDate <- seq(this$.endDate,length=2,by='-1000 DSTday')[2]		
})

method("setParameters", "CouponSwapTransformation", function(this, ...){
	this$.couponSwap$setKnotPoints(knotPoints=this$.knotPoints)
	this$.couponSwap$setStartEndDatesForCoupons()
})

method("loadHistoricalData", "CouponSwapTransformation", function(this, ...){
	this$.couponSwap$setRawDataFromTSDB()
	this$.couponSwap$setCouponDv01FromTsdb(couponDv01.source=this$.dv01.source)
	this$.couponSwap$setRollDataFromTsdb()
})

method("fitModel", "CouponSwapTransformation", function(this, ...){
	this$.couponSwap$prepareAndFitModel()
})

method("validateInitialize", "CouponSwapTransformation", function(this, ...){
	currentCoupon <- last(this$.couponSwap$.currentCouponZoo)[[1]]
	slope <- last(this$.couponSwap$.swapRate10yZoo)[[1]] -last(this$.couponSwap$.swapRate2yZoo)[[1]]
	this$compareVersusTsdb(squish(this$.forwardDays,"d_price"),"internal", this$.coupon.vector, this$getActualValues(currentCoupon, slope))
	this$compareVersusTsdb(squish(this$.forwardDays,"d_price"),this$.modelName, this$.coupon.vector, this$getModelValues(currentCoupon, slope))
	this$compareVersusTsdb("1n_weighted_roll",this$.dv01.source, this$.coupon.vector, this$getModelWeightedRolls(currentCoupon, slope))
})

method("compareVersusTsdb", "CouponSwapTransformation", function(this, attribute, source, coupons, objectValues, ...){
	for (coupon in 1:(length(coupons)-1)){
		coupon.high <- coupons[coupon+1]
		coupon.low  <- coupons[coupon]
		ticker <- paste(this$.program,coupon.high,coupon.low,sep="_", attribute)
		print(squish("Checking v TSDB: ",ticker, ' : source: ',source))
		tsdbValue <- TimeSeriesDB$retrieveOneTimeSeriesByName(ticker, source, this$.endDate)
		if (length(tsdbValue)==1){
			if (round(tsdbValue[[1]],10)==round(objectValues[coupon],10)){
				print (squish("Valid: TSDB: ",tsdbValue[[1]],"  Object: ",objectValues[coupon]))
			}
			else fail(squish("Error, does not match!  TSDB: ",tsdbValue[[1]],"  Object: ",objectValues[coupon]))
		}
		else print("No data in TSDB")
	}				
})

method("getModelValues", "CouponSwapTransformation", function(this, currentCoupon, slope, ...){
	return(this$.couponSwap$getModelValues(as.numeric(this$.coupon.vector[2:length(this$.coupon.vector)]), currentCoupon, slope))
})

method("getModelWeightedRolls", "CouponSwapTransformation", function(this, currentCoupon, slope, ...){
	return(this$.couponSwap$getModelWeightedRolls(as.numeric(this$.coupon.vector[2:length(this$.coupon.vector)]), 
													currentCoupon, slope, method='dv01.source'))
})

method("getActualValues", "CouponSwapTransformation", function(this, ...){
	return(this$.couponSwap$getActualValues(as.numeric(this$.coupon.vector[2:length(this$.coupon.vector)])))		
})

method("addNewDate", "CouponSwapTransformation", function(this, newDate, ...){
	newDate <- as.POSIXlt(this$.liveDate)
	newDate$hour <- 15
	this$.couponSwap$addNewDate(as.POSIXct(newDate))
	this$fitModel()
})
