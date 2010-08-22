# Portfolio Run for the entire period for each subsector
# 
# Author: rsheftel
###############################################################################

#Initialize R
library(QFPortfolio)
library(QFReports)

allocationMetric <- DownSideDeviation 
diversityMethod <- 'PrincipalComponents'

for (sector in CurveGroup(ae.group)$childNames()){
	print(squish('Sector: ',sector))
	runName <- paste('IntraSector',sector,sep='_')
	
	prun <- PortfolioRun(name = runName, groupName = sector, curvesDirectory = ae.curvesDir, curvesExtension="bin")
#Set dates
	print(squish("Date range: ",format(as.POSIXct('1996-01-01'),"%Y-%m-%d")," to ",format(ae.runDate,"%Y-%m-%d")))
	prun$dateRange(start=as.POSIXct('1996-01-01'), end=ae.runDate)	
	
#Load curves
	print("Loading Curves...")
	prun$loadCurves()					
	systems <- prun$curveNames()
	print(squish("Curves Loaded: ",paste(systems,collapse=", ")))
	systems.count <- length(systems)
	
	#Set objective
	prun$objective(allocationMetric)
	print(squish("Allocation Metric: ", as.character(prun$objective())))		

	#Set constraints
	print("Setting constraints.")
	DrawDownConstraint <- -ae.aum*0.03 * ae.sector.weights[[sector]] * 3
	prun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=DrawDownConstraint,max=0)
		
	print('Adding Diversity...')
	diverse <- Diversity(groupName=sector)
	diversityScore <- diverse$score(diversityMethod,Range$before(ae.runDate))
	prun$diversityScore(as.list(diversityScore))
	prun$params(list(diversity=diverse,methodName=diversityMethod))
	
	#Set seeds
	print("Setting seeds.")
	seeds <- rep(1,systems.count)
	names(seeds) <- systems
	print(squish("Seeds : ",paste(as.character(seeds),collpase=",")))
	prun$seeds(as.list(seeds))
	
	#Optimize
	print("Optimizing.")
	prun$optimType("metricAllocation") 
	prun$optimize()
	
	#Calc metrics
	prun$calculateMetrics(ae.metrics, verbose=TRUE)
	
	print("Generating report.")
	preport <- PortfolioRunReport(portfolioRun=prun,filename=squish(ae.reportDir,runName))
	preport$reportOptimization()
	preport$graphOptimization()
	preport$reportCMF()
	preport$closeConnection()
}

#Email report
	email <- Mail$notification(squish('Portfolio Optimization - IntraSectors - ',ae.group),squish('Report - available in ', 
													makeWindowsFilename(ae.reportDir)))
	email$sendTo('team')

#Remove variables
rm(prun)
rm(preport)
