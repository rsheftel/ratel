## Test file for the TriUtils class
library("GSFAnalytics")

test.updateCumTriFromDailyTri <- function()
{
    tsdb <- TimeSeriesDB()
    refDate <- "2008-01-02"
    newDates <- "2008-01-03"
    returnDates <- c("2008-01-03","2008-01-04","2008-01-07")

# Test updateCumTriFromDailyTri OneDate

    # case where the cumulative time series exists
    
        tsNameTriDaily <- "irs_usd_rate_10y_tri_daily"
        tsNameTriCum <- "irs_usd_rate_10y_tri"
        attributeListTriCum <- list(quote_type = "close",ccy = "usd",tenor = "10y",quote_convention = "rate",transformation = "tri",instrument = "irs")
        source <- "internal_test"
        baseTri <- 100
        
        # clean time series
        tsdb$purgeTimeSeries(tsNameTriDaily,source,TRUE)
        tsdb$purgeTimeSeries(tsNameTriCum,source,TRUE)
        
        # write tri_daily series
        
        tsArray <- array(list(NULL),dim = c(1,1),dimnames = list(tsNameTriDaily, source))
        tsArray[[1,1]] <- zoo(c(0.02,-0.01,0.03),returnDates)
        tsdb$writeTimeSeries(tsArray)
        
        # check when no data originally (have to start with the base)
        updateCumTriFromDailyTri(tsdb,refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri)
        triCum <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,source)
        checkEquals(triCum[[1]],zoo(as.matrix(c(100,100.02)),as.POSIXct(c("2008-01-02","2008-01-03"))))

        # check when this is just a daily update
        updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri)
        triCum <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,source)
        checkEquals(triCum[[1]],zoo(as.matrix(c(100,100.02,100.01)),as.POSIXct(c("2008-01-02","2008-01-03","2008-01-04"))))
        
    # case where the cumulative time series does not exist
        
        tsNameTriCum <- "irs_usd_rate_10y_tri_test"
        attributeListTriCum <- list(quote_type = "close",ccy = "usd",tenor = "10y",quote_convention = "rate",transformation = "tri",instrument = "test")
        
        on.exit(tsdb$.deleteTimeSeries("irs_usd_rate_10y_tri_test", are.you.sure = TRUE))
        assert(!tsdb$timeSeriesExists(tsNameTriCum))
        # check when no data originally (have to start with the base)
        updateCumTriFromDailyTri(tsdb,refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri)
        triCum <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,source)
        checkEquals(triCum[[1]],zoo(as.matrix(c(100,100.02)),as.POSIXct(c("2008-01-02","2008-01-03"))))
        
        # check when this is just a daily update
        updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri)
        triCum <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,source)
        checkEquals(triCum[[1]],zoo(as.matrix(c(100,100.02,100.01)),as.POSIXct(c("2008-01-02","2008-01-03","2008-01-04"))))
        tsdb$.deleteTimeSeries(tsNameTriCum, are.you.sure = TRUE)
        
    # test bad inputs
    
        shouldBomb(updateCumTriFromDailyTri("S","2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri))
        shouldBomb(updateCumTriFromDailyTri(tsdb,TRUE,"2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri))
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03",TRUE,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",TRUE,tsNameTriCum,attributeListTriCum,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,TRUE,attributeListTriCum,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,TRUE,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,TRUE,baseTri))
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,TRUE))
    
# Test updateCumTriFromDailyTri MultipleDates

    # test good inputs

        tsNameTriCum <- "irs_usd_rate_10y_tri"
        attributeListTriCum <- list(quote_type = "close",ccy = "usd",tenor = "10y",quote_convention = "rate",transformation = "tri",instrument = "irs")
        
        # clean time series
        tsdb$purgeTimeSeries(tsNameTriDaily,source,TRUE)
        tsdb$purgeTimeSeries(tsNameTriCum,source,TRUE)
        
        # write tri_daily series
        
        tsArray <- array(list(NULL),dim = c(1,1),dimnames = list(tsNameTriDaily, source))
        tsArray[[1,1]] <- zoo(c(0.02,-0.01,0.03),returnDates)
        tsdb$writeTimeSeries(tsArray)
        
        # check when no data originally (have to start with the base)
        updateCumTriFromDailyTri(tsdb,refDate,returnDates,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri)
        triCum <- tsdb$retrieveTimeSeriesByName(tsNameTriCum,source)
        checkEquals(triCum[[1]],zoo(as.matrix(c(100,100.02,100.01,100.04)),as.POSIXct(c(refDate,returnDates))))
        
    # test bad inputs
    
        shouldBomb(updateCumTriFromDailyTri("S","2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri))
        shouldBomb(updateCumTriFromDailyTri(tsdb,TRUE,"2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri))
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03",TRUE,tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",TRUE,tsNameTriCum,attributeListTriCum,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,TRUE,attributeListTriCum,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,TRUE,source,baseTri))    
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,TRUE,baseTri))
        shouldBomb(updateCumTriFromDailyTri(tsdb,"2008-01-03","2008-01-04",tsNameTriDaily,tsNameTriCum,attributeListTriCum,source,TRUE))
}

