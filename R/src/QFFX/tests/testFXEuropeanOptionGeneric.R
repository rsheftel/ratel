library("QFFX")

testFXEuropeanOptionGeneric <- function()
{
    
   
    expiry <- "2y"
    cross <- "usd/chf"
    quoteside <-"mid"
    
#   Set up forwards and calculate the at-the-money rate

    usdchf <- FXForwardGeneric(FXCurr$setByCross(cross),quoteside,"2y")

# check for bad inputs    
    
    shouldBomb(usdchfoption$setTermsFXGeneric("BusinessTime",expiry)) 
    shouldBomb(usdchfoption$setTermsFXGeneric(usdchf,"none"))
  
  
#check for good inputs
    
  
    usdchfoption <- FXEuropeanOptionGeneric(usdchf,expiry)
    
    checkEquals(usdchfoption$getFXForwardGeneric()$getFXCurrency()$over(),"usd")
    checkEquals(usdchfoption$getFXForwardGeneric()$getFXCurrency()$under(),"chf")
    checkEquals(usdchfoption$getExpiry(),expiry)
    checkEquals(usdchfoption$getFXForwardGeneric()$.quoteSide,quoteside)
    checkEquals(usdchfoption$getFXForwardGeneric()$getFXCurrency()$over(),usdchfoption$getFXCurrency()$over())
	checkEquals(usdchfoption$getFXForwardGeneric()$getFXCurrency()$under(),usdchfoption$getFXCurrency()$under())
}
    
