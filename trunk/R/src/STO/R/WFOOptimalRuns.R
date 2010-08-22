or.nBestNetProfit <- function(wfo,range,msiv,nBest){
# Returns the n best runs for NetProfit 
	needs(wfo = "WFO",range = "Range",msiv = "MSIV",nBest = "numeric")
	nbBestRuns <- nBest
	metricCube <- wfo$metricsSlow(msivs = msiv,range = range)
	optiRuns <- wfo$.sto$runNumbers()[order(metricCube$values(NetProfit,msiv))]
	nRuns <- NROW(optiRuns)
	optiRuns[(nRuns-nbBestRuns+1):nRuns]	
}

or.LiqInjETF <- function(wfo,range,msiv,nBest,q,optiMetric){
# Returns the n best runs for optiMetric
# With constraint of being in the best q-quantile for NetProfit, Calmar, KRatio and PercentProfit
	needs(wfo = "WFO",range = "Range",msiv = "MSIV",nBest = "numeric",q = "numeric",optiMetric = "Metric")
	quantileFilter <- function(x,q){x >= quantile(x,q)}
	metricCube <- wfo$metricsSlow(msivs = msiv,range = range)
	netProfits <- metricCube$values(NetProfit,msiv)
	calmarRatios <- metricCube$values(CalmarRatio,msiv)
	kRatios <- metricCube$values(KRatio,msiv)
	optiMetrics <- metricCube$values(optiMetric,msiv)
	quantileFilters <- quantileFilter(netProfits,q) & quantileFilter(kRatios,q) & quantileFilter(calmarRatios,q)
	while(NROW(sort(optiMetrics[quantileFilters])) < nBest){
		q <- q - 0.05
		quantileFilters <- quantileFilter(netProfits,q) & quantileFilter(kRatios,q) & quantileFilter(calmarRatios,q)
	}
	matched <- unique(match(lastN(zoo(sort(optiMetrics[quantileFilters])),nBest),optiMetrics))			
	wfo$.sto$runNumbers()[matched]
}

or.nBestPsi <- function(wfo,range,msiv,nBest){
# Returns the n best runs for Psi 
	needs(wfo = "WFO",range = "Range",msiv = "MSIV",nBest = "numeric")
	nbBestRuns <- nBest
	metricCube <- wfo$metricsSlow(msivs = msiv,range = range)
	optiRuns <- wfo$.sto$runNumbers()[order(metricCube$values(Psi,msiv))]
	nRuns <- NROW(optiRuns)
	optiRuns[(nRuns-nbBestRuns+1):nRuns]	
}