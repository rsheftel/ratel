setConstructorS3("FXCreateForwardSeries", function(tsdb=NULL,...)
{
    extend(RObject(), "FXCreateForwardSeries",
        .tsdb = tsdb
    )
})

setMethodS3(".insertCurrencyPair", "FXCreateForwardSeries", function(this, over, under, ...)
{
    ccy.pair <- paste(over, under, sep = "")
    conn <- SQLConnection()
    conn$init()
    over.order <- conn$select(paste("select precedence from ccy where ccy_name = '", over, "'", sep = ""))
    under.order <- conn$select(paste("select precedence from ccy where ccy_name = '", under, "'", sep = ""))

    assert(over.order[[1,1]] < under.order[[1,1]])

    res <- conn$select(paste("select 1 from ccy_pair where ccy_pair_name = '", ccy.pair, "'", sep = ""))
    if(nrow(res) == 0)
        conn$query(paste("
            insert into ccy_pair (ccy_pair_name, ccy_id1, ccy_id2)
            select
                '", ccy.pair, "',
                c1.ccy_id,
                c2.ccy_id
            from
                ccy c1,
                ccy c2
            where
                c1.ccy_name = '", over, "' and
                c2.ccy_name = '", under, "'", sep = ""))
    conn$disconnect()
})

setMethodS3("createFXForwardTimeSeriesIfNotExists", "FXCreateForwardSeries", function(this, fxForwardObj, ...)
{
    ccy.pair <- paste(fxForwardObj$getFXCurrency()$over(), fxForwardObj$getFXCurrency()$under(), sep = "")
    quote.convention <- "rate"
    quote.type <- "close"
    time.series.name <- paste(ccy.pair, fxForwardObj$getTenor(), quote.convention, fxForwardObj$getQuoteSide(), sep = "_")

    if(this$.tsdb$timeSeriesExists(time.series.name))
        return()

    this$.insertCurrencyPair(fxForwardObj$getFXCurrency()$over(), fxForwardObj$getFXCurrency()$under())

    attributes = list(
        ccy_pair = ccy.pair,
        tenor = fxForwardObj$getTenor(),
        quote_side = fxForwardObj$getQuoteSide(),
        quote_convention = quote.convention,
        quote_type = quote.type,
        instrument = "fxfwd"
    )

    this$.tsdb$createTimeSeries(name = time.series.name, attributes = attributes)
})

setMethodS3("createAllFXForwardTimeSeries", "FXCreateForwardSeries", function(this, ...)
{
    fxfwd <- FXForwardGeneric()
    ccy.list <- GetCurrencies(this$.tsdb)
    for(i in 1:(length(ccy.list)-1)) {
        for(j in (i+1):length(ccy.list)) {
            for(tenor in c("spot", "1w", "1m", "2m", "3m", "6m", "9m", "1y", "2y", "5y")) {
                fxfwdccy <- FXCurr$setByCross(paste(ccy.list[[i]], ccy.list[[j]], sep = "/"))
                fxfwd <- FXForwardGeneric(fxfwdccy,"mid",tenor)        
                this$createFXForwardTimeSeriesIfNotExists(fxfwd)
            }
        }
    }
})


setMethodS3("purgeAllFXForwardTimeSeries", "FXCreateForwardSeries", function(this, ...)
{
    fxfwd <- FXForwardGeneric()
    ccy.list <- GetCurrencies(this$.tsdb)
    for(i in 1:(length(ccy.list)-1)) {
        for(j in (i+1):length(ccy.list)) {
            for(tenor in c("spot", "1w", "1m", "2m", "3m", "6m", "9m", "1y", "2y", "5y")) {
                fxfwdccy <- FXCurr$setByCross(paste(ccy.list[[i]], ccy.list[[j]], sep = "/"))
                fxfwd <- FXForwardGeneric(fxfwdccy,"mid",tenor)        
                this$purgeFXForwardTimeSeries(fxfwd)
            }
        }
    }
})

setMethodS3("purgeFXForwardTimeSeries", "FXCreateForwardSeries", function(this, fxForwardObj, ...)
{
    ccy.pair <- paste(fxForwardObj$getFXCurrency()$over(), fxForwardObj$getFXCurrency()$under(), sep = "")
    quote.convention <- "rate"
    quote.type <- "close"
    time.series.name <- paste(ccy.pair, fxForwardObj$getTenor(), quote.convention, fxForwardObj$getQuoteSide(), sep = "_")
    cat(time.series.name,"\n")
    if(this$.tsdb$timeSeriesExists(time.series.name))
    {
      this$.tsdb$purgeTimeSeries(time.series.name,data.source="goldman")
      return()
    }
})

