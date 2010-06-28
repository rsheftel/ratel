.First.lib <- function(libname, pkgname) {
    library("rJava")
    library("Core")
    params <- Sys.getenv("R_JAVA_OPTIONS")
    params <- ifElse(params == "", "-Xss2M -Xmx512M", params)
    params <- the(strsplit(params, split=" "))
    options(java.parameters=params)
    .jpackage("Java")
#    svn <- Sys.getenv("MAIN")
#    jarDir <- squish(svn, "/Java/systematic/lib")
#    allFiles <- list.files(jarDir, full.names = FALSE)
#    jars <- grep(".*\\.jar", allFiles, TRUE, value = TRUE)
#    if (isWindows()) { 
#        cacheDir <- squish(tempdir(), "\\QRJarFileCache")
#        dir.create(cacheDir, recursive = TRUE)
#        lapply(jars, function(jar) { 
#            jarFile <- squish(jarDir, "\\", jar)
#            cacheFile <- squish(cacheDir, "\\", jar)
#            file.copy(jarFile, cacheFile) 
#        })
#        jars <- paste(cacheDir, jars, sep = "\\")
#    } else {
#        jars <- paste(jarDir, jars, sep="/")
#    }

#    if (length(jars)) 
#        .jaddClassPath(jars)
#    jCall("util/Log", "V", "debugSql", theLogical(FALSE))
    
#    rJavaClassLoader <- .jcall(.jcall(.jnew("javax/mail/Flags"), "Ljava/lang/Class;", "getClass"), "Ljava/lang/ClassLoader;", "getClassLoader")
#    .jcall(.jcall("java.lang.Thread", "Ljava/lang/Thread;", "currentThread"), "V", "setContextClassLoader", rJavaClassLoader)

}

.First.lib(NULL, NULL)
