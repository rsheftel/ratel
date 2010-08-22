constructor("JSizable", function(jobj = NULL) {
    extend(JObject(), "JSizable", .jobj = jobj)
})

method("isEmpty", "JSizable", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "isEmpty")
})

method("size", "JSizable", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

