setConstructorS3("FXTenor", function(...)
{
    extend(RObject(), "FXTenor",

        .tenor = NULL
    )
})

setMethodS3("checkTenor","FXTenor",function(this,tenor=NULL,...)
{
    this <- FXTenor()
    if(!is.null(tenor)){
            tenorList <- this$getTenors()
            assert(any(tenor == tenorList),paste(tenor,"is not a valid tenor"))
            this$.tenor <- tenor
            return(TRUE)
            }
     return(FALSE)       
        
})

setMethodS3("getTenors","FXTenor",function(this,...)
{
  this <- FXTenor()
  tenorList<- c("spot","1w","1m","2m","3m","6m","9m","1y","2y","3y","4y","5y","7y","10y")
  return(tenorList)
})

  