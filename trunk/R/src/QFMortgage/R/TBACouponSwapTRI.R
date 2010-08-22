# CouponSwapTRI.R
# 
# TODO: Add comment
#
# Author: rsheftel
###############################################################################


constructor("TBACouponSwapTRI", function(program = NULL, couponVector = NULL, TBATRIhedge = NULL){
	this <- extend(RObject(), "TBACouponSwapTRI", .program = program, .couponVector = couponVector, .TBATRIhedge = TBATRIhedge)
	if (inStaticConstructor(this)) return(this)
	constructorNeeds (this, program = "character", couponVector = "numeric", TBATRIhedge = "character")
	
	#Set up initial variables
	this$.couponStringVector = format(this$.couponVector,nsmall=1)
	this$.couponStepSize = 0.5
	this$setTsdbSources()
	this$setHedgeRatio
	return(this)
})

method("setHedgeRatio", "TBACouponSwapTRI", function(this, forwardDays = "45d", ...){
	needs(forwardDays = "character")
	this$.forwardDays = forwardDays
})

method("setTsdbSources", "TBACouponSwapTRI", function(this, tbaCouponTRISource="internal", couponSwapHedgeRatioSource="internal", 
															couponDv01='internal', tbaPriceSource='internal', ...){
	#Set up the tsdb data sources for the various elements
	needs(tbaCouponTRISource="character?", couponSwapHedgeRatioSource="character?", couponDv01='character?')
		
	this$.sources$tbaCouponTRI			<- tbaCouponTRISource
	this$.sources$couponSwapHedgeRatio	<- couponSwapHedgeRatioSource
	this$.sources$couponDv01			<- couponDv01
	this$.sources$tbaPrice				<- tbaPriceSource
})

method("setTBATRIDataFromTsdb", "TBACouponSwapTRI", function(this, container='tsdb', ...){
#Get all the raw TRI Data from TSDB
	attributeList <- list(
			instrument="mbs_tba", 
			program=this$.program, 
			settle="1c", 
			hedge=this$.TBATRIhedge,
			quote_type="close",
			transformation_output="tri",
			coupon=this$.couponStringVector
			)
	
	this$.TBA$TRI <- TSDataLoader$getDataByAttributeList(container, attributeList, source=this$.sources$tbaCouponTRI, arrangeBy="coupon")
	rownames(this$.TBA$TRI) <- as.numeric(rownames(this$.TBA$TRI))
})

method("setTBAPrices", "TBACouponSwapTRI", function(this, container='tsdb', ...){
	needs(container="character")
	
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program,
		quote_convention='price',  
		quote_type="close",
		quote_side='bid',
		coupon=this$.couponStringVector
	)
	
	list.1n <- attributeList
	list.1n$settle='1n'
	this$.TBA$price[['1n']] <- TSDataLoader$getDataByAttributeList(container, list.1n, source=this$.sources$tbaPrice, arrangeBy="coupon")
	rownames(this$.TBA$price[['1n']]) <- as.numeric(rownames(this$.TBA$price[['1n']]))

	list.2n <- attributeList
	list.2n$settle='2n'
	this$.TBA$price[['2n']] <- TSDataLoader$getDataByAttributeList(container, list.2n, source=this$.sources$tbaPrice, arrangeBy="coupon")
	rownames(this$.TBA$price[['2n']]) <- as.numeric(rownames(this$.TBA$price[['2n']]))

	for (coupon in as.character(this$.couponVector)){
		if ((!is.null(this$.TBA$price[['1n']][[coupon]])) && (!is.null(this$.TBA$price[['2n']][[coupon]]))){
			this$.TBA$roll[[coupon]] <- this$.TBA$price[['1n']][[coupon]] - this$.TBA$price[['2n']][[coupon]]
		}else{
			this$.TBA$roll[[coupon]] <- NULL
		}
	}
})

method("setHedgeRatiosFromTsdb", "TBACouponSwapTRI", function(this, ...){
#Get hedge ratio data from TSDB
	attributeList <- list(
			instrument="mbs_tba", 
			program=this$.program, 
			settle=this$.forwardDays, 
			quote_type="close",
			quote_convention="hedge_ratio",
			coupon_high=this$.couponStringVector[2:length(this$.couponStringVector)]
			)
		
	this$.couponSwap$hedgeRatio <- TimeSeriesDB$retrieveTimeSeriesByAttributeList(attributeList, this$.sources$couponSwapHedgeRatio, arrange.by="coupon_high")
	rownames(this$.couponSwap$hedgeRatio) <- as.numeric(rownames(this$.couponSwap$hedgeRatio))
})

