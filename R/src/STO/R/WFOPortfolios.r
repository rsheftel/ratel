applyEqualWeights <- function(wfo,weightsData){
	if(all(!is.na(weightsData))){
		for(step in wfo$steps()) weightsData[step,-1] <- weightsData[step,-1]/sum(weightsData[step,-1])
	}else{
		print("Failure in applyEqualWeights!")
	}
	wfo$updateCsv(weightsData,wfo$.portfolioWeightsPath)
}

p.EqualWeighting <- function(wfo){
# Equally weighted portfolios across all MSIVs()
	weightsData <- wfo$readCsv(wfo$.portfolioWeightsPath)
	weightsData[,sapply(wfo$msivs(),function(x){as.character(x)})] <- 1
	applyEqualWeights(wfo,weightsData)
}

p.PositiveMedianProfit <- function(wfo,msivs){
# portfolios with positive median NetProfit MSIVs (equal weights)
	runs <- wfo$runs(); ranges <- wfo$ranges(); sto.curves <- wfo$curvesFast()
	calcMsivWeights <- function(msiv){
		weightsData <- wfo$readCsv(wfo$.portfolioWeightsPath)
		print(squish("Working on ",as.character(msiv)))
		if(any(is.na(weightsData[,as.character(msiv)]))){
			msivCurves <- NULL
			for(run in runs) msivCurves <- c(list(sto.curves$curve(msiv,run)$as.zoo()[,"pnl"]),msivCurves)
			msivCurves <- do.call(merge,msivCurves)
			msivWeights <- NULL
			msivCurvesRanges <- sapply(ranges,function(r){r$cut(msivCurves)})
			for(range in 1:NROW(ranges)){
				if(length(msivCurvesRanges[[range]]) == 0){
					medianNetProfit <- 0
				}else{
					medianNetProfit <- median(sapply(1:NROW(runs),function(x){last(cumsum(msivCurvesRanges[[range]][,x]))}))	
				}
				msivWeights <- c(msivWeights,ifelse(medianNetProfit > 0,1,0))
			}
			weightsData[,as.character(msiv)] <- msivWeights
			wfo$updateCsv(weightsData,wfo$.portfolioWeightsPath)
		}
	}
	sapply(msivs,calcMsivWeights)
	applyEqualWeights(wfo,wfo$readCsv(wfo$.portfolioWeightsPath))
}