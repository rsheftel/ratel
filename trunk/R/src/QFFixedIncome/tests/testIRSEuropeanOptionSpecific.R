library(QFFixedIncome)

testIRSEuropeanOptionSpecific <- function()
{
    expiryDate <- "2007-11-14"
    effDate <- "2007-11-16"
    valueDate <- "2007-11-16"
    matDate <- "2017-11-16"
    notional<-100
    quoteSide = "mid"
    direction = "receive"
    currency = "usd"
    optionType = "call"
#   Set up forwards and calculate the at-the-money rate

    irsSpecific <- SpecificIRS()
    irsSpecific$setDefault(curerncy=currency,quoteSide=quoteSide,notional=notional,effDate = effDate, matDate=matDate,direction=direction)        
    irspricer <-IRSPricer(ccy = currency,holidayCenter = "nyb",holidaySource = "financialcalendar", holidayList = 0)
    
    curveDate <- "2006-11-14"
    extendYears <- 5
    cashRates = c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    cashTenors = c("1w","1m","2m","3m","4m","5m","6m","9m")   
    swapRates = c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    swapTenors = c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    builder <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    dfCurve <- builder$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors)
    
    test.valueDate = "2006-11-16"
    test.effDate = "2006-11-16"
    test.matDate = "2016-11-16"
    test.fixedCoupon = 0.046
    notional = 10000000
        
    result <- irspricer$getFincadPrice(test.valueDate,test.effDate,test.matDate,test.fixedCoupon,notional,dfCurve[,-3])
    checkSame(result["parSwap","swap"],test.fixedCoupon)
    
    
    effDate <- effDate
    matDate <- matDate
    fixedCoupon <- 4.0
    atmRate <- 0.046898
    result <- irspricer$getFincadPrice(valueDate,effDate,matDate,fixedCoupon,notional,dfCurve[,-3])
    checkSame(result["parSwap","swap"],atmRate)   

#check for good inputs
    
    irsoption <- IRSEuropeanOptionSpecific(irsSpecific,strike = atmRate,expiryDate,optionType)
    checkEquals(irsoption$getStrike(),atmRate)
    checkEquals(irsoption$getExpiryDate(),as.POSIXlt(expiryDate))
    checkEquals(irsoption$getType(),optionType)
    
}
    
