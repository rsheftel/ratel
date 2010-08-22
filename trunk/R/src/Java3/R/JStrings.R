constructor("JStrings", function(jobj = NULL) {
    extend(JObject(), "JStrings", .jobj = jobj)
})

method("by", "JStrings", enforceRCC = TRUE, function(static, ...) {
    JStrings(jNew("util/Strings"))
})

method("fromBase64_by_String", "JStrings", enforceRCC = TRUE, function(static, base64 = NULL, ...) {
    jCall("util/Strings", "[B", "fromBase64", the(base64))
})

method("toBase64_by_byteArray", "JStrings", enforceRCC = TRUE, function(static, bytes = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "toBase64", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"))
})

method("sprintf_by_String_ObjectArray", "JStrings", enforceRCC = TRUE, function(static, format = NULL, params = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "sprintf", the(format),  {
        if(inherits(params, "jarrayRef")) {
            params <- .jevalArray(params)
        }
        jArray(lapply(params, function(x) {
            x$.jobj
        }), "java/lang/Object")
    })
})

method("nDecimals_by_int_double", "JStrings", enforceRCC = TRUE, function(static, n = NULL, value = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "nDecimals", theInteger(n), theNumeric(value))
})

method("toHumanString_by_Map", "JStrings", enforceRCC = TRUE, function(static, data = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "toHumanString", .jcast(data$.jobj, "java.util.Map"))
})

method("toSortedHumanString_by_Map", "JStrings", enforceRCC = TRUE, function(static, data = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "toSortedHumanString", .jcast(data$.jobj, "java.util.Map"))
})

method("human_by_double", "JStrings", enforceRCC = TRUE, function(static, d = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "human", theNumeric(d))
})

method("leftZeroPad_by_int_int", "JStrings", enforceRCC = TRUE, function(static, num = NULL, length = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "leftZeroPad", theInteger(num), theInteger(length))
})

method("commaSep_by_ObjectArray", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "commaSep",  {
        if(inherits(s, "jarrayRef")) {
            s <- .jevalArray(s)
        }
        jArray(lapply(s, function(x) {
            x$.jobj
        }), "java/lang/Object")
    })
})

method("commaSep_by_StringArray", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "commaSep", jArray(s, "[Ljava/lang/String;"))
})

method("commaSep_by_Collection", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "commaSep", .jcast(s$.jobj, "java.util.Collection"))
})

method("hasContent_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Z", "hasContent", the(s))
})

method("hasContent_by_Collection", "JStrings", enforceRCC = TRUE, function(static, foos = NULL, ...) {
    jCall("util/Strings", "Z", "hasContent", .jcast(foos$.jobj, "java.util.Collection"))
})

method("isEmpty_by_Collection", "JStrings", enforceRCC = TRUE, function(static, foos = NULL, ...) {
    jCall("util/Strings", "Z", "isEmpty", .jcast(foos$.jobj, "java.util.Collection"))
})

method("isEmpty_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Z", "isEmpty", the(s))
})

method("javaConstify_by_String", "JStrings", enforceRCC = TRUE, function(static, name = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "javaConstify", the(name))
})

method("javaIdentifier_by_String", "JStrings", enforceRCC = TRUE, function(static, name = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "javaIdentifier", the(name))
})

method("javaClassify_by_String", "JStrings", enforceRCC = TRUE, function(static, name = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "javaClassify", the(name))
})

method("join_by_String_Collection", "JStrings", enforceRCC = TRUE, function(static, delim = NULL, strings = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "join", the(delim), .jcast(strings$.jobj, "java.util.Collection"))
})

method("strings_by_Collection", "JStrings", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JList(jobj = jCall("util/Strings", "Ljava/util/List;", "strings", .jcast(ts$.jobj, "java.util.Collection")))
})

method("strings_by_ObjectArray", "JStrings", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JList(jobj = jCall("util/Strings", "Ljava/util/List;", "strings",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("join_by_String_StringArray", "JStrings", enforceRCC = TRUE, function(static, delim = NULL, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "join", the(delim), jArray(s, "[Ljava/lang/String;"))
})

method("chomp_by_String", "JStrings", enforceRCC = TRUE, function(static, maybeLineTerminated = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "chomp", the(maybeLineTerminated))
})

method("split_by_String_String", "JStrings", enforceRCC = TRUE, function(static, delim = NULL, s = NULL, ...) {
    JList(jobj = jCall("util/Strings", "Ljava/util/List;", "split", the(delim), the(s)))
})

method("bracket_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "bracket", the(s))
})

method("brace_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "brace", the(s))
})

method("paren_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "paren", the(s))
})

method("sQuote_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "sQuote", the(s))
})

method("toBoolean_by_String_boolean", "JStrings", enforceRCC = TRUE, function(static, human = NULL, defalt = NULL, ...) {
    jCall("util/Strings", "Z", "toBoolean", the(human), theLogical(defalt))
})

method("dQuote_by_String", "JStrings", enforceRCC = TRUE, function(static, s = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "dQuote", the(s))
})

method("leftSpacePad_by_int_String", "JStrings", enforceRCC = TRUE, function(static, desired = NULL, string = NULL, ...) {
    jCall("util/Strings", "Ljava/lang/String;", "leftSpacePad", theInteger(desired), the(string))
})

