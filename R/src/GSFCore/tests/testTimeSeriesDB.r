cat("\n\nTest cases for TimeSeriesDB object\n\n")

library("GSFCore")

tsdb <- NULL
testEnv <- environment()
.setUp <- function() { assign("tsdb", TimeSeriesDB(), envir = testEnv) }

source(system.file("testHelpers.R", package="GSFCore"))

testConstructor <- function() {
    checkTrue( is(tsdb, "TimeSeriesDB") );
}

testLookupAttributesForTimeSeries <- function() {
    attributes <- tsdb$lookupAttributesForTimeSeries(time.series = c("aapl close", "irs_usd_rate_10y_mid"))
    checkSame(as.character(attributes["aapl close", "ticker"]), "aapl")
    checkSame(as.character(attributes["aapl close", "quote_type"]), "close")
    checkTrue(is.na(attributes["aapl close", "quote_convention"]))
    checkSame(as.character(attributes["irs_usd_rate_10y_mid", "instrument"]), "irs")
    checkSame(as.character(attributes["irs_usd_rate_10y_mid", "ccy"]), "usd")
    checkTrue(is.na(attributes["irs_usd_rate_10y_mid", "ticker"]))
}

testLookupAttributesForTimeSeriesSpecificAttributes <- function() {
    attributes <- tsdb$lookupAttributesForTimeSeries(time.series = c("aapl close", "irs_usd_rate_10y_mid"), attributes = c("ticker", "quote_convention"))
    checkSame(ncol(attributes), 2)
    checkSame(as.character(attributes["aapl close", "ticker"]), "aapl")
    checkTrue(is.na(attributes["aapl close", "quote_convention"]))
    checkSame(as.character(attributes["irs_usd_rate_10y_mid", "quote_convention"]), "rate")
    checkTrue(is.na(attributes["irs_usd_rate_10y_mid", "ticker"]))
}

testRetrieveTimeSeriesByName <- function() {

    aapl <- tsdb$retrieveTimeSeriesByName(name = "aapl close", data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")

    checkTrue( is(aapl, "array") )
    checkSame( length(aapl), 1 )

    aapl.zoo <- aapl[["aapl close", "yahoo"]]

    checkTrue( is(aapl.zoo, "zoo") )
    checkAaplClose1999(aapl.zoo)
}

testRetrieveTimeSeriesByMultipleNames <- function() {
    aapl <- tsdb$retrieveTimeSeriesByName(name = c("aapl close", "aapl open"), data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")
    checkTrue( is(aapl, "array") )
    checkSame( length(aapl), 2 )
    checkTrue(all(c("aapl close", "aapl open") %in% rownames(aapl)))
    checkAaplClose1999(aapl[["aapl close", "yahoo"]])
}

testRetrieveTimeSeriesByNameAllDataSources <- function() {
    purgeTestTimeSeries()

    aapl <- tsdb$retrieveTimeSeriesByName(name = "aapl close", start = "2006-01-01", end = "2007-01-01")
    checkTrue( is(aapl, "array") )
    checkTrue(all(c("yahoo", "bogus") %in% colnames(aapl)))
}

testRetrieveTimeSeriesByNameBadName <- function() {
    checkNull(tsdb$retrieveTimeSeriesByName("this_is_not_a_time_series"))
}

testRetrieveTimeSeriesByAttributeList <- function() {
    aapl <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        quote_type = "close",
        ticker = "aapl"
    ), data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")

    checkAaplClose1999(aapl[[1,1]])
}

testRetrieveTimeSeriesByAttributeListReverseAttributeOrder <- function() {
    aapl <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        ticker = "aapl",
        quote_type = "close"
    ), data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")

    checkAaplClose1999(aapl[[1,1]])
}

testRetrieveTimeSeriesByAttributeListNoData <- function() {
    checkTrue(is.null(tsdb$retrieveTimeSeriesByAttributeList(list(ticker = "aapl"), end = "1910-01-01")))
}

testRetrieveTimeSeriesByAttributeListUnspecifiedAttribute <- function() {
    aapl <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        ticker = "aapl"
    ), data.source = "yahoo", start = "1999-01-01", end = "1999-03-01")

    checkSame( ncol(aapl), 1 )
    checkSame( nrow(aapl), 5 )
    checkTrue(all(c("aapl close", "aapl open", "aapl high", "aapl low", "aapl volume") %in% rownames(aapl)))
}

