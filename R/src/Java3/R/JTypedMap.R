constructor("JTypedMap", function(jobj = NULL) {
    extend(JObject(), "JTypedMap", .jobj = jobj)
})

method("by", "JTypedMap", enforceRCC = TRUE, function(static, ...) {
    JTypedMap(jNew("util/TypedMap"))
})

method("timeOn_by_Date_String", "JTypedMap", enforceRCC = TRUE, function(this, date = NULL, key = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "timeOn", .jcast(date$.jobj, "java.util.Date"), the(key)))
})

method("todayAt_by_String", "JTypedMap", enforceRCC = TRUE, function(this, key = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "todayAt", the(key)))
})

method("long__by_String_long", "JTypedMap", enforceRCC = TRUE, function(this, key = NULL, defalt = NULL, ...) {
    jCall(this$.jobj, "J", "long_", the(key), theLong(defalt))
})

method("numeric_by_String", "JTypedMap", enforceRCC = TRUE, function(this, lastField = NULL, ...) {
    jCall(this$.jobj, "D", "numeric", the(lastField))
})

