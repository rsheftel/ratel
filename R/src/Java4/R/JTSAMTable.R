constructor("JTSAMTable", function(jobj = NULL) {
    extend(JObject(), "JTSAMTable", .jobj = jobj)
})

method("attributes_by_TimeSeries", "JTSAMTable", enforceRCC = TRUE, function(this, timeSeries = NULL, ...) {
    JAttributeValues(jobj = jCall(this$.jobj, "Ltsdb/AttributeValues;", "attributes", .jcast(timeSeries$.jobj, "tsdb.TimeSeries")))
})

method("deleteAttributes_by_int", "JTSAMTable", enforceRCC = TRUE, function(this, timeSeriesId = NULL, ...) {
    jCall(this$.jobj, "V", "deleteAttributes", theInteger(timeSeriesId))
})

method("add_by_int_AttributeValue", "JTSAMTable", enforceRCC = TRUE, function(this, tsId = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "add", theInteger(tsId), .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("create_by_int_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(this, tsId = NULL, values = NULL, ...) {
    jCall(this$.jobj, "V", "create", theInteger(tsId), .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("timeSeriesIdMatches_by_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(this, values = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "timeSeriesIdMatches", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("timeSeriesIdLookup_by_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(this, values = NULL, ...) {
    JSelectOne(jobj = jCall(this$.jobj, "Ldb/SelectOne;", "timeSeriesIdLookup", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("attributeMatches_by_AttributeValue", "JTSAMTable", enforceRCC = TRUE, function(this, value = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "attributeMatches", .jcast(value$.jobj, "tsdb.AttributeValue")))
})

method("timeSeriesName_by_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(static, values = NULL, ...) {
    jCall("tsdb/TSAMTable", "Ljava/lang/String;", "timeSeriesName", .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("timeSeriesNames_by_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(static, values = NULL, ...) {
    JList(jobj = jCall("tsdb/TSAMTable", "Ljava/util/List;", "timeSeriesNames", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("timeSeriesIds_by_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(static, values = NULL, ...) {
    JList(jobj = jCall("tsdb/TSAMTable", "Ljava/util/List;", "timeSeriesIds", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("timeSeriesId_by_AttributeValues", "JTSAMTable", enforceRCC = TRUE, function(static, values = NULL, ...) {
    jCall("tsdb/TSAMTable", "I", "timeSeriesId", .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("alias_by_String", "JTSAMTable", enforceRCC = TRUE, function(static, alias = NULL, ...) {
    JTSAMTable(jobj = jCall("tsdb/TSAMTable", "Ltsdb/TSAMTable;", "alias", the(alias)))
})

method("TSAM", "JTSAMTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JTSAMTable$...TSAM, JTSAMTable(jobj = jField("tsdb/TSAMTable", "Ltsdb/TSAMTable;", "TSAM")), log = FALSE)
})

