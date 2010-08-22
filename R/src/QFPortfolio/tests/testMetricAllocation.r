# tests for MetricAllocation class
#
########################################################################

library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

sto <- STO(stoDirectory(), "SimpleSTOTemplate")
curve.cube <- sto$curves()
curve1 <- curve.cube$curve(first(simpleMsivs), 1) # 0, -1  (NetProfit, MaxDrawDown)
curve2 <- curve.cube$curve(first(simpleMsivs), 2) # 5, 0 
curve3 <- curve.cube$curve(first(simpleMsivs), 3) #45, 0
curves <- list(curve1, curve2, curve3)

test.construction <- function(){
	checkInherits(MetricAllocation(),"MetricAllocation")
	checkInherits(MetricAllocation(verbose=FALSE),"MetricAllocation")
}

test.setup <- function(){
	ma <- MetricAllocation(verbose=TRUE)
	shouldBombMatching(ma$objectiveMetric(),'ObjetiveFunction not set yet.')	
	shouldBombMatching(ma$optimize(),'Objective Function not initialized.')
	
	objFunc <- PortfolioObjectiveFunction(DailyStandardDeviation)
	ma$objectiveFunction(objFunc)
	checkSame(ma$objectiveFunction(), objFunc)
	checkSame(ma$objectiveMetric(), DailyStandardDeviation)	
	shouldBombMatching(ma$optimize(),'Optimization Routine not initialized.')
	
	optRoutine <- OptimizationRoutine(type="metricAllocation")
	ma$optimizationRoutine(optRoutine)
	checkSame(ma$optimizationRoutine(),optRoutine)
	shouldBombMatching(ma$optimize(),'Curves not loaded.')
	
	objFunc$curves(curves=curves)
	checkSame(ma$curves(),objFunc$curves())	
}

optimSetup <- function(){
	ma <- MetricAllocation(verbose=TRUE)
	objFunc <- PortfolioObjectiveFunction(DailyStandardDeviation)
	optRoutine <- OptimizationRoutine(type="metricAllocation")
	objFunc$curves(curves=curves)
	ma$objectiveFunction(objFunc)
	ma$optimizationRoutine(optRoutine)
	return(ma)
}

test.optimize <- function(){
	#No constraint, no diversity
	ma <- optimSetup()
	checkSame(round(ma$optimize(),6),c(2.192450,3.797435,0.438490))
}
	
test.optimize.withConstraint <- function(){	
	# One constraint
	ma <- optimSetup()
	ma$objectiveFunction()$constrainPortfolio(DailyStandardDeviation, 0, 24)
	checkSame(round(ma$optimize(),5),c(24.00000,41.56922,4.80000))

	#Negative constraint
	ma <- optimSetup()
	ma$objectiveFunction()$constrainPortfolio(MaxDrawDown, -5, 0)
	ma$objectiveFunction()$curves(list(curve1))
	checkSame(ma$optimize(),5)
		
	# Multiple constraints
	ma <- optimSetup()
	ma$objectiveFunction()$constrainPortfolio(DailyStandardDeviation, 0, 24)
	ma$objectiveFunction()$constrainPortfolio(NetProfit, 0, 15)
	checkSame(round(ma$optimize(),4), c(0.8494,1.4711,0.1699))
		
	
	# No reasonable answer
	ma <- optimSetup()
	ma$objectiveFunction()$constrainPortfolio(DailyStandardDeviation, -24, 0)
	checkSame(ma$optimize(), c(0,0,0))
}

test.optimize.withDiversity <- function(){
	ma <- optimSetup()
	curves <- list(curve2,curve3)
	names(curves) <- c('TestExposureSub2','TestSubGroup2')
	ma$objectiveFunction()$curves(curves)
	div <- Diversity('TestPortfolio4')
	div$loadMarkets()
	ma$optimizationRoutine()$params(list(diversity=div,methodName='TotalCorrelationLevel'))

	expected <-c(4.6170317,0.5823412)
	names(expected) <- names(curves) 	
	checkSame(round(ma$optimize(),7), expected)
	
	names(curves) <- c('TestExposureSub2','BadCurveName')
	ma$objectiveFunction()$curves(curves)
	shouldBombMatching(ma$optimize(), 'All Curves do not exist in diversity object.')
}
	
test.optimize.withConstraintAndDiverity <- function(){
	ma <- optimSetup()
	curves <- list(curve2,curve3)
	names(curves) <- c('TestExposureSub2','TestSubGroup2')
	ma$objectiveFunction()$curves(curves)
	div <- Diversity('TestPortfolio4')
	div$loadMarkets()
	ma$optimizationRoutine()$params(list(diversity=div,methodName='TotalCorrelationLevel'))
	ma$objectiveFunction()$constrainPortfolio(DailyStandardDeviation, 0, 24)
		
	expected <-c(28.06978, 3.54041)
	names(expected) <- names(curves) 	
	checkSame(round(ma$optimize(),5), expected)
}	

test.optimizeWithFixedWeights <- function(){
	ma <- optimSetup()
	curves <- list(curve2,curve3)
	names(curves) <- c('TestExposureSub2','TestSubGroup2')
	ma$objectiveFunction()$curves(curves)
	div <- Diversity('TestPortfolio4')
	div$loadMarkets()
	weights <- list(TestExposureSub2=0.25, TestSubGroup2=0.75)
	ma$optimizationRoutine()$params(list(diversity=div,methodName='Fixed',diversity.params=list(weights=weights)))
	
	expected <- c( 2.4150635,0.8366025)
	names(expected) <- names(curves)
	checkSame(round(ma$optimize(),7), expected)
}
