IRSSlopePayoutRatioBackPop <- function(currency=NULL, startDate = NULL, endDate = NULL)
{
#  tenorFrame <- data.frame(cbind(c("2y","5y","10y","15y","30y"),c("1y","2y","5y","10y","15y")))
  tenorFrame <- data.frame(cbind(c("5y","10y","15y","30y"),c("2y","5y","10y","15y")))
  colnames(tenorFrame) <- c("longTenor","shortTenor")

  volLookBack <- 60 
  forwardStart <- "6m"
  tsdb <- TimeSeriesDB()

  for (i in 1:nrow(tenorFrame)) {
    longTenor <- as.character(tenorFrame[i,"longTenor"])
    shortTenor <- as.character(tenorFrame[i,"shortTenor"])
    time.series.name <- paste("irs",currency,"slope",paste(longTenor,"-",shortTenor,sep=""),forwardStart,"payout_ratio",sep="_")
    irspayoutcompute <- IRSSlopePayoutRatio(currency=currency, forwardStart=forwardStart, longTenor=longTenor, 
      shortTenor=shortTenor, startDate=startDate, endDate= endDate, tsdb = tsdb, volLookBack=volLookBack)    
    irspayoutcompute$getSpotDataLoad()
    cat("Computing:",time.series.name,"\n")
    
    irspayoutcompute$computePayoutRatio()
    payout.zoo <- irspayoutcompute$getPayoutRatio()
    tsdb <- TimeSeriesDB()
    time.series.source <- "internal"
    
    cat("Time Series:",time.series.name,"\n")
    tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(time.series.name, time.series.source))
        tsMatrix[[1,1]] <- payout.zoo
    irs.data <- tsdb$writeTimeSeries(tsMatrix)
    }
}