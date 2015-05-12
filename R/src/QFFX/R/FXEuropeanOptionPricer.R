setConstructorS3("FXEuropeanOptionPricer", function(...)
{
    extend(RObject(), "FXEuropeanOptionPricer",
        .FXOptionObj = NULL,
        .valueDate = NULL,
        .spotRate = NULL,
        .volLn = NULL,
        .overRepoRate = NULL,
        .underRepoRate = NULL,
        .holidayDates = NULL,
        .spotSettleDate = NULL,
        .isDefined = FALSE
    )
})

setMethodS3("setTerms","FXEuropeanOptionPricer",function(this,FXOptionObj=NULL,valueDate=NULL,spotSettleDate=NULL,spotRate=NULL,volLn=NULL,overRepoRate=NULL,underRepoRate=NULL,holidayDates=NULL,...)
{
    if (!is.null(FXOptionObj)){
        assert(FXOptionObj$.isDefined==TRUE, paste(FXOptionObj," is not a defined FX Option Object"))
        this$.FXOptionObj <- FXOptionObj
        }
    
    if (!is.null(valueDate)){
        posix.valueDate <- as.POSIXct(valueDate)
        this$.valueDate <- posix.valueDate
        assert(this$.valueDate < (this$.FXOptionObj)$getExpiryDate(),paste("Value Date",this$.valueDate," must be less than expiry"))
        }
        
    if (!is.null(spotRate)){
        assert(class(spotRate)=="numeric" && spotRate>0)
        this$.spotRate <- spotRate
        }
        
    if (!is.null(volLn)){
        assert(class(volLn)=="numeric" && volLn>0)
        this$.volLn <- volLn
        }
    if (!is.null(overRepoRate)){
        assert(class(overRepoRate)=="numeric" && overRepoRate>=0)
        this$.overRepoRate <- overRepoRate
        }
    if (!is.null(underRepoRate)){
        assert(class(underRepoRate)=="numeric" && underRepoRate>=0)
        this$.underRepoRate <- underRepoRate
        }
    if (!is.null(holidayDates)){
      this$.holidayDates <- holidayDates
      }
    if (is.null(spotSettleDate)) {
      regDates <- FXSettleDates$getExpirySettleDate(this$.FXOptionObj$getFXForward()$getFXCurrency(),this$.valueDate,"spot",this$.holidayDates)
      this$.spotSettleDate <- as.POSIXct(regDates$settleDate)
    }
    else this$.spotSettleDate <- as.POSIXct(spotSettleDate)
    if ((!is.null(FXOptionObj))&&(!is.null(valueDate))&&(!is.null(spotRate))&&(!is.null(volLn))
		&&(!is.null(overRepoRate))&&(!is.null(underRepoRate))){
        this$.isDefined = TRUE
        }
})

setMethodS3("getPrice","FXEuropeanOptionPricer",function(this,overUnder="over",...)
{
    assert(any(overUnder==c("over","under")),paste("Incorrect over/under flag"))
    result <- this$getFincadPrice(this)
    
#This converts all of the results to the "over" currency.
    if (overUnder == "over")
    {
      result[1] <- result[1]/this$.spotRate
      result[3] <- result[3]*this$.spotRate
      result[4] <- result[4]/this$.spotRate
      result[5] <- result[5]/this$.spotRate
      result[6] <- result[6]/this$.spotRate
      result[7] <- result[7]/this$.spotRate
    }
	#In the case of "under", no need to convert as they are already in under currency.
    this$.output <- result
})
    
setMethodS3("getFincadPrice","FXEuropeanOptionPricer",function(this,...)
{

#the fincad price is always pricing things in terms of constant lower units, ie the strike is in terms of lower currency.

    assert(any(this$.FXOptionObj$getType() ==c("call","put")))
    if ( (this$.FXOptionObj)$getType() =="call" ) opt <- 1
    else opt <- 2   
    ratio <- this$getAdjustmentVol()
    #1 = Price, 2 = Delta, 3 = Gamma, 4 = Theta, 5 = Vega, 6= Rho (For), 7=Rho (Dom)
    res_vec <- c(1,2,3,4,5,6,7)
    result<- fincad("aaBSG",
            # underlying rate
            price_u = (this$.spotRate),
            # exercise rate
            ex = (this$.FXOptionObj)$getStrike(), 
            # expiry
            d_exp = (this$.FXOptionObj)$getSettleDate(),
            # value date
            d_v = this$.spotSettleDate,
            # vol
            vlt = this$.volLn*ratio,
            # rate of the under currency
            rate_ann = this$.underRepoRate,
            # rate of the over currency
            cost_hldg = this$.overRepoRate,
            # call or put
            option_type = opt, 
            stat = res_vec,
            # will assume repo rates are now actual/360 (annual compounding)
            acc_rate = 2,
            # same
            acc_cost_hldg = 2)
 	result    
})

setMethodS3("getAdjustmentVol","FXEuropeanOptionPricer",function(this,...)
{
  # Issue arises when the settle to settle forward has different number of days than the valueDate to expiry.
  # In this case, need to scale the spot (or the forward) to account for days differences.
  # Repo rates are actual/360,annualized
      settleDays <- getDaysBetween(this$.spotSettleDate,this$.FXOptionObj$getFXForward()$getSettleDate())
      expiryDays <- getDaysBetween(this$.valueDate,this$.FXOptionObj$getFXForward()$getExpiryDate())
      ratio <- sqrt(expiryDays/settleDays)
      return(ratio)
})
 
setMethodS3("summary","FXEuropeanOptionPricer",function(this,...)
{
    if(this$.isDefined){
        cat("Value Date:",as.character(this$.valueDate),"\n")
        cat("Expiry Date:",as.character(this$.FXOptionObj$getExpiryDate()),"\n")
        cat("Over Repo Rate:",this$.overRepoRate,"\n")
        cat("Under Repo Rate:", this$.underRepoRate,"\n")
        cat("Spot Exch Rate:",this$.spotRate,"\n")
        cat("Strike:",this$.FXOptionObj$getStrike(),"\n")
        cat("Vol:", this$.volLn,"\n")
    }
	else print("FXEuropeanOptionPricer not defined")
})






