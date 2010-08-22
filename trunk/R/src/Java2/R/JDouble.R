constructor("JDouble", function(jobj = NULL) {
    extend(JObject(), "JDouble", .jobj = jobj)
})

method("by_String", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JDouble(jNew("java/lang/Double", the(j_arg0)))
})

method("by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JDouble(jNew("java/lang/Double", theNumeric(j_arg0)))
})

method("compareTo_by_Object", "JDouble", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("compare_by_double_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Double", "I", "compare", theNumeric(j_arg0), theNumeric(j_arg1))
})

method("compareTo_by_Double", "JDouble", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Double"))
})

method("longBitsToDouble_by_long", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "D", "longBitsToDouble", theLong(j_arg0))
})

method("doubleToRawLongBits_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "J", "doubleToRawLongBits", theNumeric(j_arg0))
})

method("doubleToLongBits_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "J", "doubleToLongBits", theNumeric(j_arg0))
})

method("equals_by_Object", "JDouble", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("hashCode", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("doubleValue", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "doubleValue")
})

method("floatValue", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "F", "floatValue")
})

method("longValue", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "longValue")
})

method("intValue", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "intValue")
})

method("shortValue", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "T", "shortValue")
})

method("byteValue", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "B", "byteValue")
})

method("toString", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("isInfinite", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isInfinite")
})

method("isNaN", "JDouble", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isNaN")
})

method("isInfinite_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "Z", "isInfinite", theNumeric(j_arg0))
})

method("isNaN_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "Z", "isNaN", theNumeric(j_arg0))
})

method("parseDouble_by_String", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "D", "parseDouble", the(j_arg0))
})

method("valueOf_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JDouble(jobj = jCall("java/lang/Double", "Ljava/lang/Double;", "valueOf", theNumeric(j_arg0)))
})

method("valueOf_by_String", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JDouble(jobj = jCall("java/lang/Double", "Ljava/lang/Double;", "valueOf", the(j_arg0)))
})

method("toHexString_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "Ljava/lang/String;", "toHexString", theNumeric(j_arg0))
})

method("toString_by_double", "JDouble", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Double", "Ljava/lang/String;", "toString", theNumeric(j_arg0))
})

method("TYPE", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...TYPE, JClass(jobj = jField("java/lang/Double", "Ljava/lang/Class;", "TYPE")), log = FALSE)
})

method("SIZE", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...SIZE, jField("java/lang/Double", "I", "SIZE"), log = FALSE)
})

method("MIN_EXPONENT", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...MIN_EXPONENT, jField("java/lang/Double", "I", "MIN_EXPONENT"), log = FALSE)
})

method("MAX_EXPONENT", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...MAX_EXPONENT, jField("java/lang/Double", "I", "MAX_EXPONENT"), log = FALSE)
})

method("MIN_VALUE", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...MIN_VALUE, jField("java/lang/Double", "D", "MIN_VALUE"), log = FALSE)
})

method("MIN_NORMAL", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...MIN_NORMAL, jField("java/lang/Double", "D", "MIN_NORMAL"), log = FALSE)
})

method("MAX_VALUE", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...MAX_VALUE, jField("java/lang/Double", "D", "MAX_VALUE"), log = FALSE)
})

method("j_NaN", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...NaN, jField("java/lang/Double", "D", "NaN"), log = FALSE)
})

method("NEGATIVE_INFINITY", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...NEGATIVE_INFINITY, jField("java/lang/Double", "D", "NEGATIVE_INFINITY"), log = FALSE)
})

method("POSITIVE_INFINITY", "JDouble", enforceRCC = FALSE, function(static, ...) {
    lazy(JDouble$...POSITIVE_INFINITY, jField("java/lang/Double", "D", "POSITIVE_INFINITY"), log = FALSE)
})

