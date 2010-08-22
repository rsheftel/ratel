constructor("JDataUpload", function(jobj = NULL) {
    extend(JObject(), "JDataUpload", .jobj = jobj)
})

method("by_QFile", "JDataUpload", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JDataUpload(jNew("db/DataUpload", .jcast(file$.jobj, "file.QFile")))
})

method("by_String", "JDataUpload", enforceRCC = TRUE, function(static, csvPath = NULL, ...) {
    JDataUpload(jNew("db/DataUpload", the(csvPath)))
})

method("upload_by_StringArray", "JDataUpload", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("db/DataUpload", "Z", "upload", jArray(args, "[Ljava/lang/String;"))
})

method("main_by_StringArray", "JDataUpload", enforceRCC = TRUE, function(static, args = NULL, ...) {
    jCall("db/DataUpload", "V", "main", jArray(args, "[Ljava/lang/String;"))
})

method("upload", "JDataUpload", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "upload")
})

method("writeCsv", "JDataUpload", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "writeCsv")
})

method("add_by_List", "JDataUpload", enforceRCC = TRUE, function(this, datums = NULL, ...) {
    jCall(this$.jobj, "V", "add", .jcast(datums$.jobj, "java.util.List"))
})

method("addColumn_by_ConcreteColumn", "JDataUpload", enforceRCC = TRUE, function(this, c = NULL, ...) {
    jCall(this$.jobj, "V", "addColumn", .jcast(c$.jobj, "db.ConcreteColumn"))
})

