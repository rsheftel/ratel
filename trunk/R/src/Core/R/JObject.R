constructor("JObject", function(jobj = NULL) {
	extend(RObject(), "JObject", .jobj = jobj)
})

handleJavaException <- function(func.description) {
    exception <- .jgetEx(TRUE)

    if (!is.jnull(exception)) {
        w <- .jnew("java/io/StringWriter")
        pw <- .jnew("java/io/PrintWriter", .jcast(w, "java/io/Writer"))
        .jcall(.jcast(exception, "java/lang/Throwable"), "V", "printStackTrace", pw)
        stack.trace <- .jcall(.jcast(w, "java/lang/Object"), "S", "toString")

        stop(func.description," failed (", .jcall(exception, "S", "getMessage"),")\n", stack.trace)
    }
}

jNew <- function(class, ...) {
    on.exit(handleJavaException(".jnew"))
    .jnew(class, ..., check=FALSE, silent=TRUE)
}

jCall <- function(obj, returnSig = "V", method, ...) {
    on.exit(handleJavaException(".jcall"))
    .jcall(obj, returnSig, method, ..., check=FALSE)
} 

jField <- function(obj, sig = NULL, name, ...) {
    on.exit(handleJavaException(".jfield"))
    .jfield(obj, sig, name, ...)
}

jArray <- function(x, contents.class = NULL, ...) {
    on.exit(handleJavaException(".jarray"))
    if(inherits(first(x), "JObject"))
        x <- lapply(x, function(obj) obj$.jobj)
    .jarray(x, contents.class, ...)
}

theNumeric <- function(x) {
    as.numeric(the(x))
}

theInteger <- function(x) {
    as.integer(the(x))
}

theLogical <- function(x) {
    as.logical(the(x))
}

theLong <- function(x) {
    .jlong(the(x))
}

method("as", "JObject", function(this, proto, ...) {
    needs(proto="JObject")
    class(this) <- class(proto)
    this
})
