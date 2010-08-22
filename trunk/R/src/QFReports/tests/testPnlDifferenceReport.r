#
#	Test of PnlDifferenceReport class  (rsheftel)
#
#################################################################

library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/PnlDifferenceReport/')

tempFilename <- function(){
	squish(testdata,'testPnlDiffReport_',round(abs(rnorm(1))*1000,4))
}

analysisObject <- function(){
	pnl <- PnlAnalysis()
	pnl$addPnlGroup("Group1",c("Test.Tag1"))
	pnl$addPnlGroup("Group2", PerformanceDB$tagsForTagGroup("TestTagGroup1"))
	pnl$loadPnl("Test_source1")
	pnl$loadPnl("Test_source3")
	pnl$loadAUM("Test.AUM1")
	return(pnl)
}

test.Constuctor <- function(){
	shouldBomb(PnlDifferenceReport())
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	checkInherits(report,"PnlDifferenceReport")
	checkSame(squish(testFile,".html"), report$filenames()$html)
}

test.addTitle <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	report$addTitle()
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlDiffReport_addTitle.html'),deleteFile=FALSE, startLine=5)
}

test.addGroupTags <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	report$addGroupTags()
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlDiffReport_addGroupTags.html'),deleteFile=FALSE, startLine=5)		
}

test.differenceGrid <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	report$addDifferenceGrid(Range(as.POSIXct('1980-01-04'),as.POSIXct('1980-01-04')))
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlDiffReport_addDiffGrid.html'),deleteFile=FALSE, startLine=5)
	
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	report$addDifferenceGrid()
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlDiffReport_addDiffGridTotal.html'),deleteFile=FALSE, startLine=5)		
}

test.dailyDifference <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	report$addDailyDifference()
	report$closeConnection()
	hWriterFileMatches(squish(testFile,'.html'), squish(testdata,'testPnlDiffReport_addDailyDiff.html'),deleteFile=FALSE, startLine=6)
}

test.all <- function(){
	pnl <- analysisObject()
	testFile <- tempFilename()
	report <- PnlDifferenceReport(testFile,pnl,c("Test_source1",'Test_source3'))
	
	report$addTitle()
	report$addGroupTags()
	report$addDifferenceGrid()
	report$addDifferenceGrid(Range(as.POSIXct('1980-01-04'),as.POSIXct('1980-01-04')))
	report$addDailyDifference()
	
	report$closeConnection()
#	report$openReport()	
}
