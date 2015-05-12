constructor("BondIndexSpread", function(useSwaps = FALSE,...){
    this <- extend(BondIndex(), "BondIndexSpread",
        .useSwaps = useSwaps
    )
    return(this)
})

method("update","BondIndexSpread",function(this,ticker,startDate = NULL,...){    

    if(ticker=="lehman_us_credit"){
        res <- this$updateMarketValueWeightedSpreads(c("lehman_us_credit_intermediate","lehman_us_credit_long"),ticker,startDate,NULL,TRUE)
    }else{
        res <- this$updateAdjustedSpreads(ticker,startDate,NULL,TRUE)
    }
})

method("updateAdjustedSpreads","BondIndexSpread",function(this,ticker,startDate = NULL,endDate = NULL,updateTSDB = FALSE,...){

    this$checkTicker(ticker)
    
    # get underlying data
    
    if(this$.useSwaps){
        benchmark.termStructure <- TermStructure$irs
        benchmark.yield.termStructure <- getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "tenor")
        benchmark.dv01.termStructure <- getTermStructureForTimeSeries("irs_usd_rate_tenor_dv01",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "tenor")
    }else{
        benchmark.termStructure <- TermStructure$us_treasury
        benchmark.yield.termStructure <- getTermStructureForTimeSeries("bond_government_usd_maturity_otr_yield",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "maturity")
        benchmark.dv01.termStructure <- getTermStructureForTimeSeries("bond_government_usd_maturity_otr_dv01",benchmark.termStructure,this$.analyticsSource,startDate,endDate,lookFor = "maturity")
    }
    
    index.dv01 <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","dv01"),data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]
    non.adjusted.index.yield <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","yield_to_worst"),data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]
    index.maturity <- (this$.tsdb)$retrieveTimeSeriesByName(this$getTsName(ticker,"raw","maturity"),data.source = this$.analyticsSource,start = startDate,end = endDate)[[1]]

    # calculate interpolated benchmark yield
    interpolated.benchmark.yield <- this$getInterpolatedZoo(benchmark.termStructure,benchmark.yield.termStructure,index.maturity,this$getRequiredTenor(ticker))
    # calculate interpolated benchmark dv01
    interpolated.benchmark.dv01 <- this$getInterpolatedZoo(benchmark.termStructure,benchmark.dv01.termStructure,index.maturity,this$getRequiredTenor(ticker))
    
    # calculate spread
    data <- na.omit(merge(non.adjusted.index.yield,interpolated.benchmark.yield,index.dv01,interpolated.benchmark.dv01))
    dv01Adjustment <- abs(data[,3]/data[,4])
    adjustedSpread <- getZooDataFrame(data[,1] - data[,2] * dv01Adjustment)    
    
    # update TSDB
    if(updateTSDB)this$updateTSDB(adjustedSpread,ticker,"adjusted","spread")
    
    return(adjustedSpread)
})

method("updateMarketValueWeightedSpreads","BondIndexSpread",function(this,subIndexList,mainIndex,startDate = NULL,endDate = NULL,updateTSDB = FALSE,...){

    this$checkTicker(mainIndex)
    
    weights <- this$getMarketValueWeights(subIndexList,startDate = startDate,endDate = endDate)
    spread.data <- na.omit(getMergedTimeSeries(this$.tsdb,paste(subIndexList,"adjusted_spread",sep = "_"),"internal",startDate,endDate))
    weighted.adjusted.spread <- this$getWeightedSeries(spread.data,weights)
  
    # update TSDB
    if(updateTSDB)this$updateTSDB(weighted.adjusted.spread,mainIndex,"adjusted","spread")
    
    return(weighted.adjusted.spread)
})
