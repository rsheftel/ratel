constructor("JSystemTimeSeriesTable", function(jobj = NULL) {
    extend(JObject(), "JSystemTimeSeriesTable", .jobj = jobj)
})

method("by", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(static, ...) {
    JSystemTimeSeriesTable(jNew("systemdb/metadata/SystemTimeSeriesTable"))
})

method("currency_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "currency", the(name))
})

method("has_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Z", "has", the(name))
})

method("isUseAdjustment_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Z", "isUseAdjustment", the(name))
})

method("setUseAdjustment_by_String_boolean", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, adjust = NULL, ...) {
    jCall(this$.jobj, "V", "setUseAdjustment", the(symbol), theLogical(adjust))
})

method("insertAsciiIntraday_by_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, filename = NULL, ...) {
    jCall(this$.jobj, "V", "insertAsciiIntraday", the(symbol), the(filename))
})

method("insertBloomberg_by_String_String_String_String_String_String_String_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, bloombergTicker = NULL, open = NULL, high = NULL, low = NULL, bid = NULL, ask = NULL, size = NULL, time = NULL, ...) {
    jCall(this$.jobj, "V", "insertBloomberg", the(symbol), the(bloombergTicker), the(open), the(high), the(low), the(bid), the(ask), the(size), the(time))
})

method("exchange_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, market = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "exchange", the(market))
})

method("insertBloomberg_by_String_String_String_String_String_String_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, bloombergTicker = NULL, open = NULL, high = NULL, low = NULL, last = NULL, size = NULL, time = NULL, ...) {
    jCall(this$.jobj, "V", "insertBloomberg", the(symbol), the(bloombergTicker), the(open), the(high), the(low), the(last), the(size), the(time))
})

method("intraday_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, ...) {
    JIntradaySource(jobj = jCall(this$.jobj, "Lsystemdb/metadata/IntradaySource;", "intraday", the(symbol)))
})

method("insertBloomberg_by_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, bloombergTicker = NULL, ...) {
    jCall(this$.jobj, "V", "insertBloomberg", the(symbol), the(bloombergTicker))
})

method("type_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "type", the(name))
})

method("live_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, symbol = NULL, ...) {
    JLiveDataSource(jobj = jCall(this$.jobj, "Lsystemdb/metadata/LiveDataSource;", "live", the(symbol)))
})

method("symbolsByExchangeSubsector_by_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, exchange = NULL, subsector = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "symbolsByExchangeSubsector", the(exchange), the(subsector)))
})

method("subsectors_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, exchange = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "subsectors", the(exchange)))
})

method("dailyHistorical_by_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JHistoricalDailyData(jobj = jCall(this$.jobj, "Lsystemdb/data/HistoricalDailyData;", "dailyHistorical", the(name)))
})

method("insert_by_String_String_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, histDaily = NULL, live = NULL, liveFeedName = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), the(histDaily), the(live), the(liveFeedName))
})

method("insert_by_String_String_String", "JSystemTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, histDaily = NULL, live = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), the(histDaily), the(live))
})

method("topicPrefix_FOR_TESTING", "JSystemTimeSeriesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystemTimeSeriesTable$...topicPrefix_FOR_TESTING, jField("systemdb/metadata/SystemTimeSeriesTable", "Ljava/lang/String;", "topicPrefix_FOR_TESTING"), log = FALSE)
})

method("SYSTEM_TS", "JSystemTimeSeriesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystemTimeSeriesTable$...SYSTEM_TS, JSystemTimeSeriesTable(jobj = jField("systemdb/metadata/SystemTimeSeriesTable", "Lsystemdb/metadata/SystemTimeSeriesTable;", "SYSTEM_TS")), log = FALSE)
})

method("ACTIVE_MQ", "JSystemTimeSeriesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystemTimeSeriesTable$...ACTIVE_MQ, jField("systemdb/metadata/SystemTimeSeriesTable", "Ljava/lang/String;", "ACTIVE_MQ"), log = FALSE)
})

