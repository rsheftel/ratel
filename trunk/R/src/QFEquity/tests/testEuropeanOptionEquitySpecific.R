## Test file for the EuropeanOptionEquitySpecific object
library(QFEquity)

testEuropeanOptionEquitySpecific <- function()
{
    # tests bad inputs
    
    euroOptionEquitySpecific <- EuropeanOptionEquitySpecific()
    
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific())
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest, strike = "a", expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0))   
    
    eqTest <- Equity(ticker = "cah")    
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest, strike = "a", expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0))   
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest,strike = -1, expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0))
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest,strike = 50, expiryDate = 12, putCall = "call", dividendAnn = 0))
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest,strike = 50, expiryDate = "2007-12-31", putCall = "junk", dividendAnn = 0))
    shouldBomb(euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest,strike = 50, expiryDate = "2007-12-31", putCall = "call", dividendAnn = -1))
    

    # tests good inputs
        
    euroOptionEquitySpecific <- EuropeanOptionEquitySpecific()
    euroOptionEquitySpecific$setTermsSpecific(equityObj = eqTest, strike = 50, expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0.01)
        
    checkEquals(50,euroOptionEquitySpecific$.strike)
    checkEquals("2007-12-31",as.character(euroOptionEquitySpecific$.expiryDate))
    checkEquals("call",euroOptionEquitySpecific$.putCall)
    checkEquals(0.01,euroOptionEquitySpecific$.dividendAnn)
    checkEquals("cah", euroOptionEquitySpecific$.equityObj$.ticker)
    
    rm(euroOptionEquitySpecific)
    rm(eqTest)
}
