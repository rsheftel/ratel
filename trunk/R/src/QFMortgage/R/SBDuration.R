# SBDuration class
# 
# Works from the article: http://www.quadrafund.com/TWiki/pub/Research/MortgageOptions/MortgageOption.pdf
######################################################################################################################

constructor("SBDuration", function(program=NULL){
	this <- extend(RObject(), "SBDuration", .program=program)
	constructorNeeds(this, program="character?")
	if (inStaticConstructor(this)) return(this)
	this$.model <- 'qfmodel_smithBreedan_vector1.0'
	return(this)
})

###################################################################################
#   Smith Breeden duration function and the objective for optimization
###################################################################################
sbDuration <- function(a,b,c,d,r){
	#r is the CC-Coupon, so for example 6.5s with a 5 CC is -1.5
	return(a + b/(1 + exp(-c*(r-d))))
}

sbObjective <- function(x, r, actuals){
	return(sum((sbDuration(x[1],x[2],x[3],x[4],r) - actuals)^2))
}

###################################################################################
#   Constants
###################################################################################

method(".params", "SBDuration", function(this, slope, ...){
	needs(slope="numeric")
	failIf((slope>100),"Slope in form of yield, ie: 0.5 is 50bp")
	failIf(is.null(this$.program),'No program defined.')
	
	res <- list()
	if (this$.program=='fncl'){
		res$a <- approx(c(0,3), c(0.5,1), slope, method='linear', rule=2)$y
		res$b <- approx(c(0,3), c(5.0,5.75), slope, method='linear', rule=2)$y
		res$c <- 2.25
		res$d <- approx(c(0,0.5,1), c(0,-0.25,-0.5), slope, method='linear', rule=2)$y
	}else if (this$.program=='fnci'){
		res$a <- approx(c(0,3), c(0.75,1), slope, method='linear', rule=2)$y
		res$b <- approx(c(0,3), c(4, 4.5), slope, method='linear', rule=2)$y
		res$c <- 2.0
		res$d <- approx(c(0,0.5,1), c(0,-0.25,-0.5), slope, method='linear', rule=2)$y
	}
	return(res)	
})

###################################################################################
#   Duration Methods
###################################################################################

method("durations", "SBDuration", function(this, slope, otm, ...){
	needs(slope="numeric", otm="numeric")
	failIf((length(slope)!=1),"Only one slope allowed.")
	sapply(otm, function(x) this$duration(slope, x))		
})

method("duration", "SBDuration", function(this, slope, otm, ...){
	needs(slope="numeric", otm="numeric")
	param <- this$.params(slope)
	return(sbDuration(param$a, param$b, param$c, param$d, otm))
})

method("solver", "SBDuration", function(static, seeds, r, actuals, ...){
	needs(seeds="numeric|integer", r="numeric|integer", actuals="numeric|integer")
	failIf((length(r) != length(actuals)),"Length of 'r' vector must be same as 'actuals'")
	failIf((length(seeds) != 4),"Seeds not of correct length.")
	return(nlm(sbObjective,seeds,r=r,actuals=actuals))			
})

###################################################################################
#   Actual Coupon Methods
###################################################################################
method("setCurrentCoupon", "SBDuration", function(this, container='tsdb', source='internal', ...){
	needs(container="character", source="character")
	this$.currentCoupon <- TSDataLoader$getDataByName(container, 'fncl_cc_30d_yield', source)
})

method("setSwapSlope", "SBDuration", function(this, container='tsdb', source='internal', ...){
	needs(container="character", source="character")
	sr2  <- TSDataLoader$getDataByName(container, 'irs_usd_rate_2y_mid', source)
	sr10 <- TSDataLoader$getDataByName(container, 'irs_usd_rate_10y_mid', source)
	this$.swapSlope <- (sr10 - sr2)
})

method("couponDurations", "SBDuration", function(this, coupons, range=NULL, verbose=FALSE, ...){
	needs (coupons="numeric|integer", range="Range?")
	failIf(is.null(this$.currentCoupon), 'Current coupon not set.')
	failIf(is.null(this$.swapSlope), 'Swap slope not set.')
	
	mergeZoo <- merge(this$.currentCoupon, this$.swapSlope, all=FALSE)
	if(!is.null(range)) mergeZoo <- range$cut(mergeZoo)
	failIf((NROW(mergeZoo)==0),'No dates for range.')
	cc.zoo <- mergeZoo[,1]
	slope.zoo <- mergeZoo[,2]
	all.dates <- index(mergeZoo)
	
	result.matrix <- NULL
	for (x in 1:length(all.dates)){
		runDate <- all.dates[[x]]
		if(verbose) print(squish('Calculating durations for : ',format(runDate,'%Y-%m-%d')))
		otm <- cc.zoo[[x]] - coupons
		result <- this$durations(slope.zoo[[x]], otm)
		result.matrix <- rbind(result.matrix, result) 
	}
	result.zoo <- zoo(result.matrix, all.dates)
	colnames(result.zoo) <- coupons
	return(result.zoo)
})

method("uploadDurations", "SBDuration", function(this, durationZoo, uploadMethod='file', uploadFilename=NULL, uploadPath=NULL, ...){
	needs(durationZoo='zoo', uploadPath='character?', uploadMethod='character?', uploadFilename='character?')
		
	if(is.null(uploadPath)) uploadPath <- tsdbUploadDirectory()
	tsdbNames = paste(this$.program,format(as.numeric(colnames(durationZoo)),nsmall=1),'1n','dv01',sep="_")
	
	uploadZooToTsdb(durationZoo, tsdbNames=tsdbNames, tsdbSources=this$.model, uploadMethod=uploadMethod, 
									uploadFilename=uploadFilename, uploadPath=uploadPath)
})
