## Test file for the IRSPricer object
library(QFFixedIncome)

test.IRSPricer.Constructor <- function()
{
    this <- IRSPricer(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    checkEquals(this$.ccy,"usd")
    checkEquals(this$.holidayCenter,"nyb")
    checkEquals(this$.holidaySource,"financialcalendar")
    checkEquals(this$.holidayList,NULL)
    
    # feed holiday list
    
    this <- IRSPricer(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = "2007-01-01")
    checkEquals(this$.holidayList,"2007-01-01")
}


test.IRSPricer.getFincadPrice <- function()
{
    # getFincadPrice
    
    this <- IRSPricer(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = 0)
  
    curveDate <- "2006-11-14"
    extendYears <- 5
    cashRates = c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    cashTenors = c("1w","1m","2m","3m","4m","5m","6m","9m")   
    swapRates = c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    swapTenors = c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    builder <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    dfCurve <- builder$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors)
    
    valueDate = "2006-11-16"
    effDate = "2006-11-16"
    matDate = "2016-11-16"
    fixedCoupon = 0.046
    notional = 10000000
        
    result <- this$getFincadPrice(valueDate,effDate,matDate,fixedCoupon,notional,dfCurve[,-3])
    target <- data.frame(t(matrix((c(0,10000000,-10000000,0,0,0,0,10000000,-10000000,0.046,0.046,0.046,0,0,0,0,0,0,
        -7927.885522,-7927.885522,0,7.927886,7.927886,0,75.504261,75.504261,0)),ncol = 9,nrow = 3)))
    rownames(target) <- c("marketPrice","accrued","cleanPrice","parSwap","parSpreadClean","parSpreadMarket","bp","dv01","convexity")
    colnames(target) <- c("swap","fixed","float") 
    checkEquals(target,result)
    
    # getPrice
    
    this <- IRSPricer(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = "2001-01-01")
    
    result <- this$getPrice(valueDate,effDate,matDate,fixedCoupon,notional,
        curveDate,cashRates,cashTenors,swapRates,swapTenors)
    checkEquals(target,result)
    
    # Should bombs
    
    shouldBomb(this$getPrice(1,effDate,matDate,fixedCoupon,notional,curveDate,cashRates,cashTenors,swapRates,swapTenors))
    shouldBomb(this$getPrice(valueDate,1,matDate,fixedCoupon,notional,curveDate,cashRates,cashTenors,swapRates,swapTenors))
    shouldBomb(this$getPrice(valueDate,effDate,1,fixedCoupon,notional,curveDate,cashRates,cashTenors,swapRates,swapTenors))    
    shouldBomb(this$getPrice(valueDate,effDate,matDate,-0.05,notional,curveDate,cashRates,cashTenors,swapRates,swapTenors))    
    shouldBomb(this$getPrice(valueDate,effDate,matDate,fixedCoupon,-1,curveDate,cashRates,cashTenors,swapRates,swapTenors))            
}

test.IRSPricer.getIRSPrice <- function()
{
    # set up IRSObj
    
    direction="receive"
    valueDate = "2006-11-16"
    effDate = "2006-11-16"
    matDate = "2016-11-16"
    fixedCoupon = 0.046
    notional = 10000000
    irsObj <- SpecificIRS()
    irsObj$setDefault(direction=direction,coupon=fixedCoupon,effDate=effDate,matDate=matDate,notional=notional)
    
#problematic.....Pricer needs to load union of holidays from     
    this <- IRSPricer(ccy = "usd",holidayCenter = irsObj$getRelevantFinancialCenters(),holidaySource = "financialcalendar",holidayList = 0)
  
    curveDate <- "2006-11-14"
    extendYears <- 5
    cashRates = c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    cashTenors = c("1w","1m","2m","3m","4m","5m","6m","9m")   
    swapRates = c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    swapTenors = c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    builder <- SwapCurveBuilder(ccy = "usd",holidayCenter = irsObj$getRelevantFinancialCenters(),holidaySource = "financialcalendar")
    dfCurve <- builder$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors)
    
    result <- this$getIRSPrice(irsObj,valueDate,dfCurve[,-3])
    target <- data.frame(t(matrix((c(0,10000000,-10000000,0,0,0,0,10000000,-10000000,0.046,0.046,0.046,0,0,0,0,0,0,
        -7927.885522,-7927.885522,0,7.927886,7.927886,0,75.504261,75.504261,0)),ncol = 9,nrow = 3)))
    rownames(target) <- c("marketPrice","accrued","cleanPrice","parSwap","parSpreadClean","parSpreadMarket","bp","dv01","convexity")
    colnames(target) <- c("swap","fixed","float") 
    checkEquals(target,result)
    
    
        
}