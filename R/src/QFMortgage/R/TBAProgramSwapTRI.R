# Calculates the Program Swap TRIs
# 
# Author: RSheftel
###############################################################################

constructor("TBAProgramSwapTRI", function(baseProgram = NULL, hedgeProgram = NULL, baseCoupons = NULL, hedgeCoupons = NULL){
	this <- extend(RObject(), "TBAProgramSwapTRI")
	if (inStaticConstructor(this)) return(this)
	constructorNeeds (this, baseProgram = "character", hedgeProgram = "character", baseCoupons='numeric', hedgeCoupons='numeric')
	
	this$.base$program 	<- baseProgram
	this$.hedge$program <- hedgeProgram
	this$.base$coupons 	<- baseCoupons
	this$.hedge$coupons <- hedgeCoupons
	this$.base$couponsString 	<- format(baseCoupons, nsmall=1)
	this$.hedge$couponsString	<- format(hedgeCoupons, nsmall=1)
	return(this)
})

method("setSources", "TBAProgramSwapTRI", function(this, dv01="internal", TRI="internal", CC="internal", hedgeBasket="vNoHedge", ...){
	needs(dv01="character", TRI="character", hedgeBasket="character")
	
	this$.sources$dv01			<- dv01
	this$.sources$TRI			<- TRI
	this$.sources$CC			<- CC
	this$.sources$hedgeBasket	<- hedgeBasket
})

method("setStartEndDatesForProgram", "TBAProgramSwapTRI", function(this, baseOrHedge, ...){
	needs(baseOrHedge = "character")
	program <- squish('.',baseOrHedge)
	coupons <- this[[program]]$coupons
	
	startEndDates <- TBA$startEndDatesForCoupons(this[[program]]$program, coupons)
	
	startDates.list <- as.list(as.character(startEndDates$startDates))
	startDates.list <- lapply(startDates.list, function(x) as.POSIXct(x))
	names(startDates.list) <- as.character(coupons)
	this[[program]]$startDates <- startDates.list
	
	endDates.list <- as.list(as.character(startEndDates$endDates))
	endDates.list <- lapply(endDates.list, function(x) as.POSIXct(x))
	names(endDates.list) <- as.character(coupons)
	this[[program]]$endDates <- endDates.list		
})

method("setStartEndDatesForCoupons", "TBAProgramSwapTRI", function(this, ...){
	this$setStartEndDatesForProgram('base')
	this$setStartEndDatesForProgram('hedge')	
})

method("filterByDateRange", "TBAProgramSwapTRI", function(this, baseOrHedge, listToFilter, ...){
	program <- squish('.',baseOrHedge)
	result.list <- list()
	for (coupon in names(listToFilter)){
		if (coupon %in% names(this[[program]]$startDates)){
			rangeObj <- Range(this[[program]]$startDates[[coupon]], this[[program]]$endDates[[coupon]])
			result.list[[coupon]] <- rangeObj$cut(listToFilter[[coupon]])
			if(is.empty(result.list[[coupon]])) result.list[[coupon]] <- zoo(NA,index(listToFilter[[coupon]]))
		}
	}
	return(result.list)
})

method("setTRIs", "TBAProgramSwapTRI", function(this, container.base='tsdb', container.hedge='tsdb', ...){
	needs(container.base='character',container.hedge='character')
	this$setTBATRI('base',container.base)
	this$setTBATRI('hedge',container.hedge)	
})

method("setTBATRI", "TBAProgramSwapTRI", function(this, baseOrHedge, container='tsdb', ...){
	needs(baseOrHedge="character", container='character')
	program <- squish('.',baseOrHedge)
	attributeList <- list(
		instrument="mbs_tba", 
		program=this[[program]]$program, 
		settle="1c", 
		hedge=this$.sources$hedgeBasket,
		quote_type="close",
		transformation_output="tri",
		coupon=this[[program]]$couponsString
	)
	this[[program]]$TBA$tri <- TSDataLoader$getDataByAttributeList(container, attributeList, this$.sources$TRI, arrangeBy="coupon")
	rownames(this[[program]]$TBA$tri) <- as.numeric(rownames(this[[program]]$TBA$tri))
	this[[program]]$TBA$tri <- this$filterByDateRange(baseOrHedge, this[[program]]$TBA$tri)
})

method("setDv01s", "TBAProgramSwapTRI", function(this, container.base='tsdb', container.hedge='tsdb', ...){
	needs(container.base='character',container.hedge='character')
	this$setDv01('base',container.base)
	this$setDv01('hedge',container.hedge)	
})


method("setDv01", "TBAProgramSwapTRI", function(this, baseOrHedge, container='tsdb', ...){
	needs(baseOrHedge="character", container='character')
	program <- squish('.',baseOrHedge)
	attributeList <- list(
		instrument="mbs_tba", 
		program=this[[program]]$program, 
		quote_type="close",
		quote_convention="dv01",
		settle="1n",
		coupon=this[[program]]$couponsString
	)
	this[[program]]$TBA$dv01 <- TSDataLoader$getDataByAttributeList(container, attributeList, source=this$.sources$dv01, arrangeBy="coupon")
	rownames(this[[program]]$TBA$dv01) <- as.numeric(rownames(this[[program]]$TBA$dv01))
	this[[program]]$TBA$dv01 <- this$filterByDateRange(baseOrHedge, this[[program]]$TBA$dv01)
})

method("setCurrentCouponData", "TBAProgramSwapTRI", function(this, baseOrHedge='base', container='tsdb', ...){
	needs(baseOrHedge="character", container='character')
	program <- squish('.',baseOrHedge)
	tsdbTicker <- squish(this[[program]]$program,'_cc_1c_coupon')
	this[[program]]$CC$coupon <- TSDataLoader$getDataByName(container, tsdbTicker, source=this$.sources$CC)
})