testRetrieveTimeSeriesByAttributeListMultipleAttributeValues <- function() {
    quotes <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        quote_type = c("close", "open"),
        ticker = c("aapl", "s&p500")
    ), data.source = "yahoo", start = "1999-01-01", end = "1999-03-01")

    checkSame( ncol(quotes), 1 )
    checkSame( nrow(quotes), 4 )
    checkTrue(all(c("aapl close", "aapl open", "s&p500 close", "s&p500 open") %in% rownames(quotes)))
}

testRetrieveTimeSeriesByAttributeListArrangeBy <- function() {
    quotes <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        quote_type = c("close", "open"),
        ticker = c("aapl", "s&p500")
    ), data.source = "yahoo", start = "1999-01-01", end = "2000-01-01", arrange.by = c("ticker", "quote_type"))
    checkSame( as.vector(ncol(quotes)), 2 )
    checkSame( as.vector(nrow(quotes)), 2 )
    checkTrue(all(c("aapl", "s&p500") %in% rownames(quotes)))
    checkTrue(all(c("open", "close") %in% colnames(quotes)))
}

testRetrieveTimeSeriesByAttributeListArrangeByReverseOrder <- function() {
    quotes <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        quote_type = c("close", "open"),
        ticker = c("aapl", "s&p500")
    ), data.source = "yahoo", start = "1999-01-01", end = "1999-03-01", arrange.by = c("quote_type", "ticker"))
    checkSame( as.vector(ncol(quotes)), 2 )
    checkSame( as.vector(nrow(quotes)), 2 )
    checkTrue(all(c("open", "close") %in% rownames(quotes)))
    checkTrue(all(c("aapl", "s&p500") %in% colnames(quotes)))
}

testRetrieveTimeSeriesByAttributeListArrangeByNotEnoughDimensions <- function() {
    shouldBomb(tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        quote_type = c("close", "open"),
        ticker = c("aapl", "s&p500")
    ), data.source = "yahoo", start = "1999-01-01", end = "2000-01-01", arrange.by = c("ticker")))
}

