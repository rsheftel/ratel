# Calculates the CC TRI
# 
# Author: RSheftel
###############################################################################


constructor("TBACurrentCouponTRI", function(program = NULL, couponVector = NULL, hedgeBasket = NULL, triggerLow = NULL, triggerHigh = NULL){
	this <- extend(RObject(), "TBACurrentCouponTRI", .program = program, .couponVector = couponVector, .hedgeBasket = hedgeBasket, .triggerHigh = triggerHigh, .triggerLow = triggerLow)
	if (inStaticConstructor(this)) return(this)
	constructorNeeds (this, program = "character", couponVector = "numeric", hedgeBasket = "character", triggerHigh = 'numeric', triggerLow='numeric')
	
	#Set up initial variables
	this$.couponStringVector = format(this$.couponVector,nsmall=1)
	this$.couponStep = 0.5
	this$.TBA.settle = "1n"
	this$setSources()
	
	return(this)
})

method("setSources", "TBACurrentCouponTRI", function(this, tbaCouponTRISource="internal", tbaPriceSource="internal", ...){
	#Set up the tsdb data sources for the various elements
	needs(tbaCouponTRISource="character?", tbaPriceSource="character?")
	
	this$.sources$tbaCouponTRI			<- tbaCouponTRISource
	this$.sources$tbaPrice				<- tbaPriceSource
})

method("setStartEndDatesForCoupons", "TBACurrentCouponTRI", function(this, startDates=NULL, endDates=NULL,...){
	needs(startDates="POSIXct|character?", endDates="POSIXct|character?")
	
	startEndDates <- TBA$startEndDatesForCoupons(this$.program, this$.couponVector, startDates=startDates, endDates=endDates)
	
	startDates.list <- as.list(as.character(startEndDates$startDates))
	startDates.list <- lapply(startDates.list, function(x) as.POSIXct(x))
	names(startDates.list) <- as.character(this$.couponVector)
	this$.TBA$startDates <- startDates.list
	
	endDates.list <- as.list(as.character(startEndDates$endDates))
	endDates.list <- lapply(endDates.list, function(x) as.POSIXct(x))
	names(endDates.list) <- as.character(this$.couponVector)
	this$.TBA$endDates <- endDates.list		
})

method("filterByDateRange", "TBACurrentCouponTRI", function(this, listToFilter, ...){
	result.list <- list()
	for (coupon in names(listToFilter)){
		if (coupon %in% names(this$.TBA$startDates)){
			rangeObj <- Range(this$.TBA$startDates[[coupon]], this$.TBA$endDates[[coupon]])
			result.list[[coupon]] <- rangeObj$cut(listToFilter[[coupon]])
			if(is.empty(result.list[[coupon]])) result.list[[coupon]] <- zoo(NA,index(listToFilter[[coupon]]))
		}
	}
	return(result.list)
})

method("setTBATRI", "TBACurrentCouponTRI", function(this, container='tsdb', ...){
	
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		settle="1c", 
		hedge=this$.hedgeBasket,
		quote_type="close",
		transformation_output="tri_daily",
		coupon=this$.couponStringVector
	)
	
	this$.TBA$tri.daily <- TSDataLoader$getDataByAttributeList(container, attributeList, this$.sources$tbaCouponTRI, arrangeBy="coupon") # TimeSeriesDB$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$tbaCouponTRI, arrange.by="coupon")
	rownames(this$.TBA$tri.daily) <- as.numeric(rownames(this$.TBA$tri.daily))
	this$.TBA$tri.daily <- this$filterByDateRange(this$.TBA$tri.daily)
})


method("setTBAPrice", "TBACurrentCouponTRI", function(this, container='tsdb', ...){
	
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="price",
		quote_side="bid",
		settle=this$.TBA.settle,
		coupon=this$.couponStringVector
	)
	
	this$.TBA$price <- TSDataLoader$getDataByAttributeList(container, attributeList, source=this$.sources$tbaPrice, arrangeBy="coupon")
	rownames(this$.TBA$price) <- as.numeric(rownames(this$.TBA$price))
	this$.TBA$price <- this$filterByDateRange(this$.TBA$price)
})

###################################################################################################
#	TRI Generation
###################################################################################################

method("generateTRI", "TBACurrentCouponTRI", function(this, ...){
	
	print("Setup TRI inputs...")
	this$makeGrids()
	this$makeCrossovers()
	print("Calculate TRI...")
	this$calculateTRI()		
})

