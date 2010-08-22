constructor("JCsv", function(jobj = NULL) {
    extend(JObject(), "JCsv", .jobj = jobj)
})

method("by_QFile_boolean", "JCsv", enforceRCC = TRUE, function(static, file = NULL, hasHeader = NULL, ...) {
    JCsv(jNew("file/Csv", .jcast(file$.jobj, "file.QFile"), theLogical(hasHeader)))
})

method("by_QFile", "JCsv", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JCsv(jNew("file/Csv", .jcast(file$.jobj, "file.QFile")))
})

method("by_boolean", "JCsv", enforceRCC = TRUE, function(static, useQuotesOnWrite = NULL, ...) {
    JCsv(jNew("file/Csv", theLogical(useQuotesOnWrite)))
})

method("by", "JCsv", enforceRCC = TRUE, function(static, ...) {
    JCsv(jNew("file/Csv"))
})

method("header_by_QFile", "JCsv", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JList(jobj = jCall("file/Csv", "Ljava/util/List;", "header", .jcast(file$.jobj, "file.QFile")))
})

method("split_by_int_QDirectory_String", "JCsv", enforceRCC = TRUE, function(this, maxPerFile = NULL, dir = NULL, prefix = NULL, ...) {
    jCall(this$.jobj, "V", "split", theInteger(maxPerFile), .jcast(dir$.jobj, "file.QDirectory"), the(prefix))
})

method("overwrite_by_QFile", "JCsv", enforceRCC = TRUE, function(this, file = NULL, ...) {
    jCall(this$.jobj, "V", "overwrite", .jcast(file$.jobj, "file.QFile"))
})

method("columns", "JCsv", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "columns"))
})

method("record_by_int", "JCsv", enforceRCC = TRUE, function(this, i = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "record", theInteger(i)))
})

method("value_by_String_int", "JCsv", enforceRCC = TRUE, function(this, column = NULL, record = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "value", the(column), theInteger(record))
})

method("value_by_String_List", "JCsv", enforceRCC = TRUE, function(this, column = NULL, record = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "value", the(column), .jcast(record$.jobj, "java.util.List"))
})

method("count", "JCsv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "count")
})

method("asText", "JCsv", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "asText")
})

method("write_by_QFile", "JCsv", enforceRCC = TRUE, function(this, file = NULL, ...) {
    jCall(this$.jobj, "V", "write", .jcast(file$.jobj, "file.QFile"))
})

method("main_by_StringArray", "JCsv", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("file/Csv", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("records", "JCsv", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "records"))
})

method("add_by_StringArray", "JCsv", enforceRCC = TRUE, function(this, parts = NULL, ...) {
    jCall(this$.jobj, "V", "add", jArray(parts, "[Ljava/lang/String;"))
})

method("add_by_List", "JCsv", enforceRCC = TRUE, function(this, record = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(record$.jobj, "java.util.List"))
})

method("addHeader_by_StringArray", "JCsv", enforceRCC = TRUE, function(this, headerRecord = NULL, ...) {
    jCall(this$.jobj, "V", "addHeader", jArray(headerRecord, "[Ljava/lang/String;"))
})

method("addHeader_by_List", "JCsv", enforceRCC = TRUE, function(this, headerRecord = NULL, ...) {
    jCall(this$.jobj, "V", "addHeader", .jcast(headerRecord$.jobj, "java.util.List"))
})

