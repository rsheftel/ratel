constructor("JFieldsListener", function(jobj = NULL) {
    extend(JObject(), "JFieldsListener", .jobj = jobj)
})

method("onMessage_by_Fields", "JFieldsListener", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "onMessage", .jcast(j_arg0$.jobj, "systemdb.data.Fields"))
})

