constructor("JTimeSeriesTable", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesTable", .jobj = jobj)
})

method("by_String", "JTimeSeriesTable", enforceRCC = TRUE, function(static, alias = NULL, ...) {
    JTimeSeriesTable(jNew("tsdb/TimeSeriesTable", the(alias)))
})

method("by", "JTimeSeriesTable", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesTable(jNew("tsdb/TimeSeriesTable"))
})

method("dataTables_by_SelectOne", "JTimeSeriesTable", enforceRCC = TRUE, function(this, seriesLookup = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "dataTables", .jcast(seriesLookup$.jobj, "db.SelectOne")))
})

method("dataTables_by_Table", "JTimeSeriesTable", enforceRCC = TRUE, function(this, temp = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "dataTables", .jcast(temp$.jobj, "db.Table")))
})

method("dataTables_by_TimeSeriesArray", "JTimeSeriesTable", enforceRCC = TRUE, function(this, serieses = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "dataTables",  {
        if(inherits(serieses, "jarrayRef")) {
            serieses <- .jevalArray(serieses)
        }
        jArray(lapply(serieses, function(x) {
            x$.jobj
        }), "tsdb/TimeSeries")
    }))
})

method("dataTables_by_AttributeValues", "JTimeSeriesTable", enforceRCC = TRUE, function(this, values = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "dataTables", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("dataTable_by_int", "JTimeSeriesTable", enforceRCC = TRUE, function(this, seriesId = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "dataTable", theInteger(seriesId))
})

method("create_by_String_AttributeValues_TimeSeriesDataTable", "JTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, values = NULL, dataTable = NULL, ...) {
    jCall(this$.jobj, "V", "create", the(name), .jcast(values$.jobj, "tsdb.AttributeValues"), .jcast(dataTable$.jobj, "tsdb.TimeSeriesDataTable"))
})

method("delete_by_int", "JTimeSeriesTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "V", "delete", theInteger(id))
})

method("addAtributeValue_by_int_AttributeValue", "JTimeSeriesTable", enforceRCC = TRUE, function(this, tsId = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "addAtributeValue", theInteger(tsId), .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("createAttributeValues_by_String_AttributeValues", "JTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, values = NULL, ...) {
    jCall(this$.jobj, "V", "createAttributeValues", the(name), .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("create_by_String_AttributeValues", "JTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, values = NULL, ...) {
    jCall(this$.jobj, "V", "create", the(name), .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("exists_by_String", "JTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Z", "exists", the(name))
})

method("joinTo_by_IntColumn", "JTimeSeriesTable", enforceRCC = TRUE, function(this, otherId = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "joinTo", .jcast(otherId$.jobj, "db.columns.IntColumn")))
})

method("name_by_SelectOne", "JTimeSeriesTable", enforceRCC = TRUE, function(this, ids = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name", .jcast(ids$.jobj, "db.SelectOne"))
})

method("names_by_SelectOne", "JTimeSeriesTable", enforceRCC = TRUE, function(this, ids = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "names", .jcast(ids$.jobj, "db.SelectOne")))
})

method("name_by_int", "JTimeSeriesTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name", theInteger(id))
})

method("name_by_AttributeValues", "JTimeSeriesTable", enforceRCC = TRUE, function(this, values = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name", .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("names_by_AttributeValues", "JTimeSeriesTable", enforceRCC = TRUE, function(this, values = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "names", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("id_by_String", "JTimeSeriesTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "I", "id", the(name))
})

method("TIME_SERIES", "JTimeSeriesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JTimeSeriesTable$...TIME_SERIES, JTimeSeriesTable(jobj = jField("tsdb/TimeSeriesTable", "Ltsdb/TimeSeriesTable;", "TIME_SERIES")), log = FALSE)
})

