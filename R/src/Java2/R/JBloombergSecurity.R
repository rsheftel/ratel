constructor("JBloombergSecurity", function(jobj = NULL) {
    extend(JObject(), "JBloombergSecurity", .jobj = jobj)
})

method("by_String", "JBloombergSecurity", enforceRCC = TRUE, function(static, key = NULL, ...) {
    JBloombergSecurity(jNew("bloomberg/BloombergSecurity", the(key)))
})

method("bars_by_Range_Interval", "JBloombergSecurity", enforceRCC = TRUE, function(this, range = NULL, interval = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(range$.jobj, "util.Range"), .jcast(interval$.jobj, "systemdb.data.Interval")))
})

method("observations_by_String_Range", "JBloombergSecurity", enforceRCC = TRUE, function(this, fieldName = NULL, range = NULL, ...) {
    JObservations(jobj = jCall(this$.jobj, "Ltsdb/Observations;", "observations", the(fieldName), .jcast(range$.jobj, "util.Range")))
})

method("numeric_by_String_Date", "JBloombergSecurity", enforceRCC = TRUE, function(this, fieldName = NULL, date = NULL, ...) {
    jCall(this$.jobj, "D", "numeric", the(fieldName), .jcast(date$.jobj, "java.util.Date"))
})

method("numeric_by_String", "JBloombergSecurity", enforceRCC = TRUE, function(this, fieldName = NULL, ...) {
    jCall(this$.jobj, "D", "numeric", the(fieldName))
})

method("string_by_String", "JBloombergSecurity", enforceRCC = TRUE, function(this, fieldName = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "string", the(fieldName))
})

method("equals_by_Object", "JBloombergSecurity", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JBloombergSecurity", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JBloombergSecurity", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("setOn_by_Request", "JBloombergSecurity", enforceRCC = TRUE, function(this, request = NULL, ...) {
    jCall(this$.jobj, "V", "setOn", .jcast(request$.jobj, "com.bloomberglp.blpapi.Request"))
})

method("addTo_by_Request", "JBloombergSecurity", enforceRCC = TRUE, function(this, request = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", .jcast(request$.jobj, "com.bloomberglp.blpapi.Request"))
})

method("security_by_String", "JBloombergSecurity", enforceRCC = TRUE, function(static, key = NULL, ...) {
    JBloombergSecurity(jobj = jCall("bloomberg/BloombergSecurity", "Lbloomberg/BloombergSecurity;", "security", the(key)))
})

method("BBG_END_INTRADAY", "JBloombergSecurity", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergSecurity$...BBG_END_INTRADAY, JDate(jobj = jField("bloomberg/BloombergSecurity", "Ljava/util/Date;", "BBG_END_INTRADAY")), log = FALSE)
})

method("BBG_START_INTRADAY", "JBloombergSecurity", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergSecurity$...BBG_START_INTRADAY, JDate(jobj = jField("bloomberg/BloombergSecurity", "Ljava/util/Date;", "BBG_START_INTRADAY")), log = FALSE)
})

method("BBG_END_HISTORICAL", "JBloombergSecurity", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergSecurity$...BBG_END_HISTORICAL, JDate(jobj = jField("bloomberg/BloombergSecurity", "Ljava/util/Date;", "BBG_END_HISTORICAL")), log = FALSE)
})

method("BBG_START_HISTORICAL", "JBloombergSecurity", enforceRCC = FALSE, function(static, ...) {
    lazy(JBloombergSecurity$...BBG_START_HISTORICAL, JDate(jobj = jField("bloomberg/BloombergSecurity", "Ljava/util/Date;", "BBG_START_HISTORICAL")), log = FALSE)
})

