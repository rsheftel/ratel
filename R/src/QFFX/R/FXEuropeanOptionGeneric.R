setConstructorS3("FXEuropeanOptionGeneric", function(FXForwardGeneric = NULL, expiry = NULL, putCall = NULL,...)
{
    this <- extend(EuropeanOption(), "FXEuropeanOptionGeneric",
        .expiry = NULL,
        .FXForwardGeneric = NULL
        
    )
    
    constructorNeeds(this,FXForwardGeneric = "FXForwardGeneric",expiry = "character")
        
    if(!inStaticConstructor(this))
    {
      this$.FXForwardGeneric <- FXForwardGeneric
      expiryList <- c("spot","1w","1m","2m","3m","6m","9m","1y","2y","3y","4y","5y","7y","10y")
      assert(any(expiry == expiryList),paste(expiry,"is not a valid expiry"))
      this$.expiry <- expiry
      this$.putCall <- putCall      
    }
    
    this
    
})

setMethodS3("getExpiry","FXEuropeanOptionGeneric",function(this,...)
{
  return(this$.expiry)
})

setMethodS3("getFXForwardGeneric","FXEuropeanOptionGeneric",function(this,...)
{
  return(this$.FXForwardGeneric)
})

setMethodS3("getPutCall","FXEuropeanOptionGeneric",function(this,...)
	{
		return(this$.putCall)
	})

setMethodS3("getFXCurrency","FXEuropeanOptionGeneric",function(this,...)
	{
		return(this$getFXForwardGeneric()$getFXCurrency())
	})

setMethodS3("summary","FXEuropeanOptionGeneric",function(this,...)
{
        cat("Cross:",this$getFXForwardGeneric()$getFXCurrency()$cross(),"\n")
        cat("Expiry:",as.character(this$getExpiry()),"\n")
		cat("Put/Call:",as.character(this$getPutCall()),"\n")

    
})