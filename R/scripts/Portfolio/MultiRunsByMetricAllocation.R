# Multi Portfolio Runs for MetricAllocations
# 
# Author: rsheftel
###############################################################################

#Initialize R
library(QFPortfolio)
library(QFReports)

#Initialize multi-run object
	print("Initializing PortfolioRuns...")
	multiRuns <- PortfolioRuns(	name = "MultiRunsByMetricAllocation",
								groupName = ae.group,
								curvesDirectory = ae.curvesDir,
								curvesExtension="bin")

#Set up date ranges, metrics, and diversity choices
	dateRange <- list()
	#Fixed end date, variable start 2y, 5y, 10y windows
	yearsBack <- c(2,5,10,15)
	dateRange$end <- rep(ae.runDate,4)
	dateRange$begin <- as.POSIXct(sapply(yearsBack, function(x) seq(ae.runDate,length=2,by=squish('-',x,' year'))[2],simplify=TRUE))
	dateRange$name <- paste('FixedEnd_',yearsBack,'y',sep="")
	
	allocationMetrics 	<- list(DownSideDeviation,WeeklyStandardDeviation) 
	diversityMethods 	<- c('Fixed') #,'PrincipalComponents')
	 
#Loop through each run	

	diverse <- Diversity(groupName=ae.group)
	for (dateCount in seq_along(dateRange$name)){
		for (allocationMetric in allocationMetrics){
			for (diversityMethod in diversityMethods){
				
				########	Metric Allocation Method	##########
				if(is.na(diversityMethod)) diversityMethod <- 'NoDiversity'
				runName <- paste(dateRange$name[[dateCount]],as.character(allocationMetric),diversityMethod,sep="_")
				multiRuns$addRun(runName)
				prun <- multiRuns$currentRun()
				print(squish("Run: ", prun$name()))

				#Set dates
				print(squish("Date range: ",format(dateRange$begin[[dateCount]],"%Y-%m-%d")," to ",format(dateRange$end[[dateCount]],"%Y-%m-%d")))
				prun$dateRange(start=dateRange$begin[[dateCount]], end=dateRange$end[[dateCount]])	
				
				#Loading curves
				print("Loading Curves...")
				prun$loadCurves(multiRuns$childWeightedCurves())
				systems <- prun$curveNames()
				print(squish("Curves Loaded: ",paste(systems,collapse=", ")))
				systems.count <- length(systems)
				
				#Set objective
				prun$objective(allocationMetric)
				print(squish("Allocation Metric: ", as.character(prun$objective())))		
			
				#Set constraints
				print("Setting constraints.")
				DrawDownConstraint <- -ae.aum*0.03
				prun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=DrawDownConstraint,max=0)
				
				#Set up diversity
				if(diversityMethod=='PrincipalComponents'){
					print('Adding Diversity...')
					diversityScore <- diverse$score(diversityMethod,Range(dateRange$begin[[dateCount]],dateRange$end[[dateCount]]))
					prun$diversityScore(as.list(diversityScore))
					prun$params(list(diversity=diverse,methodName=diversityMethod))
				}
				if(diversityMethod=='Fixed'){
					print('Adding Fixed Weights...')
					diversityScore <- diverse$score(diversityMethod,Range(dateRange$begin[[dateCount]],dateRange$end[[dateCount]]),weights=ae.sector.weights)
					prun$diversityScore(as.list(diversityScore))
					prun$params(list(diversity=diverse,methodName='Fixed',diversity.params=list(weights=ae.sector.weights)))
				}
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
				
			#######	Now use NLMINB to optimize around the weights   #########
				runName <- paste(dateRange$name[[dateCount]],as.character(allocationMetric),diversityMethod,'Optimized',sep="_")
				multiRuns$addRun(runName)
				oPrun <- multiRuns$currentRun()
				print(squish("Run: ", oPrun$name()))
				print(squish("Date range: ",format(dateRange$begin[[dateCount]],"%Y-%m-%d")," to ",format(dateRange$end[[dateCount]],"%Y-%m-%d")))
				oPrun$dateRange(start=dateRange$begin[[dateCount]], end=dateRange$end[[dateCount]])	
				
				#Loading curves
				print("Loading Curves...")
				oPrun$loadCurves(multiRuns$childWeightedCurves())
				systems <- oPrun$curveNames()
				print(squish("Curves Loaded: ",paste(systems,collapse=", ")))
				systems.count <- length(systems)
				
				#Set objective
				oPrun$objective(ae.objective)
				print(squish("Objective function: ", as.character(oPrun$objective())))		
				
				#Min/max weights
				print("Set min/max weights...")
				mins <- rep(0,systems.count)
				names(mins) <- systems
				maxs <- rep(2,systems.count)			#Review this.
				names(maxs) <- systems
				oPrun$minMaxWeights(mins=as.list(mins), maxs=as.list(maxs))
				
				#Show diversity score	
				print("Set diversity score...")
				oPrun$diversityScore(as.list(diversityScore))	# This is from above
				
				#Set constraints
				print("Setting constraints.")	
				
				#Total portfolio constraints
				oPrun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=DrawDownConstraint,max=0)
				
				#Summed Metric Constraints
				allocatedMetric <- prun$metric(source='optimal',curves='cut',slip='slipped',curveName='Portfolio',as.character(allocationMetric))
				summedMetConstraint <- SummedMetricConstraint(systems, summedMetric=allocationMetric, metricBudget=allocatedMetric)
				for(system in systems) {
					targetScore <- ae.sector.weights[system]
					scale <- 0.1
					summedMetConstraint$set(system, targetScore*(1-scale), targetScore*(1+scale))
				}
				oPrun$addConstraint(summedMetConstraint)
				
				#Set seeds	
				print("Setting seeds.")
				seeds <- prun$optimalWeights()*0.99
				print(squish("Seeds : ",paste(seeds,collapse=",")))
				oPrun$seeds(as.list(seeds))
				
				#Optimize
				print("Optimizing.")
				oPrun$penalty(2000 * ae.aum)
				oPrun$optimType("nlminb") 
				oPrun$params(params=list(eval.max=20000,iter.max=20000,trace=5,rel.tol=0.1)) 
				oPrun$optimize()
				
				#Calculate metrics
				oPrun$calculateMetrics(ae.metrics, verbose=TRUE)
			}
		}
	}												

	
#Generate Report
	print("Generation Report")
	preport <- PortfolioRunsReport(	portfolioRuns=multiRuns,
									filename=squish(ae.reportDir,multiRuns$name()),
									childReportDirectory=ae.reportDir)
	preport$reportAll(ae.aum)
	preport$closeConnection()
	
#Email report
	email <- Mail$notification(squish('Portfolio Optimization - MultiRunsByMetricAllocation - ',ae.group),squish('Report - available in ', 
									makeWindowsFilename(preport$filenames()$html)))
	email$attachFile(preport$filenames()$html)
	email$sendTo('team')
	
#Remove variables
	rm(prun)
	rm(oPrun)
	rm(multiRuns)
	rm(preport)