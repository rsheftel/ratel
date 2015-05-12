## Test file for the BondIndexSpreadDv01 object
library(QFCredit)
library(GSFCore)

this <- BondIndexSpreadDv01(sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00")
ticker <- "lehman_us_credit"
startDate = "2008-01-28"
endDate = "2008-02-01"
dv01Dates <- as.POSIXct(c("2008-01-28 15:00:00","2008-01-29 15:00:00","2008-01-30 15:00:00","2008-01-31 15:00:00","2008-02-01 15:00:00"))

test.BondIndexSpreadDv01.update <- function(){
    result <- this$update(ticker,startDate,endDate,updateTSDB = FALSE)
    target <- getZooDataFrame(zoo(c(0.060195,0.060209,0.060272,0.060282,0.060347),dv01Dates))
    colnames(target) <- colnames(result)
    checkSame(round(target,3),round(result,3))
}

test.BondIndexSpreadDv01.constructor <- function(){
    checkSame(this$.acc,2)
    checkSame(this$.holidaySource,"financialcalendar")    
    checkSame(this$.holidayCenter,"nyb")        
    checkSame(this$.yearForward,20)            
}