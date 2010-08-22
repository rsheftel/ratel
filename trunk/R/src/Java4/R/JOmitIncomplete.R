constructor("JOmitIncomplete", function(jobj = NULL) {
    extend(JObject(), "JOmitIncomplete", .jobj = jobj)
})

method("by", "JOmitIncomplete", enforceRCC = TRUE, function(static, ...) {
    JOmitIncomplete(jNew("systemdb/data/bars/OmitIncomplete"))
})

method("name", "JOmitIncomplete", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("convert_by_HistoricalDailyData_List", "JOmitIncomplete", enforceRCC = TRUE, function(this, data = NULL, observations = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "convert", .jcast(data$.jobj, "systemdb.data.HistoricalDailyData"), .jcast(observations$.jobj, "java.util.List")))
})

