library(JavaUtils)

setUp <- tearDown <- function() {
    conn <- SQLConnection()
    conn$init()
    conn$query("delete from time_series_data_scratch where data_source_id = 4")
}

testDataUpload <- function() {
    filename <- system.file("testdata/good_uploader_data.csv", package="JavaUtils")
    checkTrue(DataUpload$upload(filename))
    
    message("This exception is expected: \n")
    filename <- system.file("testdata/bad_uploader_data.csv", package="JavaUtils")
    checkFalse(DataUpload$upload(filename))
}

test.DataUpload.uploadToDB <- function(){
	
	checkSame(FALSE, uploadToDB(c('MyDB','myDB2'),'MyTable','MyField1',3, commitToDB=FALSE))
	checkSame(FALSE, uploadToDB('MyDB',c('table1','table2'), 'fiels1', 3, commitToDB=FALSE))
	checkSame(FALSE, uploadToDB('MyDB','MyTable',c('Field1','Field2'), 3, commitToDB=FALSE))
	checkSame(TRUE, is.character(uploadToDB('MyDB','MyTable','Field1',3, commitToDB=FALSE)))	
	checkSame(TRUE, is.character(uploadToDB('MyDB',c('MyTable','MyTable2'),c('Field1','Field2'),c(3,4),commitToDB=FALSE)))
	checkSame(TRUE, is.character(uploadToDB('MyDB','MyTable',c('Field1','Field2'),c(3,4),commitToDB=FALSE)))
	
	outFilename <- uploadToDB('MyDB','MyTable',c('Field1','Field2'), matrix(c(1,2,3,4),nrow=2), commitToDB=FALSE)
	outFile <- read.csv(outFilename)
	testdataPath <- squish(system.file("testdata", package="JavaUtils"),'/')
	benchFile <- read.csv(squish(testdataPath,'TSDB_upload_test_01.csv'))	
	checkSame(outFile,benchFile)
}