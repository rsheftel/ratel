constructor("JLiveDataSource", function(jobj = NULL) {
    extend(JObject(), "JLiveDataSource", .jobj = jobj)
})

method("subscribe_by_TickListener", "JLiveDataSource", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "subscribe", .jcast(j_arg0$.jobj, "systemdb.data.TickListener"))
})

method("subscribe_by_ObservationListener", "JLiveDataSource", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "subscribe", .jcast(j_arg0$.jobj, "systemdb.data.ObservationListener"))
})

