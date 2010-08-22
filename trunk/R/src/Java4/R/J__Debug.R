constructor("J__Debug", function(jobj = NULL) {
    extend(JObject(), "J__Debug", .jobj = jobj)
})

method("by", "J__Debug", enforceRCC = TRUE, function(static, ...) {
    J__Debug(jNew("util/__Debug"))
})

method("debug_by_String", "J__Debug", enforceRCC = TRUE, function(static, message = NULL, ...) {
    jCall("util/__Debug", "V", "debug", the(message))
})

