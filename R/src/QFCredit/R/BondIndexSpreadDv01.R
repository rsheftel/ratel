constructor("BondIndexSpreadDv01", function(...){
    this <- extend(BondIndex(), "BondIndexSpreadDv01",
        .acc = 2, # act/360 by default
        .holidaySource = "financialcalendar",
        .holidayCenter = "nyb",
        .yearForward = 20
    )
    return(this)
})

method("update","BondIndexSpreadDv01",function(this,ticker,startDate = NULL,endDate = NULL,updateTSDB = FALSE,...){

    this$checkTicker(ticker)
    
    # get underlying data
    tsNames <- c(this$getTsName(ticker,"adjusted","tri"),this$getTsName(ticker,"adjusted","spread"),this$getTsName(ticker,"raw","maturity"))
    data <- getMergedTimeSeries(this$.tsdb,tsNames,this$.analyticsSource,startDate,endDate,filter = this$.dataTimeStamp)
    data <- na.omit(data)
    data <- data[data[,2]>0] # no negative spread
    
    zooDv01 <- this$updateUsingPricer(data,ticker,startDate,endDate)
    zooDv01 <- getZooDataFrame(zoo(as.numeric(zooDv01),as.POSIXct(paste(index(zooDv01),this$.dataTimeStamp))))
    # update TSD                                 
    if(updateTSDB)this$updateTSDB(zooDv01,ticker,"adjusted","dv01")
    return(zooDv01)
})


method("updateUsingPricer","BondIndexSpreadDv01",function(this,data,ticker,startDate = NULL,endDate = NULL,...){

    # approximate rates (we take a flat rate curve at the index maturity)
    rate.termStructure <- getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",TermStructure$irs,this$.analyticsSource,startDate,endDate,lookFor = "tenor")
    rate.termStructure <- make.zoo.daily(rate.termStructure,this$.dataTimeStamp)
    interpolated.rate <- this$getInterpolatedZoo(TermStructure$irs,rate.termStructure,data[,3])
    data <- na.omit(merge(data,interpolated.rate))
    
    dates <- index(data)
    zooDv01 <- zoo(0,dates);
    holidayList <- HolidayDataLoader$getHolidaysToTenor(source = this$.holidaySource,this$.holidayCenter,this$.yearForward,startDate,endDate)

    for (i in 1:NROW(dates)){
        nextBusinessDate <- getFincadDateAdjust(dates[i], "d",1,holidayList) 
        spread <- as.numeric(data[i,2])/100
        rate <- as.numeric(data[i,3])/100
        matDate <- getFincadDateAdjust(dates[i], "d", as.numeric(data[i,3])*252,holidayList)
        
        pricerResult <- CDSPricer$getFincadPrice(
            direction = "buy",
            strike = spread,
            notional = this$.principal,
            recovery = 0,
            valueDate = nextBusinessDate,
            effDate = nextBusinessDate,
            matDate = matDate,
            cdsTable = data.frame(effDate = nextBusinessDate,matDate = matDate,spread = spread),
            dfTable = rate,
            upfrontPayment = NULL
        )
        zooDv01[i] <- pricerResult[7,]
    }
    
    return(getZooDataFrame(zooDv01))
})