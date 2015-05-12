library("QFFX")
testFXDiscRate <- function()
{

    shouldBomb(FXDiscRate(ccy = "chf",tenor = "1.5"))
    shouldBomb(FXDiscRate(ccy = "BusinessTime",tenor="2y"))
    

    
    jpy <- FXDiscRate("jpy","2y")
    checkEquals(jpy$ccy(),"jpy")
    checkEquals(jpy$isDefined(),TRUE)
    checkEquals(jpy$tenor(),"2y")
    
    shouldBomb(jpy$setDiscRate(-1.567))
    shouldBomb(jpy$setDiscRate("BusinessTime"))
    
    jpy$setDiscRate(1.567)
    
    checkEquals(jpy$discRate(),1.567)
    
    
}
    
