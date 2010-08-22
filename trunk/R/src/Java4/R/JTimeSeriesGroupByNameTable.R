constructor("JTimeSeriesGroupByNameTable", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesGroupByNameTable", .jobj = jobj)
})

method("by", "JTimeSeriesGroupByNameTable", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesGroupByNameTable(jNew("tsdb/TimeSeriesGroupByNameTable"))
})

method("delete_by_int", "JTimeSeriesGroupByNameTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "V", "delete", theInteger(id))
})

method("seriesLookup_by_int_Date", "JTimeSeriesGroupByNameTable", enforceRCC = TRUE, function(this, id = NULL, asOf = NULL, ...) {
    JSelectOne(jobj = jCall(this$.jobj, "Ldb/SelectOne;", "seriesLookup", theInteger(id), .jcast(asOf$.jobj, "java.util.Date")))
})

method("insert_by_int_TimeSeries", "JTimeSeriesGroupByNameTable", enforceRCC = TRUE, function(this, id = NULL, series = NULL, ...) {
    jCall(this$.jobj, "V", "insert", theInteger(id), .jcast(series$.jobj, "tsdb.TimeSeries"))
})

method("GROUP_BY_NAME", "JTimeSeriesGroupByNameTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JTimeSeriesGroupByNameTable$...GROUP_BY_NAME, JTimeSeriesGroupByNameTable(jobj = jField("tsdb/TimeSeriesGroupByNameTable", "Ltsdb/TimeSeriesGroupByNameTable;", "GROUP_BY_NAME")), log = FALSE)
})

