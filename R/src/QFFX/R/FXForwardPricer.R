## All inputted repo rates are annual compunding, actual/360

setConstructorS3("FXForwardPricer", function(...)
{
    extend(RObject(), "FXForwardPricer",
        .FXForwardObj = NULL,
        .settleDate = NULL,
        .valueDate = NULL,
        .spotRate = NULL,
        .overRepoRate = NULL,
        .overDiscRate = NULL,
        .underRepoRate = NULL,
        .underDiscRate = NULL,
        .forwardRate = NULL,
        .FXForwardOverPrice = NULL,
        .FXForwardUnderPrice = NULL,
        .isDefined = NULL
    )
})

setMethodS3("setTerms","FXForwardPricer",function(this,FXForwardObj,valueDate = NULL,spotRate = NULL,overRepoRate = NULL
    ,underRepoRate = NULL,holidayDates=NULL,...)
{
    # test all of the characteristics
    assert(any(class(FXForwardObj)=="FXForwardSpecific"),paste(FXForwardObj,"is not a specific FXForward"))
    this$.FXForwardObj <- FXForwardObj
# We are adjusting the valueDate to be the settle date for a spot transaction initiated on the date    
    regDates <- FXSettleDates$getExpirySettleDate(FXForwardObj$getFXCurrency(),valueDate,"spot",holidayDates)
    this$.valueDate <- as.POSIXct(regDates$settleDate)
	if(!is.null(this$.valueDate) && !is.null((this$.FXForwardObj)$getSettleDate())){
#This handles the case when forward is cash already, ie past settle date
			if(this$.valueDate > ((this$.FXForwardObj)$getSettleDate())) this$.settleDate <- this$.valueDate
            else this$.settleDate <- this$.FXForwardObj$getSettleDate()
			}
    if(!is.null(spotRate)){
       assert(class(spotRate)=="numeric" && spotRate>0)
        this$.spotRate <- spotRate
        }
    if(!is.null(overRepoRate)){
        assert(class(overRepoRate)=="numeric" && overRepoRate >=0)
        this$.overRepoRate <- overRepoRate
        }         
    if(!is.null(underRepoRate)){
        assert(class(underRepoRate)=="numeric" && underRepoRate >=0)
        this$.underRepoRate <- underRepoRate
        }
    this$discfromRepo(this,...)
    this$.isDefined <- TRUE
})

setMethodS3("discfromRepo","FXForwardPricer",function(this,...)
{
    if (this$.valueDate <  this$.settleDate)
    {
      this$.overDiscRate<-as.numeric(fincad("aaConvertR_DF",
      d_e = this$.valueDate,
      d_t = this$.settleDate,
      rate_ann = this$.overRepoRate,
#1 = anuual compunding    
      rate_basis = 1,
#1 = actual/360
      acc_rate = 2))
    this$.underDiscRate<-as.numeric(fincad("aaConvertR_DF",
      d_e = this$.valueDate,
      d_t = this$.settleDate,
      rate_ann = this$.underRepoRate,
#1 = anuual compounding    
      rate_basis = 1,
#1 = actual/360
      acc_rate = 2))
     }
     else if (this$.valueDate >= this$.settleDate)
     {
      this$.overDiscRate <- 1.0
      this$.underDiscRate <- 1.0
     }
 })
    
setMethodS3("getFairForwardRate","FXForwardPricer",function(this,...)
{
# call Fincad forward rate calculator
    this$.forwardRate <- this$getFincadForward(this)
    return(this$.forwardRate)
})

setMethodS3("getPrice","FXForwardPricer",function(this,overUnderFlag,...)
{
  	assert(any(overUnderFlag==c("over","under")),paste("Over/Under Flag not correctly specified"))
 	if (is.null(this$.FXForwardOverPrice) || is.null(this$.FXForwardUnderPrice))
    {
      this$.forwardRate <- this$getFincadForward(this)
      if (this$.forwardRate>0) {
        if (this$.FXForwardObj$getFXNotional()$getNotionalFlag()=="over")
        {
           this$.FXForwardOverPrice <- (this$.forwardRate - this$.FXForwardObj$getRate())/(this$.forwardRate) * this$.overDiscRate 
           this$.FXForwardUnderPrice <- (this$.FXForwardOverPrice * this$.spotRate)
        }
        else 
        {
          this$.FXForwardOverPrice <- (1/this$.FXForwardObj$getRate() - 1/this$.forwardRate)* this$.overDiscRate
          this$.FXForwardUnderPrice <- this$.FXForwardOverPrice * this$.spotRate
        }
      }        
    }
    if (overUnderFlag=="over") return(this$.FXForwardOverPrice)
    else return(this$.FXForwardUnderPrice)
})

    
setMethodS3("getFincadForward","FXForwardPricer",function(this,...)
{
    # We are assuming that the repo rates are being supplied as Actual/360
    # This function also needs simple interest rates.
    if (this$.settleDate > this$.valueDate) {
		underRepoRate <-  fincad("aaConvert_cmpd2",d_e = this$.valueDate,d_t = this$.settleDate,
        rate_from = this$.underRepoRate,rate_basis_from = 1, acc_from = 2, rate_basis_to = 7, acc_to = 2)
      overRepoRate <-  fincad("aaConvert_cmpd2",d_e = this$.valueDate,d_t = this$.settleDate,
        rate_from = this$.overRepoRate,rate_basis_from = 1, acc_from = 2, rate_basis_to = 7, acc_to = 2)    
      
      result<- as.numeric(fincad("aaFXfwd",
        # spot rate
        FX_spot = this$.spotRate, 
        # forward repo rate for over currency 
        rate_dom = underRepoRate, 
        # forward repo rate for under currency
        rate_for = overRepoRate, 
        # valuation date
        d_v = this$.valueDate, 
        # forward settle date
        d_del = this$.settleDate, 
        # accrual types. acc_dom = acc_for = 1 = actual/360, simple interest rates
        acc_dom = 2, 
        acc_for = 2, 
        # result is in terms of forward rate
        stat = 2))
      return(result)
    }
    else if (as.POSIXct(this$.settleDate) <= as.POSIXct(this$.valueDate)) return(this$.spotRate)
})

setMethodS3("summary","FXForwardPricer",function(this,...)
{
    if(this$.isDefined){
            cat("Cross:",(this$.FXForwardObj)$getFXCurrency()$cross(),"\n")
            cat("Rate:",(this$.FXForwardObj)$getRate(),"\n")
            cat("Settle:",as.character(this$.settleDate),"\n")
            cat("Notional:",(this$.FXForwardObj)$getFXNotional()$getNotional(),"\n")
            cat("Direction:",(this$.FXForwardObj)$getDirection(),"\n")
    		cat("ValueDate:",as.character(this$.valueDate),"\n")
            cat("Spot Rate:",this$.spotRate,"\n")
            cat("Over Repo Rate:",this$.overRepoRate,"\n")
            cat("Under Repo Rate:",this$.underRepoRate,"\n")
            cat("Over Disc Fact:",this$.overDiscRate,"\n")
            cat("Under Repo Fact:",this$.underDiscRate,"\n")
            cat("Over Price:", this$.FXForwardOverPrice,"\n")
            cat("Under Price:", this$.FXForwardUnderPrice,"\n")
            cat("Fair forward:", this$.forwardRate,"\n")
            }
})






