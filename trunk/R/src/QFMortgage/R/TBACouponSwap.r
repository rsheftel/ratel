#########################################################################################################
#                               Base Class definitions                                                  #
#########################################################################################################

constructor("TBACouponSwap", function(program = NULL, couponVector = NULL, forwardDays = NULL, modelName="qfmodel_couponSwapModel_1.0"){
    this <- extend(RObject(), "TBACouponSwap", .program = program, .couponVector = couponVector, .forwardDays = forwardDays, .modelName=modelName)
    if (inStaticConstructor(this)) return(this)
    constructorNeeds (this, program = "character", couponVector = "numeric", forwardDays = "numeric")

    #Set up initial variable
    this$.couponStringVector = format(this$.couponVector,nsmall=1)
    this$.settleString = paste(forwardDays,'d',sep="")
    this$.source = "internal"
    
    #Default start and end dates, knots and coupon start end
    this$setStartEndDates(as.POSIXct('1900-01-01'), as.POSIXct('2100-01-01'))
    this$setKnotPoints()
    this$setStartEndDatesForCoupons()

    return(this)
})

method("setStartEndDates", "TBACouponSwap", function(this, startDate, endDate,...){
    needs(startDate = "POSIXct|character", endDate = "POSIXct|character")
    this$.startDate <- as.POSIXct(startDate)
    this$.endDate <- as.POSIXct(endDate)
})

method("setRawDataFromTSDB", "TBACouponSwap", function(this,...){
    #Get all of the raw data from TSDB: raw prices, current coupon, and swap rates. These are zoos
    tsdb <- TimeSeriesDB()

    #retreive priceZoo
    tickerPrice <- paste(this$.program,this$.couponStringVector,paste(this$.forwardDays,'d',sep=""),'price',sep="_")
    priceZoos <- tsdb$retrieveTimeSeriesByName(tickerPrice,this$.source, this$.startDate, this$.endDate)
    this$.priceZoos <- TSDataLoader$matrixToZoo(priceZoos)

    #retrieve current coupon
    tickerCurrentCoupon <- paste(this$.program,'cc','30d','yield',sep="_")
    this$.currentCouponZoo <- tsdb$retrieveOneTimeSeriesByName(tickerCurrentCoupon, this$.source, this$.startDate, this$.endDate)
    
    #retrieve 2y / 10y swap rates
    tickerSwapRate <- 'irs_usd_rate'
    this$.swapRate2yZoo  <- tsdb$retrieveOneTimeSeriesByName(paste(tickerSwapRate,'2y','mid',sep="_"), this$.source, this$.startDate, this$.endDate)
    this$.swapRate10yZoo <- tsdb$retrieveOneTimeSeriesByName(paste(tickerSwapRate,'10y','mid',sep="_"), this$.source, this$.startDate, this$.endDate)
})

method("setCouponDv01FromTsdb", "TBACouponSwap", function(this, couponDv01.source='internal',...){
    needs (couponDv01.source='character?')
    tsdb <- TimeSeriesDB()
        
    couponDv01.tickers <- paste(this$.program,this$.couponStringVector,'1n','dv01',sep="_")

    this$.couponDv01 <- rep(NA, length(couponDv01.tickers))
        
    for (couponCount in 1:length(couponDv01.tickers)){
        couponDv01 <- tsdb$retrieveTimeSeriesByName(couponDv01.tickers[couponCount],couponDv01.source,this$.endDate,this$.endDate)
        if (!NROW(couponDv01)==0)
            this$.couponDv01[couponCount] <- as.numeric(couponDv01[[1]][[1]])
    }    
})

