setConstructorS3("IRSEuropeanOptionGeneric", function(GenericIRS = NULL, expiry = NULL,...)
{
    this <- extend(EuropeanOption(), "IRSEuropeanOptionGeneric",
        .expiry = NULL,
        .GenericIRS = NULL
        
    )
    
    constructorNeeds(this,GenericIRS = "GenericIRS",expiry = "character")
        
    if(!inStaticConstructor(this))
    {
      this$.GenericIRS <- GenericIRS
      expiryList <- c("spot","1w","1m","2m","3m","6m","9m","1y","2y","3y","4y","5y","7y","10y")
      assert(any(expiry == expiryList),paste(expiry,"is not a valid expiry"))
      this$.expiry <- expiry
            
    }
    
    this
    
})

setMethodS3("getExpiry","IRSEuropeanOptionGeneric",function(this,...)
{
  return(this$.expiry)
})

setMethodS3("getGenericIRS","IRSEuropeanOptionGeneric",function(this,...)
{
  return(this$.GenericIRS)
})



setMethodS3("summary","IRSEuropeanOptionGeneric",function(this,...)
{
        cat("Currency:",(this$getGenericIRS())$.currency,"\n")
        cat("Expiry:",as.character(this$getExpiry()),"\n")
        cat("Tenor:",as.character(this$getGenericIRS()$.tenor),"\n")

    
})