testRetrieveOneTimeSeriesByName <- function() {
    aapl <- tsdb$retrieveOneTimeSeriesByName(name = "aapl close", data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")
    checkTrue( is(aapl, "zoo") )
    checkAaplClose1999(aapl)
}

oneAaplClose <- function(start, end) {
    tsdb$retrieveOneTimeSeriesByAttributeList(attributes = list( 
        quote_type = "close",
        ticker = "aapl"
    ), data.source = "yahoo", start = start, end = end)
}

testRetrieveOneTimeSeriesByAttributeList <- function() {
    aapl <- oneAaplClose("1999-01-01", "2000-01-01")
    checkTrue( is(aapl, "zoo") )
    checkAaplClose1999(aapl)
}

testRetrieveIsInclusiveOfEndDate <- function() {
    aapl <- oneAaplClose("1999-12-30", "1999-12-31")

    checkShape(aapl, rows=2, cols=1)
    checkSame(aapl[[1,1]], 100.31)
    checkSame(aapl[[2,1]], 102.81)

    aapl2 <- oneAaplClose(as.POSIXct("1999-12-30"), as.POSIXct("1999-12-31"))
    checkSame(aapl, aapl2)
}

testRetrieveDoesNotChangeEndTimeIfSupplied <- function() {
    aapl <- oneAaplClose("1999-12-30", "1999-12-31 13:59:59")
    checkShape(aapl, rows=1, cols=1)
    checkSame(aapl[[1,1]], 100.31)
    aapl2 <- oneAaplClose(as.POSIXct("1999-12-30"), as.POSIXct("1999-12-31 13:59:59"))
    checkSame(aapl, aapl2)

    aapl <- oneAaplClose("1999-12-30", "1999-12-31 14:00:00")
    checkShape(aapl, rows=2, cols=1)
    checkSame(aapl[[1,1]], 100.31)
    checkSame(aapl[[2,1]], 102.81)
    aapl2 <- oneAaplClose(as.POSIXct("1999-12-30"), as.POSIXct("1999-12-31 14:00:00"))
    checkSame(aapl, aapl2)

    aapl <- oneAaplClose("1999-12-30", "1999-12-31 00:00:00")
    checkShape(aapl, rows=1, cols=1)
    checkSame(aapl[[1,1]], 100.31)
    aapl2 <- oneAaplClose(as.POSIXct("1999-12-30"), as.POSIXct("1999-12-31 00:00:00"))
    checkFalse(isTRUE(all.equal(aapl, aapl2)))
}

testRetrieveOneTimeSeriesByNameTooManyTimeSeriesException <- function() {
    shouldBomb(tsdb$retrieveOneTimeSeriesByName(name = "aapl close", start="2006-07-01", end="2006-08-01"))
}

testRetrieveOneTimeSeriesByAttributeListTooManyTimeSeriesException <- function() {
    shouldBomb(tsdb$retrieveOneTimeSeriesByAttributeList(attributes = list(
            ticker = "aapl"
    ), data.source = "yahoo", start = "1999-01-01", end = "1999-02-01"))
}

testRetrieveOneTimeSeriesByNameWithNoObservationsInRange <- function() {
    tsdb$retrieveOneTimeSeriesByName(name = "abc_snrfor_usd_xr_spread_5y", data.source = "internal", start = "2006-01-02", end = "2006-01-02")
    tsdb$retrieveOneTimeSeriesByName(name = "abc_snrfor_usd_xr_spread_5y", data.source = "internal", start = "1987-01-01", end = "1987-01-01")
}

testWriteOneTimeSeries <- function() {
    purgeTestTimeSeries()
    on.exit(purgeTestTimeSeries())

    aapl.source <- tsdb$retrieveOneTimeSeriesByName(name = "aapl close", data.source = "yahoo", start = "1999-10-01", end = "2000-01-01")
    checkWriteAndRetrieveOneTimeSeries(aapl.source, name = "aapl close", data.source = "test")

    window(aapl.source, as.POSIXct("1999-12-31 14:00:00")) <- 100.01
    checkWriteAndRetrieveOneTimeSeries(aapl.source, name = "aapl close", data.source = "test")
}

testCreateAndWriteOneTimeSeriesByName <- function() {
    tsName <- "irs_test"
    on.exit(tsdb$.deleteTimeSeries(name = tsName,are.you.sure = TRUE))
    tsdb$.deleteTimeSeries(name = tsName,are.you.sure = TRUE)
    ts <- zoo(matrix(1),as.POSIXct("2000-01-01"))
    attributeList <- list(instrument = "test",tenor = "10y")
    checkTrue(!tsdb$timeSeriesExists(name = "irs_test"))
    tsdb$createAndWriteOneTimeSeriesByName(ts,"irs_test","internal_test",attributeList)
    checkTrue(tsdb$timeSeriesExists(name = "irs_test"))
    checkSame(ts,tsdb$retrieveTimeSeriesByName("irs_test","internal_test")[[1]])    
    ts <- zoo(matrix(2),as.POSIXct("2000-01-01"))
    tsdb$createAndWriteOneTimeSeriesByName(ts,"irs_test","internal_test",attributeList)
    checkSame(ts,tsdb$retrieveTimeSeriesByName("irs_test","internal_test")[[1]]) 
}

testWriteLotsaObservations <- function() {
    tsNames <- c(
        "irs_usd_rate_12y_convexity", "irs_usd_rate_12y_dv01", "irs_usd_rate_12y_mid", 
        "irs_usd_rate_12y_tri", "irs_usd_rate_12y_tri_daily", "irs_usd_rate_2y_convexity", 
        "irs_usd_rate_2y_dv01", "irs_usd_rate_2y_mid", "irs_usd_rate_2y_tri", 
        "irs_usd_rate_2y_tri_daily"
    )
    for(ts in tsNames ) tsdb$purgeTimeSeries(ts, "test")
    obs <- tsdb$retrieveTimeSeriesByName(tsNames, "internal")
    colnames(obs) <- "test"
    tsdb$writeTimeSeries(obs)
    test <- tsdb$retrieveTimeSeriesByName(tsNames, "test")
    checkSame(obs, test)
    for(ts in tsNames ) tsdb$purgeTimeSeries(ts, "test")
}

testPurgeAndReWriteOneTimeSeries <- function() {
    purgeTestTimeSeries()
    on.exit(purgeTestTimeSeries())

    aapl.source <- tsdb$retrieveOneTimeSeriesByName(name = "aapl close", data.source = "bogus")
    checkWriteAndRetrieveOneTimeSeries(aapl.source, name = "aapl close", data.source = "test")
}

testWriteTimeSeries <- function() {
    purgeTestTimeSeries()
    on.exit(purgeTestTimeSeries())

    aapl.source <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        ticker = "aapl"
    ), data.source = "yahoo", start = "1999-01-01", end = "1999-03-01")

    colnames(aapl.source) <- "test"

    tsdb$writeTimeSeries(aapl.source)

    aapl.target <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list( 
        ticker = "aapl"
    ), data.source = "test", start = "1999-01-01", end = "1999-03-01")

    checkSame(aapl.source, aapl.target)
}

