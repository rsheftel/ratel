library(QFReports)
directory <- system.file("testdata/GroupCurves", package="QFPortfolio")

test.Constuctor <- function(){
	this <- PnlStructuralChange('TestCurveGroup',directory,'csv')
	checkSame(this$.curvesDirectory,directory)
	checkSame(this$.groupName,'TestCurveGroup')
	checkSame(this$.curvesExtension,'csv')	
}

test.getKSFrame <- function(){
	this <- PnlStructuralChange('TestCurveGroup',directory,'csv')
	curvesLive <- this$getCurves('live')	
	curvesBacktest <- this$getCurves('backtest')
	colNames <- c('Group','PValue','NObsLive','NObsBacktest','ObsRatio (%)')
	res <- this$getKSFrame(curvesLive,curvesBacktest,'two.sided',colNames)
	expected <- data.frame('TestCurveGroup1',1,3,1,300)
	colnames(expected) <- colNames
	checkSame(res,expected)
	
	curves <- this$getCurves()	
	res <- this$getKSFrame(curves,curves,'two.sided',colNames)
	expected <- data.frame(c('TestCurveGroup1','TestCurveGroup2'),c(1,1),c(4,3),c(4,3),c(100,100))
	colnames(expected) <- colNames
	checkSame(res,expected)
}