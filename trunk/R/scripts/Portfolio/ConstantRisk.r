
#Initialize R
library(QFPortfolio)
library(QFReports)

##################################################################################
#	Set up the inputs	
##################################################################################

runName		<- 'ConstantRisk'
groupName 	<- 'AllSystemsQ'
curvesDir	<- 'h:/temp/curves/'
outputDir   <- 'h:/temp/'
startDate	<- '2006-04-30'
endDate		<- '2009-04-30'
aum			<- 50000000


##################################################################################
#	Set the weights	
##################################################################################

sizeParameter <- list(	NDayBreak=	'InitEquity',
						FaderClose=	'RiskDollars',
						FXCarry = 	'StopLoss',
						FXCommodityClose =	'RiskDollars',
						FadeMonthEndPush = 	'InitEquity',
						FadeWeekEndPush =	'InitEquity',
						NBarFade = 			'riskDollars',
						CouponSwap = 		'TradeSize',
						LiqInj =			'startSize',
						NDayBreakCloseMinVol = 'InitEquity',
						BuySellAndHold = 	'TradeSize')

riskDollarsForPV <- function(system, pv){
	riskDollars <- SystemDB$lastParameterValue(system, pv, sizeParameter[[system]])
	if (system=='NDayBreak') riskDollars <- riskDollars * 0.02
	if (system=='FadeMonthEndPush') riskDollars <- riskDollars * 0.02
	if (system=='FadeWeekEndPush') riskDollars <- riskDollars * 0.02
	if (system=='NDayBreakCloseMinVol') riskDollars <- riskDollars * 0.02
	if (system=='CouponSwap') riskDollars <- riskDollars * 312.5 * 15
	if (system=='BuySellAndHold') riskDollars <- 100000
	return(riskDollars)
}

parentGroup <- CurveGroup(groupName)
children <- parentGroup$childNames()

rawRisk <- list()
allRiskFrame <- NULL
for (child in children){
	childGroup <- CurveGroup(child)
	riskFrame <- data.frame(market=childGroup$markets(unique=FALSE), system=childGroup$systems(unique=FALSE), pv=childGroup$pvs(unique=FALSE), 
															riskDollars=NA)
	system <- childGroup$systems()
	pvs <- childGroup$pvs(unique=TRUE)
	for (pv in pvs){
		riskDollars <- riskDollarsForPV(system, pv)
		riskFrame[riskFrame$pv==pv,'riskDollars'] <- riskDollars
	}
	rawRisk[[child]] <- sum(riskFrame$riskDollars)
	allRiskFrame <- rbind(allRiskFrame, riskFrame)
}

diverse <- Diversity(groupName=groupName)
diversityScore <- diverse$score("PrincipalComponents")

rawRisk <- unlist(rawRisk)
totalRawRisk <- sum(rawRisk)
totalDiversity <- sum(diversityScore)
riskPerDiversity <- totalRawRisk/totalDiversity

fixedWeights <- (diversityScore*riskPerDiversity) / rawRisk


metrics <- list(	Psi,
					CalmarRatio,
					CalmarRatioWeekly,
					CalmarRatioMonthly,
					ConditionalTwentyPercentileCalmarRatio,
					ConditionalTenPercentileCalmarRatio,
					ConditionalTwentyPercentileDrawDown,
					ConditionalTenPercentileDrawDown,
					MaxDrawDown,
					KRatio,
					AnnualizedNetProfit,
					DailyStandardDeviation,
					WeeklyStandardDeviation,
					SharpeRatioWeekly,
					SharpeRatioMonthly,
					SortinoRatio,
					OmegaRatio,
					ConditionalTenPercentileDailyVaR,
					ConditionalFivePercentileDailyVaR,
					ConditionalOnePercentileDailyVaR
					)
					
##################################################################################
#	Now just run all the below code...	
##################################################################################
										
#Initialize object
	print("Initializing PortfolioRun...")
	prun <- PortfolioRun(name = runName, groupName = groupName, curvesDirectory = curvesDir, curvesExtension="bin")

#Set dates
	print(squish("Date range: ",startDate," to ",endDate))
	prun$dateRange(start=as.POSIXct(startDate), end=as.POSIXct(endDate))	
					
#Load curves
	print("Loading Curves...")
	prun$loadCurves()					
	systems <- prun$curveNames()
	print(squish("Curves Loaded: ",paste(systems,sep=",")))
	systems.count <- length(systems)
	print(squish("# of Systems: ",systems.count))
			
#Min/max weights
	print("Set min/max weights")
	mins <- rep(0,systems.count)
	names(mins) <- systems
	maxs <- rep(max(fixedWeights),systems.count)
	names(maxs) <- systems
	prun$minMaxWeights(mins=as.list(mins), maxs=as.list(maxs))
				
			
#Set seeds
	print("Setting seeds.")
	seeds <- rep(1,systems.count)
	names(seeds) <- systems
	print(squish("Seeds : ",paste(seeds,sep=",")))
	prun$seeds(as.list(seeds))
	
#Set the Optimal weights
	prun$setOptimalWeights(fixedWeights)
	
#Calc metrics
	prun$calculateMetrics(metrics, verbose=TRUE)
	
#Generate Report
	print("Generating report.")
	preport <- PortfolioRunReport(portfolioRun=prun,filename=squish(outputDir,runName))
	preport$reportOptimization()
	preport$reportReturns(aum)
	preport$graphOptimization()
	preport$graphReturns()
	preport$reportCMF()
	preport$closeConnection()