method("generateCurrentCouponSwapTRI", "TBAProgramSwapTRI", function(this, hedgeCouponOffset, ...){
	needs(hedgeCouponOffset="numeric")

	allDataAvailable <- function(date, baseCoupon=NULL, hedgeCoupon=NULL){
		if (is.null(baseCoupon)) baseCoupon <- as.character(this$.base$CC$coupon[date][[1]])
		if (is.null(hedgeCoupon)) hedgeCoupon <- as.character(this$.hedge$CC$coupon[date][[1]])
		return(	(date %in% index(this$.base$TBA$tri[[baseCoupon]])) &&
				(date %in% index(this$.base$TBA$dv01[[baseCoupon]])) &&
				(date %in% index(this$.hedge$TBA$tri[[hedgeCoupon]])) &&
				(date %in% index(this$.hedge$TBA$dv01[[hedgeCoupon]])))
	}
	
	allDataAvailableTwoDates <- function(thisDate, priorDate){
		return(	allDataAvailable(thisDate) &&
				allDataAvailable(thisDate, as.character(this$.base$CC$coupon[priorDate][[1]]), 
											as.character(this$.hedge$CC$coupon[priorDate][[1]])))
	}
	
	this$.hedge$CC$coupon <- this$.base$CC$coupon + hedgeCouponOffset
	
	dates <- index(this$.hedge$CC$coupon)
	datesLength <- length(dates)
	
	#find first valid date
	for (dateIndex in 1:datesLength){
		if (allDataAvailable(dates[[dateIndex]])) break()
	}
	failIf((dateIndex == datesLength),"Not enough valid dates to calculate TRI")
	goodIndexes <- dateIndex
	
	#Get all the other valid date pairs
	while (dateIndex < datesLength){
		if (allDataAvailable(dates[[dateIndex]])){		#Is this redundent?
			nextDate <- dateIndex + 1
			while ((nextDate <= datesLength) && (!allDataAvailableTwoDates(dates[[nextDate]], dates[[dateIndex]]))) nextDate <- nextDate + 1
			if(nextDate <= datesLength) goodIndexes <- c(goodIndexes, nextDate)
			dateIndex <- nextDate
		}
		else dateIndex <- dateIndex + 1
	}
	goodDates <- dates[goodIndexes]

	this$.base$cc$tri.daily		<- this$currentCouponTRI('base',goodDates)
	this$.hedge$cc$tri.daily	<- this$currentCouponTRI('hedge',goodDates)
	this$.base$cc$dv01			<- this$currentCouponDv01('base', goodDates)
	this$.hedge$cc$dv01			<- this$currentCouponDv01('hedge', goodDates)
	
	hedgeRatio <- this$.base$cc$dv01 / this$.hedge$cc$dv01
	hedgeRatio <- c(0,hedgeRatio[1:(length(hedgeRatio)-1)])
	hedgeRatio[is.na(hedgeRatio)] <- 0
	this$.cc$tri.daily <- this$.base$cc$tri.daily - (this$.hedge$cc$tri.daily * hedgeRatio)
	this$.cc$tri <- zoo(100 + cumsum(this$.cc$tri.daily), goodDates)
})

method("currentCouponTRI", "TBAProgramSwapTRI", function(this, baseOrHedge, dates, ...){
	needs(baseOrHedge="character", dates="POSIXct")
	program <- squish('.',baseOrHedge)
	
	tris <- list()
	for (coupon in names(this[[program]]$TBA$tri)){
		tris[[coupon]] <- zooForDates(this[[program]]$TBA$tri[[coupon]], dates, remove.na=FALSE)
	}
	
	coupons <- as.character(zooForDates(this[[program]]$CC$coupon, dates, remove.na=FALSE))
	tri.daily <- c(0)
	for (dateIndex in 2:length(dates)){
		coupon <- coupons[[dateIndex-1]]
		tri.daily <- c(tri.daily, tris[[coupon]][[dateIndex]] - tris[[coupon]][[dateIndex-1]]) 
	}
	return(tri.daily)
})

method("currentCouponDv01", "TBAProgramSwapTRI", function(this, baseOrHedge, dates, ...){
	needs(baseOrHedge="character", dates="POSIXct")
	program <- squish('.',baseOrHedge)
	dv01s <- list()
	for (coupon in names(this[[program]]$TBA$dv01)){
		dv01s[[coupon]] <- zooForDates(this[[program]]$TBA$dv01[[coupon]], dates, remove.na=FALSE)
	}
	
	coupons <- as.character(zooForDates(this[[program]]$CC$coupon, dates, remove.na=FALSE))
	dv01.daily <- c()
	for (dateIndex in seq_along(dates)){
		coupon <- coupons[[dateIndex]]
		dv01.daily <- c(dv01.daily, dv01s[[coupon]][[dateIndex]]) 
	}
	return(dv01.daily)
		
})

method("uploadTRIs", "TBAProgramSwapTRI", function(this, uploadPath=NULL, uploadMethod='file', ...){
	needs(uploadPath="character?", uploadMethod="character?")
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
	tsdbSource <- this$.sources$TRI
	element <- c('tri')
	tsdbName <- paste(this$.base$program, this$.hedge$program, 'cc','1c',element,this$.sources$hedgeBasket,sep="_")
	#PURGE!
	if ((TimeSeriesDB()$timeSeriesExists(tsdbName)) && uploadMethod=='direct') TimeSeriesDB()$purgeTimeSeries(tsdbName, tsdbSource)			
	uploadZooToTsdb(this$.cc[[element]], tsdbNames=tsdbName, tsdbSources=tsdbSource,
					uploadMethod=uploadMethod, uploadFilename=tsdbName, uploadPath=uploadPath)
})
