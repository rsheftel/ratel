constructor("JBarSmith", function(jobj = NULL) {
    extend(JObject(), "JBarSmith", .jobj = jobj)
})

method("name", "JBarSmith", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("convert_by_HistoricalDailyData_List", "JBarSmith", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "convert", .jcast(j_arg0$.jobj, "systemdb.data.HistoricalDailyData"), .jcast(j_arg1$.jobj, "java.util.List")))
})

