#
#	Metric Class and all supporting functions
###########################################################################################

constructor("Metric", function(name = NULL, calculate = NULL) {
    this <- extend(RObject(), "Metric", .name = name, .calculate = calculate, .lookup = NULL)
    constructorNeeds(this, name="character", calculate = "function")
    if(inStaticConstructor(this))
        this$.lookup <- Map("character", "Metric")
    else
        Metric$.addToLookup(this)
    this
})

pnl <- function(equity) { equity - c(0, equity[-length(equity)]) }

method(".addToLookup", "Metric", function(staticThis, metric, ...) {
    failIf(staticThis$.lookup$has(metric$.name), metric$.name, " already defined.  Use Metric$fetch(", metric$.name, ") to retrieve.")
    staticThis$.lookup$set(metric$.name, metric)
})

method("fetch", "Metric", function(staticThis, metricName, ...) {
    failIf(is.null(staticThis$.lookup), "cannot call fetch() on a Metric instance.  Use Metric$fetch() instead.")
    failUnless(staticThis$.lookup$has(metricName), metricName, " not defined.")
    staticThis$.lookup$fetch(metricName)
})

method("all", "Metric", function(staticThis, ...) {
    failIf(is.null(staticThis$.lookup), "cannot call all() on a Metric instance.  Use Metric$all() instead.")
    staticThis$.lookup$values()
})

method("as.character", "Metric", function(this, ...) {
    this$.name
})

method("calculate", "Metric", function(this, msiv, getMetricValues, getCurves, ...) {
    needs(msiv="MSIV", getMetricValues="function", getCurves="function")
    this$.calculate(this, msiv, getMetricValues, getCurves)
})

method(".valueFunc", "Metric", function(this, getMetricValues, ...) {
    function(msiv) getMetricValues(this, msiv)
})

metricNA <- function(metric, msiv, getMetricValues, getCurves) { as.numeric(NA) }

metricWeightedSum <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$scale(metric$.valueFunc(getMetricValues), "numeric")$values()
    accumulate('+', values)
}

metricSum <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$pick(metric$.valueFunc(getMetricValues), "numeric")$values()
    accumulate('+', values)
}

metricMax <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$pick(metric$.valueFunc(getMetricValues), "numeric")$values()
    apply(data.frame(values), 1, max)
}

metricMin <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$pick(metric$.valueFunc(getMetricValues), "numeric")$values()
    apply(data.frame(values), 1, min)
}

metricWeightedMax <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$scale(metric$.valueFunc(getMetricValues), "numeric")$values()
    apply(data.frame(values), 1, max)
}

metricWeightedMin <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$scale(metric$.valueFunc(getMetricValues), "numeric")$values()
    apply(data.frame(values), 1, min)
}

metricMean <- function(metric, msiv, getMetricValues, getCurves) { 
    needs(metric="Metric", msiv="Portfolio", getMetricValues="function", getCurves="function")
    values <- msiv$pick(metric$.valueFunc(getMetricValues), "numeric")$values()
    apply(data.frame(values), 1, mean)
}

metricTSNetProfit <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSNetProfit))

    getMetricValues(TSOpenPositionProfit, msiv) + getMetricValues(TSClosePositionProfit, msiv)
}

curveCalcFunc <- function(func) {
    function(metric, msiv, getMetricValues, getCurves) {
        needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
        Progress$start("C", 10)
        # numeric vector net profit parallel to runs
        sapply(getCurves(msiv), function(curve) { progress("C"); func(curve) })
    }
}

pnlCurveCalcFunc <- function(func) {
    curveCalcFunc(function(curve) { func(pnl(curve)) })
}

###########################################################################################
#	Supporting metric calculation functions
###########################################################################################

yearsInZoo <- function(z){	
	as.numeric(as.POSIXct(index(z)[NROW(z)]) - as.POSIXct(index(z)[1])) / 365.25	
}

annualizationFactor <- function(z){
	sqrt(length(z)/yearsInZoo(z))
}

annualizedNetProfit <- function(curve){
    pnl <- curve$pnl()
    assert(length(pnl) > 0, "must have at least one observation")
	yearsSinceInception <- yearsInZoo(pnl)    
    sum(pnl)/yearsSinceInception
}

monthlyStandardDeviation <- function(curve) {
	pnl <- curve$pnl()
	assert(length(pnl) > 0, "must have at least one observation")
	if (length(pnl) < 20) cat("Warning: Calculation does not have enough data.\n")	
	monthly <- Interval$MONTHLY$collapse(pnl, sum)
	sd(monthly)
}

weeklyStandardDeviation <- function(curve) {
	pnl <- curve$pnl()
	assert(length(pnl) > 0, "must have at least one observation")	
	if (length(pnl) < 5) cat("Warning: Calculation does not have enough data.\n")
	weekly <- Interval$WEEKLY$collapse(pnl, sum)
    sd(weekly)
}

dailyStandardDeviation <- function(curve) {
	pnl <- curve$pnl()	
	assert(length(pnl) > 0, "must have at least one observation.\n")
	dailyPnl <- Interval$DAILY$collapse(pnl, sum)
	sd(dailyPnl)
}

