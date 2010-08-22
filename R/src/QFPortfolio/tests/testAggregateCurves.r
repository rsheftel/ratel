# AggregateCurves tests
# 
# Author: RSheftel
###############################################################################

library(QFPortfolio)

testdata <- squish(system.file("testdata", package="QFPortfolio"),'/AggregateCurves/')

test.constructor <- function(){
	checkInherits(AggregateCurves('TestExposure'), "AggregateCurves")
	checkSame(AggregateCurves('TestExposure',verbose=FALSE)$groupName(), 'TestExposure')
	shouldBombMatching(AggregateCurves('BadGroupName'), 'bad groupName in AggregateCurves')
}

test.weights <- function(){
	ac <- AggregateCurves('TestExposure')
	checkSame(ac$smash()$weight, rep(1,5))
	
	ac$weights(9)
	checkSame(ac$smash()$weight, rep(9,5))
	
	ac$weights(data.frame(system='TestSystem1',weight=55))
	sm <- ac$smash()
	checkSame(sm[sm['system']=='TestSystem1',]$weight, c(55,55,55))
	checkSame(sm[sm['system']=='TestSystem2',]$weight, c(9,9))
	
	ac$weights(data.frame(system='TestSystem1',pv='Slow',weight=2))
	sm <- ac$smash()
	checkSame(sm[sm['pv']=='Slow',]$weight, c(2,2))
	checkSame(sum(ac$smash()$weight), (2+2+55+9+9))
}

test.loadCurves <- function(){
	ac <- AggregateCurves(groupName='TestExposure')
	ac$loadCurves(squish(testdata,'curves/'),'bin')
	
	checkSame(names(ac$atomicCurves()), as.character(ac$smash()$msivpv))
	checkInherits(first(ac$atomicCurves()),'PositionEquityCurve')
}

test.atomicCurves <- function(){
	ac <- AggregateCurves(groupName='TestExposure') 
	ac$loadCurves(squish(testdata,'curves/'),'bin')
	curves <- ac$atomicCurves(aggregationLevels='system')
	checkSame(names(curves),as.character(unique(ac$smash()$system)))
	checkSame(names(curves$TestSystem1), as.character(ac$smash()[ac$smash()=='TestSystem1','msivpv']))
	
	curves <- ac$atomicCurves(aggregationLevels=c('system','pv'))
	checkSame(names(curves$TestSystem2_TestPV3), as.character(ac$smash()[ac$smash()$system=='TestSystem2'& ac$smash()$pv=='TestPV3','msivpv']))
}

test.weightedCurves <- function(){
	ac <- AggregateCurves(groupName='TestExposure') 
	ac$loadCurves(squish(testdata,'curves/'),'bin')
	allCurve <- ac$weightedCurves()
	checkInherits(allCurve,'WeightedCurves')
	checkSame(allCurve$curve()$metric(NetProfit),3731821)
	
	ac$weights(data.frame(system='TestSystem1',pv='Slow',weight=2))
	checkSame(round(ac$weightedCurves()$curve()$metric(NetProfit),0),5515149)
	
	ac$weights(data.frame(market='TEST.SP.1C',weight=10))
	curves <- ac$weightedCurves(aggregationLevels=c('system','market'))
	testCurves <- ac$atomicCurves()
	expected <- WeightedCurves(testCurves[3:4],c(10,10))
	checkSame(curves$TestSystem1_TEST.SP.1C$curve()$metric(NetProfit), expected$curve()$metric(NetProfit))
}

test.addFactor <- function(){
	ac <- AggregateCurves(groupName='TestExposure')
	ac$addFactors('NewFactor', data.frame(system=c('TestSystem1','TestSystem2'), NewFactor=c('first','second')))
	checkSame(as.character(ac$smash()$NewFactor), c('first','first','first','second','second'))
	
	ac$addFactors(c('NewFactor','NextFactor'), data.frame(system=c('TestSystem1','TestSystem2'), NewFactor=c('redone','second'), NextFactor=c(9,8)))
	checkSame(as.character(ac$smash()$NewFactor), c('redone','redone','redone','second','second'))
	checkSame(as.numeric(ac$smash()$NextFactor), c(9,9,9,8,8))
	
	ac$loadCurves(squish(testdata,'curves/'),'bin')
	curves <- ac$atomicCurves(aggregationLevels='NewFactor')
	checkSame(names(curves), c('redone','second'))
	checkSame(length(curves$redone), 3)
}
