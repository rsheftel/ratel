## Test file for the TransformationDataLoader object
library("QFFX")

test.getFXCarry <- function()
{
    fxCurr <- FXCurr$setByCross("usd/jpy")
    # test bad inputs
    
    loaderSample <- FXSystemDataLoader()
    
    shouldBomb(loaderSample$getFXCarry())
    shouldBomb(loaderSample$getFXCarry(fxCurr = "badmojo",tenor = "2y",putCall = "call",source = "internal",startDate = "2007-01-01",endDate = "2007-01-03"))
    shouldBomb(loaderSample$getFXCarry(fxCurr = fxCurr,tenor = "5",putCall = "call" ,source = "internal",startDate = "2007-01-01",endDate = "2007-01-03"))   
    shouldBomb(loaderSample$getFXCarry(fxCurr = fxCurr,tenor = "5y",putCall = "BusinessTime",source = "internal",startDate = "2007-01-01",endDate = "2007-01-03"))    
    shouldBomb(loaderSample$getFXCarry(fxCurr = fxCurr,tenor = "5y",putCall = "call",source = "int",startDate = "2007-01-01",endDate = "2007-01-03"))    
    
   
    target <- getZooDataFrame(
        zoo(c(1.711467,1.694266,1.688362),order.by = as.POSIXct(c("2007-01-01","2007-01-02","2007-01-03")))
    )
    colnames(target) <- "payout_ratio"
    
    res <-  loaderSample$getFXCarry(fxCurr = fxCurr,tenor = "1y",putCall = "call",source = "internal",startDate = "2007-01-01",endDate = "2007-01-03")
    checkEquals(round(res,6),target)
    
}

