library("QFFX")
testFXNotional <- function()
{
    
#check for bad inputs

    shouldBomb(FXNotional(notional=-125,notionalFlag="over"))
    shouldBomb(FXNotional(notional=125,notionalFlag="BusinessTime"))


#check for good inputs
    
    fxNotional <- FXNotional()
    checkEquals(fxNotional$getNotional(),100)
    checkEquals(fxNotional$getNotionalFlag(),"over")
    
    fxNotional <- FXNotional(notional=100.3,"under")
    checkEquals(fxNotional$getNotional(),100.3)
    checkEquals(fxNotional$getNotionalFlag(),"under")
    

}
    