method("setHedgeRatiosFromDv01s", "TBACouponSwapTRI", function(this, container='tsdb', ...){
	needs(container='character')
	
	attributeList <- list(
			instrument='mbs_tba',
			program=this$.program,
			settle="1n",
			quote_type="close",
			quote_convention="dv01"
			)				
			
	this$.TBA$dv01 <- TSDataLoader$getDataByAttributeList(container, attributeList, source=this$.sources$couponDv01, arrangeBy="coupon")
	
	this$.couponSwap$hedgeRatio <- list()
	for (count in 2:length(this$.couponVector)){
		couponHigh <- format(this$.couponVector[count], nsmall=1)
		couponLow  <- format(this$.couponVector[count-1], nsmall=1)
		if ((couponHigh %in% names(this$.TBA$dv01)) && (couponLow %in% names(this$.TBA$dv01)))
			this$.couponSwap$hedgeRatio[[couponHigh]] <- na.omit(this$.TBA$dv01[[couponHigh]] / this$.TBA$dv01[[couponLow]])
	}
	names(this$.couponSwap$hedgeRatio) <- as.numeric(names(this$.couponSwap$hedgeRatio)) 
})

##############################################################################################
#          TRI Calculations
##############################################################################################

method("calculateTRI", "TBACouponSwapTRI", function(this, couponHigh, hedgeRatio.min=NULL, hedgeRatio.max=NULL, ...){
#Calculate the TRI for a single coupon
	needs(couponHigh="numeric", hedgeRatio.min='numeric?', hedgeRatio.max='numeric?')
	
	problem <- function(err) { print(squish(err, " for couponHigh ", couponHigh, "\n")); NA}
	
	if (!(couponHigh%in%this$.couponVector)) return(problem("Coupon not in couponVector"))
	if (!((couponHigh - this$.couponStepSize)%in%this$.couponVector)) return(problem("No couponLow"))
	couponLow <- couponHigh - this$.couponStepSize
	
	if (!(couponHigh%in%names(this$.TBA$TRI))) return(problem("No TRI for couponHigh"))
	if (!(couponLow%in%names(this$.TBA$TRI))) return(problem("No TRI for couponLow"))
	if (!(couponHigh%in%names(this$.couponSwap$hedgeRatio))) return("No hedgeRatio")
	
	couponTRIHigh <- this$.TBA$TRI[[as.character(couponHigh)]]
	couponTRILow  <- this$.TBA$TRI[[as.character(couponLow)]]
	
	hedgeRatios <- this$.hedgeRatios(couponHigh, hedgeRatio.min, hedgeRatio.max)   
	
	couponPair <- Pair(couponTRIHigh, couponTRILow)
	triZoo.daily <- couponPair$calculateZooFromHedgeRatios(hedgeRatios)
	firstDate <- couponPair$getFirstDateFromZooFromHedgeRatios()
	triZoo <- getCumTriFromDailyTri(triZoo.daily, baseTri = 100,refDate = firstDate)
	return(triZoo)
})

method("calculateTRIs", "TBACouponSwapTRI", function(this, hedgeRatio.min=NULL, hedgeRatio.max=NULL, ...){
#Calculates all the TRIs to each coupon in the couponVector
	needs(hedgeRatio.min='numeric?', hedgeRatio.max='numeric?')	
	coupons <- this$.couponVector[2:length(this$.couponVector)]
	
	for (coupon in coupons){
		print(squish("Calculating TRI for ",as.character(coupon)))
		this$.couponSwap$TRI[[as.character(coupon)]] <- this$calculateTRI(coupon, hedgeRatio.min, hedgeRatio.max)
	} 
})

method(".hedgeRatios", "TBACouponSwapTRI", function(this, couponHigh, hedgeRatio.min=NULL, hedgeRatio.max=NULL, ...){
	needs(couponHigh="numeric|integer")
	hedgeRatios <- this$.couponSwap$hedgeRatio[[as.character(couponHigh)]]
	if (!is.null(hedgeRatio.max))
		hedgeRatios[hedgeRatios > hedgeRatio.max] <- hedgeRatio.max
	if (!is.null(hedgeRatio.min))
		hedgeRatios[hedgeRatios < hedgeRatio.min] <- hedgeRatio.min
	return(hedgeRatios)		
})

