constructor("CDSPricer", function(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = NULL,flatCurve = TRUE,...)
{
    library(QFFixedIncome)
    library(RUnit)
    this <- extend(RObject(), "CDSPricer",
        .ccy = ccy,
        .holidayCenter = holidayCenter,
        .holidaySource = holidaySource,
        .holidayList = holidayList,
        .flatCurve = flatCurve,
        .unitSett = "d",
        .numUnitsSett = 1,
        .acc = 2, # act/360
        .adjRule = 2 # next good business day convention
    )
    this
})

method("getQuickPrice","CDSPricer",function(this,
    tickerID = "not relevant",valueDate,effDate,matDate,strike,notional,recovery,direction,
    cdsSpread, cdsTenor, irsLevels, irsTenors, outputChoice = NULL,holidays = NULL,...)
{
    irsTenors <- numericToCharacterTenor(irsTenors)
    cdsTenor <- numericToCharacterTenor(cdsTenor)
    cdsCurveDate <- businessDaysAgo(1,valueDate,this$.holidayCenter)
    swapCurveDate <- cdsCurveDate
    
    if(!is.null(outputChoice))assert(outputChoice %in% c("price", "dv01"), "outputChoice in CDSPricer$getQuickPrice must be either price or dv01")        
    if(!exists("holidays") || shouldBomb(as.POSIXct(holidayData)))
        holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar", financialCenter = "nyb")        
    if(!exists("dfTable"))
        dfTable =  this$getDiscountFactorTable(valueDate,dfCurve = NULL,swapRates = irsLevels,swapTenors = irsTenors,cashRates = NULL,cashTenors = NULL)

    cdsTable <- this$getCDSTable(cdsCurveDate = cdsCurveDate,cdsSpreads = cdsSpread,cdsTenors = cdsTenor,cdsEffDates = NULL,cdsMatDates = NULL,cdsTableType = "singleName")
       
    output <- this$getFincadPrice(direction,strike,notional,recovery,valueDate, effDate,matDate,cdsTable,dfTable,upfrontPayment = NULL)
   
    if(!is.null(outputChoice)){
        if(outputChoice == "price") output['pnl',]
        else if(outputChoice == "dv01") output['cds spread DVOI',]
    }else{
        output 
    }
})

method("getPrice","CDSPricer",function(this, direction, strike, notional, recovery, valueDate, effDate, matDate,upfrontPayment = NULL,
    cdsCurveDate = NULL,cdsSpreads = NULL,cdsTenors = NULL,cdsEffDates = NULL,cdsMatDates = NULL,
    swapCurveDate = NULL,dfCurve = NULL,swapRates = NULL,swapTenors = NULL,cashRates = NULL,cashTenors = NULL,
    cdsTableType = "singleName",...)
{
    effDate <- as.POSIXct(effDate)
    valueDate <- as.POSIXct(valueDate);
    matDate <- as.POSIXct(matDate)
    assert(valueDate < matDate,"value date must be lower than maturity date")
    assert(valueDate >= effDate,"value date must be greater than effective date")
    
    cdsTable <- this$getCDSTable(cdsCurveDate,cdsSpreads,cdsTenors,cdsEffDates,cdsMatDates,cdsTableType)
    if(this$.flatCurve)cdsTable <- this$flatCurveAdjustment(cdsTable,matDate)
    
    dfTable <- this$getDiscountFactorTable(swapCurveDate,dfCurve,swapRates,swapTenors,cashRates,cashTenors)
    
    res <- this$getFincadPrice(direction,strike,notional,recovery,valueDate, effDate,matDate,cdsTable,dfTable,upfrontPayment)
    return(res)
})

method("getSingleNameCDSTable","CDSPricer",function(this,cdsCurveDate,cdsSpreads,cdsTenors,isSNAC = FALSE,...)
{    
    funcMatDates <- function(myDate,tenor) IMMDates$maturityFromEffective(myDate,tenor)	    
	funcEffDates <- function(myDate) getFincadDateAdjust(myDate,this$.unitSett,this$.numUnitsSett,this$.holidayList,this$.adjRule)
    effDates <- as.POSIXct(array(as.character(funcEffDates(cdsCurveDate)),dim = length(cdsTenors)))
    matDates <- as.POSIXct(t(data.frame(mapply(funcMatDates,effDates,cdsTenors,SIMPLIFY = FALSE))))        
	if(isSNAC){
		funcSNACEffDates <- function(myDate){Period('days',60)$rewind(myDate)}
		effDates <- as.POSIXct(array(as.character(funcSNACEffDates(cdsCurveDate)),dim = length(cdsTenors)))
	}
    result <- data.frame(tenor = cdsTenors,effDate = effDates,matDate = matDates,spread = cdsSpreads,row.names = seq_len(length(cdsTenors)))
    na.omit(result)
})

