constructor("CdsIndexTriTransformation", function(tsdb.ticker = NULL, live.ticker = NULL, ...) {
    this <- extend(Transformation(), "CdsIndexTriTransformation", 
        .tsdb.ticker = tsdb.ticker, 
        .live.ticker = live.ticker,
        .tsdb.swap.tenors = c(2:10, 20, 30),
        .tsdb.swap.string.tenors = c("2y","3y","4y","5y","6y","7y","8y","9y","10y","20y","30y")
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, tsdb.ticker="character", live.ticker="character")

    this$setDate(as.POSIXct(trunc(Sys.time(), "days")))
    this$.marketdata.ticker = squish("TRI.",getSystemDBCreditMarketName(tsdb.ticker,"5y"))
    cat("CdsIndexTriTransformation(", this$.tsdb.ticker, ")\n")
    this
})

method("setDate",  "CdsIndexTriTransformation", function(this, date, ...) {
    needs(date="POSIXct")
    this$.data <- CdsTransformationDailyData(this$.tsdb.ticker, date)
})

method(".inputSeries", "CdsIndexTriTransformation", function(this, ...) {
    c(
        list(Spread = SeriesDefinition("CDS", squish(this$.live.ticker, "_5y"), "LastSpread")),
        SwapCache$swapDefinitions()
    )
})

method(".outputSeries", "CdsIndexTriTransformation", function(this, ...) {
    defROLLINGREGRESSION <- function(field) SeriesDefinition("CDSTRI", squish(this$.live.ticker, "_5y"), field)
    defMARKETDATA <- function(field) SeriesDefinition("MARKETDATA", this$.marketdata.ticker, field)

    list(
        LastMV = defROLLINGREGRESSION("LastMV"),
        LastTRI = defROLLINGREGRESSION("LastTRI"),
        Timestamp = defROLLINGREGRESSION("Timestamp"),
        HighPrice = defMARKETDATA("HighPrice"),
        LastPrice = defMARKETDATA("LastPrice"),
        LastVolume = defMARKETDATA("LastVolume"),
        LowPrice = defMARKETDATA("LowPrice"),
        OpenPrice = defMARKETDATA("OpenPrice"),
        MDTimestamp = defMARKETDATA("Timestamp")
    )
})

method("initialize", "CdsIndexTriTransformation", function(this, ...) {
    this$.setIndexDetails()
    "SUCCESS"
})

method(".setIndexDetails", "CdsIndexTriTransformation", function(this, ...) {
    effective.date <- this$.data$date(1)
    conn <- SQLConnection()
    conn$init()
    details <- conn$select(squish("
        select * 
        from cds_index_details a
        where 
            ticker_name = '", this$.tsdb.ticker, "' and 
            tenor = '5y' and
            effective_date in (
                select max(effective_date)
                from cds_index_details b
                where 
                    a.ticker_name = b.ticker_name and 
                    b.effective_date <= '", as.character(effective.date), "'
            )
        order by effective_date
    "))
    failUnless(nrow(details) == 1, nrow(details), " rows returned from cds_index_details query.\n", details)
    print(details)
    this$.strike <- as.numeric(details[[1, "strike_bp"]])
    this$.effective.date <- as.POSIXct(details[1, "effective_date"])
    this$.maturity.date <- as.POSIXct(details[1, "maturity_date"])
})

method("skipUpdate", "CdsIndexTriTransformation", function(this, inputs, ...) {
    if(this$changed(inputs, "Spread")) return(FALSE)
    !SwapCache$needsUpdate()
})

method("indexMV", "CdsIndexTriTransformation", function(this, spread, irsData, valueDate, ...) {
    pricerSample <- CDSPricer(holidayList = this$.data$holidays())
    cdsTable <- pricerSample$getCDSTable(this$.effective.date,as.numeric(spread / 10000),"5y",cdsEffDates = NULL,cdsMatDates = NULL,cdsTableType = "singleName")
    cdsTable <- pricerSample$flatCurveAdjustment(cdsTable,this$.maturity.date)
    output <- pricerSample$getFincadPrice(
        direction = "buy",
        strike = this$.strike/10000,
        notional = 1000000,
        recovery = 0.4,
        valueDate = valueDate,
        effDate = this$.effective.date,
        matDate = this$.maturity.date,
        cdsTable = cdsTable,
        dfTable = irsData
    )	
    first(the(output))
})

method("closingSpread", "CdsIndexTriTransformation", function(this, ...) {
    lazy(this$.closing.index.spread, this$.data$oneClose(squish(this$.tsdb.ticker, '_market_spread_5y_otr')) * 10000)
})

method("closingTRI", "CdsIndexTriTransformation", function(this, ...) {
    lazy(this$.closing.tri, this$.data$oneClose(squish(this$.tsdb.ticker, '_tri_daily_5y_otr')))
})

method(".borkedTSDBTicker", "CdsIndexTriTransformation", function(this, ...) {
    toupper(gsub("-", "", this$.tsdb.ticker))
})

method("closingMV", "CdsIndexTriTransformation", function(this, ...) {
    if(!is.null(this$.closing.mv)) return(this$.closing.mv)
    swaps <- sapply(this$.tsdb.swap.tenors, function(t) { this$.data$oneClose(squish("irs_usd_rate_", t, "y_mid")) }) / 100;
    
    builder <- SwapCurveBuilder()
    
    this$.closing.mv <- ifElse(any(is.na(swaps)), 
        NA,
        this$indexMV(
            this$closingSpread(), 
            builder$getSmoothedDiscountFactors(curveDate = this$.data$yesterday(),cashRates = NULL,cashTenors = NULL,swapRates = swaps,swapTenors = this$.tsdb.swap.string.tenors)[,-3], 
            this$.data$date()
        )
    )
})

method("outputValues", "CdsIndexTriTransformation", function(this, inputs, ...) {
    irsData <- SwapCache$data(inputs)
    cat(squish("Using swap data:\n", humanString(irsData[[1]]), "\n"))
    currentSpread <- this$value(inputs, "Spread")
    cdsData <- data.frame(currentSpread, 5)
    currentMV <- this$indexMV(currentSpread, irsData, this$.data$date(1))
    currentTRI <- (currentMV - this$closingMV()) / 10000 + this$closingTRI()
    list(
        this$outputs()$LastMV$valueString(currentMV),
        this$outputs()$LastTRI$valueString(currentTRI),
        this$outputs()$Timestamp$now(),
        this$outputs()$HighPrice$valueString(currentTRI),
        this$outputs()$LastPrice$valueString(currentTRI),
        this$outputs()$LastVolume$valueString(currentTRI),
        this$outputs()$LowPrice$valueString(currentTRI),
        this$outputs()$OpenPrice$valueString(currentTRI),
        this$outputs()$MDTimestamp$now()
    )
})