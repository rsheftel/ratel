setConstructorS3("EuropeanOptionSpecific", function(...)
{
    extend(EuropeanOption(), "EuropeanOptionSpecific",
        .strike = NULL,              # strike
        .expiryDate = NULL,    # expiry in number of years
        .dividendAnn = NULL,     # dividend, annual compounding
        .isDefinedSpecific = TRUE
    )
})

setMethodS3("setTermsSpecific", "EuropeanOptionSpecific", function(this, strike=NULL, expiryDate=NULL, putCall=NULL, dividendAnn = NULL,...)
{
    this$setTerms(putCall = putCall)
    
    assert(!is.null(strike))
    assert(!is.null(expiryDate))
    assert(!is.null(dividendAnn))
   
    assert(is.numeric(strike) && strike > 0, paste(strike, "is not a valid strike for EuropeanOption obj."))
    this$.strike <- strike
    
        
    this$.expiryDate <- as.POSIXlt(expiryDate)
    assert(is.numeric(dividendAnn) && dividendAnn >= 0, paste(dividendAnn, "is not a valid dividend for EuropeanOption obj."))
    this$.dividendAnn = dividendAnn
        
    if (this$.isDefined == TRUE) 
    {
        this$.isDefinedSpecific = TRUE
        }
})

setMethodS3("summary", "EuropeanOptionSpecific", function(this,...)
{
    cat("Strike: ", this$.strike, "\n")
    cat("Expiration Date: ", as.character(this$.expiryDate), "\n")
    cat("Option Type: ", this$.putCall, "\n")
    cat("Ann. Dividend: ", this$.dividendAnn, "\n")
})    
    
    