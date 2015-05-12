options(warning.length = 8170)

assert <- function(condition, message = "") {
    if(length(condition) == 0 || !condition)
        throw("Assertion failed (", deparse(substitute(condition), width.cutoff = 500), ") ", message, "\n")
}

fail <- function(message = "failed", ...) {
    params <- list(...)
    if (length(params) != 0) message <- squish(message, params)
    throw(message)
}

failIf <- function(condition, ...) {
    needs(condition = "logical")
    if (!the(condition)) return()
    fail(...)
}

failUnless <- function(condition, message = "failed, condition was not met", ...) { 
    failIf(!condition, message, ...) 
}

isWindows <- function() {
    Sys.getenv("OS") == "Windows_NT"
}

dataDirectory <- function() {
    ifElse(isWindows(), "V:/", "/data/")
}

tsdbUploadDirectory <- function() {
	paste(dataDirectory(),"TSDB_upload/Today/",sep="")
}

homeDirectory <- function() {
    ifElse(isWindows(), "H:/", "~/")
}

tempDirectory <- function(){
	squish(dataDirectory(),'/temp_TSDB/')
}

loadBloomberg <- function() {
    if(isWindows()) library(RBloomberg)
}

requiresBloomberg <- function() {
    if(!isWindows() || Sys.getenv("NO_BLOOMBERG") == "TRUE") DEACTIVATED("Cannot run this test without Bloomberg")
}

reloadPackage <- function(pkgname) {
    message("Reloading ", pkgname)
    detach(pos =  match(paste("package:", pkgname, sep = ""), search())) 
    library(pkgname, character.only = TRUE)
}

runCmd <- function(...) {
    cat("running in shell: ", ..., "\n")
    shellFunc <- ifElse(isWindows(), shell, system)
    rc <- shellFunc(...)
    if(rc != 0)
        fail(squish("Call to system(", ..., ") failed.  Return code: ", rc))
}

singleQuote <- function(...) {
    paste("'", ..., "'", sep="")
}

doubleQuote <- function(...) {
    paste('"', ..., '"', sep="")
}

g <- function() {
    quit(save="no")
}

getopt <- function(optstring, argv) {
    stopifnot(length(grep("^[a-zA-Z][a-zA-Z:]*$", optstring)) == 1)

    options <- list()
    res <- list()
    last <- NA
    
    for (i in unlist(strsplit(optstring, split = NULL))) {
        if (!identical(i, ":")) {
            options[i] <- "logical"
            res[i] <- FALSE
            last <- i
        } else {
            options[last] <- "text"
            res[last] <- NULL 
        }
    }

    i <- 1
    while(i <= length(argv)) {
        next.opt <- unlist(strsplit(argv[[i]], split = NULL))
        if( next.opt[[1]] == "-" && length(next.opt) == 2 && !is.null(options[[next.opt[[2]]]]) ) {
            if (options[[next.opt[[2]]]] == "logical") {
                res[[next.opt[[2]]]] <- TRUE
            } else {
                res[[next.opt[[2]]]] <- argv[[i+1]]
                i <- i + 1
            }
        } else {
            res[[".argv"]] <- argv[i:length(argv)]
            break
        }

        i <- i + 1
    }

    res
}

source.all <- function(directory) {
    lapply(list.files(directory, ".*\\.R", full.names = TRUE), source)
}

# TODO:  Fix to use expand.grid - much faster...
array.apply <- function(a, func, ...) {
    dims <- sapply(dimnames(a), length)
    indices <- rep(1, length(dims))
    for(i in 1:cumprod(dims)[length(dims)]) {
        a[matrix(indices, ncol = length(dims))] <- do.call(match.fun(func), c(
            mapply(function(dimname, index) dimname[index], dimnames(a), indices, SIMPLIFY = FALSE),
            list(a[matrix(indices, ncol = length(dims))])
        ))
        for(i in length(indices):1) {
            indices[i] <- indices[i] + 1
            if (indices[i] > dims[i]) {
                indices[i] <- 1
            } else {
                break
            }
        }
    }
    a
}

