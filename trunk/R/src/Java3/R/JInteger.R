constructor("JInteger", function(jobj = NULL) {
    extend(JObject(), "JInteger", .jobj = jobj)
})

method("by_String", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JInteger(jNew("java/lang/Integer", the(j_arg0)))
})

method("by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JInteger(jNew("java/lang/Integer", theInteger(j_arg0)))
})

method("compareTo_by_Object", "JInteger", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("reverseBytes_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "reverseBytes", theInteger(j_arg0))
})

method("signum_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "signum", theInteger(j_arg0))
})

method("reverse_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "reverse", theInteger(j_arg0))
})

method("rotateRight_by_int_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Integer", "I", "rotateRight", theInteger(j_arg0), theInteger(j_arg1))
})

method("rotateLeft_by_int_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Integer", "I", "rotateLeft", theInteger(j_arg0), theInteger(j_arg1))
})

method("bitCount_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "bitCount", theInteger(j_arg0))
})

method("numberOfTrailingZeros_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "numberOfTrailingZeros", theInteger(j_arg0))
})

method("numberOfLeadingZeros_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "numberOfLeadingZeros", theInteger(j_arg0))
})

method("lowestOneBit_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "lowestOneBit", theInteger(j_arg0))
})

method("highestOneBit_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "highestOneBit", theInteger(j_arg0))
})

method("compareTo_by_Integer", "JInteger", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Integer"))
})

method("decode_by_String", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "decode", the(j_arg0)))
})

method("getInteger_by_String_Integer", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "getInteger", the(j_arg0), .jcast(j_arg1$.jobj, "java.lang.Integer")))
})

method("getInteger_by_String_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "getInteger", the(j_arg0), theInteger(j_arg1)))
})

method("getInteger_by_String", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "getInteger", the(j_arg0)))
})

method("equals_by_Object", "JInteger", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("hashCode", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("doubleValue", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "doubleValue")
})

method("floatValue", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "F", "floatValue")
})

method("longValue", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "longValue")
})

method("intValue", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "intValue")
})

method("shortValue", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "T", "shortValue")
})

method("byteValue", "JInteger", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "B", "byteValue")
})

method("valueOf_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "valueOf", theInteger(j_arg0)))
})

method("valueOf_by_String", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "valueOf", the(j_arg0)))
})

method("valueOf_by_String_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JInteger(jobj = jCall("java/lang/Integer", "Ljava/lang/Integer;", "valueOf", the(j_arg0), theInteger(j_arg1)))
})

method("parseInt_by_String", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "I", "parseInt", the(j_arg0))
})

method("parseInt_by_String_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Integer", "I", "parseInt", the(j_arg0), theInteger(j_arg1))
})

method("toString_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "Ljava/lang/String;", "toString", theInteger(j_arg0))
})

method("toBinaryString_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "Ljava/lang/String;", "toBinaryString", theInteger(j_arg0))
})

method("toOctalString_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "Ljava/lang/String;", "toOctalString", theInteger(j_arg0))
})

method("toHexString_by_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Integer", "Ljava/lang/String;", "toHexString", theInteger(j_arg0))
})

method("toString_by_int_int", "JInteger", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Integer", "Ljava/lang/String;", "toString", theInteger(j_arg0), theInteger(j_arg1))
})

method("SIZE", "JInteger", enforceRCC = FALSE, function(static, ...) {
    lazy(JInteger$...SIZE, jField("java/lang/Integer", "I", "SIZE"), log = FALSE)
})

method("TYPE", "JInteger", enforceRCC = FALSE, function(static, ...) {
    lazy(JInteger$...TYPE, JClass(jobj = jField("java/lang/Integer", "Ljava/lang/Class;", "TYPE")), log = FALSE)
})

method("MAX_VALUE", "JInteger", enforceRCC = FALSE, function(static, ...) {
    lazy(JInteger$...MAX_VALUE, jField("java/lang/Integer", "I", "MAX_VALUE"), log = FALSE)
})

method("MIN_VALUE", "JInteger", enforceRCC = FALSE, function(static, ...) {
    lazy(JInteger$...MIN_VALUE, jField("java/lang/Integer", "I", "MIN_VALUE"), log = FALSE)
})

