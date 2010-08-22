updateCumTriFromDailyTri <- function(tsdb,refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri){

    needs(tsdb = "TimeSeriesDB?",tsNameTriDaily = "character?",tsNameTriCum = "character?",attributeListTriCum = "list?",source = "character?",baseTri = "numeric?")
    
    # loads daily on newDate (breaks if no data)
    
    dailyNewData <- tsdb$retrieveTimeSeriesByName(tsNameTriDaily,start = first(newDates), end = last(newDates), data.source = source)[[1]]
    dailyNewData <- as.numeric(dailyNewData)
    
    # loads cum on refDate, if no data => save base notional for that date
    
    cumRefData <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,start = refDate, end = refDate,  data.source = source)[[1]]
    if(is.null(cumRefData))cumRefData <- baseTri
    cumRefData <- as.numeric(cumRefData)
    
    # save back cum on newDate
    
    tsdb$createAndWriteOneTimeSeriesByName(
        ts = getZooDataFrame(zoo(c(cumRefData + diffinv(dailyNewData)),c(refDate,newDates))),
        name = tsNameTriCum,data.source = source,attributes = attributeListTriCum
    )
}

updateDailyTriFromCumTri <- function(tsdb,refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source){

    needs(tsdb = "TimeSeriesDB?",tsNameTriDaily = "character?",tsNameTriCum = "character?",attributeListTriDaily = "list?",source = "character?")
    if(is.null(newDates) || is.null(refDate)){
        start <- NULL;end <- NULL    
    }else{
        start <- refDate; end <- last(newDates)  
    }
    cumNewData <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,start = start, end = end, data.source = source)[[1]]
    tsdb$createAndWriteOneTimeSeriesByName(ts = diff(cumNewData),name = tsNameTriDaily,data.source = source,attributes = attributeListTriDaily)
}

getLinearCombinationDailyTri <- function(zooDailyTris,zooHedgeCoefficients,holdCoefficients = TRUE){
    # This is doing: linearCombinationDailyTri(t) = zooHedgeCoefficients(t-1) * zooDailyTris(t)
    # Optional: this holds coefficients constant from one date to the other in case of a missing coefficient date
    nCol <- NCOL(zooDailyTris)
    m <- merge(zooDailyTris,zooHedgeCoefficients)
    if(holdCoefficients)m <- na.omit(merge(m[,1:nCol],na.locf(lag(m[,(nCol+1):(2 * nCol)],-1))))
    else m <- na.omit(merge(m[,1:nCol],lag(m[,(nCol+1):(2 * nCol)],-1)))
    res <- zoo(0,index(m))
    for(i in 1:nCol)res <- res + m[,i] * m[,nCol + i]
    return(getZooDataFrame(res))
}

getCumTriFromDailyTri <- function(zooDailyTri,baseTri = 100,refDate = NULL){
    needs(zooDailyTri = "zoo",baseTri = "numeric")
    zooDailyTri <- na.omit(zooDailyTri)
    baseTri <- as.numeric(baseTri)
    returnValues <- as.numeric(zooDailyTri)
    returnDates <- as.POSIXct(index(zooDailyTri))
    if(is.null(refDate))refDate <- as.character(as.Date(first(returnDates))-1)
    refDate <- as.POSIXct(refDate)
    res <- getZooDataFrame(zoo(c(baseTri + diffinv(returnValues)),c(refDate,returnDates)))
    colnames(res) <- "TRI"
    return(res)
}