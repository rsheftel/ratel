## Test file for the EuropeanOption object
library("GSFAnalytics")


testEuropeanOptionSpecific <- function()
{
    # tests bad inputs
    
    
    euroOption <- EuropeanOptionSpecific()
    
    shouldBomb(euroOption$setTermsSpecific())
    
    shouldBomb(euroOption$setTermsSpecific(strike = "a", expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0))   
    shouldBomb(euroOption$setTermsSpecific(strike = -1, expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0))
#    shouldBomb(euroOption$setTermsSpecific(strike = 50, expiryDate = 12, putCall = "call", dividendAnn = 0))
    shouldBomb(euroOption$setTermsSpecific(strike = 50, expiryDate = "2007-12-31", putCall = "junk", dividendAnn = 0))
    shouldBomb(euroOption$setTermsSpecific(strike = 50, expiryDate = "2007-12-31", putCall = "call", dividendAnn = -1))
    

    # tests good inputs
        
    euroOption <- EuropeanOptionSpecific()
    euroOption$setTermsSpecific(strike = 50, expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0.01)
    
    
    checkEquals(50,euroOption$.strike)
    checkEquals("2007-12-31",as.character(euroOption$.expiryDate))
    checkEquals("call",euroOption$.putCall)
    checkEquals(0.01,euroOption$.dividendAnn)
    
    rm(euroOption)

}
