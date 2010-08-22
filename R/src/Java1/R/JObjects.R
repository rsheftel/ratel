constructor("JObjects", function(jobj = NULL) {
    extend(JObject(), "JObjects", .jobj = jobj)
})

method("by", "JObjects", enforceRCC = TRUE, function(static, ...) {
    JObjects(jNew("util/Objects"))
})

method("urlDecode_by_String", "JObjects", enforceRCC = TRUE, function(static, toDecode = NULL, ...) {
    jCall("util/Objects", "Ljava/lang/String;", "urlDecode", the(toDecode))
})

method("urlEncode_by_String", "JObjects", enforceRCC = TRUE, function(static, toEncode = NULL, ...) {
    jCall("util/Objects", "Ljava/lang/String;", "urlEncode", the(toEncode))
})

method("guid_by_boolean", "JObjects", enforceRCC = TRUE, function(static, useZeros = NULL, ...) {
    jCall("util/Objects", "Ljava/lang/String;", "guid", theLogical(useZeros))
})

method("deserialize_by_String", "JObjects", enforceRCC = TRUE, function(static, text = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "deserialize", the(text)))
})

method("serialize_by_Object", "JObjects", enforceRCC = TRUE, function(static, toSerialize = NULL, ...) {
    jCall("util/Objects", "Ljava/lang/String;", "serialize", .jcast(toSerialize$.jobj, "java.lang.Object"))
})

method("requiredValue_by_Map_Object", "JObjects", enforceRCC = TRUE, function(static, map = NULL, key = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "requiredValue", .jcast(map$.jobj, "java.util.Map"), .jcast(key$.jobj, "java.lang.Object")))
})

method("exitOnUncaughtExceptions_by_StringArray_String_String", "JObjects", enforceRCC = TRUE, function(static, args = NULL, emailAddress = NULL, name = NULL, ...) {
    jCall("util/Objects", "V", "exitOnUncaughtExceptions", jArray(args, "[Ljava/lang/String;"), the(emailAddress), the(name))
})

method("emptyMap", "JObjects", enforceRCC = TRUE, function(static, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "emptyMap"))
})

method("copyMap_by_Map", "JObjects", enforceRCC = TRUE, function(static, other = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "copyMap", .jcast(other$.jobj, "java.util.Map")))
})

method("copy_by_List", "JObjects", enforceRCC = TRUE, function(static, list = NULL, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "copy", .jcast(list$.jobj, "java.util.List")))
})

method("emptySet", "JObjects", enforceRCC = TRUE, function(static, ...) {
    JSet(jobj = jCall("util/Objects", "Ljava/util/Set;", "emptySet"))
})

method("empty", "JObjects", enforceRCC = TRUE, function(static, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "empty"))
})

method("list_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "list", .jcast(ts$.jobj, "java.util.Collection")))
})

method("map_by_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, k4 = NULL, v4 = NULL, k5 = NULL, v5 = NULL, k6 = NULL, v6 = NULL, k7 = NULL, v7 = NULL, k8 = NULL, v8 = NULL, k9 = NULL, v9 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object"), .jcast(k4$.jobj, "java.lang.Object"), .jcast(v4$.jobj, "java.lang.Object"), .jcast(k5$.jobj, "java.lang.Object"), .jcast(v5$.jobj, "java.lang.Object"), .jcast(k6$.jobj, "java.lang.Object"), .jcast(v6$.jobj, "java.lang.Object"), .jcast(k7$.jobj, "java.lang.Object"), .jcast(v7$.jobj, "java.lang.Object"), .jcast(k8$.jobj, "java.lang.Object"), .jcast(v8$.jobj, "java.lang.Object"), .jcast(k9$.jobj, "java.lang.Object"), .jcast(v9$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, k4 = NULL, v4 = NULL, k5 = NULL, v5 = NULL, k6 = NULL, v6 = NULL, k7 = NULL, v7 = NULL, k8 = NULL, v8 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object"), .jcast(k4$.jobj, "java.lang.Object"), .jcast(v4$.jobj, "java.lang.Object"), .jcast(k5$.jobj, "java.lang.Object"), .jcast(v5$.jobj, "java.lang.Object"), .jcast(k6$.jobj, "java.lang.Object"), .jcast(v6$.jobj, "java.lang.Object"), .jcast(k7$.jobj, "java.lang.Object"), .jcast(v7$.jobj, "java.lang.Object"), .jcast(k8$.jobj, "java.lang.Object"), .jcast(v8$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, k4 = NULL, v4 = NULL, k5 = NULL, v5 = NULL, k6 = NULL, v6 = NULL, k7 = NULL, v7 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object"), .jcast(k4$.jobj, "java.lang.Object"), .jcast(v4$.jobj, "java.lang.Object"), .jcast(k5$.jobj, "java.lang.Object"), .jcast(v5$.jobj, "java.lang.Object"), .jcast(k6$.jobj, "java.lang.Object"), .jcast(v6$.jobj, "java.lang.Object"), .jcast(k7$.jobj, "java.lang.Object"), .jcast(v7$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, k4 = NULL, v4 = NULL, k5 = NULL, v5 = NULL, k6 = NULL, v6 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object"), .jcast(k4$.jobj, "java.lang.Object"), .jcast(v4$.jobj, "java.lang.Object"), .jcast(k5$.jobj, "java.lang.Object"), .jcast(v5$.jobj, "java.lang.Object"), .jcast(k6$.jobj, "java.lang.Object"), .jcast(v6$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, k4 = NULL, v4 = NULL, k5 = NULL, v5 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object"), .jcast(k4$.jobj, "java.lang.Object"), .jcast(v4$.jobj, "java.lang.Object"), .jcast(k5$.jobj, "java.lang.Object"), .jcast(v5$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, k4 = NULL, v4 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object"), .jcast(k4$.jobj, "java.lang.Object"), .jcast(v4$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, k3 = NULL, v3 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object"), .jcast(k3$.jobj, "java.lang.Object"), .jcast(v3$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, k2 = NULL, v2 = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object"), .jcast(k2$.jobj, "java.lang.Object"), .jcast(v2$.jobj, "java.lang.Object")))
})

