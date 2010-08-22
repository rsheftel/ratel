constructor("JTimeSeriesGroupDefinition", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesGroupDefinition", .jobj = jobj)
})

method("delete_by_int", "JTimeSeriesGroupDefinition", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "delete", theInteger(j_arg0))
})

method("seriesLookup_by_int_Date", "JTimeSeriesGroupDefinition", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JSelectOne(jobj = jCall(this$.jobj, "Ldb/SelectOne;", "seriesLookup", theInteger(j_arg0), .jcast(j_arg1$.jobj, "java.util.Date")))
})

