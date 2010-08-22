constructor("JBar", function(jobj = NULL) {
    extend(JObject(), "JBar", .jobj = jobj)
})

method("by_Date_double_double_double_double_Long_Long", "JBar", enforceRCC = TRUE, function(static, date = NULL, open = NULL, high = NULL, low = NULL, close = NULL, volume = NULL, openInterest = NULL, ...) {
    JBar(jNew("systemdb/data/Bar", .jcast(date$.jobj, "java.util.Date"), theNumeric(open), theNumeric(high), theNumeric(low), theNumeric(close), .jcast(volume$.jobj, "java.lang.Long"), .jcast(openInterest$.jobj, "java.lang.Long")))
})

method("compareTo_by_Object", "JBar", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(x0$.jobj, "java.lang.Object"))
})

method("compareTo_by_Bar", "JBar", enforceRCC = TRUE, function(this, o = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(o$.jobj, "systemdb.data.Bar"))
})

method("na", "JBar", enforceRCC = TRUE, function(static, ...) {
    jCall("systemdb/data/Bar", "J", "na")
})

method("rVolume", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "rVolume")
})

method("rOpenInterest", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "rOpenInterest")
})

method("volume", "JBar", enforceRCC = TRUE, function(this, ...) {
    JLong(jobj = jCall(this$.jobj, "Ljava/lang/Long;", "volume"))
})

method("openInterest", "JBar", enforceRCC = TRUE, function(this, ...) {
    JLong(jobj = jCall(this$.jobj, "Ljava/lang/Long;", "openInterest"))
})

method("low", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "low")
})

method("high", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "high")
})

method("close", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "close")
})

method("open", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "open")
})

method("date", "JBar", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "date"))
})

method("in_by_Range", "JBar", enforceRCC = TRUE, function(this, range = NULL, ...) {
    jCall(this$.jobj, "Z", "in", .jcast(range$.jobj, "util.Range"))
})

method("equals_by_Object", "JBar", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JBar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

