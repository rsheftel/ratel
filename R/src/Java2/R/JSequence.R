constructor("JSequence", function(jobj = NULL) {
    extend(JObject(), "JSequence", .jobj = jobj)
})

method("requireParallel_by_CollectionArray", "JSequence", enforceRCC = TRUE, function(static, lists = NULL, ...) {
    jCall("util/Sequence", "V", "requireParallel",  {
        if(inherits(lists, "jarrayRef")) {
            lists <- .jevalArray(lists)
        }
        jArray(lapply(lists, function(x) {
            x$.jobj
        }), "java/util/Collection")
    })
})

method("iterator", "JSequence", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("along_by_doubleArray", "JSequence", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JSequence(jobj = jCall("util/Sequence", "Lutil/Sequence;", "along", jArray(as.numeric(ts), "[D")))
})

method("along_by_ObjectArray", "JSequence", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JSequence(jobj = jCall("util/Sequence", "Lutil/Sequence;", "along",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("along_by_Collection", "JSequence", enforceRCC = TRUE, function(static, c = NULL, ...) {
    JSequence(jobj = jCall("util/Sequence", "Lutil/Sequence;", "along", .jcast(c$.jobj, "java.util.Collection")))
})

method("zeroTo_by_long", "JSequence", enforceRCC = TRUE, function(static, length = NULL, ...) {
    JSequence(jobj = jCall("util/Sequence", "Lutil/Sequence;", "zeroTo", theLong(length)))
})

method("sequence_by_int_int", "JSequence", enforceRCC = TRUE, function(static, start = NULL, end = NULL, ...) {
    JSequence(jobj = jCall("util/Sequence", "Lutil/Sequence;", "sequence", theInteger(start), theInteger(end)))
})

method("reverse", "JSequence", enforceRCC = TRUE, function(this, ...) {
    JSequence(jobj = jCall(this$.jobj, "Lutil/Sequence;", "reverse"))
})

method("oneTo_by_int", "JSequence", enforceRCC = TRUE, function(static, n = NULL, ...) {
    JSequence(jobj = jCall("util/Sequence", "Lutil/Sequence;", "oneTo", theInteger(n)))
})