method("setRollDataFromTsdb", "TBACouponSwap", function(this,...){
	tsdb <- TimeSeriesDB()
	
	price.1n.tickers <- paste(this$.program,this$.couponStringVector,'1n','price',sep="_")
	price.2n.tickers <- paste(this$.program,this$.couponStringVector,'2n','price',sep="_")
	
	price.1n.vector <- rep(NA, length(this$.couponStringVector))
	price.2n.vector <- rep(NA, length(this$.couponStringVector))
	
	for (couponCount in 1:length(price.1n.tickers)){
		price.1n <- tsdb$retrieveOneTimeSeriesByName(price.1n.tickers[couponCount],this$.source,this$.endDate,this$.endDate)
		price.2n <- tsdb$retrieveOneTimeSeriesByName(price.2n.tickers[couponCount],this$.source,this$.endDate,this$.endDate)
		if (!NROW(price.1n)==0)
			price.1n.vector[couponCount] <- price.1n
		if (!NROW(price.2n)==0)
			price.2n.vector[couponCount] <- price.2n
	}
	
	this$.rolls <- price.1n.vector - price.2n.vector
})

method("generateActualData", "TBACouponSwap", function(this,...){
#Generate the actual data matrix to be used.

    couponVector.length <- length(this$.couponVector)

    #date vector from prices
    this$.dateTimes.vector <- index(this$.priceZoos)
    this$.dates.vector <- as.POSIXct(format(this$.dateTimes.vector,'%Y-%m-%d'))
    dates.zoo <- zoo(NA, this$.dateTimes.vector)
    this$.daysAgo <- rev(1:length(this$.dateTimes.vector) -1)

    #Price matrix (may not be needed again)
    tickerPrice <- paste(this$.program,this$.couponStringVector,paste(this$.forwardDays,'d',sep=""),'price',sep="_")
    this$.price.matrix <- matrix(NA, nrow = length(this$.dateTimes.vector), ncol = length(this$.couponVector))
    priceZoos.colnames <- colnames(this$.priceZoos)
    for (couponCount in 1:couponVector.length){
        couponColumn <- priceZoos.colnames == tickerPrice[couponCount]
        if(any(couponColumn)) this$.price.matrix[,couponCount] <- this$.priceZoos[,couponColumn]
    }

    #Coupon Swap Actual
    this$.couponSwap.actual.matrix <- this$.price.matrix[,2:couponVector.length] - this$.price.matrix[,1:(couponVector.length-1)]
    
    #Current Coupon Vector
    currentCoupon.vector <- merge(this$.currentCouponZoo, dates.zoo, all=FALSE)
    currentCoupon.vector <- merge(currentCoupon.vector, dates.zoo, all=TRUE)
    this$.currentCoupon.vector <- as.vector(currentCoupon.vector[,1])
    
    #itmHigh
    couponHigh.matrix <- matrix(rep(this$.couponVector[2:couponVector.length],length(this$.dateTimes.vector)),nrow=length(this$.dateTimes.vector),byrow=TRUE)
    currentCoupon.matrix <- matrix(rep(this$.currentCoupon.vector,couponVector.length-1),ncol=couponVector.length-1,byrow=FALSE)
    this$.itmHigh.matrix <- couponHigh.matrix - currentCoupon.matrix

    #slope vector
    swapRate2y <- merge(this$.swapRate2yZoo, dates.zoo, all=FALSE)
    swapRate2y <- merge(swapRate2y, dates.zoo, all=TRUE)
    swapRate2y.vector <- as.vector(swapRate2y[,1])

    swapRate10y <- merge(this$.swapRate10yZoo, dates.zoo, all=FALSE)
    swapRate10y <- merge(swapRate10y, dates.zoo, all=TRUE)
    swapRate10y.vector <- as.vector(swapRate10y[,1])

    this$.slope.vector <- swapRate10y.vector - swapRate2y.vector
})

method("setKnotPoints", "TBACouponSwap", function(this,knotPoints=c(-3:3),...){
#Set up the know points
    this$.knotPoints <- knotPoints
    this$.knotLen <- length(this$.knotPoints)
})