maxDrawDownMonthly <- function(curve) {
	pnl <- curve$pnl()
	assert(length(pnl) > 0, "must have at least one observation")	
	if (length(pnl) < 20) cat("Warning: Calculation does not have enough data.\n")
	monthlypnl <- Interval$MONTHLY$collapse(pnl, sum)
	equity <- cumsum(monthlypnl)
	min(equity - cummax(equity))
}

maxDrawDownWeekly <- function(curve) {
	pnl <- curve$pnl()
	assert(length(pnl) > 0, "must have at least one observation")	
	if (length(pnl) < 20) cat("Warning: Calculation does not have enough data.\n")
	weeklypnl <- Interval$WEEKLY$collapse(pnl, sum)
	equity <- cumsum(weeklypnl)
	min(equity - cummax(equity))
}

downSideDeviation <- function(curve) {
	pnl <- curve$pnl()
	assert(length(pnl) > 0, "must have at least one observation.\n")
	semiDeviation(as.vector(pnl))	
}

maxDrawDown <- function(pnl) {
    equity <- cumsum(pnl)
    min(equity - cummax(equity))
}
                                          
drawdowns <- function(equity) {
    # We must "un-zoo" equity curve, otherwise shifting the vector over by one does not work.
    equity <- as.vector(equity)
    # remove run-ups
    diff <- cummax(equity) - equity
    equity <- equity[!((diff == 0) & c((diff == 0)[-1], FALSE))]

    # build groups
    zeros <- rep(0, length(equity))
    diff <- cummax(equity) - equity
    zeros[which(diff==0)] <- which(diff==0)

    # find drawdowns
    drawdowns <- split(diff, cummax(zeros))
    if(length(drawdowns) > 0 && length(last(drawdowns)) == 1) # last point is a new high water mark
        drawdowns <- drawdowns[-length(drawdowns)]
    drawdowns
}

averageDrawDown <- function(pnl) {
    equity <- cumsum(pnl)
    drawdowns <- drawdowns(equity)
    if (length(drawdowns) == 0) return(0)

    -mean(sapply(drawdowns, max))
}

tenPercentileDrawDown <- function(pnl) {
	equity <- cumsum(pnl)
	drawdowns <- drawdowns(equity)
	if (length(drawdowns) ==0) return(0)
	
	drawdowns <- -1*sapply(drawdowns,max)
	n = length(drawdowns)
	alpha = 0.1
	if (n < 10) return(0)
	n.alpha = floor(n * alpha)
	ans = as.vector(sort(drawdowns)[n.alpha])
}

conditionalTenPercentileDrawDown <- function(pnl) {
	equity <- cumsum(pnl)
	drawdowns <- drawdowns(equity)
	if (length(drawdowns) ==0) return(0)
	drawdowns <- -1*sapply(drawdowns,max)
	n = length(drawdowns)
	alpha = 0.1
	if (n < 10) return(0)
	n.alpha = floor(n * alpha)
	ans = as.vector(mean(sort(drawdowns)[1:n.alpha]))
}

twentyPercentileDrawDown <- function(pnl) {
	equity <- cumsum(pnl)
	drawdowns <- drawdowns(equity)
	if (length(drawdowns) ==0) return(0)
	
	drawdowns <- -1*sapply(drawdowns,max)
	n = length(drawdowns)
	alpha = 0.2
	if (n < 5) return(0)
	n.alpha = floor(n * alpha)
	ans = as.vector(sort(drawdowns)[n.alpha])
}

conditionalTwentyPercentileDrawDown <- function(pnl) {	
	#print(class(pnl))
	equity <- cumsum(pnl)
	drawdowns <- drawdowns(equity)
	if (length(drawdowns) ==0) return(0)
	drawdowns <- -1*sapply(drawdowns,max)
	n = length(drawdowns)
	alpha = 0.2
	if (n < 5) return(0)
	n.alpha = floor(n * alpha)
	ans = as.vector(mean(sort(drawdowns)[1:n.alpha]))
}

conditionalTenPercentileDailyVaR <- function(pnl) {
	alpha = 0.1
	dailyPnl <- Interval$DAILY$collapse(pnl, sum)
	ans = as.numeric(expectedShortFallVaR(dailyPnl,1,alpha))
}

conditionalFivePercentileDailyVaR <- function(pnl) {
	alpha = 0.05
	dailyPnl <- Interval$DAILY$collapse(pnl, sum)
	ans = as.numeric(expectedShortFallVaR(dailyPnl,1,alpha))
}

conditionalOnePercentileDailyVaR <- function(pnl) {
	alpha = 0.01
	dailyPnl <- Interval$DAILY$collapse(pnl, sum)
	ans = as.numeric(expectedShortFallVaR(dailyPnl,1,alpha))
}

averageDrawDownTime <- function(pnl) {
    equity <- cumsum(pnl)
    drawdowns <- drawdowns(equity)
    if (length(drawdowns) == 0) return(0)
    if (last(equity) != max(equity))
        length(drawdowns) <- length(drawdowns) - 1
    if (length(drawdowns) == 0) return(0)
    mean(sapply(drawdowns, length))
}

