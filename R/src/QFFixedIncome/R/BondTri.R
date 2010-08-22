constructor("BondTri", function(couponTypeLead = "otr",couponTypeLag = "1o",modified = "1c",...)
{
    library(QFFixedIncome)
    this <- extend(RObject(), "BondTri",
        .tsdb = TimeSeriesDB(),
        .principal = 100,
        .instrument = "bond",
        .modified = modified,
        .couponTypeLead = couponTypeLead,
        .couponTypeLag = couponTypeLag,
        .holidaySource = "financialcalendar",
        .triSource = "internal"
    )
})

method("getFincadOutputs","BondTri",function(this,settlementDate,fincadCountryCode,price,coupon,maturityDateNumeric,...){

    fincadFunctionName <- paste("aaBond",fincadCountryCode,"y",sep = "_")
    maturityDate <- strptime(as.character(maturityDateNumeric),"%Y%m%d")
    nextCouponDate <- fincad(fincadFunctionName,d_s = settlementDate,d_m = maturityDate,d_dated = 0,d_f_cpn = 0,cpn = coupon,princ_m = this$.principal,price = price,stat = 9)
    previousCouponDate <- fincad(fincadFunctionName,d_s = settlementDate,d_m = maturityDate,d_dated = 0,d_f_cpn = 0,cpn = coupon,princ_m = this$.principal,price = price,stat = 10)
    
    couponDays <- as.numeric(nextCouponDate - previousCouponDate)
    dv01 <- as.numeric(fincad(fincadFunctionName,d_s = settlementDate,d_m = maturityDate,d_dated = 0,d_f_cpn = 0,cpn = coupon/100,princ_m = this$.principal,price = price,stat = 7) * -100)
    yield <- as.numeric(fincad(fincadFunctionName,d_s = settlementDate,d_m = maturityDate,d_dated = 0,d_f_cpn = 0,cpn = coupon/100,princ_m = this$.principal,price = price,stat = 1) * 100)
    convexity <- as.numeric(fincad(fincadFunctionName,d_s = settlementDate,d_m = maturityDate,d_dated = 0,d_f_cpn = 0,cpn = coupon/100,princ_m = this$.principal,price = price,stat = 6)/100)

    return(list(couponDays = couponDays,dv01 = dv01,convexity = convexity,yield = yield))
})

method("getDailyZooSeries","BondTri",function(this,seriesList,numUnitsSett,unitSett,accRepo,accBond,fincadCountryCode,holidayList,...){

    dates <- index(seriesList[[1]])
    nbDates <- NROW(dates)
    settlementDates <- dates; couponDays <- NULL
    zooTriDaily <- zoo(0,dates); zooDv01 <- zoo(0,dates); zooYield <- zoo(0,dates); zooConvexity <- zoo(0,dates)
    zooRepoTriDaily <- zooTriDaily

    for (i in 1:nbDates){

        settlementDates[i] <- getFincadDateAdjust(dates[i], unitSett, numUnitsSett,holidayList)
        
        fincadOutputs <- this$getFincadOutputs(
            settlementDates[i],
            fincadCountryCode,
            seriesList$price.lead[i],
            seriesList$coupon.lead[i],
            seriesList$maturity.lead[i]
        )
        
        couponDays[i] <- fincadOutputs$couponDays
        zooDv01[i] <- fincadOutputs$dv01
        zooYield[i] <- fincadOutputs$yield
        zooConvexity[i] <- fincadOutputs$convexity
        
        if(i > 1){
            priceChange <- ifelse(
                as.numeric(seriesList$maturity.lead[i])==as.numeric(seriesList$maturity.lead[i-1]),
                as.numeric(seriesList$price.lead[i])-as.numeric(seriesList$price.lead[i-1]),
                ifelse(
                    as.numeric(seriesList$maturity.lag[i])==as.numeric(seriesList$maturity.lead[i-1]),
                    as.numeric(seriesList$price.lag[i])-as.numeric(seriesList$price.lead[i-1]),
                    0 #throw("error in BondTri$getDailyZooSeries !!!")
                )
            )
            
            # bond accrue t+0, repo t+0
            bondAccrued <- as.numeric(as.Date(dates[i]) - as.Date(dates[i-1])) * as.numeric(seriesList$coupon.lead[i-1]/100) * this$.principal / couponDays[i-1] / 2
            repoAccrued <- IRSTri$getAccrual(as.numeric(seriesList$repo_on.lead[i-1]),dates[i-1],dates[i],accRepo) # repo accrues
            
            zooRepoTriDaily[i] <- as.numeric(repoAccrued)
            zooTriDaily[i] <- as.numeric(priceChange + bondAccrued - zooRepoTriDaily[i])
        }
    }
    zooTriDaily <- getZooDataFrame(zooTriDaily[-1])
    zooRepoTriDaily <- getZooDataFrame(zooRepoTriDaily[-1])    
    zooDv01 <- getZooDataFrame(zooDv01[-1])
    zooYield <- getZooDataFrame(zooYield[-1])
    zooConvexity <- getZooDataFrame(zooConvexity[-1])
    return(list(zooTriDaily = zooTriDaily,zooDv01 = zooDv01,zooYield = zooYield,zooConvexity = zooConvexity, zooRepoTriDaily = zooRepoTriDaily))
})

method("cleanSeries","BondTri",function(this,seriesList,...){
    m <- do.call(merge,seriesList)
    m <- m[!is.na(m[,"price.lead"]) & !is.na(m[,"maturity.lead"]) & !is.na(m[,"coupon.lead"]) & !is.na(m[,"repo_on.lead"]),] # insure that all the lead info is valid
    for (i in 1:NROW(seriesList))seriesList[[i]] <- m[,i]
    return(seriesList)
})

