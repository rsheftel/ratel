##
##
## Our convention is that the Over/Under quote refers to the value of one unit of the Over
## currency in terms of the Under. In the future, we will change this to have a flag on the quotation.
##
##
## All prices and notionals to be in terms of the Over currency....
##
##


setConstructorS3("FXForward", function(FXCurrency=NULL, quoteSide=NULL,...)
{
    this <- extend(RObject(), "FXForward",
        .FXCurrency = NULL,
        .quoteSide = "mid"
    )
    
##This is a workaround for the static constructor issue

    if(is.null(FXCurrency) && is.null(quoteSide)) return(this)
    
    constructorNeeds(this,FXCurrency = "FXCurr")
        
    if(!inStaticConstructor(this))
    {
      quoteList <- c("bid","mid","ask")
      assert(any(quoteSide == quoteList),paste(quoteSide,"is not a valid quoteside"))
      this$.FXCurrency <- FXCurrency
      this$.quoteSide <- quoteSide
    }
    
    this    
})

setMethodS3("getFXCurrency","FXForward",function(this,...)
{
  return(this$.FXCurrency)
})

setMethodS3("getQuoteSide","FXForward",function(this,...)
{
  return(this$.quoteSide)
})


setMethodS3("summary","FXForward",function(this,...)
{
 
        cat("Cross:",this$getFXCurrency()$cross(),"\n")
})