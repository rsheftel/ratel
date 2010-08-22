constructor("JMessageResponder", function(jobj = NULL) {
    extend(JObject(), "JMessageResponder", .jobj = jobj)
})

method("by", "JMessageResponder", enforceRCC = TRUE, function(static, ...) {
    JMessageResponder(jNew("jms/MessageResponder"))
})

method("setHeartbeatOn_by_boolean", "JMessageResponder", enforceRCC = TRUE, function(static, isOn = NULL, ...) {
    jCall("jms/MessageResponder", "V", "setHeartbeatOn", theLogical(isOn))
})

method("reply_by_String", "JMessageResponder", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "reply", the(j_arg0))
})

method("onMessage_by_Envelope", "JMessageResponder", enforceRCC = TRUE, function(this, e = NULL, ...) {
    jCall(this$.jobj, "V", "onMessage", .jcast(e$.jobj, "jms.Envelope"))
})

