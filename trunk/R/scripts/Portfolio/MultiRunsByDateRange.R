# Multi Portfolio Runs for the date ranges
# 
# Author: rsheftel
###############################################################################

#Initialize R
library(QFPortfolio)
library(QFReports)

#Initialize multi-run object
	print("Initializing PortfolioRuns...")
	multiRuns <- PortfolioRuns(	name = "MultiRunsByDateRange",
								groupName = ae.group,
								curvesDirectory = ae.curvesDir,
								curvesExtension="bin")

#Set up date ranges
	dateRange <- list()
	#Fixed end date, variable start 2y, 4y, 10y windows
	yearsBack <- c(2,4,10)
	dateRange$end <- rep(ae.runDate,3)
	dateRange$begin <- as.POSIXct(sapply(yearsBack, function(x) seq(ae.runDate,length=2,by=squish('-',x,' year'))[2],simplify=TRUE))
	dateRange$name <- paste('FixedEnd_',yearsBack,'y',sep="")
	
#Loop through each run	

	for (runCount in seq_along(dateRange$name)){
		multiRuns$addRun(dateRange$name[[runCount]])
		prun <- multiRuns$currentRun()
		print(squish("Run: ", prun$name()))
		
	#Set dates
		print(squish("Date range: ",format(dateRange$begin[[runCount]],"%Y-%m-%d")," to ",format(dateRange$end[[runCount]],"%Y-%m-%d")))
		prun$dateRange(start=dateRange$begin[[runCount]], end=dateRange$end[[runCount]])	
	
	#Loading curves
		print("Loading Curves...")
		prun$loadCurves(multiRuns$childWeightedCurves())
		systems <- prun$curveNames()
		print(squish("Curves Loaded: ",paste(systems,sep=",")))
		systems.count <- length(systems)
	
	#Set objective
		prun$objective(ae.objective)
		print(squish("Objective function: ", as.character(prun$objective())))		
		
	#Set penalty
		print("Set penalty...")
		prun$penalty(penalty=10000000000)
			
	#Min/max weights
		print("Set min/max weights")
		mins <- rep(0,systems.count)
		names(mins) <- systems
		maxs <- rep(5,systems.count)			#Review this.
		names(maxs) <- systems
		prun$minMaxWeights(mins=as.list(mins), maxs=as.list(maxs))
		
	#Set constraints
		print("Setting constraints.")
		VaRBudget <- -ae.aum*0.01
		DrawDownConstraint <- -ae.aum*0.03
		
		diverse <- Diversity(groupName=ae.group)
		diversityScore <- diverse$score("PrincipalComponents")
		prun$diversityScore(as.list(diversityScore))
		summedVarConstraint <- SummedVarConstraint(systems,summedAlpha=0.1,varBudget = VaRBudget)
		for(system in systems) {
			percentScore <- diversityScore[system] / sum(diversityScore)
			summedVarConstraint$set(system, -percentScore, percentScore)
		}
		prun$addConstraint(summedVarConstraint)
		prun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=DrawDownConstraint,max=0)
		
	#Set seeds
		print("Setting seeds.")
		seeds <- rep(1,systems.count)
		names(seeds) <- systems
		print(squish("Seeds : ",paste(seeds,sep=",")))
		prun$seeds(as.list(seeds))
				
	#Optimize
		print("Optimizing.")
		prun$penalty(2000 * ae.aum)
		prun$optimType("nlminb") 
		prun$params(params=list(eval.max=20000,iter.max=20000,trace=5,rel.tol=0.1)) 
		prun$optimize()
		
		prun$calculateMetrics(ae.metrics, verbose=TRUE)
	}												

	
#Generate Report
	print("Generation Report")
	preport <- PortfolioRunsReport(	portfolioRuns=multiRuns,
									filename=squish(ae.reportDir,"MultiRunsByDateRange"),
									childReportDirectory=ae.reportDir)
	preport$reportAll(ae.aum)
	preport$closeConnection()
	
#Email report
	email <- Mail$notification(squish('Portfolio Optimization - MultiRunsByDateRange - ',ae.group),squish('Report - available in ', 
									makeWindowsFilename(preport$filenames()$html)))
	email$attachFile(preport$filenames()$html)
	email$sendTo('team')
	
#Remove variables
	rm(prun)
	rm(multiRuns)
	rm(preport)