constructor("JString", function(jobj = NULL) {
    extend(JObject(), "JString", .jobj = jobj)
})

method("by_StringBuilder", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JString(jNew("java/lang/String", .jcast(j_arg0$.jobj, "java.lang.StringBuilder")))
})

method("by_StringBuffer", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JString(jNew("java/lang/String", .jcast(j_arg0$.jobj, "java.lang.StringBuffer")))
})

method("by_byteArray", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B")))
})

method("by_byteArray_int_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), theInteger(j_arg1), theInteger(j_arg2)))
})

method("by_byteArray_Charset", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), .jcast(j_arg1$.jobj, "java.nio.charset.Charset")))
})

method("by_byteArray_String", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), the(j_arg1)))
})

method("by_byteArray_int_int_Charset", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), theInteger(j_arg1), theInteger(j_arg2), .jcast(j_arg3$.jobj, "java.nio.charset.Charset")))
})

method("by_byteArray_int_int_String", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), theInteger(j_arg1), theInteger(j_arg2), the(j_arg3)))
})

method("by_byteArray_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), theInteger(j_arg1)))
})

method("by_byteArray_int_int_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), theInteger(j_arg1), theInteger(j_arg2), theInteger(j_arg3)))
})

method("by_intArray_int_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(as.integer(j_arg0), "[I"), theInteger(j_arg1), theInteger(j_arg2)))
})

method("by_charArray_int_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C"), theInteger(j_arg1), theInteger(j_arg2)))
})

method("by_charArray", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JString(jNew("java/lang/String", jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C")))
})

method("by_String", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JString(jNew("java/lang/String", the(j_arg0)))
})

method("by", "JString", enforceRCC = TRUE, function(static, ...) {
    JString(jNew("java/lang/String"))
})

method("compareTo_by_Object", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("intern", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "intern")
})

method("valueOf_by_double", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", theNumeric(j_arg0))
})

method("valueOf_by_float", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", fail("float parameters not handled by R/Java convertPrimitive!"))
})

method("valueOf_by_long", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", theLong(j_arg0))
})

method("valueOf_by_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", theInteger(j_arg0))
})

method("valueOf_by_char", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", fail("char parameters not handled by R/Java convertPrimitive!"))
})

method("valueOf_by_boolean", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", theLogical(j_arg0))
})

method("copyValueOf_by_charArray", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "copyValueOf", jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C"))
})

method("copyValueOf_by_charArray_int_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "copyValueOf", jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C"), theInteger(j_arg1), theInteger(j_arg2))
})

method("valueOf_by_charArray_int_int", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C"), theInteger(j_arg1), theInteger(j_arg2))
})

method("valueOf_by_charArray", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C"))
})

method("valueOf_by_Object", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "valueOf", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("format_by_Locale_String_ObjectArray", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "format", .jcast(j_arg0$.jobj, "java.util.Locale"), the(j_arg1),  {
        if(inherits(j_arg2, "jarrayRef")) {
            j_arg2 <- .jevalArray(j_arg2)
        }
        jArray(lapply(j_arg2, function(x) {
            x$.jobj
        }), "java/lang/Object")
    })
})

method("format_by_String_ObjectArray", "JString", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/String", "Ljava/lang/String;", "format", the(j_arg0),  {
        if(inherits(j_arg1, "jarrayRef")) {
            j_arg1 <- .jevalArray(j_arg1)
        }
        jArray(lapply(j_arg1, function(x) {
            x$.jobj
        }), "java/lang/Object")
    })
})

method("toCharArray", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[C", "toCharArray")
})

method("toString", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("trim", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "trim")
})

method("toUpperCase", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toUpperCase")
})

method("toUpperCase_by_Locale", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toUpperCase", .jcast(j_arg0$.jobj, "java.util.Locale"))
})

method("toLowerCase", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toLowerCase")
})

method("toLowerCase_by_Locale", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toLowerCase", .jcast(j_arg0$.jobj, "java.util.Locale"))
})

method("split_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "split", the(j_arg0))
})

method("split_by_String_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "split", the(j_arg0), theInteger(j_arg1))
})

method("replace_by_CharSequence_CharSequence", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "replace", .jcast(j_arg0$.jobj, "java.lang.CharSequence"), .jcast(j_arg1$.jobj, "java.lang.CharSequence"))
})

method("replaceAll_by_String_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "replaceAll", the(j_arg0), the(j_arg1))
})

method("replaceFirst_by_String_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "replaceFirst", the(j_arg0), the(j_arg1))
})