method("map_by_Object_Object", "JObjects", enforceRCC = TRUE, function(static, k = NULL, v = NULL, ...) {
    JMap(jobj = jCall("util/Objects", "Ljava/util/Map;", "map", .jcast(k$.jobj, "java.lang.Object"), .jcast(v$.jobj, "java.lang.Object")))
})

method("set_by_List", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JSet(jobj = jCall("util/Objects", "Ljava/util/Set;", "set", .jcast(ts$.jobj, "java.util.List")))
})

method("set_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JSet(jobj = jCall("util/Objects", "Ljava/util/Set;", "set",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("list_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "list",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("rest_by_List", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "rest", .jcast(ts$.jobj, "java.util.List")))
})

method("nth_by_Collection_int", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, n = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "nth", .jcast(ts$.jobj, "java.util.Collection"), theInteger(n)))
})

method("twentieth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "twentieth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("nineteenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "nineteenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("eighteenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "eighteenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("seventeenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "seventeenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("sixteenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "sixteenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("fifteenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "fifteenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("fourteenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "fourteenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("thirteenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "thirteenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("twelfth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "twelfth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("eleventh_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "eleventh", .jcast(ts$.jobj, "java.util.Collection")))
})

method("tenth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "tenth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("ninth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "ninth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("eighth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "eighth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("seventh_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "seventh", .jcast(ts$.jobj, "java.util.Collection")))
})

method("sixth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "sixth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("fifth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "fifth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("fourth_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "fourth", .jcast(ts$.jobj, "java.util.Collection")))
})

method("third_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "third", .jcast(ts$.jobj, "java.util.Collection")))
})

method("second_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "second", .jcast(ts$.jobj, "java.util.Collection")))
})

method("first_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "first", .jcast(ts$.jobj, "java.util.Collection")))
})

method("penultimate_by_List", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "penultimate", .jcast(ts$.jobj, "java.util.List")))
})

method("last_by_List", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "last", .jcast(ts$.jobj, "java.util.List")))
})

method("last_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "last",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("fourth_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "fourth",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("third_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "third",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("second_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "second",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("first_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "first",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("rest_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "rest",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("join_by_Thread", "JObjects", enforceRCC = TRUE, function(static, thread = NULL, ...) {
    jCall("util/Objects", "V", "join", .jcast(thread$.jobj, "java.lang.Thread"))
})

method("emptySynchronized", "JObjects", enforceRCC = TRUE, function(static, ...) {
    JList(jobj = jCall("util/Objects", "Ljava/util/List;", "emptySynchronized"))
})

method("join_by_List", "JObjects", enforceRCC = TRUE, function(static, threads = NULL, ...) {
    jCall("util/Objects", "V", "join", .jcast(threads$.jobj, "java.util.List"))
})

method("nonEmpty_by_Object_String", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, message = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "nonEmpty", .jcast(ts$.jobj, "java.lang.Object"), the(message)))
})

method("nonEmpty_by_Object", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "nonEmpty", .jcast(ts$.jobj, "java.lang.Object")))
})

method("the_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "the", .jcast(ts$.jobj, "java.util.Collection")))
})

method("theOrNull_by_Collection", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "theOrNull", .jcast(ts$.jobj, "java.util.Collection")))
})

method("the_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    JObject(jobj = jCall("util/Objects", "Ljava/lang/Object;", "the",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }))
})

method("array_by_ObjectArray", "JObjects", enforceRCC = TRUE, function(static, ts = NULL, ...) {
    lapply(jCall("util/Objects", "[Ljava/lang/Object;", "array",  {
        if(inherits(ts, "jarrayRef")) {
            ts <- .jevalArray(ts)
        }
        jArray(lapply(ts, function(x) {
            x$.jobj
        }), "java/lang/Object")
    }), JObject)
})

method("local_by_Object", "JObjects", enforceRCC = TRUE, function(static, init = NULL, ...) {
    JThreadLocal(jobj = jCall("util/Objects", "Ljava/lang/ThreadLocal;", "local", .jcast(init$.jobj, "java.lang.Object")))
})

method("local", "JObjects", enforceRCC = TRUE, function(static, ...) {
    JThreadLocal(jobj = jCall("util/Objects", "Ljava/lang/ThreadLocal;", "local"))
})

