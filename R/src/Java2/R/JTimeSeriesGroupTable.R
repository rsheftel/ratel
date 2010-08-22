constructor("JTimeSeriesGroupTable", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesGroupTable", .jobj = jobj)
})

method("by", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesGroupTable(jNew("tsdb/TimeSeriesGroupTable"))
})

method("has_by_String", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, groupName = NULL, ...) {
    jCall(this$.jobj, "Z", "has", the(groupName))
})

method("id_by_String", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, groupName = NULL, ...) {
    jCall(this$.jobj, "I", "id", the(groupName))
})

method("get_by_String", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JTimeSeriesGroup(jobj = jCall(this$.jobj, "Ltsdb/TimeSeriesGroupTable/TimeSeriesGroup;", "get", the(name)))
})

method("insert_by_String_String", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, name = NULL, type = NULL, ...) {
    JTimeSeriesGroup(jobj = jCall(this$.jobj, "Ltsdb/TimeSeriesGroupTable/TimeSeriesGroup;", "insert", the(name), the(type)))
})

method("insert_by_String_AttributeValues", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, name = NULL, values = NULL, ...) {
    JTimeSeriesGroup(jobj = jCall(this$.jobj, "Ltsdb/TimeSeriesGroupTable/TimeSeriesGroup;", "insert", the(name), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("insert_by_String_TimeSeriesArray", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, name = NULL, series = NULL, ...) {
    JTimeSeriesGroup(jobj = jCall(this$.jobj, "Ltsdb/TimeSeriesGroupTable/TimeSeriesGroup;", "insert", the(name),  {
        if(inherits(series, "jarrayRef")) {
            series <- .jevalArray(series)
        }
        jArray(lapply(series, function(x) {
            x$.jobj
        }), "tsdb/TimeSeries")
    }))
})

method("insert_by_String_List", "JTimeSeriesGroupTable", enforceRCC = TRUE, function(this, name = NULL, series = NULL, ...) {
    JTimeSeriesGroup(jobj = jCall(this$.jobj, "Ltsdb/TimeSeriesGroupTable/TimeSeriesGroup;", "insert", the(name), .jcast(series$.jobj, "java.util.List")))
})

method("GROUPS", "JTimeSeriesGroupTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JTimeSeriesGroupTable$...GROUPS, JTimeSeriesGroupTable(jobj = jField("tsdb/TimeSeriesGroupTable", "Ltsdb/TimeSeriesGroupTable;", "GROUPS")), log = FALSE)
})

