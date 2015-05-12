constructor("BondIndex", function(    
    sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00"
,...){
    library(QFFutures)
    this <- extend(RObject(), "BondIndex",
        .tsdb = TimeSeriesDB(),
        .instrument = "bond_index",
        .quote_type = "close",
        .originalSource = "lehman",
        .analyticsSource = "internal",
        .principal = 100
    )
    
    if(!inStaticConstructor(this)){
        assert(sector %in% c("credit")); assert(issuer %in% c("all"));
        assert(ccy %in% c("usd")); assert(maturity %in% c("all","intermediate","long","1y_3y"))
        assert(rating %in% c("all")); assert(dataTimeStamp %in% c("15:00:00"))
        
        this$.sector = sector
        this$.issuer = issuer
        this$.ccy = ccy
        this$.maturity = maturity
        this$.rating = rating
        this$.dataTimeStamp = dataTimeStamp
    }
    
    return(this)
})



method("getInterpolatedZoo","BondIndex",function(this,termStructure,termStructureZoo,maturityZoo,requiredTenor = NULL,...){
    # Returns an interpolated zoo, the only merge requirement is that for all dates
        # there is at least on point in the curve
        # the requiredTenor exists
        # the maturity to interpolate on exis

    m <- merge(termStructureZoo,maturityZoo)
    m <- m[!is.na(m[,NCOL(m)]),]    
    if(!is.null(requiredTenor))m <- m[!is.na(m[,requiredTenor]),]
    
    interpolated <- na.omit(getInterpolatedTermStructure(
        zooCurve = m[,-NCOL(m)],
        numTenorList = characterToNumericTenor(termStructure),
        numTenorListTarget = as.numeric(m[,NCOL(m)]),
        type = 1,TRUE
    ))
    
    return(getZooDataFrame(interpolated))
})



method("getMarketValueWeights","BondIndex",function(this,subIndexList,startDate = NULL,endDate = NULL,...){

    sapply(subIndexList,function(x){this$checkTicker(x)})
    
    # get data
    market.value.data <- na.omit(getMergedTimeSeries(this$.tsdb,paste(subIndexList,"market_value",sep = "_"),"internal",startDate,endDate))

    # calculate weights
    total.market.value <- market.value.data[,1]
    for (i in 2:NCOL(market.value.data))total.market.value <- total.market.value + market.value.data[,i]
    
    return(getZooDataFrame(market.value.data/total.market.value))
})



method("getWeightedSeries","BondIndex",function(this,seriesZoo,weightsZoo,...){

    assert(NCOL(seriesZoo) == NCOL(weightsZoo))
    
    m <- na.omit(merge(seriesZoo,weightsZoo))
    weighted.series <- m[,1] * m[,NCOL(seriesZoo)+1]
    for (i in 2:NCOL(seriesZoo))weighted.series <- weighted.series + m[,i] * m[,NCOL(seriesZoo)+i]
    
    return(getZooDataFrame(weighted.series))
})



method("getRequiredTenor","BondIndex",function(this,ticker,...){
    if(ticker == "lehman_us_credit_intermediate")return("5y")
    if(ticker == "lehman_us_credit")return("10y")    
    if(ticker == "lehman_us_credit_long")return("20y")
    if(ticker == "lehman_us_credit_1y_3y")return("3y")
    return(NULL)
})

method("getAvailableTickers","BondIndex",function(this,...){
    return(c("lehman_us_credit_intermediate","lehman_us_credit_long","lehman_us_credit_1y_3y","lehman_us_credit"))
})


method("getAttributeList","BondIndex",function(this,ticker,index_version,quote_convention,...){

    this$checkTicker(ticker)
    
    return(list(
        instrument = this$.instrument, ticker = ticker, index_version = index_version,
        sector = this$.sector, issuer = this$.issuer, ccy = this$.ccy, quote_type = this$.quote_type,
        maturity = this$.maturity, rating = this$.rating,quote_convention = quote_convention
    ))
})


method("getTsName","BondIndex",function(this,ticker,index_version,quote_convention,...){

    this$checkTicker(ticker)
    
    tsNamePref <- ifelse(index_version == "adjusted",squish(ticker,"_",index_version),ticker)
    
    return(paste(tsNamePref,quote_convention,sep = "_"))
})


method("checkTicker","BondIndex",function(this,ticker,...){ assert(ticker %in% this$getAvailableTickers()) })

method("updateTSDB","BondIndex",function(this,ts,ticker,index_version,quote_convention,...){

    (this$.tsdb)$createAndWriteOneTimeSeriesByName(ts,
        this$getTsName(ticker,index_version,quote_convention),
        this$.analyticsSource,this$getAttributeList(ticker,index_version,quote_convention)
    )
})

method("arbitrateRawData","BondIndex",function(this,ticker,startDate = NULL,endDate = NULL,...){

    this$checkTicker(ticker)
    
    # No arbitration at this point, just copying the raw data
    matrix <- (this$.tsdb)$retrieveTimeSeriesByAttributeList(attributes = list(
        ticker = ticker,index_version = "raw",
        quote_convention = c("yield_to_worst","maturity","market_value","oas","dv01")
    ), data.source = this$.originalSource,start = startDate, end = endDate)
    
    return(TSDataLoader$loadMatrixInTSDB(matrix,NULL,this$.analyticsSource))
})

################################# Util Functions ###############################


createBondIndexTimeSeries <- function(){

    ticker <- "lehman_us_credit_1y_3y"
    this <- BondIndex(maturity = "1y_3y")
    quote_convention_list <- c("dv01") #c("tri","tri_daily","maturity","yield_to_worst","oas","dv01")
    index_version_list <- c("adjusted") # "adjusted"
    for (i in 1:NROW(index_version_list)){
        for (k in 1:NROW(quote_convention_list)){
            attrList <- this$getAttributeList(ticker,index_version_list[i],quote_convention_list[k])
            tsName <- this$getTsName(ticker,index_version_list[i],quote_convention_list[k])
            print(tsName)
            tsdb$createTimeSeries(tsName, attrList)
        }
    }
}

backFillMarketValueData <- function(){

    tickerList <- this$getAvailableTickers()

    for (j in 1:NROW(tickerList)){
    		tsNames <- c(paste(tickerList[j],"market_value",sep = "_"),paste(tickerList[j],"tri",sep = "_"))
    		print(tsNames)
    		tsdb$purgeTimeSeries(tsNames[1],"internal")
    		data <- getMergedTimeSeries(tsdb,tsNames,"lehman")
    		m <- (na.locf(data[!is.na(data[,2]),],rev = TRUE))
    		tsdb$writeOneTimeSeriesByName(m[,1],tsNames[1],"internal")
    		print("Done")
    }
}