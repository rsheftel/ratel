constructor("JSystemTSDBTable", function(jobj = NULL) {
    extend(JObject(), "JSystemTSDBTable", .jobj = jobj)
})

method("by", "JSystemTSDBTable", enforceRCC = TRUE, function(static, ...) {
    JSystemTSDBTable(jNew("systemdb/metadata/SystemTSDBTable"))
})

method("dataSource_by_String", "JSystemTSDBTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JHistoricalDailyData(jobj = jCall(this$.jobj, "Lsystemdb/data/HistoricalDailyData;", "dataSource", the(name)))
})

method("insert_by_String_DataSource_String_BarSmith", "JSystemTSDBTable", enforceRCC = TRUE, function(this, name = NULL, source = NULL, close = NULL, smith = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), .jcast(source$.jobj, "tsdb.DataSource"), the(close), .jcast(smith$.jobj, "systemdb.data.bars.BarSmith"))
})

method("insert_by_String_DataSource_String", "JSystemTSDBTable", enforceRCC = TRUE, function(this, name = NULL, source = NULL, close = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), .jcast(source$.jobj, "tsdb.DataSource"), the(close))
})

method("insert_by_String_DataSource_String_String_String_String_String_String_BarSmith", "JSystemTSDBTable", enforceRCC = TRUE, function(this, name = NULL, source = NULL, close = NULL, open = NULL, high = NULL, low = NULL, volume = NULL, openInterest = NULL, smith = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), .jcast(source$.jobj, "tsdb.DataSource"), the(close), the(open), the(high), the(low), the(volume), the(openInterest), .jcast(smith$.jobj, "systemdb.data.bars.BarSmith"))
})

method("insert_by_String_DataSource_String_String_String_String_String_String", "JSystemTSDBTable", enforceRCC = TRUE, function(this, name = NULL, source = NULL, close = NULL, open = NULL, high = NULL, low = NULL, volume = NULL, openInterest = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), .jcast(source$.jobj, "tsdb.DataSource"), the(close), the(open), the(high), the(low), the(volume), the(openInterest))
})

method("SYSTEM_SERIES_DATA", "JSystemTSDBTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystemTSDBTable$...SYSTEM_SERIES_DATA, JSystemTSDBTable(jobj = jField("systemdb/metadata/SystemTSDBTable", "Lsystemdb/metadata/SystemTSDBTable;", "SYSTEM_SERIES_DATA")), log = FALSE)
})