method("getCreditIndexTable","CDSPricer",function(this,cdsCurveDate,cdsSpreads,cdsTenors,...)
{                     
})

method("loadHolidayList","CDSPricer",function(this,valueDate,matDate,...){
    if(is.null(this$.holidayList))this$.holidayList <- HolidayDataLoader$getHolidays(source = this$.holidaySource,financialCenter = this$.holidayCenter)	
    this$.holidayList <- as.Date(as.POSIXct(this$.holidayList)) 
    this$.holidayList <- subset(this$.holidayList,this$.holidayList >= as.Date(as.POSIXct(valueDate)))
    this$.holidayList <- subset(this$.holidayList,this$.holidayList <= as.Date(as.POSIXct(matDate)))
})

method("getCDSTable","CDSPricer",function(this,cdsCurveDate,cdsSpreads,cdsTenors,cdsEffDates,cdsMatDates,cdsTableType,...){
    if(any(is.null(cdsSpreads),is.null(cdsEffDates),is.null(cdsMatDates))){
        cdsTenors <- characterToNumericTenor(cdsTenors)
        needs(cdsSpreads = "numeric"); assert(all(na.omit(cdsSpreads)>0)); assert(length(cdsSpreads)==length(cdsTenors))       
        if(cdsTableType == "singleName"){
            cdsTable <- this$getSingleNameCDSTable(cdsCurveDate,cdsSpreads,cdsTenors)[,-1]
        }else if(cdsTableType == "creditIndex"){
            cdsTable <- this$getCreditIndexTable(cdsCurveDate,cdsSpreads,cdsTenors)[,-1]
        }else{
            throw("Invalid cdsTableType in CDSPricer$getCDSTable")
        }
    }else{
        cdsTable <- data.frame(effDate=cdsEffDates,matDate = cdsMatDates,spread = cdsSpreads,row.names = seq_len(length(cdsEffDates)))
    }
    return(cdsTable)
})

method("flatCurveAdjustment","CDSPricer",function(this,cdsTable,matDate,flatCurve = this$.flatCurve,...){
    if(flatCurve && NROW(cdsTable)>1){
        y <- cdsTable[,3];x <- as.Date(cdsTable[,2])
        cdsTable[,3] <- approx(x,y,xout = as.Date(matDate),rule = 2)[2]
   	}
    return(cdsTable)
})

method("getDiscountFactorTable","CDSPricer",function(this,swapCurveDate,dfCurve,swapRates,swapTenors,cashRates,cashTenors,...){
    if(is.null(dfCurve)){
        builder <- SwapCurveBuilder(ccy = this$.ccy,holidayList = this$.holidayList)
        dfCurve <- builder$getSmoothedDiscountFactors(curveDate = swapCurveDate,cashRates = cashRates,cashTenors = cashTenors,swapRates = swapRates,swapTenors = swapTenors)[,-3]
    }
    return(dfCurve)
})

