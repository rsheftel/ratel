library("GSFCore")

source(system.file("testHelpers.R", package="GSFCore"))

tsdb <- TimeSeriesDB()
testEnv <- environment()
.setUp <- function() {
    assign("tsdb", TimeSeriesDB(), envir = testEnv)
    createTempDir("TSDB_upload/Today")
    createTempDir("TSDB_upload/Archive")
    createTempDir("TSDB_upload/Failures")
    deleteTestTimeSeries()
    createTestTimeSeries()
}

todayDir <- function() { path(baseDir(), "Today") }
archiveDir <- function() { path(baseDir(), "Archive", todayString()) }
baseDir <- function() { path(tempdir(), "TSDB_upload") }

todayString <- function() { strftime(as.POSIXlt(Sys.time()), "%Y%m%d") }

.tearDown <- function() {
    deleteTestTimeSeries()
    recreateSessionTempDir()
}

aapl <- TimeSeriesDB$retrieveOneTimeSeriesByName(
    name = "aapl close", 
    data.source = "yahoo", 
    start = "1999-01-01", 
    end = "1999-02-01"
)

testBatchUploadConstructsAndCreatesArchiveDirIfNeeded <- function() { 
    requireNoDirectory(archiveDir())
    checkInherits(TimeSeriesBatchUpload(baseDir()), "TimeSeriesBatchUpload")
    requireDirectory(archiveDir())
}


testNewTimeSeriesFormat <- function() {
    name <- rep("test-quantys close", 2*length(index(aapl)))
    source <- c(rep("test", length(index(aapl))), rep("bogus", length(index(aapl)))) 
    date <- rep(as.character(index(aapl)), 2)
    value <- rep(aapl, 2)
    df <- data.frame(name, source, date, value)
    write.csv(df, file=path(todayDir(), "test_data.csv"), row.names=FALSE)
    
    uploadAndCheck()
    
    colnames(df) <- c("name", "date", "source", "value")
    write.csv(df, file=path(todayDir(), "test_data.csv"), row.names=FALSE)
    failures <- TimeSeriesBatchUpload(baseDir())$upload()
    checkMatches(the(failures), "name, source, date, value")
}

uploadAndCheck <- function() {
    checkLength(TimeSeriesBatchUpload(baseDir())$upload(), 0)
    
    testData <- tsdb$retrieveOneTimeSeriesByName(
        name = "test-quantys close", 
        data.source = "test"
        )
    
    checkSame(aapl, testData)
    
    testData2 <- tsdb$retrieveOneTimeSeriesByName(
        name = "test-quantys close", 
        data.source = "bogus"
        )
    
    checkSame(aapl, testData2)
}
    
testTimeSeriesBatchUpload <- function() {
    TimeSeriesFile$writeOneTimeSeries(aapl, "test-quantys close", "test", path(todayDir(), "test_data.csv"))
    TimeSeriesFile$writeOneTimeSeries(aapl, "test-quantys close", "bogus", path(todayDir(), "test_data2.csv"))

    # testBatchUploadIgnoresNonCsvFiles
    file.create(path(todayDir(), "someOtherFile.oth"))

    checkFileCount(todayDir(), 3)
    requireNoDirectory(archiveDir())

    uploadAndCheck()

    checkFileExists(archiveDir(), "test_data.csv")
    checkFileExists(archiveDir(), "test_data2.csv")
    checkFileExists(todayDir(), "someOtherFile.oth")
    checkFileCount(todayDir(), 1)
    checkFileCount(archiveDir(), 2)

    # testbatchUploadFindsNoFiles

    checkLength(TimeSeriesBatchUpload(baseDir())$upload(), 0)
    checkFileCount(todayDir(), 1)
    checkFileCount(archiveDir(), 2)

}

testBadDataMovesFileToFailuresAndAllGoodDataFilesGetUploaded <- function() {
    TimeSeriesFile$writeOneTimeSeries(aapl, "test-quantys close", "test", path(todayDir(), "test_data.csv"))
    TimeSeriesFile$writeOneTimeSeries(aapl, "test-quantys close", "bogus", path(todayDir(), "test_data2.csv"))
    badPath1 <- path(todayDir(), "bad_data.csv")
    badPath2 <- path(todayDir(), "zbad_data.csv")
    TimeSeriesFile$writeOneTimeSeries(aapl, "not a time series", "bogus", badPath1)
    TimeSeriesFile$writeOneTimeSeries(aapl, "test-quantys close", "not a data source", badPath2)
    checkFileCount(todayDir(), 4)

    uploader <- TimeSeriesBatchUpload(baseDir())
    failures <- uploader$upload()
    checkLength(failures, 2)
    checkSame(rownames(failures), c(badPath1, badPath2))
    checkMatches(first(failures), "not a time series")
    checkMatches(last(failures), "not a data source")
    checkFileCount(uploader$failuresDir(), 2)
    checkFileCount(archiveDir(), 2)
}

testMalformedDataMovesFileToFailuresAndAllGoodDataFilesGetUploaded <- function() {
    badPath <- path(todayDir(), "bad_time_series_file1.csv")
    file.copy(system.file("testdata/bad_time_series_file1.csv", package="GSFCore"), badPath)
    checkFileCount(todayDir(), 1)
    uploader <- TimeSeriesBatchUpload(baseDir())
    failures <- uploader$upload()
    checkLength(failures, 1)
    checkSame(rownames(failures), badPath)
    checkMatches(the(failures), "")
    checkFileCount(uploader$failuresDir(), 1)
}

