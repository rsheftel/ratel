# RegressionPartials class
# 
# Sets the partial durations using the regression methodology.
######################################################################################################################

constructor("RegressionPartials", function(program=NULL, model=NULL){
	this <- extend(RObject(), "RegressionPartials", .program=program, .model=model)
	constructorNeeds(this, program="character?", model="character?")
	if (inStaticConstructor(this)) return(this)
	this$.partialPoints <- c('2y','5y','10y','30y')
	this$minMaxPercents()
	this$.setCoefficients()
	this$.dv01Settle = "1n"
	this$.coupons = format(TBA$couponVector(program),nsmall=1)
	return(this)
})

method("minMaxPercents", "RegressionPartials", function(this, min=0, max=1, ...){
	needs(min='integer|numeric', max='integer|numeric')
	this$.maxPartial <- max
	this$.minPartial <- min	
})

###################################################################################
#   Partials methods
###################################################################################

method("partial", "RegressionPartials", function(this, itm, slope, dv01=NULL, ...){
	needs(itm='numeric', slope='numeric', dv01='numeric?')
	
	coefs <- this$.coefs[[this$.model]][[this$.program]]
	partials <- vector()	
	for (partialPoint in this$.partialPoints[1:(length(this$.partialPoints)-1)]){
		partial <- (coefs[[partialPoint]]$const + itm*coefs[[partialPoint]]$itm + (itm^2)*coefs[[partialPoint]]$itmSq
							+ slope * coefs[[partialPoint]]$slope)
		partials <- c(partials,partial)
	}
	partials[partials > this$.maxPartial] <- this$.maxPartial
	partials[partials < this$.minPartial] <- this$.minPartial						

	partials <- c(partials, (1- sum(partials)))
	names(partials) <- this$.partialPoints
	if(!is.null(dv01)) partials <- partials * dv01
	return(partials)
})

method("partials", "RegressionPartials", function(this, coupons, range=NULL, verbose=FALSE, ...){
	needs(coupons = 'numeric', range="Range?")
	failIf(is.null(this$.currentCoupon), 'Current coupon not set.')	
	failIf(is.null(this$.swapSlope), 'Swap slope not set.')
	failIf(is.null(this$.TBAdv01), 'TBA durations not set.')
	
	result.list <- list()
	for (coupon in coupons){
		result.list[[format(coupon,nsmall=1)]] <- this$.partialZoo(coupon, range, verbose)
	}
	return(result.list)
})

method(".partialZoo", "RegressionPartials", function(this, coupon, range=NULL, verbose=FALSE, ...){
	needs(coupon="numeric", range="Range?")
	
	mergeZoo <- merge(this$.currentCoupon, this$.swapSlope, this$.TBAdv01[[format(coupon,nsmall=1)]], all=FALSE)
	if(!is.null(range)) mergeZoo <- range$cut(mergeZoo)
	
	all.dates <- index(mergeZoo)
	cc.zoo 		<- mergeZoo[,1]
	slope.zoo 	<- mergeZoo[,2]
	dv01.zoo 	<- mergeZoo[,3]
	itm.zoo 	<- coupon - cc.zoo
	
	res.matrix <- NULL
	for (x in 1:length(all.dates)){
		if(verbose) print(squish('Coupon : ',coupon,' - ',format(all.dates[[x]],'%Y-%m-%d')))
		res.matrix <- rbind(res.matrix,this$partial(itm.zoo[[x]], slope.zoo[[x]], dv01.zoo[[x]]))
	}
	return(zoo(res.matrix,all.dates))
})

method("uploadPartials", "RegressionPartials", function(this, partialList, uploadMethod='file', uploadFilename=NULL, uploadPath=NULL, ...){
	needs(partialList='list(zoo)', uploadPath='character?', uploadMethod='character?', uploadFilename='character?')
	
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()
	tsdbNames = NULL
	for (coupon in names(partialList))
		tsdbNames <- c(tsdbNames,paste(this$.program,coupon,'1n','partial_duration',this$.partialPoints,sep="_"))
	
	tsdbValues <- do.call(merge, partialList)
	uploadZooToTsdb(tsdbValues, tsdbNames=tsdbNames, tsdbSources=this$.model, uploadMethod=uploadMethod, 
						uploadFilename=uploadFilename, uploadPath=uploadPath)
})