method("getFincadPrice","CDSPricer",function(this,direction,strike,notional,recovery,valueDate, effDate,matDate,cdsTable,dfTable,upfrontPayment = NULL,...)
{
    if(!is.null(upfrontPayment))assert(ifelse(direction == "sell",upfrontPayment > 0,upfrontPayment < 0),"invalid upfront payment")
    this$loadHolidayList(valueDate,matDate)
    result <- round(fincad("aaCDS",
            d_v = valueDate,
                # value date
            contra_d = data.frame(matDate, effDate, 0, 0, 2, 2, 1),
                # maturity date, effective date, no first coupon date, no last coupon date,
                # no effective date adjustment,no maturity date adjustment, coupon date generation method set to backward
            cpn_pr = strike,
                # coupon rate
            freq_pr = 3,
                # quaterly payment frequency
            pr_acc_type = 1,
                # pay accured interest upon default
            acc = this$.acc,
                # actual/360 accrual method
            d_rul = this$.adjRule,
                # next good business day convention
            pr_fix = 0,
                # no upfront payment
            ref_type = 1,
                # notional based contract
            ref_tbl = notional,
                # contract notional
            p_off = 1,
                # pay at default
            dp_type = 1,
                # compute from par cds spread curve
            dp_crv = cdsTable,
                # date/spread termstructure
            intrp_tb = c(2,2,2,2,2,3),
                # exponential interpolation of the cds curve, constant hazard rates,actual/360 accrual,
                # no effective date adjustment,no maturity date adjustment, cds spread date generation method set to IMM
            rate_recover = recovery,
                # recovery rate
            hl = this$.holidayList,
                # holiday list
            dfstd = dfTable,
                # interest rate data
            intrp = 3,
                # interpolation method for rates set to exponential
            pos = ifelse(direction=="buy",1,2),
                # buy/sell
            calc_para = c(1,1),
                # simplified calculation method, 1 bps shock for DVOI
            stat = data.frame(1:14)
                # we return all statistics
    ),6)

    row.names(result) <- c(
            "fair value","value of payoff","value of premium","accrued","fair value minus accrued","par spread",
            "interest rate DVOI","# days accrued","next cash flow date","previous cash flow date","number of remaining cash flows",
            "cds spread DVOI","1% recovery impact","1 day theta"
    )
    result <- data.frame(result[c("fair value","accrued","par spread","interest rate DVOI","cds spread DVOI","1% recovery impact","1 day theta"),])
    mtm <- result["fair value",] - result["accrued",]
    yearsAccrued <- as.numeric(as.Date(valueDate)-as.Date(effDate))/360
    carrySign <- ifelse(direction=="buy",-1,1)
    pnl <- mtm + carrySign * strike * notional * yearsAccrued
    if(!is.null(upfrontPayment))pnl <- pnl + upfrontPayment
    result <- rbind(pnl = pnl,mtm = mtm,result)
    colnames(result) <- c("Fincad output")
    result
})

method("checkAccruedInputs","CDSPricer",function(this,dateRef,valueDate,strike,notional,...){	
	needs(strike = 'numeric',notional = 'numeric',dateRef = 'POSIXt',valueDate = 'POSIXt')
	assert(valueDate >= dateRef)
	assert(strike > 0)
	assert(notional > 0)
})

method("accruedSNAC","CDSPricer",function(this,dateRef,valueDate,strike,notional,...){
	this$checkAccruedInputs(dateRef,valueDate,strike,notional)	
	strike * notional * (as.numeric(as.Date(valueDate)-as.Date(dateRef))+1)/360	
})

method("accrued","CDSPricer",function(this,dateRef,valueDate,strike,notional,...){
	this$checkAccruedInputs(dateRef,valueDate,strike,notional)	
	strike * notional * as.numeric(as.Date(valueDate)-as.Date(dateRef))/360	
})

method("upfrontToSpread","CDSPricer",function(this,
	upfrontPaymentPercent,strike,recovery,valueDate,effDate,matDate,dfTable,tenor = '5y'
,...){		
	upperBound <- 1
	lowerBound <- 0
	lastGuess <- 0
	currGuess <- (lowerBound + upperBound) / 2
	
	while(round(lastGuess,5) != round(currGuess,5)){
		cdsTable <- CDSPricer$getSingleNameCDSTable(valueDate,as.numeric(currGuess),characterToNumericTenor(tenor),isSNAC =TRUE)[,-1]	
		cdsTable <- CDSPricer$flatCurveAdjustment(cdsTable,matDate,flatCurve = TRUE)		
		output <- CDSPricer$getFincadPrice(
			direction = "buy",strike = strike,notional = 100,
			recovery = recovery,valueDate = valueDate,effDate = effDate,
			matDate = matDate,cdsTable = cdsTable,dfTable = dfTable
		)
		paymentDiff <- upfrontPaymentPercent - as.numeric(output['mtm',])/100    
		if(paymentDiff > 0) lowerBound <- currGuess
		if(paymentDiff < 0) upperBound <- currGuess
		lastGuess <- currGuess
		currGuess <- (lowerBound + upperBound) / 2
	}
	return(lastGuess)
})