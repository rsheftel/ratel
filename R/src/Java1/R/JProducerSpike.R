constructor("JProducerSpike", function(jobj = NULL) {
    extend(JObject(), "JProducerSpike", .jobj = jobj)
})

method("by", "JProducerSpike", enforceRCC = TRUE, function(static, ...) {
    JProducerSpike(jNew("jms/ProducerSpike"))
})

method("onException_by_JMSException", "JProducerSpike", enforceRCC = TRUE, function(this, e = NULL, ...) {
    jCall(this$.jobj, "V", "onException", .jcast(e$.jobj, "javax.jms.JMSException"))
})

method("main_by_StringArray", "JProducerSpike", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("jms/ProducerSpike", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

