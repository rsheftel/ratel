constructor("JTickListener", function(jobj = NULL) {
    extend(JObject(), "JTickListener", .jobj = jobj)
})

method("onTick_by_Tick", "JTickListener", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "onTick", .jcast(j_arg0$.jobj, "systemdb.data.Tick"))
})

