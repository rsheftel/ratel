## Test file for the BondIndexTri object
library(QFCredit)
library(GSFCore)

this <- BondIndexTri(useSwap = FALSE,sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00")
ticker <- "lehman_us_credit"
indexList <- c("lehman_us_credit_intermediate","lehman_us_credit_long")
startDate = "2008-01-28"
endDate = "2008-02-01"
rawTriDates <- as.POSIXct(c("2008-01-29 15:00:00","2008-01-30 15:00:00","2008-01-31 15:00:00","2008-02-01 15:00:00"))

test.BondIndexTri.updateAdjustedDailyTris <- function(){
    result <- this$updateAdjustedDailyTris(ticker,startDate = startDate,endDate = endDate,updateTSDB = FALSE)
    target <- getZooDataFrame(zoo(c(0.12956562566387514,0.05020918011627817,-0.25393643504967500,0.09039148701734906),rawTriDates))
    colnames(target) <- colnames(result)
    checkSame(result,target)
    
    shouldBomb(this$updateAdjustedDailyTris("junk",startDate = startDate,endDate = endDate,updateTSDB = FALSE))
    shouldBomb(this$updateAdjustedDailyTris(ticker,startDate = TRUE,endDate = endDate,updateTSDB = FALSE))    
    shouldBomb(this$updateAdjustedDailyTris(ticker,startDate = startDate,endDate = TRUE,updateTSDB = FALSE))        
}