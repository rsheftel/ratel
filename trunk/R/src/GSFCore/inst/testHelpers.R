tsdb <- TimeSeriesDB()

createTestTimeSeries <- function() {
    tsdb$createTimeSeries(name = "test-quantys close", attributes = list(
        ticker = "test-quantys",
        quote_type = "close"
    ))
}

deleteTestTimeSeries <- function() {
    tsdb$.deleteTimeSeries(name = "test-quantys close", are.you.sure = TRUE)
    tsdb$.deleteTimeSeries(name = "another time series", are.you.sure = TRUE)
}

purgeTestTimeSeries <- function() {
    tsdb$purgeTimeSeries(name = "aapl volume", data.source = "test")
    tsdb$purgeTimeSeries(name = "aapl low", data.source = "test")
    tsdb$purgeTimeSeries(name = "aapl high", data.source = "test")
    tsdb$purgeTimeSeries(name = "aapl open", data.source = "test")
    tsdb$purgeTimeSeries(name = "aapl close", data.source = "test")
}


