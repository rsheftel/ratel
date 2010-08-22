setConstructorS3("IRS", function(...)
{
    extend(RObject(), "IRS",
        .currency = NULL,
        .quoteType = NULL,
        .quoteSide = NULL,
        .quoteConvention = NULL,
        .instrument = NULL,
        .spreadFloat = NULL,
        .resetFreqFloat = NULL,
        .payFreqFloat = NULL,
        .dayCountFloat = NULL,
        .rateBasisFloat = NULL,
        .payFreqFixed = NULL,
        .dayCountFixed = NULL,
        .rateBasisFixed = NULL,
        .indexFloat = NULL,
        .busDayConvFixed = NULL,
        .busDayConvFloat = NULL,
        .relevantFinancialCenters = NULL,
        .isDefined = FALSE
    )
})

setMethodS3("setTerms","IRS",function(this,currency = NULL,quoteType = NULL,quoteSide = NULL,quoteConvention = NULL,instrument = NULL,
    indexFloat = NULL,spreadFloat = NULL,resetFreqFloat = NULL,payFreqFloat = NULL,dayCountFloat = NULL,rateBasisFloat = NULL,
    payFreqFixed = NULL,dayCountFixed = NULL,rateBasisFixed = NULL, busDayConvFixed = NULL, busDayConvFloat = NULL,relevantFinancialCenters = NULL,...)
{
    # set IRS characteristics
    
    if(!is.null(currency)){
        currencyList <- c("usd")
        assert(any(currency == currencyList), paste(currency, "is not a valid IRS currency."))
        this$.currency <- currency
    }
    if(!is.null(quoteType)){
        quoteTypeList <- c("close", "open")
        assert(any(quoteType == quoteTypeList), paste(quoteType, "is not a valid IRS quote type."))
        this$.quoteType = quoteType
    }
    if(!is.null(quoteSide)){
        quoteSideList <- c("bid", "ask", "mid")
        assert(any(quoteSide == quoteSideList), paste(quoteSide, "is not a valid IRS quote side."))
        this$.quoteSide <- quoteSide
    }
    if(!is.null(quoteConvention)){
        quoteConventionList <- c("rate", "spread")
        assert(any(quoteConvention == quoteConventionList), paste(quoteConvention, "is not a valid IRS quote convention."))
        this$.quoteConvention <- quoteConvention
    }
    if(!is.null(instrument)){
        instrumentList <- c("irs")
        assert(any(instrument == instrumentList), paste(instrument, "is not a valid IRS instrument."))
        this$.instrument <- instrument
    }
    if(!is.null(indexFloat)){
        assert(class(indexFloat) == "character", paste(indexFloat, "must be of type 'character' for an IRS."))
        this$.indexFloat <- indexFloat
    }
    if(!is.null(spreadFloat)){
        assert(class(spreadFloat) == "numeric", paste(spreadFloat, "must be of type 'numeric' for an IRS."))
        this$.spreadFloat <- spreadFloat
    }
    
    frequencyList <- c("annual", "semi-annual", "quarterly", "monthly","weekly","daily")
    if(!is.null(resetFreqFloat)){
        assert(any(resetFreqFloat == frequencyList), paste(resetFreqFloat, "is not a valid IRS reset frequency."))
        this$.resetFreqFloat <- resetFreqFloat
    }
    if(!is.null(payFreqFloat)){
        assert(any(payFreqFloat == frequencyList), paste(payFreqFloat,"is not a valid IRS pay frequency."))
        this$.payFreqFloat <- payFreqFloat
    }
    if(!is.null(payFreqFixed)){
        assert(any(payFreqFixed == frequencyList), paste(payFreqFixed,"is not a valid IRS pay frequency."))
        this$.payFreqFixed <- payFreqFixed
    }
    
    frequencyList <- c("annual", "semi-annual", "quarterly", "monthly","weekly","daily","continuously","simple interest")
    if(!is.null(rateBasisFloat)){
        assert(any(rateBasisFloat == frequencyList), paste(rateBasisFloat,"is not a valid rate basis."))
        this$.rateBasisFloat <- rateBasisFloat
    }
    if(!is.null(rateBasisFixed)){
        assert(any(rateBasisFixed == frequencyList), paste(rateBasisFixed,"is not a valid rate basis."))
        this$.rateBasisFixed <- rateBasisFixed
    }
    
    dayCountList <- c("act/365","act/360","30/360","act/act")
    if(!is.null(dayCountFloat)){
        assert(any(dayCountFloat == dayCountList), paste(dayCountFloat,"is not a valid day count convention."))
        this$.dayCountFloat <- dayCountFloat
    }
    if(!is.null(dayCountFixed)){
        assert(any(dayCountFixed == dayCountList), paste(dayCountFixed,"is not a valid day count convention."))
        this$.dayCountFixed <- dayCountFixed
    }
    
    busDayConvList <- c("No Adjustment","Next Good","Previous Good","Modified Following")
    
    if (!is.null(busDayConvFixed)){
      assert(any(busDayConvFixed==busDayConvList),paste(busDayConvFixed,"is not a valid business day convention."))
      this$.busDayConvFixed <- busDayConvFixed
    }
    
    if (!is.null(busDayConvFloat)){
      assert(any(busDayConvFloat==busDayConvList),paste(busDayConvFloat,"is not a valid business day convention."))
      this$.busDayConvFloat <- busDayConvFloat
    }    
    
    financialCenterList <- c("nyb","lnb","tkb")
   
    if (!is.null(relevantFinancialCenters)) {
      assert(any(relevantFinancialCenters %in% financialCenterList),paste(relevantFinancialCenters," not correct."))
      this$.relevantFinancialCenters <- relevantFinancialCenters
    }
    
    # update isDefined
    
    if(!is.null(this$.currency) && !is.null(this$.quoteType) && !is.null(this$.quoteSide)
        && !is.null(this$.quoteConvention) && !is.null(this$.instrument) && !is.null(this$.indexFloat)
        && !is.null(this$.spreadFloat) && !is.null(this$.resetFreqFloat) && !is.null(this$.payFreqFloat)
        && !is.null(this$.payFreqFixed) && !is.null(this$.rateBasisFloat) && !is.null(this$.rateBasisFixed)
        && !is.null(this$.dayCountFloat) && !is.null(this$.dayCountFixed) && !is.null(this$.busDayConvFixed) 
        && !is.null(this$.busDayConvFloat) && !is.null(this$.relevantFinancialCenters)){
        this$.isDefined <- TRUE
    }else{
        throw("IRS$setTerms: irs object is not properly defined")
    }
})

