constructor("JErrors", function(jobj = NULL) {
    extend(JObject(), "JErrors", .jobj = jobj)
})

method("by", "JErrors", enforceRCC = TRUE, function(static, ...) {
    JErrors(jNew("util/Errors"))
})

method("trace_by_Throwable", "JErrors", enforceRCC = TRUE, function(static, e = NULL, ...) {
    jCall("util/Errors", "Ljava/lang/String;", "trace", .jcast(e$.jobj, "java.lang.Throwable"))
})

method("bombNotNull_by_Object_String", "JErrors", enforceRCC = TRUE, function(static, o = NULL, message = NULL, ...) {
    jCall("util/Errors", "V", "bombNotNull", .jcast(o$.jobj, "java.lang.Object"), the(message))
})

method("bombEmpty_by_String_String", "JErrors", enforceRCC = TRUE, function(static, s = NULL, message = NULL, ...) {
    jCall("util/Errors", "Ljava/lang/String;", "bombEmpty", the(s), the(message))
})

method("bombNull_by_Object_String", "JErrors", enforceRCC = TRUE, function(static, o = NULL, message = NULL, ...) {
    JObject(jobj = jCall("util/Errors", "Ljava/lang/Object;", "bombNull", .jcast(o$.jobj, "java.lang.Object"), the(message)))
})

method("bombIf_by_boolean_String", "JErrors", enforceRCC = TRUE, function(static, condition = NULL, message = NULL, ...) {
    jCall("util/Errors", "V", "bombIf", theLogical(condition), the(message))
})

method("bombUnless_by_boolean_String", "JErrors", enforceRCC = TRUE, function(static, condition = NULL, message = NULL, ...) {
    jCall("util/Errors", "V", "bombUnless", theLogical(condition), the(message))
})

method("bomb_by_String", "JErrors", enforceRCC = TRUE, function(static, s = NULL, ...) {
    JRuntimeException(jobj = jCall("util/Errors", "Ljava/lang/RuntimeException;", "bomb", the(s)))
})

method("bomb_by_String_Throwable", "JErrors", enforceRCC = TRUE, function(static, s = NULL, t = NULL, ...) {
    JRuntimeException(jobj = jCall("util/Errors", "Ljava/lang/RuntimeException;", "bomb", the(s), .jcast(t$.jobj, "java.lang.Throwable")))
})

method("bomb_by_Throwable", "JErrors", enforceRCC = TRUE, function(static, t = NULL, ...) {
    JRuntimeException(jobj = jCall("util/Errors", "Ljava/lang/RuntimeException;", "bomb", .jcast(t$.jobj, "java.lang.Throwable")))
})

