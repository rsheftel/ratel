# TODO: Add comment
# 
# Author: RSheftel
###############################################################################


constructor("TBAModifiedOAD", function(program = NULL, couponVector = NULL){
	this <- extend(RObject(), "TBAModifiedOAD", .program = program, .couponVector = couponVector)
	if (inStaticConstructor(this)) return(this)
	constructorNeeds (this, program = "character", couponVector = "numeric")
		
	#Set up initial variables
	this$.couponStringVector = format(this$.couponVector,nsmall=1)
	this$.couponStepSize = 0.5
	this$.settle = "1n"		
	this$setTsdbSources()
	return(this)
})

method("getData", "TBAModifiedOAD", function(this, container='tsdb', attributeList, source, arrangeBy, ...){
	TSDataLoader$getDataByAttributeList(container, attributeList, source, arrangeBy)
})

method("setTsdbSources", "TBAModifiedOAD", function(this, tbaOADSource="internal", currentCoupon='internal', smileModel='qfmodel_smileAdjustedOAD',...){
#Set up the tsdb data sources for the various elements
	needs(tbaOADSource="character?")
		
	this$.sources$oad <- tbaOADSource
	this$.sources$currentCoupon <- currentCoupon
	this$.sources$smileModel <- smileModel
})

method("setITMKnotPoints", "TBAModifiedOAD", function(this, knotPoints, ...){
	needs(knotPoints="numeric")
	
	this$.ITMKnotPoints <- knotPoints
	this$.ITMKnotPointsStringVector <- format(this$.ITMKnotPoints,nsmall=1)
	this$.ITMKnotPointsLength <- length(knotPoints)
})

method("setTBAOAD", "TBAModifiedOAD", function(this, container='tsdb', ...){
#TBA$oad$[coupon]

	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="dv01",
		quote_side="bid",
		settle=this$.settle,
		coupon=this$.couponStringVector
	)

	this$.TBA$oad <- this$getData(container, attributeList, this$.sources$oad, arrangeBy="coupon")
	rownames(this$.TBA$oad) <- as.numeric(rownames(this$.TBA$oad))
	this$.TBA$oad <- this$filterByDateRange(this$.TBA$oad)
})

method("setTBASpreadDv01", "TBAModifiedOAD", function(this, container='tsdb', ...){
#TBA$spreadDv01$[coupon]

	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="spread_duration",
		quote_side="bid",
		settle=this$.settle,
		coupon=this$.couponStringVector
	)
	
	this$.TBA$spreadDv01 <- this$getData(container, attributeList, this$.sources$oad, arrangeBy="coupon")
	rownames(this$.TBA$spreadDv01) <- as.numeric(rownames(this$.TBA$spreadDv01))
	this$.TBA$spreadDv01 <- this$filterByDateRange(this$.TBA$spreadDv01)
})

method("setCC", "TBAModifiedOAD", function(this, container='tsdb',...){
#TBA$currentCoupon$yield
	
	if (container == 'tsdb'){
		this$.currentCoupon$yield = TimeSeriesDB$retrieveOneTimeSeriesByName('fncl_cc_30d_yield',this$.sources$currentCoupon)
	}
	else{
		this$.currentCoupon$yield <- this$getData(container, NULL, this$.sources$currentCoupon, arrangeBy="settle")
	}	
})

method("setOAS", "TBAModifiedOAD", function(this, container='tsdb', ...){
#TBA$oas$coupon$[coupon]
	
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="oas",
		quote_side="bid",
		settle=this$.settle,
		curve_type="libor",
		coupon=this$.couponStringVector
	)
	
	#Set start and end dates by coupon
	this$.TBA$oas$coupon <- this$getData(container, attributeList, this$.sources$oad, arrangeBy="coupon")
	rownames(this$.TBA$oas$coupon) <- as.numeric(rownames(this$.TBA$oas$coupon))
	this$.TBA$oas$coupon <- this$filterByDateRange(this$.TBA$oas$coupon)
})

