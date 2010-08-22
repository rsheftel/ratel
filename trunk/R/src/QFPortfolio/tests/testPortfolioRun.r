#
#	Test of PortfolioRun class  (rsheftel)
#
#################################################################

library(QFPortfolio)
library(STO)

curvesPath <- squish(system.file("testdata", package="QFPortfolio"),'/PortfolioRun/testCurves/')

makeObject <- function(){
	return(PortfolioRun(name="TestRun", groupName="TestPortfolio1", curvesDirectory=curvesPath, curvesExtension="csv"))
}

test.constructor <- function(){    
	prun <- makeObject()
	checkInherits(prun, "PortfolioRun")
	checkSame("TestRun", prun$name())
	checkSame(curvesPath, prun$curvesDirectory())
	checkSame("TestPortfolio1", prun$groupName())
	shouldBomb(PortfolioRun())
}

test.objective <- function(){
	prun <- makeObject()
	prun$objective(NetProfit)
	checkSame(NetProfit, prun$objective())
	shouldBomb(prun$objective(NetProfit))	
}

test.diversificationScore <- function(){
	prun <- makeObject()
	prun$loadCurves()
	childNames <- prun$curveNames()
	divScore <- c(2,8)
	names(divScore) <- childNames
	prun$diversityScore(as.list(divScore))
	checkSame(c(2,8), prun$diversityScore())
}

test.startEndDates <- function(){
	prun <- makeObject()
	checkSame(NULL, prun$startDate())
	checkSame(NULL, prun$endDate())
	prun$dateRange(start='2006-01-01', end='2007-12-31')
	checkSame(as.POSIXct('2006-01-01'), prun$startDate())
	checkSame(as.POSIXct('2007-12-31'), prun$endDate())
}

test.loadCurves <- function(){
	prun <- makeObject()
	prun$loadCurves()
	checkSame(245905, last(prun$childCurvesEquity(curves='raw')[,"TEST1_TestSystem1_daily_1.0:TestPV1"]))
	checkSame(prun$childCurvesEquity(curves='raw'), prun$childCurvesEquity(curves='cut'))
	shouldBomb(prun$loadCurves())
	shouldBomb(prun$dateRange(start='2006-01-01', end='2007-12-31'))	#Currently cannot update the range after curves loaded
	expected <- c("TEST1_TestSystem1_daily_1.0:TestPV1","TestSubGroup1")
	checkSame(TRUE, !any(is.na(match(prun$curveNames(),expected))))
	expected <- last(prun$childCurvesEquity(curves='raw')[,1]) + last(prun$childCurvesEquity(curves='raw')[,2])
	checkSame(expected, last(prun$portfolioCurveEquity(source='original',curves='raw')))
	shouldBomb(prun$portfolioCurveEquity(source='bad'))
	
	checkSameLooking(prun$childCurvesEquity(),prun$allCurvesEquity()[,2:3])
	checkSameLooking(prun$portfolioCurveEquity(),prun$allCurvesEquity()[,1])
	
	checkSame(FALSE, prun$.nullCurve('original','raw','slipped'))
	checkSame(TRUE, prun$.nullCurve('optimal','raw','slipped'))

	#Confirm that setting the curves is the same as loading.
	loadedCurves <- prun$childWeightedCurves('original','raw','noslip')
	prunSetCurves <- makeObject()
	prunSetCurves$loadCurves(loadedCurves)
	checkSame(prun$childWeightedCurves('original','raw','noslip'), prunSetCurves$childWeightedCurves('original','raw','noslip'))
	
	prun$unloadCurves()
	checkSame(NULL, prun$curveNames())
	
	#Load with cut dates
	prun$dateRange(start='2006-01-01', end='2007-12-31')
	prun$loadCurves()
	checkSameLooking(as.POSIXct('2006-01-03'), first(index(prun$portfolioCurve('original','cut','slipped')$pnl())))
	checkSameLooking(as.POSIXct('2007-12-31'), last(index(prun$portfolioCurve('original','cut','slipped')$pnl())))
	checkSameLooking(as.POSIXct('2003-09-11'), first(index(prun$portfolioCurve('original','raw','noslip')$pnl())))
	checkSameLooking(as.POSIXct('2008-05-27'), last(index(prun$portfolioCurve('original','raw','noslip')$pnl())))
	
	#Load with raw Range
	prun$unloadCurves()
	prun$dateRange(start='2006-01-01', end='2007-12-31')
	prun$loadCurves(rawRange=Range("2005/01/01", "2008/01/31"))
	checkSameLooking(as.POSIXct('2005-01-03'), first(index(prun$portfolioCurve('original','raw','noslip')$pnl())))
	checkSameLooking(as.POSIXct('2008-01-31'), last(index(prun$portfolioCurve('original','raw','noslip')$pnl())))
	checkSameLooking(as.POSIXct('2006-01-03'), first(index(prun$portfolioCurve('original','cut','noslip')$pnl())))
	checkSameLooking(as.POSIXct('2007-12-31'), last(index(prun$portfolioCurve('original','cut','noslip')$pnl())))
}

