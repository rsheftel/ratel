constructor("JDates", function(jobj = NULL) {
    extend(JObject(), "JDates", .jobj = jobj)
})

method("by", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDates(jNew("util/Dates"))
})

method("secondsBetween_by_Date_Date", "JDates", enforceRCC = TRUE, function(static, early = NULL, late = NULL, ...) {
    jCall("util/Dates", "I", "secondsBetween", .jcast(early$.jobj, "java.util.Date"), .jcast(late$.jobj, "java.util.Date"))
})

method("millisAgo_by_long_Date", "JDates", enforceRCC = TRUE, function(static, millis = NULL, date = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "millisAgo", theLong(millis), .jcast(date$.jobj, "java.util.Date")))
})

method("millisAhead_by_long_Date", "JDates", enforceRCC = TRUE, function(static, millis = NULL, date = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "millisAhead", theLong(millis), .jcast(date$.jobj, "java.util.Date")))
})

method("xmlTimestamp_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "xmlTimestamp", .jcast(d$.jobj, "java.util.Date"))
})

method("monthIndex_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Dates", "I", "monthIndex", .jcast(d$.jobj, "java.util.Date"))
})

method("monthNumber_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Dates", "I", "monthNumber", .jcast(d$.jobj, "java.util.Date"))
})

method("dateRange_by_String_String", "JDates", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    JRange(jobj = jCall("util/Dates", "Lutil/Range;", "dateRange", the(start), the(end)))
})

method("dateMaybe_by_String", "JDates", enforceRCC = TRUE, function(static, s = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "dateMaybe", the(s)))
})

method("midnightGMT", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "midnightGMT"))
})

method("midnight", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "midnight"))
})

method("dayOfWeek_by_Date", "JDates", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    jCall("util/Dates", "I", "dayOfWeek", .jcast(asOf$.jobj, "java.util.Date"))
})

method("hasTodayValue_by_Date", "JDates", enforceRCC = TRUE, function(static, notified = NULL, ...) {
    jCall("util/Dates", "Z", "hasTodayValue", .jcast(notified$.jobj, "java.util.Date"))
})

method("isAfterNow_by_Date", "JDates", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    jCall("util/Dates", "Z", "isAfterNow", .jcast(asOf$.jobj, "java.util.Date"))
})

method("isBeforeNow_by_Date", "JDates", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    jCall("util/Dates", "Z", "isBeforeNow", .jcast(asOf$.jobj, "java.util.Date"))
})

method("laterOf_by_Date_Date", "JDates", enforceRCC = TRUE, function(static, a = NULL, b = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "laterOf", .jcast(a$.jobj, "java.util.Date"), .jcast(b$.jobj, "java.util.Date")))
})

method("timeOn_by_String_Date", "JDates", enforceRCC = TRUE, function(static, at = NULL, date = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "timeOn", the(at), .jcast(date$.jobj, "java.util.Date")))
})

method("todayAt_by_String", "JDates", enforceRCC = TRUE, function(static, at = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "todayAt", the(at)))
})

method("year_by_Date", "JDates", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    jCall("util/Dates", "I", "year", .jcast(asOf$.jobj, "java.util.Date"))
})

method("twoDigitYear_by_Date", "JDates", enforceRCC = TRUE, function(static, asOf = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "twoDigitYear", .jcast(asOf$.jobj, "java.util.Date"))
})

method("isHoliday_by_Date_String", "JDates", enforceRCC = TRUE, function(static, date = NULL, center = NULL, ...) {
    jCall("util/Dates", "Z", "isHoliday", .jcast(date$.jobj, "java.util.Date"), the(center))
})

method("setHour_by_Date_int", "JDates", enforceRCC = TRUE, function(static, date = NULL, hour = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "setHour", .jcast(date$.jobj, "java.util.Date"), theInteger(hour)))
})

method("thawNow", "JDates", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Dates", "V", "thawNow")
})

