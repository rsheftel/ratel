constructor("TBAMatrix", function(program = NULL, couponVector = NULL, settleVector= c('1n','2n','3n','4n'))
{
    this <- extend(RObject(), "TBAMatrix", .program = program, .couponVector = couponVector)
    if (inStaticConstructor(this)) return(this)
    constructorNeeds (this, program = "character", couponVector = "numeric")
    this$.couponVector = format(this$.couponVector,nsmall=1)
    this$.settleVector = settleVector
    this$.source = "internal"
    this$.tsdb <- TimeSeriesDB()
    return(this)
})

method("setHistoryFromTSDB", "TBAMatrix", function(this,...)
{
    #create ticker names
    tickerPrice <- vector('character',1)
    tickerSettle <- vector('character',1)

    count <- 1
    for (countCoupon in 1:length(this$.couponVector)){
        for (countSettle in 1:length(this$.settleVector)){
            tickerPrice[count] <- paste(this$.program,this$.couponVector[countCoupon],this$.settleVector[countSettle],'price',sep="_")
            tickerSettle[count] <- paste(this$.program,this$.couponVector[countCoupon],this$.settleVector[countSettle],'settle_date',sep="_")
            count <- count + 1
        }
    }
    #load the data from tsdb
    this$.nPriceZoo <- this$.tsdb$retrieveTimeSeriesByName(tickerPrice,this$.source)
    this$.nSettleZoo <- this$.tsdb$retrieveTimeSeriesByName(tickerSettle,this$.source)
})

method("getPriceZoo", "TBAMatrix", function(this,...) {
    return(TSDataLoader$matrixToZoo(this$.nPriceZoo))
})

method("getSettleZoo", "TBAMatrix", function(this,...) {
    return(TSDataLoader$matrixToZoo(this$.nSettleZoo))
})

method("getPriceArray", "TBAMatrix", function(this, getDateTime, couponVector, ...)
{
    #gets a single date and returns in a couponXsettle array
    needs(getDateTime = "POSIXct", couponVector = "numeric")
    
    couponVectorChar = format(couponVector,nsmall=1)
    priceGrid <- Array("numeric", "character", "numeric", couponVector, this$.settleVector)

    for (countCoupon in 1:length(couponVector)){
        for (countSettle in 1:length(this$.settleVector)){
            ticker <- paste(this$.program,couponVectorChar[countCoupon],this$.settleVector[countSettle],'price',sep="_")
            priceZoo <- this$.nPriceZoo[[ticker,this$.source]]
            
            if (getDateTime %in% index(priceZoo))
                priceValue <- as.numeric(priceZoo[getDateTime])
            else
                priceValue <- NA

            priceGrid$set(couponVector[countCoupon],this$.settleVector[countSettle], price)
        }
    }
    return(priceGrid)
})

method("getSettleDateArray", "TBAMatrix", function(this, getDateTime, couponVector,...)
{
    #get a single date and returns in a couponXsettle array
    needs(getDateTime = "POSIXct", couponVector = "numeric")

    couponVectorChar = format(couponVector,nsmall=1)
    settleDateGrid <- Array("numeric", "character", "numeric", couponVector, this$.settleVector)

    for (countCoupon in 1:length(couponVector)){
        for (countSettle in 1:length(this$.settleVector)){
            ticker <- paste(this$.program,couponVectorChar[countCoupon],this$.settleVector[countSettle],'settle_date',sep="_")
            settleDateZoo <- this$.nSettleZoo[[ticker,this$.source]]

            if (getDateTime %in% index(settleDateZoo))
                settleDateValue <- as.numeric(settleDateZoo[getDateTime])
            else
                settleDateValue <- NA
                
            settleDateGrid$set(couponVector[countCoupon],this$.settleVector[countSettle], settleDateValue)
        }
    }
    return(settleDateGrid)
})

method("getPriceMatrix", "TBAMatrix", function(this, getDateTime, couponVector, ...)
{
    #gets a single date and returns in a couponXsettle grid of type used in TBAGrid
    needs(getDateTime = "POSIXct", couponVector = "numeric")

    couponVectorChar = format(couponVector,nsmall=1)
    priceGrid <- matrix(NA, nrow = length(couponVector), ncol = length(this$.settleVector))

    for (countCoupon in 1:length(couponVector)){
        if (!is.na(match(couponVectorChar[countCoupon],this$.couponVector))){
            for (countSettle in 1:length(this$.settleVector)){
                ticker <- paste(this$.program,couponVectorChar[countCoupon],this$.settleVector[countSettle],'price',sep="_")

                if (!is.na(match(ticker,rownames(this$.nPriceZoo)))){
                    priceZoo <- this$.nPriceZoo[[ticker,this$.source]]

                    if (getDateTime %in% index(priceZoo))
                        priceValue <- as.numeric(priceZoo[getDateTime])
                    else
                        priceValue <- NA
                }
                else
                    priceValue <- NA

                priceGrid[countCoupon,countSettle] <- priceValue
            }
        }
    }
    return(priceGrid)
})

method("getSettleDateMatrix", "TBAMatrix", function(this, getDateTime, couponVector,...)
{
    #get a single date and returns settle dates in a couponXsettle grid in numeric form
    needs(getDateTime = "POSIXct", couponVector = "numeric")

    couponVectorChar = format(couponVector,nsmall=1)
    settleDateGrid <- matrix(NA, nrow = length(couponVector), ncol = length(this$.settleVector))

    for (countCoupon in 1:length(couponVector)){
        if (!is.na(match(couponVectorChar[countCoupon],this$.couponVector))){
            for (countSettle in 1:length(this$.settleVector)){
                ticker <- paste(this$.program,couponVectorChar[countCoupon],this$.settleVector[countSettle],'settle_date',sep="_")
                
                if (!is.na(match(ticker,rownames(this$.nSettleZoo)))){
                    settleDateZoo <- this$.nSettleZoo[[ticker,this$.source]]

                    if (getDateTime %in% index(settleDateZoo))
                        settleDateValue <- as.numeric(settleDateZoo[getDateTime])
                    else
                        settleDateValue <- NA
                    }
                else
                    settleDateValue <- NA

                settleDateGrid[countCoupon, countSettle] <- settleDateValue
            }
        }
    }
    return(settleDateGrid)
})

method("getSettleDateVector", "TBAMatrix", function(this, getDateTime, couponVector,...)
{
    #get a single date and returns settle dates in a vector of Numeric
    needs(getDateTime = "POSIXct", couponVector = "numeric")
    
    settleDateGrid <- this$getSettleDateMatrix(getDateTime, couponVector)
    
    settleDateVector <- rep(NA,length(couponVector))
    
    for (countSettle in 1:length(this$.settleVector)){
        goodRow <- match(TRUE, !is.na(settleDateGrid[,countSettle]))
        if (!is.na(goodRow))
            settleDateVector[countSettle] <- settleDateGrid[goodRow,countSettle]
    }
    return(settleDateVector)
})

