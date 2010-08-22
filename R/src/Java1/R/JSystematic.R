constructor("JSystematic", function(jobj = NULL) {
    extend(JObject(), "JSystematic", .jobj = jobj)
})

method("by", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    JSystematic(jNew("util/Systematic"))
})

method("dataDirectory", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    JQDirectory(jobj = jCall("util/Systematic", "Lfile/QDirectory;", "dataDirectory"))
})

method("setFakeDataDirectoryForTest_by_QDirectory", "JSystematic", enforceRCC = TRUE, function(static, fake = NULL, ...) {
    jCall("util/Systematic", "V", "setFakeDataDirectoryForTest", .jcast(fake$.jobj, "file.QDirectory"))
})

method("isLoggingTicks", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Z", "isLoggingTicks")
})

method("failureAddress", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    JEmailAddress(jobj = jCall("util/Systematic", "Lmail/EmailAddress;", "failureAddress"))
})

method("logsDir", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    JQDirectory(jobj = jCall("util/Systematic", "Lfile/QDirectory;", "logsDir"))
})

method("username", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Ljava/lang/String;", "username")
})

method("hostname", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Ljava/lang/String;", "hostname")
})

method("isDevDb", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Z", "isDevDb")
})

method("dbPassword", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Ljava/lang/String;", "dbPassword")
})

method("dbUser", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Ljava/lang/String;", "dbUser")
})

method("dbServer", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Systematic", "Ljava/lang/String;", "dbServer")
})

method("mainDir", "JSystematic", enforceRCC = TRUE, function(static, ...) {
    JQDirectory(jobj = jCall("util/Systematic", "Lfile/QDirectory;", "mainDir"))
})

method("QRUN_PARTS", "JSystematic", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystematic$...QRUN_PARTS, jField("util/Systematic", "[Ljava/lang/String;", "QRUN_PARTS"), log = FALSE)
})

method("JAVA_LIB_PARTS", "JSystematic", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystematic$...JAVA_LIB_PARTS, jField("util/Systematic", "[Ljava/lang/String;", "JAVA_LIB_PARTS"), log = FALSE)
})

