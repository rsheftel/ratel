constructor("JTimeSeriesGroupByAttributesTable", function(jobj = NULL) {
    extend(JObject(), "JTimeSeriesGroupByAttributesTable", .jobj = jobj)
})

method("by", "JTimeSeriesGroupByAttributesTable", enforceRCC = TRUE, function(static, ...) {
    JTimeSeriesGroupByAttributesTable(jNew("tsdb/TimeSeriesGroupByAttributesTable"))
})

method("delete_by_int", "JTimeSeriesGroupByAttributesTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    jCall(this$.jobj, "V", "delete", theInteger(id))
})

method("seriesLookup_by_int_Date", "JTimeSeriesGroupByAttributesTable", enforceRCC = TRUE, function(this, id = NULL, asOf = NULL, ...) {
    JSelectOne(jobj = jCall(this$.jobj, "Ldb/SelectOne;", "seriesLookup", theInteger(id), .jcast(asOf$.jobj, "java.util.Date")))
})

method("deleteAll", "JTimeSeriesGroupByAttributesTable", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "deleteAll")
})

method("attributes_by_int", "JTimeSeriesGroupByAttributesTable", enforceRCC = TRUE, function(this, id = NULL, ...) {
    JAttributeValues(jobj = jCall(this$.jobj, "Ltsdb/AttributeValues;", "attributes", theInteger(id)))
})

method("insert_by_int_AttributeValue", "JTimeSeriesGroupByAttributesTable", enforceRCC = TRUE, function(this, arbId = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "insert", theInteger(arbId), .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("GROUP_BY_ATTRIBUTES", "JTimeSeriesGroupByAttributesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JTimeSeriesGroupByAttributesTable$...GROUP_BY_ATTRIBUTES, JTimeSeriesGroupByAttributesTable(jobj = jField("tsdb/TimeSeriesGroupByAttributesTable", "Ltsdb/TimeSeriesGroupByAttributesTable;", "GROUP_BY_ATTRIBUTES")), log = FALSE)
})

