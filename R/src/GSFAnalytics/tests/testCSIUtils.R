## Test file for the CSIUtils object
library("GSFAnalytics")

test.getCSIData <- function(){
    symbol <- "ES"
    startDate <- "2008-04-08"
    endDate <- "2008-04-09"
    
    # all fields
    result <- getCSIData(symbol,startDate,endDate)
    checkSame(dim(result),c(2,7))
    checkSame(index(result),as.POSIXct(c("2008-04-08","2008-04-09")))
    checkSame(colnames(result),c("Open","High","Low","Close","Total.Volume","Total.Open.Interest","Numeric.Delivery.Month"))
    
    # Close field
    result <- getCSIData(symbol,startDate,endDate,"Close")
    checkSame(dim(result),c(2,1))
    checkSame(index(result),as.POSIXct(c("2008-04-08","2008-04-09")))
    checkSame(colnames(result),"Close")
    
    # No data
    result <- getCSIData(symbol,endDate = "1980-01-01",field = "Close")
    checkSame(NULL,result)
    
    # ShouldBombs
    shouldBomb(getCSIData(symbol = "junk"))
    shouldBomb(getCSIData(symbol,startDate = TRUE))
    shouldBomb(getCSIData(symbol,fields = "junk"))    
}

test.getCSIFuturesMultiplier <- function(){
    symbol <- "ES"
    checkSame(getCSIFuturesMultiplier(symbol),50)
    shouldBomb(getCSIFuturesMultiplier("junk"))
}