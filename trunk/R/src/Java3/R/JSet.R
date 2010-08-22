constructor("JSet", function(jobj = NULL) {
    extend(JObject(), "JSet", .jobj = jobj)
})

method("hashCode", "JSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("equals_by_Object", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("clear", "JSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("removeAll_by_Collection", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "removeAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("retainAll_by_Collection", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "retainAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("addAll_by_Collection", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "addAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("containsAll_by_Collection", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "containsAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("remove_by_Object", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "remove", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("add_by_Object", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "add", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("toArray_by_ObjectArray", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    lapply(jCall(this$.jobj, "[Ljava/lang/Object;", "toArray",  {
        if(inherits(j_arg0, "jarrayRef")) {
            j_arg0 <- .jevalArray(j_arg0)
        }
        jArray(lapply(j_arg0, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }), JObject)
})

method("toArray", "JSet", enforceRCC = TRUE, function(this, ...) {
    lapply(jCall(this$.jobj, "[Ljava/lang/Object;", "toArray"), JObject)
})

method("iterator", "JSet", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("contains_by_Object", "JSet", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "contains", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("isEmpty", "JSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("size", "JSet", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

