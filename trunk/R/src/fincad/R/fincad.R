

.First.lib <- function(libname, pkgname) 
{
    library.dynam("fincad", pkgname)
}

.initJava <- function(...)
{
    pkgname <- "fincad"
    library("rJava")
    .jinit(classpath = normalizePath(system.file("java", paste(pkgname, ".jar", sep=""), package = pkgname)), silent = FALSE, force.init = TRUE)
} 

.myJcall <- function(...)
{
    result <- .jcall(..., check = FALSE)
    ex <- .jgetEx(clear = TRUE)
    if(!is.null(ex)) {
        .jclear()
        throw("Exception caught in call to Java:", ex)
    }

    result
}

fincad.date <- function(dates)
{
    if(!inherits(dates, "POSIXlt"))
        dates <- as.POSIXlt(dates)
        
    mapply(
        function(year, mon, mday) {
            .process.fincad.result(.External("aaDateSerial_wrap", year, mon, mday))
        },
        dates$year + 1900,
        dates$mon + 1,
        dates$mday
    )
}

fincad.to.POSIXct <- function(dates) {
    as.POSIXct(sapply(dates, function(d) {
        year <- fincad("aaYear", d = d) 
        mon <- fincad("aaMonth", d = d) 
        day <- fincad("aaDay", d = d)

        paste(year, mon, day, sep = "-")
    }, USE.NAMES = FALSE))
}

.convert.dates <- function(arg.value)
{
    if(is.character(arg.value) || is.factor(arg.value) || is(arg.value, "POSIXct") || is(arg.value, "POSIXlt"))
        arg.value <- fincad.date(arg.value)
    arg.value
}

.convert.to.double.array <- function(arg.value)
{
    if(identical(class(arg.value), "data.frame"))
        .jcastToArray(.jarray(apply(data.frame(lapply(arg.value, .convert.dates)), 1, function(x) .jarray(as.numeric(x))), "[D"), "[[D")
    else
        .jcastToArray(.jarray(lapply(.convert.dates(arg.value), function(x) .jarray(as.numeric(x))), "[D"), "[[D")
}

fincad_java <- function(func, ...)
{
    func <- paste(func, "_java", sep = "")
    func.args <- fincad.funcs[[func]]
    call.args <- list(...)

    arg.list <- lapply(names(func.args), function(arg.name) 
    {
        arg.type <- func.args[[arg.name]]
        arg.value <- call.args[[arg.name]]

        if(is.null(arg.value))
            throw("Error:  arg ", arg.name, " not supplied to fincad call ", func)
            
        switch(arg.type,
            int = as.integer(.convert.dates(arg.value)),
            double = as.numeric(.convert.dates(arg.value)),
            double.table = .convert.to.double.array(arg.value),
            throw("Error:  Unable to handle argument type ", arg.type, "in fincad()\n")
        )
    })

    arg.list <- c("com/fincad/ShowAll", "[D", func, arg.list)

    .process.fincad.result(do.call(.myJcall, arg.list))
}

.process.fincad.result <- function(fincad.result) {
    switch(as.character(fincad.result[[1]]), 
        "1"  = fincad.result[[2]],
        "16" = throw("FinCAD Error: ", .myJcall("com/fincad/ShowAll", "S", "getErrorString", as.integer(fincad.result[[2]]))),
        "64" = matrix(fincad.result[4:length(fincad.result)], nrow = fincad.result[[2]], ncol = fincad.result[[3]], byrow = TRUE),
        throw("Error: Cannot understand FinCAD result:", fincad.result)
    )
}

.convert.to.double.table <- function(arg.value)
{
    if(!is.null(dim(arg.value))) {
        list(nrow(arg.value), ncol(arg.value), as.vector(t(sapply(arg.value, .convert.dates))))
    } else {
        list(1, length(arg.value), unlist(lapply(arg.value, .convert.dates)))
    }
}

fincadNoErrorHandling <- function(func, ...) {
    setFincadErrorHandling <- function(level) 
        .External("aaErrorHandlingEnable_wrap", level)
    setFincadErrorHandling(0)
    tryCatch(fincad(func, ...), finally = setFincadErrorHandling(1))
}

fincad <- function(func, ...)
{
    func.args <- fincad.funcs[[paste(func, "_java", sep = "")]]
    func <- paste(func, "_wrap", sep = "")
    call.args <- list(...)

    arg.list <- lapply(names(func.args), function(arg.name) 
    {
        arg.type <- func.args[[arg.name]]
        arg.value <- call.args[[arg.name]]

        if(is.null(arg.value))
            throw("Error:  arg ", arg.name, " not supplied to fincad call ", func)
            
        switch(arg.type,
            int = as.integer(.convert.dates(arg.value)),
            double = as.numeric(.convert.dates(arg.value)),
            double.table = .convert.to.double.table(arg.value),
            throw("Error:  Unable to handle argument type ", arg.type, "in fincad()\n")
        )
    })

    arg.list <- c(func, as.list(unlist(arg.list, recursive = FALSE)))

    fincad.result <- do.call(.External, arg.list)

    switch(as.character(fincad.result[[1]]), 
        "1"  = fincad.result[[2]],
        "16" = throw("FinCAD Error: ", .External("getErrorString_wrap", as.integer(fincad.result[[2]]))),
        "64" = matrix(fincad.result[4:length(fincad.result)], nrow = fincad.result[[2]], ncol = fincad.result[[3]], byrow = TRUE),
        throw("Error: Cannot understand FinCAD result:", fincad.result)
    )
}
