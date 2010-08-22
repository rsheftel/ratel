constructor("JIntradaySource", function(jobj = NULL) {
    extend(JObject(), "JIntradaySource", .jobj = jobj)
})

method("bars_by_Range_Interval", "JIntradaySource", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "bars", .jcast(j_arg0$.jobj, "util.Range"), .jcast(j_arg1$.jobj, "systemdb.data.Interval")))
})

