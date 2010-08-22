# Collection of helper and wrapper functions for the STO process to generate reports 
# 
# Author: RSheftel
###############################################################################

constructor("MetricFrame", function(){
	this <- extend(RObject(), "MetricFrame")
	if (inStaticConstructor(this)) return(this)
	return(this)
})

####################################################################################################
#    Report methods
####################################################################################################

method("multipleMsivsSingleRun", "MetricFrame", function (static, curve.cube, msivs, run, metrics, ...){
#report equity curve multiple msivs for a single run
	equityCurves <- STOUtils$mergeMultipleMsivsSingleRun(curve.cube, msivs, run)
	
	reportFrame <- data.frame()
	for (metricCount in 1:length(metrics)){
		for (msivCount in 1:ncol(equityCurves)){
			reportFrame[msivCount,metricCount] <- ZooCurveLoader$fromEquity(na.omit(equityCurves[,msivCount]),'tempZoo')$metric(metrics[[metricCount]])
		}
	} 
	colnames(reportFrame) <- sapply(metrics, function(met) as.character(met))
	rownames(reportFrame) <- colnames(equityCurves)
	return(reportFrame)
})

method("singleMsivMultipleRuns", "MetricFrame", function (static, curve.cube, msiv, runs, metrics, ...){
#report equity curve single msiv for multiple runs
	equityCurves <- STOUtils$mergeSingleMsivMultipleRuns(curve.cube, msiv, runs)
	
	reportFrame <- data.frame()
	for (metricCount in 1:length(metrics)){
		for (msivCount in 1:ncol(equityCurves)){
			reportFrame[msivCount,metricCount] <- ZooCurveLoader$fromEquity(na.omit(equityCurves[,msivCount]),'tempZoo')$metric(metrics[[metricCount]])
		}
	} 
	colnames(reportFrame) <- sapply(metrics, function(met) as.character(met))
	rownames(reportFrame) <- colnames(equityCurves)
	return(reportFrame)
})
