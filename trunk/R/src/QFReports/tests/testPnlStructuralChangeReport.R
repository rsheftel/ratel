library(QFReports)

directory <- system.file("testdata/GroupCurves", package="QFPortfolio")

testdata <- squish(system.file("testdata", package="QFReports"),'/PnlStructuralChangeReport/')

pnlStructuralChange <- PnlStructuralChange('TestCurveGroup',directory,'csv')
curvesLive <- pnlStructuralChange$getCurves('live')	
curvesBacktest <- pnlStructuralChange$getCurves('backtest')

tempFilename <- function(){
	squish(testdata,'testPnlStructuralChangeReport_',round(abs(rnorm(1))*1000,4))
}

test.Constuctor <- function(){	
	this <- PnlStructuralChangeReport('PnlStructuralChange','TestCurveGroup')
	checkSame(this$.report$.filename,'TestCurveGroup')
	checkSame(this$.report$.name,'PnlStructuralChange')
	checkSame(this$.report,this$underlying())
}

test.addKSGrid <- function(){
	
	testFile <- tempFilename()
	this <- PnlStructuralChangeReport('PnlStructuralChange',testFile)
	ksColnames <- c('Group','PValue','NObsLive','NObsBacktest','ObsRatio (%)')
	this$addKSGrid('KS Grid - H0: Live = Backtest',curvesLive,curvesBacktest,'two.sided',ksColnames)
	this$underlying()$closeConnection()
	
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlStructuralChangeReport_addKSGrid.html'),deleteFile=FALSE, startLine=5)
}

test.addPlot <- function(){
	this <- PnlStructuralChangeReport('PnlStructuralChange',tempFilename())
	this$underlying()$addTitle('Pnl Structural Change Report')
	this$underlying()$addPlotLink("Plots")
	this$addPlot(curvesLive,curvesBacktest)
	this$underlying()$closeConnection()
}