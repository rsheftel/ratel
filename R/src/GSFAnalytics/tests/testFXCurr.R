library("GSFAnalytics")
testFXCurr <- function()
{
    
#check for bad inputs

    shouldBomb(FXCurr$setByCross("usdchf"))
    shouldBomb(FXCurr$setByPairs("noidea","usd"))
    shouldBomb(FXCurr$setByPairs(3,4))

#incorrect precedence
   
    shouldBomb(FXCurr$setByPairs("usd","eur"))
        


#check for good inputs
    
    usdchf <- FXCurr$setByCross("usd/chf")
    checkEquals(usdchf$over(),"usd")
    checkEquals(usdchf$under(),"chf")
    checkEquals(usdchf$cross(),"usd/chf")
    
    eurusd <- FXCurr$setByPairs("eur","usd")
    checkEquals(eurusd$over(),"eur")
    checkEquals(eurusd$under(),"usd")
    checkEquals(eurusd$cross(),"eur/usd")
    

}
    
