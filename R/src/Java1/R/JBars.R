constructor("JBars", function(jobj = NULL) {
    extend(JObject(), "JBars", .jobj = jobj)
})

method("by", "JBars", enforceRCC = TRUE, function(static, ...) {
    JBars(jNew("systemdb/data/bars/Bars"))
})

method("rBars_by_List", "JBars", enforceRCC = TRUE, function(static, bars = NULL, ...) {
    JRBarData(jobj = jCall("systemdb/data/bars/Bars", "Lsystemdb/data/RBarData;", "rBars", .jcast(bars$.jobj, "java.util.List")))
})

method("main_by_StringArray", "JBars", enforceRCC = TRUE, function(static, ss = NULL, ...) {
    jCall("systemdb/data/bars/Bars", "V", "main", jArray(ss, "[Ljava/lang/String;"))
})

