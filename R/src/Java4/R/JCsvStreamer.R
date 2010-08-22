constructor("JCsvStreamer", function(jobj = NULL) {
    extend(JObject(), "JCsvStreamer", .jobj = jobj)
})

method("by_QFile_boolean", "JCsvStreamer", enforceRCC = TRUE, function(static, file = NULL, hasHeader = NULL, ...) {
    JCsvStreamer(jNew("file/CsvStreamer", .jcast(file$.jobj, "file.QFile"), theLogical(hasHeader)))
})

method("by_QFile", "JCsvStreamer", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JCsvStreamer(jNew("file/CsvStreamer", .jcast(file$.jobj, "file.QFile")))
})

method("close", "JCsvStreamer", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "close")
})

method("header", "JCsvStreamer", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "header"))
})

method("j_next", "JCsvStreamer", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "next"))
})

