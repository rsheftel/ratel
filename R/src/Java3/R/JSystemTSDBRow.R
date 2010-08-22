constructor("JSystemTSDBRow", function(jobj = NULL) {
    extend(JObject(), "JSystemTSDBRow", .jobj = jobj)
})

method("by_SystemTSDBTable_Clause", "JSystemTSDBRow", enforceRCC = TRUE, function(static, table = NULL, matches = NULL, ...) {
    JSystemTSDBRow(jNew("systemdb/metadata/SystemTSDBRow", .jcast(table$.jobj, "systemdb.metadata.SystemTSDBTable"), .jcast(matches$.jobj, "db.clause.Clause")))
})

method("lastCloseBefore_by_Date", "JSystemTSDBRow", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "lastCloseBefore", .jcast(date$.jobj, "java.util.Date")))
})

method("bars_by_Range", "JSystemTSDBRow", enforceRCC = TRUE, function(this, range = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(range$.jobj, "util.Range")))
})

method("lastBars_by_int", "JSystemTSDBRow", enforceRCC = TRUE, function(this, count = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "lastBars", theInteger(count)))
})