##############################################################################################
#          Roll Calculations
##############################################################################################
method("calculateWeightedRoll", "TBACouponSwapTRI", function(this, couponHigh, hedgeRatio.min=NULL, hedgeRatio.max=NULL, ...){
	needs(couponHigh='numeric|integer|character')
	
	problem <- function(err) { print(squish(err, " for couponHigh ", couponHigh, "\n")); NA}
	
	if (!(couponHigh%in%this$.couponVector)) return(problem("Coupon not in couponVector"))
	if (!((couponHigh - this$.couponStepSize)%in%this$.couponVector)) return(problem("No couponLow"))
	couponLow <- couponHigh - this$.couponStepSize
	
	if (!(couponHigh%in%names(this$.TBA$roll))) return(problem("No roll for couponHigh"))
	if (!(couponLow%in%names(this$.TBA$roll))) return(problem("No roll for couponLow"))
	if (!(couponHigh%in%names(this$.couponSwap$hedgeRatio))) return("No hedgeRatio")

	return(this$.TBA$roll[[as.character(couponHigh)]] 
			- (this$.hedgeRatios(couponHigh, hedgeRatio.min, hedgeRatio.max) * this$.TBA$roll[[as.character(couponLow)]]))
})

method("calculateWeightedRolls", "TBACouponSwapTRI", function(this, hedgeRatio.min=NULL, hedgeRatio.max=NULL, ...){
	needs(hedgeRatio.min='numeric?', hedgeRatio.max='numeric?')	
	coupons <- this$.couponVector[2:length(this$.couponVector)]
	
	for (coupon in coupons){
		print(squish("Calculating Weighted Roll for ",as.character(coupon)))
		weightedRoll <- this$calculateWeightedRoll(coupon, hedgeRatio.min, hedgeRatio.max)
		if (is.zoo(weightedRoll)) this$.couponSwap$weightedRoll[[as.character(coupon)]] <- weightedRoll 
	} 
})

##############################################################################################
#          TSDB Population
##############################################################################################

method("uploadTRIs", "TBACouponSwapTRI", function(this, uploadPath=NULL, uploadMethod='file',...){
#fncl_5.5_5.0_1c_45d_tri_vSwapPartials, source= qfmodel_couponSwapModel_1.0

	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
	coupons <- names(this$.couponSwap$TRI)
		
	for (coupon in coupons){
		tsdbName <- paste(this$.program, format(as.numeric(coupon),nsmall=1), format(as.numeric(coupon)-this$.couponStepSize,nsmall=1),
							'1c', this$.forwardDays, 'tri', this$.TBATRIhedge, sep="_"
							)
		
		#PURGE!
		if ((uploadMethod=='direct') && TimeSeriesDB()$timeSeriesExists(tsdbName)) TimeSeriesDB()$purgeTimeSeries(tsdbName, this$.sources$couponSwapHedgeRatio)
		
		uploadZooToTsdb(this$.couponSwap$TRI[[coupon]], tsdbNames=tsdbName, tsdbSources=this$.sources$couponSwapHedgeRatio, 
							uploadMethod=uploadMethod,uploadFilename=tsdbName, uploadPath=uploadPath)
	}			
})

method("uploadWeightedRolls", "TBACouponSwapTRI", function(this, uploadPath=NULL, uploadMethod='file',...){
#fncl_5.5_5.0_1n_weighted_roll, source= qfmodel_couponSwapModel_1.0
	
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()		
	coupons <- names(this$.couponSwap$weightedRoll)
	
	for (coupon in coupons){
		tsdbName <- paste(this$.program, format(as.numeric(coupon),nsmall=1), format(as.numeric(coupon)-this$.couponStepSize,nsmall=1),
			'1n', 'weighted_roll', sep="_"
		)
		
		#PURGE!
		if ((uploadMethod=='direct') && TimeSeriesDB()$timeSeriesExists(tsdbName)) TimeSeriesDB()$purgeTimeSeries(tsdbName, this$.sources$couponSwapHedgeRatio)
		
		rollZoo <- this$.couponSwap$weightedRoll[[coupon]]
		rollZoo[rollZoo==Inf] <- NA
		rollZoo[rollZoo==-Inf] <- NA
		rollZoo <- na.omit(rollZoo)
		
		uploadZooToTsdb(rollZoo, tsdbNames=tsdbName, tsdbSources=this$.sources$couponSwapHedgeRatio, 
								uploadMethod=uploadMethod,uploadFilename=tsdbName, uploadPath=uploadPath)
	}			
})
