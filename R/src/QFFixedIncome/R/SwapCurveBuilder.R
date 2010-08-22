constructor("SwapCurveBuilder", function(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = NULL,...)
{
    this <- extend(RObject(),"SwapCurveBuilder",
        .ccy = ccy,
        .holidayCenter = holidayCenter,
        .holidaySource = holidaySource,
        .holidayList = holidayList,
        .fincadDefinitionTab = read.csv(system.file("swap_curve_fincad_definition",paste(ccy,".csv",sep = ""), package = "QFFixedIncome"), sep = ",", header = TRUE)
    )
})



method("getSmoothedDiscountFactors","SwapCurveBuilder",function(this,curveDate,extendYears = 5,
    cashRates = NULL,cashTenors = NULL,swapRates = NULL,swapTenors = NULL
,...){
    dfCurve <- this$getDiscountFactors(curveDate,extendYears,cashRates,cashTenors,swapRates,swapTenors)
    sm <- DiscountFactorCurveSmoother(dfCurve = dfCurve[,-3])
    return(sm$getSmoothedDiscountFactorCurve())
})



method("getDiscountFactors","SwapCurveBuilder",function(this,curveDate,extendYears = 5,
    cashRates = NULL,cashTenors = NULL,
    swapRates = NULL,swapTenors = NULL
,...){

    needs(extendYears = "numeric",cashRates = "numeric?",cashTenors = "character?",swapRates = "numeric?",swapTenors = "character?")
    assert(extendYears>0);
    
    horizonYears <- max(characterToNumericTenor(swapTenors)) + extendYears    
    curveDate <- as.character(as.Date(curveDate))
    horizonDate <- as.character(as.Date(curveDate) + 365 * horizonYears)
    
    # get holiday data
    if(is.null(this$.holidayList))
        this$.holidayList <- as.character(as.Date(HolidayDataLoader$getHolidays(this$.holidaySource,this$.holidayCenter,curveDate,horizonDate)))
    
    # build fincad tables
    cash_crv <- 0; fut_crv <- 0; swp_crv <- 0
    if(!is.null(cashRates)){
        assert(all(cashRates>0));assert(NROW(cashRates)==NROW(cashTenors));
        cash_crv <- this$buildFincadTable(curveDate,cashRates,cashTenors,FALSE)
    }
    if(!is.null(swapRates)){
        assert(all(swapRates>0));assert(NROW(swapRates)==NROW(swapTenors));
        swp_crv <- this$buildFincadTable(curveDate,swapRates,swapTenors,TRUE)
    }
    
    if(!is.null(cashRates) && !is.null(swapRates))methodGen <- 2
    if(is.null(cashRates) && !is.null(swapRates))methodGen <- 4
    
    return(this$calcDiscountFactors(curveDate,methodGen,horizonYears,cash_crv,fut_crv = fut_crv,swp_crv = swp_crv))
})

method("buildFincadTable","SwapCurveBuilder",function(this,curveDate,rates,tenors,isSwapTable,...)
{   
    # next business date adjustment
    fincadTab <- this$.fincadDefinitionTab[match(tenors,as.character(this$.fincadDefinitionTab[,1])),]
    table <- data.frame(eff_date = NULL,term_date = NULL,rate = NULL,fix_freq = NULL,fix_acc = NULL,day_conv = NULL,use_point = NULL,check.rows = TRUE)
    
    for(i in 1:NROW(tenors)){
        effDate <- getFincadDateAdjust(curveDate,as.character(fincadTab[i,"effDateUnit"]),as.numeric(fincadTab[i,"effDateNumUnits"]),this$.holidayList,fincadTab[i,"effDateAdjRule"])
        matDate <- getFincadDateAdjust(effDate,as.character(fincadTab[i,"matDateUnit"]),as.numeric(fincadTab[i,"matDateNumUnits"]),this$.holidayList,fincadTab[i,"matDateAdjRule"])
        table <- rbind(table,data.frame(
            eff_date = effDate,term_date = matDate,rate = rates[i],fix_freq = fincadTab[i,"freq"],fix_acc = fincadTab[i,"acc"],use_point = fincadTab[i,"use"],
        check.rows = TRUE))
    }
    if(isSwapTable)table <- cbind(table[,c(1:5)],day_conv = 2,use_point = table[,6])
    return(table)
})

method("calcDiscountFactors","SwapCurveBuilder",function(this,curveDate,methodGen,horizonYears,cash_crv,fut_crv,swp_crv,...)
{   
    # some times, the Fincad function breaks when it tries to extend the df curve. In that case, the function returns
    # the df curve non extended
    
    callFincad <- function(min_years){
        fincad("aaSwap_crv3",
            d_v = curveDate,
            cash_crv = cash_crv,
            fut_crv = fut_crv,
            swp_crv = swp_crv,
            boot_swap = 5, # bootstrapping method set to quadratic forward rates
            boot_intrp = 3, # interpolation exponential from discount factors 
            fut_splice = 2, # futures gap method use next forward rate
            rate_basis = 1, # ouput rates set to be annual compounding rates
            acc_rate = 2, # output rates set to actual/360
            hl = this$.holidayList, # holiday list
            method_gen = methodGen,
            min_years = horizonYears,
            extend_method = 1, # method for extending the curve set to constant swap rates
            sprd = 0, # yield spread set to 0
            sprdtype = 1, # spread type set to absolute
            table_type = 3 # output table set to date/discount factor/spot rate
        )
    }
    result <- try(callFincad(horizonYears),TRUE)
    if(class(result)=="try-error")result <- callFincad(0)
    result <- data.frame(result)
    colnames(result) <- c("date","df","spot")
    result[,1] = fincad.to.POSIXct(result[,1])
    return(result)
})