constructor("JSystem", function(jobj = NULL) {
    extend(JObject(), "JSystem", .jobj = jobj)
})

method("mapLibraryName_by_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "Ljava/lang/String;", "mapLibraryName", the(j_arg0))
})

method("loadLibrary_by_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "loadLibrary", the(j_arg0))
})

method("load_by_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "load", the(j_arg0))
})

method("runFinalizersOnExit_by_boolean", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "runFinalizersOnExit", theLogical(j_arg0))
})

method("runFinalization", "JSystem", enforceRCC = TRUE, function(static, ...) {
    jCall("java/lang/System", "V", "runFinalization")
})

method("gc", "JSystem", enforceRCC = TRUE, function(static, ...) {
    jCall("java/lang/System", "V", "gc")
})

method("exit_by_int", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "exit", theInteger(j_arg0))
})

method("getenv", "JSystem", enforceRCC = TRUE, function(static, ...) {
    JMap(jobj = jCall("java/lang/System", "Ljava/util/Map;", "getenv"))
})

method("getenv_by_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "Ljava/lang/String;", "getenv", the(j_arg0))
})

method("clearProperty_by_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "Ljava/lang/String;", "clearProperty", the(j_arg0))
})

method("setProperty_by_String_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/System", "Ljava/lang/String;", "setProperty", the(j_arg0), the(j_arg1))
})

method("getProperty_by_String_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, ...) {
    jCall("java/lang/System", "Ljava/lang/String;", "getProperty", the(j_arg0), the(j_arg1))
})

method("getProperty_by_String", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "Ljava/lang/String;", "getProperty", the(j_arg0))
})

method("setProperties_by_Properties", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "setProperties", .jcast(j_arg0$.jobj, "java.util.Properties"))
})

method("getProperties", "JSystem", enforceRCC = TRUE, function(static, ...) {
    JProperties(jobj = jCall("java/lang/System", "Ljava/util/Properties;", "getProperties"))
})

method("identityHashCode_by_Object", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "I", "identityHashCode", .jcast(j_arg0$.jobj, "java.lang.Object"))
})

method("arraycopy_by_Object_int_Object_int_int", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, j_arg1 = NULL, j_arg2 = NULL, j_arg3 = NULL, j_arg4 = NULL, ...) {
    jCall("java/lang/System", "V", "arraycopy", .jcast(j_arg0$.jobj, "java.lang.Object"), theInteger(j_arg1), .jcast(j_arg2$.jobj, "java.lang.Object"), theInteger(j_arg3), theInteger(j_arg4))
})

method("nanoTime", "JSystem", enforceRCC = TRUE, function(static, ...) {
    jCall("java/lang/System", "J", "nanoTime")
})

method("currentTimeMillis", "JSystem", enforceRCC = TRUE, function(static, ...) {
    jCall("java/lang/System", "J", "currentTimeMillis")
})

method("getSecurityManager", "JSystem", enforceRCC = TRUE, function(static, ...) {
    JSecurityManager(jobj = jCall("java/lang/System", "Ljava/lang/SecurityManager;", "getSecurityManager"))
})

method("setSecurityManager_by_SecurityManager", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "setSecurityManager", .jcast(j_arg0$.jobj, "java.lang.SecurityManager"))
})

method("inheritedChannel", "JSystem", enforceRCC = TRUE, function(static, ...) {
    JChannel(jobj = jCall("java/lang/System", "Ljava/nio/channels/Channel;", "inheritedChannel"))
})

method("console", "JSystem", enforceRCC = TRUE, function(static, ...) {
    JConsole(jobj = jCall("java/lang/System", "Ljava/io/Console;", "console"))
})

method("setErr_by_PrintStream", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "setErr", .jcast(j_arg0$.jobj, "java.io.PrintStream"))
})

method("setOut_by_PrintStream", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "setOut", .jcast(j_arg0$.jobj, "java.io.PrintStream"))
})

method("setIn_by_InputStream", "JSystem", enforceRCC = TRUE, function(static, j_arg0 = NULL, ...) {
    jCall("java/lang/System", "V", "setIn", .jcast(j_arg0$.jobj, "java.io.InputStream"))
})

method("err", "JSystem", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystem$...err, JPrintStream(jobj = jField("java/lang/System", "Ljava/io/PrintStream;", "err")), log = FALSE)
})

method("out", "JSystem", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystem$...out, JPrintStream(jobj = jField("java/lang/System", "Ljava/io/PrintStream;", "out")), log = FALSE)
})

method("j_in", "JSystem", enforceRCC = FALSE, function(static, ...) {
    lazy(JSystem$...in, JInputStream(jobj = jField("java/lang/System", "Ljava/io/InputStream;", "in")), log = FALSE)
})

