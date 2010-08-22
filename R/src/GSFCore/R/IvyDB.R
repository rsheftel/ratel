library("R.oo")

setConstructorS3("IvyDB", function(dbname = "IvyDB")
{
    connection <- SQLConnection()
    connection$init(dbname = dbname)

    this <- extend(RObject(), "IvyDB",
        .connection = connection
    )
})

setMethodS3("lookupSecurityID", "IvyDB", function(this, ticker, class = "", ...)
{
    query.result <- this$.connection$select(paste("
        select 
            securityID 
        from 
            Security 
        where 
            ticker = '", toupper(ticker), "' and
            class = '", toupper(class), "' and
            exchangeDesignator != 0", sep = ""))

    assert(nrow(query.result) == 1)
    query.result[[1,1]]
})

setMethodS3("lookupTicker", "IvyDB", function(this, security.id, ...)
{
    query.result <- this$.connection$select(paste("
        select 
            ticker
        from 
            Security 
        where 
            securityID = ", security.id, sep = ""))

    assert(nrow(query.result) == 1)
    tolower(trim(query.result[1,1]))
})

setMethodS3(".checkTSDBAttributes", "IvyDB", function(this, attributes, ...)
{
    assert(is.numeric(attributes[["security_id"]]))
    if(attributes[["instrument"]] == "cm_equity_option") {
        assert(any(attributes[["expiry"]] == c("30d", "60d", "91d", "182d", "365d", "547d", "730d", "912d", "1095d")))
        assert(any(attributes[["option_type"]] == c("call", "put")))
        assert(any(attributes[["quote_type"]] == c("close", "strike")))
        if(attributes[["quote_type"]] == "close") {
            assert(any(attributes[["quote_convention"]] == c("vol_ln", "delta")))
            assert(attributes[["quote_side"]] == "mid")
        }
    }

    if(attributes[["instrument"]] == "equity") {
        assert(any(attributes[["quote_type"]] == c("close", "shares_outstanding", "volume")))
        if(attributes[["quote_type"]] == "close") {
            assert(any(attributes[["quote_convention"]] == c("price", "total_return", "total_return_factor")))
            assert(attributes[["quote_side"]] == "mid")
        }
    }

})

setMethodS3(".completeQuery", "IvyDB", function(this, query, start, end, ...)
{
    if(!is.null(start))
        query <- paste(query, " and
            date >= '", start, "'", sep = "")

    if(!is.null(end))
        query <- paste(query, " and
            date <= '", end, "'", sep = "")

    query.results <- this$.connection$select(query)

    zoo(as.matrix(query.results[["observation_value"]]), order.by = as.POSIXct(query.results[["observation_time"]]))
})

setMethodS3(".retrieveCMEquityOptionData", "IvyDB", function(this, security.id, expiry, option.type, field, start, end, ...)
{
    query <- paste("
        select 
            date observation_time, 
            ", field, " observation_value
        from 
            StdOptionPrice 
        where 
            securityID = ", security.id, " and
            days = ", expiry, " and
            callPutFlag = '", option.type, "'
    ", sep = "")

    this$.completeQuery(query, start, end)
})

setMethodS3(".retrieveEquityData", "IvyDB", function(this, security.id, field, start, end, ...)
{
    query <- paste("
        select 
            date observation_time, 
            ", field, " observation_value
        from 
            SecurityPrice 
        where 
            securityID = ", security.id, 
    sep = "")

    this$.completeQuery(query, start, end)
})

setMethodS3("retrieveOneTimeSeriesByAttributeList", "IvyDB", function(this, attributes, data.source, start = NULL, end = NULL, ...)
{
    this$.checkTSDBAttributes(attributes)
    assert(data.source == "ivydb")

    # For convenience in switching to tsdb down the road, we replicate the interface of TimeSeriesDB.  This method just dispatches to others
    switch(attributes[["instrument"]],
        cm_equity_option = this$.retrieveCMEquityOptionData(
            security.id = attributes[["security_id"]], 
            expiry = as.integer(strsplit(attributes[["expiry"]], "d")[[1]][[1]]), 
            option.type = toupper(substr(attributes[["option_type"]], 1,1)),
            field = switch(attributes[["quote_type"]], 
                close = switch(attributes[["quote_convention"]], vol_ln = "impliedVolatility", delta = "delta"),
                strike = "strikePrice"),
            start = start,
            end = end),
        equity = this$.retrieveEquityData(
            security.id = attributes[["security_id"]],
            field = switch(attributes[["quote_type"]],
                close = switch(attributes[["quote_convention"]], price = "closePrice", total_return = "totalReturn", total_return_factor = "cumulativeTotalReturnFactor"),
                shares_outstanding = "sharesOutstanding",
                volume = "volume"),
            start = start,
            end = end),

        throw("Unknown instrument type: ", attributes[["instrument"]])
    )
})

