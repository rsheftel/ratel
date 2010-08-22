library(QFFixedIncome)

test.IRSSlopePayoutRatio.loadIRSSlopeData <- function()
{
  startDate <- "2008-01-01"
  endDate <- "2008-02-20"
  currency <- "usd"
  longTenor <- "5y"
  shortTenor <- "2y"
  forwardStart <- "1y"
  tsdb <- TimeSeriesDB()

#test for bad data
shouldBomb(IRSSlopePayoutRatio$loadIRSSlopeData(currency=currency, forwardStart=forwardStart, tenorList=c("BusinessTime",shortTenor), startDate=startDate, endDate= endDate, tsdb = tsdb))    
shouldBomb(IRSSlopePayoutRatio$loadIRSSlopeData(currency=currency, forwardStart=forwardStart, tenorList=c(longTenor,"BusinessTime"), startDate=startDate, endDate= endDate, tsdb = tsdb))

# Test for good data 
  dataLoad <- IRSSlopePayoutRatio$loadIRSSlopeData(currency=currency, forwardStart=forwardStart, tenorList=c(longTenor,shortTenor), startDate=startDate, endDate= endDate, tsdb = tsdb)
  dataLoad <- strip.times.zoo(dataLoad)
  checkSame(as.numeric(dataLoad[as.POSIXct("2008-02-19"),longTenor]),4.35607)
  checkSame(as.numeric(dataLoad[as.POSIXct("2008-02-19"),shortTenor]),3.51903)
  
}

test.IRSSlopePayoutRatio.computePayoutRatio <- function()
{
  startDate <- "2007-01-02"
  endDate <- "2008-02-20"
  currency <- "usd"
  longTenor <- "5y"
  shortTenor <- "2y"
  forwardStart <- "6m"
  tsdb <- TimeSeriesDB()

#test for bad data
irspayoutcompute <- IRSSlopePayoutRatio(currency=currency, forwardStart=forwardStart, longTenor, shortTenor, startDate=startDate, endDate= endDate, tsdb = tsdb, volLookBack=20)    
results <- irspayoutcompute$getSpotDataLoad()
irspayoutcompute$setCurrentDate(as.POSIXct("2008-02-12"))
realized.vol <- irspayoutcompute$computeVol()
checkSame(round(realized.vol,5),0.03668)
irspayoutcompute$computePayoutRatio()
payout.zoo <- irspayoutcompute$getPayoutRatio()
checkSame(round(as.numeric(payout.zoo[as.POSIXct("2008-02-4")]),5),-0.20768)

  
}  
