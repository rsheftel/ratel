library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/PnlReport/')

tempFilename <- function(){
	squish(testdata,'testPnlReport_',round(abs(rnorm(1))*1000,4))
}

analysisObject <- function(){
	pnl <- PnlAnalysis()
	pnl$addPnlGroup("Group1",c("Test.Tag1"))
	pnl$addPnlGroup("Group2", PerformanceDB$tagsForTagGroup("TestTagGroup1"))
	pnl$loadPnl("Test_source1")
	pnl$loadAUM("Test.AUM1")
	return(pnl)
}

test.Constuctor <- function(){
	shouldBomb(PnlReport())
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	checkInherits(report,"PnlReport")
	checkSame(squish(testFile,".html"), report$underlying()$filenames()$html)
}

test.addTitle <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	report$underlying()$addTitle('Pnl Analysis ')
	report$underlying()$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlReport_addTitle.html'),deleteFile=FALSE, startLine=5)
}

test.addGroupTags <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	report$underlying()$addGroupTags(pnl)
	report$underlying()$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlReport_addGroupTags.html'),deleteFile=FALSE, startLine=5)		
}

test.dailyPnl <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	report$addDailyPnl(pnl)
	report$underlying()$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlReport_addDailyPnl.html'),deleteFile=FALSE, startLine=5)		
}

test.dailyEquity <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	report$addDailyEquity(pnl)
	report$underlying()$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlReport_addDailyEquity.html'),deleteFile=FALSE, startLine=6)		
}

test.plots <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	
	report$underlying()$addPlotLink("Plots for Run")
	report$addPlotEquity(pnl)
	
	report$underlying()$closeConnection()
}

test.all <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlReport(testFile,pnl)
	
	report$underlying()$addTitle('Pnl Analysis ')
	report$underlying()$addPlotLink("Plots for Run")
	report$underlying()$addGroupTags(pnl)
	report$addDailyPnl(pnl)
	report$addPlotEquity(pnl)
	
	report$underlying()$closeConnection()
}