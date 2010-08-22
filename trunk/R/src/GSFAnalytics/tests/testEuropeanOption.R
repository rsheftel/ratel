## Test file for the EuropeanOption object
library("GSFAnalytics")

testEuropeanOption <- function()
{
    # tests bad inputs
    
    euroOption <- EuropeanOption()
    
    shouldBomb(euroOption$setTerms())
    
    shouldBomb(euroOption$setTerms(putCall = "junk"))

    # tests good inputs
        
    euroOption <- EuropeanOption()
    euroOption$setTerms(putCall = "call")
        
    
    checkEquals("call",euroOption$.putCall)
    checkEquals(TRUE,euroOption$.isDefined)
    
    rm(euroOption)
}
