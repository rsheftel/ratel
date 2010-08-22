constructor("JChannel", function(jobj = NULL) {
    extend(JObject(), "JChannel", .jobj = jobj)
})

method("by_String_String", "JChannel", enforceRCC = TRUE, function(static, name = NULL, broker = NULL, ...) {
    JChannel(jNew("jms/Channel", the(name), the(broker)))
})

method("by_String", "JChannel", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JChannel(jNew("jms/Channel", the(name)))
})

method("main_by_StringArray", "JChannel", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("jms/Channel", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("setReadonly_by_boolean", "JChannel", enforceRCC = TRUE, function(this, beReadonly = NULL, ...) {
    jCall(this$.jobj, "V", "setReadonly", theLogical(beReadonly))
})

method("equals_by_Object", "JChannel", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JChannel", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("name", "JChannel", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("clear", "JChannel", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("shutdown", "JChannel", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "shutdown")
})

method("send_by_String_Map", "JChannel", enforceRCC = TRUE, function(this, text = NULL, properties = NULL, ...) {
    jCall(this$.jobj, "V", "send", the(text), .jcast(properties$.jobj, "java.util.Map"))
})

method("send_by_String_Map_String", "JChannel", enforceRCC = TRUE, function(this, text = NULL, properties = NULL, correlationId = NULL, ...) {
    jCall(this$.jobj, "V", "send", the(text), .jcast(properties$.jobj, "java.util.Map"), the(correlationId))
})

method("send_by_Fields", "JChannel", enforceRCC = TRUE, function(this, fields = NULL, ...) {
    jCall(this$.jobj, "V", "send", .jcast(fields$.jobj, "systemdb.data.Fields"))
})

method("send_by_String", "JChannel", enforceRCC = TRUE, function(this, message = NULL, ...) {
    jCall(this$.jobj, "V", "send", the(message))
})

method("toString", "JChannel", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("closeResources", "JChannel", enforceRCC = TRUE, function(static, ...) {
    jCall("jms/Channel", "V", "closeResources")
})

method("register_by_Object", "JChannel", enforceRCC = TRUE, function(this, receiver = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "register", .jcast(receiver$.jobj, "java.lang.Object")))
})

method("logMessage_by_char_String_String", "JChannel", enforceRCC = TRUE, function(this, mark = NULL, divider = NULL, message = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "logMessage", fail("char parameters not handled by R/Java convertPrimitive!"), the(divider), the(message))
})

method("log_by_char_String_String", "JChannel", enforceRCC = TRUE, function(this, mark = NULL, divider = NULL, message = NULL, ...) {
    jCall(this$.jobj, "V", "log", fail("char parameters not handled by R/Java convertPrimitive!"), the(divider), the(message))
})

method("connect", "JChannel", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "connect")
})

method("defaultBroker", "JChannel", enforceRCC = TRUE, function(static, ...) {
    jCall("jms/Channel", "Ljava/lang/String;", "defaultBroker")
})

method("setDefaultBroker_by_String", "JChannel", enforceRCC = TRUE, function(static, broker = NULL, ...) {
    jCall("jms/Channel", "V", "setDefaultBroker", the(broker))
})

