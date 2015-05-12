## Test file for the BondIndexSpread object
library(QFCredit)
library(GSFCore)

this <- BondIndexSpread(useSwaps = FALSE,sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00")
ticker <- "lehman_us_credit"
indexList <- c("lehman_us_credit_intermediate","lehman_us_credit_long")
startDate = "2008-01-28"
endDate = "2008-02-01"
rawTriDates <- as.POSIXct(c("2008-01-28 15:00:00","2008-01-29 15:00:00","2008-01-30 15:00:00","2008-01-31 15:00:00"))
rawTri <- zoo(c(1685.22,1688.21,1690.05,1700.51),rawTriDates)

test.BondIndexSpread.updateAdjustedSpreads <- function(){

    # several dates
    result <- this$updateAdjustedSpreads(ticker,startDate,endDate)
    target <- getZooDataFrame(zoo(c(2.629980948432493,2.624196872733370,2.609815637817640,2.642376057927275,2.610205971510244),
        as.POSIXct(c("2008-01-28 15:00:00","2008-01-29 15:00:00","2008-01-30 15:00:00","2008-01-31 15:00:00","2008-02-01 15:00:00"))
    ))
    colnames(target) <- colnames(result)
    checkEquals(result,target)
    # one date
    result <- this$updateAdjustedSpreads(ticker,endDate,endDate)
    target <- getZooDataFrame(target[5])
    colnames(target) <- colnames(result)
    checkEquals(result,target)
    
    # should  bombs
    shouldBomb(this$updateAdjustedSpreads("invalid",startDate,endDate))
    shouldBomb(this$updateAdjustedSpreads(ticker,TRUE,endDate))
    shouldBomb(this$updateAdjustedSpreads(ticker,startDate,TRUE))
}

test.BondIndexSpread.updateMarketValueWeightedSpreads <- function(){ 

    # several dates
    result <- this$updateMarketValueWeightedSpreads(indexList,"lehman_us_credit",startDate,"2008-01-31",FALSE) 
    target <- getZooDataFrame(structure(c(1.734056992393569, 1.730709437026378, 1.698296031269160, 
            1.700449322464452), index = structure(c(1201550400, 1201636800, 
            1201723200, 1201809600), class = c("POSIXt", "POSIXct")), class = "zoo"))
    checkEquals(result,target)
    # one date
    result <- this$updateMarketValueWeightedSpreads(indexList,"lehman_us_credit",startDate,startDate,FALSE) 
    checkEquals(result,getZooDataFrame(target[1]))
    # three indices
    result <- this$updateMarketValueWeightedSpreads(c(indexList,"lehman_us_credit_1y_3y"),"lehman_us_credit",startDate,startDate,FALSE)
    target <- structure(1.61433752104218, index = structure(1201550400, class = c("POSIXt", 
        "POSIXct")), class = "zoo")
    checkEquals(result,getZooDataFrame(target))
    
    # should bombs
    shouldBomb(this$updateMarketValueWeightedSpreads(indexList,"garbage",startDate,"2008-01-31"))
    shouldBomb(this$updateMarketValueWeightedSpreads(indexList,"lehman_us_credit",TRUE,"2008-01-31"))
    shouldBomb(this$updateMarketValueWeightedSpreads(indexList,"lehman_us_credit",startDate,TRUE))
    shouldBomb(this$updateMarketValueWeightedSpreads("garbage","lehman_us_credit",startDate,endDate))
    shouldBomb(this$updateMarketValueWeightedSpreads(indexList[1],"lehman_us_credit",startDate,endDate))    
}