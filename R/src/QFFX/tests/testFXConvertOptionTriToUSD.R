library("QFFX")

testFXConvertOptionTriToUSD <- function()
{
  	startDate <- "1997-01-03"
  	endDate <- "2007-10-31"
   
    fxCurr <- FXCurr$setByCross("usd/jpy")
    tenor <- "2y"
    putCall<-"call"
    rebalPeriod <- "2w"
    rebalDelta <- 0.15
 
    res <- FXConvertOptionTriToUSD$convertLocalCcyTri(fxCurr=fxCurr,tenor=tenor,putCall=putCall,rebalPeriod = rebalPeriod, rebalDelta = rebalDelta)
    checkEquals(round(as.numeric(res[as.POSIXct("2007-09-28"),]),5),199.10614)
}