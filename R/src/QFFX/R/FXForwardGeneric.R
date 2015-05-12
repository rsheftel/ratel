setConstructorS3("FXForwardGeneric", function(FXCurrency=NULL,quoteSide=NULL,tenor=NULL,...)
{
    this <- extend(FXForward(FXCurrency = FXCurrency,quoteSide = quoteSide), "FXForwardGeneric",
        .tenor = NULL  
    )
    
    if(!inStaticConstructor(this))
    {
      if (FXTenor$checkTenor(tenor)) this$.tenor <- tenor 
    }
    this
        
})

setMethodS3("getTenor","FXForwardGeneric",function(this,...)
{
  return(this$.tenor)
})
    

setMethodS3("summary","FXForwardGeneric",function(this,...)
{
        cat("Cross:",this$getFXCurrency()$cross(),"\n")
        cat("Tenor:",this$getTenor(),"\n")
        
})