method("contains_by_CharSequence", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "contains", .jcast(j_arg0$.jobj, "java.lang.CharSequence"))
})

method("matches_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "matches", the(j_arg0))
})

method("replace_by_char_char", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "replace", fail("char parameters not handled by R/Java convertPrimitive!"), fail("char parameters not handled by R/Java convertPrimitive!"))
})

method("concat_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "concat", the(j_arg0))
})

method("subSequence_by_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JCharSequence(jobj = jCall(this$.jobj, "Ljava/lang/CharSequence;", "subSequence", theInteger(j_arg0), theInteger(j_arg1)))
})

method("substring_by_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "substring", theInteger(j_arg0), theInteger(j_arg1))
})

method("substring_by_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "substring", theInteger(j_arg0))
})

method("lastIndexOf_by_String_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "I", "lastIndexOf", the(j_arg0), theInteger(j_arg1))
})

method("lastIndexOf_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "lastIndexOf", the(j_arg0))
})

method("indexOf_by_String_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "I", "indexOf", the(j_arg0), theInteger(j_arg1))
})

method("indexOf_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "indexOf", the(j_arg0))
})

method("lastIndexOf_by_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "I", "lastIndexOf", theInteger(j_arg0), theInteger(j_arg1))
})

method("lastIndexOf_by_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "lastIndexOf", theInteger(j_arg0))
})

method("indexOf_by_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "I", "indexOf", theInteger(j_arg0), theInteger(j_arg1))
})

method("indexOf_by_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "indexOf", theInteger(j_arg0))
})

method("hashCode", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("endsWith_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "endsWith", the(j_arg0))
})

method("startsWith_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "startsWith", the(j_arg0))
})

method("startsWith_by_String_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Z", "startsWith", the(j_arg0), theInteger(j_arg1))
})

method("regionMatches_by_boolean_int_String_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, j_arg4 = NULL, ...) {
    jCall(this$.jobj, "Z", "regionMatches", theLogical(j_arg0), theInteger(j_arg1), the(j_arg2), theInteger(j_arg3), theInteger(j_arg4))
})

method("regionMatches_by_int_String_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    jCall(this$.jobj, "Z", "regionMatches", theInteger(j_arg0), the(j_arg1), theInteger(j_arg2), theInteger(j_arg3))
})

method("compareToIgnoreCase_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareToIgnoreCase", the(j_arg0))
})

method("compareTo_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", the(j_arg0))
})

method("equalsIgnoreCase_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equalsIgnoreCase", the(j_arg0))
})

method("contentEquals_by_CharSequence", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "contentEquals", .jcast(j_arg0$.jobj, "java.lang.CharSequence"))
})

method("contentEquals_by_StringBuffer", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "contentEquals", .jcast(j_arg0$.jobj, "java.lang.StringBuffer"))
})

method("equals_by_Object", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("getBytes", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[B", "getBytes")
})

method("getBytes_by_Charset", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "[B", "getBytes", .jcast(j_arg0$.jobj, "java.nio.charset.Charset"))
})

method("getBytes_by_String", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "[B", "getBytes", the(j_arg0))
})

method("getBytes_by_int_int_byteArray_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    jCall(this$.jobj, "V", "getBytes", theInteger(j_arg0), theInteger(j_arg1), jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"), theInteger(j_arg3))
})

method("getChars_by_int_int_charArray_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, ...) {
    jCall(this$.jobj, "V", "getChars", theInteger(j_arg0), theInteger(j_arg1), jArray(fail("char parameters not handled by R/Java convertPrimitive!"), "[C"), theInteger(j_arg3))
})

method("offsetByCodePoints_by_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "I", "offsetByCodePoints", theInteger(j_arg0), theInteger(j_arg1))
})

method("codePointCount_by_int_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "I", "codePointCount", theInteger(j_arg0), theInteger(j_arg1))
})

method("codePointBefore_by_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "codePointBefore", theInteger(j_arg0))
})

method("codePointAt_by_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "codePointAt", theInteger(j_arg0))
})

method("charAt_by_int", "JString", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "C", "charAt", theInteger(j_arg0))
})

method("isEmpty", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("length", "JString", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "length")
})

method("CASE_INSENSITIVE_ORDER", "JString", enforceRCC = FALSE, function(static, ...) {
    lazy(JString$...CASE_INSENSITIVE_ORDER, JComparator(jobj = jField("java/lang/String", "Ljava/util/Comparator;", "CASE_INSENSITIVE_ORDER")), log = FALSE)
})

