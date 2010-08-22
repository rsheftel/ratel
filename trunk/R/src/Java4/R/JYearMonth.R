constructor("JYearMonth", function(jobj = NULL) {
    extend(JObject(), "JYearMonth", .jobj = jobj)
})

method("by_int_int", "JYearMonth", enforceRCC = TRUE, function(static, year = NULL, month = NULL, ...) {
    JYearMonth(jNew("util/YearMonth", theInteger(year), theInteger(month)))
})

method("by_Date", "JYearMonth", enforceRCC = TRUE, function(static, d = NULL, ...) {
    JYearMonth(jNew("util/YearMonth", .jcast(d$.jobj, "java.util.Date")))
})

method("by_String", "JYearMonth", enforceRCC = TRUE, function(static, yyyyMm = NULL, ...) {
    JYearMonth(jNew("util/YearMonth", the(yyyyMm)))
})

method("in_by_NcharColumn", "JYearMonth", enforceRCC = TRUE, function(this, c = NULL, ...) {
    JCell(jobj = jCall(this$.jobj, "Ldb/Cell;", "in", .jcast(c$.jobj, "db.columns.NcharColumn")))
})

method("matches_by_NcharColumn", "JYearMonth", enforceRCC = TRUE, function(this, c = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(c$.jobj, "db.columns.NcharColumn")))
})

method("toString", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("string", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "string")
})

method("equals_by_Object", "JYearMonth", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("end", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "end"))
})

method("first", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "first"))
})

method("month", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "month")
})

method("year", "JYearMonth", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "year")
})