method("setStartEndDatesForCoupons", "TBACouponSwap", function(this, startDates=NULL, endDates=NULL,...){
    needs(startDates="POSIXct|character?", endDates="POSIXct|character?")
	
	startDates <- TBA$startEndDatesForCoupons(this$.program, this$.couponVector, startDates=NULL, endDates=NULL)$startDates
	endDates   <- TBA$startEndDatesForCoupons(this$.program, this$.couponVector, startDates=NULL, endDates=NULL)$endDates
	
	startDates <- pmax(startDates[1:(length(startDates)-1)],startDates[2:length(startDates)])
	endDates <- pmin(endDates[1:(length(endDates)-1)],endDates[2:length(endDates)])

    this$.couponSwap.startDates <- startDates
    this$.couponSwap.endDates <- endDates
})

method("generateWeights", "TBACouponSwap", function(this, halfLife=120, maxPeriods=504, ...){
#Generate the weight matrix
    needs(halfLife="numeric", maxPeriods="numeric")
    
    lambda <- 0.5^(1/halfLife)
    weights.time        <- lambda^this$.daysAgo
    weights.maxPeriods  <- ifelse(this$.daysAgo < maxPeriods,1,0)

    weights.startDate   <- do.call(cbind,lapply(this$.couponSwap.startDates, function(d) this$.dates.vector >= d))
    weights.endDate     <- do.call(cbind,lapply(this$.couponSwap.endDates, function(d) this$.dates.vector <= d))
    weights.dates.matrix <- ifelse(weights.startDate & weights.endDate,1,0)

    weights.dates <- weights.time * weights.maxPeriods
    this$.weights.matrix <- weights.dates * weights.dates.matrix
})

method("vectorizeMatrix", "TBACouponSwap", function(this,...){
#Vectorize the matrixs for use in the fitting function

    this$.couponSwap.actual.vector <- as.vector(unlist(this$.couponSwap.actual.matrix))
    this$.itmHigh.vector <- as.vector(unlist(this$.itmHigh.matrix))
    this$.currentCoupon.repeatVector <- rep(this$.currentCoupon.vector,length(this$.couponVector)-1)
    this$.slope.repeatVector <- rep(this$.slope.vector,length(this$.couponVector)-1)
    this$.weights.vector <- as.vector(unlist(this$.weights.matrix))
})

method("filterVectors", "TBACouponSwap", function(this,...){
#Filter down the vectors to only the good points, meaning !NA and weight > 0

    goodRows <- !is.na(this$.couponSwap.actual.vector)
    goodRows <- goodRows & !is.na(this$.itmHigh.vector)
    goodRows <- goodRows & !is.na(this$.currentCoupon.repeatVector)
    goodRows <- goodRows & !is.na(this$.slope.repeatVector)
    
    goodRows <- goodRows & !is.na(this$.weights.vector)
    goodRows <- goodRows & (this$.weights.vector > 0)
    
    this$.couponSwap.actual.vector  <- this$.couponSwap.actual.vector[goodRows]
    this$.itmHigh.vector            <- this$.itmHigh.vector[goodRows]
    this$.currentCoupon.repeatVector <- this$.currentCoupon.repeatVector[goodRows]
    this$.slope.repeatVector        <- this$.slope.repeatVector[goodRows]
    this$.weights.vector            <- this$.weights.vector[goodRows]
})