averageDrawDownRecoveryTime <- function(pnl) {
    equity <- cumsum(pnl)
    drawdowns <- drawdowns(equity)
    if (length(drawdowns) == 0) return(0)
    if (last(equity) != max(equity))
        length(drawdowns) <- length(drawdowns) - 1
    if (length(drawdowns) == 0) return(0)
    recoveryTime <- function(drawdown) {
        firstTrough <- first(which(drawdown == max(drawdown)))
        length(drawdown) - firstTrough + 1
    }
    mean(sapply(drawdowns, recoveryTime))
}

kRatio <- function(pnl) {
    assert(length(pnl) > 0, "must have at least one observation")
    if (all(pnl == 0)) return(0) # special case
    
    equity <- cumsum(pnl)
    bars <- 1:length(equity)
    regression <- summary(lm(equity ~ bars))
    b1 <- regression$coefficients[[2,1]]
    stdError <- regression$coefficients[[2,2]]
    b1 / (stdError * sqrt(length(bars)))
}

kappaRatio <- function(pnl, moment, threshold=0) {
#http://www.quadrafund.com/TWiki/pub/Research/PerformanceMetrics/KappaADownsideRisk.pdf	
	needs(moment = 'numeric',threshold='numeric')
	assert(length(pnl) > 0, "must have at least one observation")
	if (all(pnl == 0)) return(0) # special case	
	
	pnl.vector <- as.vector(pnl)
	return((mean(pnl.vector)-threshold) / lowerPartialMoment(pnl.vector,moment,threshold)^(1/moment))
}

sortinoRatio <- function(pnl, MAR=0){
#http://www.quadrafund.com/TWiki/pub/Research/PerformanceMetrics/KappaADownsideRisk.pdf	
	return(kappaRatio(pnl,2,MAR) * annualizationFactor(pnl))
}

omegaRatio <- function(pnl, MAR=0){
#http://www.quadrafund.com/TWiki/pub/Research/PerformanceMetrics/KappaADownsideRisk.pdf
	return(kappaRatio(pnl,1,MAR)+1)
}

upsidePotentialRatio <- function(pnl, MAR=0){
#http://www.quadrafund.com/TWiki/pub/Research/PerformanceMetrics/Upside_Potential_Ratio.mht
	needs(MAR='numeric')
	assert(length(pnl) > 0, "must have at least one observation")
	if (all(pnl == 0)) return(0) # special case

	pnl.vector <- as.vector(pnl)
	return(semiDeviation(pnl.vector,moment=1,0,'upside')/semiDeviation(pnl.vector,moment=2,0,'downside'))			
}

metricTSExpectancy <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, TSExpectancy))
	
	probability.win <- getMetricValues(TSPercentProfit, msiv)
	average.loss <- getMetricValues(TSAvgLossTrade, msiv)
	
	((getMetricValues(TSAvgWinTrade, msiv) * probability.win) +  (average.loss * (1-probability.win)))/abs(average.loss)
}

metricTSExpectancyScore <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, TSExpectancyScore))
	
	totalBars <- NROW(first(getCurves(msiv))$pnl())
	
	
	getMetricValues(TSExpectancy, msiv) * (getMetricValues(TSTotalTrades, msiv)/totalBars)
}

metricCalmarRatio <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, CalmarRatio))

    result <- getMetricValues(AnnualizedNetProfit, msiv) / abs(getMetricValues(MaxDrawDown, msiv))
	result[is.na(result)] <- 0
	return(result)	
}

metricSharpeRatioDaily <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, SharpeRatioDaily))
	getMetricValues(AnnualizedNetProfit, msiv) / getMetricValues(DailyStandardDeviation, msiv) / sqrt(252)
}

metricSharpeRatioWeekly <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, SharpeRatioWeekly))
	getMetricValues(AnnualizedNetProfit, msiv) / getMetricValues(WeeklyStandardDeviation, msiv) / sqrt(52)
}

metricSharpeRatioMonthly <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, SharpeRatioMonthly))
	getMetricValues(AnnualizedNetProfit, msiv) / getMetricValues(MonthlyStandardDeviation, msiv) / sqrt(12)
}

metricCalmarRatioWeekly <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, CalmarRatioWeekly))
	 getMetricValues(AnnualizedNetProfit, msiv) / abs(getMetricValues(MaxDrawDownWeekly, msiv))
}
metricCalmarRatioMonthly <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, CalmarRatioMonthly))
	getMetricValues(AnnualizedNetProfit, msiv) / abs(getMetricValues(MaxDrawDownMonthly, msiv))
}

metricConditionalTwentyPercentileCalmarRatio <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, ConditionalTwentyPercentileCalmarRatio))	
	getMetricValues(AnnualizedNetProfit, msiv) / abs(getMetricValues(ConditionalTwentyPercentileDrawDown, msiv))
}

metricConditionalTenPercentileCalmarRatio <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, ConditionalTenPercentileCalmarRatio))
	getMetricValues(AnnualizedNetProfit, msiv) / abs(getMetricValues(ConditionalTenPercentileDrawDown, msiv))
}

lessTransactionCosts <- function(baseMetric, transactionCost) {
    needs(baseMetric="Metric", transactionCost="numeric")
    metricFunction <- function(metric, msiv, getMetricValues, getCurves) {
        needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")

        getMetricValues(baseMetric, msiv) - getMetricValues(TSTotalTrades, msiv) * transactionCost
    }
    Metric(squish(baseMetric$as.character(), "LessTransactionCosts", transactionCost), metricFunction)
}

