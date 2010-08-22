constructor("JGenerateMetricCsvs", function(jobj = NULL) {
    extend(JObject(), "JGenerateMetricCsvs", .jobj = jobj)
})

method("by", "JGenerateMetricCsvs", enforceRCC = TRUE, function(static, ...) {
    JGenerateMetricCsvs(jNew("systemdb/qworkbench/GenerateMetricCsvs"))
})

method("main_by_StringArray", "JGenerateMetricCsvs", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("systemdb/qworkbench/GenerateMetricCsvs", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

