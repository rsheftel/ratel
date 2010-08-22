constructor("JLog", function(jobj = NULL) {
    extend(JObject(), "JLog", .jobj = jobj)
})

method("by", "JLog", enforceRCC = TRUE, function(static, ...) {
    JLog(jNew("util/Log"))
})

method("verbose", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "Z", "verbose")
})

method("setVerboseLoggingForever_by_boolean", "JLog", enforceRCC = TRUE, function(static, beVerbose = NULL, ...) {
    jCall("util/Log", "Z", "setVerboseLoggingForever", theLogical(beVerbose))
})

method("setVerboseLogging_by_boolean", "JLog", enforceRCC = TRUE, function(static, beVerbose = NULL, ...) {
    jCall("util/Log", "Z", "setVerboseLogging", theLogical(beVerbose))
})

method("setBothStreams_by_PrintStream", "JLog", enforceRCC = TRUE, function(static, logStream = NULL, ...) {
    jCall("util/Log", "V", "setBothStreams", .jcast(logStream$.jobj, "java.io.PrintStream"))
})

method("setFile_by_String", "JLog", enforceRCC = TRUE, function(static, file = NULL, ...) {
    jCall("util/Log", "V", "setFile", the(file))
})

method("setFile_by_QFile", "JLog", enforceRCC = TRUE, function(static, file = NULL, ...) {
    jCall("util/Log", "V", "setFile", .jcast(file$.jobj, "file.QFile"))
})

method("setToSystem", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "V", "setToSystem")
})

method("stack_by_String", "JLog", enforceRCC = TRUE, function(static, message = NULL, ...) {
    jCall("util/Log", "V", "stack", the(message))
})

method("errMessage_by_Throwable", "JLog", enforceRCC = TRUE, function(static, t = NULL, ...) {
    jCall("util/Log", "Ljava/lang/String;", "errMessage", .jcast(t$.jobj, "java.lang.Throwable"))
})

method("err_by_String_Throwable", "JLog", enforceRCC = TRUE, function(static, string = NULL, e = NULL, ...) {
    jCall("util/Log", "V", "err", the(string), .jcast(e$.jobj, "java.lang.Throwable"))
})

method("info_by_String_Exception", "JLog", enforceRCC = TRUE, function(static, string = NULL, e = NULL, ...) {
    jCall("util/Log", "V", "info", the(string), .jcast(e$.jobj, "java.lang.Exception"))
})

method("err_by_String", "JLog", enforceRCC = TRUE, function(static, string = NULL, ...) {
    jCall("util/Log", "V", "err", the(string))
})

method("dot_by_String", "JLog", enforceRCC = TRUE, function(static, type = NULL, ...) {
    jCall("util/Log", "V", "dot", the(type))
})

method("dot", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "V", "dot")
})

method("progressDots_by_boolean", "JLog", enforceRCC = TRUE, function(static, b = NULL, ...) {
    jCall("util/Log", "V", "progressDots", theLogical(b))
})

method("debugSql_by_boolean", "JLog", enforceRCC = TRUE, function(static, b = NULL, ...) {
    jCall("util/Log", "V", "debugSql", theLogical(b))
})

method("restoreSqlDebugging", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "V", "restoreSqlDebugging")
})

method("setDebugSqlStateForever_by_boolean", "JLog", enforceRCC = TRUE, function(static, debugState = NULL, ...) {
    jCall("util/Log", "V", "setDebugSqlStateForever", theLogical(debugState))
})

method("debugSqlForever", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "V", "debugSqlForever")
})

method("doNotDebugSqlForever", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "V", "doNotDebugSqlForever")
})

method("debugSql", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "Z", "debugSql")
})

method("lineEnd_by_String", "JLog", enforceRCC = TRUE, function(static, string = NULL, ...) {
    jCall("util/Log", "V", "lineEnd", the(string))
})

method("linePart_by_String", "JLog", enforceRCC = TRUE, function(static, string = NULL, ...) {
    jCall("util/Log", "V", "linePart", the(string))
})

method("lineStart_by_String_boolean", "JLog", enforceRCC = TRUE, function(static, string = NULL, includeContext = NULL, ...) {
    jCall("util/Log", "V", "lineStart", the(string), theLogical(includeContext))
})

method("lineStart_by_String", "JLog", enforceRCC = TRUE, function(static, string = NULL, ...) {
    jCall("util/Log", "V", "lineStart", the(string))
})

method("debug_by_Collection", "JLog", enforceRCC = TRUE, function(static, o = NULL, ...) {
    jCall("util/Log", "V", "debug", .jcast(o$.jobj, "java.util.Collection"))
})

method("debug_by_Object", "JLog", enforceRCC = TRUE, function(static, o = NULL, ...) {
    jCall("util/Log", "V", "debug", .jcast(o$.jobj, "java.lang.Object"))
})

method("debug_by_String", "JLog", enforceRCC = TRUE, function(static, string = NULL, ...) {
    jCall("util/Log", "V", "debug", the(string))
})

method("prolog", "JLog", enforceRCC = TRUE, function(static, ...) {
    jCall("util/Log", "Ljava/lang/String;", "prolog")
})

method("logStream", "JLog", enforceRCC = TRUE, function(static, ...) {
    JPrintStream(jobj = jCall("util/Log", "Ljava/io/PrintStream;", "logStream"))
})

method("errStream", "JLog", enforceRCC = TRUE, function(static, ...) {
    JPrintStream(jobj = jCall("util/Log", "Ljava/io/PrintStream;", "errStream"))
})

method("info_by_String", "JLog", enforceRCC = TRUE, function(static, string = NULL, ...) {
    jCall("util/Log", "V", "info", the(string))
})

method("setContext_by_String", "JLog", enforceRCC = TRUE, function(static, context = NULL, ...) {
    jCall("util/Log", "Ljava/lang/String;", "setContext", the(context))
})