method("freezeNow_by_String", "JDates", enforceRCC = TRUE, function(static, date = NULL, ...) {
    jCall("util/Dates", "V", "freezeNow", the(date))
})

method("freezeNow_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Dates", "V", "freezeNow", .jcast(d$.jobj, "java.util.Date"))
})

method("freezeNow", "JDates", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Dates", "V", "freezeNow")
})

method("timestamp_by_Date", "JDates", enforceRCC = TRUE, function(static, start = NULL, ...) {
    JTimestamp(jobj = jCall("util/Dates", "Ljava/sql/Timestamp;", "timestamp", .jcast(start$.jobj, "java.util.Date")))
})

method("calendar_by_Date", "JDates", enforceRCC = TRUE, function(static, start = NULL, ...) {
    JCalendar(jobj = jCall("util/Dates", "Ljava/util/Calendar;", "calendar", .jcast(start$.jobj, "java.util.Date")))
})

method("date_by_int_int_int", "JDates", enforceRCC = TRUE, function(static, year = NULL, month = NULL, day = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "date", theInteger(year), theInteger(month), theInteger(day)))
})

method("businessDaysBetween_by_Date_Date_String", "JDates", enforceRCC = TRUE, function(static, startDate = NULL, endDate = NULL, center = NULL, ...) {
    jCall("util/Dates", "I", "businessDaysBetween", .jcast(startDate$.jobj, "java.util.Date"), .jcast(endDate$.jobj, "java.util.Date"), the(center))
})

method("businessDaysAhead_by_int_Date_String", "JDates", enforceRCC = TRUE, function(static, days = NULL, startDate = NULL, center = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "businessDaysAhead", theInteger(days), .jcast(startDate$.jobj, "java.util.Date"), the(center)))
})

method("nextBusinessDay_by_Date", "JDates", enforceRCC = TRUE, function(static, startDate = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "nextBusinessDay", .jcast(startDate$.jobj, "java.util.Date")))
})

method("nextBusinessDay_by_Date_String", "JDates", enforceRCC = TRUE, function(static, startDate = NULL, center = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "nextBusinessDay", .jcast(startDate$.jobj, "java.util.Date"), the(center)))
})

method("businessDaysAgo_by_int_Date_String", "JDates", enforceRCC = TRUE, function(static, days = NULL, startDate = NULL, center = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "businessDaysAgo", theInteger(days), .jcast(startDate$.jobj, "java.util.Date"), the(center)))
})

method("fridayBeforeThirdWed_by_YearMonth_String", "JDates", enforceRCC = TRUE, function(static, ym = NULL, financialCenter = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "fridayBeforeThirdWed", .jcast(ym$.jobj, "util.YearMonth"), the(financialCenter)))
})

method("thirdFriday_by_YearMonth", "JDates", enforceRCC = TRUE, function(static, ym = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "thirdFriday", .jcast(ym$.jobj, "util.YearMonth")))
})

method("thirdWedLessTwo_by_YearMonth_String", "JDates", enforceRCC = TRUE, function(static, ym = NULL, financialCenter = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "thirdWedLessTwo", .jcast(ym$.jobj, "util.YearMonth"), the(financialCenter)))
})

method("nowFrozen", "JDates", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Dates", "Z", "nowFrozen")
})

method("reallyNow", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "reallyNow"))
})

method("now", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "now"))
})

method("daysAgo_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ago = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "daysAgo", theInteger(ago), .jcast(d$.jobj, "java.util.Date")))
})

method("daysAhead_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ahead = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "daysAhead", theInteger(ahead), .jcast(d$.jobj, "java.util.Date")))
})

method("monthsAgo_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ago = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "monthsAgo", theInteger(ago), .jcast(d$.jobj, "java.util.Date")))
})

method("monthsAhead_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ahead = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "monthsAhead", theInteger(ahead), .jcast(d$.jobj, "java.util.Date")))
})

method("hoursAgo_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ago = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "hoursAgo", theInteger(ago), .jcast(d$.jobj, "java.util.Date")))
})

