constructor("IRSSlopeTri", function(instrument = "irs",ccy = "usd",dataTimeStamp = Close$NY.irs,...){
    this <- extend(IRSTri(...), "IRSSlopeTri",
        .principal = 100,
        .instrument = instrument,
        .ccy = ccy,
        .dataTimeStamp = dataTimeStamp
    )
})

method("updateSlopeTriByTenor","IRSSlopeTri",function(this,swapTenorShort,swapTenorLong,startDate = NULL,endDate = NULL, updateTSDB = FALSE,...){
    # get the data
    assert(characterToNumericTenor(swapTenorLong) > characterToNumericTenor(swapTenorShort),"swapTenorShort should be shorter maturity than swapTenorLong")
    m <- this$getMergedSeries(swapTenorShort,swapTenorLong,startDate,endDate)
    firstDate <- as.POSIXct(paste(first(index(m)),this$.dataTimeStamp))
    returnDates <- as.POSIXct(paste(index(m)[-1],this$.dataTimeStamp))
    # calc tri daily
    dailyTriSeries <- this$calcDailyTriSeries(m)
    index(dailyTriSeries) <- returnDates
    # update TSDB
    if(updateTSDB){
        slope <- paste(swapTenorLong,"-",swapTenorShort,sep = "")
        slopeTriTsName <- paste(this$.instrument,this$.ccy,"slope",slope,"tri_daily",sep = "_")
        slopeTriCumTsName <- paste(this$.instrument,this$.ccy,"slope",slope,"tri",sep = "_")
        slopeTriAttributeList <- list(quote_type = "close", ccy = this$.ccy, tenor = slope, quote_convention = "slope", instrument = this$.instrument,transformation = "tri_daily")
        slopeTriCumAttributeList <- list(quote_type = "close", ccy = this$.ccy, tenor = slope, quote_convention = "slope", instrument = this$.instrument,transformation = "tri")
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(dailyTriSeries,slopeTriTsName,this$.triSource,slopeTriAttributeList)
        updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,slopeTriTsName,slopeTriCumTsName,slopeTriCumAttributeList,source = this$.triSource,this$.principal)
    }
    return(dailyTriSeries)
})

method("calcDailyTriSeries","IRSSlopeTri",function(this,m,...){
    hedgeRatio <- - (m[,"longDv01"]/m[,"shortDv01"])
    merged <- na.omit(merge(hedgeRatio = lag(hedgeRatio,-1),m))
    slopeTriDaily <- merged[,"longTriDaily"] + merged[,"hedgeRatio"] * merged[,"shortTriDaily"]
    slopeTriDaily <- zoo(as.numeric(slopeTriDaily),as.POSIXct(index(m)[-1]))
    return(slopeTriDaily)
})

method("getMergedSeries","IRSSlopeTri",function(this,swapTenorShort,swapTenorLong,startDate = NULL,endDate = NULL,...){
    shortTriTsName <- paste(this$.instrument,this$.ccy,"rate",swapTenorShort,"tri",sep = "_")
    longTriTsName <- paste(this$.instrument,this$.ccy,"rate",swapTenorLong,"tri",sep = "_")
    shortDv01TsName <- paste(this$.instrument,this$.ccy,"rate",swapTenorShort,"dv01",sep = "_")
    longDv01TsName <- paste(this$.instrument,this$.ccy,"rate",swapTenorLong,"dv01",sep = "_")
    m <- na.omit(getMergedTimeSeries(this$.tsdb,c(shortTriTsName,longTriTsName,shortDv01TsName,longDv01TsName),this$.triSource,startDate,endDate,filter = this$.dataTimeStamp))
    m <- merge(diff(m[,shortTriTsName]),diff(m[,longTriTsName]),m[,c(shortDv01TsName,longDv01TsName)])
    colnames(m) <- c("shortTriDaily","longTriDaily","shortDv01","longDv01")
    return(m)
})