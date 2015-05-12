library("QFFX")
testFXForward <- function()
{
    
#check for bad inputs

    usdchf <- FXCurr$setByCross("usd/chf")

    shouldBomb(FXForward("usdchf","mid"))
    shouldBomb(FXForward(usdchf,"bidmidask"))

#check for good inputs
    
    usdjpy <- FXForward(FXCurr$setByCross("usd/jpy"),"bid")
    
    checkEquals(usdjpy$getFXCurrency()$over(),"usd")
    checkEquals(usdjpy$getFXCurrency()$under(),"jpy")
    checkEquals(usdjpy$getQuoteSide(),"bid")
   
    
    
    
}
    
