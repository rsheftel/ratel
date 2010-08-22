constructor("JList", function(jobj = NULL) {
    extend(JObject(), "JList", .jobj = jobj)
})

method("subList_by_int_int", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "subList", theInteger(j_arg0), theInteger(j_arg1)))
})

method("listIterator_by_int", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JListIterator(jobj = jCall(this$.jobj, "Ljava/util/ListIterator;", "listIterator", theInteger(j_arg0)))
})

method("listIterator", "JList", enforceRCC = TRUE, function(this, ...) {
    JListIterator(jobj = jCall(this$.jobj, "Ljava/util/ListIterator;", "listIterator"))
})

method("lastIndexOf_by_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "lastIndexOf", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("indexOf_by_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "I", "indexOf", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("remove_by_int", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "remove", theInteger(j_arg0)))
})

method("add_by_int_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "V", "add", theInteger(j_arg0), .jcast(j_arg1$.jobj, "java.lang.Object"))
})

method("set_by_int_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "set", theInteger(j_arg0), .jcast(j_arg1$.jobj, "java.lang.Object")))
})

method("get_by_int", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "get", theInteger(j_arg0)))
})

method("hashCode", "JList", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("equals_by_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("clear", "JList", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("retainAll_by_Collection", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "retainAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("removeAll_by_Collection", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "removeAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("addAll_by_int_Collection", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall(this$.jobj, "Z", "addAll", theInteger(j_arg0), .jcast(j_arg1$.jobj, "java.util.Collection"))
})

method("addAll_by_Collection", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "addAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("containsAll_by_Collection", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "containsAll", .jcast(j_arg0$.jobj, "java.util.Collection"))
})

method("remove_by_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "remove", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("add_by_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "add", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("toArray_by_ObjectArray", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    lapply(jCall(this$.jobj, "[Ljava/lang/Object;", "toArray",  {
        if(inherits(j_arg0, "jarrayRef")) {
            j_arg0 <- .jevalArray(j_arg0)
        }
        jArray(lapply(j_arg0, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }), JObject)
})

method("toArray", "JList", enforceRCC = TRUE, function(this, ...) {
    lapply(jCall(this$.jobj, "[Ljava/lang/Object;", "toArray"), JObject)
})

method("iterator", "JList", enforceRCC = TRUE, function(this, ...) {
    JIterator(jobj = jCall(this$.jobj, "Ljava/util/Iterator;", "iterator"))
})

method("contains_by_Object", "JList", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "contains", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("isEmpty", "JList", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("size", "JList", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

