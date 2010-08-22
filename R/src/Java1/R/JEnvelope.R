constructor("JEnvelope", function(jobj = NULL) {
    extend(JObject(), "JEnvelope", .jobj = jobj)
})

method("by_Message_String", "JEnvelope", enforceRCC = TRUE, function(static, m = NULL, broker = NULL, ...) {
    JEnvelope(jNew("jms/Envelope", .jcast(m$.jobj, "javax.jms.Message"), the(broker)))
})

method("correlationId", "JEnvelope", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "correlationId")
})

method("giveTo_by_Channel_MessageReceiver", "JEnvelope", enforceRCC = TRUE, function(this, from = NULL, receiver = NULL, ...) {
    jCall(this$.jobj, "V", "giveTo", .jcast(from$.jobj, "jms.Channel"), .jcast(receiver$.jobj, "jms.MessageReceiver"))
})

method("sendBack_by_String_Map", "JEnvelope", enforceRCC = TRUE, function(this, reply = NULL, properties = NULL, ...) {
    jCall(this$.jobj, "V", "sendBack", the(reply), .jcast(properties$.jobj, "java.util.Map"))
})

method("heartbeatFrequencyMillis", "JEnvelope", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "heartbeatFrequencyMillis")
})

method("returnAddress", "JEnvelope", enforceRCC = TRUE, function(this, ...) {
    JChannel(jobj = jCall(this$.jobj, "Ljms/Channel;", "returnAddress"))
})

method("text", "JEnvelope", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "text")
})

