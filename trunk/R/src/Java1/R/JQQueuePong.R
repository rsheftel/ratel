constructor("JQQueuePong", function(jobj = NULL) {
    extend(JObject(), "JQQueuePong", .jobj = jobj)
})

method("main_by_StringArray", "JQQueuePong", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("jms/QQueuePong", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