test.getLinearCombinationDailyTri <- function()
{
    dates <- as.POSIXct(c("2001-01-01","2001-01-02","2001-01-03"))
    zooDailyTris <- zoo(matrix(1:6,ncol = 2,nrow = 3), dates)
    zooHedgeCoefficients <- zoo(matrix(c(1,1.5,1,2,1,1),ncol = 2,nrow = 3), dates)
    
    # test base case
    result <- getLinearCombinationDailyTri(zooDailyTris,zooHedgeCoefficients) 
    target <- getZooDataFrame(zoo(matrix(c(2 + 5 * 2,3 * 1.5 + 6 * 1),ncol = 1,nrow = 2), dates[-1]))
    checkEquals(result,target)
    
    # missing hedge date first
    result <- getLinearCombinationDailyTri(zooDailyTris,zooHedgeCoefficients[-1,]) 
    checkEquals(result,getZooDataFrame(target[2]))
    # missing hedge date last    
    result <- getLinearCombinationDailyTri(zooDailyTris,zooHedgeCoefficients[-3,]) 
    checkEquals(result,target)
    # missing hedge date middle
    result <- getLinearCombinationDailyTri(zooDailyTris,zooHedgeCoefficients[-2,]) 
    target <- getZooDataFrame(zoo(matrix(c(2 + 5 * 2,3 * 1 + 6 * 2),ncol = 1,nrow = 2), dates[-1]))
    checkEquals(result,target)
    # missing return date first
    result <- getLinearCombinationDailyTri(zooDailyTris[-1,],zooHedgeCoefficients) 
    target <- getZooDataFrame(zoo(matrix(c(2 + 5 * 2,3 * 1.5 + 6 * 1),ncol = 1,nrow = 2), dates[-1]))
    checkEquals(result,getZooDataFrame(target))  
    # missing return date last
    result <- getLinearCombinationDailyTri(zooDailyTris[-3,],zooHedgeCoefficients) 
    checkEquals(result,getZooDataFrame(target[1])) 
    # missing return date middle
    result <- getLinearCombinationDailyTri(zooDailyTris[-2,],zooHedgeCoefficients) 
    checkEquals(result,getZooDataFrame(target[2])) 
    
    # test 3 dimensions
    dates <- as.POSIXct(c("2001-01-01","2001-01-02","2001-01-03"))
    zooDailyTris <- zoo(matrix(1:9,ncol = 3,nrow = 3), dates)
    zooHedgeCoefficients <- zoo(matrix(c(1,0.5,1.5,1,1.5,2,1,0.5,1),ncol = 3,nrow = 3), dates)
    
    # test base case
    result <- getLinearCombinationDailyTri(zooDailyTris,zooHedgeCoefficients) 
    target <- getZooDataFrame(zoo(matrix(c(2 + 5 + 8,3*0.5 + 6*1.5 + 9*0.5),ncol = 1,nrow = 2), dates[-1]))
    checkEquals(result,target)
    
    # ShouldBombs
    shouldBomb(getLinearCombinationDailyTri(TRUE,zooHedgeCoefficients))
    shouldBomb(getLinearCombinationDailyTri(zooDailyTris,TRUE))
    
    # holdCoefficients = FALSE

    zooHedgeCoefficients[2,2] <- NA
    result <- getLinearCombinationDailyTri(zooDailyTris,zooHedgeCoefficients,holdCoefficients = FALSE) 
    target <- getZooDataFrame(zoo(matrix(c(15),ncol = 1,nrow = 2), dates[2]))
    checkEquals(result,target)
    
}

