constructor("JMarketHistoryTable", function(jobj = NULL) {
    extend(JObject(), "JMarketHistoryTable", .jobj = jobj)
})

method("by", "JMarketHistoryTable", enforceRCC = TRUE, function(static, ...) {
    JMarketHistoryTable(jNew("systemdb/metadata/MarketHistoryTable"))
})

method("delete_by_Date_Date_String", "JMarketHistoryTable", enforceRCC = TRUE, function(this, start = NULL, end = NULL, name = NULL, ...) {
    jCall(this$.jobj, "V", "delete", .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"), the(name))
})

method("inactivePeriods_by_List", "JMarketHistoryTable", enforceRCC = TRUE, function(this, active = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "inactivePeriods", .jcast(active$.jobj, "java.util.List")))
})

method("insert_by_Symbol_Date_Date", "JMarketHistoryTable", enforceRCC = TRUE, function(this, test = NULL, start = NULL, end = NULL, ...) {
    jCall(this$.jobj, "V", "insert", .jcast(test$.jobj, "systemdb.data.Symbol"), .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"))
})

method("activePeriods_by_String", "JMarketHistoryTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "activePeriods", the(name)))
})

method("MARKET_HISTORY", "JMarketHistoryTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JMarketHistoryTable$...MARKET_HISTORY, JMarketHistoryTable(jobj = jField("systemdb/metadata/MarketHistoryTable", "Lsystemdb/metadata/MarketHistoryTable;", "MARKET_HISTORY")), log = FALSE)
})