out <- function(...) {
    i <- 1
    for(o in list(...)) {
        cat(squish("\n--", rep(i, 10), "--> OBJECT: \n"))
        dput(o)
        i <- i + 1
    }
}

squish <- function(...) {
    paste(unlist(list(...), recursive=TRUE), sep = "", collapse="")
}

join <- function(delim, cs) { 
    needs(delim = "character", cs = "list(character)|character?")
    paste(unlist(cs), sep="", collapse=delim)
}

Q <- function() { q("no") }

checkLength <- function(l, expectedLength) {
    needs(expectedLength="numeric|integer")
    failIf(is.null(l), "can't check length of NULL!")
    failIf(is.null(expectedLength), "can't check length of NULL!")
    failIf(is.null(expectedLength), "expectedLength is NULL in checkLength")
    failUnless(
        length(l) == the(expectedLength), 
        squish(
            "expected length of ", substitute(l), " was ", expectedLength, 
            " but got ", length(l), 
            ", list contained\n", commaSep(l)
        )
    )
}

doProfile <- function(func) {
    tempFile <- tempfile()
    Rprof(tempFile)
    times <- system.time(func())
    Rprof(NULL)
    print(summaryRprof(tempFile))
    print("total time:")
    print(times)
    unlink(tempFile)
}

checkNull <- function(o) {
    failUnless(is.null(o), "object should be null, but was: ", humanString(o))
}


checkShape <- function(table, rows = NULL, cols = NULL, rownames = NULL, colnames = NULL, index = NULL) {
    needs(rows="numeric|integer?", cols="numeric|integer?")
    assertFalse(all(nulls(rows, cols, rownames, colnames, index)), "must have at least one thing to check!")
    checkSameIfExpectedNotNull(rows, nrow(table))
    checkSameIfExpectedNotNull(cols, ncol(table))
    checkSameIfExpectedNotNull(rownames, rownames(table))
    checkSameIfExpectedNotNull(colnames, colnames(table))
    checkSameIfExpectedNotNull(index, index(table))
}

checkSameIfExpectedNotNull <- function(expected, actual) {
    if(!is.null(expected)) checkSame(expected, actual)
}

checkSameLooking <- function(a, b) {
    checkSame(paste(a), paste(b))
}

checkSame2 <- function(a, b) {
    if (isTRUE(all.equal(a, b))) return(TRUE)
    lhs = paste(a)
    rhs = paste(b)
    if (identical(lhs, rhs)) {
        print("EXPECTED EQUALS BUT WASNT:\n")
        cat(all.equal(a, b))
        out(a)
        if (inherits(a, "list")) { print(a) }
        out(b)
        if (inherits(b, "list")) { print(b) }
        assert(FALSE, "checkSame failed, but string representation matched - check stdout for dputs.")
    }
    assert(FALSE, squish("\n", lhs, "\nwas not all.equal to\n", rhs))
}

checkSameSet <- function(a, b) {
    checkLength(a, length(b))
    checkTrue(all(a %in% b))
}

checkIsType <- function(a, type) {
    needs(type = "character", a = type)
}

checkIsNotType <- function(a, type) {
    shouldBomb(checkIsType(a, type))
}
checkInherits <- function(a, classes) {
    needs(classes = "character")
    checkTrue(all(classes %in% class(a)), squish("classes was ", commaSep(class(a)), " not ", commaSep(classes)))
}

assertFalse <- function(condition, message = NULL) {
    needs(condition = "logical", message="character?")
    assert(!condition, message)
}

nulls <- function(...) {
   sapply(list(...), is.null)
}

checkFalse <- function(a, ...) {
    checkTrue(!a, ...)
}
    
checkNotEquals <- function(a, b) {
    assert(!isTRUE(all.equal(a,b)))
}