method("fitModel", "TBACouponSwap", function(this,...){

    knotLen <- length(this$.knotPoints)
    #Create the basis matrix

    ##Use an new name for itmHigh.marix, like itmHigh.vector.repeat or some other temp hold
    itmHigh.matrix <- matrix(rep(this$.itmHigh.vector,knotLen),ncol=knotLen)
    itmHigh.vector.length <- length(this$.itmHigh.vector)
    knotPoints.matrix <- matrix(rep(this$.knotPoints,itmHigh.vector.length),nrow=itmHigh.vector.length,byrow=TRUE)
    basis.matrix <- pmax((itmHigh.matrix - knotPoints.matrix),0)

    #Create the itmHigh regressor data frame
    itmHigh.regressors <- data.frame(basis.matrix)
    itmHigh.varNames <- paste('itmHigh.',1:knotLen,sep="")
    colnames(itmHigh.regressors) <- itmHigh.varNames

    #Create the slope regressor data frame
    slope.matrix <- matrix(rep(this$.slope.repeatVector,knotLen),ncol=knotLen)
    slope.regressors <- data.frame(slope.matrix * basis.matrix)
    slope.varNames <- paste('slope.',1:knotLen,sep="")
    colnames(slope.regressors) <- slope.varNames

    #Set up the lm model
    data.df <- data.frame(cpnSwap=this$.couponSwap.actual.vector)
    data.df <- cbind(data.df,itmHigh.regressors,slope.regressors)
    formulaText <- paste(c('cpnSwap',paste(itmHigh.varNames,collapse=" + ")),collapse=" ~ ")
    formulaText <- as.formula(paste(c(formulaText,paste(slope.varNames,collapse=" + ")),collapse=" + "))

    #Solve the model
    couponSwapModel <- lm(formulaText,data=data.df,weights=this$.weights.vector)
	this$.couponSwapModel.lm <- couponSwapModel
	
    #Populate the coefficients for later use
    this$.couponSwapModel.const <- as.vector(couponSwapModel$coefficients['(Intercept)'])

    this$.couponSwapModel.betas.itmHigh <- as.vector(couponSwapModel$coefficients[itmHigh.varNames])
    this$.couponSwapModel.betas.itmHigh[is.na(this$.couponSwapModel.betas.itmHigh)] <- 0

    this$.couponSwapModel.betas.slope <- as.vector(couponSwapModel$coefficients[slope.varNames])
    this$.couponSwapModel.betas.slope[is.na(this$.couponSwapModel.betas.slope)] <- 0
})

method("getOneModelValue", "TBACouponSwap", function(this, couponHigh, currentCoupon, slope, ...){
#Given market inputs, return the model value from the fitted model
#This works for single values only
    needs (couponHigh='numeric',currentCoupon='numeric',slope='numeric')
    itmHigh <- couponHigh - currentCoupon
    basis.vector <- pmax((itmHigh - this$.knotPoints),0)
    slopeRegressors <- (basis.vector * slope)
    couponSwap.model <- this$.couponSwapModel.const + (this$.couponSwapModel.betas.itmHigh %*% basis.vector) + (this$.couponSwapModel.betas.slope %*% slopeRegressors)
    return(couponSwap.model)
})

method("getModelValues", "TBACouponSwap", function(this, couponHigh, currentCoupon, slope, ...){
#Given market inputs, return the model value from the fitted model
    needs (couponHigh='numeric',currentCoupon='numeric',slope='numeric')
    return(sapply(couponHigh, function(cpn) this$getOneModelValue(cpn, currentCoupon, slope)))
})

method("getActualValues", "TBACouponSwap", function(this, couponHigh, ...){
	couponCols <- match(couponHigh, this$.couponVector[2:length(this$.couponVector)])
	return(this$.couponSwap.actual.matrix[dim(this$.couponSwap.actual.matrix)[1],couponCols])
})
	
method("getModelDurations", "TBACouponSwap", function(this, couponHigh, currentCoupon, slope, bump=0.10, ...){
#The bump is the amount to shift CC to calculate the duration
    needs (couponHigh='numeric',currentCoupon='numeric',slope='numeric',bump='numeric?')
    couponSwap.up = sapply(couponHigh, function(cpn) this$getOneModelValue(cpn, currentCoupon + bump, slope))
    couponSwap.down = sapply(couponHigh, function(cpn) this$getOneModelValue(cpn, currentCoupon - bump, slope))
    return((couponSwap.down - couponSwap.up)/(bump*2))
})

