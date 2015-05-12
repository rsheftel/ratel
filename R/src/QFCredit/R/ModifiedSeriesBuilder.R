constructor("ModifiedSeriesBuilder", function(...)
{
    this <- extend(RObject(), "ModifiedSeriesBuilder",
        .tsdb = TimeSeriesDB()
    )
})

method("getContinuousSeries","ModifiedSeriesBuilder",function(this,zooFront,zooBack,zooFrontMaturity,zooBackMaturity,adjustmentType,...){
    gapZooIndexDates <- this$getGapDatesFromMaturityDateSeries(zooFrontMaturity,zooBackMaturity)
    adjustedZoo <- this$getGapBackwardAdjustedZoo(zooFront,zooBack,gapZooIndexDates,adjustment = adjustmentType)
    return(adjustedZoo)
})

method("getGapDatesFromMaturityDateSeries","ModifiedSeriesBuilder",function(this,zooFrontMaturity,zooBackMaturity,...){
    needs(zooFrontMaturity = "zoo",zooBackMaturity = "zoo"); assert(NROW(zooFrontMaturity)==NROW(zooBackMaturity));
    m <- na.omit(merge(lag(zooFrontMaturity,-1),zooBackMaturity))
    res <- index(m)[m[,1] == m[,2]]
    if(NROW(res)==0 || NCOL(m) == 1)return(NULL)
    return(as.POSIXct(res))
})

method("getGapBackwardAdjustedZoo","ModifiedSeriesBuilder",function(this,zooFront,zooBack,gapZooIndexDates,adjustment = "ratio",...){
    needs(zooFront = "zoo",zooBack = "zoo"); assert(NROW(zooFront)==NROW(zooBack)); assert(adjustment %in% c("ratio","simple"))
    gapZooIndexDates <- gapZooIndexDates[gapZooIndexDates %in% index(zooFront)[-1]] # we only take dates that are susceptible to change the zoo
    
    if(adjustment == "ratio"){
        adjustmentFactors <- zoo(rep(1,NROW(zooFront)),index(zooFront))
        frontDivBack <- zooFront/zooBack
        m <- match(gapZooIndexDates,index(frontDivBack))
        adjustmentFactors[m-1] <- frontDivBack[m]
        adjustmentFactors <- zoo(rev(cumprod(rev(as.numeric(adjustmentFactors)))),index(adjustmentFactors))
        adjustedZoo <- zooFront * adjustmentFactors
    }else if(adjustment == "simple"){
        throw("Not implemented yet")
    }
    return(adjustedZoo)
})
