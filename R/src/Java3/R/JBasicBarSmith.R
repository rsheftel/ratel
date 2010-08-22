constructor("JBasicBarSmith", function(jobj = NULL) {
    extend(JObject(), "JBasicBarSmith", .jobj = jobj)
})

method("by", "JBasicBarSmith", enforceRCC = TRUE, function(static, ...) {
    JBasicBarSmith(jNew("systemdb/data/bars/BasicBarSmith"))
})

method("calculator_by_String", "JBasicBarSmith", enforceRCC = TRUE, function(static, calculateName = NULL, ...) {
    JBarSmith(jobj = jCall("systemdb/data/bars/BasicBarSmith", "Lsystemdb/data/bars/BarSmith;", "calculator", the(calculateName)))
})

method("protoBars_by_ObservationsMap_Object_Object_Object_Object_Object_Object", "JBasicBarSmith", enforceRCC = TRUE, function(static, group = NULL, open = NULL, high = NULL, low = NULL, close = NULL, volume = NULL, openInterest = NULL, ...) {
    JList(jobj = jCall("systemdb/data/bars/BasicBarSmith", "Ljava/util/List;", "protoBars", .jcast(group$.jobj, "tsdb.ObservationsMap"), .jcast(open$.jobj, "java.lang.Object"), .jcast(high$.jobj, "java.lang.Object"), .jcast(low$.jobj, "java.lang.Object"), .jcast(close$.jobj, "java.lang.Object"), .jcast(volume$.jobj, "java.lang.Object"), .jcast(openInterest$.jobj, "java.lang.Object")))
})

method("name", "JBasicBarSmith", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("convert_by_HistoricalDailyData_List", "JBasicBarSmith", enforceRCC = TRUE, function(this, data = NULL, observations = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "convert", .jcast(data$.jobj, "systemdb.data.HistoricalDailyData"), .jcast(observations$.jobj, "java.util.List")))
})

