setConstructorS3("TimeSeriesStitcher", function(...)
{
    library(QFCredit)
    library(QFFixedIncome)
    extend(RObject(), "TimeSeriesStitcher",
            .zooObj = NULL,
            .tsObj = NULL,
            .lenObj = NULL,
            .winLength = NULL,
            .numWindows = NULL,
            .randomSelectors = NULL,
            .replacement = FALSE,
            .tsOut = NULL        
    )
})

setMethodS3("loadZooObj", "TimeSeriesStitcher", function(this, zooObj, ...)
{
    needs(zooObj = "zoo")
    assert(NROW(zooObj) > 1)
    this$.zooObj <- zooObj
    this$.tsObj <- ts(this$.zooObj)
    this$.lenObj <- NROW(this$.tsObj)
})

setMethodS3("chooseWindows", "TimeSeriesStitcher", function(this, winLength, ...)
{
    needs(winLength = "numeric")
    assert(winLength > 0)
    this$.winLength <- winLength
    numWin <- round(this$.lenObj / this$.winLength, 0)
    if((this$.lenObj / this$.winLength - numWin) > 0) numWin <- numWin + 1
    
    this$.numWindows <- numWin

    this$.randomSelectors <- sample(1:numWin, replace = this$.replacement)
})

setMethodS3("restitchSeries", "TimeSeriesStitcher", function(this, ...)
{
    this$.tsOut <- NULL
    for(i in 1:this$.numWindows)
    {
        currentSelection <- this$.randomSelectors[i]
        if(currentSelection != this$.numWindows)
        {
            this$.tsOut <- c(this$.tsOut, window(this$.tsObj,
                                                start = ((currentSelection - 1) * this$.winLength),
                                                end = (currentSelection * this$.winLength) - 1))
        }
        else this$.tsOut <- c(this$.tsOut, window(this$.tsObj,
                                                 start = ((currentSelection - 1) * this$.winLength),
                                                 end = this$.lenObj))
    }
    ts(this$.tsOut)           
})

setMethodS3("restitchTRISeries", "TimeSeriesStitcher", function(this, zooTRI, winLength, replacement = TRUE, selectNewWindows = TRUE, ...)
{
    needs(zooTRI = "zoo", winLength = "numeric", replacement = "logical", selectNewWindows = "logical")
    diffZoo <- diff(zooTRI, lag = 1) / lag(zooTRI, k = 1)
    this$loadZooObj(diffZoo)
    this$.replacement <- replacement
    if(is.null(this$.randomSelectors) || selectNewWindows) this$chooseWindows(winLength)
    stitchedChanges <- this$restitchSeries()
    
    this$.tsOut <- NULL
    
    this$.tsOut[1] <- 100
    for(i in 1:NROW(stitchedChanges))
    {
        this$.tsOut[i+1] <- this$.tsOut[i] * (1 + stitchedChanges[i])     
    }
    this$.tsOut
})

setMethodS3("restitchLevelsSeries", "TimeSeriesStitcher", function(this, zooTRI, winLength, replacement = TRUE, selectNewWindows = TRUE, ...)
{
    needs(zooTRI = "zoo", winLength = "numeric", replacement = "logical", selectNewWindows = "logical")
    diffZoo <- diff(zooTRI, lag = 1)
    this$loadZooObj(diffZoo)
    this$.replacement <- replacement
    if(is.null(this$.randomSelectors) || selectNewWindows) this$chooseWindows(winLength)
    stitchedChanges <- this$restitchSeries()
    
    this$.tsOut <- NULL
    this$.tsOut[1] <- as.numeric(zooTRI[1])
    for(i in 1:NROW(stitchedChanges))
    {
        this$.tsOut[i+1] <- this$.tsOut[i] + stitchedChanges[i]
    }
    this$.tsOut
})

setMethodS3("restitchMultipleSeries", "TimeSeriesStitcher", function(this, zooTRI, winLength, replacement = TRUE, seriesType = NULL, ...)
{
    needs(seriesType = "character")
    assert(seriesType %in% c("levels","tri"), paste(seriesType,"is not a valid SeriesType in TimeSeriesStitcher$restitchMultiplesSeries"))
    
    # calc first level
    if(seriesType == "levels")
        output <- zoo(this$restitchLevelsSeries(zooTRI[,1], winLength, replacement, selectNewWindows = TRUE))
    if(seriesType == "tri")
        output <- zoo(this$restitchTRISeries(zooTRI[,1], winLength, replacement, selectNewWindows = TRUE))
    
    if(ncol(zooTRI) > 1)
    {
        for(i in 2:ncol(zooTRI))
        {
            if(seriesType == "levels")
                newOutput <- zoo(this$restitchLevelsSeries(zooTRI[,i], winLength, replacement, selectNewWindows = FALSE))
            if(seriesType == "tri")
                newOutput <- zoo(this$restitchTRISeries(zooTRI[,i], winLength, replacement, selectNewWindows = FALSE))
            output <- merge(output,newOutput)
        }
    }
    output
})


