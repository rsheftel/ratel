## Test file for the CDSPricer object
rm(list = ls())
library(QFCredit)

testCDSPricer.constructor <- function()
{
    this <- CDSPricer(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",flatCurve = TRUE)
    checkEquals(this$.ccy,"usd")
    checkEquals(this$.holidayCenter,"nyb")    
    checkEquals(this$.holidaySource,"financialcalendar")    
    checkEquals(this$.flatCurve,TRUE)        
    checkEquals(this$.holidaylist,NULL)        
    checkEquals(this$.acc,2)        
    checkEquals(this$.adjRule,2)            
    checkEquals(this$.unitSett,"d")  
    checkEquals(this$.numUnitsSett,1)      
    this <- CDSPricer(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",flatCurve = TRUE,holidayList = "2001-01-01")
    checkEquals(this$.holidayList,"2001-01-01")        
}

testCDSPricer.loadHolidayList <- function()
{
    this <- CDSPricer(holidayList = c("2001-01-01","2002-01-01","2003-01-01","2004-01-01"))
    checkEquals(this$.holidayList,c("2001-01-01","2002-01-01","2003-01-01","2004-01-01"))
    this$loadHolidayList(valueDate = "2001-06-01",matDate = "2003-06-01")
    checkEquals(this$.holidayList,as.Date(as.POSIXct(c("2002-01-01","2003-01-01"))))
}

testCDSPricer.getDiscountFactorTable <- function()
{
    swapCurveDate <- "2006-11-14"
    cashRates = c(0.03724,0.03772,0.03820,0.03847,0.03924,0.03968,0.04007,0.04089)
    cashTenors = c("1w","1m","2m","3m","4m","5m","6m","9m")   
    swapRates = c(0.0427,0.0432,0.0436,0.0439,0.0449,0.0460,0.0473,0.0485,0.0489,0.0491)
    swapTenors = c("2y","3y","4y","5y","7y","10y","15y","20y","30y","40y")
    dfCurve <- NULL

    this <- CDSPricer()
    result <- this$getDiscountFactorTable(swapCurveDate,dfCurve,swapRates,swapTenors,cashRates,cashTenors)
    target <- read.csv(system.file("testdata","testfileDiscountFactorCurveSmoother.csv", package = "QFFixedIncome"), sep = ",", header = FALSE)[,-3]
    colnames(target) <- c("date","df")
    colnames(result) <- c("date","df")
    
    target[,1] <- as.POSIXct(target[,1])
    target[,2] <- as.numeric(round(target[,2],6))
    result[,2] <- as.numeric(round(result[,2],6))
    #checkEquals(result,target)
}

testCDSPricer.getSingleNameCDSTable <- function()
{
    this <- CDSPricer()
    cdsCurveDate <- "2008-01-30"
    cdsSpreads <- c(0.05,0.06,0.07)
    cdsTenors <- characterToNumericTenor(c("6m","5y","10y"))
    result <- this$getSingleNameCDSTable(cdsCurveDate,cdsSpreads,cdsTenors)
    
    target <- data.frame(tenor = c(0.5,5,10),effDate = as.POSIXlt(c("2008-01-31","2008-01-31","2008-01-31")),matDate = as.POSIXlt(c("2008-09-20","2013-03-20","2018-03-20")),spread = c(0.05,0.06,0.07))
    checkEquals(target,result)
    
    cdsSpreads <- c(0.05,NA,0.07)
    result <- this$getSingleNameCDSTable(cdsCurveDate,cdsSpreads,cdsTenors)
    target <- target[-2,]
    assert(all(target==result))
}

testCDSPricer.flatCurveAdjustment <- function()
{
    this <- CDSPricer()
    cdsTable <- data.frame(effDate = as.POSIXlt(c("2008-01-31","2008-01-31","2008-01-31")),matDate = as.POSIXlt(c("2008-09-20","2013-03-20","2018-03-20")),spread = c(0.05,0.06,0.07))
    
    matDate <- "2018-03-20"
    result <- this$flatCurveAdjustment(cdsTable,matDate)
    target <- data.frame(effDate = as.POSIXlt(c("2008-01-31","2008-01-31","2008-01-31")),matDate = as.POSIXlt(c("2008-09-20","2013-03-20","2018-03-20")),spread = c(0.07,0.07,0.07))
    checkEquals(result,target)
    matDate <- "2015-09-20"
    result <- this$flatCurveAdjustment(cdsTable,matDate)
    target <- data.frame(effDate = as.POSIXlt(c("2008-01-31","2008-01-31","2008-01-31")),matDate = as.POSIXlt(c("2008-09-20","2013-03-20","2018-03-20")),spread = c(0.0650054764512596,0.0650054764512596,0.0650054764512596))
    checkEquals(result,target)
    matDate <- "2008-02-01"
    result <- this$flatCurveAdjustment(cdsTable,matDate)
    target <- data.frame(effDate = as.POSIXlt(c("2008-01-31","2008-01-31","2008-01-31")),matDate = as.POSIXlt(c("2008-09-20","2013-03-20","2018-03-20")),spread = c(0.05,0.05,0.05))
    checkEquals(result,target)
    matDate <- "2008-02-01"
    
    this$.flatCurve <- FALSE
    result <- this$flatCurveAdjustment(cdsTable,matDate)
    checkEquals(result,cdsTable)
}


testCDSPricer.getCDSTable <- function()
{
    # create cds table for single names
    
    cdsEffDates <- NULL
    cdsMatDates <- NULL
    cdsTableType <- "singleName"
    cdsCurveDate <- "2008-01-30"
    cdsSpreads <- c(0.05,0.06,0.07)
    cdsTenors <- c("6m","5y","10y")
    
    target <- data.frame(effDate = as.POSIXlt(c("2008-01-31","2008-01-31","2008-01-31")),matDate = as.POSIXlt(c("2008-09-20","2013-03-20","2018-03-20")),spread = c(0.05,0.06,0.07))
    this <- CDSPricer()
    result <- this$getCDSTable(cdsCurveDate,cdsSpreads,cdsTenors,cdsEffDates,cdsMatDates,cdsTableType)
    checkEquals(target,result)
    
    # no need to create cds table
    
    cdsEffDates <- target[,1]
    cdsMatDates <- target[,2]
    result <- this$getCDSTable(cdsCurveDate,cdsSpreads,cdsTenors,cdsEffDates,cdsMatDates,cdsTableType)
    checkEquals(target,result)
}

testCDSPricer.getPrice <- function()
{
# test getFincadPrice()
    
    # buy ATM
    direction = "buy"
    strike = 0.06
    notional = 10000000
    recovery = 0.4
    valueDate = "2008-01-31"
    effDate = "2008-01-31"
    matDate = "2013-03-20"
    upfrontPayment = NULL
    holidayList = "2001-01-01"
    
    cdsEffDates = c("2008-01-31","2008-01-31","2008-01-31")
    cdsMatDates = c("2008-09-20","2013-03-20","2018-03-20")
    cdsSpreads = c(0.05,0.06,0.07)
    
    swapRates = c(0.05,0.05,0.05)
    swapTenors = c("2y","5y","10y")
    swapCurveDate = "2008-01-30"
    cashRates = NULL
    cashTenors = NULL
    dfCurve = NULL
        
    cdsTable = data.frame(effDate = cdsEffDates,matDate = cdsMatDates,spread = cdsSpreads)
    this <- CDSPricer()
    dfTable =  this$getDiscountFactorTable(swapCurveDate,dfCurve,swapRates,swapTenors,cashRates,cashTenors)
    this = CDSPricer(holidayList = holidayList)
    result <- this$getFincadPrice(direction,strike,notional,recovery,valueDate, effDate,matDate,cdsTable,dfTable,upfrontPayment)
    
    target <- data.frame(c(0,0,0,0,0.06,0,3617.056198,0,-1666.666667))
    row.names(target) <- c("pnl","mtm","fair value","accrued","par spread","interest rate DVOI","cds spread DVOI","1% recovery impact","1 day theta")
    colnames(target) <- c("Fincad output")
    checkEquals(target,result)
    
    # sell ITM
    
    direction = "sell"
    valueDate = "2010-01-29"
    cdsEffDates = c("2010-01-29","2010-01-29","2010-01-29")
    cdsMatDates = c("2012-03-20","2013-03-20","2018-03-20")
    swapCurveDate = "2010-01-28"
    
    this = CDSPricer(holidayList = holidayList)
    cdsTable = data.frame(effDate = cdsEffDates,matDate = cdsMatDates,spread = cdsSpreads)
    dfTable =  this$getDiscountFactorTable(swapCurveDate,dfCurve,swapRates,swapTenors,cashRates,cashTenors)
    result <- this$getFincadPrice(direction,strike,notional,recovery,valueDate, effDate,matDate,cdsTable,dfTable,upfrontPayment)
    
    target <- data.frame(c(1214549.966019999934,-450.033979999993,66216.632687000005,66666.666666999998,0.060018, -0.875853,-2561.346451,0,1675.641498))
    row.names(target) <- c("pnl","mtm","fair value","accrued","par spread","interest rate DVOI","cds spread DVOI","1% recovery impact","1 day theta")
    colnames(target) <- c("Fincad output")
    checkEquals(target,result)
    
    # test upfront payment
    
    upfrontPayment = 1000000
    result <- this$getFincadPrice(direction,strike,notional,recovery,valueDate, effDate,matDate,cdsTable,dfTable,upfrontPayment = upfrontPayment)
    target["pnl",] = upfrontPayment + target["pnl",]
    checkEquals(target,result) 
    
# test getPrice()
    
    # case where we feed directly the cds effective and maturity dates
    
    this = CDSPricer(holidayList = holidayList,flatCurve = FALSE)
    cdsTenors = NULL
    cdsCurveDate = NULL
    cdsTableType = "singleName"
    
    result <- this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    )
    checkEquals(target,result)
    
    # case where we need to recalculate the cdsTable
    
    cdsTenors = c("2y","3y","8y")
    cdsEffDates = NULL
    cdsMatDates = NULL
    cdsCurveDate = "2010-01-28"
    result <- this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    )
    checkEquals(target,result)
    
    # Should bombs
    
    shouldBomb(this$getPrice(direction = "no", strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike = -2, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional = -100, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery = -0.2, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate = "2008-01-30", effDate, matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate = "2014-03-20", matDate,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate = TRUE,upfrontPayment,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment = -1000000,
        cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate = TRUE,cdsSpreads,cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate,cdsSpreads = c(0.05,-0.01,0.06),cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate,cdsSpreads = c(0.05,0.06),cdsTenors = cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate,cdsSpreads,cdsTenors = TermStructure$cds,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = cdsTableType
    ))
    shouldBomb(this$getPrice(direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment,
        cdsCurveDate,cdsSpreads,cdsTenors,cdsEffDates = cdsEffDates,cdsMatDates = cdsMatDates,
        swapCurveDate = swapCurveDate,dfCurve = dfCurve,swapRates = swapRates,swapTenors = swapTenors,cashRates = cashRates,cashTenors = cashTenors,
        cdsTableType = "unknown"
    ))    
}


