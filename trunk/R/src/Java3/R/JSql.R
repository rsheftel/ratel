constructor("JSql", function(jobj = NULL) {
    extend(JObject(), "JSql", .jobj = jobj)
})

method("by", "JSql", enforceRCC = TRUE, function(static, ...) {
    JSql(jNew("util/Sql"))
})

method("quote_by_String", "JSql", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Sql", "Ljava/lang/String;", "quote", the(s))
})