method("getModelHedgeRatios", "TBACouponSwap", function(this, couponHigh, currentCoupon, slope, bump=0.10, method='model', ...){
#The hedge ratio of the % of the couponLow to hedge for 1 unit of couponHigh
    needs (couponHigh='numeric',currentCoupon='numeric',slope='numeric',bump='numeric?')
    couponSwap.dv01 <- this$getModelDurations(couponHigh, currentCoupon, slope, bump=0.10)
    couponLow <- couponHigh - (this$.couponVector[2] - this$.couponVector[1])
    couponLow.dv01 <- this$.couponDv01[match(couponLow,this$.couponVector)]

	if (method=='model'){
	    couponSwap.hedgeRatio <- (couponLow.dv01 + couponSwap.dv01) / couponLow.dv01
	}else if (method=='dv01.source'){
		couponHigh.dv01 <- this$.couponDv01[match(couponHigh,this$.couponVector)]
		couponSwap.hedgeRatio <- couponHigh.dv01 / couponLow.dv01
	}else couponSwap.hedgeRatio <- NULL
    return(couponSwap.hedgeRatio)
})

method("getModelWeightedRolls", "TBACouponSwap", function(this, couponHigh, currentCoupon, slope, bump=0.10, method='model', ...){
	needs (couponHigh='numeric',currentCoupon='numeric',slope='numeric',bump='numeric?')
	
	hedgeRatios <- this$getModelHedgeRatios(couponHigh, currentCoupon, slope, bump, method)
	coupon.step <- (this$.couponVector[2] - this$.couponVector[1])
	rollHigh <- this$.rolls[match(couponHigh, this$.couponVector)]
	rollLow  <- this$.rolls[match(couponHigh - coupon.step, this$.couponVector)]
	weightedRolls <- rollHigh - (hedgeRatios * rollLow) 
	return(weightedRolls)	
})

method("prepareAndFitModel", 'TBACouponSwap', function(this, ...){
#Just a wrapper function for the fitting process
	this$generateActualData()
	this$generateWeights()
	this$vectorizeMatrix()
	this$filterVectors()
	this$fitModel()	
})

#########################################################################################################
#                                       Live running items
#########################################################################################################

method("addNewDate", "TBACouponSwap", function(this, newDate, ...){
	needs(newDate='POSIXct')
	copyForward <- function(z){
		z.last <- z[NROW(z)]
		index(z.last) <- newDate
		return(rbind(z,z.last))
	}
		
	needs(newDate = "POSIXct|character")
	newDate <- as.POSIXct(newDate)
	this$setStartEndDates(this$.startDate, newDate)
	
	this$.priceZoos 		<- copyForward(this$.priceZoos)
	this$.currentCouponZoo 	<- copyForward(this$.currentCouponZoo)
	this$.swapRate2yZoo 	<- copyForward(this$.swapRate2yZoo)
	this$.swapRate10yZoo 	<- copyForward(this$.swapRate10yZoo)
})

method("updateLastTBAPrices", "TBACouponSwap", function(this, priceVector, couponVector, ...){
	needs(priceVector='numeric', couponVector='numeric')
	
	newPrices <- rep(NA,NCOL(this$.priceZoos))
	colHeaders <- paste(this$.program,as.character(format(couponVector,nsmall=1)),paste(this$.forwardDays,'d',sep=""),'price',sep="_")
	rowMatches <- match(colHeaders, colnames(this$.priceZoos))
	priceVector <- priceVector[!is.na(rowMatches)]
	couponVector <- couponVector[!is.na(rowMatches)]
	newPrices[match(couponVector,this$.couponVector)] <- priceVector
	this$.priceZoos[NROW(this$.priceZoos),1:NCOL(this$.priceZoos)] <- newPrices 		
})

method("updateLastSwapRates", "TBACouponSwap", function(this, swapRate2y, swapRate10y, ...){
	needs(swapRate2y='numeric', swapRate10y='numeric')
	this$.swapRate2yZoo[NROW(this$.swapRate2yZoo)] <- swapRate2y
	this$.swapRate10yZoo[NROW(this$.swapRate10yZoo)] <- swapRate10y  		
})

