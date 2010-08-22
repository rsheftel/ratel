constructor("JSeriesControl", function(jobj = NULL) {
    extend(JObject(), "JSeriesControl", .jobj = jobj)
})

method("by", "JSeriesControl", enforceRCC = TRUE, function(static, ...) {
    JSeriesControl(jNew("tsdb/SeriesControl"))
})

method("run_by_StringArray", "JSeriesControl", enforceRCC = TRUE, function(static, j_in = NULL, ...) {
    jCall("tsdb/SeriesControl", "I", "run", jArray(j_in, "[Ljava/lang/String;"))
})

method("main_by_StringArray", "JSeriesControl", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("tsdb/SeriesControl", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

