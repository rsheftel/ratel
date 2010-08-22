constructor("IRSPricer", function(ccy = "usd",holidayCenter = "nyb",holidaySource = "financialcalendar",holidayList = NULL,...)
{
    this <- extend(RObject(), "IRSPricer",
        .ccy = ccy,
        .holidayCenter = holidayCenter,
        .holidaySource = holidaySource,
        .holidayList = holidayList
    )
})

setMethodS3("getIRSPrice","IRSPricer",function(this,IRSObj,valueDate,dfCurve,extendYears=5,...)
{
    accrual_list <- list("act/365"=3, "act/360" = 2, "30/360" = 4, "act/act" = 8)
    frequency_list <- list("annual" = 1,"semi-annual" = 2,"quarterly"=3,"monthly"=4)
    bus_day_convention_list <- list("No Adjustment"=1,"Next Good"=2,"Previous Good"=3,"Modified Following"=4)
    direction_list <- list("receive"=1,"pay"=2)
    
    result <- data.frame(fincad("aaSwap3_p",
# valuation date        
        d_s = as.character(valueDate), # value date
# effective and maturity dates
        d_tbl =data.frame(as.character(IRSObj$getMaturityDate()),as.character(IRSObj$getEffectiveDate()),0,0,0,0), 
# direction of swap        
        swp_struct = as.numeric(direction_list[IRSObj$getDirection()]), 
# For now...FX Rate
        FX_spot = 1,
# principal exchanged at maturity
        asset = 3, 
# fixed coupon
        cpn = IRSObj$getCoupon(),
# notional 
        npa_fix = IRSObj$getNotional(), 
# fixed frequency
        freq_fix = as.numeric(frequency_list[IRSObj$getPayFreqFixed()]), 
# fixed accrual
        acc_fix = as.numeric(accrual_list[IRSObj$getDayCountFixed()]),
# holiday List
        hl_fix = this$.holidayList, 
# Fixed Business Day Convention        
        d_rul_fix = as.numeric(bus_day_convention_list[IRSObj$getBusDayConvFixed()]), 
# discount factors, fixed        
        df_disc_fix = dfCurve,
# float margin         
        mgn = 0, 
# scale factor of forward rates
        scale_factor = 1, 
# Notional        
        npa_flt = IRSObj$getNotional(),
# Frequency Float       
        freq_fl = as.numeric(frequency_list[IRSObj$getPayFreqFloat()]), 
# Float accrual
        acc_flt = as.numeric(accrual_list[IRSObj$getDayCountFloat()]), 
# Float holiday List
        hl_flt = this$.holidayList, 
# Float Business Day Convention
        d_rul_flt = as.numeric(bus_day_convention_list[IRSObj$getBusDayConvFloat()]),
# discount factors, Float        
        df_disc_flt = dfCurve, 
# accrual, float
        df_acc = dfCurve,
# accrual method for resets
        acc_rt = as.numeric(accrual_list[IRSObj$getDayCountFloat()]),
# number of business days prior to reset date that rate is fixed        
        r_mktdays = 0, 
# fixed reset rates
        r_table = 0, 
# Interpolation: exponential        
        intrp = 3, 
# output type: 7= swap, fixed,a dn floatin
        output_type = 7, # output_type
        stat = c(3,2,1,4,9,5,6,7,8) # statistic
    ))
    rownames(result) <- c("marketPrice","accrued","cleanPrice","parSwap","parSpreadClean","parSpreadMarket","bp","dv01","convexity")
    colnames(result) <- c("swap","fixed","float")
    return(round(result,6))
}) 




method("getPrice","IRSPricer",function(this,
    valueDate,effDate,matDate,fixedCoupon,notional,
    dfCurveDate,cashRates,cashTenors,swapRates,swapTenors,
    extendYears = 5,...)
{
    needs(fixedCoupon = "numeric",notional = "numeric",valueDate = "character|POSIXt?",effDate="character|POSIXt?",matDate="character|POSIXt?")
    valueDate <- as.Date(as.POSIXct(valueDate))
    effDate <- as.Date(as.POSIXct(effDate))
    matDate <- as.Date(as.POSIXct(matDate))
    
    # get holiday list
    
    if(is.null(this$.holidayList)){
        horizonDate <- valueDate + 365 * max(characterToNumericTenor(swapTenors)) + extendYears
        this$.holidayList <- as.Date(HolidayDataLoader$getHolidays(this$.holidaySource,this$.holidayCenter,valueDate,horizonDate))
    }
    
    # get smoothed discount curve
    
    builder <- SwapCurveBuilder(ccy = this$.ccy,holidayList = this$.holidayList)
    dfCurve <- builder$getSmoothedDiscountFactors(dfCurveDate,extendYears = extendYears,cashRates = cashRates,cashTenors = cashTenors,swapRates  = swapRates,swapTenors = swapTenors)[,-3]
    
    result <- this$getFincadPrice(valueDate,effDate,matDate,fixedCoupon,notional,dfCurve)
    return(result)
})

method("getFincadPrice","IRSPricer",function(this,valueDate,effDate,matDate,fixedCoupon,notional,dfCurve,...)
{
      result <- data.frame(fincad("aaSwap3_p",
        d_s = as.character(valueDate), # value date
        d_tbl =data.frame(as.character(matDate),as.character(effDate),0,0,0,0), # eff/mat dates
        swp_struct = 1, # receive fixed
        FX_spot = 1,
        asset = 3, # principal exchanged at maturity
        cpn = fixedCoupon, # fixed coupon
        npa_fix = notional, # notional
        freq_fix = 2, # fixed semi-annual
        acc_fix = 4, # fixed accrues 30/360 (ISDA)
        hl_fix = this$.holidayList, # fixed holiday list
        d_rul_fix = 4, # fixed: modified following business day
        df_disc_fix = dfCurve, # discount factor curve - fixed leg - discounting
        mgn = 0, # float margin
        scale_factor = 1, # scale factor of forward rates
        npa_flt = notional, # notional
        freq_fl = 3, # float quaterly
        acc_flt = 2, # float act/360
        hl_flt = this$.holidayList, # fixed holiday list
        d_rul_flt = 4, # float modified following business day
        df_disc_flt = dfCurve, # float df curve
        df_acc = dfCurve, # float df curve accruing
        acc_rt = 2, # actual/360
        r_mktdays = 0, # number of business days prior to reset date that rate is fixed
        r_table = 0, # fixed reset rates
        intrp = 3, # interpolation method: exponential
        output_type = 7, # output_type
        stat = c(3,2,1,4,9,5,6,7,8) # statistic
    ))
    rownames(result) <- c("marketPrice","accrued","cleanPrice","parSwap","parSpreadClean","parSpreadMarket","bp","dv01","convexity")
    colnames(result) <- c("swap","fixed","float")
    return(round(result,6))
})