percentile <- function(baseMetric, smallerBetter=FALSE) {
    needs(baseMetric="Metric", smallerBetter="logical")
    metricFunction <- function(metric, msiv, getMetricValues, getCurves) {
        needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")

        values <- getMetricValues(baseMetric, msiv)
        if(smallerBetter) values <- -values
        rank(values)/length(values)*100
    }
    Metric(squish("Percentile", baseMetric$as.character()), metricFunction)
}

metricTSAvgWinTrade <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSAvgWinTrade))

    result <- getMetricValues(TSGrossProfit, msiv) / getMetricValues(TSNumWinTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSAvgLossTrade <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSAvgLossTrade))

    result <- getMetricValues(TSGrossLoss, msiv) / getMetricValues(TSNumLosTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSWinLossRatio <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSWinLossRatio))

    result <- getMetricValues(TSAvgWinTrade, msiv) / (-1 * getMetricValues(TSAvgLossTrade, msiv))
	result[is.na(result)] <- 0
	return(result)
}

metricTSAverageTrade <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSAverageTrade))

    result <- getMetricValues(TSNetProfit, msiv) / getMetricValues(TSTotalTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricNetProfitMultMaxDrawdown <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, NetProfitMultMaxDrawdown))

    result <- getMetricValues(NetProfit, msiv) / (-1 * getMetricValues(MaxDrawDown, msiv))
    result[is.na(result)] <- 0
    result
}

metricTSLargestLossMultAvgLoss <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSLargestLossMultAvgLoss))

    result <- getMetricValues(TSLargestLosTrade, msiv) / getMetricValues(TSAvgLossTrade, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSLargestLossPerGrossLoss <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSLargestLossPerGrossLoss))

    result <- getMetricValues(TSLargestLosTrade, msiv) / getMetricValues(TSGrossLoss, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSLargestWinMultAvgWin <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSLargestWinMultAvgWin))

    result <- getMetricValues(TSLargestWinTrade, msiv) / getMetricValues(TSAvgWinTrade, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSLargestWinPerGrossProfit <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSLargestWinPerGrossProfit))

    result <- getMetricValues(TSLargestWinTrade, msiv) / getMetricValues(TSGrossProfit, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSLargestWinPerNetProfit <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSLargestWinPerNetProfit))

    result <- getMetricValues(TSLargestWinTrade, msiv) / getMetricValues(TSNetProfit, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricTSProfitFactor <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSProfitFactor))

    result <- getMetricValues(TSGrossProfit, msiv) / (-1 * getMetricValues(TSGrossLoss, msiv))
	result[is.na(result)] <- 0
	result[is.infinite(result)] <- getMetricValues(TSGrossProfit, msiv)[is.infinite(result)]
	return(result)
}

metricTSPercentProfit <- function(metric, msiv, getMetricValues, getCurves) {
    needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
    assert(equals(metric, TSPercentProfit))

    result <- getMetricValues(TSNumWinTrades, msiv) / getMetricValues(TSTotalTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricQWinningTradesPct <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, QWinningTradesPct))
	
	result <- getMetricValues(QWinningTrades, msiv) / getMetricValues(QTotalFinishedTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricQLosingTradesPct <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, QLosingTradesPct))
	
	result <- getMetricValues(QLosingTrades, msiv) / getMetricValues(QTotalFinishedTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricQAverageProfit <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, QAverageProfit))
	
	result <- getMetricValues(QRealizedNetProfit, msiv) / getMetricValues(QTotalFinishedTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricQAverageWin <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, QAverageWin))
	
	result <- getMetricValues(QRealizedGrossProfit, msiv) / getMetricValues(QWinningTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricQAverageLoss <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, QAverageLoss))
	
	result <- getMetricValues(QRealizedGrossLoss, msiv) / getMetricValues(QLosingTrades, msiv)
	result[is.na(result)] <- 0
	return(result)
}

metricPsi <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, Psi))

	scale <- 1
	scale <- scale * approx(c(0.5, 4),0:1,xout=getMetricValues(KRatio, msiv),method="linear",rule=2)$y
	scale <- scale * approx(c(0.3, 1.2),0:1,xout=getMetricValues(ConditionalTwentyPercentileCalmarRatio, msiv),method="linear",rule=2)$y
	scale <- scale * approx(c(1.05, 1.25),0:1,xout=getMetricValues(OmegaRatio, msiv),method="linear",rule=2)$y
	scale <- scale * approx(c(0.5, 5),0:1,xout=getMetricValues(SortinoRatio, msiv),method="linear",rule=2)$y
	
	(getMetricValues(NetProfit, msiv) * scale)
}

metricPsiNu <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, PsiNu))
	
	first <- approx(x = c(2,8), y = c(0,1), xout = getMetricValues(KRatio, msiv), rule = 2)$y
	second <- approx(x = c(1.5,4), y = c(0,1), xout = getMetricValues(ConditionalTwentyPercentileCalmarRatio, msiv),rule=2)$y
	third <- approx(x = c(1.1,1.3), y = c(0,1), xout = getMetricValues(OmegaRatio, msiv),rule=2)$y
	fourth <- approx(x = c(1.75,4), y = c(0,1), xout = getMetricValues(SortinoRatio, msiv),rule=2)$y
	pnl <- getMetricValues(AnnualizedNetProfit, msiv)
	
	return(pnl*(0.3*first + 0.3*second + 0.2*third + 0.2 * fourth))
}

