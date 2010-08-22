constructor("JIndex", function(jobj = NULL) {
    extend(JObject(), "JIndex", .jobj = jobj)
})

method("isLast", "JIndex", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isLast")
})

method("isFirst", "JIndex", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isFirst")
})

method("indexing_by_Iterable", "JIndex", enforceRCC = TRUE, function(static, l1 = NULL, ...) {
    JIterable(jobj = jCall("util/Index", "Ljava/lang/Iterable;", "indexing", .jcast(l1$.jobj, "java.lang.Iterable")))
})

method("indexing_by_ObjectArray", "JIndex", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JIterable(jobj = jCall("util/Index", "Ljava/lang/Iterable;", "indexing",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("value", "JIndex", enforceRCC = FALSE, function(this, ...) {
    lazy(JIndex$...value, JObject(jobj = jField(this$.jobj, "Ljava/lang/Object;", "value")), log = FALSE)
})

method("num", "JIndex", enforceRCC = FALSE, function(this, ...) {
    lazy(JIndex$...num, jField(this$.jobj, "I", "num"), log = FALSE)
})