method("updateLastCurrentCoupon", "TBACouponSwap", function(this, currentCoupon, ...){
	needs(currentCoupon='numeric')
	this$.currentCouponZoo[NROW(this$.currentCouponZoo)] <- currentCoupon  		
})

method("setRollData", "TBACouponSwap", function(this, rollVector, couponVector, ...){	
	rolls <- rep(NA, length(this$.couponVector))
	rolls[match(couponVector, this$.couponVector)] <- rollVector
	this$.rolls <- rolls
})

method("setCouponDv01s", "TBACouponSwap", function(this, couponDv01s, couponVector, ...){
	dv01s <- rep(NA, length(this$.couponVector))
	dv01s[match(couponVector, this$.couponVector)] <- couponDv01s
	this$.couponDv01 <- dv01s
})

method("getLastCurrentCoupon", "TBACouponSwap", function(this, ...){
	return(as.numeric(last(this$.currentCouponZoo)))
})

method("getLastSlope", "TBACouponSwap", function(this, ...){
	return(as.numeric(last(this$.swapRate10yZoo) - last(this$.swapRate2yZoo)))
})

#########################################################################################################
#                                       Daily run items
#########################################################################################################


method("writeTSDBuploadFile", "TBACouponSwap", function(this,uploadMethod='file',path=NULL,...){
    
	if (is.null(path)) path <- tsdbUploadDirectory()

    dataDate <- as.POSIXlt(this$.endDate)
    dataDate$hour <- 15
    dataDateTime <- as.POSIXct(dataDate)

    tickerHeader <- paste(this$.program, this$.couponStringVector[2:length(this$.couponStringVector)],this$.couponStringVector[1:length(this$.couponStringVector)-1],sep="_")

    #Make sure the last date in the matrix is the requested end date
    if(last(this$.dates.vector) == this$.endDate)
    {
        #Actual coupon swap values
        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_price",sep="")
        tsdbValues <- zoo(t(this$.couponSwap.actual.matrix[dim(this$.couponSwap.actual.matrix)[1],]), dataDateTime)
		uploadZooToTsdb(tsdbValues,tickerName,this$.source,uploadMethod=uploadMethod,uploadFilename=squish('TBA_CouponSwap_Price_Actual_',this$.program,'_',format(dataDateTime,'%Y%m%d')),uploadPath=path)
			
        #Model coupon swap values
        endCC <- as.numeric(this$.currentCouponZoo[dataDateTime])
        endSlope <- as.numeric(this$.swapRate10yZoo[dataDateTime] - this$.swapRate2yZoo[dataDateTime])
        
        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_price",sep="")
        tsdbValues <- zoo(t(this$getModelValues(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)), dataDateTime)
    	uploadZooToTsdb(tsdbValues, tickerName, this$.modelName, uploadMethod=uploadMethod, uploadFilename=squish('TBA_CouponSwap_Price_Model_',this$.program,'_',format(dataDateTime,'%Y%m%d')),uploadPath=path)    
	
        #Model coupon swap durations (dv01)
        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_dv01",sep="")
        tsdbValues <- zoo(t(this$getModelDurations(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)), dataDateTime)
		uploadZooToTsdb(tsdbValues, tickerName, this$.modelName, uploadMethod=uploadMethod, uploadFilename=squish('TBA_CouponSwap_DV01_Model_',this$.program,'_',format(dataDateTime,'%Y%m%d')),uploadPath=path)
	
        #Model coupon swap hedge ratios
        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_hedge_ratio",sep="")
        tsdbValues <- zoo(t(this$getModelHedgeRatios(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)), dataDateTime)
		uploadZooToTsdb(tsdbValues, tickerName, this$.modelName, uploadMethod=uploadMethod, uploadFilename=squish('TBA_CouponSwap_HedgeRatio_Model_',this$.program,'_',format(dataDateTime,'%Y%m%d')), uploadPath=path)
		
		#	Weighted Roll funtionality moved to TBATRICouponSwap.R class	
		#Model coupon weighted rolls
		#tickerName <- paste(tickerHeader,"1n","weighted_roll",sep="_")
		#tsdbValues <- zoo(t(this$getModelWeightedRolls(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)), dataDateTime)
		#uploadZooToTsdb(tsdbValues, tickerName, this$.modelName, uploadMethod=uploadMethod, uploadFilename=squish('TBA_CouponSwap_WgtRoll_Model_',this$.program,'_',format(dataDateTime,'%Y%m%d')), uploadPath=path)
    }
})