###########################################################################################
#	Tradestation build in metrics
###########################################################################################

TSAvgBarsEvenTrade <- Metric("TSAvgBarsEvenTrade", metricMean)
TSAvgBarsLosTrade <- Metric("TSAvgBarsLosTrade", metricMean)
TSAvgBarWinTrade <- Metric("TSAvgBarWinTrade", metricMean)
TSGrossLoss <- Metric("TSGrossLoss", metricWeightedSum)
TSGrossProfit <- Metric("TSGrossProfit", metricWeightedSum)
TSLargestLosTrade <- Metric("TSLargestLosTrade", metricWeightedMin)
TSLargestWinTrade <- Metric("TSLargestWinTrade", metricWeightedMax)
TSMaxConsecLosers <- Metric("TSMaxConsecLosers", metricMax)
TSMaxConsecWinners <- Metric("TSMaxConsecWinners", metricMax)
TSMaxContractsHeld <- Metric("TSMaxContractsHeld", metricNA)
TSClosePositionProfit <- Metric("TSClosePositionProfit", metricWeightedSum)
TSNumEvenTrades <- Metric("TSNumEvenTrades", metricSum)
TSNumLosTrades <- Metric("TSNumLosTrades", metricSum)
TSNumWinTrades <- Metric("TSNumWinTrades", metricSum)
TSPercentProfit <- Metric("TSPercentProfit", metricTSPercentProfit)
TSTotalTrades <- Metric("TSTotalTrades", metricSum)
TSTotalBarsEvenTrades <- Metric("TSTotalBarsEvenTrades", metricSum)
TSTotalBarsLosTrades <- Metric("TSTotalBarsLosTrades", metricSum)
TSTotalBarsWinTrades <- Metric("TSTotalBarsWinTrades", metricSum)
TSOpenPositionProfit <- Metric("TSOpenPositionProfit", metricWeightedSum)
TSMaxIDDrawDown <- Metric("TSMaxIDDrawDown", metricNA)
TSNetProfit <- Metric("TSNetProfit", metricTSNetProfit)
TSAvgWinTrade <- Metric("TSAvgWinTrade", metricTSAvgWinTrade)
TSAvgLossTrade <- Metric("TSAvgLossTrade", metricTSAvgLossTrade)
TSWinLossRatio <- Metric("TSWinLossRatio", metricTSWinLossRatio)
TSAverageTrade <- Metric("TSAverageTrade", metricTSAverageTrade)
TSLargestLossMultAvgLoss <- Metric("TSLargestLossMultAvgLoss", metricTSLargestLossMultAvgLoss)
TSLargestLossPerGrossLoss <- Metric("TSLargestLossPerGrossLoss", metricTSLargestLossPerGrossLoss)
TSLargestWinMultAvgWin <- Metric("TSLargestWinMultAvgWin", metricTSLargestWinMultAvgWin)
TSLargestWinPerGrossProfit <- Metric("TSLargestWinPerGrossProfit", metricTSLargestWinPerGrossProfit)
TSLargestWinPerNetProfit <- Metric("TSLargestWinPerNetProfit", metricTSLargestWinPerNetProfit)
TSProfitFactor <- Metric("TSProfitFactor", metricTSProfitFactor)
TSExpectancy <- Metric("TSExpectancy", metricTSExpectancy)
TSExpectancyScore <- Metric("TSExpectancyScore", metricTSExpectancyScore)