###################################################################################
#   Set access methods
###################################################################################
method("setCurrentCoupon", "RegressionPartials", function(this, container='tsdb', source='internal', ...){
	needs(container="character", source="character")
	this$.currentCoupon <- TSDataLoader$getDataByName(container, squish(this$.program,'_cc_30d_yield'), source)
})

method("setSwapSlope", "RegressionPartials", function(this, container='tsdb', source='internal', ...){
	needs(container="character", source="character")
	sr2  <- TSDataLoader$getDataByName(container, 'irs_usd_rate_2y_mid', source)
	sr10 <- TSDataLoader$getDataByName(container, 'irs_usd_rate_10y_mid', source)
	this$.swapSlope <- (sr10 - sr2)
})

method("setTBADv01", "RegressionPartials", function(this, container='tsdb', source=NULL, ...){
	needs(container="character", source="character?")
	if(is.null(source)) source <- this$.model
	attributeList <- list(
		instrument="mbs_tba", 
		program=this$.program, 
		quote_type="close",
		quote_convention="dv01",
		quote_side="bid",
		settle=this$.dv01Settle,
		coupon=this$.coupons
	)
	this$.TBAdv01 <- TSDataLoader$getDataByAttributeList(container=container, attributeList, source=source, arrangeBy="coupon")
})


###################################################################################
#   Coefficients for the models
###################################################################################

method(".setCoefficients", "RegressionPartials", function(this, ...){
	this$.coefs <- list()
	
	model <- 'qfmodel_smithBreedan_vector1.0'
	#FNCL
	this$.coefs[[model]][['fncl']][['2y']]$const <- 0.3208346
	this$.coefs[[model]][['fncl']][['2y']]$itm 	 <- 0.1897757
	this$.coefs[[model]][['fncl']][['2y']]$itmSq <- 0.0228955
	this$.coefs[[model]][['fncl']][['2y']]$slope <- -0.0496277 
	
	this$.coefs[[model]][['fncl']][['5y']]$const <- 0.2679271 
	this$.coefs[[model]][['fncl']][['5y']]$itm 	 <- -0.0360427
	this$.coefs[[model]][['fncl']][['5y']]$itmSq <- -0.0152784
	this$.coefs[[model]][['fncl']][['5y']]$slope <- -0.0311525
	
	this$.coefs[[model]][['fncl']][['10y']]$const <- 0.2623881
	this$.coefs[[model]][['fncl']][['10y']]$itm   <- -0.1481475
	this$.coefs[[model]][['fncl']][['10y']]$itmSq <- -0.0061007
	this$.coefs[[model]][['fncl']][['10y']]$slope <- 0.0456916
	
	#FNCI
	this$.coefs[[model]][['fnci']][['2y']]$const <- 0.3170046
	this$.coefs[[model]][['fnci']][['2y']]$itm 	 <- 0.1411506
	this$.coefs[[model]][['fnci']][['2y']]$itmSq <- 0.0106018
	this$.coefs[[model]][['fnci']][['2y']]$slope <- -0.0253569 
	
	this$.coefs[[model]][['fnci']][['5y']]$const <- 0.3444643 
	this$.coefs[[model]][['fnci']][['5y']]$itm 	 <- -0.0865246
	this$.coefs[[model]][['fnci']][['5y']]$itmSq <- -0.0219803
	this$.coefs[[model]][['fnci']][['5y']]$slope <- -0.0527414
	
	this$.coefs[[model]][['fnci']][['10y']]$const <- 0.3272437
	this$.coefs[[model]][['fnci']][['10y']]$itm   <- -0.0611712
	this$.coefs[[model]][['fnci']][['10y']]$itmSq <- -0.0026945
	this$.coefs[[model]][['fnci']][['10y']]$slope <- 0.0716029	
})