testUpfrontToSpread <- function()
{	
	recovery <- 0.4
	valueDate <- as.POSIXct('2009-05-07')
	effDate <- as.POSIXct('2009-03-08')
	matDate <- as.POSIXct('2014-06-20')
	dfTable <- CDSPricer$getDiscountFactorTable(valueDate,dfCurve = NULL,swapRates = rep(as.numeric(2.67)/100,NROW(TermStructure$irs)),swapTenors = TermStructure$irs,cashRates = NULL,cashTenors = NULL)
	tenor <- '5y'
		
	spread <- CDSPricer$upfrontToSpread(
		0.04,strike = 0.01,recovery,valueDate,effDate,matDate,dfTable,tenor = '5y'
	)	
   	checkSame(spread,0.0189361572265625)
	
	spread <- CDSPricer$upfrontToSpread(
		0.2,strike = 0.05,recovery,valueDate,effDate,matDate,dfTable,tenor = '5y'
	)
	
	checkSame(spread,0.114372253417969)
	
	spread <- CDSPricer$upfrontToSpread(
		-.05,strike = 0.05,recovery,valueDate,effDate,matDate,dfTable,tenor = '5y'
	)
	
	spread <- CDSPricer$upfrontToSpread(
		-.03,strike = 0.01,recovery,valueDate,effDate,matDate,dfTable,tenor = '5y'
	)
	
	checkSame(spread,0.00370025634765625)	
}