#########################################################################################################
#                                       Batch history cache items
#########################################################################################################


method("setHistoryCacheFromTSDB", "TBACouponSwap", function(this, couponDv01.source='internal', ...){
    #Get all of the raw data from TSDB: raw prices, current coupon, and swap rates. These are zoos
    #Store in a cache for a bulk backfill
    tsdb <- TimeSeriesDB()

    #retreive priceZoo
    tickerPrice <- paste(this$.program,this$.couponStringVector,paste(this$.forwardDays,'d',sep=""),'price',sep="_")
    priceZoos <- tsdb$retrieveTimeSeriesByName(tickerPrice,this$.source)
    this$.cache.priceZoos <- TSDataLoader$matrixToZoo(priceZoos)

    #retrieve current coupon
    tickerCurrentCoupon <- paste(this$.program,'cc','30d','yield',sep="_")
    this$.cache.currentCouponZoo <- tsdb$retrieveOneTimeSeriesByName(tickerCurrentCoupon, this$.source)

    #retrieve 2y / 10y swap rates
    tickerSwapRate <- 'irs_usd_rate'
    this$.cache.swapRate2yZoo  <- tsdb$retrieveOneTimeSeriesByName(paste(tickerSwapRate,'2y','mid',sep="_"), this$.source)
    this$.cache.swapRate10yZoo <- tsdb$retrieveOneTimeSeriesByName(paste(tickerSwapRate,'10y','mid',sep="_"), this$.source)
    
    #retrieve dv01s
    tickerDv01 <- paste(this$.program,this$.couponStringVector,'1n','dv01',sep="_")
    dv01Zoos <- tsdb$retrieveTimeSeriesByName(tickerDv01,couponDv01.source)
    this$.cache.dv01Zoos <- TSDataLoader$matrixToZoo(dv01Zoos)
	
	#retrieve price.1n & price.2n
	tickerPrice1n <- paste(this$.program,this$.couponStringVector,'1n','price',sep="_")
	tickerPrice2n <- paste(this$.program,this$.couponStringVector,'2n','price',sep="_")
	price1nZoos <- tsdb$retrieveTimeSeriesByName(tickerPrice1n,this$.source)
	price2nZoos <- tsdb$retrieveTimeSeriesByName(tickerPrice2n,this$.source)
	price1nZoos <- TSDataLoader$matrixToZoo(price1nZoos)
	price2nZoos <- TSDataLoader$matrixToZoo(price2nZoos)
	this$.cache.rollZoos <- price1nZoos - price2nZoos
	
})

method("setRawDataFromCache", "TBACouponSwap", function(this,...){
    #Populate the raw data from a date range extract from the Cache. Data range set with appropriate method
    filteredZoo <- function (zooData){
        return(zooData[(index(strip.times.zoo(zooData))>=this$.startDate)&(index(strip.times.zoo(zooData))<=this$.endDate),])
    }
    this$.priceZoos <- filteredZoo(this$.cache.priceZoos)
    this$.currentCouponZoo <- filteredZoo(this$.cache.currentCouponZoo)
    this$.swapRate2yZoo <- filteredZoo(this$.cache.swapRate2yZoo)
    this$.swapRate10yZoo <- filteredZoo(this$.cache.swapRate10yZoo)
})

method("setCouponDv01FromCache", "TBACouponSwap", function(this, ...){
    this$.couponDv01 <- as.numeric(this$.cache.dv01Zoos[(index(strip.times.zoo(this$.cache.dv01Zoos))==this$.endDate),])
})

