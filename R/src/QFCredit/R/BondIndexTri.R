constructor("BondIndexTri", function(useSwaps = FALSE,...){
    this <- extend(BondIndex(), "BondIndexTri",
        .useSwaps = useSwaps)
    return(this)
})


method("updateRaw","BondIndexTri",function(this,ticker,refDate = NULL,...){    

  
    res <- this$updateReinvestmentFreeRawDailyTris(ticker,refDate,NULL,TRUE)

    if(is.null(refDate))refDate <- as.POSIXct(paste(as.Date(first(index(res)))-1,this$.dataTimeStamp))
    else refDate <- as.POSIXct(paste(as.Date(refDate),this$.dataTimeStamp))
    
    updateCumTriFromDailyTri(this$.tsdb,refDate,index(res),
        tsNameTriDaily = this$getTsName(tickerList[i],"raw","tri_daily"),
        tsNameTriCum = this$getTsName(tickerList[i],"raw","tri"),
        attributeListTriCum = this$getAttributeList(tickerList[i],"raw","tri"),
        this$.analyticsSource,this$.principal
    )
})            


method("updateReinvestmentFreeRawDailyTris","BondIndexTri",function(this,ticker,startDate,endDate,updateTSDB = FALSE,...){
    
    this$checkTicker(ticker)
    
    originalTri <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","tri"),data.source = this$.originalSource,startDate,endDate)[[1]]
    dailyTri <- (diff(originalTri)/lag(originalTri,-1)) * this$.principal
    
    # update reinvestment free daily tris
    if(updateTSDB)this$updateTSDB(dailyTri,ticker,"raw","tri_daily")
    
    return(dailyTri)
})


method("updateAdjusted","BondIndexTri",function(this,ticker,refDate = NULL,startDate = NULL,...){

    if(ticker=="lehman_us_credit"){
        res <- this$updateMarketValueWeightedDailyTris(c("lehman_us_credit_intermediate","lehman_us_credit_long"),ticker,refDate,NULL,TRUE)
    }else{
        res <- this$updateAdjustedDailyTris(ticker,refDate,NULL,TRUE)
    }
    
    if(is.null(refDate))refDate <- as.POSIXct(paste(as.Date(first(index(res)))-1,this$.dataTimeStamp))
    else refDate <- as.POSIXct(paste(as.Date(refDate),this$.dataTimeStamp))

    updateCumTriFromDailyTri(this$.tsdb,refDate,index(res),
        tsNameTriDaily = this$getTsName(tickerList[i],"adjusted","tri_daily"),
        tsNameTriCum = this$getTsName(tickerList[i],"adjusted","tri"),
        attributeListTriCum = this$getAttributeList(tickerList[i],"adjusted","tri"),
        this$.analyticsSource,this$.principal
    )
})

method("updateAdjustedDailyTris","BondIndexTri",function(this,ticker,startDate = NULL,endDate = NULL,updateTSDB = FALSE,...){

    this$checkTicker(ticker)
    
    # get index data
    non.adjusted.index.tri <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","tri"),data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]
    index.dv01 <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","dv01"),data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]
    index.maturity <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","maturity"),data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]
    
    # get benchmark data
    
    if(this$.useSwaps){
        benchmark.termStructure <- TermStructure$irs
        benchmark.tri.daily.termStructure <- getTermStructureForTimeSeries("irs_usd_rate_tenor_tri_daily",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "tenor")
        benchmark.dv01.termStructure <- getTermStructureForTimeSeries("irs_usd_rate_tenor_dv01",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "tenor")
        
        interpolated.benchmark.tri.financing <- (this$.tsdb)$retrieveTimeSeriesByName("libor_usd_tri_on",data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]
    }else{
        benchmark.termStructure <- TermStructure$us_treasury
        benchmark.tri.daily.termStructure <- getTermStructureForTimeSeries("bond_government_usd_maturity_1c_tri_daily",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "maturity")
        benchmark.dv01.termStructure <- getTermStructureForTimeSeries("bond_government_usd_maturity_otr_dv01",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "maturity")
        benchmark.tri.daily.financing.termStructure <- getTermStructureForTimeSeries("bond_government_usd_maturity_1c_repo_on_tri_daily",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "maturity")        
    
        # calculate interpolated benchmark financing
        interpolated.benchmark.tri.financing <- getCumTriFromDailyTri(this$getInterpolatedZoo(benchmark.termStructure,benchmark.tri.daily.financing.termStructure,index.maturity,this$getRequiredTenor(ticker)))
    }
          
    # calculate interpolated benchmark tri
    interpolated.benchmark.tri <- getCumTriFromDailyTri(this$getInterpolatedZoo(benchmark.termStructure,benchmark.tri.daily.termStructure,index.maturity,this$getRequiredTenor(ticker)))
    # calculate interpolated benchmark dv01
    interpolated.benchmark.dv01 <- this$getInterpolatedZoo(benchmark.termStructure,benchmark.dv01.termStructure,index.maturity,this$getRequiredTenor(ticker))
    
    # calculate adjusted daily tri
    adjustedDailyTri <- this$getAdjustedDailyTri(
        non.adjusted.index.tri,index.dv01,
        interpolated.benchmark.tri,interpolated.benchmark.dv01,
        interpolated.benchmark.tri.financing
    )

    # update TSDB
    if(updateTSDB)this$updateTSDB(adjustedDailyTri,ticker,"adjusted","tri_daily")
    
    return(adjustedDailyTri)
})

method("getAdjustedDailyTri","BondIndexTri",function(this,
        non.adjusted.index.tri,index.dv01,
        interpolated.benchmark.tri,interpolated.benchmark.dv01,
        interpolated.benchmark.tri.financing,...){
        
    data <- na.omit(merge(
        non.adjusted.index.tri,
        interpolated.benchmark.tri,
        interpolated.benchmark.tri.financing,
        index.dv01,
        interpolated.benchmark.dv01
    ))
    
    dailyTri <- diff(data[,c(1:3)])
    dv01Adjustment <- abs(lag(data[,4]/data[,5],-1))
    
    return(getZooDataFrame(dailyTri[,1] - dv01Adjustment * dailyTri[,2] - dailyTri[,3]))
})


method("updateMarketValueWeightedDailyTris","BondIndexTri",function(this,subIndexList,mainIndex,startDate = NULL,endDate = NULL,updateTSDB = FALSE,...){
    
    this$checkTicker(mainIndex)
    
    weights <- this$getMarketValueWeights(subIndexList,startDate = startDate,endDate = endDate)
    daily.tri.data <- na.omit(getMergedTimeSeries(this$.tsdb,paste(subIndexList,"adjusted_tri_daily",sep = "_"),"internal",startDate,endDate))
    weighted.adjusted.daily.tri <- this$getWeightedSeries(daily.tri.data,lag(weights,-1))
  
    # update TSDB
    if(updateTSDB)this$updateTSDB(weighted.adjusted.daily.tri,mainIndex,"adjusted","tri_daily")
    
    return(weighted.adjusted.daily.tri)
})