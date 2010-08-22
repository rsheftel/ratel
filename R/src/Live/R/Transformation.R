constructor("Transformation", function(...) {
    this <- extend(RObject(), "Transformation", .disabled = FALSE, .initialized = FALSE)
    this
})

method("inputs", "Transformation", function(this, ...) {
    lazy(this$.inputs, this$.inputSeries(), log=FALSE)
})

method("outputs", "Transformation", function(this, ...) {
    lazy(this$.outputs, this$.outputSeries(), log=FALSE)
})

method("changed", "Transformation", function(this, inputs, name, ...) {
    inputs$fetch(this$inputs()[[name]])$changed()
})

method("value", "Transformation", function(this, inputs, name, ...) {
    inputs$fetch(this$inputs()[[name]])$value()
})

method("initializeOnce", "Transformation", function(this, quiet=FALSE, ...) {
    if(this$.disabled) return("DISABLED")
    if(this$.initialized) return("SUCCESS")

    initLocal <- function() { 
        res <- this$initialize()
        this$.initialized <- TRUE
        if(res != "SUCCESS") {
            cat("initialization failed:", res, "\n")
            this$disable()
        }
        res
    }
    res <- withRecovery( 
        { initLocal() }, 
        errorHandler = function(e, calls) {
            stackTrace <- lapply(calls, deparse)
            msg <- geterrmessage()
            msg <- sub(".*= <environment>,  : \n\t", "", msg) 
            begin <- the(match("initLocal()", stackTrace))
            stackTrace <- stackTrace[-c(1:begin, length(stackTrace))]
            tabSepStackTrace <- lapply(stackTrace, function(x) paste("\t", x, collapse="\n"))
            stackMess <- paste(1:length(stackTrace), ":", tabSepStackTrace, collapse="\n")
            if (quiet) stackMess <- ''
            cat("initialization BOMBED:", msg, stackMess, "\n")
            this$disable()
            "Init FAILED"
        }
    )
})

method("disable", "Transformation", function(this, ...) {
    this$.disabled <- TRUE
})

method("isDisabled", "Transformation", function(this, ...) {
    !is.null(this$.disabled) && this$.disabled
})

method("initialize", "Transformation", function(this, ...) {
    fail("implement in subclass")
})

method("errors", "Transformation", function(this, error, ...) {
    needs(error = "character")
    res <- lapply(this$outputs(), function(series) { 
        ifElse(series$isTimestamp(), series$now(), series$errorString(error)) 
    })
    names(res) <- NULL
    res
})

method("logInputs", "Transformation", function(this, inputs, quiet=FALSE, ...) {
    if (quiet) return(NULL)
    needs(inputs = "character")
    cat(squish("\nInputs:\n", paste("    ", inputs, collapse="\n")), "\n")
})

method("parseInputs", "Transformation", function(static, seriesWithValues, ...) {
    tryCatch(
        { SeriesDefinition$withValues(seriesWithValues) },
        error=function(e) {
            cat(squish("\nfailed parsing\n", asLines(seriesWithValues)))
            Map("SeriesDefinition", "character")
        }
    )
})

method("skipUpdate", "Transformation", function(this, inputs, ...) {
    fail("implement in subclass")
})

method("outputValues", "Transformation", function(this, inputs, ...) {
    fail("implement in subclass")
})

method("logOutputs", "Transformation", function(this, outputs, quiet=FALSE, ...) {
    if (quiet) return(NULL)
    cat(squish("\nOutputs:\n", paste(outputs, collapse="\n")), "\n")
})

method("update", "Transformation", function(this, seriesWithValues, quiet=FALSE, ...){
    needs(seriesWithValues="character|list(character)")
    seriesWithValues <- unlist(seriesWithValues)
    init <- this$initializeOnce(quiet=quiet)
    if (init == "DISABLED") return(list())
    else if (init != "SUCCESS") return(this$errors(init))
    this$logInputs(seriesWithValues, quiet = quiet)
    inputs <- Transformation$parseInputs(seriesWithValues)
    if (length(inputs$keys()) == 0) return(list())
    if (this$skipUpdate(inputs)) return(list())
    outs <- this$outputValues(inputs)
    this$logOutputs(outs, quiet = quiet)
    outs
})

