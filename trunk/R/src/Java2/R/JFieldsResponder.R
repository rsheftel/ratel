constructor("JFieldsResponder", function(jobj = NULL) {
    extend(JObject(), "JFieldsResponder", .jobj = jobj)
})

method("by", "JFieldsResponder", enforceRCC = TRUE, function(static, ...) {
    JFieldsResponder(jNew("systemdb/data/FieldsResponder"))
})

method("reply_by_Fields", "JFieldsResponder", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JFields(jobj = jCall(this$.jobj, "Lsystemdb/data/Fields;", "reply", .jcast(j_arg0$.jobj, "systemdb.data.Fields")))
})

method("responder_by_FieldsResponderListener", "JFieldsResponder", enforceRCC = TRUE, function(static, listener = NULL, ...) {
    JFieldsResponder(jobj = jCall("systemdb/data/FieldsResponder", "Lsystemdb/data/FieldsResponder;", "responder", .jcast(listener$.jobj, "systemdb.data.FieldsResponderListener")))
})

method("reply_by_String", "JFieldsResponder", enforceRCC = TRUE, function(this, message = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "reply", the(message))
})

