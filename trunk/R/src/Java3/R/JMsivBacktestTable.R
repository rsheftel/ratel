constructor("JMsivBacktestTable", function(jobj = NULL) {
    extend(JObject(), "JMsivBacktestTable", .jobj = jobj)
})

method("by", "JMsivBacktestTable", enforceRCC = TRUE, function(static, ...) {
    JMsivBacktestTable(jNew("systemdb/metadata/MsivBacktestTable"))
})

method("range_by_int_String", "JMsivBacktestTable", enforceRCC = TRUE, function(this, systemId = NULL, market = NULL, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "range", theInteger(systemId), the(market)))
})

method("markets_by_Siv_String", "JMsivBacktestTable", enforceRCC = TRUE, function(this, siv = NULL, stoId = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "markets", .jcast(siv$.jobj, "systemdb.metadata.Siv"), the(stoId)))
})

method("stoIds_by_Siv", "JMsivBacktestTable", enforceRCC = TRUE, function(this, siv = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "stoIds", .jcast(siv$.jobj, "systemdb.metadata.Siv")))
})

method("insert_by_String_String", "JMsivBacktestTable", enforceRCC = TRUE, function(this, msiv = NULL, stoId = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(msiv), the(stoId))
})

method("insert_by_String_String_Date_Date", "JMsivBacktestTable", enforceRCC = TRUE, function(this, msiv = NULL, stoId = NULL, start = NULL, end = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(msiv), the(stoId), .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"))
})

method("BACKTEST", "JMsivBacktestTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JMsivBacktestTable$...BACKTEST, JMsivBacktestTable(jobj = jField("systemdb/metadata/MsivBacktestTable", "Lsystemdb/metadata/MsivBacktestTable;", "BACKTEST")), log = FALSE)
})

