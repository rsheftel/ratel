
#Initialize R
library(QFPortfolio)
library(QFReports)

##################################################################################
#	Set up the inputs	
##################################################################################

runName		<- 'FixedWeightRunExample'
groupName 	<- 'AllSystemsQ'
curvesDir	<- 'h:/temp/curves/'
outputDir   <- 'h:/temp/'
startDate	<- '2006-04-30'
endDate		<- '2009-04-30'
aum			<- 50000000

fixedWeights <- unlist(list(BuySellAndHold	= 1,
							CouponSwap		= 2,
							FXCarry			= 1,             
							FXCommodityClose = 2,
							FadeMonthEndPush = 3,
							FadeWeekEndPush  = 4,
							FaderClose		 = 5,
							LiqInj			 = 6,
							NBarFadeFast	= 1,
							NBarFadeSlow	= 2,
							NDayBreak		= 3,           
							NDayBreakCloseMinVol = 4))

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
	print(squish("# of Systems: ",length(prun$curveNames)))
				
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

