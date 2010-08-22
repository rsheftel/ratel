constructor("JClearLog", function(jobj = NULL) {
    extend(JObject(), "JClearLog", .jobj = jobj)
})

method("by", "JClearLog", enforceRCC = TRUE, function(static, ...) {
    JClearLog(jNew("util/ClearLog"))
})

method("main_by_StringArray", "JClearLog", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("util/ClearLog", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

