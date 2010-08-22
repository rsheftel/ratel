library("GSFCore")

tsdb <- TimeSeriesDB()


testCreateTimeSeriesFromFile <- function() {
    deleteTestTimeSeries()
    on.exit(deleteTestTimeSeries)

    filename <- system.file("testdata", "time_series_defs.csv", package="GSFCore")
    TimeSeriesFile$createTimeSeries(tsdb, filename)
    ts <- tsdb$lookupTimeSeriesByAttributeList(list(instrument="test"))
    checkTrue(tsdb$timeSeriesExists("test_sit_rate_10y_mid"))
    checkLength(ts, 3)
    attrs <- tsdb$lookupAttributesForTimeSeries(ts)
    checkShape(attrs, rows=3, cols=6)
    checkSame(the(attrs["test_sit_rate_40y_mid", "tenor"]), "40y")
    checkSame(the(attrs["test_sit_rate_30y_mid", "quote_type"]), "close")
    checkSame(the(attrs["test_sit_rate_10y_mid", "quote_side"]), "mid")
    checkSame(the(attrs["test_sit_rate_40y_mid", "ccy"]), "sit")
    checkSame(the(attrs["test_sit_rate_30y_mid", "quote_convention"]), "rate")
    # full dupe ok
    TimeSeriesFile$createTimeSeries(tsdb, filename)
    # different definition not ok
    bombsFilename <- system.file("testdata", "time_series_defs_small_change.csv", package="GSFCore")
    shouldBombMatching(TimeSeriesFile$createTimeSeries(tsdb, bombsFilename), "duplicate time series found")
}

testCreateTSFromFileBombsWithDuplicateAttributeColumn <- function() { 
    checkBadTimeSeriesDefinitionFile("time_series_defs_with_dupe_attr_column.csv", "duplicated attribute tenor")
}

testCreateTimeSeriesFromFileBadAttributesBombs <- function() {
    checkBadTimeSeriesDefinitionFile("time_series_defs_with_bad_attr.csv", "couldn't find attribute for quote_BAD")
}

testCreateTimeSeriesFromFileBadAttributeValuesBombs <- function() {
    checkBadTimeSeriesDefinitionFile("time_series_defs_with_bad_value.csv", "cannot find id in attribute quote_side, table: TSDB..general_attribute_value for value: BAD")
}

testWriteTimeSeriesWithNonDateIndexedZooFails <- function() {
    a <- zoo(1:10, order.by = 2:11)
    shouldBombMatching(
        TimeSeriesFile$writeOneTimeSeries(a, "test", "test", "test.csv"),
        "index of a zoo in ts.array is not date"
    )

    b <- zoo(data.frame(1:10), order.by = 2:11)
    shouldBombMatching(
        TimeSeriesFile$writeOneTimeSeries(b, "test", "test", "test.csv"),
        "index of a zoo in ts.array is not date"
    )
}

testWriteTimeSeriesWithOneColumnVectorWorks <- function() { 
    deleteTestTimeSeries()
    on.exit(deleteTestTimeSeries)
    dates <- Sys.time() + (2:11)*86400
    filename <- system.file("testdata", "fileCreatedByTest.csv", package="GSFCore")
    
    a <- zoo(data.frame(1:10), order.by = dates)
    TimeSeriesFile$writeOneTimeSeries(a, "test", "test", filename)

    a <- zoo(1:10, order.by = dates)
    TimeSeriesFile$writeOneTimeSeries(a, "test", "test", filename)
}


checkBadTimeSeriesDefinitionFile <- function(filename, errmsg) {
    deleteTestTimeSeries()
    on.exit(deleteTestTimeSeries)

    filename <- system.file("testdata", filename, package="GSFCore")
    shouldBombMatching(TimeSeriesFile$createTimeSeries(tsdb, filename), errmsg)
    ts <- tsdb$lookupTimeSeriesByAttributeList(list(instrument="test"))
    checkNull(ts)
}