test.slippageCurves <- function(){
	prun <- makeObject()
	prun$loadCurves()
	checkSame(prun$childCurvesEquity(curves='cut', slip='noslip'), prun$childCurvesEquity(curves='cut', slip='slipped'))
	shouldBomb(prun$slipCurves(list(TestSubGroup1=0.5)))	#Must have all cuvres defined
	slippages <- list(0.5, 0.25)
	names(slippages) <- rev(prun$curveNames())
	prun$slipCurves(slippages)
	checkSame(as.numeric(rev(slippages)),prun$slippages())
	checkSame(TRUE, all(na.omit(prun$childCurvesEquity(curves='cut', slip='slipped') <= prun$childCurvesEquity(curves='cut'))))
	
	cut <- prun$childWeightedCurves(source='original',curves='cut')[['TestSubGroup1']]$pnl()
	expected <- cut - 0.25*abs(cut)
	checkSame(expected, prun$childWeightedCurves(source='original',curves='cut',slip='slipped')[['TestSubGroup1']]$pnl())
}

test.minMaxWeights <- function(){
	prun <- makeObject()
	prun$loadCurves()
	mins <- list(0.2)
	names(mins) <- rev(prun$curveNames())[1]
	maxs <- list(3)
	names(maxs) <- prun$curveNames()[1]
	prun$minMaxWeights(mins, maxs)
	checkSame(c(0,0.2), prun$minMaxWeights()$mins)
	checkSame(c(3,1), prun$minMaxWeights()$maxs)	
}