requireAllMatchFirst <- function(func, theList, failureMessage = "") {
    if(all(mapply(function(a, b) isTRUE(all.equal(func(a), func(b))), theList, theList[1]))) return()
    assert(FALSE, squish(
        failureMessage, 
        "\nfirst\n\t", 
        func(theList[[1]]),
        "\nmust match all according to '", 
        substitute(func),
        "' the mapped list was \n", 
        paste("\t", lapply(theList, func), collapse="\n")
    ))
}

unimplemented <- function() {
    throw("unimplemented")
}

asLines <- function(o) {
    if (is.null(o)) return("")
    squish(paste(o, collapse = "\n"), "\n")
}

commaSep <- function(o) {
    if(is.null(o)) return("")
    paste(sapply(listify(o), as.character), collapse = ", ")
}

the <- function(o, empty="") {
    if (length(o) == 0) fail("tried to take the only element of none: ", empty)
    if (length(o) > 1) fail("tried to take the only element of ", length(o), " elements: \n", commaSep(o))
    first(o)
}

second <- function(o) {
    assert(length(o) > 1, squish("tried to take the second of less than 2 elements"))
    o[[2]]
}

fourth <- function(o) {
    assert(length(o) > 3, squish("tried to take the fourth of less than 4 elements"))
    o[[4]]
}

fifth <- function(o) {
    assert(length(o) > 4, squish("tried to take the fifth of less than 5 elements"))
    o[[5]]
}

third <- function(o) {
    assert(length(o) > 2, squish("tried to take the third of less than 3 elements"))
    o[[3]]
}

first <- function(o) {
    assert(length(o) > 0, squish("tried to take the first of zero elements"))
    o[[1]]
}

last <- function(o) {
    assert(length(o) > 0, squish("tried to take the last of zero elements"))
    o[[length(o)]]
}

bombMissing <- function(o) {
   assert(!is.null(o))
   na.fail(o)
}

listify <- function(o) {
   if (inherits(o, "list")) return(o)
   list(o)
}

appendSlowly <- function(l, val) {
    needs(l="list")
    l[[length(l)+1]] <- val
    l
}

isDirectory <- function(dir) { file.exists(the(dir)) && file.info(dir)$isdir }
requireDirectory <- function(dir, message = squish("invalid directory")) { 
    needs(dir="character", message="character")
    assert(isDirectory(the(dir)), squish(message, ": ", dir))
}

requireNoDirectory <- function(dir, message = squish("directory unexpectedly found")) { 
    needs(dir="character", message="character")
    assert(!isDirectory(the(dir)), squish(message, ": ", dir))
}

requireFile <- function(filename, message = squish("nonexistant filename: ", filename)) { 
    needs(filename="character", message="character")
    assert(file.exists(filename), squish("file does not exist: ", filename))
}

matches <- function(pattern, target, fixed=FALSE) {
    needs(pattern = "character", target="character", fixed="logical")
    length(grep(the(pattern), the(target), perl=!fixed, fixed=fixed)) == 1
}

withRecovery <- function(expressionToRun, errorHandler) {
    result <- try( 
        withRestarts(
            withCallingHandlers(
                expressionToRun,
                error = function(e) {
                    .Internal(seterrmessage(paste("Error: ", conditionMessage(e), "\n", sep = "")[1]))
                    invokeRestart("reportError", e, sys.calls())
                }
            ),
            reportError = function(e, calls) errorHandler(e, calls)
        )
    )
    result
}


