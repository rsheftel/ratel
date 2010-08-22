rm(list=ls())
library(QFPortfolio)
library(QFReports)
dirName <- squish(dataDirectory(), "/STProcess/Portfolio/Curves/200901")

##############################################
# Setup

metricNewMetric <- function(metric, msiv, getMetricValues, getCurves) {
	needs(metric="Metric", msiv="MSIV", getMetricValues="function", getCurves="function")
	assert(equals(metric, NewMetric))
	first <- approx(x=c(2,8),y=c(0,1),xout=getMetricValues(KRatio,msiv),rule=2)$y
	second <- approx(x=c(1.5,4),y=c(0,1),xout=getMetricValues(ConditionalTwentyPercentileCalmarRatio,msiv),rule=2)$y
	third <- approx(x=c(1.1,1.3),y=c(0,1),xout=getMetricValues(OmegaRatio,msiv),rule=2)$y
	fourth <- approx(x=c(1.75,4.0),y=c(0,1),xout=getMetricValues(SortinoRatio,msiv),rule=2)$y
	pnl <- getMetricValues(AnnualizedNetProfit,msiv)
	return(pnl*(0.3*first + 0.3*second+ 0.2*third+ 0.2*fourth))
}
NewMetric <- Metric("NewMetric", metricNewMetric) 
metric <- NewMetric
aum <- 50000000
drawdownconstraint <- -aum*0.06
VaRBudget <- -aum*0.01

RunName <- c("NewOptim-100-Fader-LimitLiqInj")

currentWeights <- c(1,1,1,1,1,1,1,1)
prun <- PortfolioRun(RunName,"AllSystemsQ",dirName)
prun$objective(NewMetric)
prun$dateRange("1997-01-02","2009-02-02")
prun$optimType("nlminb")
prun$params(params=list(eval.max=20000,iter.max=20000,trace=5,rel.tol=10^-5))
prun$loadCurves()
systems <- prun$curveNames()

mins <- c(0,0,0,0,0,0,0,0)
names(mins) <- systems
mins <- as.list(mins)

maxs <- c(10,10,10,10,10,2,10,10)
names(maxs) <- systems
maxs <- as.list(maxs)

prun$minMaxWeights(mins=mins,maxs=maxs)

seeds <- c(1,1,1,1,1,1,1,1)
names(seeds) <- systems
seeds <- as.list(seeds)
prun$seeds(seeds)
prun$penalty(100000000000)

##############################################
# Optimizer
summedVarConstraint <- SummedVarConstraint(systems,summedAlpha=0.05,varBudget = VaRBudget)
for(system in systems) summedVarConstraint$set(system, 0, 0.30)
prun$addConstraint(summedVarConstraint)
prun$addConstraint("Portfolio",ConditionalFivePercentileDailyVaR,min=VaRBudget,max=0)
#prun$addConstraint("Portfolio",ConditionalTwentyPercentileDrawDown,min=drawdownconstraint, max=0)
weightConstraint <- WeightConstraint(systems)

for (i in 1:length(systems)) weightConstraint$set(systems[[i]],mins[[i]],maxs[[i]])
prun$addConstraint(weightConstraint) 
prun$optimize()
metrics <- list(CalmarRatio,KRatio,AnnualizedNetProfit,WeeklyStandardDeviation,SharpeRatioWeekly,SharpeRatioMonthly, CalmarRatioWeekly,CalmarRatioMonthly,MaxDrawDown,
		ConditionalTwentyPercentileDrawDown,ConditionalTenPercentileDrawDown,ConditionalTwentyPercentileCalmarRatio, ConditionalTenPercentileCalmarRatio,
		SortinoRatio, OmegaRatio,ConditionalTenPercentileDailyVaR, ConditionalFivePercentileDailyVaR, ConditionalOnePercentileDailyVaR)
prun$calculateMetrics(metrics)

preport <- PortfolioRunReport(portfolioRun=prun,filename=squish(dataDirectory(),"STProcess/Optimization/",RunName))
preport$reportOptimization()
preport$graphOptimization()
preport$reportReturns(50000000)
preport$graphReturns()
preport$openReport()




