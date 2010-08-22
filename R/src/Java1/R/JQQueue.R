constructor("JQQueue", function(jobj = NULL) {
    extend(JObject(), "JQQueue", .jobj = jobj)
})

method("by_Queue_String", "JQQueue", enforceRCC = TRUE, function(static, q = NULL, broker = NULL, ...) {
    JQQueue(jNew("jms/QQueue", .jcast(q$.jobj, "javax.jms.Queue"), the(broker)))
})

method("by_String_String", "JQQueue", enforceRCC = TRUE, function(static, name = NULL, broker = NULL, ...) {
    JQQueue(jNew("jms/QQueue", the(name), the(broker)))
})

method("by_String", "JQQueue", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JQQueue(jNew("jms/QQueue", the(name)))
})

method("shutdown", "JQQueue", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "shutdown")
})

method("register_by_Object", "JQQueue", enforceRCC = TRUE, function(this, receiver = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "register", .jcast(receiver$.jobj, "java.lang.Object")))
})

method("response_by_String_MessageReceiver_int", "JQQueue", enforceRCC = TRUE, function(this, text = NULL, receiver = NULL, heartbeatFrequencyMillis = NULL, ...) {
    jCall(this$.jobj, "V", "response", the(text), .jcast(receiver$.jobj, "jms.MessageReceiver"), theInteger(heartbeatFrequencyMillis))
})

method("main_by_StringArray", "JQQueue", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("jms/QQueue", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

