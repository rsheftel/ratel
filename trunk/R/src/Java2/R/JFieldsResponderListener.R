constructor("JFieldsResponderListener", function(jobj = NULL) {
    extend(JObject(), "JFieldsResponderListener", .jobj = jobj)
})

method("reply_by_Fields", "JFieldsResponderListener", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JFields(jobj = jCall(this$.jobj, "Lsystemdb/data/Fields;", "reply", .jcast(j_arg0$.jobj, "systemdb.data.Fields")))
})