method("setCouponDateRange", "TBAModifiedOAD", function(this, startDates=NULL, endDates=NULL,...){	
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

method("filterByDateRange", "TBAModifiedOAD", function(this, listToFilter, ...){
	result.list <- list()
	for (coupon in names(listToFilter)){
		if (coupon %in% names(this$.TBA$startDates)){
			rangeObj <- Range(this$.TBA$startDates[[coupon]], this$.TBA$endDates[[coupon]])
			result.list[[coupon]] <- rangeObj$cut(listToFilter[[coupon]])
			if(is.empty(result.list[[coupon]])) result.list[[coupon]] <- zoo(NA,index(listToFilter[[coupon]]))
		}
		else
			result.list[[coupon]] <- NA
	}
	return(result.list)
})

method("generateITMOASs", "TBAModifiedOAD", function(this, ...){
	oasZoo <- do.call(merge.zoo, this$.TBA$oas$coupon)
	#Fix this botch job later, it is only because getting from file or tsdb are different.
	if (!is.null(names(this$.currentCoupon$yield)))
		itmZoo <- merge(the(this$.currentCoupon$yield), oasZoo, all=FALSE)
	else
		itmZoo <- merge(this$.currentCoupon$yield, oasZoo, all=FALSE)
	
	currentCouponZoo <- itmZoo[,1]
	couponZoo <- zoo(matrix(as.numeric(colnames(oasZoo)),ncol=length(colnames(oasZoo))), index(itmZoo))
	this$.TBA$itmMatrix <- couponZoo - currentCouponZoo
	
	itmOASMatrix <- matrix(NA,ncol=this$.ITMKnotPointsLength,nrow=length(index(couponZoo)))  
	
	for (zooIndex in seq_along(index(this$.TBA$itmMatrix))){
		zooDate <- index(couponZoo)[zooIndex]
		itmOASMatrix[zooIndex,] <- qf.interpolateVector(this$.ITMKnotPoints, as.vector(this$.TBA$itmMatrix[zooDate,]), as.vector(oasZoo[zooDate,]))
	}

	for (knotCount in seq_along(this$.ITMKnotPoints)){
		knotPoint <- as.character(this$.ITMKnotPoints)[knotCount]
		this$.TBA$oas$itm[[knotPoint]] <- zoo(itmOASMatrix[,knotCount], index(this$.TBA$itmMatrix))
	}	
})

method("generateWeights", "TBAModifiedOAD", function(this, halfLife=120, maxPeriods=504, totalPeriods, ...){
#Generate the weight vector
	needs(halfLife="numeric?", maxPeriods="numeric?", totalPeriods="numeric|integer")
	
	daysAgo <- (totalPeriods-1):0
	lambda <- 0.5^(1/halfLife)
	weights.time        <- lambda^daysAgo
	weights.maxPeriods  <- ifelse(daysAgo < maxPeriods,1,0)
	
	#weights.startDate   <- do.call(cbind,lapply(this$.couponSwap.startDates, function(d) this$.dates.vector >= d))
	#weights.endDate     <- do.call(cbind,lapply(this$.couponSwap.endDates, function(d) this$.dates.vector <= d))
	#weights.dates.matrix <- ifelse(weights.startDate & weights.endDate,1,0)
	
	return(weights.time * weights.maxPeriods)
})

method("calculateDoasDccForITM", "TBAModifiedOAD", function(this, knotPoint, endDate, ...){
#TBA$dOASdCC$itm$[knotpoint]
	needs(knotPoint="numeric", endDate="POSIXct")

	#endDate <- as.Date(endDate)
	
	#Ugly hack like above to get working for tsdb load and file load 
	if(!is.null(names(this$.currentCoupon$yield)))				
		mergedZoo <- merge(this$.TBA$oas$itm[[as.character(knotPoint)]], the(this$.currentCoupon$yield), all=FALSE)
	else
		mergedZoo <- merge(this$.TBA$oas$itm[[as.character(knotPoint)]], this$.currentCoupon$yield, all=FALSE)
	
	if(!(endDate%in%index(mergedZoo))) return(list(dOASdCC=NA,rSquare=NA))
	
	OAS <- mergedZoo[index(mergedZoo)<=endDate,1]/100
	CCyield <- mergedZoo[index(mergedZoo)<=endDate,2]
	
	dOAS <-diff(OAS,lag=this$.smileModel$parameters$lagPeriods,differences=1,arithmatic=TRUE,na.pad=FALSE)
	dCCyield <-diff(CCyield,lag=this$.smileModel$parameters$lagPeriods,differences=1,arithmatic=TRUE,na.pad=FALSE)
	
	weights <- this$generateWeights(halfLife=this$.smileModel$parameters$halfLife, maxPeriods=this$.smileModel$parameters$maxPeriods, totalPeriods=length(dOAS))
	
	data.df <- data.frame(dOAS=as.vector(dOAS),dCCyield=as.vector(dCCyield))
	
	smileModel <- lm(dOAS ~ dCCyield, data=data.df, weights=weights)
	return(list(dOASdCC=smileModel$coefficients[['dCCyield']],rSquare= summary(smileModel)$r.square))
})

method("setSmileModelParameters", "TBAModifiedOAD", function(this, halfLife=120, maxPeriods=504, lagPeriods=5, ...){
	needs(halfLife="numeric?", maxPeriods="numeric?", lagPeriods="numeric?")
		
	this$.smileModel$parameters$halfLife <- halfLife
	this$.smileModel$parameters$maxPeriods <- maxPeriods
	this$.smileModel$parameters$lagPeriods <- lagPeriods
})

method("calculateDoasDccForITMs", "TBAModifiedOAD", function(this, dateRange, knotPoints=NULL, ...){
#this$.smileModel$itm$[knot]$dOASdCC
	needs(knotPoints="numeric?", dateRange="POSIXct")
	if(is.null(knotPoints)) knotPoints=this$.ITMKnotPoints
		
	this$.smileModel$itm$dOASdCC <- list()
	this$.smileModel$itm$rSquare <- list()			
	for (knotPoint in knotPoints){
		dOASdCC.vector <- rep(NA, length(dateRange))
		rSquare.vector <- rep(NA, length(dateRange))	
		for(dateCount in seq_along(dateRange)){
			endDate <- dateRange[[dateCount]]
			print(squish("calculating dOASdCC for knot: ",knotPoint," date: ",as.character(endDate)))
			smileResult <- this$calculateDoasDccForITM(knotPoint=knotPoint, endDate=endDate)
			dOASdCC.vector[dateCount] <- smileResult$dOASdCC
			rSquare.vector[dateCount] <- smileResult$rSquare
		}
		this$.smileModel$itm$dOASdCC[[as.character(knotPoint)]] <- zoo(dOASdCC.vector,dateRange)
		this$.smileModel$itm$rSquare[[as.character(knotPoint)]] <- zoo(rSquare.vector,dateRange)
	}		
})

method("generateDoasDccForCoupons", "TBAModifiedOAD", function(this, dOASdCC.max=100, dOASdCC.min=-100, ...){
	needs(dOASdCC.max="numeric?", dOASdCC.min="numeric?")
	
	itm.dOASdCC.zoo <- do.call(merge.zoo, this$.smileModel$itm$dOASdCC)
	#If there is only one column, zoos do not preserve the column name, must insert
	if (NCOL(itm.dOASdCC.zoo) == 1){
		itm.dOASdCC.zoo <- getZooDataFrame(itm.dOASdCC.zoo)
		colnames(itm.dOASdCC.zoo) <- names(this$.smileModel$itm$dOASdCC)
	}
	
	if(!is.null(names(this$.currentCoupon$yield)))
		itmZoo <- merge(the(this$.currentCoupon$yield), itm.dOASdCC.zoo, all=FALSE)
	else
		itmZoo <- merge(this$.currentCoupon$yield, itm.dOASdCC.zoo, all=FALSE)

	currentCouponZoo <- itmZoo[,1]
	knotPointZoo <- zoo(matrix(as.numeric(colnames(itm.dOASdCC.zoo)),ncol=length(colnames(itm.dOASdCC.zoo))), index(itmZoo))
	couponZoo <- knotPointZoo + currentCouponZoo
	
	couponOASMatrix <- matrix(NA, ncol=length(this$.couponVector), nrow=length(index(knotPointZoo)))
	
	for (zooIndex in seq_along(index(couponZoo))){
		zooDate <- index(knotPointZoo)[zooIndex]
		couponOASMatrix[zooIndex,] <- qf.interpolateVector(this$.couponVector, as.vector(couponZoo[zooDate,]), as.vector(itm.dOASdCC.zoo[zooDate,]))		
	}
	
	this$.smileModel$coupon$dOASdCC <- list()
	for (couponCount in seq_along(this$.couponVector)){
		coupon <- as.character(this$.couponVector)[couponCount]
		dOASdCC.zoo <- zoo(couponOASMatrix[,couponCount], index(itmZoo))
		if(any(dOASdCC.zoo > dOASdCC.max))
			dOASdCC.zoo[dOASdCC.zoo > dOASdCC.max] <- dOASdCC.max
		if(any(dOASdCC.zoo < dOASdCC.min))
			dOASdCC.zoo[dOASdCC.zoo < dOASdCC.max] <- dOASdCC.min
		this$.smileModel$coupon$dOASdCC[[as.character(coupon)]] <- dOASdCC.zoo
	}
})

method("calculateSmileAdjustedDv01", "TBAModifiedOAD", function(this, minDv01=NULL, ...){
#TBA$smileAdjustedOAD[coupon]
	needs(minDv01="numeric?")
	
	this$.smileModel$coupon$dv01 <- list()
	for (coupon in this$.couponVector){
		coupon <- as.character(coupon)
		print(squish("Calculate SmileAdjusted Dv01 for coupon : ",coupon))
		if (length(this$.TBA$oad[[coupon]])!=0 & length(this$.TBA$spreadDv01[[coupon]])!=0) {
			if  (any(index(this$.smileModel$coupon$dOASdCC[[coupon]]) <= this$.TBA$endDates[[coupon]])){
				mergedZoo <- merge(this$.TBA$oad[[coupon]], this$.TBA$spreadDv01[[coupon]], this$.smileModel$coupon$dOASdCC[[coupon]], all=FALSE)
				this$.smileModel$coupon$dv01[[coupon]] <- mergedZoo[,1] - mergedZoo[,2]*mergedZoo[,3]
				if (!is.null(minDv01)) {
					if (any(this$.smileModel$coupon$dv01[[coupon]] <= minDv01))
						this$.smileModel$coupon$dv01[[coupon]][this$.smileModel$coupon$dv01[[coupon]] <= minDv01] <- minDv01 
				}
			}
		}
		else
			this$.smileModel$coupon$dv01[[coupon]] <- NA
	}
})

method("uploadSmileAdjustedDv01", "TBAModifiedOAD", function(this, uploadPath=NULL, uploadFilename=NULL, uploadMethod='file',...){
		
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
	tsdbNames <- c()
	if(is.null(uploadFilename))
		uploadFilename <- paste('TBA_dv01','smileModel','hl',this$.smileModel$parameters$halfLife,'mp',this$.smileModel$parameters$maxPeriods,this$.program,sep="_")	
	mergedZoo <- zoo(NA,as.POSIXct('1900-01-01 15:00:00'))
	for (coupon in names(this$.smileModel$coupon$dv01)){
		couponZoo <- this$.smileModel$coupon$dv01[[as.character(coupon)]]
		if (!all(is.na(couponZoo))){
			mergedZoo <- merge(mergedZoo,this$.smileModel$coupon$dv01[[as.character(coupon)]], all=TRUE)
			tsdbNames <- c(tsdbNames,paste(this$.program, format(as.numeric(coupon),nsmall=1), this$.settle, 'dv01', sep="_"))
		}
	}
	tsdbValues <- mergedZoo[2:length(index(mergedZoo)),2:NCOL(mergedZoo)]
	uploadZooToTsdb(tsdbValues, tsdbNames=tsdbNames, tsdbSources=this$.sources$smileModel, uploadMethod=uploadMethod, uploadFilename=uploadFilename, uploadPath=uploadPath)			
})
