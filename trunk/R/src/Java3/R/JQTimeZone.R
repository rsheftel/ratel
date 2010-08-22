constructor("JQTimeZone", function(jobj = NULL) {
    extend(JObject(), "JQTimeZone", .jobj = jobj)
})

method("by_String", "JQTimeZone", enforceRCC = TRUE, function(static, timeZoneId = NULL, ...) {
    JQTimeZone(jNew("util/QTimeZone", the(timeZoneId)))
})

method("toLocalTime_by_Date", "JQTimeZone", enforceRCC = TRUE, function(this, inMyTimeZone = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "toLocalTime", .jcast(inMyTimeZone$.jobj, "java.util.Date")))
})

method("GMT", "JQTimeZone", enforceRCC = FALSE, function(static, ...) {
    lazy(JQTimeZone$...GMT, JQTimeZone(jobj = jField("util/QTimeZone", "Lutil/QTimeZone;", "GMT")), log = FALSE)
})

