constructor("JTimes", function(jobj = NULL) {
    extend(JObject(), "JTimes", .jobj = jobj)
})

method("by", "JTimes", enforceRCC = TRUE, function(static, ...) {
    JTimes(jNew("util/Times"))
})

method("perReallyMilliSince_by_long_int", "JTimes", enforceRCC = TRUE, function(static, start = NULL, count = NULL, ...) {
    jCall("util/Times", "D", "perReallyMilliSince", theLong(start), theInteger(count))
})

method("sleep_by_long", "JTimes", enforceRCC = TRUE, function(static, millis = NULL, ...) {
    jCall("util/Times", "V", "sleep", theLong(millis))
})

method("nowMillis", "JTimes", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Times", "J", "nowMillis")
})

method("sleepSeconds_by_long", "JTimes", enforceRCC = TRUE, function(static, seconds = NULL, ...) {
    jCall("util/Times", "V", "sleepSeconds", theLong(seconds))
})

method("reallySecondsSince_by_long", "JTimes", enforceRCC = TRUE, function(static, start = NULL, ...) {
    jCall("util/Times", "D", "reallySecondsSince", theLong(start))
})

method("reallyMillisSince_by_Date", "JTimes", enforceRCC = TRUE, function(static, start = NULL, ...) {
    jCall("util/Times", "J", "reallyMillisSince", .jcast(start$.jobj, "java.util.Date"))
})

method("reallyMillisSince_by_long", "JTimes", enforceRCC = TRUE, function(static, start = NULL, ...) {
    jCall("util/Times", "J", "reallyMillisSince", theLong(start))
})

method("millisSince_by_Date", "JTimes", enforceRCC = TRUE, function(static, start = NULL, ...) {
    jCall("util/Times", "J", "millisSince", .jcast(start$.jobj, "java.util.Date"))
})

method("millisSince_by_long", "JTimes", enforceRCC = TRUE, function(static, start = NULL, ...) {
    jCall("util/Times", "J", "millisSince", theLong(start))
})

