# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
library(hwriter)
library(QFReports)

testData <- data.frame(Col1 = c(1,2,3),Col2 = c(4,5,6),Col3 = c(7,8,9), row.names = c('Row1','Row2','Row3'))

testDataComp <- squish(system.file("testdata", package="QFReports"), '/HWriterUtils/')

tempdir <- tempDirectory()

testHWriterUtils <- function(){
	filename <- squish(tempdir,'testHWriterUtils')
	hwu <- HWriterUtils(filename, pdf=FALSE)	
	hwrite(HWriterUtils$dataTable(testData), hwu$connection())
	hwu$closeConnection()
	testfile <- squish(filename, '.html')
	benchfile <- squish(testDataComp, '/testHWriterUtils.html')
	hWriterFileMatches(testfile, benchfile)		
}
