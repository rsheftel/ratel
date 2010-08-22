library(QFFixedIncome)

testIRSEuropeanOptionPricer <- function()
{
    expiryDate <- "2007-11-14"
    effDate <- "2007-11-16"
    valueDate <- "2006-11-14"
    matDate <- "2017-11-16"
    notional <- 1000000
    quoteSide = "mid"
    direction = "receive"
    currency = "usd"
    optionType = "call"
    atmRate <- 0.046898
#   Set up forwards and calculate the at-the-money rate

    irsSpecific <- SpecificIRS()
    irsSpecific$setDefault(notional=notional,effDate = effDate, matDate=matDate,direction=direction,coupon=atmRate)        
    
    curveDate <- "2006-11-14"
    horizonDate <- "2046-11-14"
    extendYears <- 5
    cashRates = c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    cashTenors = c("1w","1m","2m","3m","4m","5m","6m","9m")   
    swapRates = c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    swapTenors = c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    builder <- SwapCurveBuilder(ccy = "usd",holidayCenter = irsSpecific$getRelevantFinancialCenters(),holidaySource = "financialcalendar")
    dfCurve <- builder$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors)
    
    holidayList <- as.character(as.Date(HolidayDataLoader$getHolidays(source="financialcalendar",irsSpecific$getRelevantFinancialCenters(),curveDate,horizonDate)))
    
    atmRate <- 0.046898
    vol = 0.01
#check for good inputs
    
    irsoption <- IRSEuropeanOptionSpecific(irsSpecific,strike = atmRate,expiryDate,optionType)
    irsEuropeanOptionPricer <- IRSEuropeanOptionPricer()
    irsEuropeanOptionPricer$setTerms(IRSOptionObj=irsoption,valueDate=valueDate,dfCurve = dfCurve[,-3], vol=vol, holidayDates=holidayList)
    result <- irsEuropeanOptionPricer$getPrice()
    target <- data.frame(t(matrix((c(30432.656740,381.412442,3.043170,-38.264693,1427.193585,0.046898)),ncol = 6,nrow =1)))
    rownames(target) <- c("Fair Value","Dollar Delta","Dollar Gamma","Dollar Theta","Dollar Vega","Forward Par Swap Rate")
    colnames(target) <- c("swaption") 
    checkEquals(target,result)
        
    
}
    
