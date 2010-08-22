constructor("SlippageCurves", function(curves = NULL, slippage = rep(1.0, length(curves))) {
    this <- extend(RObject(), "SlippageCurves", .curves = curves,.slippage = slippage)
    constructorNeeds(this, curves="list(PositionEquityCurve)", slippage="slippage|numeric")
    if(inStaticConstructor(this)) return(this)
    
    this
})

method(".zoos", "SlippageCurves", function(this, ...) {
    lapply(this$.curves, as.zoo)
})

method("slippageAdjustedCurves", "SlippageCurves", function(this, ...) {
    rawzoos <- this$.zoos()
	slippedzoos <- rawzoos
	for (zooIndex in 1:length(rawzoos))
		slippedzoos[[zooIndex]] <- this$slippageFunction(this$.slippage[[zooIndex]],rawzoos[[zooIndex]])
	
	lapply(slippedzoos, function(z) {
			colnames(z) <- c("pnl", "position")
			z[, "position"] <- NA
			PositionEquityCurve(ZooCurveLoader(z, "weighted"))})
})

method("slippageFunction","SlippageCurves",function(this,slippage,startingZoo,...) {
	endingZoo <- startingZoo - slippage * abs(startingZoo) 	
})



