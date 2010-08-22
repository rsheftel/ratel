constructor("JTimeSeries", function(jobj = NULL) {
    extend(JObject(), "JTimeSeries", .jobj = jobj)
})

method("by_String", "JTimeSeries", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JTimeSeries(jNew("tsdb/TimeSeries", the(name)))
})

method("requireMissing_by_Attribute", "JTimeSeries", enforceRCC = TRUE, function(this, attr = NULL, ...) {
    jCall(this$.jobj, "V", "requireMissing", .jcast(attr$.jobj, "tsdb.Attribute"))
})

method("requireHas_by_AttributeValue", "JTimeSeries", enforceRCC = TRUE, function(this, value = NULL, ...) {
    jCall(this$.jobj, "V", "requireHas", .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("replaceAll_by_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(this, newAttributes = NULL, ...) {
    jCall(this$.jobj, "V", "replaceAll", .jcast(newAttributes$.jobj, "tsdb.AttributeValues"))
})

method("createFromFile_by_String", "JTimeSeries", enforceRCC = TRUE, function(static, filename = NULL, ...) {
    jCall("tsdb/TimeSeries", "V", "createFromFile", the(filename))
})

method("createFile_by_QDirectory", "JTimeSeries", enforceRCC = TRUE, function(this, directory = NULL, ...) {
    jCall(this$.jobj, "V", "createFile", .jcast(directory$.jobj, "file.QDirectory"))
})

method("createFromFile_by_String_boolean", "JTimeSeries", enforceRCC = TRUE, function(static, filename = NULL, ignoreExisting = NULL, ...) {
    jCall("tsdb/TimeSeries", "V", "createFromFile", the(filename), theLogical(ignoreExisting))
})

method("createSpecial_by_String", "JTimeSeries", enforceRCC = TRUE, function(static, filename = NULL, ...) {
    JSeriesToCreate(jobj = jCall("tsdb/TimeSeries", "Ltsdb/TimeSeries/SeriesToCreate;", "createSpecial", the(filename)))
})

method("with_by_DataSource", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, ...) {
    JSeriesSource(jobj = jCall(this$.jobj, "Ltsdb/SeriesSource;", "with", .jcast(source$.jobj, "tsdb.DataSource")))
})

method("main_by_StringArray", "JTimeSeries", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("tsdb/TimeSeries", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("createIfNeeded_by_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(this, attr = NULL, ...) {
    jCall(this$.jobj, "V", "createIfNeeded", .jcast(attr$.jobj, "tsdb.AttributeValues"))
})

method("series_by_Integer", "JTimeSeries", enforceRCC = TRUE, function(static, id = NULL, ...) {
    JTimeSeries(jobj = jCall("tsdb/TimeSeries", "Ltsdb/TimeSeries;", "series", .jcast(id$.jobj, "java.lang.Integer")))
})

method("equals_by_Object", "JTimeSeries", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("is_by_Column", "JTimeSeries", enforceRCC = TRUE, function(this, seriesId = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "is", .jcast(seriesId$.jobj, "db.Column")))
})

method("id", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "id")
})

method("exists", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "exists")
})

method("observationRows_by_DataSource_Observations", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, observations = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "observationRows", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(observations$.jobj, "tsdb.Observations")))
})

method("create_by_String_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(static, name = NULL, values = NULL, ...) {
    JTimeSeries(jobj = jCall("tsdb/TimeSeries", "Ltsdb/TimeSeries;", "create", the(name), .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("attributes", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    JAttributeValues(jobj = jCall(this$.jobj, "Ltsdb/AttributeValues;", "attributes"))
})

method("write_by_DataSource_Observations", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, observations = NULL, ...) {
    jCall(this$.jobj, "V", "write", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(observations$.jobj, "tsdb.Observations"))
})

method("purgeAllData", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "purgeAllData")
})

method("delete", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "delete")
})

method("addAttributeValue_by_AttributeValue", "JTimeSeries", enforceRCC = TRUE, function(this, value = NULL, ...) {
    jCall(this$.jobj, "V", "addAttributeValue", .jcast(value$.jobj, "tsdb.AttributeValue"))
})

method("create_by_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(this, values = NULL, ...) {
    jCall(this$.jobj, "V", "create", .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("name", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("toString", "JTimeSeries", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("exists_by_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(static, values = NULL, ...) {
    jCall("tsdb/TimeSeries", "Z", "exists", .jcast(values$.jobj, "tsdb.AttributeValues"))
})

method("multiSeries_by_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(static, values = NULL, ...) {
    JList(jobj = jCall("tsdb/TimeSeries", "Ljava/util/List;", "multiSeries", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("series_by_AttributeValues", "JTimeSeries", enforceRCC = TRUE, function(static, values = NULL, ...) {
    JTimeSeries(jobj = jCall("tsdb/TimeSeries", "Ltsdb/TimeSeries;", "series", .jcast(values$.jobj, "tsdb.AttributeValues")))
})

method("series_by_List", "JTimeSeries", enforceRCC = TRUE, function(static, names = NULL, ...) {
    JList(jobj = jCall("tsdb/TimeSeries", "Ljava/util/List;", "series", .jcast(names$.jobj, "java.util.List")))
})

method("series_by_String", "JTimeSeries", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JTimeSeries(jobj = jCall("tsdb/TimeSeries", "Ltsdb/TimeSeries;", "series", the(name)))
})

method("observations_by_DataSource_Range_int", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, range = NULL, count = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range"), theInteger(count)))
})

method("observations_by_DataSource_int", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, count = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), theInteger(count)))
})

method("observations_by_DataSource_Range", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, range = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource"), .jcast(range$.jobj, "util.Range")))
})

method("observations_by_DataSource", "JTimeSeries", enforceRCC = TRUE, function(this, source = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(source$.jobj, "tsdb.DataSource")))
})

method("BY_NAME", "JTimeSeries", enforceRCC = FALSE, function(static, ...) {
    lazy(JTimeSeries$...BY_NAME, JByName(jobj = jField("tsdb/TimeSeries", "Ltsdb/TimeSeries/ByName;", "BY_NAME")), log = FALSE)
})

