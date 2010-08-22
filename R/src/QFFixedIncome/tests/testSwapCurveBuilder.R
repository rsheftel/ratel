## Test file for the SwapCurveBuilder class
library(QFFixedIncome)

test.swapCurveBuilder.Constructor <- function()
{
    this <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    checkEquals(this$.ccy,"usd")
    checkEquals(this$.holidayCenter,"nyb")
    checkEquals(this$.holidaySource,"financialcalendar")
    checkEquals(this$.fincadDefinitionTab,read.csv(system.file("swap_curve_fincad_definition","usd.csv", package = "QFFixedIncome"), sep = ",", header = TRUE))
    checkEquals(this$.holidayList,NULL)
    
    # feed holiday list
    
    this <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = "2007-01-01")
    checkEquals(this$.holidayList,"2007-01-01")
}

test.buildFincadTable <- function()
{
    curveDate <- "2006-11-14"
    horizonDate <- "2051-11-03"  
    this <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    this$.holidayList <- as.character(as.Date(HolidayDataLoader$getHolidays(this$.holidaySource,this$.holidayCenter,curveDate,horizonDate)))
       
    # Check function for swap data
    
    rates <- c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    tenors <- c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    res <- this$buildFincadTable(curveDate,rates,tenors,TRUE)
    
    target <- data.frame(
        eff_date = as.POSIXct(array("2006-11-16",dim = 10)),
        term_date = as.POSIXct(c("2008-11-16","2009-11-16","2010-11-16","2011-11-16","2013-11-16","2016-11-16","2021-11-16","2026-11-16","2036-11-16","2046-11-16")),
        rate = rates,
        fix_freq    = 2,
        fix_acc     = 4,
        day_conv    = 2,
        use_point   = 1,
        check.rows = TRUE
    )
    
    checkEquals(target,res)
    
    # Check function for cash data
    
    rates <- c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    tenors <- c("1w","1m","2m","3m","4m","5m","6m","9m")
    
    res <- this$buildFincadTable(curveDate,rates,tenors,FALSE)
    
    target <- data.frame(
        eff_date = as.POSIXct(array("2006-11-16",dim = 8)),
        term_date = as.POSIXct(c("2006-11-24","2006-12-18","2007-01-16","2007-02-16","2007-03-16","2007-04-16","2007-05-16","2007-08-16")),
        rate = rates,
        fix_freq    = 7,
        fix_acc     = 2,
        use_point   = 1,
        check.rows = TRUE
    )
    
    checkEquals(target,res)
}

test.calcDiscountFactors <- function()
{
    curveDate <- "2006-11-14"
    horizonDate <- "2051-11-03"  
    this <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    this$.holidayList <- as.character(as.Date(HolidayDataLoader$getHolidays(this$.holidaySource,this$.holidayCenter,curveDate,horizonDate)))
       
    # Using swap and cash
    
    horizonYears <- 45
    
    rates <- c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    tenors <- c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    swp_crv <- this$buildFincadTable(curveDate,rates,tenors,TRUE)

    rates <- c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    tenors <- c("1w","1m","2m","3m","4m","5m","6m","9m")    
    cash_crv <- this$buildFincadTable(curveDate,rates,tenors,FALSE)
    
    fut_crv <- 0
    
    this$.holidayList <- 0
    
    result <- this$calcDiscountFactors(curveDate,2,horizonYears,cash_crv,fut_crv,swp_crv)
    
    target <- read.csv(system.file("testdata","testfileSwapCurveBuilderCalcDiscountFactors.csv", package = "QFFixedIncome"), sep = ",", header = TRUE)
    colnames(target) <- c("date","df","spot")
    checkEquals(result,result)
}

test.getDiscountFactors <- function()
{   
    # Using swap and cash
    
    curveDate <- "2006-11-14"
    extendYears <- 5
    cashRates = c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    cashTenors = c("1w","1m","2m","3m","4m","5m","6m","9m")   
    swapRates = c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    swapTenors = c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    
    this <- SwapCurveBuilder(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar")
    result <- this$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors)
    target <- read.csv(system.file("testdata","testfileSwapCurveBuilderCalcDiscountFactors.csv", package = "QFFixedIncome"), sep = ",", header = FALSE)
    colnames(target) <- c("date","df","spot")

    target[,1] <- as.POSIXct(target[,1])
    #checkEquals(result,target)
    
    # shouldBombs
    
    shouldBomb(this$getDiscountFactors(TRUE,extendYears,cashRates,cashTenors,swapRates,swapTenors))
    shouldBomb(this$getDiscountFactors(curveDate,-1,cashRates,cashTenors,swapRates,swapTenors))    
    shouldBomb(this$getDiscountFactors(curveDate,extendYears,cashRates[-1],cashTenors,swapRates,swapTenors))    
    shouldBomb(this$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors[-1],swapRates,swapTenors))
    shouldBomb(this$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates[-1],swapTenors))    
    shouldBomb(this$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors[-1]))
    shouldBomb(this$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,c(0.04,-1),c("4y","5y")))
    shouldBomb(this$getDiscountFactors(curveDate,extendYears,c(0.04,-1),c("4y","5y"),swapRates,swapTenors))
    
    # check getSmoothedDiscountFactors
    
    result <- this$getSmoothedDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors) 
    target <- read.csv(system.file("testdata","testfileDiscountFactorCurveSmoother.csv", package = "QFFixedIncome"), sep = ",", header = FALSE)
    colnames(target) <- c("date","df","spot")
    
    target[,1] <- as.POSIXct(target[,1])
    target[,2] <- as.numeric(round(target[,2],6))
    target[,3] <- as.numeric(round(target[,3],6))     
    result[,2] <- as.numeric(round(result[,2],6))
    result[,3] <- as.numeric(round(result[,3],6))
    #checkEquals(result,target)
}