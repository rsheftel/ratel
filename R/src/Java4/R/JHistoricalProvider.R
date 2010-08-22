constructor("JHistoricalProvider", function(jobj = NULL) {
    extend(JObject(), "JHistoricalProvider", .jobj = jobj)
})

method("dataSource_by_String", "JHistoricalProvider", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JHistoricalDailyData(jobj = jCall(this$.jobj, "Lsystemdb/data/HistoricalDailyData;", "dataSource", the(j_arg0)))
})

method("BLOOMBERG", "JHistoricalProvider", enforceRCC = FALSE, function(static, ...) {
    lazy(JHistoricalProvider$...BLOOMBERG, jField("systemdb/metadata/HistoricalProvider", "Ljava/lang/String;", "BLOOMBERG"), log = FALSE)
})

method("TSDB", "JHistoricalProvider", enforceRCC = FALSE, function(static, ...) {
    lazy(JHistoricalProvider$...TSDB, jField("systemdb/metadata/HistoricalProvider", "Ljava/lang/String;", "TSDB"), log = FALSE)
})

method("ASC11", "JHistoricalProvider", enforceRCC = FALSE, function(static, ...) {
    lazy(JHistoricalProvider$...ASC11, jField("systemdb/metadata/HistoricalProvider", "Ljava/lang/String;", "ASC11"), log = FALSE)
})

