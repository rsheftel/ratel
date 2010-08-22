constructor("JArguments", function(jobj = NULL) {
    extend(JObject(), "JArguments", .jobj = jobj)
})

method("by_List", "JArguments", enforceRCC = TRUE, function(static, allowed = NULL, ...) {
    JArguments(jNew("util/Arguments", .jcast(allowed$.jobj, "java.util.List")))
})

method("get_by_Object", "JArguments", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "get", .jcast(x0$.jobj, "java.lang.Object")))
})

method("get_by_String_double", "JArguments", enforceRCC = TRUE, function(this, key = NULL, default_ = NULL, ...) {
    jCall(this$.jobj, "D", "get", the(key), theNumeric(default_))
})

method("arguments_by_StringArray_List", "JArguments", enforceRCC = TRUE, function(static, args = NULL, allowed = NULL, ...) {
    JArguments(jobj = jCall("util/Arguments", "Lutil/Arguments;", "arguments", jArray(args, "[Ljava/lang/String;"), .jcast(allowed$.jobj, "java.util.List")))
})

method("values_by_Arguments", "JArguments", enforceRCC = TRUE, function(static, arguments = NULL, ...) {
    JAttributeValues(jobj = jCall("util/Arguments", "Ltsdb/AttributeValues;", "values", .jcast(arguments$.jobj, "util.Arguments")))
})

method("numeric_by_String", "JArguments", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "D", "numeric", the(key))
})

method("integer_by_String", "JArguments", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "I", "integer", the(key))
})

method("date_by_Object", "JArguments", enforceRCC = TRUE, function(this, key = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "date", .jcast(key$.jobj, "java.lang.Object")))
})

method("string_by_String", "JArguments", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "string", the(name))
})

method("get_by_Object", "JArguments", enforceRCC = TRUE, function(this, key = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "get", .jcast(key$.jobj, "java.lang.Object"))
})

method("get_by_String_boolean", "JArguments", enforceRCC = TRUE, function(this, key = NULL, default_ = NULL, ...) {
    jCall(this$.jobj, "Z", "get", the(key), theLogical(default_))
})

method("get_by_String_int", "JArguments", enforceRCC = TRUE, function(this, name = NULL, defalt = NULL, ...) {
    jCall(this$.jobj, "I", "get", the(name), theInteger(defalt))
})

method("get_by_String_Date", "JArguments", enforceRCC = TRUE, function(this, name = NULL, defalt = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "get", the(name), .jcast(defalt$.jobj, "java.util.Date")))
})

method("get_by_String_String", "JArguments", enforceRCC = TRUE, function(this, name = NULL, defalt = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "get", the(name), the(defalt))
})

