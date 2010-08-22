constructor("IRSSpread", function(
    ccy = "usd",bondIssuer = "us_treasury",bondSector = "government",
    dataTimeStamp = Close$NY.irs,dataSource = "internal",exportSource = "internal"
,...){
    this <- extend(RObject(), "IRSSpread",
        .tsdb = TimeSeriesDB(),
        .swapInstrument = "irs",
        .bondInstrument = "bond",
        .ccy = ccy,              
        .bondIssuer = bondIssuer,
        .bondSector = bondSector,
        .dataTimeStamp = dataTimeStamp,
        .dataSource = dataSource,
        .exportSource = exportSource
    )
})

method("updateSpreadSeriesByTenor","IRSSpread",function(this,swapTenor,bondYieldType,modified,startDate = NULL,endDate = NULL,...){
     # get time series names
     swapRateTsName <- paste(this$.swapInstrument,this$.ccy,"rate",swapTenor,"mid",sep = "_")
     bondYieldTsName <- paste(this$.bondInstrument,this$.bondSector,this$.ccy,swapTenor,bondYieldType,"yield",sep = "_")
     # load data and calc spread series
     m <- na.omit(getMergedTimeSeries(this$.tsdb,c(swapRateTsName,bondYieldTsName),this$.dataSource,startDate,endDate,filter = this$.dataTimeStamp))
     colnames(m) <- c("swapRates","bondYields")
     spreadSeries <- zoo(as.numeric(m[,"swapRates"] - m[,"bondYields"]),as.POSIXct(paste(as.Date(index(m[,"swapRates"])),this$.dataTimeStamp,sep = " ")))
     # update TSDB
     spreadTsName <- paste(this$.swapInstrument,this$.ccy,"spread",swapTenor,modified,sep = "_")
     spreadAttributeList <- list(modified = modified,quote_type = "close", ccy = this$.ccy, tenor = swapTenor, quote_convention = "spread", instrument = this$.swapInstrument,transformation = "spread")
    (this$.tsdb)$createAndWriteOneTimeSeriesByName(spreadSeries,spreadTsName,this$.exportSource,spreadAttributeList)
     return(spreadSeries)
})