constructor("JObservationListener", function(jobj = NULL) {
    extend(JObject(), "JObservationListener", .jobj = jobj)
})

method("onUpdate_by_Date_double", "JObservationListener", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "V", "onUpdate", .jcast(j_arg0$.jobj, "java.util.Date"), theNumeric(j_arg1))
})

