constructor("JQMethod", function(jobj = NULL) {
    extend(JObject(), "JQMethod", .jobj = jobj)
})

method("isStatic", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isStatic")
})

method("simpleReturnType", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "simpleReturnType")
})

method("returnTypeName", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "returnTypeName")
})

method("returnType", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    JQType(jobj = jCall(this$.jobj, "Lutil/QType;", "returnType"))
})

method("isPublic", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isPublic")
})

method("toString", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("name", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("parameters", "JQMethod", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "parameters"))
})

