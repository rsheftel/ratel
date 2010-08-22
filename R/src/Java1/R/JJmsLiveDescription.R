constructor("JJmsLiveDescription", function(jobj = NULL) {
    extend(JObject(), "JJmsLiveDescription", .jobj = jobj)
})

method("by_String_String_String_String", "JJmsLiveDescription", enforceRCC = TRUE, function(static, topic = NULL, source = NULL, dateField = NULL, valueField = NULL, ...) {
    JJmsLiveDescription(jNew("systemdb/metadata/JmsLiveDescription", the(topic), the(source), the(dateField), the(valueField)))
})

method("by_String_String_String", "JJmsLiveDescription", enforceRCC = TRUE, function(static, topicName = NULL, source = NULL, template = NULL, ...) {
    JJmsLiveDescription(jNew("systemdb/metadata/JmsLiveDescription", the(topicName), the(source), the(template)))
})

method("publish_by_double_Date", "JJmsLiveDescription", enforceRCC = TRUE, function(this, value = NULL, date = NULL, ...) {
    jCall(this$.jobj, "V", "publish", theNumeric(value), .jcast(date$.jobj, "java.util.Date"))
})

method("equals_by_Object", "JJmsLiveDescription", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JJmsLiveDescription", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JJmsLiveDescription", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("subscribe_by_TickListener", "JJmsLiveDescription", enforceRCC = TRUE, function(this, listener = NULL, ...) {
    jCall(this$.jobj, "V", "subscribe", .jcast(listener$.jobj, "systemdb.data.TickListener"))
})

method("subscribe_by_ObservationListener", "JJmsLiveDescription", enforceRCC = TRUE, function(this, listener = NULL, ...) {
    jCall(this$.jobj, "V", "subscribe", .jcast(listener$.jobj, "systemdb.data.ObservationListener"))
})

method("publish_by_Tick", "JJmsLiveDescription", enforceRCC = TRUE, function(this, tick = NULL, ...) {
    jCall(this$.jobj, "V", "publish", .jcast(tick$.jobj, "systemdb.data.Tick"))
})

method("topic", "JJmsLiveDescription", enforceRCC = TRUE, function(this, ...) {
    JQTopic(jobj = jCall(this$.jobj, "Ljms/QTopic;", "topic"))
})

method("valueField", "JJmsLiveDescription", enforceRCC = FALSE, function(this, ...) {
    lazy(JJmsLiveDescription$...valueField, jField(this$.jobj, "Ljava/lang/String;", "valueField"), log = FALSE)
})

method("dateField", "JJmsLiveDescription", enforceRCC = FALSE, function(this, ...) {
    lazy(JJmsLiveDescription$...dateField, jField(this$.jobj, "Ljava/lang/String;", "dateField"), log = FALSE)
})

method("template", "JJmsLiveDescription", enforceRCC = FALSE, function(this, ...) {
    lazy(JJmsLiveDescription$...template, jField(this$.jobj, "Ljava/lang/String;", "template"), log = FALSE)
})

method("source", "JJmsLiveDescription", enforceRCC = FALSE, function(this, ...) {
    lazy(JJmsLiveDescription$...source, jField(this$.jobj, "Ljava/lang/String;", "source"), log = FALSE)
})

method("topicName", "JJmsLiveDescription", enforceRCC = FALSE, function(this, ...) {
    lazy(JJmsLiveDescription$...topicName, jField(this$.jobj, "Ljava/lang/String;", "topicName"), log = FALSE)
})

