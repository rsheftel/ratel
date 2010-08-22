constructor("JSymbol", function(jobj = NULL) {
    extend(JObject(), "JSymbol", .jobj = jobj)
})

method("by_String", "JSymbol", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JSymbol(jNew("systemdb/data/Symbol", the(name)))
})

method("by_String_double", "JSymbol", enforceRCC = TRUE, function(static, name = NULL, bigPointValue = NULL, ...) {
    JSymbol(jNew("systemdb/data/Symbol", the(name), theNumeric(bigPointValue)))
})

method("hasPeriods", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasPeriods")
})

method("ends", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    lapply(jCall(this$.jobj, "[Ljava/util/Date;", "ends"), JDate)
})

method("starts", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    lapply(jCall(this$.jobj, "[Ljava/util/Date;", "starts"), JDate)
})

method("inactivePeriods", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "inactivePeriods"))
})

method("addPeriod_by_Date_Date", "JSymbol", enforceRCC = TRUE, function(this, start = NULL, end = NULL, ...) {
    jCall(this$.jobj, "V", "addPeriod", .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"))
})

method("addPeriod_by_String_String", "JSymbol", enforceRCC = TRUE, function(this, start = NULL, end = NULL, ...) {
    jCall(this$.jobj, "V", "addPeriod", the(start), the(end))
})

method("activePeriods", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "activePeriods"))
})

method("lastBarDate", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "lastBarDate"))
})

method("currency", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "currency")
})

method("firstBarDate", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "firstBarDate"))
})

method("barCount", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "barCount")
})

method("type", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "type")
})

method("observations", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations"))
})

method("observations_by_Range", "JSymbol", enforceRCC = TRUE, function(this, range = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", .jcast(range$.jobj, "util.Range")))
})

method("live", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JLiveDataSource(jobj = jCall(this$.jobj, "Lsystemdb/metadata/LiveDataSource;", "live"))
})

method("jmsLive", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JJmsLiveDescription(jobj = jCall(this$.jobj, "Lsystemdb/metadata/JmsLiveDescription;", "jmsLive"))
})

method("main_by_StringArray", "JSymbol", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("systemdb/data/Symbol", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("subscribe_by_TickListener", "JSymbol", enforceRCC = TRUE, function(this, listener = NULL, ...) {
    jCall(this$.jobj, "V", "subscribe", .jcast(listener$.jobj, "systemdb.data.TickListener"))
})

method("subscribe_by_ObservationListener", "JSymbol", enforceRCC = TRUE, function(this, listener = NULL, ...) {
    jCall(this$.jobj, "V", "subscribe", .jcast(listener$.jobj, "systemdb.data.ObservationListener"))
})

method("toString", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("bigPointValue", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "bigPointValue")
})

method("equals_by_Object", "JSymbol", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("name", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("rBars", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JRBarData(jobj = jCall(this$.jobj, "Lsystemdb/data/RBarData;", "rBars"))
})

method("rBars_by_Range", "JSymbol", enforceRCC = TRUE, function(this, range = NULL, ...) {
    JRBarData(jobj = jCall(this$.jobj, "Lsystemdb/data/RBarData;", "rBars", .jcast(range$.jobj, "util.Range")))
})

method("lastBars_by_int", "JSymbol", enforceRCC = TRUE, function(this, count = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "lastBars", theInteger(count)))
})

method("bars", "JSymbol", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars"))
})

method("bars_by_Range", "JSymbol", enforceRCC = TRUE, function(this, range = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(range$.jobj, "util.Range")))
})

method("bars_by_Interval", "JSymbol", enforceRCC = TRUE, function(this, interval = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(interval$.jobj, "systemdb.data.Interval")))
})

method("bars_by_Range_Interval", "JSymbol", enforceRCC = TRUE, function(this, range = NULL, interval = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(range$.jobj, "util.Range"), .jcast(interval$.jobj, "systemdb.data.Interval")))
})

