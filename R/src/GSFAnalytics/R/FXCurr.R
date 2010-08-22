setConstructorS3("FXCurr", function(over=NULL, under=NULL, cross=NULL,...)
{
    this <- extend(RObject(), "FXCurr",
        .over = NULL,
        .under = NULL,
        .cross = NULL   
    )
    constructorNeeds(this,over = "character", under="character", cross = "character")
     if(!inStaticConstructor(this))
     {
     	assert((IsPrecedenceCorrectPairs(over,under)),paste(this$.cross," currencies were in initialzed with wrong precedence"))
      	this$.over <- over
      	this$.under <- under
      	this$.cross <- cross
      }
      this    
})

setMethodS3("setByCross","FXCurr",function(this,cross=NULL,...)
{
    temp.list <- ParseCross(cross = cross)
    CurrInstance <- FXCurr(temp.list$over, temp.list$under, temp.list$cross)
    return(CurrInstance)       
})
    

setMethodS3("setByPairs","FXCurr",function(this,over=NULL, under=NULL,...)
{
  needs(over="character")
  needs(under="character")
  assert(nchar(over)==3)
  assert(nchar(under)==3)
  cross = squish(over,"/",under)
  CurrInstance <- FXCurr(over,under,cross)
  return(CurrInstance)
})

setMethodS3("over","FXCurr",function(this,...)
{
  this$.over
})

setMethodS3("under","FXCurr",function(this,...)
{
  this$.under
})

setMethodS3("cross","FXCurr",function(this,...)
{
  this$.cross
})

