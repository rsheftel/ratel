constructor("IRSTri", function(...)
{
    this <- extend(RObject(), "IRSTri",
        .tsdb = TimeSeriesDB(),
        .linlast = 2, # yield is always compounded (cf. aaBond_p)
        .exdays = 0, # number of days bond is ex-dividend (cf. aaBond_p)
        .principal = 100,
        .holidaySource = "financialcalendar",
        .triSource = "internal"
    )
})

method("taylorPriceChange","IRSTri",function(this,
    rate,rateChange,settlementDate,maturityDate,freq,acc,...){
    
    acc <- fincadAccrualSwitchMap.sw_331.to.sw_376(acc)
    
    dv01 <- as.numeric(fincad("aaBond_p",
        d_s = settlementDate, d_m = maturityDate,
        d_dated = 0, d_f_cpn = 0, d_l_cpn = 0,
        cpn = rate/100, princ_m = this$.principal,
        freq = freq, acc = acc, yield = rate/100, linlast = this$.linlast,exdays = this$.exdays,
        stat = 5
    ))
    convexity <- as.numeric(fincad("aaBond_p",
        d_s = settlementDate, d_m = maturityDate,
        d_dated = 0, d_f_cpn = 0, d_l_cpn = 0,
        cpn = rate/100, princ_m = this$.principal,
        freq = freq, acc = acc, yield = rate/100, linlast = this$.linlast,exdays = this$.exdays,
        stat = 6
    )/100)
    
    priceChange <- as.numeric(- rateChange * dv01 + 0.5 * convexity * (rateChange)^2)
  
    return(list(priceChange = priceChange,dv01 = dv01,convexity = convexity))
})

fincadAccrualSwitchMap.sw_331.to.sw_376 <- function(num){
    if(num == 1)return(11)
    if(num == 2)return(12)
    if(num == 3)return(13)
    if(num == 4)return(14)
    if(num == 5)return(15)
    if(num == 6)return(16)
    if(num == 7)return(17)
    if(num == 8)return(18)
    if(num == 9)return(19)
    if(num == 10)return(20)
}

method("getAccrual","IRSTri",function(this,rate,start,end,acc,...){
    res <- fincad("aaAccrual_factor",d_e = start,d_t = end, acc = acc) * rate
    return(res)
})

method("getDailyZooSeries","IRSTri",function(this,curve,tenor,termStructure,unitSett,numUnitsSett,freq,acc,holidayList,...){

    dates <- index(curve)
    nbDates <- NROW(dates)
    tenorNumeric <- characterToNumericTenor(tenor)
    settlementDates <- dates; maturityDates <- dates;
    zooTriDaily <- zoo(0,dates); zooDv01 <- zoo(0,dates); zooConvexity <- zoo(0,dates)
    
    for (i in 1:nbDates){
        settlementDates[i] <- getFincadDateAdjust(dates[i], unitSett, numUnitsSett,holidayList)

        # Maturity dates, we shift the settlement date by the exact maturity date
        thisMat.POSIXlt <- as.POSIXlt(settlementDates[i])
        thisMat.POSIXlt$mon <- thisMat.POSIXlt$mon + 12 * tenorNumeric
        maturityDates[i] <- as.POSIXct(as.character(thisMat.POSIXlt))

        if(i > 1){
            strike <- as.numeric(curve[i-1,tenor])
            roll <- as.numeric(as.Date(settlementDates[i]) - as.Date(settlementDates[i-1]))
            mark <- as.numeric(getInterpolatedTermStructure(curve[i,],characterToNumericTenor(termStructure),tenorNumeric - roll/365,type = 1))
            rateChange <- mark - strike
            taylorResult <- this$taylorPriceChange(as.numeric(curve[i-1,tenor]),rateChange,settlementDates[i-1],maturityDates[i-1],freq,acc)
            accrued <- this$getAccrual(strike,settlementDates[i-1],settlementDates[i],acc)
            zooTriDaily[i] <- taylorResult$priceChange + accrued
            zooDv01[i] <- taylorResult$dv01
            zooConvexity[i] <- taylorResult$convexity
        }
    }
    zooTriDaily <- getZooDataFrame(zooTriDaily[-1])
    zooDv01 <- getZooDataFrame(zooDv01[-1])
    zooConvexity <- getZooDataFrame(zooConvexity[-1])
    return(list(zooTriDaily = zooTriDaily,zooDv01 = zooDv01,zooConvexity = zooConvexity))
})

