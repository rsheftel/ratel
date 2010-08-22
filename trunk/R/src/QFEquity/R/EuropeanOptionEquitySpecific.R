setConstructorS3("EuropeanOptionEquitySpecific", function(...)
{
    extend(EuropeanOptionSpecific(), "EuropeanOptionEquitySpecific",
        .equityObj = NULL,
        .isDefined = FALSE
    )
})

setMethodS3("setTermsSpecific", "EuropeanOptionEquitySpecific", function(this, equityObj, strike, expiryDate, putCall, dividendAnn = NULL, ...)
{
#   assert(class(equityObj) == "Equity", paste(equityObj, "is not an equityObj for EuropeanOptionEquity."))
    assert(is.numeric(strike) && strike > 0, paste(strike, "is not a valid strike for EuropeanOptionEquity."))
    assert(any(putCall == c("put","call")), paste(putCall, "is not a valid option type for EuropeanOptionEquity."))
    assert(is.numeric(dividendAnn) && dividendAnn >= 0, paste(dividendAnn, "is not a valid dividend for EuropeanOptionEquity."))
    
    this$.equityObj <- equityObj
    this$.strike <- strike
    this$.expiryDate <- as.POSIXlt(expiryDate)
    this$.putCall <- putCall
    this$.dividendAnn <- dividendAnn
    this$.isDefinedSpecific <- TRUE
})

setMethodS3("summary", "EuropeanOptionEquitySpecific", function(this,...)
{
    cat("Underlying: ", this$.equityObj$.ticker, "\n")
    cat("Strike: ", this$.strike, "\n")
    cat("Expiration Date: ", as.character(this$.expiryDate), "\n")
    cat("Option Type: ", this$.putCall, "\n")
    cat("Ann. Dividend: ", this$.dividendAnn, "\n")
})


    
    
    