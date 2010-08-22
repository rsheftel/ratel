# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

library(QFReports)

testSTOSelectedRunsTable <- function(){
	
	testSTOSelectedRunsTableDirectory <- squish(system.file("testdata", package="QFReports"), '/STOSelectedRunsTable')
	
	sto <- STO(testSTOSelectedRunsTableDirectory, '20090115_in', calculateMetrics = FALSE)
	msiv <- sto$msivs()[[1]]
	
	STOSelectedRunsTable$createFiles(sto, msiv, runNums = c(1,2,3), filePath = squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns'))
	
	currentTable <- read.csv(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns.html'))
	compTable <- read.csv(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRunsComp.html'))
	
	#need to be careful to remove the Directory and the dateStamp
	checkSame(as.character(currentTable[8:240,1]), as.character(compTable[8:240,1]))
	
	STOSelectedRunsTable$createFiles(sto, msiv, textFilter = 'HalfLife < 8 & ExitLevel == 60', 
			filePath = squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns2'))
	
	currentTable2 <- read.csv(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns2.html'))
	compTable2 <- read.csv(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRunsComp2.html'))
	 	
	checkSame(as.character(currentTable2[8:210,1]), as.character(compTable2[8:210,1]))
	
	file.remove(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns.html'))
	file.remove(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns.pdf'))
	file.remove(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns2.html'))
	file.remove(squish(testSTOSelectedRunsTableDirectory, '/20090115_in/testSTOSelectedRuns2.pdf'))
	
}
