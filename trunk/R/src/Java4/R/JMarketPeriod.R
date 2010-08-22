constructor("JMarketPeriod", function(jobj = NULL) {
    extend(JObject(), "JMarketPeriod", .jobj = jobj)
})

method("by_Symbol_Date_Date", "JMarketPeriod", enforceRCC = TRUE, function(static, market = NULL, start = NULL, end = NULL, ...) {
    JMarketPeriod(jNew("systemdb/metadata/MarketPeriod", .jcast(market$.jobj, "systemdb.data.Symbol"), .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date")))
})

method("by_Row", "JMarketPeriod", enforceRCC = TRUE, function(static, r = NULL, ...) {
    JMarketPeriod(jNew("systemdb/metadata/MarketPeriod", .jcast(r$.jobj, "db.Row")))
})

method("range", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "range"))
})

method("overlaps_by_Range", "JMarketPeriod", enforceRCC = TRUE, function(this, range = NULL, ...) {
    jCall(this$.jobj, "Z", "overlaps", .jcast(range$.jobj, "util.Range"))
})

method("equals_by_Object", "JMarketPeriod", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("start", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "start"))
})

method("after", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    JMarketPeriod(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MarketPeriod;", "after"))
})

method("endUntilStartOf_by_MarketPeriod", "JMarketPeriod", enforceRCC = TRUE, function(this, period = NULL, ...) {
    JMarketPeriod(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MarketPeriod;", "endUntilStartOf", .jcast(period$.jobj, "systemdb.metadata.MarketPeriod")))
})

method("before", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    JMarketPeriod(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MarketPeriod;", "before"))
})

method("end", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "end"))
})

method("hasEnd", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasEnd")
})

method("hasStart", "JMarketPeriod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasStart")
})