test.optimization <- function() {
	prun <- makeObject()
	prun$dateRange(start=as.POSIXct('2003-09-11'), end=as.POSIXct('2008-05-27'))	
	prun$loadCurves()
	prun$objective(NetProfit)
	prun$penalty(penalty=10000000000)
	
	
	mins <- list(0,0)
	names(mins) <- prun$curveNames()
	maxs <- list(2,2)
	names(maxs) <- prun$curveNames()
	prun$minMaxWeights(mins, maxs)
	
	prun$addConstraint('Portfolio',MaxDrawDown,-200000,0)
	expected <- appendSlowly(list(),list(type='Portfolio',metric=MaxDrawDown,min=-200000,max=0))
	checkSame(expected, prun$constraintList())
	
	seeds <- list(1,1)
	names(seeds) <- prun$curveNames()
	prun$seeds(seeds)
	checkSame(as.numeric(seeds), prun$seeds())
	prun$optimize(fastRestarts=1, slowRestarts=1, slowTol=10^-3)
	checkSame(c(0.0001, 1.3423), round(prun$optimalWeights(),4))
	
	#Compare versus doing directly
	expectedObjFunction <- PortfolioObjectiveFunction(NetProfit)
	expectedObjFunction$curves(curves=prun$childWeightedCurves(type='original',curves='cut',slip='slipped'))
	expectedObjFunction$constrainPortfolio(MaxDrawDown,min=-200000,max=0)
	expectedObjFunction$penalty(10000000000)
	optRoutine <- OptimizationRoutine(type="constrOptim")
	optRoutine$upperWeights(upper=c(2,2))
	optRoutine$lowerWeights(lower=c(0,0))
	expected <- PortfolioOptimizer()
	expected$objectiveFunction(objectiveFunction=expectedObjFunction)
	expected$optimizationRoutine(optimizationRoutine=optRoutine)
	expected.weights <- expected$optimize(start=c(1,1), fastRestarts=1, slowRestarts=1, slowTol=10^-3)
	checkSame(expected.weights,prun$optimalWeights())
	
	checkSame(prun$optimalSource(),'optimizer')  #When the optimizer is done, generate optimal cuvres and port at the new weights
	shouldBomb(prun$addConstraint('Portfolio',MaxDrawDown,-200000,0)) #cannot add if already optimized
	
	#Make sure the child curves from optimal = original * weights 
	expected <- prun$optimalWeights()[2] * prun$childWeightedCurves(source='original',curves='cut',slip='slipped')[["TEST1_TestSystem1_daily_1.0:TestPV1"]]$pnl()
	actual <- prun$childWeightedCurves(source='optimal',curves='cut',slip='slipped')[["TEST1_TestSystem1_daily_1.0:TestPV1"]]$pnl()
	checkSame(round(expected,10), round(actual,10))
	
	expected2 <- prun$optimalWeights()[1] * prun$childWeightedCurves(source='original',curves='cut',slip='slipped')[["TestSubGroup1"]]$pnl()
	actual2 <- prun$childWeightedCurves(source='optimal',curves='cut',slip='slipped')[["TestSubGroup1"]]$pnl()
	checkSame(round(expected2,10), round(actual2,10))
	
	#check that the optimal portfolio is the sum of the children
	x <- prun$childCurvesEquity(source='optimal',curves='cut',slip='slipped')
	x[is.na(x)] <- 0
	y <- x[,1] + x[,2]
	checkSame(round(y,10), round(prun$portfolioCurveEquity(source='optimal',curves='cut',slip='slipped'),10))
	
	#check that setting weights by hand is same as optimized
	optimizerWeights <- prun$optimalWeights()
	names(optimizerWeights) <- prun$curveNames()
	optimizerEquity <- prun$portfolioCurveEquity(source='optimal',curves='cut',slip='slipped')
	
	shouldBomb(prun$setOptimalWeights(optimizerWeights))
	manual <- makeObject()
	manual$loadCurves()
	manual$setOptimalWeights(optimizerWeights)
	checkSame('manual',manual$optimalSource())
	checkSame(optimizerEquity, manual$portfolioCurveEquity(source='optimal',curves='cut',slip='slipped'))
	
	###########################################
	#  tests for PortfolioRunManager.R
	###########################################
	
	testFileName <- squish(curvesPath, 'testPortfolioRunManager.csv')
	if(file.exists(testFileName)) file.remove(testFileName)
	PortfolioRunManager()$saveRun(prun, testFileName)
	test <- PortfolioRunManager()$getRun(testFileName)
	target <- data.frame(RunName = 'TestRun',
						 StartDate = as.POSIXct('2003-09-11'),
						 EndDate = as.POSIXct('2008-05-27'),
						 Seeds.TestSubGroup1 = 1,
						 'Seeds.TEST1_TestSystem1_daily_1.0:TestPV1' = 1,
						 Con1.MC.MetricName = 'MaxDrawDown',
						 Con1.MC.Min = -200000,
						 Con1.MC.Max = 0,
						 Weights.Min.TestSubGroup1 = 0,
					 	 'Weights.Min.TEST1_TestSystem1_daily_1.0:TestPV1' = 0,
						 Weights.Max.TestSubGroup1 = 2,
						 'Weights.Max.TEST1_TestSystem1_daily_1.0:TestPV1' = 2,
						 Weights.Optimal.TestSubGroup1 = 8.706114e-05,
						 'Weights.Optimal.TEST1_TestSystem1_daily_1.0:TestPV1' = 1.34229866923717)
	names(target) <- c('RunName', 'StartDate', 'EndDate', 'Seeds.TestSubGroup1', 'Seeds.TEST1_TestSystem1_daily_1.0:TestPV1',
					   'Con1.MC.MetricName', 'Con1.MC.Min', 'Con1.MC.Max', 'Weights.Min.TestSubGroup1', 'Weights.Min.TEST1_TestSystem1_daily_1.0:TestPV1',
					   'Weights.Max.TestSubGroup1', 'Weights.Max.TEST1_TestSystem1_daily_1.0:TestPV1', 'Weights.Optimal.TestSubGroup1',
					   'Weights.Optimal.TEST1_TestSystem1_daily_1.0:TestPV1')
	checkSame(target, test$outputData)
	outputCurve <- prun$portfolioCurveEquity(source = 'optimal', curves = 'cut', slip = 'slipped')
	names(index(outputCurve)) <- NULL
	checkSame(test$portfolioEquity, outputCurve)
	file.remove(testFileName)
}

test.optimization.MetricAllocation <- function(){
	prun <- makeObject()
	prun$dateRange(start=as.POSIXct('2003-09-11'), end=as.POSIXct('2008-05-27'))	
	prun$loadCurves()
	prun$objective(NetProfit)
	prun$addConstraint('Portfolio',MaxDrawDown,-200000,0)	
	prun$optimType('metricAllocation')
	seeds <- list(1,1)
	names(seeds) <- prun$curveNames()
	prun$seeds(seeds)	
	prun$optimize()
	expected <- c(-2.6012453,0.9638838)
	names(expected) <- names(seeds)
	checkSame(round(prun$optimalWeights(),7),expected)
	
	#Now with Diversity object
	prun <- makeObject()	
	div <- Diversity('TestPortfolio1')
	prun$params(list(diversity=div, methodName='PrincipalComponents', 
					diversity.params=list(weights=list(TestSubGroup1=0.25,TEST1_TestSystem1_daily_1.0TestPV1=0.75))))	
}

