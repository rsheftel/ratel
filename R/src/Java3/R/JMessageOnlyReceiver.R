constructor("JMessageOnlyReceiver", function(jobj = NULL) {
    extend(JObject(), "JMessageOnlyReceiver", .jobj = jobj)
})

method("by", "JMessageOnlyReceiver", enforceRCC = TRUE, function(static, ...) {
    JMessageOnlyReceiver(jNew("jms/MessageOnlyReceiver"))
})

method("onHeartBeat_by_Envelope", "JMessageOnlyReceiver", enforceRCC = TRUE, function(this, envelope = NULL, ...) {
    jCall(this$.jobj, "V", "onHeartBeat", .jcast(envelope$.jobj, "jms.Envelope"))
})

