# Regression class
# 
# Author: RSheftel
###############################################################################

constructor("Regression", function(){
	this <- extend(RObject(), "Regression")
		if (inStaticConstructor(this)) return(this)
})

method("exponentialWeights", "Regression", function(static, totalPeriods, halfLife, maxPeriods=NULL, ...){
	needs(totalPeriods="numeric", halfLife="numeric", maxPeriods="numeric?")
	
	lambda <- 0.5^(1/halfLife)
	weights <- lambda^(0:(totalPeriods-1))
	if (!is.null(maxPeriods))	weights <- weights * c(rep(1,min(maxPeriods,length(weights))), rep(0,max(0,length(weights)-maxPeriods)))
		
	return(weights)
})
