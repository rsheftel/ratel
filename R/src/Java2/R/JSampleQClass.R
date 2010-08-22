constructor("JSampleQClass", function(jobj = NULL) {
    extend(JObject(), "JSampleQClass", .jobj = jobj)
})

method("by_int", "JSampleQClass", enforceRCC = TRUE, function(static, i = NULL, ...) {
    JSampleQClass(jNew("util/SampleQClass", theInteger(i)))
})

method("by_String", "JSampleQClass", enforceRCC = TRUE, function(static, s = NULL, ...) {
    JSampleQClass(jNew("util/SampleQClass", the(s)))
})

method("compareTo_by_Object", "JSampleQClass", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(x0$.jobj, "java.lang.Object"))
})

method("equals_by_Object", "JSampleQClass", enforceRCC = TRUE, function(this, obj = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(obj$.jobj, "java.lang.Object"))
})

method("hashCode", "JSampleQClass", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("allSame_by_SampleQClassArray", "JSampleQClass", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("util/SampleQClass", "Z", "allSame",  {
        if(inherits(args, "jarrayRef")) {
            args <- .jevalArray(args)
        }
        jArray(lapply(args, function(x) {
            x$.jobj
        }), "util/SampleQClass")
    })
})

method("doubleCopies_by_double", "JSampleQClass", enforceRCC = TRUE, function(static, n = NULL, ...) {
    jCall("util/SampleQClass", "[D", "doubleCopies", theNumeric(n))
})

method("intCopies_by_int", "JSampleQClass", enforceRCC = TRUE, function(static, n = NULL, ...) {
    jCall("util/SampleQClass", "[I", "intCopies", theInteger(n))
})

method("stringCopies_by_int", "JSampleQClass", enforceRCC = TRUE, function(this, n = NULL, ...) {
    jCall(this$.jobj, "[Ljava/lang/String;", "stringCopies", theInteger(n))
})

method("copies_by_int", "JSampleQClass", enforceRCC = TRUE, function(this, n = NULL, ...) {
    lapply(jCall(this$.jobj, "[Lutil/SampleQClass;", "copies", theInteger(n)), JSampleQClass)
})

method("sum_by_doubleArray", "JSampleQClass", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("util/SampleQClass", "D", "sum", jArray(as.numeric(args), "[D"))
})

method("sum_by_intArray", "JSampleQClass", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("util/SampleQClass", "I", "sum", jArray(as.integer(args), "[I"))
})

method("squish_by_StringArray", "JSampleQClass", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("util/SampleQClass", "Ljava/lang/String;", "squish", jArray(args, "[Ljava/lang/String;"))
})

method("compareTo_by_SampleQClass", "JSampleQClass", enforceRCC = TRUE, function(this, o = NULL, ...) {
    jCall(this$.jobj, "I", "compareTo", .jcast(o$.jobj, "util.SampleQClass"))
})

method("doNothing", "JSampleQClass", enforceRCC = TRUE, function(static, ...) {
    jCall("util/SampleQClass", "V", "doNothing")
})

method("world", "JSampleQClass", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "world")
})

method("toString", "JSampleQClass", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("GOODBYE", "JSampleQClass", enforceRCC = FALSE, function(static, ...) {
    lazy(JSampleQClass$...GOODBYE, JSampleQClass(jobj = jField("util/SampleQClass", "Lutil/SampleQClass;", "GOODBYE")), log = FALSE)
})

method("HELLO", "JSampleQClass", enforceRCC = FALSE, function(static, ...) {
    lazy(JSampleQClass$...HELLO, JSampleQClass(jobj = jField("util/SampleQClass", "Lutil/SampleQClass;", "HELLO")), log = FALSE)
})

