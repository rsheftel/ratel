
options(dontWarnPkgs = "base")

method <- setMethodS3

constructor <- setConstructorS3

inStaticConstructor <- function(this) {
    exists(".isCreatingStaticInstance", envir = attr(get(class(this)[1]), ".env"))
}

constructorNeeds <- function(this, ...) {
    if (inStaticConstructor(this)) return()
    needs(..., ..environ = parent.frame())
}

needs.checkOne <- function(arg.name, types, arg.value) { 
    if(!any(sapply(types, function(type) isType(arg.value, type))))
        fail(
            arg.name, " is not ", paste(types, collapse="|"), 
            " is ", commaSep(class(arg.value))
        )
}
needs.missingAndUnrequired <- function(arg.name, type, arg.value) {
    required <- length(grep("\\?$", type)) != 1 
    if(required && is.null(arg.value)) fail(arg.name, " is NULL")
    return(is.null(arg.value))
}

needs.types <- function(type) {
    type <- sub("\\?$", "", type)
    the(strsplit(type, "|", fixed = TRUE))
}

doNeeds <- function() {
    Sys.getenv("R_NO_NEEDS") == ""
}

noNeeds <- function(expr) {
    oldNeeds <- Sys.getenv("R_NO_NEEDS")
    Sys.setenv(R_NO_NEEDS = 1)
    result <- expr
    Sys.setenv(R_NO_NEEDS=oldNeeds)
    result
}

needs <- function(..., ..environ = parent.frame()) {
    if(!doNeeds()) return()
    params <- list(...)
    if (Sys.getenv("DEBUG_NEEDS") != "") {
        lapply(params, function(x) x)
        msg <- "needs: "
    }
    for(arg.name in names(params)) {
        type <- params[[arg.name]]
        arg.value <- get(arg.name, envir = ..environ)

        if (Sys.getenv("DEBUG_NEEDS") != "") {
            msg <- squish(msg, type, " -> ", noNeeds(humanString(arg.value)), " ")
        }
        if (needs.missingAndUnrequired(arg.name, type, arg.value)) next
        types <- needs.types(type)
        needs.checkOne(arg.name, types, arg.value)
    }
    if (Sys.getenv("DEBUG_NEEDS") != "") {
        noNeeds(cat(msg, "\n", sep = ""))
    }
}

method("isType", "default", function(val, type, ...) inherits(val, type))

innerType <- function(type) {
        inner.match <- regexpr("\\(.+\\)", type, perl=TRUE)
        if (inner.match == -1) return(NULL)
        start <- inner.match + 1
        end <- inner.match + attr(inner.match, "match.length") - 2
        substr(type, start, end)
}

defineIsTypeForCollection <- function(collectionType) {
    collectionTypeLength <- nchar(collectionType)
    function(val, type, ...) { 
        if(substr(type,1,collectionTypeLength) != collectionType) return(FALSE)
        if(length(val) == 0) return(TRUE)
        inner.type <- innerType(type)
        if(is.null(inner.type)) return(TRUE)
        isType(val[[1]], inner.type)
    }
}

method("isType", "list", defineIsTypeForCollection("list"))
method("isType", "array", defineIsTypeForCollection("array"))
method("isType", "matrix", defineIsTypeForCollection("matrix"))


dout <- function(...) {
    for(o in list(...)) cat(humanString(o))
    cat("\n")
}

humanString <- function(o) {
    if(isType(o, "list")) result <- paren(commaSep(strings(o)))
    else result <- commaSep(o)
    if (!is.null(names(o)) && length(names(o)) > 0) result <- squish(commaSep(names(o)), "\n", result)
    if (matches("^\\s", result) || matches("\\s$", result)) result <- squish('"', result, '"')
    result
}
strings <- function(o) {
    as.vector(sapply(o, function(i) as.character(i), USE.NAMES=FALSE))
}

paren <- function(o) {
    sapply(o, function(i) squish("(", i, ")"))
}

checkSame <- function(a, b) {
    if (!isTRUE(all.equal(a, b))) {
        lhs <- humanString(a)
        rhs <- humanString(b)
        if (lhs == rhs) { # try looking at names, rownames, colnames
            checkSame(squish("names:", names(a)), squish("names:", names(b)))
            checkSame(squish("colnames:", colnames(a)), squish("colnames:", colnames(b)))
            checkSame(squish("rownames:", rownames(a)), squish("rownames:", rownames(b)))
            checkSame(
                squish("attribute names:", commaSep(names(attributes(a)))), 
                squish("attribute names:", commaSep(names(attributes(b))))
            )
            for(name in names(attributes(a)))
            checkSame(
                squish("attr ", name, " = ", attr(a, name)), 
                squish("attr ", name, " = ", attr(b, name))
            )
        }
        lhs <- squish(ifLong(a, "\n"), lhs, ifLong(a, "\n\t"))
        rhs <- squish(ifLong(b, "\n"), rhs, ifLong(b, "\n"))
        fail(lhs, "did not match", rhs)
    }
}

ifLong <- function(o, yes, no = " ") {
    ifelse(length(o) > 1, yes, no)
}




