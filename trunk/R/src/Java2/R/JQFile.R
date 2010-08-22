constructor("JQFile", function(jobj = NULL) {
    extend(JObject(), "JQFile", .jobj = jobj)
})

method("by_File", "JQFile", enforceRCC = TRUE, function(static, file = NULL, ...) {
    JQFile(jNew("file/QFile", .jcast(file$.jobj, "java.io.File")))
})

method("by_String", "JQFile", enforceRCC = TRUE, function(static, fileName = NULL, ...) {
    JQFile(jNew("file/QFile", the(fileName)))
})

method("response", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JObject(jobj = jCall(this$.jobj, "Ljava/lang/Object;", "response"))
})

method("reader", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JReader(jobj = jCall(this$.jobj, "Ljava/io/Reader;", "reader"))
})

method("deleteFrom_by_QDirectory", "JQFile", enforceRCC = TRUE, function(this, other = NULL, ...) {
    jCall(this$.jobj, "V", "deleteFrom", .jcast(other$.jobj, "file.QDirectory"))
})

method("missing", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Z", "missing")
})

method("moveTo_by_QDirectory", "JQFile", enforceRCC = TRUE, function(this, newDir = NULL, ...) {
    jCall(this$.jobj, "V", "moveTo", .jcast(newDir$.jobj, "file.QDirectory"))
})

method("moveTo_by_QFile", "JQFile", enforceRCC = TRUE, function(this, newFile = NULL, ...) {
    jCall(this$.jobj, "V", "moveTo", .jcast(newFile$.jobj, "file.QFile"))
})

method("response", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[B", "response")
})

method("key_by_MetaBucket", "JQFile", enforceRCC = TRUE, function(this, bucket = NULL, ...) {
    JKey(jobj = jCall(this$.jobj, "Lamazon/MetaBucket/Key;", "key", .jcast(bucket$.jobj, "amazon.MetaBucket")))
})

method("csvHeader", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "csvHeader"))
})

method("withSuffix_by_String", "JQFile", enforceRCC = TRUE, function(this, suffix = NULL, ...) {
    JQFile(jobj = jCall(this$.jobj, "Lfile/QFile;", "withSuffix", the(suffix)))
})

method("basename", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "basename")
})

method("bytes", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "[B", "bytes")
})

method("copy_by_InputStream_OutputStream", "JQFile", enforceRCC = TRUE, function(static, j_in = NULL, out = NULL, ...) {
    jCall("file/QFile", "V", "copy", .jcast(j_in$.jobj, "java.io.InputStream"), .jcast(out$.jobj, "java.io.OutputStream"))
})

method("copyTo_by_OutputStream", "JQFile", enforceRCC = TRUE, function(this, out = NULL, ...) {
    jCall(this$.jobj, "V", "copyTo", .jcast(out$.jobj, "java.io.OutputStream"))
})

method("copyFrom_by_InputStream", "JQFile", enforceRCC = TRUE, function(this, j_in = NULL, ...) {
    jCall(this$.jobj, "V", "copyFrom", .jcast(j_in$.jobj, "java.io.InputStream"))
})

method("copyTo_by_QFile", "JQFile", enforceRCC = TRUE, function(this, dest = NULL, ...) {
    jCall(this$.jobj, "V", "copyTo", .jcast(dest$.jobj, "file.QFile"))
})

method("copyTo_by_QDirectory", "JQFile", enforceRCC = TRUE, function(this, dir = NULL, ...) {
    jCall(this$.jobj, "V", "copyTo", .jcast(dir$.jobj, "file.QDirectory"))
})

method("lastModified", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JDate(jobj = jCall(this$.jobj, "Ljava/util/Date;", "lastModified"))
})

method("parent", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JQDirectory(jobj = jCall(this$.jobj, "Lfile/QDirectory;", "parent"))
})

method("overwrite_by_byteArray", "JQFile", enforceRCC = TRUE, function(this, bytes = NULL, ...) {
    jCall(this$.jobj, "V", "overwrite", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"))
})

method("overwrite_by_String", "JQFile", enforceRCC = TRUE, function(this, text = NULL, ...) {
    jCall(this$.jobj, "V", "overwrite", the(text))
})

method("copyTo_by_String", "JQFile", enforceRCC = TRUE, function(this, destination = NULL, ...) {
    jCall(this$.jobj, "V", "copyTo", the(destination))
})

method("xml", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JTag(jobj = jCall(this$.jobj, "Lutil/Tag;", "xml"))
})

method("toString", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("append_by_String", "JQFile", enforceRCC = TRUE, function(this, string = NULL, ...) {
    jCall(this$.jobj, "V", "append", the(string))
})

method("appender", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JWriter(jobj = jCall(this$.jobj, "Ljava/io/Writer;", "appender"))
})

method("printAppender", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JPrintStream(jobj = jCall(this$.jobj, "Ljava/io/PrintStream;", "printAppender"))
})

method("csv_by_boolean", "JQFile", enforceRCC = TRUE, function(this, hasHeader = NULL, ...) {
    JCsv(jobj = jCall(this$.jobj, "Lfile/Csv;", "csv", theLogical(hasHeader)))
})

method("csv", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JCsv(jobj = jCall(this$.jobj, "Lfile/Csv;", "csv"))
})

method("file", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JFile(jobj = jCall(this$.jobj, "Ljava/io/File;", "file"))
})

method("name", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "name")
})

method("deleteIfExists", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "deleteIfExists")
})

method("ensurePath", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "ensurePath")
})

method("lines", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "lines"))
})

method("dataOut", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JDataOutputStream(jobj = jCall(this$.jobj, "Ljava/io/DataOutputStream;", "dataOut"))
})

method("size", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "J", "size")
})

method("j_in", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JInputStream(jobj = jCall(this$.jobj, "Ljava/io/InputStream;", "in"))
})

method("dataIn", "JQFile", enforceRCC = TRUE, function(this, ...) {
    JDataInputStream(jobj = jCall(this$.jobj, "Ljava/io/DataInputStream;", "dataIn"))
})

method("delete", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "V", "delete")
})

method("text_by_Reader", "JQFile", enforceRCC = TRUE, function(static, reader = NULL, ...) {
    jCall("file/QFile", "Ljava/lang/String;", "text", .jcast(reader$.jobj, "java.io.Reader"))
})

method("text_by_InputStream", "JQFile", enforceRCC = TRUE, function(static, j_in = NULL, ...) {
    jCall("file/QFile", "Ljava/lang/String;", "text", .jcast(j_in$.jobj, "java.io.InputStream"))
})

method("text", "JQFile", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "text")
})

method("create_by_byteArray", "JQFile", enforceRCC = TRUE, function(this, bytes = NULL, ...) {
    jCall(this$.jobj, "V", "create", jArray(fail("byte parameters not handled by R/Java convertPrimitive!"), "[B"))
})

method("create_by_String", "JQFile", enforceRCC = TRUE, function(this, text = NULL, ...) {
    jCall(this$.jobj, "V", "create", the(text))
})

