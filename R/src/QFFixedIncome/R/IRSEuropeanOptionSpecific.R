setConstructorS3("IRSEuropeanOptionSpecific", function(IRSObj=NULL,strike=NULL,expiryDate=NULL,putCall=NULL,...)
{
    this <- extend(EuropeanOptionSpecific(), "IRSEuropeanOptionSpecific",
        .IRSObj = NULL
    )
    
    if(!inStaticConstructor(this))
    {
      this$setTermsSpecific(strike = strike,expiryDate = expiryDate,putCall = putCall,0)
      this$.IRSObj <- IRSObj
      this
    }
    this   
})

#setMethodS3("initSpecificFromGeneric","IRSEuropeanOptionSpecific",function(this,IRSGeneric,putCall,tradeDate,holidayList=NULL,settleDateList=NULL,...)
#{
#    # The purpose of this is to set up a holder "specific" option when passed a generic option...default values
#    # will be given to all of the relevant variables that aren't defined.
    
#    if (is.null(settleDateList))
#    {
#      dates.list <- FXSettleDates$getExpirySettleDate(FXOptionGeneric$getFXForwardGeneric()$getFXCurrency(), tradeDate,
#        transaction = FXOptionGeneric$getExpiry(),holidayList = holidayList)
#    }
#    else
#    {
#      dates.list <- settleDateList
#    }
    
#    temp.IRSObj <- SpecificIRS$initSpecificFromGeneric(GenericIRS = FXOptionGeneric$getFXForwardGeneric(),tradeDate = tradeDate, 
#        holidayList = holidayList, settleDateList = settleDateList)
#   
#    strike <- 999
    
#    expiry<- as.character(dates.list$expiryDate)
    
#    temp.instance <- IRSEuropeanOptionSpecific(FXForwardObj = temp.FXForwardObj,strike = strike,expiryDate = expiry,direction = "buy",putCall = putCall)
#    return(temp.instance)
#})

setMethodS3("checkGoodTradeDate","IRSEuropeanOptionSpecific",function(this,tradeDate, holidayList=NULL,...)
{
  if (any(as.POSIXct(tradeDate)==as.POSIXct(holidayList))) return(FALSE)
  return(TRUE)
})

setMethodS3("getStrike","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.strike)
})

setMethodS3("getExpiryDate","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.expiryDate)
})

setMethodS3("getDirection","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.direction)
})

setMethodS3("getType","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.putCall)
})

setMethodS3("getIRS","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.IRSObj)
})

setMethodS3("getEffDate","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.IRSObj$.effDate)
})

setMethodS3("getMaturityDate","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.IRSObj$.matDate)
})

setMethodS3("getNotional","IRSEuropeanOptionSpecific",function(this,...)
{
  return(this$.IRSObj$.notional)
})

setMethodS3("summary","IRSEuropeanOptionSpecific",function(this,...)
{
        cat("Strike:",this$getStrike(),"\n")
        cat("Expiry:",as.character(this$getExpiryDate()),"\n")
        cat("Effective Date:",as.character(this$getEffDate()),"\n")
        cat("Maturity Date:",as.character(this$getMaturityDate()),"\n")
        cat("Notional:",this$getIRS()$.notional,"\n")
        cat("Direction:",this$getDirection(),"\n")
        cat("Type:",this$getType(),"\n")
    
})