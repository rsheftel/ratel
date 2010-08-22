constructor("JRange", function(jobj = NULL) {
    extend(JObject(), "JRange", .jobj = jobj)
})

method("by_Date_Date", "JRange", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    JRange(jNew("util/Range", .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date")))
})

method("hasEnd", "JRange", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasEnd")
})

method("hasStart", "JRange", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasStart")
})

method("union_by_Range", "JRange", enforceRCC = TRUE, function(this, newRange = NULL, ...) {
    JRange(jobj = jCall(this$.jobj, "Lutil/Range;", "union", .jcast(newRange$.jobj, "util.Range")))
})

method("adjacentTo_by_Range", "JRange", enforceRCC = TRUE, function(this, range = NULL, ...) {
    jCall(this$.jobj, "Z", "adjacentTo", .jcast(range$.jobj, "util.Range"))
})

method("requireNoOverlaps_by_List", "JRange", enforceRCC = TRUE, function(static, ranges = NULL, ...) {
    jCall("util/Range", "V", "requireNoOverlaps", .jcast(ranges$.jobj, "java.util.List"))
})

method("overlaps_by_Range", "JRange", enforceRCC = TRUE, function(this, range = NULL, ...) {
    jCall(this$.jobj, "Z", "overlaps", .jcast(range$.jobj, "util.Range"))
})

method("requireOrdered_by_Date_Date", "JRange", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    jCall("util/Range", "V", "requireOrdered", .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date"))
})

method("iterator", "JRange", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("contains_by_Range", "JRange", enforceRCC = TRUE, function(this, inner = NULL, ...) {
    jCall(this$.jobj, "Z", "contains", .jcast(inner$.jobj, "util.Range"))
})

method("containsInclusive_by_Date", "JRange", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "Z", "containsInclusive", .jcast(date$.jobj, "java.util.Date"))
})

method("containsEndExclusive_by_Date", "JRange", enforceRCC = TRUE, function(this, date = NULL, ...) {
    jCall(this$.jobj, "Z", "containsEndExclusive", .jcast(date$.jobj, "java.util.Date"))
})

method("range_by_Date_int", "JRange", enforceRCC = TRUE, function(static, start = NULL, daysAhead = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "range", .jcast(start$.jobj, "java.util.Date"), theInteger(daysAhead)))
})

method("onDayOf_by_String", "JRange", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "onDayOf", the(asOf)))
})

method("onDayOf_by_Date", "JRange", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "onDayOf", .jcast(asOf$.jobj, "java.util.Date")))
})

method("toString", "JRange", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("equals_by_Object", "JRange", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JRange", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("range_by_Date_Date", "JRange", enforceRCC = TRUE, function(static, startDate = NULL, endDate = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "range", .jcast(startDate$.jobj, "java.util.Date"), .jcast(endDate$.jobj, "java.util.Date")))
})

method("range_by_Date", "JRange", enforceRCC = TRUE, function(static, date = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "range", .jcast(date$.jobj, "java.util.Date")))
})

method("range_by_String", "JRange", enforceRCC = TRUE, function(static, date = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "range", the(date)))
})

method("end", "JRange", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "end"))
})

method("start", "JRange", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "start"))
})

method("stamp_by_Date", "JRange", enforceRCC = TRUE, function(static, d = NULL, ...) {
    JTimestamp(jobj = jCall("util/Range", "Ljava/sql/Timestamp;", "stamp", .jcast(d$.jobj, "java.util.Date")))
})

method("matches_by_DatetimeColumn", "JRange", enforceRCC = TRUE, function(this, c = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(c$.jobj, "db.columns.DatetimeColumn")))
})

method("unorderedRange_by_Date_Date", "JRange", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "unorderedRange", .jcast(start$.jobj, "java.util.Date"), .jcast(end$.jobj, "java.util.Date")))
})

method("unorderedRange_by_String_String", "JRange", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "unorderedRange", the(start), the(end)))
})

method("allTime", "JRange", enforceRCC = TRUE, function(static, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "allTime"))
})

method("range_by_String_String_boolean", "JRange", enforceRCC = TRUE, function(static, start = NULL, end = NULL, reorder = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "range", the(start), the(end), theLogical(reorder)))
})

method("range_by_String_String", "JRange", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    JRange(jobj = jCall("util/Range", "Lutil/Range;", "range", the(start), the(end)))
})

