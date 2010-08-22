constructor("JTbaTable", function(jobj = NULL) {
    extend(JObject(), "JTbaTable", .jobj = jobj)
})

method("by", "JTbaTable", enforceRCC = TRUE, function(static, ...) {
    JTbaTable(jNew("mortgage/TbaTable"))
})

method("frontNotificationDate_by_String_Date", "JTbaTable", enforceRCC = TRUE, function(this, program = NULL, date = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "frontNotificationDate", the(program), .jcast(date$.jobj, "java.util.Date")))
})

method("frontSettle_by_String_Date", "JTbaTable", enforceRCC = TRUE, function(this, program = NULL, date = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "frontSettle", the(program), .jcast(date$.jobj, "java.util.Date")))
})

method("tba_by_String", "JTbaTable", enforceRCC = TRUE, function(this, program = NULL, ...) {
    JTba(jobj = jCall(this$.jobj, "Lmortgage/TbaTable/Tba;", "tba", the(program)))
})

method("insert_by_String_double_double", "JTbaTable", enforceRCC = TRUE, function(this, program = NULL, low = NULL, high = NULL, ...) {
    JTba(jobj = jCall(this$.jobj, "Lmortgage/TbaTable/Tba;", "insert", the(program), theNumeric(low), theNumeric(high)))
})

method("tbas", "JTbaTable", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "tbas"))
})

method("TBA", "JTbaTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JTbaTable$...TBA, JTbaTable(jobj = jField("mortgage/TbaTable", "Lmortgage/TbaTable;", "TBA")), log = FALSE)
})

