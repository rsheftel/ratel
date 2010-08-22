constructor("JValidMessageReceiver", function(jobj = NULL) {
    extend(JObject(), "JValidMessageReceiver", .jobj = jobj)
})

method("by", "JValidMessageReceiver", enforceRCC = TRUE, function(static, ...) {
    JValidMessageReceiver(jNew("jms/ValidMessageReceiver"))
})

method("onError_by_Envelope", "JValidMessageReceiver", enforceRCC = TRUE, function(this, envelope = NULL, ...) {
    jCall(this$.jobj, "V", "onError", .jcast(envelope$.jobj, "jms.Envelope"))
})

