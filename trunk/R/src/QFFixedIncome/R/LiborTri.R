constructor("LiborTri", function(ccy = "usd",dataTimeStamp = "15:00:00",unitSett = "d",numUnitsSett = 1,acc = 2,...)
{
    this <- extend(RObject(), "LiborTri",
        .tsdb = TimeSeriesDB(),
        .principal = 100,
        .instrument = "libor",
        .financialCenter = c("lnb","nyb"),
        .holidaySource = "financialcalendar",
        .dataSource = "internal",
        .triSource = "internal"
    )
    if(!inStaticConstructor(this)){
        assert(ccy %in% c("usd"))
        assert(dataTimeStamp %in% c("15:00:00"))
        assert(unitSett %in% c("d"))
        assert(numUnitsSett %in% c(1))        
        assert(acc %in% c(2))                
        
        this$.unitSett = unitSett
        this$.numUnitsSett = numUnitsSett
        this$.acc = acc # act/360 by default
        this$.ccy = ccy
        this$.financialCenter
        this$.dataTimeStamp = dataTimeStamp
    }                              
    return(this)
})

method("run","LiborTri",function(this,startDate,endDate,tenor,updateTSDB = FALSE,...){
    
    assert(tenor %in% TermStructure$libor)
    
    # get holiday list
    
    holidayList <- HolidayDataLoader$getHolidaysToTenor(this$.holidaySource,this$.financialCenter,characterToNumericTenor(tenor),startDate,endDate)

    # get underlying time series
    
    data <- (this$.tsdb)$retrieveTimeSeriesByName(paste(this$.instrument,this$.ccy,"rate",tenor,sep = "_"),data.source = this$.dataSource,startDate,endDate)[[1]]
    firstDate <- as.POSIXct(paste(first(index(data)),this$.dataTimeStamp))
    returnDates <- as.POSIXct(paste(index(data)[-1],this$.dataTimeStamp))
    
    # get transformed data

    triDaily <- this$getDailyZooSeries(data,tenor,holidayList)
    
    # put time stamps back
    
    triDaily <- getZooDataFrame(zoo(as.numeric(triDaily),returnDates))
    
    # tsdb updates
    
    if(updateTSDB){
        tsNameTriDaily <- paste(this$.instrument,this$.ccy,"tri_daily",tenor,sep = "_")
        tsNameTriCum <- paste(this$.instrument,this$.ccy,"tri",tenor,sep = "_")
        
        attributeListTriDaily <- list(quote_type = "close",quote_side = "ask",tenor = tenor,quote_convention = "tri_daily",instrument = this$.instrument)
        attributeListTriCum <- list(quote_type = "close",quote_side = "ask",tenor = tenor,quote_convention = "tri",instrument = this$.instrument)        
        
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(triDaily,tsNameTriDaily,this$.triSource,attributeListTriDaily)
        updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source = this$.triSource,this$.principal)
    }
    return(triDaily)
})

method("getDailyZooSeries","LiborTri",function(this,data,tenor,holidayList,...){
    dates <- index(data)
    tenorNumeric <- characterToNumericTenor(tenor)
    settlementDates <- dates;
    zooTriDaily <- zoo(0,dates);

    for (i in 1:NROW(dates)){
        settlementDates[i] <- getFincadDateAdjust(dates[i], this$.unitSett, this$.numUnitsSett,holidayList)
        if(i > 1)
            zooTriDaily[i] <- fincad("aaAccrual_factor",d_e = settlementDates[i-1],d_t = settlementDates[i], acc = this$.acc) * as.numeric(data[i-1]) * this$.principal / 100
    }
    zooTriDaily <- getZooDataFrame(zooTriDaily[-1])
    return(zooTriDaily)
})