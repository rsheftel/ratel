constructor("JLong", function(jobj = NULL) {
    extend(JObject(), "JLong", .jobj = jobj)
})

method("by_String", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JLong(jNew("java/lang/Long", the(j_arg0)))
})

method("by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JLong(jNew("java/lang/Long", theLong(j_arg0)))
})

method("compareTo_by_Object", "JLong", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("reverseBytes_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "J", "reverseBytes", theLong(j_arg0))
})

method("signum_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "I", "signum", theLong(j_arg0))
})

method("reverse_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "J", "reverse", theLong(j_arg0))
})

method("rotateRight_by_long_int", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Long", "J", "rotateRight", theLong(j_arg0), theInteger(j_arg1))
})

method("rotateLeft_by_long_int", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Long", "J", "rotateLeft", theLong(j_arg0), theInteger(j_arg1))
})

method("bitCount_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "I", "bitCount", theLong(j_arg0))
})

method("numberOfTrailingZeros_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "I", "numberOfTrailingZeros", theLong(j_arg0))
})

method("numberOfLeadingZeros_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "I", "numberOfLeadingZeros", theLong(j_arg0))
})

method("lowestOneBit_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "J", "lowestOneBit", theLong(j_arg0))
})

method("highestOneBit_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "J", "highestOneBit", theLong(j_arg0))
})

method("compareTo_by_Long", "JLong", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Long"))
})

method("getLong_by_String_Long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "getLong", the(j_arg0), .jcast(j_arg1$.jobj, "java.lang.Long")))
})

method("getLong_by_String_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "getLong", the(j_arg0), theLong(j_arg1)))
})

method("getLong_by_String", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "getLong", the(j_arg0)))
})

method("equals_by_Object", "JLong", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("hashCode", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("toString", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("doubleValue", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "doubleValue")
})

method("floatValue", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "F", "floatValue")
})

method("longValue", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "longValue")
})

method("intValue", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "intValue")
})

method("shortValue", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "T", "shortValue")
})

method("byteValue", "JLong", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "B", "byteValue")
})

method("decode_by_String", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "decode", the(j_arg0)))
})

method("valueOf_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "valueOf", theLong(j_arg0)))
})

method("valueOf_by_String", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "valueOf", the(j_arg0)))
})

method("valueOf_by_String_int", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JLong(jobj = jCall("java/lang/Long", "Ljava/lang/Long;", "valueOf", the(j_arg0), theInteger(j_arg1)))
})

method("parseLong_by_String", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "J", "parseLong", the(j_arg0))
})

method("parseLong_by_String_int", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Long", "J", "parseLong", the(j_arg0), theInteger(j_arg1))
})

method("toString_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "Ljava/lang/String;", "toString", theLong(j_arg0))
})

method("toBinaryString_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "Ljava/lang/String;", "toBinaryString", theLong(j_arg0))
})

method("toOctalString_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "Ljava/lang/String;", "toOctalString", theLong(j_arg0))
})

method("toHexString_by_long", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/Long", "Ljava/lang/String;", "toHexString", theLong(j_arg0))
})

method("toString_by_long_int", "JLong", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/Long", "Ljava/lang/String;", "toString", theLong(j_arg0), theInteger(j_arg1))
})

method("SIZE", "JLong", enforceRCC = FALSE, function(static, ...) {
    lazy(JLong$...SIZE, jField("java/lang/Long", "I", "SIZE"), log = FALSE)
})

method("TYPE", "JLong", enforceRCC = FALSE, function(static, ...) {
    lazy(JLong$...TYPE, JClass(jobj = jField("java/lang/Long", "Ljava/lang/Class;", "TYPE")), log = FALSE)
})

method("MAX_VALUE", "JLong", enforceRCC = FALSE, function(static, ...) {
    lazy(JLong$...MAX_VALUE, jField("java/lang/Long", "J", "MAX_VALUE"), log = FALSE)
})

method("MIN_VALUE", "JLong", enforceRCC = FALSE, function(static, ...) {
    lazy(JLong$...MIN_VALUE, jField("java/lang/Long", "J", "MIN_VALUE"), log = FALSE)
})

