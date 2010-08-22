constructor("JQType", function(jobj = NULL) {
    extend(JObject(), "JQType", .jobj = jobj)
})

method("by_Type", "JQType", enforceRCC = TRUE, function(static, type = NULL, ...) {
    JQType(jNew("util/QType", .jcast(type$.jobj, "com.sun.tools.javac.code.Type")))
})

method("isGeneric", "JQType", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isGeneric")
})

method("simpleName", "JQType", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "simpleName")
})

method("componentType", "JQType", enforceRCC = TRUE, function(this, ...) {
    JQType(jobj = jCall(this$.jobj, "Lutil/QType;", "componentType"))
})

method("toString", "JQType", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("qualifiedName", "JQType", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "qualifiedName")
})

method("isArray", "JQType", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isArray")
})

