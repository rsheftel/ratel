library(QFPortfolio)

jtest <- JTestGroups$by()

.setUp <- function() {
    jtest$setUp()
}

.tearDown <- function() {
    jtest$tearDown()
    jtest$releaseLock()
}

testGroupFiles <- function() {
    # pnls:
    # US.1C slow = 1
    # SP.1C Fast = 10
    # SP.1C Slow = 100
    # US.1C Fast = 1000
    # mega group is 2 * test + 4 * foob.  
    # foob is 7 * US.1C slow. test is 2 * SP fast + 0.5 * SP slow + 3 * US fast 
    
    # curve() returns total curve, with ALL weights applied
    # so, pnl is 2 * (2 * 10 + 0.5 * 100 + 3 * 1000) + 4 * (7 * 1) = 2 * 3070 + 4 * 7 = 6140 + 28
    curve <- CurveGroup("mega")$curve(system.file("testdata/GroupCurves", package="QFPortfolio"), extension="csv")
    checkSame(curve$metric(NetProfit), 6168)

    # childCurves() returns the top-level curves with no weighting, so multiplying by the weighting
    # gives you 2 * test(6140) + 4 * foob(28) = 12392
    childCurves <- CurveGroup("mega")$childCurves(system.file("testdata/GroupCurves", package="QFPortfolio"), extension="csv")
    weights <- CurveGroup("mega")$weights()
    checkSameLooking(weights, c(4, 2))
    unweightedCurves <- WeightedCurves(childCurves, sapply(weights, function(x) 1/x))$scaledCurves()
    unweightedPnls <- sapply(unweightedCurves, function(c) c$metric(NetProfit))
    checkSameLooking(c(7, 3070), unweightedPnls)
}

testGroupMembers <- function() {
	curve <- CurveGroup("TestPortfolio2")
	checkSame(c('TEST.SP.1C','TEST1','TEST4','TEST5'), curve$markets())
	checkSame(rep('TestSystem1',4), curve$systems(unique=FALSE))
	checkSame(c('TestSystem1'), curve$systems())
	checkSame(c('daily'), curve$intervals())
	checkSame(c('Fast','TestPV1'), curve$pvs())
	checkSame(rep('1.0',4), curve$versions(unique=FALSE))
	checkSame(c('TEST.SP.1C_TestSystem1_daily_1.0','TEST1_TestSystem1_daily_1.0','TEST4_TestSystem1_daily_1.0','TEST5_TestSystem1_daily_1.0'), 
						curve$msivs())
	checkSame(c('TestPortfolio1','TEST.SP.1C_TestSystem1_daily_1.0:Fast','TEST4_TestSystem1_daily_1.0:TestPV1'), curve$childNames())
	checkSame(CurveGroup('TestPortfolio1')$totalWeights(), c(1,0.75,0.75*0.5))
	checkSame(curve$filenames(), c("TestSystem1_1.0_daily_Fast_TEST.SP.1C","TestSystem1_1.0_daily_TestPV1_TEST1",
									"TestSystem1_1.0_daily_TestPV1_TEST4","TestSystem1_1.0_daily_TestPV1_TEST5" ))
	
	expected <- data.frame(	system 	= curve$systems(unique=FALSE),
							interval= curve$intervals(unique=FALSE),
							version = curve$versions(unique=FALSE),
							pv		= curve$pvs(unique=FALSE),
							market	= curve$markets(unique=FALSE),
							msiv	= curve$msivs(unique=FALSE),
							msivpv  = curve$msivpvs())						
	checkSame(curve$smash(), expected)
}

testCheckRangeType <- function() {
	shouldBomb(CurveGroup$checkRangeType('shouldBomb'))
	checkSame(CurveGroup$checkRangeType('live'),NULL)
}

testChildCurvesWithRangeTypes <- function() {
	checkZoos <- function(z1,z2){
		checkSame(as.numeric(z1),as.numeric(z2))	
		checkSame(as.character(index(z1)),as.character(index(z2)))
	}	
	zTest <- zoo(c(1,800,-1200,400),as.POSIXct(c('2007-01-01','2007-01-02','2007-01-03','2007-01-04')))
	directory <- system.file("testdata/GroupCurves", package="QFPortfolio")
	
	this <- CurveGroup('TestCurveGroup')
	curves <- this$childCurves(directory, extension = 'csv')
	checkSame(length(curves),2)
	checkZoos(curves[[1]]$pnl(),zTest+zTest+1)
	checkZoos(curves[[2]]$pnl(),zTest-1)
	curves <- this$childCurves(directory, extension = 'csv',rangeType = 'live')
	checkSame(length(curves),1)
	checkZoos(curves[[1]]$pnl(),zTest[2:4])
	curves <- this$childCurves(directory, extension = 'csv',rangeType = 'backtest')
	checkSame(length(curves),1)
	checkZoos(curves[[1]]$pnl(),zTest[1])
	
	this <- CurveGroup('TestCurveGroup1')
	curves <- this$childCurves(directory, extension = 'csv',rangeType = 'live')
	checkSame(length(curves),1)
	checkZoos(curves[[1]]$pnl(),zTest[2:4])
	curves <- this$childCurves(directory, extension = 'csv',rangeType = 'backtest')
	checkSame(length(curves),1)
	checkZoos(curves[[1]]$pnl(),zTest[1])
	
	this <- CurveGroup('TestCurveGroup2')
	curves <- this$childCurves(directory, extension = 'csv',rangeType = 'live')
	checkSame(length(curves),0)	
	curves <- this$childCurves(directory, extension = 'csv',rangeType = 'backtest')
	checkSame(length(curves),0)
}

test.curveList <- function(){
	directory <- system.file("testdata/GroupCurves", package="QFPortfolio")
	this <- CurveGroup('TestCurveGroup1')
	curveList <- this$curveList(directory, extension = 'csv')
	checkSame(names(curveList), c('TestSystem1_1.0_daily_TestPV_TEST4', 'TestSystem5_1.0_daily_TestPV_TEST4'))
	checkInherits(curveList[[1]], 'PositionEquityCurve')
}