method("hoursAhead_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ahead = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "hoursAhead", theInteger(ahead), .jcast(d$.jobj, "java.util.Date")))
})

method("minutesAgo_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ago = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "minutesAgo", theInteger(ago), .jcast(d$.jobj, "java.util.Date")))
})

method("minutesAhead_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ahead = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "minutesAhead", theInteger(ahead), .jcast(d$.jobj, "java.util.Date")))
})

method("secondsAgo_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ago = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "secondsAgo", theInteger(ago), .jcast(d$.jobj, "java.util.Date")))
})

method("secondsAhead_by_int_Date", "JDates", enforceRCC = TRUE, function(static, ahead = NULL, d = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "secondsAhead", theInteger(ahead), .jcast(d$.jobj, "java.util.Date")))
})

method("tomorrow", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "tomorrow"))
})

method("yesterday", "JDates", enforceRCC = TRUE, function(static, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "yesterday"))
})

method("yearMonth_by_String", "JDates", enforceRCC = TRUE, function(static, s = NULL, ...) {
    JYearMonth(jobj = jCall("util/Dates", "Lutil/YearMonth;", "yearMonth", the(s)))
})

method("yearMonth_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    JYearMonth(jobj = jCall("util/Dates", "Lutil/YearMonth;", "yearMonth", .jcast(d$.jobj, "java.util.Date")))
})

method("hhMmSs_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "hhMmSs", .jcast(d$.jobj, "java.util.Date"))
})

method("yyyyMmDdHhMmSsMmm_by_String", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "yyyyMmDdHhMmSsMmm", the(value)))
})

method("yyyyMmDdHhMmSs_by_String", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "yyyyMmDdHhMmSs", the(value)))
})

method("mmYyyy_by_Date", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "mmYyyy", .jcast(value$.jobj, "java.util.Date"))
})

method("yyyyMmDdHhMmSsMmm_by_Date", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "yyyyMmDdHhMmSsMmm", .jcast(value$.jobj, "java.util.Date"))
})

method("yyyyMmDdHhMmSsNoSeparator_by_Date", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "yyyyMmDdHhMmSsNoSeparator", .jcast(value$.jobj, "java.util.Date"))
})

method("yyyyMmDdHhMmSs_by_Date", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "yyyyMmDdHhMmSs", .jcast(value$.jobj, "java.util.Date"))
})

method("ymdHuman_by_Date", "JDates", enforceRCC = TRUE, function(static, start = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "ymdHuman", .jcast(start$.jobj, "java.util.Date"))
})

method("midnight_by_Date", "JDates", enforceRCC = TRUE, function(static, time = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "midnight", .jcast(time$.jobj, "java.util.Date")))
})

method("asLong_by_Date", "JDates", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Dates", "J", "asLong", .jcast(d$.jobj, "java.util.Date"))
})

method("yyyyMmDd_by_Double", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "yyyyMmDd", .jcast(value$.jobj, "java.lang.Double")))
})

method("yyyyMmDd_by_String", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "yyyyMmDd", the(value)))
})

method("sqlDate_by_Date", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "sqlDate", .jcast(value$.jobj, "java.util.Date"))
})

method("yyyyMmDd_by_Date", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    jCall("util/Dates", "Ljava/lang/String;", "yyyyMmDd", .jcast(value$.jobj, "java.util.Date"))
})

method("date_by_Calendar", "JDates", enforceRCC = TRUE, function(static, calendar = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "date", .jcast(calendar$.jobj, "java.util.Calendar")))
})

method("date_by_String", "JDates", enforceRCC = TRUE, function(static, value = NULL, ...) {
    JDate(jobj = jCall("util/Dates", "Ljava/util/Date;", "date", the(value)))
})

method("EPOCH", "JDates", enforceRCC = FALSE, function(static, ...) {
    lazy(JDates$...EPOCH, JDate(jobj = jField("util/Dates", "Ljava/util/Date;", "EPOCH")), log = FALSE)
})

