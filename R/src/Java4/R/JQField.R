constructor("JQField", function(jobj = NULL) {
    extend(JObject(), "JQField", .jobj = jobj)
})

method("by_Symbol", "JQField", enforceRCC = TRUE, function(static, symbol = NULL, ...) {
    JQField(jNew("util/QField", .jcast(symbol$.jobj, "com.sun.tools.javac.code.Symbol")))
})

method("isStatic", "JQField", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isStatic")
})

method("type", "JQField", enforceRCC = TRUE, function(this, ...) {
    JQType(jobj = jCall(this$.jobj, "Lutil/QType;", "type"))
})

method("name", "JQField", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