testWriteOneTimeSeriesNoValues <- function() {
    tsdb$writeOneTimeSeriesByName(logical(0), name = "aapl close", data.source = "test")
}

testWriteTimeSeriesWorksOnEvenlyDivisibleTimeSeriesIds <- function() {
    # tobrai_snrfor_jpy_cr_spread_10y = 100000
    data <- tsdb$retrieveTimeSeriesByName("tobrai_snrfor_jpy_cr_spread_10y", "markit", start = '2003-10-27', end = '2003-10-27')
    tsdb$writeTimeSeries(data)
}

testCreateTimeSeries <- function() {
    deleteTestTimeSeries()
    on.exit(deleteTestTimeSeries())

    checkTrue(!tsdb$timeSeriesExists(name = "test-quantys close"))
    createTestTimeSeries()
    checkTrue(tsdb$timeSeriesExists(name = "test-quantys close"))

    test.source <- tsdb$retrieveOneTimeSeriesByName(name = "aapl close", data.source = "yahoo", start = "1999-01-01", end = "2000-01-01")
    checkWriteAndRetrieveOneTimeSeries(test.source, name = "test-quantys close", data.source = "test")
}

testCreateTimeSeriesNoDuplicateNames <- function() {
    deleteTestTimeSeries()
    on.exit(deleteTestTimeSeries())

    createTestTimeSeries()
    shouldBomb(createTestTimeSeries())
}

testCreateTimeSeriesNoDuplicateAttributes <- function() {
    deleteTestTimeSeries()
    on.exit(deleteTestTimeSeries())

    shouldBombMatching(tsdb$createTimeSeries(name = "another time series", attributes = list(
        ticker = "test-quantys",
        ticker = "test-quantys",
        quote_type = "close"
    )), "duplicated attribute ticker")

    createTestTimeSeries()

    shouldBombMatching(tsdb$createTimeSeries(name = "another time series", attributes = list(
        ticker = "test-quantys",
        quote_type = "close"
    )), "time series already exists")

    shouldBombMatching(tsdb$createTimeSeries(name = "another time series", attributes = list(
        quote_type = "open"
    )), "time series already exists")
}

checkAaplClose1999 <- function(aapl.zoo) {
    checkSame( length(aapl.zoo), 252 )
    checkSame( sum(aapl.zoo), 14558.22 )
    checkSame( min(aapl.zoo), 32.19 )
    checkSame( max(aapl.zoo), 117.81 )
}

checkWriteAndRetrieveOneTimeSeries <- function(source.data, name, data.source)  {
    tsdb$writeOneTimeSeriesByName(source.data, name, data.source)
    target.data <- tsdb$retrieveOneTimeSeriesByName(name, data.source)
    checkSame(source.data, target.data)
}

testLookupInCorrectOrder <- function() {
    series <- paste("bond_government_usd_", c(30,10,5,2), "y_otr_yield", sep="")
    maturities <- tsdb$lookupAttributesForTimeSeries(series, attributes = "maturity")
    checkSame(paste("bond_government_usd_", c(10,2,30,5), "y_otr_yield", sep=""), index(maturities))
    checkSame(c("10y", "2y", "30y", "5y"), as.vector(maturities[,1]))
}

testCount <- function() {
    checkSame(5688, tsdb$numObservations("aapl close", "yahoo"))
}

testLookupSeriesIDs <- function() {
	tsNames <- c('aapl open','aapl close')
	ids <- c(1,4)
	checkSame(TimeSeriesDB()$lookupSeriesIDs(tsNames),ids)
	checkSame(TimeSeriesDB()$lookupSeriesIDs(tsNames[1]),ids[1])
	shouldBombMatching(TimeSeriesDB()$lookupSeriesIDs('does not exist'),'time series does not exist')
}

testLookupSeriesNamess <- function() {
	tsNames <- c('aapl open','aapl close')
	ids <- c(1,4)
	checkSame(TimeSeriesDB()$lookupSeriesNames(ids),tsNames)
	checkSame(TimeSeriesDB()$lookupSeriesNames(ids[1]),tsNames[1])
	shouldBombMatching(TimeSeriesDB()$lookupSeriesNames(-5),'expected single result but got 0')
}