setConstructorS3("GenericIRS", function(...)
{
    extend(IRS(),"GenericIRS",
        .tenor = NULL,
        .forwardStart = NULL,
	.isDefinedGeneric = FALSE
        )
})

setMethodS3("setTermsGeneric","GenericIRS",function(this,currency = NULL,quoteType = NULL,quoteSide = NULL,quoteConvention = NULL,instrument = NULL,
    indexFloat = NULL,spreadFloat = NULL,resetFreqFloat = NULL,payFreqFloat = NULL,
    dayCountFloat = NULL,rateBasisFloat = NULL,payFreqFixed = NULL,dayCountFixed = NULL,busDayConvFixed=NULL,busDayConvFloat=NULL,rateBasisFixed = NULL, relevantFinancialCenters = NULL,
    forwardStart=NULL,tenor = NULL,...)
{
    # We check some inputs with the IRS$setTerms function
       
    this$setTerms(currency = currency,quoteSide = quoteSide,quoteType = quoteType,quoteConvention = quoteConvention,instrument = instrument,
        indexFloat = indexFloat,spreadFloat = spreadFloat,resetFreqFloat = resetFreqFloat,payFreqFloat = payFreqFloat,
        dayCountFloat = dayCountFloat,rateBasisFloat = rateBasisFloat,payFreqFixed = payFreqFixed,dayCountFixed = dayCountFixed,
        busDayConvFixed=busDayConvFixed,busDayConvFloat=busDayConvFloat,rateBasisFixed = rateBasisFixed, relevantFinancialCenters = relevantFinancialCenters)

    # Other checks
        
    if(!is.null(tenor)){
        tenorList <- c("1y","18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")
        assert(any(tenor == tenorList), paste(tenor, "is not a valid IRS tenor"))
        this$.tenor <- tenor
    }
    
    # update the isDefinedGeneric attribute (the minimum is a defined IRS and a tenor)
    if (!is.null(forwardStart)) {
      forwardStartList <- c("spot","1w","1m","3m","6m","9m","1y","2y","3y","4y","5y","6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y")
      assert(any(forwardStart == forwardStartList),paste(forwardStart,"is not a valid forward start for Generic IRS"))
      this$.forwardStart <- forwardStart
    }
            
    if(this$.isDefined && !is.null(this$.tenor) && !is.null(this$.forwardStart)){
        this$.isDefinedGeneric <- TRUE
    }
})

setMethodS3("setDefault","GenericIRS",function(this,currency = "usd",quoteType = "close",quoteSide = "mid",quoteConvention = "rate",instrument = "irs",
    indexFloat = "libor 3m",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",
    dayCountFloat = "act/360",rateBasisFloat = "simple interest",payFreqFixed = "semi-annual",dayCountFixed = "30/360",busDayConvFixed = "Modified Following",
    busDayConvFloat="Modified Following", rateBasisFixed = "simple interest",relevantFinancialCenters = c("nyb","lnb"), tenor = NULL, forwardStart="spot",...)
{
    this$setTermsGeneric(currency = currency,quoteSide = quoteSide,quoteType = quoteType,quoteConvention = quoteConvention,instrument = instrument,
        indexFloat = indexFloat,spreadFloat = spreadFloat,resetFreqFloat = resetFreqFloat,payFreqFloat = payFreqFloat,
        dayCountFloat = dayCountFloat,rateBasisFloat = rateBasisFloat,payFreqFixed = payFreqFixed,dayCountFixed = dayCountFixed,
        busDayConvFixed=busDayConvFixed, busDayConvFloat=busDayConvFloat, rateBasisFixed = rateBasisFixed,relevantFinancialCenters = relevantFinancialCenters, tenor = tenor, forwardStart= forwardStart)
})

setMethodS3("summary", "GenericIRS", function(this,...)
{
    cat("Currency: ",this$.currency,"\n")
    cat("Quote Type: ", this$.quoteType,"\n")
    cat("Quote Side: ", this$.quoteSide,"\n")
    cat("Quote Convention: ", this$.quoteConvention, "\n")
    cat("Instrument: ", this$.instrument, "\n", "\n")
    cat("Index: ", this$.indexFloat, "\n")
    cat("Spread to index: ", this$.spreadFloat, "\n")
    cat("Reset frequency (floating): ", this$.resetFreqFloat, "\n")
    cat("Payment frequency (floating): ", this$.payFreqFloat, "\n")
    cat("Rate basis (floating): ", this$.rateBasisFloat, "\n")
    cat("Day count (floating): ", this$.dayCountFloat, "\n","\n")
    cat("Payment frequency (fixed): ", this$.payFreqFixed, "\n")
    cat("Rate basis (fixed): ", this$.rateBasisFixed, "\n")    
    cat("Day count (fixed): ", this$.dayCountFixed, "\n","\n")
    cat("Relevant Financial Centers (bus days):",this$getRelevantFinancialCenters(),"\n")
    cat("Forward Start:",this$.forwardStart,"\n")
    cat("Tenor: ", this$.tenor,"\n")
})