setMethodS3("restitchTRIandSpreads", "TimeSeriesStitcher", function(this, zooSpreads, winLength, replacement = TRUE, tenor = NULL, holidaySource = NULL,
 financialCenter = NULL, irsSource, ...)
{                 
    #assert(NROW(zooTRI) == NROW(zooSpreads), "Number of rows in TRI and Spreads must be the same in TimeSeriesStitcher$restitchTRIandSpreads")
    #assert(ncol(zooTRI) == ncol(zooSpreads), "Number of columns in TRI and Spreads must be the same in TimeSeriesStitcher$restitchTRIandSpreads")
    # calc TRI first

    spreadsOutput <- zoo(this$restitchLevelsSeries(zooSpreads[,1], winLength, replacement, selectNewWindows = TRUE))

    if(ncol(zooSpreads) > 1)
    {
        for(i in 2:ncol(zooSpreads))
        {
            newOutput <- zoo(this$restitchLevelsSeries(zooSpreads[,i], winLength, replacement, selectNewWindows = FALSE))
            spreadsOutput <- merge(spreadsOutput, newOutput)
        }    
    }
    
    index(spreadsOutput) <- index(zooSpreads)
       
    irs <- IRS(); irs$setDefault()
    
    irsData <- IRSDataLoader$getCurves(irsObj = irs, source = irsSource)/100
    
    holiday <- HolidayDataLoader()
    holidayData <- holiday$getHolidays(source = holidaySource, financialCenter = financialCenter)
    
    cdsSample <- GenericCDS()
    cdsSample$setTermsGeneric(tickerID = "cdx-na-ig-hvol_snrfor_usd_xr", tenor = tenor, recovery = 0.4)
     
    spreadsOutputFlat <- getZooDataFrame(spreadsOutput[,1] / 10000)
    colnames(spreadsOutputFlat) <- tenor
    
    spreadsOutputFlat <- getZooDataFrame(spreadsOutputFlat[,match(CDSDataLoader$getTenors(),tenor)])
    colnames(spreadsOutputFlat) <- CDSDataLoader$getTenors()
    
    triIntermediateOutput <- CreditIndexTri$applyToHistory(
        ticker = "cdx-na-ig-hvol", tenor = tenor, recovery = 0.4, currency = "usd",
        cdsSource = 'internal',irsData = irsData, holidayData = holidayData,
        flatCurve = TRUE,priceOnEffectiveDate = FALSE,startDate = "2008-01-01"
    )
                                                        
    TRIOutput <- HistoricalCDSPricing$getGenericTRI(triIntermediateOutput, refBase = 100)
        
    if(ncol(zooSpreads) > 1)
    {
        for(i in 2:ncol(zooSpreads))
        {
             spreadsOutputFlat <- getZooDataFrame(spreadsOutput[,i] / 10000)
             colnames(spreadsOutputFlat) <- tenor
    
             spreadsOutputFlat <- getZooDataFrame(spreadsOutputFlat[,match(CDSDataLoader$getTenors(),tenor)])
             colnames(spreadsOutputFlat) <- CDSDataLoader$getTenors()
                                                       
             triIntermediateOutput <- CreditIndexTri$applyToHistory(
                ticker = "cdx-na-ig-hvol", tenor = tenor, recovery = 0.4, currency = "usd",
                cdsData = spreadsOutputFlat,irsData = irsData, holidayData = holidayData,lagTrigger = 2,
                flatCurve = TRUE,priceOnEffectiveDate = FALSE
            )
             
            newOutput <- HistoricalCDSPricing$getGenericTRI(triIntermediateOutput, refBase = 100)
            TRIOutput <- merge(TRIOutput,newOutput)
        }
    }
    list(spreadsOutput, TRIOutput)
})

setMethodS3("calcTRIstitched", "TimeSeriesStitcher", function(this, spreadsOutput, tenor,irsData, holidayData, ...)
{
    library(QFCredit)
    
    spreadData <- zooSpreads[na.omit(match(index(irsData), index(spreadsOutput))),]
    nbDates <- NROW(spreadData)
    
    cdsDates <- as.POSIXlt(index(spreadData))
    P <- CDSPricer()
    I <- IMMDates()
    T <- TermStructureBuilder()
    output <- NULL
    
    for(i in 2:nbDates)
    {
        strikeDate <- cdsDates[i-1]
        strike <- as.numeric(spreadData[i])
        effDate <- cdsDates[i]
        nextBusinessDate <- getFincadDateAdjust(startDate = effDate, unit = "d", NumUnits = 1, holidayList = holidayData)
        
        cdsContract <- SpecificCDS()
        cdsContract$setTermsSpecific(
            ticker = "gm",
            seniority = "snrfor",
            currency = "usd",
            docClause = "mr",
            recovery = 0.4,
            direction = "buy",
            strike = strike,
            notional = 1000000,
            effDate = effDate,
            matDate = I$maturityFromEffective(effDate = strikeDate, T$characterToNumericTenor(tenor))
        )
            
        pricerOutput <- P$getPrice(
            cdsObj = cdsContract,
            valueDate = nextBusinessDate,
            flatCurve = TRUE,
            cdsData = data.frame(cdsSpreads = as.numeric(spreadData[i]),
                                 cdsTenors = T$characterToNumericTenor(tenor)),
            irsData = data.frame(irsSpreads = as.numeric(subset(irsData,index(irsData) == cdsDates[i])),
                                 irsTenors = T$characterToNumericTenor(colnames(irsData))),
            holidayData = holidayData
            )
            
        colnames(pricerOutput) <- NULL
        
        output <- rbind(
            output,
            cbind(t(pricerOutput),
                data.frame(
                    strikeDate = strikeDate,
                    effDate = cdsContract$.effDate,
                    matDate = cdsContract$.matDate,
                    strike = cdsContract$.strike,
                    pricingDate = nextBusinessDate,
                    spread = as.numeric(spreadData[i])
                    )
             )
        )
        
            
    }
    result <- data.frame(output)
    colnames(result) <- colnames(output)
    result
        
})
    