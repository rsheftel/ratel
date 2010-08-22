# Exposure tests
# 
# Author: RSheftel
###############################################################################

library(QFPortfolio)

testdata <- squish(system.file("testdata", package="QFPortfolio"),'/Exposure/')

smashCheck <- function(ep,expected){
	checkSame(sort(names(ep$smash())), sort(names(expected)))
	checkSame(all(ep$smash()==expected[,names(ep$smash())]),TRUE)
}

test.constructor <- function(){
	checkInherits(Exposure(), "Exposure")
	checkSame(Exposure(verbose=FALSE)$groupName(), 'AllSystemsQ')
}

test.weights <- function(){
	ep <- Exposure('TestCurveGroup')
	checkSame(ep$groupName(), 'TestCurveGroup')
	expected <- c(1,1)
	names(expected) <- c('TestCurveGroup1', 'TestCurveGroup2')
	checkSame(ep$weights(), expected)
	
	ep$weights(list(TestCurveGroup2=4, TestCurveGroup1=5))
	expected[1:2] <- c(5,4)
	checkSame(ep$weights(), expected)
	
	shouldBombMatching(ep$weights(list(BadName=1, TestCurveGroup2=1)), 'Not a child group name: BadName')
	
	ep$weights(list(TestCurveGroup1=99))
	expected['TestCurveGroup1'] <- 99
	checkSame(ep$weights(), expected)
}

test.smashFrame <- function(){
	ep <- Exposure('TestExposure')
	expected <- data.frame(	group=c('TestExposureSub1','TestExposureSub1','TestExposureSub1','TestExposureSub2','TestExposureSub2'),
							market=c('TEST.SP.1C','TEST.SP.1C','TEST.US.1C','RE.TEST.TU.1C','RE.TEST.TY.1C'), 
							system=c('TestSystem1','TestSystem1','TestSystem1','TestSystem2','TestSystem2'), 
							pv=c('Fast','Slow','Slow','TestPV3','TestPV3'),
							weight.base=c(1,0.5,0.5,1,1), weight.user=c(1,1,1,1,1), 
							weight = c(1,0.5,0.5,1,1),stringsAsFactors=FALSE)
	smashCheck(ep, expected)
}

test.sizingParameter <- function(){
	ep <- Exposure('TestExposure')
	ep$addSizingParameter()
	
	expected <- data.frame(	group=c('TestExposureSub1','TestExposureSub1','TestExposureSub1','TestExposureSub2','TestExposureSub2'),
		market=c('TEST.SP.1C','TEST.SP.1C','TEST.US.1C','RE.TEST.TU.1C','RE.TEST.TY.1C'), 
		system=c('TestSystem1','TestSystem1','TestSystem1','TestSystem2','TestSystem2'), 
		pv=c('Fast','Slow','Slow','TestPV3','TestPV3'), 
		weight.base=c(1,0.5,0.5,1,1), weight.user=c(1,1,1,1,1), 
		weight = c(1,0.5,0.5,1,1),stringsAsFactors=FALSE)
	expected$sizingParameter <- c(rep('TestParameter1',3),rep('TestStrategy3Param1',2))
	expected$sizingParameter.orig <- c(10,15,15,1,1)
	expected$sizingParameter.weighted <- c(10,15*0.5,15*0.5,1,1)
	smashCheck(ep,expected)
	
	weights <- list(TestExposureSub1=5, TestExposureSub2=10)
	expected$weight <- c(5,5*0.5,5*0.5,10,10)
	expected$weight.user <- c(5,5,5,10,10)
	expected$sizingParameter.weighted <- c(50,75*0.5,75*0.5,10,10)
	ep$weights(weights)
	smashCheck(ep,expected)
		
	expected <- data.frame(	system=c('TestSystem1','TestSystem1','TestSystem2'),
							pv = c('Fast','Slow','TestPV3'),
							parameter = c('TestParameter1','TestParameter1','TestStrategy3Param1'),
							original = c(10,15,1),
							weighted = c(50,75*0.5,10),
							weight.final = c(5,5*0.5,10),
							weight.user = c(5,5,10),
							weight.base = c(1,0.5,1))					
	checkSameLooking(ep$sizing(), expected)
}

