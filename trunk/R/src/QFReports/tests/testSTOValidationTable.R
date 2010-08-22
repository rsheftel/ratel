# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
library(STO)
library(QFReports)


testSTOValidationTable <- function(){
		
	testSTOValidationTableDirectory <- squish(system.file("testdata", package="QFReports"), '/STOValidationTable')

	sto <- STO(testSTOValidationTableDirectory, '20090115_in', calculateMetrics = FALSE)
	msiv <- sto$msivs()[[1]]
	
	STOValidationTable(sto)$createFiles(msiv,squish(testSTOValidationTableDirectory,'/20090115_in/testSTOValidationTable'), 
										csvFilePath = squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTable.csv'))
			
	hWriterFileMatches(squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTable.html'),
						squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTableComp.html'),
						deleteFile=TRUE, startLine=8)
	
	file.remove(squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTable.pdf'))
	
	currentFile <- read.csv(squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTable.csv'))
	compFile <- read.csv(squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTableComp.csv'))
	file.remove(squish(testSTOValidationTableDirectory, '/20090115_in/testSTOValidationTable.csv'))
		
	checkSame(currentFile, compFile)
}
