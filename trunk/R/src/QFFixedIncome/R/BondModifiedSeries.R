constructor("BondModifiedSeries", function(sector = "government",issuer = "us_treasury",ccy = "usd",data.source = "internal",timeStamp = "15:00:00",...)
{
    extend(ModifiedSeriesBuilder(), "BondModifiedSeries",
        .ccy = ccy,
        .instrument = "bond",
        .sector = sector,
        .issuer = issuer,
        .data.source = data.source,
        .timeStamp = timeStamp
    )
})

method("updateContinuousYieldsSeriesInTSDB","BondModifiedSeries",function(this,
    maturity,front = "otr",back = "1o",modified = "1c",adjustmentType = "ratio"
,...){
    frontTimeSeriesName <- paste(this$.instrument,this$.sector,this$.ccy,maturity,front,"yield",sep = "_")
    backTimeSeriesName <- paste(this$.instrument,this$.sector,this$.ccy,maturity,back,"yield",sep = "_")
    frontMaturityTimeSeriesName <- paste(this$.instrument,this$.sector,this$.ccy,maturity,front,"maturity",sep = "_")
    backMaturityTimeSeriesName <- paste(this$.instrument,this$.sector,this$.ccy,maturity,back,"maturity",sep = "_")
    
    # load data from TSDB and calc adjusted series
  
    m <- getMergedTimeSeries(this$.tsdb,c(frontTimeSeriesName,backTimeSeriesName,frontMaturityTimeSeriesName,backMaturityTimeSeriesName),this$.data.source,filter = this$.timeStamp)
    m <- na.omit(m)
    adjustedSeries <- this$getContinuousSeries(m[,1],m[,2],m[,3],m[,4],adjustmentType)
    index(adjustedSeries) <- as.POSIXct(paste(index(adjustedSeries),this$.timeStamp))

    # update TSDB
    
    tsNameAdjusted <- paste(this$.instrument,this$.sector,this$.ccy,maturity,modified,"yield",sep = "_")
    attributeListAdjusted <- list(
        quote_type = "close",ccy = this$.ccy,maturity = maturity,quote_side = "mid",quote_convention = "yield",
        instrument = this$.instrument,issuer = this$.issuer,sector = this$.sector,modified = modified
    )
    (this$.tsdb)$createAndWriteOneTimeSeriesByName(adjustedSeries,tsNameAdjusted,this$.data.source,attributeListAdjusted)
    return(TRUE)
})