constructor("JObservationsMap", function(jobj = NULL) {
    extend(JObject(), "JObservationsMap", .jobj = jobj)
})

method("by", "JObservationsMap", enforceRCC = TRUE, function(static, ...) {
    JObservationsMap(jNew("tsdb/ObservationsMap"))
})

method("longValueMaybe_by_Object_Date", "JObservationsMap", enforceRCC = TRUE, function(this, ss = NULL, date = NULL, ...) {
    JLong(jobj = jCall(this$.jobj, "Ljava/lang/Long;", "longValueMaybe", .jcast(ss$.jobj, "java.lang.Object"), .jcast(date$.jobj, "java.util.Date")))
})

method("longValue_by_Object_Date", "JObservationsMap", enforceRCC = TRUE, function(this, ss = NULL, date = NULL, ...) {
    jCall(this$.jobj, "J", "longValue", .jcast(ss$.jobj, "java.lang.Object"), .jcast(date$.jobj, "java.util.Date"))
})

method("value_by_Object_Date", "JObservationsMap", enforceRCC = TRUE, function(this, ss = NULL, date = NULL, ...) {
    jCall(this$.jobj, "D", "value", .jcast(ss$.jobj, "java.lang.Object"), .jcast(date$.jobj, "java.util.Date"))
})

method("valueMaybe_by_Object_Date", "JObservationsMap", enforceRCC = TRUE, function(this, ss = NULL, date = NULL, ...) {
    JDouble(jobj = jCall(this$.jobj, "Ljava/lang/Double;", "valueMaybe", .jcast(ss$.jobj, "java.lang.Object"), .jcast(date$.jobj, "java.util.Date")))
})

method("totalRows", "JObservationsMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "totalRows")
})

method("add_by_ObservationsMap", "JObservationsMap", enforceRCC = TRUE, function(this, obs = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(obs$.jobj, "tsdb.ObservationsMap"))
})

method("observationses", "JObservationsMap", enforceRCC = TRUE, function(this, ...) {
    JCollection(jobj = jCall(this$.jobj, "Ljava/util/Collection;", "observationses"))
})

method("add_by_Object_Observations", "JObservationsMap", enforceRCC = TRUE, function(this, ss = NULL, o = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(ss$.jobj, "java.lang.Object"), .jcast(o$.jobj, "tsdb.Observations"))
})

method("iterator", "JObservationsMap", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("size", "JObservationsMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

method("only", "JObservationsMap", enforceRCC = TRUE, function(this, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "only"))
})

method("isEmpty", "JObservationsMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("get_by_Object", "JObservationsMap", enforceRCC = TRUE, function(this, s = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "get", .jcast(s$.jobj, "java.lang.Object")))
})

method("add_by_Object", "JObservationsMap", enforceRCC = TRUE, function(this, s = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(s$.jobj, "java.lang.Object"))
})

method("has_by_Object", "JObservationsMap", enforceRCC = TRUE, function(this, s = NULL, ...) {
    jCall(this$.jobj, "Z", "has", .jcast(s$.jobj, "java.lang.Object"))
})