shouldBombMatching <- function(expr, msgRegex="provide regex to match against error message") {

    if(exists(".testLogger", envir=.GlobalEnv)) .testLogger$incrementCheckNum() 
    funcEnv <- environment() 
    res = withRecovery(
        expr, 
        function(e, calls) { 
            stackTrace <- lapply(calls, deparse)
            assign("stackTrace", stackTrace, envir = funcEnv)
            e
        } 
    )
    if (!inherits(res, "try-error") && !inherits(res, "error")) {
        if(exists(".testLogger", envir=.GlobalEnv)) .testLogger$setFailure() 
        stop(squish("unexpected success, cannot check error message against:\n\t", msgRegex))
    }

    msg <- geterrmessage()
    msg <- sub(".*= <environment>,  : \n\t", "", msg) # for now try this - it chops off the extra error msg snippet coming from dunno where.
    withRecoveryLocation <- findWithRecovery(stackTrace)
    if (withRecoveryLocation == 20)  # in test-runner.  remove test-runner gunk.
        stackTrace <- stackTrace[-c(1:18, 20:29, length(stackTrace))]
    else if (withRecoveryLocation > 0)
        stackTrace <- stackTrace[-c(withRecoveryLocation:(withRecoveryLocation+9), length(stackTrace))]
    tabSepStackTrace <- lapply(stackTrace, function(x) paste("\t", x, collapse="\n"))
    stackMess <- paste(1:length(stackTrace), ":", tabSepStackTrace, collapse="\n")
    checkMatches(msg, msgRegex, squish("caught exception at\n", stackMess, "\nbut with incorrect error message\n"))
    return(TRUE)
}

findWithRecovery <- function(stackTrace) {
    for(i in seq_along(stackTrace))
        if(matches("withRecovery", first(stackTrace[[i]])))
            return(i)
    -1
}

checkMatches <- function(msg, msgRegex, failure = "") {
    deformedMsg <- gsub("\n", "<CR>", msg)
    exact <- matches("^:|:!|!:", msgRegex)
    negated <- matches("^!|:!|!:", msgRegex)
    chopFirst <- function(s) substr(s, 2, nchar(s))
    if (negated) 
        msgRegex <- chopFirst(msgRegex)
    if (exact) 
        msgRegex <- chopFirst(msgRegex)
    matched <- matches(msgRegex, deformedMsg, fixed = exact)
    if (negated == matched) {
        message = squish(
            failure, 
            "EXPECTED to ", 
            ifelse(negated, "NOT ", ""), 
            ifelse(exact, "LITERALLY ", ""), 
            "match\n", 
            msgRegex, 
            "\nBUT GOT\n", 
            msg
        )
        if (!identical(msg, deformedMsg)) 
            message <- squish(
                message,
                "\nDEFORMED AS\n", 
                deformedMsg
            )
        fail(message)
    }
    return(TRUE)
}

rejectDuplicates <- function(items) {
    duplicates <- duplicated(items)
    if (any(duplicates))
        fail("duplicate item: ", commaSep(unique(items[duplicates])), "\ncomplete list\n", commaSep(items))
}

bombFunc <- function(msg) {
    function() assert(FALSE, msg)
}

checkOutput <- function(expected, expr) {
    output <- capture.output({ expr; cat('\n') })
    checkSame(expected, output)
}


ifElse <- function(isYes, yes, no) { 
    needs(isYes = "logical")
    checkLength(isYes, 1)
    if (isYes) yes else no
}

applyScaling <- function(items, weights) { 
    needs(weights="numeric")
    mapply(function(i, w) { i * w }, items, weights, SIMPLIFY=FALSE)
}

noWarnings <- function(expr) {
    oldWarn <- options("warn")
    options(warn = -1)
    result <- expr
    on.exit(options(oldWarn))
    result
}

noNames <- function(x) {
    names(x) <- NULL
    x
}

lazy <- function(target, expression, log = TRUE) {
    if(is.null(target)) {
        eval(parse(text=squish(deparse(substitute(target)), " <- ", deparse(substitute(expression)))), parent.frame())
        if(log)
        	cat(squish("LAZY: ", deparse(substitute(target)), " <- ", humanString(eval(substitute(target), parent.frame())), "\n"))
        return(eval(substitute(target), parent.frame()))
    }
    target
}
