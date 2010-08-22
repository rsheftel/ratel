constructor("JExpiry", function(jobj = NULL) {
    extend(JObject(), "JExpiry", .jobj = jobj)
})

method("by", "JExpiry", enforceRCC = TRUE, function(static, ...) {
    JExpiry(jNew("futures/Expiry"))
})

method("lookup_by_String_Option", "JExpiry", enforceRCC = TRUE, function(static, expiry = NULL, option = NULL, ...) {
    JExpiry(jobj = jCall("futures/Expiry", "Lfutures/Expiry;", "lookup", the(expiry), .jcast(option$.jobj, "futures.Option")))
})

method("addTo_by_AttributeValues_YearMonth", "JExpiry", enforceRCC = TRUE, function(this, attributes = NULL, ym = NULL, ...) {
    jCall(this$.jobj, "V", "addTo", .jcast(attributes$.jobj, "tsdb.AttributeValues"), .jcast(ym$.jobj, "util.YearMonth"))
})

method("expiration_by_YearMonth", "JExpiry", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "expiration", .jcast(j_arg0$.jobj, "util.YearMonth")))
})

method("isExpired_by_YearMonth_Date", "JExpiry", enforceRCC = TRUE, function(this, ym = NULL, d = NULL, ...) {
    jCall(this$.jobj, "Z", "isExpired", .jcast(ym$.jobj, "util.YearMonth"), .jcast(d$.jobj, "java.util.Date"))
})

method("FRIDAY_BEFORE_THIRD_WED", "JExpiry", enforceRCC = FALSE, function(static, ...) {
    lazy(JExpiry$...FRIDAY_BEFORE_THIRD_WED, jField("futures/Expiry", "Ljava/lang/String;", "FRIDAY_BEFORE_THIRD_WED"), log = FALSE)
})

method("THIRD_FRIDAY_NYB_MODIFIED_FOLLOWING", "JExpiry", enforceRCC = FALSE, function(static, ...) {
    lazy(JExpiry$...THIRD_FRIDAY_NYB_MODIFIED_FOLLOWING, jField("futures/Expiry", "Ljava/lang/String;", "THIRD_FRIDAY_NYB_MODIFIED_FOLLOWING"), log = FALSE)
})

method("THIRD_WED_LESS_TWO", "JExpiry", enforceRCC = FALSE, function(static, ...) {
    lazy(JExpiry$...THIRD_WED_LESS_TWO, jField("futures/Expiry", "Ljava/lang/String;", "THIRD_WED_LESS_TWO"), log = FALSE)
})