method("getDailyZooSeriesFincad","IRSTri",function(this,ccy,tenorFixed,curveFixed,curveFloat,termStructureFixed,termStructureFloat,unitSettFixed,numUnitsSettFixed,holidayList,...){

    dates <- index(curveFixed)
    nbDates <- NROW(dates)
    tenorNumericFixed <- characterToNumericTenor(tenorFixed)
    settlementDatesFixed <- dates; maturityDatesFixed <- dates;
    zooTriDaily <- zoo(0,dates); zooDv01 <- zoo(0,dates); zooConvexity <- zoo(0,dates)
    
    pricer <- IRSPricer(ccy = ccy,holidayList = holidayList)
    
    for (i in 1:nbDates){

        settlementDatesFixed[i] <- getFincadDateAdjust(dates[i], unitSettFixed, numUnitsSettFixed,holidayList)

        # Maturity dates, we shift the settlement date by the exact maturity date
        thisMat.POSIXlt <- as.POSIXlt(settlementDatesFixed[i])
        thisMat.POSIXlt$mon <- thisMat.POSIXlt$mon + 12 * tenorNumericFixed
        maturityDatesFixed[i] <- as.POSIXct(as.character(thisMat.POSIXlt))

        if(i > 1){
            result <- pricer$getPrice(
                valueDate = settlementDatesFixed[i],
                effDate = settlementDatesFixed[i-1],
                matDate = maturityDatesFixed[i-1],
                fixedCoupon = as.numeric(curveFixed[i-1,tenorFixed])/100,
                notional = this$.principal,
                dfCurveDate = dates[i],
            	cashRates = as.numeric(curveFloat[i,!is.na(curveFloat[i,])])/100,
            	cashTenors = termStructureFloat[!is.na(curveFloat[i,])],
            	swapRates  = as.numeric(curveFixed[i,!is.na(curveFixed[i,])])/100,
            	swapTenors = termStructureFixed[!is.na(curveFixed[i,])]
            )
           zooDv01[i] <- as.numeric(result["dv01",1])
           zooConvexity[i] <- as.numeric(result["convexity",1])/100
           zooTriDaily[i] <- as.numeric(result["marketPrice",1])
        }
    }
    zooTriDaily <- getZooDataFrame(zooTriDaily[-1])
    zooDv01 <- getZooDataFrame(zooDv01[-1])
    zooConvexity <- getZooDataFrame(zooConvexity[-1])
    return(list(zooTriDaily = zooTriDaily,zooDv01 = zooDv01,zooConvexity = zooConvexity))
})

method("cleanCurves","IRSTri",function(this,curveList,tenorFixed,tenorFloat,...){

    fixedCurve <- curveList$fixedCurve
    floatCurve <- curveList$floatCurve
    fixedCurve <- fixedCurve[!is.na(fixedCurve[,tenorFixed]),] # dates where we have a fixed strike
    floatCurve <- floatCurve[index(fixedCurve),] # dates where we have a fixed strike
    
    m <- merge(fixedCurve = fixedCurve,floatCurve = floatCurve)
    rowsToTakeOff <- NULL
    seqFloat <- (NCOL(fixedCurve)+1):NCOL(m)
    seqFixed <- 1:NCOL(fixedCurve)
    for (i in 1:NROW(m)){
        if(is.na(m[i,tenorFloat]) && (i!=1)){
            m[i,seqFloat] <- m[i-1,seqFloat] # copy float t-1 curve to t if needed and if not first row
            if(is.na(m[i,seqFloat]))rowsToTakeOff <- c(rowsToTakeOff,i)
        }
        if((is.na(m[i,tenorFloat]) && (i==1)) || (is.na(m[i,tenorFixed]))){
            rowsToTakeOff <- c(rowsToTakeOff,i) # delete rows if first row without float or fixed strike
        }
    }
    if(!is.null(rowsToTakeOff))m <- m[-unique(rowsToTakeOff),]
    return(list(fixedCurve = m[,seqFixed],floatCurve = m[,seqFloat]))
})

method("getCurves","IRSTri",function(this,startDate,endDate,ccy,instrumentFixed,instrumentFloat,termStructureFixed,termStructureFloat,sourceFixed,sourceFloat,timeStampFixed,timeStampFloat,...){
        
    fixedTsCurveName <- paste(instrumentFixed,ccy,"rate","tenor","mid",sep = "_")
    floatTsCurveName <- paste(instrumentFloat,ccy,"rate","tenor",sep = "_")
    
    fixedCurve <- getTermStructureForTimeSeries(fixedTsCurveName,termStructureFixed,sourceFixed,startDate = startDate,endDate = endDate)
    floatCurve <- getTermStructureForTimeSeries(floatTsCurveName,termStructureFloat,sourceFloat,startDate = startDate,endDate = endDate)
    if(NROW(fixedCurve)<2 || NROW(floatCurve)<2)throw("There should be at least two valid dates in the startDate/endDate range")    
    fixedCurve <- make.zoo.daily(fixedCurve, timeStampFixed)
    floatCurve <- make.zoo.daily(floatCurve, timeStampFixed)    
    
    return(list(fixedCurve = fixedCurve,floatCurve = floatCurve))
})