test.riskDollars <- function(){
	ep <- Exposure('TestExposure')
	ep$addRiskDollars()
	
	expected <- data.frame(system=c('TestSystem1','TestSystem2'), riskDollars=c(2500,0.02))
	checkSameLooking(ep$riskDollars(),expected)
	checkSame(ep$riskDollars('system+pv',margins=TRUE), dget(squish(testdata,'riskDollars_system_pv.dput')))
	checkSame(ep$riskDollars('group+system',margins=TRUE), dget(squish(testdata,'riskDollars_group_system.dput')))
	checkSame(ep$riskDollars('market+system',margins=TRUE), dget(squish(testdata,'riskDollars_market_system.dput')))
	shouldBombMatching(ep$riskDollars('market+system+pv'), 'Only works for 2 or less aggregationLevels.')
} 	

test.sizingWriteCsv <- function(){
	ep <- Exposure('TestExposure')
	ep$weights(list(TestExposureSub1=5, TestExposureSub2=10))
	filename <- ep$writeSizingCsv(filename=squish(testdata,'sizing.csv'),asOfDate='19520105')
	fileMatches(filename, squish(testdata,'testSizingCsv.csv'))
}

test.aggregateCurves <- function(){
	ep <- Exposure('TestExposure')
	ep$loadCurves(squish(testdata,'curves/'))
	ep$weights(list(TestExposureSub1=5, TestExposureSub2=10))
	
	ep$aggregateCurvesWeights(2)
	checkSame(as.numeric(ep$aggregateCurvesObject()$smash()$weight), rep(2,5))
	ep$aggregateCurvesWeights('weight')
	checkSame(as.numeric(ep$aggregateCurvesObject()$smash()$weight), c(5,2.5,2.5,10,10))
	
	collapse <- ep$aggregateCurves(aggregationLevels=c('system','pv'), weights=NULL)
	checkInherits(collapse[[1]], 'WeightedCurves')
	collapse <- ep$aggregateCurves(aggregationLevels='system', weights='weight.user')
	checkSame(round(collapse$TestSystem1$curve()$metric(NetProfit),2), 8916642.45)
	checkSame(collapse$TestSystem2$curve()$metric(NetProfit), 19484925)
}

test.metricFrame <- function(){
	ep <- Exposure('TestExposure')
	ep$loadCurves(squish(testdata,'curves/'))
	
	expected <- data.frame(	id=c('TestSystem2_TestPV3', 'TestSystem1_Fast', 'TestSystem1_Slow','ALL','DiversityBenefit'),
							NetProfit=c(1948492, 0, 1783328, 3731821, 0),
							NetProfit.percent=c(0.52213, 0, 0.47787, 1, 0))
												
	mf <- ep$metricFrame(aggregationLevels=c('system','pv'),metrics=list(NetProfit,DailyStandardDeviation),
															weights=NULL,percentages=TRUE,allRow=TRUE)						
	checkSameLooking(expected$id, mf$id)
	checkSameLooking(expected$NetProfit, round(mf$NetProfit,0))
	checkSameLooking(expected$NetProfit.percent, round(mf$NetProfit.percent,5))
	
	ep$range(Range('2009-04-13','2009-05-29'))
	expected <- data.frame(	id=c('TestSystem2_TestPV3', 'TestSystem1_Fast', 'TestSystem1_Slow'), NetProfit=c(27800, 0, 71176))
	
	mf <- ep$metricFrame(aggregationLevels=c('system','pv'),metrics=list(NetProfit,DailyStandardDeviation),
															weights=NULL,percentages=FALSE,allRow=FALSE)						
	checkSameLooking(expected$id, mf$id)
	checkSameLooking(expected$NetProfit, round(mf$NetProfit,0))
}

test.correlations <- function(){
	ep <- Exposure('TestExposure')
	ep$loadCurves(squish(testdata,'curves/'))
	
	corrs <- ep$correlations(aggregationLevels=c('system','pv'))
	checkSame(round(corrs[3,2],5), -0.06094)
	
	ep$range(Range('2009-04-13','2009-05-29'))
	corrs <- ep$correlations(aggregationLevels=c('system','pv'))
	checkSame(round(corrs[3,2],5), -0.28919)
	
}

