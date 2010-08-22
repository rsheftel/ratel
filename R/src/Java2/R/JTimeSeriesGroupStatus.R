constructor("JTimeSeriesGroupStatus", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesGroupStatus", .jobj = jobj)
})

method("by", "JTimeSeriesGroupStatus", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesGroupStatus(jNew("tsdb/TimeSeriesGroupStatus"))
})

method("main_by_StringArray", "JTimeSeriesGroupStatus", enforceRCC = TRUE, function(static, arguments = NULL, ...) {
    jCall("tsdb/TimeSeriesGroupStatus", "V", "main", jArray(arguments, "[Ljava/lang/String;"))
})