###########################################################################################
#	Quantys metrics
###########################################################################################
NetProfit <- Metric("NetProfit", pnlCurveCalcFunc(sum))
NetProfitPerBar <- Metric("NetProfitPerBar", pnlCurveCalcFunc(mean))
StandardDeviationPnl <- Metric("StandardDeviationPnl", pnlCurveCalcFunc(sd))
MaxDrawDown <- Metric("MaxDrawDown", pnlCurveCalcFunc(maxDrawDown))
NetProfitMultMaxDrawdown <- Metric("NetProfitMultMaxDrawdown", metricNetProfitMultMaxDrawdown)
AverageDrawDown <- Metric("AverageDrawDown", pnlCurveCalcFunc(averageDrawDown))
AverageDrawDownTime <- Metric("AverageDrawDownTime", pnlCurveCalcFunc(averageDrawDownTime))
AverageDrawDownRecoveryTime <- Metric("AverageDrawDownRecoveryTime", pnlCurveCalcFunc(averageDrawDownRecoveryTime))
TenPercentileDrawDown <- Metric("TenPercentileDrawDown",pnlCurveCalcFunc(tenPercentileDrawDown))
ConditionalTenPercentileDrawDown <- Metric("ConditionalTenPercentileDrawDown",pnlCurveCalcFunc(conditionalTenPercentileDrawDown))
TwentyPercentileDrawDown <- Metric("TwentyPercentileDrawDown",pnlCurveCalcFunc(twentyPercentileDrawDown))
ConditionalTwentyPercentileDrawDown <- Metric("ConditionalTwentyPercentileDrawDown",pnlCurveCalcFunc(conditionalTwentyPercentileDrawDown))
KRatio <- Metric("KRatio", pnlCurveCalcFunc(kRatio))
SortinoRatio <- Metric("SortinoRatio", pnlCurveCalcFunc(sortinoRatio))
OmegaRatio <- Metric("OmegaRatio", pnlCurveCalcFunc(omegaRatio))
UpsidePotentialRatio <- Metric("UpsidePotentialRatio", pnlCurveCalcFunc(upsidePotentialRatio))
AnnualizedNetProfit <- Metric("AnnualizedNetProfit", curveCalcFunc(annualizedNetProfit))
CalmarRatio <- Metric("CalmarRatio", metricCalmarRatio)
DailyStandardDeviation <- Metric("DailyStandardDeviation",curveCalcFunc(dailyStandardDeviation))
DownSideDeviation <- Metric("DownSideDeviation",curveCalcFunc(downSideDeviation))
WeeklyStandardDeviation <- Metric("WeeklyStandardDeviation",curveCalcFunc(weeklyStandardDeviation))
MonthlyStandardDeviation <- Metric("MonthlyStandardDeviation",curveCalcFunc(monthlyStandardDeviation))
MaxDrawDownWeekly <- Metric("MaxDrawDownWeekly",curveCalcFunc(maxDrawDownWeekly))
MaxDrawDownMonthly <- Metric("MaxDrawDownMonthly",curveCalcFunc(maxDrawDownMonthly))
CalmarRatioWeekly <- Metric("CalmarRatioWeekly",metricCalmarRatioWeekly)
CalmarRatioMonthly <- Metric("CalmarRatioMonthly",metricCalmarRatioMonthly)
SharpeRatioDaily <- Metric("SharpeRatioDaily",metricSharpeRatioDaily)
SharpeRatioWeekly <- Metric("SharpeRatioWeekly",metricSharpeRatioWeekly)
SharpeRatioMonthly <- Metric("SharpeRatioMonthly",metricSharpeRatioMonthly)
ConditionalTwentyPercentileCalmarRatio <- Metric("ConditionalTwentyPercentileCalmarRatio",metricConditionalTwentyPercentileCalmarRatio)
ConditionalTenPercentileCalmarRatio <- Metric("ConditionalTenPercentileCalmarRatio",metricConditionalTenPercentileCalmarRatio)
Psi <- Metric("Psi", metricPsi)
PsiNu <- Metric('PsiNu', metricPsiNu)
ConditionalTenPercentileDailyVaR <- Metric("ConditionalTenPercentileDailyVaR",pnlCurveCalcFunc(conditionalTenPercentileDailyVaR))
ConditionalFivePercentileDailyVaR <- Metric("ConditionalFivePercentileDailyVaR",pnlCurveCalcFunc(conditionalFivePercentileDailyVaR))
ConditionalOnePercentileDailyVaR <- Metric("ConditionalOnePercentileDailyVaR",pnlCurveCalcFunc(conditionalOnePercentileDailyVaR))

###########################################################################################
#	RightEdge built in metrics
###########################################################################################
REAverageBarsHeld <- Metric("REAverageBarsHeld", metricNA)
REAverageLosingBarsHeld <- Metric("REAverageLosingBarsHeld", metricNA)
REAverageLoss <- Metric("REAverageLoss", metricNA)
REAverageProfit <- Metric("REAverageProfit", metricNA)
REAverageWin <- Metric("REAverageWin", metricNA)
REAverageWinningBarsHeld <- Metric("REAverageWinningBarsHeld", metricNA)
REAverageWinPct <- Metric("REAverageWinPct", metricNA)
REConsecutiveLosing <- Metric("REConsecutiveLosing", metricNA)
REConsecutiveWinning <- Metric("REConsecutiveWinning", metricNA)
RELongLosingTrades <- Metric("RELongLosingTrades", metricNA)
RELongWinningTrades <- Metric("RELongWinningTrades", metricNA)
RELosingBarsHeld <- Metric("RELosingBarsHeld", metricNA)
RELosingTrades <- Metric("RELosingTrades", metricNA)
RELosingTradesPct <- Metric("RELosingTradesPct", metricNA)
REMaxAccountValue <- Metric("REMaxAccountValue", metricNA)
REMaxConsecutiveLosing <- Metric("REMaxConsecutiveLosing", metricNA)
REMaxConsecutiveWinning <- Metric("REMaxConsecutiveWinning", metricNA)
REMaxDrawDown <- Metric("REMaxDrawDown", metricNA)
REMaxLoss <- Metric("REMaxLoss", metricNA)
REMaxProfit <- Metric("REMaxProfit", metricNA)
RENetProfit <- Metric("RENetProfit", metricWeightedSum)
RENeutralTrades <- Metric("RENeutralTrades", metricNA)
RERealizedGrossLoss <- Metric("RERealizedGrossLoss", metricNA)
RERealizedGrossProfit <- Metric("RERealizedGrossProfit", metricNA)
RERealizedNetProfit <- Metric("RERealizedNetProfit", metricNA)
REShortLosingTrades <- Metric("REShortLosingTrades", metricNA)
REShortWinningTrades <- Metric("REShortWinningTrades", metricNA)
RETotalBarsHeld <- Metric("RETotalBarsHeld", metricNA)
RETotalFinishedTrades <- Metric("RETotalFinishedTrades", metricNA)
RETotalLossPct <- Metric("RETotalLossPct", metricNA)
RETotalProfitPct <- Metric("RETotalProfitPct", metricNA)
RETotalTrades <- Metric("RETotalTrades", metricNA)
RETotalWinPct <- Metric("RETotalWinPct", metricNA)
REUnrealizedNetProfit <- Metric("REUnrealizedNetProfit", metricNA)
REWinningBarsHeld <- Metric("REWinningBarsHeld", metricNA)
REWinningTrades <- Metric("REWinningTrades", metricNA)
REWinningTradesPct <- Metric("REWinningTradesPct", metricNA)