method("run","IRSTri",function(this,
    startDate,endDate,
    ccy,
    financialCenterFixed,financialCenterFloat,
    tenorFixed,tenorFloat,
    instrumentFixed,instrumentFloat,
    sourceFixed,sourceFloat,
    timeStampFixed,timeStampFloat,
    termStructureFixed,termStructureFloat,
    unitSettFixed,numUnitsSettFixed,
    unitSettFloat,numUnitsSettFloat,
    freqFixed,accFixed,
    freqFloat,accFloat,
    method = "taylor",
    updateTSDB = FALSE
,...){
    
    # get holiday list
    
    holidayListFixed <- HolidayDataLoader$getHolidaysToTenor(this$.holidaySource,financialCenterFixed,characterToNumericTenor(tenorFixed),startDate,endDate)
    holidayListFloat <- HolidayDataLoader$getHolidaysToTenor(this$.holidaySource,financialCenterFloat,characterToNumericTenor(tenorFixed),startDate,endDate)    

    # get underlying time series
    
    curveList <- this$getCurves(startDate,endDate,ccy,instrumentFixed,instrumentFloat,termStructureFixed,termStructureFloat,sourceFixed,sourceFloat,timeStampFixed,timeStampFloat)
    curveList <- this$cleanCurves(curveList,tenorFixed,tenorFloat)
    firstDate <- as.POSIXct(paste(first(index(curveList$fixedCurve)),timeStampFixed))
    returnDates <- as.POSIXct(paste(index(curveList$fixedCurve)[-1],timeStampFixed))
    
    # get transformed data
    
    if(method == "taylor"){
        zooListFixed <- this$getDailyZooSeries(curveList$fixedCurve,tenorFixed,termStructureFixed,unitSettFixed,numUnitsSettFixed,freqFixed,accFixed,holidayListFixed)
        zooListFloat <- this$getDailyZooSeries(curveList$floatCurve,tenorFloat,termStructureFloat,unitSettFloat,numUnitsSettFloat,freqFloat,accFloat,holidayListFloat)
        triDaily <- zooListFixed$zooTriDaily - zooListFloat$zooTriDaily
        dv01 <- zooListFixed$zooDv01 - zooListFloat$zooDv01 
        convexity <- zooListFixed$zooConvexity - zooListFloat$zooConvexity
    }else if(method == "fincad"){
        zooList <- this$getDailyZooSeriesFincad(ccy,tenorFixed,curveList$fixedCurve,curveList$floatCurve,termStructureFixed,termStructureFloat,unitSettFixed,numUnitsSettFixed,holidayListFixed)
        triDaily <- zooList$zooTriDaily
        dv01 <- zooList$zooDv01
        convexity <- zooList$zooConvexity
    }
    
    # put time stamps back
    
    triDaily <- getZooDataFrame(zoo(as.numeric(triDaily),returnDates))
    dv01 <- getZooDataFrame(zoo(as.numeric(dv01),returnDates))
    convexity <- getZooDataFrame(zoo(as.numeric(convexity),returnDates))
    
    # tsdb updates
    
    if(updateTSDB){
        tsNameDv01 <- paste(instrumentFixed,ccy,"rate",tenorFixed,"dv01",sep = "_")
        tsNameConvexity <- paste(instrumentFixed,ccy,"rate",tenorFixed,"convexity",sep = "_")
        tsNameTriDaily <- paste(instrumentFixed,ccy,"rate",tenorFixed,"tri_daily",sep = "_")  
        tsNameTriCum <- paste(instrumentFixed,ccy,"rate",tenorFixed,"tri",sep = "_")
        
        attributeListDv01 <- list(quote_type = "close",ccy = ccy,tenor = tenorFixed,quote_convention = "rate",instrument = instrumentFixed,transformation = "dv01")
        attributeListConvexity <- list(quote_type = "close",ccy = ccy,tenor = tenorFixed,quote_convention = "rate",instrument = instrumentFixed,transformation = "convexity")
        attributeListTriDaily <- list(quote_type = "close",ccy = ccy,tenor = tenorFixed,quote_convention = "rate",instrument = instrumentFixed,transformation = "tri_daily")
        attributeListTriCum <- list(quote_type = "close",ccy = ccy,tenor = tenorFixed,quote_convention = "rate",instrument = instrumentFixed,transformation = "tri")
        
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(dv01,tsNameDv01,this$.triSource,attributeListDv01)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(convexity,tsNameConvexity,this$.triSource,attributeListConvexity)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(triDaily,tsNameTriDaily,this$.triSource,attributeListTriDaily)
        updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source = this$.triSource,this$.principal)
    }
    return(list(zooTriDaily = triDaily,zooDv01 = dv01,zooConvexity = convexity))
})