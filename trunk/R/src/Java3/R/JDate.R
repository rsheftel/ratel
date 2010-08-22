constructor("JDate", function(jobj = NULL) {
    extend(JObject(), "JDate", .jobj = jobj)
})

method("by_String", "JDate", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JDate(jNew("java/util/Date", the(j_arg0)))
})

method("by_int_int_int_int_int_int", "JDate", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, j_arg4 = NULL, j_arg5 = NULL, ...) {
    JDate(jNew("java/util/Date", theInteger(j_arg0), theInteger(j_arg1), theInteger(j_arg2), theInteger(j_arg3), theInteger(j_arg4), theInteger(j_arg5)))
})

method("by_int_int_int_int_int", "JDate", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, j_arg4 = NULL, ...) {
    JDate(jNew("java/util/Date", theInteger(j_arg0), theInteger(j_arg1), theInteger(j_arg2), theInteger(j_arg3), theInteger(j_arg4)))
})

method("by_int_int_int", "JDate", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    JDate(jNew("java/util/Date", theInteger(j_arg0), theInteger(j_arg1), theInteger(j_arg2)))
})

method("by_long", "JDate", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JDate(jNew("java/util/Date", theLong(j_arg0)))
})

method("by", "JDate", enforceRCC = TRUE, function(static, ...) {
    JDate(jNew("java/util/Date"))
})

method("compareTo_by_Object", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("getTimezoneOffset", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getTimezoneOffset")
})

method("toGMTString", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toGMTString")
})

method("toLocaleString", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toLocaleString")
})

method("toString", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("hashCode", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("compareTo_by_Date", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.util.Date"))
})

method("equals_by_Object", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("after_by_Date", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "after", .jcast(j_arg0$.jobj, "java.util.Date"))
})

method("before_by_Date", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "before", .jcast(j_arg0$.jobj, "java.util.Date"))
})

method("setTime_by_long", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setTime", theLong(j_arg0))
})

method("getTime", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "getTime")
})

method("setSeconds_by_int", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setSeconds", theInteger(j_arg0))
})

method("getSeconds", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getSeconds")
})

method("setMinutes_by_int", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setMinutes", theInteger(j_arg0))
})

method("getMinutes", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getMinutes")
})

method("setHours_by_int", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setHours", theInteger(j_arg0))
})

method("getHours", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getHours")
})

method("getDay", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getDay")
})

method("setDate_by_int", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setDate", theInteger(j_arg0))
})

method("getDate", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getDate")
})

method("setMonth_by_int", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setMonth", theInteger(j_arg0))
})

method("getMonth", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getMonth")
})

method("setYear_by_int", "JDate", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "setYear", theInteger(j_arg0))
})

method("getYear", "JDate", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "getYear")
})

method("parse_by_String", "JDate", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/util/Date", "J", "parse", the(j_arg0))
})

method("UTC_by_int_int_int_int_int_int", "JDate", enforceRCC = FALSE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, j_arg4 = NULL, j_arg5 = NULL, ...) {
    jCall("java/util/Date", "J", "UTC", theInteger(j_arg0), theInteger(j_arg1), theInteger(j_arg2), theInteger(j_arg3), theInteger(j_arg4), theInteger(j_arg5))
})

method("clone", "JDate", enforceRCC = TRUE, function(this, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "clone"))
})