###########################################################################################
#	Q metrics
###########################################################################################

QAverageBarsHeld <- Metric("QAverageBarsHeld", metricNA)
QAverageLosingBarsHeld <- Metric("QAverageLosingBarsHeld", metricNA)
QAverageLoss <- Metric("QAverageLoss", metricQAverageLoss)
QAverageProfit <- Metric("QAverageProfit", metricQAverageProfit)
QAverageWin <- Metric("QAverageWin", metricQAverageWin)
QAverageWinningBarsHeld <- Metric("QAverageWinningBarsHeld", metricNA)
QLongLosingTrades <- Metric("QLongLosingTrades", metricSum)
QLongWinningTrades <- Metric("QLongWinningTrades", metricSum)
QLosingBarsHeld <- Metric("QLosingBarsHeld", metricSum)
QLosingTrades <- Metric("QLosingTrades", metricSum)
QLosingTradesPct <- Metric("QLosingTradesPct", metricQLosingTradesPct)
QMaxDrawdown <- Metric("QMaxDrawdown", pnlCurveCalcFunc(maxDrawDown))
QNetProfit <- Metric("QNetProfit", metricWeightedSum)
QNeutralTrades <- Metric("QNeutralTrades", metricSum)
QRealizedGrossLoss <- Metric("QRealizedGrossLoss", metricWeightedSum)
QRealizedGrossProfit <- Metric("QRealizedGrossProfit", metricWeightedSum)
QRealizedNetProfit <- Metric("QRealizedNetProfit", metricWeightedSum)
QShortLosingTrades <- Metric("QShortLosingTrades",  metricSum)
QShortWinningTrades <- Metric("QShortWinningTrades",  metricSum)
QTotalBarsHeld <- Metric("QTotalBarsHeld",  metricSum)
QTotalFinishedTrades <- Metric("QTotalFinishedTrades",  metricSum)
QTotalTrades <- Metric("QTotalTrades", metricSum)
QUnrealizedNetProfit <- Metric("QUnrealizedNetProfit", metricWeightedSum)
QWinningBarsHeld <- Metric("QWinningBarsHeld", metricSum)
QWinningTrades <- Metric("QWinningTrades", metricSum)
QWinningTradesPct <- Metric("QWinningTradesPct", metricQWinningTradesPct)
QLargestWinningTrade <- Metric("QLargestWinningTrade", metricWeightedMax)
QLargestLosingTrade <- Metric("QLargestLosingTrade", metricWeightedMin)
QMaxConsecutiveWinningTrades <- Metric("QMaxConsecutiveWinningTrades", metricNA)
QMaxConsecutiveLosingTrades <- Metric("QMaxConsecutiveLosingTrades", metricNA)
QAnnualizedNetProfit <- Metric("QAnnualizedNetProfit", metricNA)
QAverageDrawdown <- Metric("QAverageDrawdown", metricNA)
QAverageDrawdownRecoveryTime <- Metric("QAverageDrawdownRecoveryTime", metricNA)
QAverageDrawdownTime <- Metric("QAverageDrawdownTime", metricNA)
QAverageTrade <- Metric("QAverageTrade", metricNA)
QCalmarRatio <- Metric("QCalmarRatio", metricNA)
QConditionalTenPercentileCalmarRatio <- Metric("QConditionalTenPercentileCalmarRatio", metricNA)
QConditionalTwentyPercentileCalmarRatio <- Metric("QConditionalTwentyPercentileCalmarRatio", metricNA)
QConditionalTenPercentileDrawdown <- Metric("QConditionalTenPercentileDrawdown", metricNA)
QConditionalTwentyPercentileDrawdown <- Metric("QConditionalTwentyPercentileDrawdown", metricNA)
QDownsideDeviation <- Metric("QDownsideDeviation", metricNA)
QExpectancy <- Metric("QExpectancy", metricNA)
QExpectancyScore <- Metric("QExpectancyScore", metricNA)
QKRatio <- Metric("QKRatio", metricNA)
QLargestLossPerAverageLoss <- Metric("QLargestLossPerAverageLoss", metricNA)
QLargestLossPerGrossLoss <- Metric("QLargestLossPerGrossLoss", metricNA)
QLargestWinPerAverageWin <- Metric("QLargestWinPerAverageWin", metricNA)
QLargestWinPerGrossProfit <- Metric("QLargestWinPerGrossProfit", metricNA)
QLargestWinPerNetProfit <- Metric("QLargestWinPerNetProfit", metricNA)
QNetProfitPerMaxDrawdown <- Metric("QNetProfitPerMaxDrawdown", metricNA)
QOmegaRatio <- Metric("QOmegaRatio", metricNA)
QSharpeRatio <- Metric("QSharpeRatio", metricNA)
QSharpeRatioMonthly <- Metric("QSharpeRatioMonthly", metricNA)
QSharpeRatioWeekly <- Metric("QSharpeRatioWeekly", metricNA)
QSortinoRatio <- Metric("QSortinoRatio", metricNA)
QStandardDeviation <- Metric("QStandardDeviation", metricNA)
QStandardDeviationMonthly <- Metric("QStandardDeviationMonthly", metricNA)
QStandardDeviationWeekly <- Metric("QStandardDeviationWeekly", metricNA)
QTenPercentileDrawdown <- Metric("QTenPercentileDrawdown", metricNA)
QTradesPerBar <- Metric("QTradesPerBar", metricNA)
QTwentyPercentileDrawdown <- Metric("QTwentyPercentileDrawdown", metricNA)
QUpsidePotentialRatio <- Metric("QUpsidePotentialRatio", metricNA)
QWinLossRatio <- Metric("QWinLossRatio", metricNA)
QTotalSlippage <- Metric("QTotalSlippage", metricNA)
QAverageSlippagePerWinningTrade <- Metric("QAverageSlippagePerWinningTrade", metricNA)
QAverageSlippagePerLosingTrade <- Metric("QAverageSlippagePerLosingTrade", metricNA)


