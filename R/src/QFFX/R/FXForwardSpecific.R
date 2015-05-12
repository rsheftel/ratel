setConstructorS3("FXForwardSpecific", function(FXCurrency=NULL, quoteSide=NULL,rate=NULL,expiryDate=NULL,settleDate=NULL,fxNotional=NULL,direction=NULL,...)
{
    this <- extend(FXForward(FXCurrency = FXCurrency, quoteSide = quoteSide),"FXForwardSpecific",
        .rate = NULL,
        .settleDate = NULL,
        .expiryDate = NULL,
        .fxNotional = NULL,   
        .direction = NULL
    )
    constructorNeeds(this,rate = "numeric")
    if(!inStaticConstructor(this))
    {        
        posix.settleDate <- as.POSIXct(settleDate)
        this$.settleDate <- posix.settleDate
     
        posix.expiryDate <- as.POSIXct(expiryDate)
        this$.expiryDate <- posix.expiryDate
        assert((this$.expiryDate < this$.settleDate),paste("Spot Date must be before Settle Date"))
         
        this$setFXNotional(fxNotional)
    
        this$setDirection(direction)
        this$setRate(rate) 
     }
     this

})

setMethodS3("initSpecificFromGeneric","FXForwardSpecific",function(this,FXForwardGeneric,tradeDate,holidayList=NULL,settleDateList=NULL,rate = 999, fxNotional = FXNotional(100,"over"),direction = "buy",...)
{
    
    if (is.null(settleDateList))
    {
      dates.list <- FXSettleDates$getExpirySettleDate(FXForwardGeneric$getFXCurrency(),tradeDate, 
        transaction = FXForwardGeneric$getTenor(),holidayList = holidayList)
    }    
    # We will have a generic default of 999 for the rate, 100 for the notional, and a buy for the direction.
    else {
      dates.list <- settleDateList
      }
    currInstance <- FXForwardSpecific(FXForwardGeneric$getFXCurrency(), quoteSide = "mid", rate = rate, expiryDate = dates.list$expiryDate,
      settleDate = dates.list$settleDate, fxNotional = fxNotional, direction = direction)
    return(currInstance)
})

setMethodS3("getRate","FXForwardSpecific",function(this,...)
{
  return(this$.rate)
})

setMethodS3("setRate","FXForwardSpecific",function(this,rate,...)
{
  assert(rate > 0)
  this$.rate <- rate
})

setMethodS3("getSettleDate","FXForwardSpecific",function(this,...)
{
  return(this$.settleDate)
}) 

setMethodS3("getExpiryDate","FXForwardSpecific",function(this,...)
{
  return(this$.expiryDate)
}) 

setMethodS3("getFXNotional","FXForwardSpecific",function(this,...)
{
  return(this$.fxNotional)
})

setMethodS3("setFXNotional","FXForwardSpecific",function(this,fxNotional,...)
{
  assert(any(class(fxNotional)=="FXNotional"))
  this$.fxNotional <- fxNotional
})

setMethodS3("getDirection","FXForwardSpecific",function(this,...)
{
  return(this$.direction)
})
  
setMethodS3("setDirection","FXForwardSpecific",function(this,direction,...)
{
  assert(any(direction == c("buy","sell")),paste(direction,"is not buy/sell"))
  this$.direction <- direction
})

  
setMethodS3("summary","FXForwardSpecific",function(this,...)
{
        cat("Cross:",this$getFXCurrency()$cross(),"\n")
        cat("Rate:",this$getRate(),"\n")
        cat("Expiry:",as.character(this$getExpiryDate()),"\n")
        cat("Settle:",as.character(this$getSettleDate()),"\n")
        cat("Notional:",this$getFXNotional()$getNotional(),"\n")
        cat("Direction:",this$getDirection(),"\n")
        
})