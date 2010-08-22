constructor("JPath", function(jobj = NULL) {
    extend(JObject(), "JPath", .jobj = jobj)
})

method("by_File", "JPath", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JPath(jNew("file/Path", .jcast(file$.jobj, "java.io.File")))
})

method("by_String", "JPath", enforceRCC = TRUE, function(static, fileName = NULL, ...) {
    JPath(jNew("file/Path", the(fileName)))
})

method("requireNotExists", "JPath", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "requireNotExists")
})

method("exists", "JPath", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "exists")
})

method("requireExists", "JPath", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "requireExists")
})

method("path", "JPath", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "path")
})

