library("QFFX")
testFXForwardGeneric <- function()
{
    
#check for bad inputs

    usdchf <- FXCurr$setByCross("usd/chf")
#bad inputs
    
    shouldBomb(FXForwardGeneric(FXCurrency = usdchf,quoteSide = "mid",tenor = "1.56y"))
    
#check for good inputs
    
    usdjpy <- FXCurr$setByCross("usd/jpy")
    usdjpycurve <- FXForwardGeneric(FXCurrency = usdjpy,quoteSide = "bid",tenor = "2y")
    
    checkEquals(usdjpycurve$getFXCurrency()$over(),"usd")
    checkEquals(usdjpycurve$getFXCurrency()$under(),"jpy")
    checkEquals(usdjpycurve$.quoteSide,"bid")
    checkEquals(usdjpycurve$getTenor(),"2y")
    
    
    
}
    
