# Collection of helper and wrapper functions for the STO process to generate reports and plots
# 
# Author: RSheftel
###############################################################################

constructor("STOUtils", function(){
	this <- extend(RObject(), "STOUtils")
	if (inStaticConstructor(this)) return(this)
	return(this)
})

####################################################################################################
#    Merge Curve Helper methods
####################################################################################################

method("msivsRunsToZoo", "STOUtils", function(static, curve.cube, msivs, runs, ...){
	needs(curve.cube='CurveCube',msivs='MSIV|list(MSIV)',runs='numeric|integer')
	failIf(((length(msivs)>1) && (length(runs)>1)),"Must be either a list of msivs and single run, or list of runs and single msiv")
	failIf(((length(msivs)==1) &&(length(runs)==1)),"Either msivs or runs must have more than one element.") 	
	
	if (length(msivs)>1) equityZoo <- static$mergeMultipleMsivsSingleRun(curve.cube, msivs, runs)
	if (length(runs)>1)  equityZoo <- static$mergeSingleMsivMultipleRuns(curve.cube, msivs, runs)	
	return(equityZoo)		
})

method("mergeMultipleMsivsSingleRun", "STOUtils", function (static, curve.cube, msivs, run, ...){
	needs(curve.cube='CurveCube',msivs='list(MSIV)',run='numeric|integer')
	
	msivCount  <- length(msivs)
	msivCounts <- c(1:msivCount)
	equityCurves <- lapply(msivs,function(msiv) curve.cube$curve(msiv,run)$equity())
	equityCurvesMerged <- do.call(merge,equityCurves)
	
	runWeights <- rep(1/msivCount,times=msivCount)
	paramMsiv <- ParameterizedMsivs(msivs, runs = rep(run,msivCount), weights = runWeights)
	averageCurve <- paramMsiv$curve(curve.cube)$equity()
	equityCurvesMerged <- cbind(equityCurvesMerged, averageCurve)
	colnames(equityCurvesMerged)=c(sapply(msivs,as.character),'Average')
	return(equityCurvesMerged)
})

method("mergeSingleMsivMultipleRuns", "STOUtils", function (static, curve.cube, msiv, runs, ...){
	needs(curve.cube='CurveCube',msiv='MSIV',runs='numeric|integer')
	numRuns <- length(runs)
	runsCounts <- c(1:(numRuns+1))
	equityCurves <- lapply(runs,function(run) curve.cube$curve(msiv,run)$equity())
	equityCurvesMerged <- do.call(merge,equityCurves)
	
	msivRepeat <- vector('list',numRuns)
	for (i in 1:numRuns){
		msivRepeat[[i]] <- msiv
	}
	
	runWeights <- rep(1/numRuns,times=numRuns)
	paramMsiv <- ParameterizedMsivs(msivRepeat, runs = runs, weights = runWeights)
	averageCurve <- paramMsiv$curve(curve.cube)$equity()
	equityCurvesMerged <- cbind(equityCurvesMerged, averageCurve)
	colnames(equityCurvesMerged) <- c(sapply(runs,as.character), "Average")
	return(equityCurvesMerged)
})

method("mergeMsivsAcrossRuns", "STOUtils", function(static, curve.cube, msivs, runs, ...){
	needs(curve.cube="CurveCube", msivs="MSIV|list(MSIV)", runs="numeric|integer")
	
	numRuns <- length(runs)
	msivCount <- length(msivs)
	equityCurves <- list()
	for (msiv in msivs){
		msivRepeat <- vector('list',numRuns)
		for (i in 1:numRuns){
			msivRepeat[[i]] <- msiv
		}
		paramMsiv <- ParameterizedMsivs(msivRepeat, runs = as.numeric(runs), weights = rep(1/numRuns,numRuns))
		equityCurves[[as.character(msiv)]] <- paramMsiv$curve(curve.cube)$equity()
	}
	
	equityCurvesMerged <- do.call(merge,equityCurves)
	averageCurve <- accumulate("+",equityCurves) / msivCount
	equityCurvesMerged <- cbind(equityCurvesMerged, averageCurve)
	colnames(equityCurvesMerged)=c(sapply(msivs,as.character),'Average')
	names(index(equityCurvesMerged)) <- NULL
	return(equityCurvesMerged)
})

####################################################################################################
#    General methods
####################################################################################################

method("portfolioMatrix", "STOUtils", function (static, sto, portfolioName, ...){
#Get the underlying markets and weights from a portfolio name
	portfolioMsiv <- sto$msiv(portfolioName)
	portfolioMarkets <- portfolioMsiv$.weights$keys()  #Get the MSIVs from the portfolio object
	portfolioWeights <- portfolioMsiv$.weights$values()  #Get the weights
	portfolioMatrix <- cbind(sapply(portfolioMarkets,market),portfolioWeights)
	return(portfolioMatrix)
})

