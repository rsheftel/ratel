constructor("JMap", function(jobj = NULL) {
    extend(JObject(), "JMap", .jobj = jobj)
})

method("hashCode", "JMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "hashCode")
})

method("equals_by_Object", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "equals", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("entrySet", "JMap", enforceRCC = TRUE, function(this, ...) {
    JSet(jobj = jCall(this$.jobj, "Ljava/util/Set;", "entrySet"))
})

method("values", "JMap", enforceRCC = TRUE, function(this, ...) {
    JCollection(jobj = jCall(this$.jobj, "Ljava/util/Collection;", "values"))
})

method("keySet", "JMap", enforceRCC = TRUE, function(this, ...) {
    JSet(jobj = jCall(this$.jobj, "Ljava/util/Set;", "keySet"))
})

method("clear", "JMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("putAll_by_Map", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "V", "putAll", .jcast(j_arg0$.jobj, "java.util.Map"))
})

method("remove_by_Object", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "remove", .jcast(j_arg0$.jobj, "java.lang.Object")))
})

method("put_by_Object_Object", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, j_arg1 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "put", .jcast(j_arg0$.jobj, "java.lang.Object"), .jcast(j_arg1$.jobj, "java.lang.Object")))
})

method("get_by_Object", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "get", .jcast(j_arg0$.jobj, "java.lang.Object")))
})

method("containsValue_by_Object", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "containsValue", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("containsKey_by_Object", "JMap", enforceRCC = TRUE, function(this, j_arg0 = NULL, ...) {
    jCall(this$.jobj, "Z", "containsKey", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("isEmpty", "JMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("size", "JMap", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