setMethodS3("setDefault","IRS",function(this,currency = "usd",quoteType = "close",quoteSide = "mid",quoteConvention = "rate",instrument = "irs",
    indexFloat = "libor 3m",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",
    dayCountFloat = "act/360",rateBasisFloat = "simple interest",payFreqFixed = "semi-annual",dayCountFixed = "30/360",rateBasisFixed = "simple interest",
    busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",relevantFinancialCenters = c("nyb","lnb"),...)
{
    this$setTerms(currency = currency,quoteSide = quoteSide,quoteType = quoteType,quoteConvention = quoteConvention,instrument = instrument,
        indexFloat = indexFloat,spreadFloat = spreadFloat,resetFreqFloat = resetFreqFloat,payFreqFloat = payFreqFloat,
        dayCountFloat = dayCountFloat,rateBasisFloat = rateBasisFloat,payFreqFixed = payFreqFixed,dayCountFixed = dayCountFixed,
        rateBasisFixed = rateBasisFixed, busDayConvFixed = busDayConvFixed, busDayConvFloat = busDayConvFloat, relevantFinancialCenters = relevantFinancialCenters)
})

setMethodS3("getDayCountFixed","IRS",function(this,...)
{
  return(this$.dayCountFixed)
})

setMethodS3("getDayCountFloat","IRS",function(this,...)
{
  return(this$.dayCountFloat)
})

setMethodS3("getBusDayConvFixed","IRS",function(this,...)
{
  return(this$.busDayConvFixed)
})

setMethodS3("getBusDayConvFloat","IRS",function(this,...)
{
  return(this$.busDayConvFloat)
})

setMethodS3("getPayFreqFixed","IRS",function(this,...)
{
  return(this$.payFreqFixed)
})

setMethodS3("getPayFreqFloat","IRS",function(this,...)
{
  return(this$.payFreqFloat)
})

setMethodS3("getResetFreqFloat","IRS",function(this,...)
{
  return(this$.resetFreqFloat)
})

setMethodS3("getCurrency","IRS",function(this,...)
{
  return(this$.currency)
})

setMethodS3("getRelevantFinancialCenters","IRS",function(this,...)
{
  return(this$.relevantFinancialCenters)
})

setMethodS3("summary", "IRS", function(this,...)
{
    cat("Currency: ",this$getCurrency(),"\n")
    cat("Quote Type: ", this$.quoteType,"\n")
    cat("Quote Side: ", this$.quoteSide,"\n")
    cat("Quote Convention: ", this$.quoteConvention, "\n")
    cat("Instrument: ", this$.instrument, "\n", "\n")
        
    cat("Index: ", this$.indexFloat, "\n")
    cat("Spread to index: ", this$.spreadFloat, "\n")
    cat("Reset frequency (floating): ", this$getResetFreqFloat(), "\n")
    cat("Payment frequency (floating): ", this$getPayFreqFloat(), "\n")
    cat("Rate basis (floating): ", this$.rateBasisFloat, "\n")
    cat("Day count (floating): ", this$getDayCountFloat(), "\n","\n")
    cat("Payment frequency (fixed): ", this$getPayFreqFixed(), "\n")
    cat("Rate basis (fixed): ", this$.rateBasisFixed, "\n")    
    cat("Day count (fixed): ", this$getDayCountFixed(), "\n")
    cat("Business Day Conv (fixed): ",this$getBusDayConvFixed(),"\n")
    cat("Business Day Conv (float): ",this$getBusDayConvFloat(),"\n")
    cat("Relevant Financial Centers: ",this$getRelevantFinancialCenters(),"\n")
})