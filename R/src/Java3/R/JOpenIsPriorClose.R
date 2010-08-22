constructor("JOpenIsPriorClose", function(jobj = NULL) {
    extend(JObject(), "JOpenIsPriorClose", .jobj = jobj)
})

method("by", "JOpenIsPriorClose", enforceRCC = TRUE, function(static, ...) {
    JOpenIsPriorClose(jNew("systemdb/data/bars/OpenIsPriorClose"))
})

method("name", "JOpenIsPriorClose", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("convert_by_HistoricalDailyData_List", "JOpenIsPriorClose", enforceRCC = TRUE, function(this, data = NULL, protos = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "convert", .jcast(data$.jobj, "systemdb.data.HistoricalDailyData"), .jcast(protos$.jobj, "java.util.List")))
})

