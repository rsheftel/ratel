## Test file for the EuropeanOptionPricer object
library("GSFAnalytics")

testEuropeanOptionPricer <- function()
{
    # Test bad inputs
    
    pricerSample <- EuropeanOptionPricer()
    
    testOption <- EuropeanOptionSpecific()
    
    testOption$setTermsSpecific(strike = 50, expiryDate = "2007-12-31", putCall = "call", dividendAnn = 0.01)
   
    shouldBomb(pricerSample$getFincadPrice())
    shouldBomb(pricerSample$getFincadPrice(europeanOptionObj = testOption, underlyingPrice = -1, rate30360Semi = .05, volLn = .3, settleDate = "2005-12-31"))
    shouldBomb(pricerSample$getFincadPrice(europeanOptionObj = testOption, underlyingPrice = 45, rate30360Semi = -1, volLn = .3, settleDate = "2005-12-31"))
    shouldBomb(pricerSample$getFincadPrice(europeanOptionObj = testOption, underlyingPrice = 45, rate30360Semi = .05, volLn = -1, settleDate = "2005-12-31"))
#    shouldBomb(pricerSample$getFincadPrice(europeanOptionObj = testOption, underlyingPrice = 45, rate30360Semi = .05, volLn = .3, settleDate = "junk"))
    shouldBomb(pricerSample$getFincadPrice(europeanOptionObj = testOption, underlyingPrice = 45, rate30360Semi = .05, volLn = .3, settleDate = "2010-12-31"))    
    
    
    # Test good inputs
    
    target <- data.frame(c(6.932640929, 0.548472231, 0.020255955, -0.006785634, 0.246109858, 0.33786764, -0.488737631, 0.391822878))
    row.names(target) <- c("fair value", "delta", "gamma", "theta", "vega", "rho of rate", "rho of holding cost", "prob of exercise")
    colnames(target) <- c("Fincad output")
    
    pricerSample <- EuropeanOptionPricer()
    
    pricerSample$getFincadPrice(testOption, underlyingPrice = 45, rate30360Semi = 0.05, volLn = 0.3, settleDate = "2005-12-31")
    
    s <- data.frame(pricerSample$.output)
    checkEquals(round(target[[1]],9), round(s[[1]],9))
    checkEquals(0.1965991,round(pricerSample$.d1,7))
    checkEquals(-0.2276650, round(pricerSample$.d2,7))
    
    rm(pricerSample)
    rm(testOption)
    rm(target)
    rm(s)
}
