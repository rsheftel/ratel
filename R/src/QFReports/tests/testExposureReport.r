# ExposureReport tests
# 
# Author: rsheftel
###############################################################################

library(QFReports)
library(QFPortfolio)
testdata <- squish(system.file("testdata", package="QFReports"),'/ExposureReport/')

makeExposure <- function(){
	ep <- Exposure('TestExposure')
	curvesDir <- squish(system.file("testdata", package="QFPortfolio"),'/Exposure/curves/')
	ep$loadCurves(curvesDir)
	return(ep)
}

test.constructor <- function(){
	shouldBomb(ExposureReport())
	report <- ExposureReport(makeExposure())
	checkInherits(report, "ExposureReport")
	
	report <- ExposureReport(makeExposure(),squish(testdata,'newreport'))
	checkSame(squish(testdata,'newreport.html'),report$filenames()$html)
}

test.addTitle <- function(){
	report <- ExposureReport(makeExposure(),squish(testdata,'test_addTitle'))
	report$addTitle()
	report$closeConnection()
	hWriterFileMatches(squish(report$filenames()$html), squish(testdata,'addTitle.html'),deleteFile=FALSE,startLine=6)
}

test.addMetricFrame <- function(){
	ep <- makeExposure()
	report <- ExposureReport(ep,squish(testdata,'test_addMetricFrame'))
	ep$range(Range('2009-04-13','2009-05-29'))
	report$addMetricFrame(aggregationLevels=c('system','pv'),metrics=list(NetProfit,DailyStandardDeviation),
										weights=NULL,percentages=TRUE,allRow=TRUE)
	report$closeConnection()
	hWriterFileMatches(squish(report$filenames()$html), squish(testdata,'addMetricFrame.html'),deleteFile=FALSE,startLine=6)
}

test.addCorrelations <- function(){
	ep <- makeExposure()
	report <- ExposureReport(ep,squish(testdata,'test_addCorrelations'))
	report$addCorrelations(aggregationLevels=c('system','pv'),weights='weight')	
	report$closeConnection()
	hWriterFileMatches(squish(report$filenames()$html), squish(testdata,'addCorrelations.html'),deleteFile=FALSE,startLine=6)
}