test.metrics <- function(){
	prun <- makeObject()
	prun$loadCurves()
	weights <- list(0.75,0.25)
	names(weights) <- prun$curveNames()
	prun$setOptimalWeights(weights)
	
	metrics <- list(NetProfit, MaxDrawDown)
	prun$calculateMetrics(metrics, verbose=TRUE)
	checkSame(-108556.875, prun$metric('optimal','raw','noslip','Portfolio','MaxDrawDown'))
	checkSame(-68339.53125, prun$metric('optimal','cut','slipped','TestSubGroup1','NetProfit'))
	checkSame(-91119.375, prun$metric('original','cut','slipped','TestSubGroup1','NetProfit'))
	
	expected <- c(-6863.28125, -108556.875)
	names(expected) <- c('NetProfit','MaxDrawDown')
	checkSame(expected, prun$metric('optimal','raw','slipped','Portfolio'))
}

test.optimType <- function(){
	prun <- makeObject()
	prun$optimType("nllib")
	checkSame("nllib", prun$optimType())
}

test.params <- function(){
	prun <- makeObject()
	params <- list(eval.max=20000,iter.max=20000,trace=5,rel.tol=10^-5) 
	checkSame(params, prun$params(params=params))	
}

test.optimizationInitialization <- function(){
	prun <- makeObject()
	
	#These are private functions but they show the bombing patterns so you know how to initialize in order.
	#The following should fail on Initialize Portfolio Objective Function, the addConstraint causes the initialize to run.
	shouldBombMatching(prun$addConstraint('Portfolio',MaxDrawDown,-200000,0), "Objective metric must be set")
	prun$objective(NetProfit)
	shouldBombMatching(prun$.initializePortfolioObjectiveFunction(), "Curves must be loaded")
	prun$loadCurves()
	
	#Initialize Optimization Routine
	#Cause the optimizationRoutine to get initialized and ensure that the inputs cannot change
	prun$.initializeOptimizationRoutine()
	shouldBomb(prun$optimType("nllib"))
	mins <- list(0,0)
	names(mins) <- prun$curveNames()
	maxs <- list(2,2)
	names(maxs) <- prun$curveNames()
	shouldBomb(prun$minMaxWeights(mins, maxs))
	shouldBomb(prun$params(params=list(eval.max=20000,iter.max=20000,trace=5,rel.tol=10^-5)))

	#Initialize optimization object
	prun$.initializeOptiObject()
	shouldBomb(prun$optimType("nllib"))
	shouldBomb(prun$penalty(10000))
}

test.constraints <- function(){
	prun <- makeObject()
	prun$loadCurves()
	prun$objective(NetProfit)
	prun$.initializeOptimizationRoutine()
	prun$addConstraint("Portfolio",ConditionalTenPercentileDrawDown,min=-10,max=0)
	
	systems <- prun$curveNames()
	diversityScore <- c(1,3)
	summedVarConstraint <- SummedVarConstraint(systems,summedAlpha=0.1,varBudget = 100)
	for(system in seq_along(systems)) {
		percentScore <- diversityScore[[system]] / sum(diversityScore)
		summedVarConstraint$set(systems[system], -0.2, 0.2)
	}
	prun$addConstraint(summedVarConstraint)
	objective <- list(list(type = 'Portfolio', metric = ConditionalTenPercentileDrawDown, min = -10, max = 0),
					  list(type = 'WeightedCurve', report = 'Summed VaR Constraint\nSummed Var:TEST1_TestSystem1_daily_1.0:TestPV1 max= 0.2 min=-0.2\nSummed Var:TestSubGroup1 max= 0.2 min=-0.2\n'))
	checkSame(objective, prun$constraintList())			  
	objFunction <- prun$.objFct
	
	checkSame(summedVarConstraint, objFunction$.weightedCurvesConstraints[[1]])
	checkSame(MetricConstraint(ConditionalTenPercentileDrawDown, -10, 0), objFunction$.portfolioConstraints[[1]])
	
	seeds <- list(1,1)
	names(seeds) <- prun$curveNames()
	prun$seeds(seeds)	
	prun$setupOptimizer()
	checkSame(objFunction, prun$optimizer()$objectiveFunction())
}
