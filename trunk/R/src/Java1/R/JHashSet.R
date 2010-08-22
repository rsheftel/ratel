constructor("JHashSet", function(jobj = NULL) {
    extend(JObject(), "JHashSet", .jobj = jobj)
})

method("by_int", "JHashSet", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JHashSet(jNew("java/util/HashSet", theInteger(j_arg0)))
})

method("by_int_float", "JHashSet", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JHashSet(jNew("java/util/HashSet", theInteger(j_arg0), fail("float parameters not handled by R/Java convertPrimitive!")))
})

method("by_Collection", "JHashSet", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    JHashSet(jNew("java/util/HashSet", .jcast(j_arg0$.jobj, "java.util.Collection")))
})

method("by", "JHashSet", enforceRCC = TRUE, function(static, ...) {
    JHashSet(jNew("java/util/HashSet"))
})

method("clone", "JHashSet", enforceRCC = TRUE, function(this, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "clone"))
})

method("clear", "JHashSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("remove_by_Object", "JHashSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "remove", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("add_by_Object", "JHashSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "add", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("contains_by_Object", "JHashSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "contains", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("isEmpty", "JHashSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("size", "JHashSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

method("iterator", "JHashSet", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

