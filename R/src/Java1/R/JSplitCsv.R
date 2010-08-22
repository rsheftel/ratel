constructor("JSplitCsv", function(jobj = NULL) {
    extend(JObject(), "JSplitCsv", .jobj = jobj)
})

method("by", "JSplitCsv", enforceRCC = TRUE, function(static, ...) {
    JSplitCsv(jNew("file/SplitCsv"))
})

method("main_by_StringArray", "JSplitCsv", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("file/SplitCsv", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