deleteTestTimeSeries <- function() {
    ts <- tsdb$lookupTimeSeriesByAttributeList(list(instrument="test"))
    sapply(ts, function(ts) tsdb$.deleteTimeSeries(ts, are.you.sure = TRUE))
}

testReadWriteTimeSeries <- function() {
    aapl.source <- retrieveAaplData()
    TimeSeriesFile$writeTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE))
    aapl.target <- TimeSeriesFile$readTimeSeries(file = textConnection(testData))
    checkEquals(aapl.source, aapl.target)
}

testReadWriteOneTimeSeries <- function() {
    aapl.source <- tsdb$retrieveOneTimeSeriesByName(name = "aapl close", data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")
    TimeSeriesFile$writeOneTimeSeries(aapl.source, name = "aapl close", data.source = "yahoo", file = textConnection("testData", open = "w", local = TRUE))
    aapl.target <- TimeSeriesFile$readOneTimeSeries(file = textConnection(testData))
    checkEquals(aapl.source, aapl.target)
}

testWriteOneTimeSeriesTooManyTimeSeriesException <- function() {
    aapl.source <- retrieveAaplData()
    shouldBomb(TimeSeriesFile$writeOneTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE)))
}

testReadOneTimeSeriesTooManyTimeSeriesException <- function() {
    aapl.source <- retrieveAaplData()
    TimeSeriesFile$writeTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE))
    shouldBomb(TimeSeriesFile$readOneTimeSeries(file = textConnection(testData)))
}

testReadTimeSeriesWithNAColumnHeader <- function() { 
    aapl.source <- retrieveAaplData()
    TimeSeriesFile$writeTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE))
    newData <- testData 
    newData[[1]] <- '"","aapl open:yahoo","aapl high:yahoo","aapl low:yahoo","aapl close:yahoo",NA'
    result <- TimeSeriesFile$readTimeSeries(file = textConnection(newData))
    checkLength(result, 4)
}

testReadTimeSeriesWithNAColumnHeaderWithAttachedDataSource <- function() { 
    aapl.source <- retrieveAaplData()
    TimeSeriesFile$writeTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE))
    newData <- testData 
    newData[[1]] <- '"","aapl open:yahoo","NA:yahoo","aapl low:yahoo","aapl close:yahoo",NA'
    result <- TimeSeriesFile$readTimeSeries(file = textConnection(newData))
    checkLength(result, 3)
}

testAsTsArray <- function() {
    # helper function for R/Excel integration
    aapl.source <- retrieveAaplData()
    TimeSeriesFile$writeTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE))
    df <- read.csv(textConnection(testData), check.names = FALSE, stringsAsFactors = FALSE, na.strings = c("NA", "na", "N/A", "n/a", "NULL", "null"))
    aapl.target <- TimeSeriesFile$as.ts.array(df)
    checkEquals(aapl.source, aapl.target)
}

testWriteTimeSeriesTimeZone <- function() {
    aapl.source <- retrieveAaplData()
    TimeSeriesFile$writeTimeSeries(aapl.source, file = textConnection("testData", open = "w", local = TRUE))
    df <- read.csv(textConnection(testData), check.names = FALSE, stringsAsFactors = FALSE, na.strings = c("NA", "na", "N/A", "n/a", "NULL", "null"))
    names(df)[1] <- "GMT"
    # temporarily, this shouldBomb.  We are temporarily disabling the time zone feature.  All times must be NY time.
    shouldBomb(aapl.target <- TimeSeriesFile$as.ts.array(df))
    # checkEquals(c(aapl.target[[1,1]][as.POSIXct("1990-01-02", tz = "GMT"), 1]), c(aapl.source[[1,1]][as.POSIXct("1990-01-02"), 1]))
    # checkEquals(c(aapl.target[[1,1]][as.POSIXct("1990-07-02", tz = "GMT"), 1]), c(aapl.source[[1,1]][as.POSIXct("1990-07-02"), 1]))
}

retrieveAaplData <- function() {
    tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        ticker = "aapl"
    ), start = "1999-01-01", end = "2000-01-01")
}

