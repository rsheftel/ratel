# Portfolio Run for the entire period
# 
# Author: rsheftel
###############################################################################

#Initialize R
library(QFPortfolio)
library(QFReports)

#Initialize object
	print("Initializing PortfolioRun...")
	prun <- PortfolioRun(name = "SingleRunEntirePeriod",
						groupName = ae.group,
						curvesDirectory = ae.curvesDir,
						curvesExtension="bin")
#Set dates
	print(squish("Date range: ",format(as.POSIXct('1996-01-01'),"%Y-%m-%d")," to ",format(ae.runDate,"%Y-%m-%d")))
	prun$dateRange(start=as.POSIXct('1996-01-01'), end=ae.runDate)	
					
#Load curves
	print("Loading Curves...")
	prun$loadCurves()					
	systems <- prun$curveNames()
	print(squish("Curves Loaded: ",paste(systems,sep=",")))
	systems.count <- length(systems)
	
#Set objective
	prun$objective(ae.objective)
	print(squish("Objective function: ", as.character(prun$objective())))		

#Min/max weights
	print("Set min/max weights...")
	mins <- rep(0,systems.count)
	names(mins) <- systems
	maxs <- rep(2,systems.count)			#Review this.
	names(maxs) <- systems
	prun$minMaxWeights(mins=as.list(mins), maxs=as.list(maxs))
	
#Show diversity score	
	print("Set diversity score...")
	diverse <- Diversity(groupName=ae.group)
	diversityScore <- diverse$score("PrincipalComponents")
	prun$diversityScore(as.list(diversityScore))
			
#Set constraints
	print("Setting constraints.")	
	
	#Total portfolio constraints
	maxDrawDown <- -ae.aum*0.03
	prun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=maxDrawDown,max=0)
	
	#Summed Metric Constraints
	downsideDeviationConstraint <- SummedMetricConstraint(systems, summedMetric=DownSideDeviation, metricBudget=maxDrawDown*9/6)
	for(system in systems) {
		targetScore <- ae.sector.weights[system]
		scale <- 0.1
		downsideDeviationConstraint$set(system, targetScore*(1-scale), targetScore*(1+scale))
	}
	prun$addConstraint(downsideDeviationConstraint)

#Set seeds
	print("Setting seeds.")
	seeds <- rep(1,systems.count)
	names(seeds) <- systems
	print(squish("Seeds : ",paste(seeds,collapse=",")))
	prun$seeds(as.list(seeds))
	
#Optimize
	print("Optimizing.")
	prun$penalty(2000 * ae.aum)
	prun$optimType("nlminb") 
	prun$params(params=list(eval.max=20000,iter.max=20000,trace=5,rel.tol=0.1)) 
	prun$optimize()

#Calculate metrics
	prun$calculateMetrics(ae.metrics, verbose=TRUE)
	
#Generate Report
	print("Generating report.")
	preport <- PortfolioRunReport(portfolioRun=prun,filename=squish(ae.reportDir,"SingleRunEntirePeriod"))
	preport$reportOptimization()
	preport$reportReturns(ae.aum)
	preport$graphOptimization()
	preport$graphReturns()
	preport$reportCMF()
	preport$closeConnection()

#Email report
	email <- Mail$notification(squish('Portfolio Optimization - SingleRunEntirePeriod - ',ae.group),squish('Report - available in ', 
									makeWindowsFilename(preport$filenames()$html)))
	email$attachFile(preport$filenames()$html)
	email$sendTo('team')

#Remove variables
rm(prun)
rm(preport)
