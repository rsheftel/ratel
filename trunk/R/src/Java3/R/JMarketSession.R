constructor("JMarketSession", function(jobj = NULL) {
    extend(JObject(), "JMarketSession", .jobj = jobj)
})

method("by_String_String_int_QTimeZone", "JMarketSession", enforceRCC = TRUE, function(static, open = NULL, close = NULL, closeOffset = NULL, timeZone = NULL, ...) {
    JMarketSession(jNew("systemdb/metadata/MarketSession", the(open), the(close), theInteger(closeOffset), .jcast(timeZone$.jobj, "util.QTimeZone")))
})

method("isOpen", "JMarketSession", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isOpen")
})

method("closeOffsetSeconds", "JMarketSession", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "closeOffsetSeconds")
})

method("close", "JMarketSession", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "close"))
})

method("open", "JMarketSession", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "open"))
})

method("processCloseAt", "JMarketSession", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "processCloseAt"))
})

method("closeOn_by_Date", "JMarketSession", enforceRCC = TRUE, function(this, day = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "closeOn", .jcast(day$.jobj, "java.util.Date")))
})

method("processCloseAt_by_Date", "JMarketSession", enforceRCC = TRUE, function(this, day = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "processCloseAt", .jcast(day$.jobj, "java.util.Date")))
})

method("openOn_by_Date", "JMarketSession", enforceRCC = TRUE, function(this, day = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "openOn", .jcast(day$.jobj, "java.util.Date")))
})

