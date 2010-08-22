constructor("JTimeSeriesExplorer", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesExplorer", .jobj = jobj)
})

method("by", "JTimeSeriesExplorer", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesExplorer(jNew("tsdb/TimeSeriesExplorer"))
})

method("main_by_StringArray", "JTimeSeriesExplorer", enforceRCC = TRUE, function(static, arguments = NULL, ...) {
    jCall("tsdb/TimeSeriesExplorer", "V", "main", jArray(arguments, "[Ljava/lang/String;"))
})

