constructor("JFields", function(jobj = NULL) {
    extend(JObject(), "JFields", .jobj = jobj)
})

method("by", "JFields", enforceRCC = TRUE, function(static, ...) {
    JFields(jNew("systemdb/data/Fields"))
})

method("parse_by_String", "JFields", enforceRCC = TRUE, function(static, text = NULL, ...) {
    JFields(jobj = jCall("systemdb/data/Fields", "Lsystemdb/data/Fields;", "parse", the(text)))
})

method("isLong_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "Z", "isLong", the(key))
})

method("hasValue_by_String_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, value = NULL, ...) {
    jCall(this$.jobj, "Z", "hasValue", the(key), the(value))
})

method("isEmpty_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "Z", "isEmpty", the(key))
})

method("hasContent_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "Z", "hasContent", the(key))
})

method("longMaybe_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "J", "longMaybe", the(key))
})

method("put_by_String_long", "JFields", enforceRCC = TRUE, function(this, key = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "put", the(key), theLong(value))
})

method("put_by_String_double", "JFields", enforceRCC = TRUE, function(this, key = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "put", the(key), theNumeric(value))
})

method("put_by_String_Date", "JFields", enforceRCC = TRUE, function(this, key = NULL, value = NULL, ...) {
    jCall(this$.jobj, "V", "put", the(key), .jcast(value$.jobj, "java.util.Date"))
})

method("messageText", "JFields", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "messageText")
})

method("text_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "text", the(key))
})

method("bytes_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "[B", "bytes", the(key))
})

method("longg_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "J", "longg", the(key))
})

method("integer_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "I", "integer", the(key))
})

method("time_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "time", the(key)))
})

method("numeric_by_String", "JFields", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "D", "numeric", the(key))
})

method("copy", "JFields", enforceRCC = TRUE, function(this, ...) {
    JFields(jobj = jCall(this$.jobj, "Lsystemdb/data/Fields;", "copy"))
})

