constructor("JFieldsReceiver", function(jobj = NULL) {
    extend(JObject(), "JFieldsReceiver", .jobj = jobj)
})

method("by", "JFieldsReceiver", enforceRCC = TRUE, function(static, ...) {
    JFieldsReceiver(jNew("systemdb/data/FieldsReceiver"))
})

method("waitForMessage_by_long", "JFieldsReceiver", enforceRCC = TRUE, function(this, timeoutMillis = NULL, ...) {
    jCall(this$.jobj, "V", "waitForMessage", theLong(timeoutMillis))
})

method("clearMessageFlag", "JFieldsReceiver", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clearMessageFlag")
})

method("receiver_by_FieldsListener", "JFieldsReceiver", enforceRCC = TRUE, function(static, listener = NULL, ...) {
    JFieldsReceiver(jobj = jCall("systemdb/data/FieldsReceiver", "Lsystemdb/data/FieldsReceiver;", "receiver", .jcast(listener$.jobj, "systemdb.data.FieldsListener")))
})

method("onMessage_by_Fields", "JFieldsReceiver", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "onMessage", .jcast(j_arg0$.jobj, "systemdb.data.Fields"))
})

method("onMessage_by_Envelope", "JFieldsReceiver", enforceRCC = TRUE, function(this, envelope = NULL, ...) {
    jCall(this$.jobj, "V", "onMessage", .jcast(envelope$.jobj, "jms.Envelope"))
})

method("onHeartBeat_by_Envelope", "JFieldsReceiver", enforceRCC = TRUE, function(this, envelope = NULL, ...) {
    jCall(this$.jobj, "V", "onHeartBeat", .jcast(envelope$.jobj, "jms.Envelope"))
})

