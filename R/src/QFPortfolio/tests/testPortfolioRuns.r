#
#	Test of PortfolioRuns class  (rsheftel)
#
#################################################################

library(QFPortfolio)
curvesPath <- squish(system.file("testdata", package="QFPortfolio"),'/PortfolioRun/testCurves/')

makeObject <- function(){
	return(PortfolioRuns(name="MultiRuns", groupName="TestPortfolio1", curvesDirectory=curvesPath, curvesExtension="csv"))
}

test.constructor <- function(){    
	pRuns <- makeObject()
	checkInherits(pRuns, "PortfolioRuns")
	checkSame(curvesPath, pRuns$curvesDirectory())
	checkSame("MultiRuns", pRuns$name())
	checkSame("TestPortfolio1", pRuns$groupName())
	shouldBomb(PortfolioRun())

	expected <-CurveGroup("TestPortfolio1")$childCurves(dir=curvesPath, extension="csv")
	checkSame(expected, pRuns$childWeightedCurves())
}

test.rawRange <- function(){
	pr <- PortfolioRuns(name="MultiRuns", groupName="TestPortfolio1", curvesDirectory=curvesPath, curvesExtension="csv", 
						rawRange=Range("2005/01/01", "2008/01/31"))
	wc <- pr$childWeightedCurves()
	checkSameLooking(as.POSIXct('2005-01-03'), first(index(wc[[1]]$pnl())))
	checkSameLooking(as.POSIXct('2008-01-31'), last(index(wc[[1]]$pnl())))
}

test.setupRuns <- function(){
	pRuns <- makeObject()
	checkSame(NULL, pRuns$currentRun())
	
	pRuns$addRun("First")
	checkSame("First", pRuns$currentRun()$name())
	checkInherits(pRuns$currentRun(), "PortfolioRun")
	
	pRuns$addRun("Second")
	checkSame("Second", pRuns$currentRun()$name())
	
	pRuns$addRun('Third', loadCurves = TRUE)
	checkSame('Third', pRuns$currentRun()$name())
	
	pRuns$useRun("First")
	checkSame("First", pRuns$currentRun()$name())
	
	prun <- pRuns$getRun("Second")
	checkSame("Second", prun$name())
	
	checkSame(c('First','Second','Third'), pRuns$runNames())
	
	#Set the curves of a child run
	loadedCurves <- pRuns$childWeightedCurves()
	prun$loadCurves(loadedCurves)
	pRuns$useRun('Second')
	checkSame(loadedCurves, pRuns$currentRun()$childWeightedCurves())
	
	prun3 <- pRuns$getRun('Third')
	checkSame(loadedCurves, prun3$childWeightedCurves())
}