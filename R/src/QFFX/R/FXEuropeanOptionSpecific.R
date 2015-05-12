setConstructorS3("FXEuropeanOptionSpecific", function(FXForwardObj=NULL,strike=NULL,expiryDate=NULL,direction=NULL,putCall=NULL,...)
{
    this <- extend(EuropeanOptionSpecific(), "FXEuropeanOptionSpecific",
        .FXForwardObj = NULL,
        .direction = NULL,
        .settleDate = NULL
        
    )
    
    constructorNeeds(this,direction = "character")
        
    if(!inStaticConstructor(this))
    {
      this$setTermsSpecific(strike = strike,expiryDate = expiryDate,putCall = putCall,0)
      this$.FXForwardObj <- FXForwardObj
      assert(any(direction == c("buy","sell")),paste(direction,"is not buy/sell"))
      this$.direction <- direction
      this$.settleDate <- (this$.FXForwardObj)$getSettleDate()
    }
    this   
})

setMethodS3("initSpecificFromGeneric","FXEuropeanOptionSpecific",function(this,FXOptionGeneric,tradeDate,holidayList=NULL,settleDateList=NULL,...)
{
    # The purpose of this is to set up a holder "specific" option when passed a generic option...default values
    # will be given to all of the relevant variables that aren't defined.
    
    if (is.null(settleDateList))
    {
      dates.list <- FXSettleDates$getExpirySettleDate(FXOptionGeneric$getFXForwardGeneric()$getFXCurrency(), tradeDate,
        transaction = FXOptionGeneric$getExpiry(),holidayList = holidayList)
    }
    else dates.list <- settleDateList
    temp.FXForwardObj <- FXForwardSpecific$initSpecificFromGeneric(FXForwardGeneric = FXOptionGeneric$getFXForwardGeneric(),tradeDate = tradeDate, 
        holidayList = holidayList, settleDateList = settleDateList)
    strike <- 999
    expiry <- as.character(dates.list$expiryDate)
    return(FXEuropeanOptionSpecific(FXForwardObj = temp.FXForwardObj,strike = strike,
			expiryDate = expiry,direction = "buy",putCall = FXOptionGeneric$getPutCall()))
})

setMethodS3("checkGoodTradeDate","FXEuropeanOptionSpecific",function(this,tradeDate, holidayList=NULL,...)
{
  if (any(as.POSIXct(tradeDate)==as.POSIXct(holidayList))) return(FALSE)
  return(TRUE)
})

setMethodS3("getStrike","FXEuropeanOptionSpecific",function(this,...)
{
  return(this$.strike)
})

setMethodS3("getExpiryDate","FXEuropeanOptionSpecific",function(this,...)
{
  return(this$.expiryDate)
})

setMethodS3("getDirection","FXEuropeanOptionSpecific",function(this,...)
{
  return(this$.direction)
})

setMethodS3("getType","FXEuropeanOptionSpecific",function(this,...)
{
  return(this$.putCall)
})

setMethodS3("getFXForward","FXEuropeanOptionSpecific",function(this,...)
{
  return(this$.FXForwardObj)
})

setMethodS3("getSettleDate","FXEuropeanOptionSpecific",function(this,...)
{
  return(this$.settleDate)
})


setMethodS3("summary","FXEuropeanOptionSpecific",function(this,...)
{
        cat("Cross:",this$getFXForward()$getFXCurrency()$cross(),"\n")
        cat("Strike:",this$getStrike(),"\n")
        cat("Expiry:",as.character(this$getExpiryDate()),"\n")
        cat("Settle Date:",as.character(this$getSettleDate()),"\n")
        cat("Notional:",this$getFXForward()$getFXNotional()$getNotional(),"\n")
        cat("Direction:",this$getDirection(),"\n")
        cat("Type:",this$getType(),"\n")
    
})