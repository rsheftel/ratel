constructor("JBloombergTable", function(jobj = NULL) {
    extend(JObject(), "JBloombergTable", .jobj = jobj)
})

method("by", "JBloombergTable", enforceRCC = TRUE, function(static, ...) {
    JBloombergTable(jNew("systemdb/metadata/BloombergTable"))
})

method("setCalculateMethod_by_String_String", "JBloombergTable", enforceRCC = TRUE, function(this, name = NULL, method = NULL, ...) {
    jCall(this$.jobj, "V", "setCalculateMethod", the(name), the(method))
})

method("dataSource_by_String", "JBloombergTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JHistoricalDailyData(jobj = jCall(this$.jobj, "Lsystemdb/data/HistoricalDailyData;", "dataSource", the(name)))
})

method("useMids_by_String_String_String", "JBloombergTable", enforceRCC = TRUE, function(this, name = NULL, bidField = NULL, askField = NULL, ...) {
    jCall(this$.jobj, "V", "useMids", the(name), the(bidField), the(askField))
})

method("setFiltered_by_String_boolean", "JBloombergTable", enforceRCC = TRUE, function(this, name = NULL, isFiltered = NULL, ...) {
    jCall(this$.jobj, "V", "setFiltered", the(name), theLogical(isFiltered))
})

method("insert_by_String_String_String_String_String_String_String_String_String_String", "JBloombergTable", enforceRCC = TRUE, function(this, symbol = NULL, bloombergTicker = NULL, open = NULL, high = NULL, low = NULL, last = NULL, bid = NULL, ask = NULL, size = NULL, time = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(symbol), the(bloombergTicker), the(open), the(high), the(low), the(last), the(bid), the(ask), the(size), the(time))
})

method("insert_by_String_String", "JBloombergTable", enforceRCC = TRUE, function(this, symbol = NULL, bloombergTicker = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(symbol), the(bloombergTicker))
})

method("intraday_by_String", "JBloombergTable", enforceRCC = TRUE, function(this, symbol = NULL, ...) {
    JBloombergIntraday(jobj = jCall(this$.jobj, "Lsystemdb/metadata/BloombergTable/BloombergIntraday;", "intraday", the(symbol)))
})

method("LOOKUP_MARKET_TICKER_SENTINEL", "JBloombergTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergTable$...LOOKUP_MARKET_TICKER_SENTINEL, jField("systemdb/metadata/BloombergTable", "Ljava/lang/String;", "LOOKUP_MARKET_TICKER_SENTINEL"), log = FALSE)
})

method("BLOOMBERG", "JBloombergTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergTable$...BLOOMBERG, JBloombergTable(jobj = jField("systemdb/metadata/BloombergTable", "Lsystemdb/metadata/BloombergTable;", "BLOOMBERG")), log = FALSE)
})