test.getQuickPrice <- function(){

    holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar", financialCenter = "nyb")
    pricerSample <- CDSPricer()
       
    res <- pricerSample$getQuickPrice(
            tickerID = "gm_snrfor_usd_mr",valueDate = "2007-06-01",matDate = "2012-09-20",effDate = "2007-06-01",
            strike = 0.044,notional = 1000000,recovery = 0.4,direction = "buy",
            cdsSpread = 0.025,
            cdsTenor = 5,
            irsLevels = c(0.03,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05),
            irsTenors = c(1.5,3,4,5,7,10,15,20,30,40),
            outputChoice = "price"
    )
    checkSame(res,-81245.42)
    
    res <- pricerSample$getQuickPrice(
            tickerID = "gm_snrfor_usd_mr",valueDate = "2007-06-01",matDate = "2012-09-20",effDate = "2007-06-01",
            strike = 0.044,notional = 1000000,recovery = 0.4,direction = "buy",
            cdsSpread = 0.025,
            cdsTenor = 5,
            irsLevels = c(0.03,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05),
            irsTenors = c(1.5,3,4,5,7,10,15,20,30,40),
            outputChoice = "dv01"
    )
    checkSame(res,460.742262)
}

test.accruedSNAC <- function(){
	checkSame(CDSPricer$accruedSNAC(as.POSIXct('2009-03-20'),as.POSIXct('2009-04-21'),0.01,10000000),9166.66666666667)
	checkSame(CDSPricer$accruedSNAC(as.POSIXct('2009-03-20'),as.POSIXct('2009-03-20'),0.01,10000000),277.777777777778)
	shouldBomb(CDSPricer$accruedSNAC(as.POSIXct('2009-03-20'),as.POSIXct('2009-03-15'),0.01,10000000))
	shouldBomb(CDSPricer$accruedSNAC('2009-03-20',as.POSIXct('2009-03-21'),0.01,10000000))
	shouldBomb(CDSPricer$accruedSNAC(as.POSIXct('2009-03-20'),'2009-03-21',0.01,10000000))
	shouldBomb(CDSPricer$accruedSNAC(as.POSIXct('2009-03-20'),as.POSIXct('2009-03-21'),-0.01,10000000))
	shouldBomb(CDSPricer$accruedSNAC(as.POSIXct('2009-03-20'),as.POSIXct('2009-03-21'),0.01,-10000000))
}

test.accrued <- function(){
	checkSame(CDSPricer$accrued(as.POSIXct('2009-03-20'),as.POSIXct('2009-04-22'),0.01,10000000),9166.66666666667)
	checkSame(CDSPricer$accrued(as.POSIXct('2009-03-20'),as.POSIXct('2009-03-21'),0.01,10000000),277.777777777778)
}