method("setRollDataFromCache", "TBACouponSwap", function(this, ...){
	this$.rolls <- as.numeric(this$.cache.rollZoos[(index(strip.times.zoo(this$.cache.rollZoos))==this$.endDate),])
})

method("calculateResultsForBackfill", "TBACouponSwap", function(this,...){
    dataDate <- as.POSIXlt(this$.endDate)
    dataDate$hour <- 15
    dataDateTime <- as.POSIXct(dataDate)

    tickerHeader <- paste(this$.program, this$.couponStringVector[2:length(this$.couponStringVector)],this$.couponStringVector[1:length(this$.couponStringVector)-1],sep="_")

    #Make sure the last date in the matrix is the requested end date
    if(last(this$.dates.vector) == this$.endDate)
    {
        #Actual coupon swap values
        tsdbValues <- this$.couponSwap.actual.matrix[dim(this$.couponSwap.actual.matrix)[1],]

        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_price",sep="")
        tickerName <- paste(tickerName, this$.source, sep=":")
        tsdbDF <- data.frame(zoo(t(tsdbValues), dataDateTime))
        colnames(tsdbDF) <- tickerName
        couponSwap.actual <- tsdbDF
        #write.csv(tsdbDF, squish(path,'TBA_CouponSwap_Price_Actual_',this$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv'),row.names=TRUE, quote=FALSE)


        #Model coupon swap values
        endCC <- as.numeric(this$.currentCouponZoo[dataDateTime])
        endSlope <- as.numeric(this$.swapRate10yZoo[dataDateTime] - this$.swapRate2yZoo[dataDateTime])
        tsdbValues <- this$getModelValues(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)

        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_price",sep="")
        tickerName <- paste(tickerName, this$.modelName, sep=":")
        tsdbDF <- data.frame(zoo(t(tsdbValues), dataDateTime))
        colnames(tsdbDF) <- tickerName
        couponSwap.model <- tsdbDF
        #write.csv(tsdbDF, squish(path,'TBA_CouponSwap_Price_Model_',this$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv'),row.names=TRUE, quote=FALSE)
        
        #Model coupon swap durations (dv01)
        tsdbValues <- this$getModelDurations(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)
        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_dv01",sep="")
        tickerName <- paste(tickerName, this$.modelName, sep=":")
        tsdbDF <- data.frame(zoo(t(tsdbValues), dataDateTime))
        colnames(tsdbDF) <- tickerName
        couponSwap.dv01 <- tsdbDF
        #write.csv(tsdbDF, squish(path,'TBA_CouponSwap_DV01_Model_',this$.program,'_',format(dataDateTime,'%Y%m%d'),'.csv'),row.names=TRUE, quote=FALSE)

        #Model coupon swap hedge ratios
        tsdbValues <- this$getModelHedgeRatios(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)
        tickerName <- paste(tickerHeader,"_",this$.forwardDays,"d_hedge_ratio",sep="")
        tickerName <- paste(tickerName, this$.modelName, sep=":")
        tsdbDF <- data.frame(zoo(t(tsdbValues), dataDateTime))
        colnames(tsdbDF) <- tickerName
        couponSwap.hedgeRatio <- tsdbDF

		#Model weighted Rolls
		tsdbValues <- this$getModelWeightedRolls(this$.couponVector[2:length(this$.couponVector)], endCC, endSlope)
		tickerName <- paste(tickerHeader,"1n","weighted_roll",sep="_")
		tickerName <- paste(tickerName, this$.modelName, sep=":")
		tsdbDF <- data.frame(zoo(t(tsdbValues), dataDateTime))
		colnames(tsdbDF) <- tickerName
		couponSwap.weightedRolls <- tsdbDF
		
        return(list(couponSwap.actual=couponSwap.actual, couponSwap.model=couponSwap.model, couponSwap.dv01=couponSwap.dv01, couponSwap.hedgeRatio=couponSwap.hedgeRatio, couponSwap.weightedRolls=couponSwap.weightedRolls))
    }
})
