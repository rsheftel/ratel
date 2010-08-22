constructor("JMonthCode", function(jobj = NULL) {
    extend(JObject(), "JMonthCode", .jobj = jobj)
})

method("yearMonth_by_Date", "JMonthCode", enforceRCC = TRUE, function(this, d = NULL, ...) {
    JYearMonth(jobj = jCall(this$.jobj, "Lutil/YearMonth;", "yearMonth", .jcast(d$.jobj, "java.util.Date")))
})

method("isQuarter_by_YearMonth", "JMonthCode", enforceRCC = TRUE, function(static, ym = NULL, ...) {
    jCall("futures/MonthCode", "Z", "isQuarter", .jcast(ym$.jobj, "util.YearMonth"))
})

method("toQuarter", "JMonthCode", enforceRCC = TRUE, function(this, ...) {
    JMonthCode(jobj = jCall(this$.jobj, "Lfutures/MonthCode;", "toQuarter"))
})

method("longName", "JMonthCode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "longName")
})

method("letter", "JMonthCode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "letter")
})

method("fromNumber_by_int", "JMonthCode", enforceRCC = TRUE, function(static, number = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "fromNumber", theInteger(number)))
})

method("fromNumber_by_String", "JMonthCode", enforceRCC = TRUE, function(static, twoDigit = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "fromNumber", the(twoDigit)))
})

method("number", "JMonthCode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "number")
})

method("numberString", "JMonthCode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "numberString")
})

method("fromChar_by_String", "JMonthCode", enforceRCC = TRUE, function(static, s = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "fromChar", the(s)))
})

method("fromChar_by_char", "JMonthCode", enforceRCC = TRUE, function(static, charAt = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "fromChar", fail("char parameters not handled by R/Java convertPrimitive!")))
})

method("month_by_Date", "JMonthCode", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "month", .jcast(asOf$.jobj, "java.util.Date")))
})

method("quarter_by_Date", "JMonthCode", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "quarter", .jcast(asOf$.jobj, "java.util.Date")))
})

method("toString", "JMonthCode", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("valueOf_by_String", "JMonthCode", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JMonthCode(jobj = jCall("futures/MonthCode", "Lfutures/MonthCode;", "valueOf", the(name)))
})

method("values", "JMonthCode", enforceRCC = TRUE, function(static, ...) {
    lapply(jCall("futures/MonthCode", "[Lfutures/MonthCode;", "values"), JMonthCode)
})

method("Z", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...Z, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "Z")), log = FALSE)
})

method("X", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...X, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "X")), log = FALSE)
})

method("V", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...V, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "V")), log = FALSE)
})

method("U", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...U, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "U")), log = FALSE)
})

method("Q", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...Q, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "Q")), log = FALSE)
})

method("N", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...N, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "N")), log = FALSE)
})

method("M", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...M, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "M")), log = FALSE)
})

method("K", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...K, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "K")), log = FALSE)
})

method("J", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...J, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "J")), log = FALSE)
})

method("H", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...H, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "H")), log = FALSE)
})

method("G", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...G, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "G")), log = FALSE)
})

method("F", "JMonthCode", enforceRCC = FALSE, function(static, ...) {
    lazy(JMonthCode$...F, JMonthCode(jobj = jField("futures/MonthCode", "Lfutures/MonthCode;", "F")), log = FALSE)
})

