constructor("JContract", function(jobj = NULL) {
    extend(JObject(), "JContract", .jobj = jobj)
})

method("by_String", "JContract", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JContract(jNew("futures/Contract", the(name)))
})

method("quarterlyTicker_by_Date", "JContract", enforceRCC = TRUE, function(this, asOf = NULL, ...) {
    JFuturesTicker(jobj = jCall(this$.jobj, "Lfutures/FuturesTicker;", "quarterlyTicker", .jcast(asOf$.jobj, "java.util.Date")))
})

method("main_by_StringArray", "JContract", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("futures/Contract", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("createTimeSeries_by_FuturesTicker", "JContract", enforceRCC = TRUE, function(this, ticker = NULL, ...) {
    jCall(this$.jobj, "V", "createTimeSeries", .jcast(ticker$.jobj, "futures.FuturesTicker"))
})

method("createTimeSeries_by_List", "JContract", enforceRCC = TRUE, function(this, tickers = NULL, ...) {
    jCall(this$.jobj, "V", "createTimeSeries", .jcast(tickers$.jobj, "java.util.List"))
})

method("priceSeries_by_Date", "JContract", enforceRCC = TRUE, function(this, asOf = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "priceSeries", .jcast(asOf$.jobj, "java.util.Date")))
})

method("expiry", "JContract", enforceRCC = TRUE, function(this, ...) {
    JExpiry(jobj = jCall(this$.jobj, "Lfutures/Expiry;", "expiry"))
})

method("futuresTickers_by_Date", "JContract", enforceRCC = TRUE, function(this, asOf = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "futuresTickers", .jcast(asOf$.jobj, "java.util.Date")))
})

