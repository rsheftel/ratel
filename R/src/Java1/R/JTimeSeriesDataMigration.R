constructor("JTimeSeriesDataMigration", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesDataMigration", .jobj = jobj)
})

method("by", "JTimeSeriesDataMigration", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesDataMigration(jNew("tsdb/TimeSeriesDataMigration"))
})

method("main_by_StringArray", "JTimeSeriesDataMigration", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("tsdb/TimeSeriesDataMigration", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

