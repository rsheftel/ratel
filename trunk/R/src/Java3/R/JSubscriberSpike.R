constructor("JSubscriberSpike", function(jobj = NULL) {
    extend(JObject(), "JSubscriberSpike", .jobj = jobj)
})

method("by", "JSubscriberSpike", enforceRCC = TRUE, function(static, ...) {
    JSubscriberSpike(jNew("jms/SubscriberSpike"))
})

method("onMessage_by_Message", "JSubscriberSpike", enforceRCC = TRUE, function(this, mess = NULL, ...) {
    jCall(this$.jobj, "V", "onMessage", .jcast(mess$.jobj, "javax.jms.Message"))
})

method("onException_by_JMSException", "JSubscriberSpike", enforceRCC = TRUE, function(this, e = NULL, ...) {
    jCall(this$.jobj, "V", "onException", .jcast(e$.jobj, "javax.jms.JMSException"))
})

method("main_by_StringArray", "JSubscriberSpike", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("jms/SubscriberSpike", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