method("calculateTRI","TBACurrentCouponTRI", function(this, ...){
	
	dateVector <- index(this$.grid$tri.daily)
	naBuffer <- rep(NA,length(dateVector)-1)
	triCoupons <- as.numeric(colnames(this$.grid$tri.daily))
	
	this$.CC$tri 		<- zoo(c(100,naBuffer),dateVector)
	this$.CC$tri.daily 	<- zoo(c(0,naBuffer),dateVector)
	this$.CC$coupon 	<- zoo(c(this$.CC$crossOver.coupon[[1]],naBuffer),dateVector)
	this$.CC$price		<- zoo(NA,dateVector)
	
	for (x in 2:length(dateVector)){
		this$.CC$tri.daily[[x]] <- this$.grid$tri.daily[[x,match(this$.CC$coupon[[x-1]],triCoupons)]]
		triDaily <- 0
		if (!is.na(this$.CC$tri.daily[[x]])) triDaily <- this$.CC$tri.daily[[x]]
		this$.CC$tri[[x]]		<- this$.CC$tri[[x-1]] + triDaily
		this$.CC$price[[x]]		<- this$.grid$price[[x,match(this$.CC$coupon[[x-1]],triCoupons)]]
		
		newCoupon <- this$.CC$coupon[[x-1]]
		if (this$.CC$price[[x]] < this$.triggerLow) newCoupon <- this$.CC$crossUnder.coupon[[x]]
		if (this$.CC$price[[x]] > this$.triggerHigh) newCoupon <- this$.CC$crossOver.coupon[[x]]
		this$.CC$coupon[[x]] <- newCoupon
	}
})

method("makeCrossovers", "TBACurrentCouponTRI", function(this, ...){
	
	cutoffHigh <- function(x){
		goodCols <- !is.na(x)
		goodCoupons <- this$.couponVector[goodCols]
		goodPrices <- x[goodCols]
		y <- approx(goodPrices,goodCoupons, this$.triggerHigh, method='constant',rule=2,f=0)$y
		#if (y > min(goodCoupons)) y <- goodCoupons[match(y,goodCoupons)-1]
		return(y)
	}
	
	cutoffLow <- function(x){
		goodCols <- !is.na(x)
		goodCoupons <- this$.couponVector[goodCols]
		goodPrices <- x[goodCols]
		y <- approx(goodPrices,goodCoupons, this$.triggerLow, method='constant',rule=2,f=1)$y  
	 	return(y)
 	}
	
	this$.CC$crossOver.coupon 	<- rollapply(this$.grid$price, width=1, FUN=cutoffHigh, by.column=FALSE)
	this$.CC$crossUnder.coupon 	<- rollapply(this$.grid$price, width=1, FUN=cutoffLow, by.column=FALSE)
})

method("makeGrids", "TBACurrentCouponTRI", function(this, ...){
	this$.grid$tri.daily <- getZooDataFrame(do.call(merge.zoo,this$.TBA$tri.daily))
	this$.grid$price <- zooForDates(getZooDataFrame(do.call(merge.zoo,this$.TBA$price)),index(this$.grid$tri.daily),remove.na=FALSE)
})


method("uploadTRIs", "TBACurrentCouponTRI", function(this, uploadPath=NULL, uploadMethod='file',...){
	
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
	tsdbSource <- this$.sources$tbaCouponTRI
	elements <- c('tri','tri.daily')
	for (element in elements){ 		
		tsdbName <- paste(this$.program, 'cc','1c',sub('\\.','_',element),this$.hedgeBasket,sep="_")
			
		#PURGE!
		if (TimeSeriesDB()$timeSeriesExists(tsdbName) && (uploadMethod=='direct'))
			TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
			
		uploadZooToTsdb(this$.CC[[element]], tsdbNames=tsdbName, tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})

method("uploadAttributes", "TBACurrentCouponTRI", function(this, uploadPath=NULL, uploadMethod='file',...){
		
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
	tsdbSource <- 'internal'
	elements <- c('coupon','price')
	for (element in elements){ 		
		tsdbName <- paste(this$.program, 'cc','1c',sub('\\.','_',element),sep="_")
		
		#PURGE!
		if (TimeSeriesDB()$timeSeriesExists(tsdbName) && (uploadMethod=='direct'))
			TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)
		
		uploadZooToTsdb(this$.CC[[element]], tsdbNames=tsdbName, tsdbSources=tsdbSource, uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
	}
})
