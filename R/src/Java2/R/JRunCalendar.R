constructor("JRunCalendar", function(jobj = NULL) {
    extend(JObject(), "JRunCalendar", .jobj = jobj)
})

method("isWeekend_by_Date", "JRunCalendar", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    jCall("util/RunCalendar", "Z", "isWeekend", .jcast(asOf$.jobj, "java.util.Date"))
})

method("isBeforeTime_by_Date_String", "JRunCalendar", enforceRCC = TRUE, function(this, asOf = NULL, time = NULL, ...) {
    jCall(this$.jobj, "Z", "isBeforeTime", .jcast(asOf$.jobj, "java.util.Date"), the(time))
})

method("asOf_by_Date", "JRunCalendar", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "asOf", .jcast(date$.jobj, "java.util.Date")))
})

method("dbName", "JRunCalendar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "dbName")
})

method("nextDay_by_Date", "JRunCalendar", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "nextDay", .jcast(j_arg0$.jobj, "java.util.Date")))
})

method("priorDay_by_Date", "JRunCalendar", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "priorDay", .jcast(j_arg0$.jobj, "java.util.Date")))
})

method("isValid_by_Date", "JRunCalendar", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "isValid", .jcast(j_arg0$.jobj, "java.util.Date"))
})

method("offset", "JRunCalendar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "offset")
})

method("name", "JRunCalendar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("matches_by_String", "JRunCalendar", enforceRCC = TRUE, function(this, center = NULL, ...) {
    jCall(this$.jobj, "Z", "matches", the(center))
})

method("cell_by_NvarcharColumn", "JRunCalendar", enforceRCC = TRUE, function(this, column = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "cell", .jcast(column$.jobj, "db.columns.NvarcharColumn")))
})

method("toString", "JRunCalendar", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("from_by_String", "JRunCalendar", enforceRCC = TRUE, function(static, s = NULL, ...) {
    JRunCalendar(jobj = jCall("util/RunCalendar", "Lutil/RunCalendar;", "from", the(s)))
})

method("NYB_P1", "JRunCalendar", enforceRCC = FALSE, function(static, ...) {
    lazy(JRunCalendar$...NYB_P1, JRunCalendar(jobj = jField("util/RunCalendar", "Lutil/RunCalendar;", "NYB_P1")), log = FALSE)
})

method("SEVENDAYS", "JRunCalendar", enforceRCC = FALSE, function(static, ...) {
    lazy(JRunCalendar$...SEVENDAYS, JRunCalendar(jobj = jField("util/RunCalendar", "Lutil/RunCalendar;", "SEVENDAYS")), log = FALSE)
})

method("WEEKDAYS", "JRunCalendar", enforceRCC = FALSE, function(static, ...) {
    lazy(JRunCalendar$...WEEKDAYS, JRunCalendar(jobj = jField("util/RunCalendar", "Lutil/RunCalendar;", "WEEKDAYS")), log = FALSE)
})

method("LNB", "JRunCalendar", enforceRCC = FALSE, function(static, ...) {
    lazy(JRunCalendar$...LNB, JRunCalendar(jobj = jField("util/RunCalendar", "Lutil/RunCalendar;", "LNB")), log = FALSE)
})

method("NYB", "JRunCalendar", enforceRCC = FALSE, function(static, ...) {
    lazy(JRunCalendar$...NYB, JRunCalendar(jobj = jField("util/RunCalendar", "Lutil/RunCalendar;", "NYB")), log = FALSE)
})

method("WEEKEND", "JRunCalendar", enforceRCC = FALSE, function(static, ...) {
    lazy(JRunCalendar$...WEEKEND, JList(jobj = jField("util/RunCalendar", "Ljava/util/List;", "WEEKEND")), log = FALSE)
})

