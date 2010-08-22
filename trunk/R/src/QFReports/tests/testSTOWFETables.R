# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

library(QFReports)

testSTOWFETables <- function(){
	
	testSTOWFETablesDirectory <- squish(system.file("testdata", package="QFReports"), '/STOWFETables')
	
	fileLocation <- testSTOWFETablesDirectory
	
	stoIn <- STO(testSTOWFETablesDirectory, '20090115_in', calculateMetrics = FALSE)
	stoOut <- STO(testSTOWFETablesDirectory, '20090115_out', calculateMetrics = FALSE)
	msivIn <- stoIn$msivs()[[1]]
	cubeIn <- stoIn$metrics()
	cubeOut <- stoOut$metrics()
	
	STOWFETables$createValidationTables(stoIn, stoOut, msivIn, cubeIn = cubeIn, cubeOut = cubeOut, fileLocation = fileLocation)
		
	currentValidTableDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLDiff.html'))
	compValidTableDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLDiffComp.html'))
	
	currentValidTablePercentDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLPercentDiff.html'))
	compValidTablePercentDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLPercentDiffComp.html'))
	
	runNumsIn <- c(1,2,3)
	STOWFETables$createSelectedRunsTables(stoIn, stoOut, msivIn, runNumsIn, cubeIn = cubeIn, cubeOut = cubeOut, fileLocation = fileLocation)
	
	currentSelectedRunsTableDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsDiff.html'))
	compSelectedRunsTableDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsDiffComp.html'))
	
	currentSelectedRunsTablePercentDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsPercentDiff.html'))
	compSelectedRunsTablePercentDiff <- read.csv(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsPercentDiffComp.html'))
	

	hWriterFileMatches(squish(fileLocation, '/RSITargets_1.0_daily_ALLDiff.html'), 
					   squish(fileLocation, '/RSITargets_1.0_daily_ALLDiffComp.html'))
	hWriterFileMatches(squish(fileLocation, '/RSITargets_1.0_daily_ALLPercentDiff.html'),
					   squish(fileLocation, '/RSITargets_1.0_daily_ALLPercentDiffComp.html'))
	
	hWriterFileMatches(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsDiff.html'), 
				       squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsDiffComp.html'), startLine = 17)
	hWriterFileMatches(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsPercentDiff.html'),
					   squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsPercentDiffComp.html'), startLine = 17)
	
	file.remove(squish(fileLocation, '/RSITargets_1.0_daily_ALLDiff.html'))
	file.remove(squish(fileLocation, '/RSITargets_1.0_daily_ALLPercentDiff.html'))
	file.remove(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsDiff.html'))
	file.remove(squish(fileLocation, '/RSITargets_1.0_daily_ALLSelectedRunsPercentDiff.html'))
}


