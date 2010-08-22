constructor("JQDirectory", function(jobj = NULL) {
    extend(JObject(), "JQDirectory", .jobj = jobj)
})

method("by_File", "JQDirectory", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JQDirectory(jNew("file/QDirectory", .jcast(file$.jobj, "java.io.File")))
})

method("by_String", "JQDirectory", enforceRCC = TRUE, function(static, path = NULL, ...) {
    JQDirectory(jNew("file/QDirectory", the(path)))
})

method("exists_by_String", "JQDirectory", enforceRCC = TRUE, function(this, childPath = NULL, ...) {
    jCall(this$.jobj, "Z", "exists", the(childPath))
})

method("clear", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "clear")
})

method("copy_by_QDirectory", "JQDirectory", enforceRCC = TRUE, function(this, destination = NULL, ...) {
    jCall(this$.jobj, "V", "copy", .jcast(destination$.jobj, "file.QDirectory"))
})

method("csv_by_String_boolean", "JQDirectory", enforceRCC = TRUE, function(this, suffix = NULL, hasHeader = NULL, ...) {
    JCsv(jobj = jCall(this$.jobj, "Lfile/Csv;", "csv", the(suffix), theLogical(hasHeader)))
})

method("files_by_String", "JQDirectory", enforceRCC = TRUE, function(this, pattern = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "files", the(pattern)))
})

method("files", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "files"))
})

method("directories", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "directories"))
})

method("createIfMissing", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "createIfMissing")
})

method("destroyIfExists", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "destroyIfExists")
})

method("directory_by_StringArray", "JQDirectory", enforceRCC = TRUE, function(this, parts = NULL, ...) {
    JQDirectory(jobj = jCall(this$.jobj, "Lfile/QDirectory;", "directory", jArray(parts, "[Ljava/lang/String;")))
})

method("file_by_String", "JQDirectory", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JQFile(jobj = jCall(this$.jobj, "Lfile/QFile;", "file", the(name)))
})

method("file_by_StringArray", "JQDirectory", enforceRCC = TRUE, function(this, pathItems = NULL, ...) {
    JQFile(jobj = jCall(this$.jobj, "Lfile/QFile;", "file", jArray(pathItems, "[Ljava/lang/String;")))
})

method("remove", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "remove")
})

method("destroy", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "destroy")
})

method("size", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "I", "size")
})

method("create", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    JQDirectory(jobj = jCall(this$.jobj, "Lfile/QDirectory;", "create"))
})

method("name", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("removeAllNonHiddenFilesRecursive", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "removeAllNonHiddenFilesRecursive")
})

method("removeAllFiles", "JQDirectory", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "removeAllFiles")
})

