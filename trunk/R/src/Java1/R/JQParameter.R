constructor("JQParameter", function(jobj = NULL) {
    extend(JObject(), "JQParameter", .jobj = jobj)
})

method("by_VarSymbol", "JQParameter", enforceRCC = TRUE, function(static, symbol = NULL, ...) {
    JQParameter(jNew("util/QParameter", .jcast(symbol$.jobj, "com.sun.tools.javac.code.Symbol.VarSymbol")))
})

method("toString", "JQParameter", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("simpleTypes_by_List", "JQParameter", enforceRCC = TRUE, function(static, params = NULL, ...) {
    JList(jobj = jCall("util/QParameter", "Ljava/util/List;", "simpleTypes", .jcast(params$.jobj, "java.util.List")))
})

method("names_by_List", "JQParameter", enforceRCC = TRUE, function(static, params = NULL, ...) {
    JList(jobj = jCall("util/QParameter", "Ljava/util/List;", "names", .jcast(params$.jobj, "java.util.List")))
})

method("simpleType", "JQParameter", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "simpleType")
})

method("type", "JQParameter", enforceRCC = TRUE, function(this, ...) {
    JQType(jobj = jCall(this$.jobj, "Lutil/QType;", "type"))
})

method("isArray", "JQParameter", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isArray")
})

method("name", "JQParameter", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

