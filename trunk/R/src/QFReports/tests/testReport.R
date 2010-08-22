
library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/Report/')

tempFilename <- function(){
	squish(testdata,'testPnlReport_',round(abs(rnorm(1))*1000,4))
}

test.Constuctor <- function(){
	testFile <- tempFilename()
	report <- Report('TestReport',testFile)	
	shouldBomb(Report())	
	checkSame(squish(testFile,".html"), report$filenames()$html)
	checkSame('TestReport', report$.name)
	checkSame(testFile, report$.filename)
}

test.addTitle <- function(){
	testFile <- tempFilename()
	report <- Report('TestReport',testFile)	
	report$addTitle('Pnl Analysis ')
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testReport_addTitle.html'),deleteFile=FALSE, startLine=5)
}

test.addPlotLink <- function(){
	report <- Report('TestReport',tempFilename())
	report$addPlotLink('Plots')
	report$closeConnection()
}

test.addGroupTags <- function(){
	pnl <- PnlAnalysis()
	pnl$addPnlGroup("Group1",c("Test.Tag1"))
	pnl$addPnlGroup("Group2", PerformanceDB$tagsForTagGroup("TestTagGroup1"))
	pnl$loadPnl("Test_source1")
	pnl$loadAUM("Test.AUM1")
	testFile <- tempFilename()
	report <- Report('TestReport',testFile)
	report$addGroupTags(pnl)	
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testReport_addGroupTags.html'),deleteFile=FALSE, startLine=5)		
}