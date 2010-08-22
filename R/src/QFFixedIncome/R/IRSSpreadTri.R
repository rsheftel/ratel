constructor("IRSSpreadTri", function(...){
    this <- extend(IRSSpread(...), "IRSSpreadTri",
        .principal = 100
    )
})

method("updateSpreadTriByTenor","IRSSpreadTri",function(this,swapTenor,modified,couponType,startDate = NULL,endDate = NULL,updateTSDB = FALSE,...){
    # get the data
    m <- this$getMergedSeries(swapTenor,modified,couponType,startDate,endDate)
    firstDate <- as.POSIXct(paste(first(index(m)),this$.dataTimeStamp))
    returnDates <- as.POSIXct(paste(index(m)[-1],this$.dataTimeStamp))
    # calc tri daily
    dailyTriSeries <- this$calcDailyTriSeries(m)
    index(dailyTriSeries) <- returnDates
    # update TSDB
    if(updateTSDB){
        spreadTriTsName <- paste(this$.swapInstrument,this$.ccy,"spread",swapTenor,modified,"tri_daily",sep = "_")
        spreadTriCumTsName <- paste(this$.swapInstrument,this$.ccy,"spread",swapTenor,modified,"tri",sep = "_")        
        spreadTriAttributeList <- list(modified = modified,quote_type = "close", ccy = this$.ccy, tenor = swapTenor, quote_convention = "spread", instrument = this$.swapInstrument,transformation = "tri_daily")
        spreadTriCumAttributeList <- list(modified = modified,quote_type = "close", ccy = this$.ccy, tenor = swapTenor, quote_convention = "spread", instrument = this$.swapInstrument,transformation = "tri")
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(dailyTriSeries,spreadTriTsName,this$.exportSource,spreadTriAttributeList)         
        updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,spreadTriTsName,spreadTriCumTsName,spreadTriCumAttributeList,source = this$.exportSource,this$.principal)
    }
    return(dailyTriSeries)
})

method("calcDailyTriSeries","IRSSpreadTri",function(this,m,...){
    hedgeRatio <- - (m[,"swapDv01"]/m[,"bondDv01"])
    merged <- na.omit(merge(hedgeRatio = lag(hedgeRatio,-1),m))
    spreadTriDaily <- merged[,"swapTriDaily"] + merged[,"hedgeRatio"] * merged[,"bondTriDaily"]
    spreadTriDaily <- zoo(as.numeric(spreadTriDaily),as.POSIXct(index(m)[-1]))
    return(spreadTriDaily)
})

method("getMergedSeries","IRSSpreadTri",function(this,swapTenor,modified,couponType,startDate = NULL,endDate = NULL,...){
    swapTriTsName <- paste(this$.swapInstrument,this$.ccy,"rate",swapTenor,"tri",sep = "_")
    bondTriTsName <- paste(this$.bondInstrument,this$.bondSector,this$.ccy,swapTenor,modified,"tri",sep = "_")
    swapDv01TsName <- paste(this$.swapInstrument,this$.ccy,"rate",swapTenor,"dv01",sep = "_")
    bondDv01TsName <- paste(this$.bondInstrument,this$.bondSector,this$.ccy,swapTenor,couponType,"dv01",sep = "_")    
    m <- na.omit(getMergedTimeSeries(this$.tsdb,c(swapTriTsName,bondTriTsName,swapDv01TsName,bondDv01TsName),this$.dataSource,startDate,endDate,filter = this$.dataTimeStamp))
    m <- merge(diff(m[,swapTriTsName]),diff(m[,bondTriTsName]),m[,c(swapDv01TsName,bondDv01TsName)])
    colnames(m) <- c("swapTriDaily","bondTriDaily","swapDv01","bondDv01")    
    return(m)
})