method("getTimeSeries","BondTri",function(this,startDate,endDate,ccy,maturity,sector,issuer,data.source,timeStamp,...){

    quote_convention_list <- c("price","maturity","coupon","repo_on")
    coupon_type_list <- c(this$.couponTypeLead,this$.couponTypeLag)
    coupon_types <- c("lead","lag")
    cNames <- NULL; seriesList <- NULL
    k <- 0
    for(i in 1:NROW(quote_convention_list)){
        for(j in 1:NROW(coupon_type_list)){
            k <- k + 1
            cNames <- c(cNames,paste(quote_convention_list[i],coupon_types[j],sep = "."))
            tsName <- paste(this$.instrument,sector,ccy,maturity,coupon_type_list[j],quote_convention_list[i],sep = "_")
            series <- (this$.tsdb)$retrieveTimeSeriesByName(tsName,data.source = data.source,start = startDate,end = endDate)
            series <- make.zoo.daily(series[[1]],timeStamp)
            seriesList[[k]] <- series
        }
    }
    seriesList <- structure(seriesList, .Names = cNames)
    return(seriesList)
})

method("run","BondTri",function(this,startDate,endDate,ccy,financialCenter,data.source,maturity,sector,issuer,timeStamp,
    numUnitsSett,unitSett,accRepo,accBond,fincadCountryCode,updateTSDB = FALSE,...){
    
    # get holiday list

    holidayList <- HolidayDataLoader$getHolidaysToTenor(this$.holidaySource,financialCenter,characterToNumericTenor(maturity),startDate,endDate)

    # get underlying time series

    seriesList <- this$getTimeSeries(startDate,endDate,ccy,maturity,sector,issuer,data.source,timeStamp)
    seriesList <- this$cleanSeries(seriesList)
    
    firstDate <- as.POSIXct(paste(first(index(seriesList[[1]])),timeStamp))
    returnDates <- as.POSIXct(paste(index(seriesList[[1]])[-1],timeStamp))

    # get transformed data

    zooList <- this$getDailyZooSeries(seriesList,numUnitsSett,unitSett,accRepo,accBond,fincadCountryCode,holidayList)

    # put time stamps back

    triDaily <- getZooDataFrame(zoo(as.numeric(zooList$zooTriDaily),returnDates))
    dv01 <- getZooDataFrame(zoo(as.numeric(zooList$zooDv01),returnDates))
    yield <- getZooDataFrame(zoo(as.numeric(zooList$zooYield),returnDates))
    convexity <- getZooDataFrame(zoo(as.numeric(zooList$zooConvexity),returnDates))
    repoTriDaily <- getZooDataFrame(zoo(as.numeric(zooList$zooRepoTriDaily),returnDates))

    # tsdb updates

    if(updateTSDB){
        tsNameDv01 <- paste(this$.instrument,sector,ccy,maturity,this$.couponTypeLead,"dv01",sep = "_")
        tsNameYield <- paste(this$.instrument,sector,ccy,maturity,this$.couponTypeLead,"yield",sep = "_")
        tsNameConvexity <- paste(this$.instrument,sector,ccy,maturity,this$.couponTypeLead,"convexity",sep = "_")
        tsNameTriDaily <- paste(this$.instrument,sector,ccy,maturity,this$.modified,"tri_daily",sep = "_")
        tsNameTriCum <- paste(this$.instrument,sector,ccy,maturity,this$.modified,"tri",sep = "_")
        tsNameRepoTriDaily <- paste(this$.instrument,sector,ccy,maturity,this$.modified,"repo_on_tri_daily",sep = "_")
        tsNameRepoTriCum <- paste(this$.instrument,sector,ccy,maturity,this$.modified,"repo_on_tri",sep = "_")        

        attributeListDv01 <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "dv01",instrument = this$.instrument,issuer = issuer,sector = sector,coupon_type = this$.couponTypeLead)
        attributeListYield <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "yield",instrument = this$.instrument,issuer = issuer,sector = sector,coupon_type = this$.couponTypeLead)
        attributeListConvexity <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "convexity",instrument = this$.instrument,issuer = issuer,sector = sector,coupon_type = this$.couponTypeLead)
        attributeListTriDaily <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "tri_daily",instrument = this$.instrument,issuer = issuer,sector = sector,modified = this$.modified)
        attributeListTriCum <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "tri",instrument = this$.instrument,issuer = issuer,sector = sector,modified = this$.modified)
        attributeListRepoTriDaily <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "repo_on_tri_daily",instrument = this$.instrument,issuer = issuer,sector = sector,modified = this$.modified)
        attributeListRepoTriCum <- list(quote_type = "close",ccy = ccy,maturity = maturity,quote_side = "mid",quote_convention = "repo_on_tri",instrument = this$.instrument,issuer = issuer,sector = sector,modified = this$.modified)        

        (this$.tsdb)$createAndWriteOneTimeSeriesByName(dv01,tsNameDv01,this$.triSource,attributeListDv01)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(yield,tsNameYield,this$.triSource,attributeListYield)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(convexity,tsNameConvexity,this$.triSource,attributeListConvexity)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(triDaily,tsNameTriDaily,this$.triSource,attributeListTriDaily)
        (this$.tsdb)$createAndWriteOneTimeSeriesByName(repoTriDaily,tsNameRepoTriDaily,this$.triSource,attributeListRepoTriDaily)        
        updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source = this$.triSource,this$.principal)
        updateCumTriFromDailyTri(this$.tsdb,firstDate,returnDates,tsNameRepoTriDaily,tsNameRepoTriCum,attributeListRepoTriCum,source = this$.triSource,this$.principal)
    }
    return(list(zooTriDaily = triDaily,zooDv01 = dv01,zooConvexity = convexity, zooYield = yield,zooRepoTriDaily = repoTriDaily))
})