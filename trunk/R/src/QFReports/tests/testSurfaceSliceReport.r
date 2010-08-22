# TODO: Add comment
# 
# Author: rsheftel
###############################################################################

library(STO)
library(QFReports)

testdata <- squish(system.file("testdata", package="QFReports"),'/SurfaceSliceReport/')

tempFilename <- function(){
	squish('testSurfaceSliceReport_',round(abs(rnorm(1))*1000,4))
}

stoObject <- function(){
	sto <- STO(squish(system.file("testdata", package="QFReports"),'/SurfaceSliceReport'), 'TestSurface', calculateMetrics=FALSE)
	return(sto)
}

test.Constructor <- function(){
	shouldBomb(SurfaceSliceReport())
	sto <- stoObject()
	testFile <- tempFilename()
	report <- SurfaceSliceReport(htmlDirectory= testdata, 
								 filename= testFile,
								 sto= sto,
								 imagesDirectory= squish(testdata,'images/'),
								 msiv=sto$msivs()[[1]],
								 metric=QNetProfit,
								 axes=c('Param1','Param2'))
	checkInherits(report,"SurfaceSliceReport")
	checkSame(squish(testdata,testFile,".html"), report$filenames()$html)
	checkSame(squish(testdata,'images/',testFile,'_contour.png'), report$filenames()$contour)
	checkSame(squish(testdata,'images/',testFile,'_multiLine.png'), report$filenames()$multiLine)
	
	#If no image directory supplied, it is the same as html
	report <- SurfaceSliceReport(htmlDirectory= testdata, filename= testFile, sto= sto, msiv=sto$msivs()[[1]], metric=QNetProfit, axes=c('Param1','Param2'))
	checkSame(squish(testdata,testFile,'_contour.png'), report$filenames()$contour)
	checkSame(squish(testdata,testFile,'_multiLine.png'), report$filenames()$multiLine)
}

test.dataGrid <- function(){
	sto <- stoObject()
	testFile <- tempFilename()
	report <- SurfaceSliceReport(htmlDirectory= testdata, filename= testFile, sto= sto, imagesDirectory= squish(testdata,'images/'),
		msiv=sto$msivs()[[1]], metric=QNetProfit, axes=c('Param1','Param2'))
	report$addTitle()
	report$addDataGrid()
	report$closeConnection()
	hWriterFileMatches(report$filenames()$html, squish(testdata,'testSurfaceSliceReport_addDataGrid.html'),deleteFile=FALSE, startLine=5)
}

test.reportAll <- function(){
	sto <- stoObject()
	testFile <- tempFilename()
	report <- SurfaceSliceReport(htmlDirectory= testdata, filename= testFile, sto= sto, imagesDirectory= squish(testdata,'images/'),
		msiv=sto$msivs()[[1]], metric=QNetProfit, axes=c('Param1','Param2'))
	report$addTitle()
	report$addContourPlot()
	report$addMultiLinePlot()
	report$addDataGrid()
	report$closeConnection()
	#report$openReport()
}