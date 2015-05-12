setConstructorS3("FXDiscRate", function(ccy=NULL, tenor=NULL, discRate=NULL,...)
{
    this <- extend(RObject(), "FXDiscRate",
        .ccy = NULL,
        .tenor = NULL,
        .disc_rate = NULL,
        .quote_side = NULL,
        .isDefined = FALSE
    )
    
    constructorNeeds(this,ccy = "character", tenor = "character")
        
    if(!inStaticConstructor(this))
     {
      assert((nchar(ccy)==3),paste("Incorrect currency initialization in FXDiscRate"))
      this$.ccy <- ccy
      if (FXTenor$checkTenor(tenor)) this$.tenor <- tenor
      this$.isDefined <- TRUE
      this$.quote_side <- "mid"
     }
     
     this  
})

setMethodS3("setDiscRate","FXDiscRate",function(this,discRate,...)
{
    assert(any(class(discRate)=="numeric"),paste("Error in rate initialization in setDiscRate"))
    assert((discRate > 0),paste("Error in rate initialization in setDiscRate"))
    this$.discRate <- discRate    
})
    
setMethodS3("isDefined","FXDiscRate",function(this,...)
{
  return(this$.isDefined)
})

setMethodS3("ccy","FXDiscRate",function(this,...)
{
    if (this$isDefined()) return(this$.ccy)    
})

setMethodS3("tenor","FXDiscRate",function(this,...)
{
    if (this$isDefined()) return(this$.tenor)    
})

setMethodS3("discRate","FXDiscRate",function(this,...)
{
    if (this$isDefined()) return(this$.discRate)    
})

setMethodS3("quoteSide","FXDiscRate",function(this,...)
{
    if (this$isDefined()) return(this$.quote_side)
})
    
    