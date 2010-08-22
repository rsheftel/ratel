setConstructorS3("IRSEuropeanOptionPricer", function(...)
{
    extend(RObject(), "IRSEuropeanOptionPricer",
        .IRSOptionObj = NULL,
        .valueDate = NULL,
        .dfCurve = NULL,
        .vol = NULL,
        .holidayDates = NULL,
        .valueDate = NULL,
        .isDefined = FALSE
    )
})

setMethodS3("setTerms","IRSEuropeanOptionPricer",function(this,IRSOptionObj=NULL,valueDate=NULL,dfCurve = dfCurve, vol=NULL,holidayDates=NULL,...)
{
    if (!is.null(IRSOptionObj)){
        assert(IRSOptionObj$.isDefined==TRUE, paste(IRSOptionObj," is not a defined IRS Option Object"))
        this$.IRSOptionObj <- IRSOptionObj
        }
    
    if (!is.null(valueDate)){
        posix.valueDate <- as.POSIXct(valueDate)
        this$.valueDate <- posix.valueDate
        assert(this$.valueDate < (this$.IRSOptionObj)$getExpiryDate(),paste("Value Date",this$.valueDate," must be less than expiry"))
        }
        
        
    if (!is.null(vol)){
        assert(class(vol)=="numeric" && vol>0)
        this$.vol <- vol
        }
        
    
    if (!is.null(holidayDates)){
      this$.holidayDates <- holidayDates
      }
    
    if (!is.null(dfCurve)) {
      this$.dfCurve <- dfCurve
      }
 
    if ((!is.null(IRSOptionObj))&&(!is.null(valueDate))&&(!is.null(vol))&&(!is.null(dfCurve))){
        this$.isDefined = TRUE
        }
})

setMethodS3("getPrice","IRSEuropeanOptionPricer",function(this,...)
{
    result <- this$getFincadPrice()
    # call Fincad pricer
    this$.output <- result
    
})
    
setMethodS3("getFincadPrice","IRSEuropeanOptionPricer",function(this,...)
{
    assert(any(this$.IRSOptionObj$getType() ==c("call","put")))
    
    option_type_list <- list("call"=1,"put"=2)
    accrual_list <- list("act/365"=3, "act/360" = 2, "30/360" = 4, "act/act" = 8)
    frequency_list <- list("annual" = 1,"semi-annual" = 2,"quarterly"=3,"monthly"=4)
    bus_day_convention_list <- list("No Adjustment"=1,"Next Good"=2,"Previous Good"=3,"Modified Following"=4)
    
    result<- data.frame(fincad("aaSwaption_Normal",
            # underlying rate
            d_v = (this$.valueDate),
            # expiry
            d_exp = (this$.IRSOptionObj)$getExpiryDate(),
            # effective date of swap
            d_e = (this$.IRSOptionObj)$getEffDate(),
            # maturity date of swap
            d_m = (this$.IRSOptionObj)$getMaturityDate(),
            # First payment date fixed
            d_f_fixed = 0,
            # First payment date float
            d_f_float = 0,
            # Next to last payment date fixed
            d_l_fixed = 0,
            # Next to last payment date float
            d_l_float = 0,
            # Notional
            princ = this$.IRSOptionObj$getNotional(),
            # Strike
            cpn = this$.IRSOptionObj$getStrike(),
            # Float Margin
            mgn = this$.IRSOptionObj$getIRS()$.spreadFloat,
            # Scale Factor
            scale_factor = 1,
            # exercise fee
            ex_fee = 0,
            # frequency of fixed leg
            freq_fixed = as.numeric(frequency_list[this$.IRSOptionObj$getIRS()$getPayFreqFixed()]),
            # frequency of floating leg
            freq_flt = as.numeric(frequency_list[this$.IRSOptionObj$getIRS()$getPayFreqFloat()]),
            # accrual for fixed 
            acc_fix = as.numeric(accrual_list[this$.IRSOptionObj$getIRS()$getDayCountFixed()]),
            # accrual for forward floating
            acc_float_rate = as.numeric(accrual_list[this$.IRSOptionObj$getIRS()$getDayCountFloat()]), 
            # accrual for floating payments
            acc_float_pay = as.numeric(accrual_list[this$.IRSOptionObj$getIRS()$getDayCountFloat()]), 
            # business day convention for fixed
            d_rul_fix =  as.numeric(bus_day_convention_list[this$.IRSOptionObj$getIRS()$getBusDayConvFixed()]),
            # business day convention for float
            d_rul_flt =  as.numeric(bus_day_convention_list[this$.IRSOptionObj$getIRS()$getBusDayConvFloat()]),
            # swaption type
            swpn = as.numeric(option_type_list[this$.IRSOptionObj$getType()]),
            # holiday List
            hl= this$.holidayDates,
            # discount factor for accruing rates
            df_crv_acc = this$.dfCurve,
            # discoutn factor for discounting
            df_crv_disc = this$.dfCurve,
            # interpolation, 3 = exponential
            intrp = 3,
            # volatility 
            vlt = this$.vol,
            #statistics
            stat = c( 1,2,3,4,5,6)
            ))
    rownames(result) <- c("Fair Value","Dollar Delta","Dollar Gamma","Dollar Theta","Dollar Vega","Forward Par Swap Rate")
    colnames(result) <- c("swaption")
    return(round(result,6))   
})

setMethodS3("summary","IRSEuropeanOptionPricer",function(this,...)
{
    if(this$.isDefined){
        cat("Value Date:",as.character(this$.valueDate),"\n")
        cat("Expiry Date:",as.character(this$.FXOptionObj$getExpiryDate()),"\n")
        cat("Over Repo Rate:",this$.overRepoRate,"\n")
        cat("Under Repo Rate:", this$.underRepoRate,"\n")
        cat("Spot Exch Rate:",this$.spotRate,"\n")
        cat("Strike:",this$.FXOptionObj$getStrike(),"\n")
        cat("Vol:", this$.volLn,"\n")
    }else{
        print("IRSEuropeanOptionPricer not defined")
    }
})