###########################################################################################
#	Metric lists
###########################################################################################
REGeneratedMetrics <- list(
    REAverageBarsHeld,
    REAverageLosingBarsHeld,
    REAverageLoss,
    REAverageProfit,
    REAverageWin,
    REAverageWinningBarsHeld,
    REAverageWinPct,
    REConsecutiveLosing,
    REConsecutiveWinning,
    RELongLosingTrades,
    RELongWinningTrades,
    RELosingBarsHeld,
    RELosingTrades,
    RELosingTradesPct,
    REMaxAccountValue,
    REMaxConsecutiveLosing,
    REMaxConsecutiveWinning,
    REMaxDrawDown,
    REMaxLoss,
    REMaxProfit,
    RENeutralTrades,
    RERealizedGrossLoss,
    RERealizedGrossProfit,
    RERealizedNetProfit,
    REShortLosingTrades,
    REShortWinningTrades,
    RETotalBarsHeld,
    RETotalFinishedTrades,
    RETotalLossPct,
    RETotalProfitPct,
    RETotalTrades,
    RETotalWinPct,
    REUnrealizedNetProfit,
    REWinningBarsHeld,
    REWinningTrades,
    REWinningTradesPct
)

QGeneratedMetrics <- list(
	QAverageBarsHeld,
	QAverageLosingBarsHeld,
	QAverageLoss,
	QAverageProfit,
	QAverageWin,
	QAverageWinningBarsHeld,
	QLongLosingTrades,
	QLongWinningTrades,
	QLosingBarsHeld,
	QLosingTrades,
	QLosingTradesPct,
	QMaxDrawdown,
	QNetProfit,
	QNeutralTrades,
	QRealizedGrossLoss,
	QRealizedGrossProfit,
	QRealizedNetProfit,
	QShortLosingTrades,
	QShortWinningTrades,
	QTotalBarsHeld,
	QTotalFinishedTrades,
	QTotalTrades,
	QUnrealizedNetProfit,
	QWinningBarsHeld,
	QWinningTrades,
	QWinningTradesPct,
	QLargestWinningTrade,
	QLargestLosingTrade,
	QMaxConsecutiveWinningTrades,
	QMaxConsecutiveLosingTrades
)


TSGeneratedMetrics <- list(
    TSAvgBarsEvenTrade,
    TSAvgBarsLosTrade,
    TSAvgBarWinTrade,
    TSGrossLoss,
    TSGrossProfit,
    TSLargestLosTrade,
    TSLargestWinTrade,
    TSMaxConsecLosers,
    TSMaxConsecWinners,
    TSMaxContractsHeld,
    TSClosePositionProfit,
    TSNumEvenTrades,
    TSNumLosTrades,
    TSNumWinTrades,
    TSPercentProfit,
    TSTotalTrades,
    TSTotalBarsEvenTrades,
    TSTotalBarsLosTrades,
    TSTotalBarsWinTrades,
    TSOpenPositionProfit,
    TSMaxIDDrawDown,
    TSNetProfit,
	TSExpectancy
)

NonTSGeneratedMetrics <- list(
    NetProfit,
    MaxDrawDown,
    AverageDrawDown,
    AverageDrawDownTime,
    AverageDrawDownRecoveryTime,
    KRatio,
	SortinoRatio,
	OmegaRatio,
	UpsidePotentialRatio,
	SharpeRatioDaily,
	SharpeRatioWeekly,
	SharpeRatioMonthly,
	TenPercentileDrawDown,
	ConditionalTenPercentileDrawDown,
	TSExpectancyScore,
	Psi
)