method("getMetricParameterSurface", "STOUtils", function(static, sto, metric.cube, msiv, metricList, ...){
	needs(sto="STO", metric.cube="MetricCube", msiv="MSIV", metricList="list(Metric)")
	#Create the parameter space dataframe
	params <- sto$parameters()
	paramFrame <- params$data()
	
	#Load the runs
	runNumbers <- sto$runNumbers()
	
	#Load the metrics
	metricFrame <- sapply(metricList,function(met) metric.cube$values(met,msiv))
	
	#Put on the headers
	colnames(metricFrame) <- sapply(metricList, as.character)
	#metric <- metric.cube$values(TSNetProfit,msiv)
	
	#Smash to a metric.plane
	rowMatch <- match(runNumbers,paramFrame[,1])
	metricSurface <- cbind(paramFrame[rowMatch,],metricFrame)
	
	#Sort it by runNumbers
	metricSurface <- metricSurface[order(runNumbers,decreasing=FALSE),]
	return(metricSurface)
})

method("filterMetricParameterSurface", "STOUtils", function(static, metricParameterSurface, metricList, lowerBounds, upperBounds, ...){
	needs(metricParameterSurface="data.frame", metricList="Metric|list(Metric)", lowerBounds="numeric|integer", upperBounds="numeric|integer")
	failIf(!(length(metricList)==length(upperBounds)&(length(upperBounds)==length(lowerBounds))),"All input lists and vectors must be same length!")
	if(!any(is(metricList)=='list')) metricList <- list(metricList)
	goodRuns <- rep(TRUE,NROW(metricParameterSurface))	
	for (metricCount in 1:length(metricList)){
		metricName <- as.character(metricList[[metricCount]])
		failIf(!any(metricName==colnames(metricParameterSurface)),squish("Metric : ",metricName," : not in the surface!"))
		goodRuns <- goodRuns & (metricParameterSurface[,metricName] > lowerBounds[metricCount])
		goodRuns <- goodRuns & (metricParameterSurface[,metricName] < upperBounds[metricCount])
	}
	return(metricParameterSurface[goodRuns,])
})

method("simultaneousTradingForRun", "STOUtils", function (static,curve.cube,msivs,run,overlappingOnly = TRUE,...){
	STOUtils$simultaneousTrading(curve.cube,msivs,rep(run,length(msivs)),overlappingOnly)
})

method("simultaneousTrading", "STOUtils", function (static,curve.cube,msivs,runs,overlappingOnly = TRUE,...){
	checkLength(runs, length(msivs))	
	positionZoos <- lapply(1:length(msivs),function(x)curve.cube$curve(msivs[[x]],runs[[x]])$position())
	STOUtils$.simultaneousTrading(positionZoos,overlappingOnly)
})

method(".simultaneousTrading", "STOUtils", function (static,positionZoos,overlappingOnly,...){
	needs(positionZoos='list(zoo)',overlappingOnly='logical')
	
	mergedPositions <- do.call(merge,positionZoos)
	if(overlappingOnly) mergedPositions <- na.omit(mergedPositions)
	if(NROW(mergedPositions)==0)return(NULL)
	isInTradeZoo <- mergedPositions
	for(i in 1:NCOL(mergedPositions)){
		if(!overlappingOnly)isInTradeZoo[,i][is.na(isInTradeZoo[,i])] <- 0
		clumn <- isInTradeZoo[,i] < 0
		if(any(clumn)) isInTradeZoo[clumn,i] <- -1
		clumn <- isInTradeZoo[,i] > 0
		if(any(clumn)) isInTradeZoo[clumn,i] <- 1			
	}
	isInTradeZooPosList <- lapply(1:NCOL(isInTradeZoo),function(x)abs(isInTradeZoo[,x]))
	isInTradeZooSum <- sumZoos(isInTradeZooPosList)			
	marketsTraded <- NULL
	marketsInTrade <- 0:max(isInTradeZooSum)
	for(i in marketsInTrade) marketsTraded <- c(marketsTraded,NROW(isInTradeZooSum[isInTradeZooSum==i]))
	data.frame(
			MSIVsTraded = marketsInTrade,
			NumberOfBars = marketsTraded,
			PercentOfTradedBars = c(NA,(marketsTraded/NROW(isInTradeZooSum[isInTradeZooSum!=0]))[-1]),
			PercentOfTotalBars = marketsTraded/NROW(isInTradeZooSum)
	)
})