constructor("JInterval", function(jobj = NULL) {
    extend(JObject(), "JInterval", .jobj = jobj)
})

method("range_by_Date", "JInterval", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "range", .jcast(date$.jobj, "java.util.Date")))
})

method("range_by_String", "JInterval", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "range", the(date)))
})

method("name", "JInterval", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("aggregate_by_List", "JInterval", enforceRCC = TRUE, function(this, bars = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "aggregate", .jcast(bars$.jobj, "java.util.List")))
})

method("minutes", "JInterval", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "minutes")
})

method("equals_by_Object", "JInterval", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JInterval", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("isDaily", "JInterval", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isDaily")
})

method("isOnBoundary_by_Date", "JInterval", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "Z", "isOnBoundary", .jcast(date$.jobj, "java.util.Date"))
})

method("nextBoundary_by_Date", "JInterval", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "nextBoundary", .jcast(date$.jobj, "java.util.Date")))
})

method("nextBoundary", "JInterval", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "nextBoundary"))
})

method("advance_by_Date", "JInterval", enforceRCC = TRUE, function(this, date = NULL, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "advance", .jcast(date$.jobj, "java.util.Date")))
})

method("lookup_by_String", "JInterval", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JInterval(jobj = jCall("systemdb/data/Interval", "Lsystemdb/data/Interval;", "lookup", the(value)))
})

method("millis", "JInterval", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "millis")
})

method("seconds", "JInterval", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "seconds")
})

method("DAILY", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...DAILY, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "DAILY")), log = FALSE)
})

method("HOURLY", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...HOURLY, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "HOURLY")), log = FALSE)
})

method("HALF_HOURLY", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...HALF_HOURLY, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "HALF_HOURLY")), log = FALSE)
})

method("FIFTEEN_MINUTES", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...FIFTEEN_MINUTES, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "FIFTEEN_MINUTES")), log = FALSE)
})

method("TEN_MINUTES", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...TEN_MINUTES, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "TEN_MINUTES")), log = FALSE)
})

method("FIVE_MINUTES", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...FIVE_MINUTES, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "FIVE_MINUTES")), log = FALSE)
})

method("MINUTE", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...MINUTE, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "MINUTE")), log = FALSE)
})

method("SECOND", "JInterval", enforceRCC = FALSE, function(static, ...) {
    lazy(JInterval$...SECOND, JInterval(jobj = jField("systemdb/data/Interval", "Lsystemdb/data/Interval;", "SECOND")), log = FALSE)
})

