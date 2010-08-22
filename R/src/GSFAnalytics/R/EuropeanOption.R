setConstructorS3("EuropeanOption", function(...)
{
    extend(RObject(), "EuropeanOption",
        .putCall = NULL,        # put/call flag
        .isDefined = FALSE
    )
})

setMethodS3("setTerms", "EuropeanOption", function(this, putCall = NULL,...)
{
    assert(!is.null(putCall))
    assert(any(putCall == c("put","call")), paste(putCall, "is not a valid option type for EuropeanOption obj."))
    this$.putCall <- putCall
    this$.isDefined <- TRUE
})

setMethodS3("summary", "EuropeanOption", function(this,...)
{
    cat("Option Type: ", this$.putCall, "\n")
})


    
    
    