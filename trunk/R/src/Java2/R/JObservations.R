constructor("JObservations", function(jobj = NULL) {
    extend(JObject(), "JObservations", .jobj = jobj)
})

method("by_longArray_doubleArray", "JObservations", enforceRCC = TRUE, function(static, datesMillis = NULL, values = NULL, ...) {
    JObservations(jNew("tsdb/Observations", jArray(.jlong(datesMillis), "[J"), jArray(as.numeric(values), "[D")))
})

method("by_Date_Double", "JObservations", enforceRCC = TRUE, function(static, date = NULL, value = NULL, ...) {
    JObservations(jNew("tsdb/Observations", .jcast(date$.jobj, "java.util.Date"), .jcast(value$.jobj, "java.lang.Double")))
})

method("by", "JObservations", enforceRCC = TRUE, function(static, ...) {
    JObservations(jNew("tsdb/Observations"))
})

method("by_List", "JObservations", enforceRCC = TRUE, function(static, rows = NULL, ...) {
    JObservations(jNew("tsdb/Observations", .jcast(rows$.jobj, "java.util.List")))
})

method("mostRecentValueMaybe", "JObservations", enforceRCC = TRUE, function(this, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "mostRecentValueMaybe"))
})

method("mostRecentValue", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "mostRecentValue")
})

method("remove_by_Date", "JObservations", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "V", "remove", .jcast(date$.jobj, "java.util.Date"))
})

method("remove_by_String", "JObservations", enforceRCC = TRUE, function(this, string = NULL, ...) {
    jCall(this$.jobj, "V", "remove", the(string))
})

method("closes_by_List", "JObservations", enforceRCC = TRUE, function(static, bars = NULL, ...) {
    JObservations(jobj = jCall("tsdb/Observations", "Ltsdb/Observations;", "closes", .jcast(bars$.jobj, "java.util.List")))
})

method("valueOrNull", "JObservations", enforceRCC = TRUE, function(this, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "valueOrNull"))
})

method("valueOrNull_by_Date", "JObservations", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "valueOrNull", .jcast(date$.jobj, "java.util.Date")))
})

method("dateRange", "JObservations", enforceRCC = TRUE, function(this, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "dateRange"))
})

method("iterator", "JObservations", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("write_by_SeriesSource", "JObservations", enforceRCC = TRUE, function(this, seriesSource = NULL, ...) {
    jCall(this$.jobj, "V", "write", .jcast(seriesSource$.jobj, "tsdb.SeriesSource"))
})

method("hasContent", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasContent")
})

method("value", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "value")
})

method("isEmpty", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("write_by_TsdbObservations", "JObservations", enforceRCC = TRUE, function(static, writeThese = NULL, ...) {
    jCall("tsdb/Observations", "V", "write", .jcast(writeThese$.jobj, "tsdb.TsdbObservations"))
})

method("equals_by_Object", "JObservations", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("values", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[D", "values")
})

method("timesMillis", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[J", "timesMillis")
})

method("times", "JObservations", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "times"))
})

method("set_by_Date_double", "JObservations", enforceRCC = TRUE, function(this, date = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "set", .jcast(date$.jobj, "java.util.Date"), theNumeric(value))
})

method("set_by_String_double", "JObservations", enforceRCC = TRUE, function(this, date = NULL, d = NULL, ...) {
    jCall(this$.jobj, "V", "set", the(date), theNumeric(d))
})

method("has_by_Date", "JObservations", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "Z", "has", .jcast(date$.jobj, "java.util.Date"))
})

method("value_by_Date", "JObservations", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "D", "value", .jcast(date$.jobj, "java.util.Date"))
})

method("value_by_String", "JObservations", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "D", "value", the(date))
})

method("time", "JObservations", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "time"))
})

method("size", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

method("toString", "JObservations", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

