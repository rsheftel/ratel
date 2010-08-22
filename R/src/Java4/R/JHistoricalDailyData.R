constructor("JHistoricalDailyData", function(jobj = NULL) {
    extend(JObject(), "JHistoricalDailyData", .jobj = jobj)
})

method("lastCloseBefore_by_Date", "JHistoricalDailyData", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "lastCloseBefore", .jcast(j_arg0$.jobj, "java.util.Date")))
})

method("lastBars_by_int", "JHistoricalDailyData", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "lastBars", theInteger(j_arg0)))
})

method("bars_by_Range", "JHistoricalDailyData", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(j_arg0$.jobj, "util.Range")))
})

