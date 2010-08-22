constructor("JMessageReceiver", function(jobj = NULL) {
    extend(JObject(), "JMessageReceiver", .jobj = jobj)
})

method("onHeartBeat_by_Envelope", "JMessageReceiver", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "onHeartBeat", .jcast(j_arg0$.jobj, "jms.Envelope"))
})

method("onError_by_Envelope", "JMessageReceiver", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "onError", .jcast(j_arg0$.jobj, "jms.Envelope"))
})

method("onMessage_by_Envelope", "JMessageReceiver", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "onMessage", .jcast(j_arg0$.jobj, "jms.Envelope"))
})