test.getCumTriFromDailyTri <- function()
{
    zooDailyTri <- zoo(c(1,-1,0.5,2),as.POSIXct(c("2001-01-01","2001-01-02","2001-01-03","2001-01-04")))
    baseTri <- 100
    refDate <- "2000-12-31"
    
    # base case
    result <- getCumTriFromDailyTri(zooDailyTri,baseTri,refDate)
    target <- getZooDataFrame(zoo(c(100,101,100,100.5,102.5),as.POSIXct(c("2000-12-31","2001-01-01","2001-01-02","2001-01-03","2001-01-04"))))
    colnames(target) <- "TRI"
    checkEquals(target,result)
    checkEquals(target,getCumTriFromDailyTri(zooDailyTri))
    # NA in the zoo
    zooDailyTri[2] <- NA
     target <- getZooDataFrame(zoo(c(100,101,101.5,103.5),as.POSIXct(c("2000-12-31","2001-01-01","2001-01-03","2001-01-04"))))
    colnames(target) <- "TRI"
    checkEquals(target,getCumTriFromDailyTri(zooDailyTri))
    # one date
    result <- getCumTriFromDailyTri(zooDailyTri[1],baseTri,refDate[1])
    target <- getZooDataFrame(zoo(c(100,101),as.POSIXct(c("2000-12-31","2001-01-01"))))
    colnames(target) <- "TRI"
    checkEquals(target,result)
    # shouldBombs
    shouldBomb(getCumTriFromDailyTri(zooDailyTri,baseTri,TRUE))
    shouldBomb(getCumTriFromDailyTri(zooDailyTri,TRUE,refDate))
    shouldBomb(getCumTriFromDailyTri(1,baseTri,refDate))
}

test.updateDailyTriFromCumTri <- function()
{
    tsdb <- TimeSeriesDB()
    newDates <- c("2008-01-04","2008-01-07")
    refDate <- "2008-01-03"

    tsNameTriDaily <- "irs_usd_rate_10y_tri_daily"
    tsNameTriCum <- "irs_usd_rate_10y_tri"
    attributeListTriDaily <- list(quote_type = "close",ccy = "usd",tenor = "10y",quote_convention = "rate",transformation = "tri_daily",instrument = "irs")
    source <- "internal_test"
    
    # clean time series
    tsdb$purgeTimeSeries(tsNameTriDaily,source,TRUE)
    tsdb$purgeTimeSeries(tsNameTriCum,source,TRUE)
    
    # write cum tri series
    
    tsArray <- array(list(NULL),dim = c(1,1),dimnames = list(tsNameTriCum, source))
    tsArray[[1,1]] <- zoo(c(100,101,98),c(refDate,newDates))
    tsdb$writeTimeSeries(tsArray)
    
    # check
    updateDailyTriFromCumTri(tsdb,refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source)
    triDaily <- tsdb$retrieveTimeSeriesByName(tsNameTriDaily,source)
    checkEquals(triDaily[[1]],zoo(as.matrix(c(1,-3)),as.POSIXct(c("2008-01-04","2008-01-07"))))
    tsdb$purgeTimeSeries(tsNameTriDaily,source,TRUE) 
    updateDailyTriFromCumTri(tsdb,NULL,NULL,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source)
    triDaily <- tsdb$retrieveTimeSeriesByName(tsNameTriDaily,source)
    checkEquals(triDaily[[1]],zoo(as.matrix(c(1,-3)),as.POSIXct(c("2008-01-04","2008-01-07"))))
    tsdb$purgeTimeSeries(tsNameTriDaily,source,TRUE) 
    updateDailyTriFromCumTri(tsdb,"2008-01-04","2008-01-07",tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source)
    triDaily <- tsdb$retrieveTimeSeriesByName(tsNameTriDaily,source)
    checkEquals(triDaily[[1]],zoo(as.matrix(-3),as.POSIXct("2008-01-07")))
     
    # ShouldBombs
    
    shouldBomb(updateDailyTriFromCumTri("s",refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source))
    shouldBomb(updateDailyTriFromCumTri(tsdb,TRUE,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source))    
    shouldBomb(updateDailyTriFromCumTri(tsdb,refDate,TRUE,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,source))
    shouldBomb(updateDailyTriFromCumTri(tsdb,refDate,newDates,TRUE,tsNameTriCum,attributeListTriDaily,source))
    shouldBomb(updateDailyTriFromCumTri(tsdb,refDate,newDates,tsNameTriDaily,TRUE,attributeListTriDaily,source)) 
    shouldBomb(updateDailyTriFromCumTri(tsdb,refDate,newDates,tsNameTriDaily,tsNameTriCum,attributeListTriDaily,TRUE)) 
}
