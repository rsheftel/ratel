constructor("JIterator", function(jobj = NULL) {
    extend(JObject(), "JIterator", .jobj = jobj)
})

method("remove", "JIterator", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "remove")
})

method("j_next", "JIterator", enforceRCC = TRUE, function(this, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "next"))
})

method("hasNext", "JIterator", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "hasNext")
})

