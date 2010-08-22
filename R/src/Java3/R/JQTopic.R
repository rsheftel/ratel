constructor("JQTopic", function(jobj = NULL) {
    extend(JObject(), "JQTopic", .jobj = jobj)
})

method("by_String_String_boolean", "JQTopic", enforceRCC = TRUE, function(static, name = NULL, broker = NULL, doRetroactiveConsumer = NULL, ...) {
    JQTopic(jNew("jms/QTopic", the(name), the(broker), theLogical(doRetroactiveConsumer)))
})

method("by_String_String", "JQTopic", enforceRCC = TRUE, function(static, name = NULL, broker = NULL, ...) {
    JQTopic(jNew("jms/QTopic", the(name), the(broker)))
})

method("by_Topic", "JQTopic", enforceRCC = TRUE, function(static, topic = NULL, ...) {
    JQTopic(jNew("jms/QTopic", .jcast(topic$.jobj, "javax.jms.Topic")))
})

method("by_String_boolean", "JQTopic", enforceRCC = TRUE, function(static, name = NULL, doRetroactiveConsumer = NULL, ...) {
    JQTopic(jNew("jms/QTopic", the(name), theLogical(doRetroactiveConsumer)))
})

method("by_String", "JQTopic", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JQTopic(jNew("jms/QTopic", the(name)))
})

method("send_by_Row", "JQTopic", enforceRCC = TRUE, function(this, data = NULL, ...) {
    jCall(this$.jobj, "V", "send", .jcast(data$.jobj, "db.Row"))
})

method("useRetroactiveConsumer", "JQTopic", enforceRCC = FALSE, function(static, ...) {
    lazy(JQTopic$...useRetroactiveConsumer, jField("jms/QTopic", "Z", "useRetroactiveConsumer"), log = FALSE)
})

