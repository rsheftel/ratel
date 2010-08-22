constructor("JTsdbObservations", function(jobj = NULL) {
    extend(JObject(), "JTsdbObservations", .jobj = jobj)
})

method("by_List", "JTsdbObservations", enforceRCC = TRUE, function(static, rows = NULL, ...) {
    JTsdbObservations(jNew("tsdb/TsdbObservations", .jcast(rows$.jobj, "java.util.List")))
})

method("by", "JTsdbObservations", enforceRCC = TRUE, function(static, ...) {
    JTsdbObservations(jNew("tsdb/TsdbObservations"))
})

method("range", "JTsdbObservations", enforceRCC = TRUE, function(this, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "range"))
})

method("add_by_ObservationRow", "JTsdbObservations", enforceRCC = TRUE, function(this, row = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(row$.jobj, "tsdb.TimeSeriesDataTable.ObservationRow"))
})

method("sourceNames", "JTsdbObservations", enforceRCC = TRUE, function(this, ...) {
    JSet(jobj = jCall(this$.jobj, "Ljava/util/Set;", "sourceNames"))
})

method("seriesNames", "JTsdbObservations", enforceRCC = TRUE, function(this, ...) {
    JSet(jobj = jCall(this$.jobj, "Ljava/util/Set;", "seriesNames"))
})

