setConstructorS3("SpecificIRS", function(...)
{
    extend(IRS(),"SpecificIRS",
        .direction = NULL,
    	.notional = NULL,
    	.coupon = NULL,
    	.effDate = NULL,
    	.matDate = NULL,
    	.isDefinedSpecific = FALSE
     )
})

setMethodS3("setTermsSpecific","SpecificIRS",function(this,currency = NULL,quoteType = NULL,quoteSide = NULL,quoteConvention = NULL,instrument = NULL,
    indexFloat = NULL,spreadFloat = NULL,resetFreqFloat = NULL,payFreqFloat = NULL,
    dayCountFloat = NULL,rateBasisFloat = NULL,payFreqFixed = NULL,dayCountFixed = NULL,rateBasisFixed = NULL,busDayConvFixed = NULL, busDayConvFloat = NULL,
    notional = NULL,direction = NULL,coupon = NULL, effDate = NULL, matDate = NULL,relevantFinancialCenters=NULL,...)
{
    # We check some inputs with the IRS$setTerms function
       
    this$setTerms(currency = currency,quoteSide = quoteSide,quoteType = quoteType,quoteConvention = quoteConvention,instrument = instrument,
        indexFloat = indexFloat,spreadFloat = spreadFloat,resetFreqFloat = resetFreqFloat,payFreqFloat = payFreqFloat,
        dayCountFloat = dayCountFloat,rateBasisFloat = rateBasisFloat,payFreqFixed = payFreqFixed,dayCountFixed = dayCountFixed,
        rateBasisFixed = rateBasisFixed,busDayConvFixed = busDayConvFixed, busDayConvFloat = busDayConvFloat, relevantFinancialCenters = relevantFinancialCenters)

    # Other checks
    
    if(!is.null(direction)){
        directionList <- c("pay", "receive")
        assert(any(direction == directionList), paste(direction,"is not a valid IRS direction."))
        this$.direction <- direction
    }
    if(!is.null(notional)){
        assert(class(notional) == "numeric" && notional > 0, paste (notional, "is not a valid IRS notional"))
        this$.notional <- notional
    }
    if(!is.null(coupon)){
        assert(class(coupon) == "numeric" && coupon > 0, paste (coupon, "is not a valid IRS coupon."))
        this$.coupon <- coupon
    }
        
    if(!is.null(effDate)){
        effDate <- as.POSIXlt(effDate)
        this$.effDate <- effDate
    }
    if(!is.null(matDate)){
        matDate <- as.POSIXlt(matDate)
        this$.matDate <- matDate
    }
    if(!is.null(this$.effDate) && !is.null(this$.matDate)){
        if(this$.matDate <= this$.effDate){
            this$.effDate <- NULL
            this$.matDate <- NULL
            this$.isDefinedSpecific <- FALSE
            throw("SpecificIRS$setTermsSpecific: maturity date must be greater than effective date")
        }
    }
    
    # update the isDefinedSpecific attribute: can we price the specific IRS?
        
    if(this$.isDefined && !is.null(this$.direction) && !is.null(this$.effDate) && !is.null(this$.matDate)
        && !is.null(this$.coupon) && !is.null(this$.notional)){
        this$.isDefinedSpecific <- TRUE
    }
})

setMethodS3("setDefault","SpecificIRS",function(this,currency = "usd",quoteType = "close",quoteSide = "mid",quoteConvention = "rate",instrument = "irs",
    indexFloat = "libor 3m",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",
    dayCountFloat = "act/360",rateBasisFloat = "simple interest",payFreqFixed = "semi-annual",dayCountFixed = "30/360",rateBasisFixed = "simple interest",
    busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",relevantFinancialCenters=c("nyb","lnb"),notional = 1000000,direction = NULL,coupon = NULL, effDate = NULL, matDate = NULL,...)
{
    this$setTermsSpecific(currency = currency,quoteSide = quoteSide,quoteType = quoteType,quoteConvention = quoteConvention,instrument = instrument,
        indexFloat = indexFloat,spreadFloat = spreadFloat,resetFreqFloat = resetFreqFloat,payFreqFloat = payFreqFloat,
        dayCountFloat = dayCountFloat,rateBasisFloat = rateBasisFloat,payFreqFixed = payFreqFixed,dayCountFixed = dayCountFixed,
        rateBasisFixed = rateBasisFixed,busDayConvFixed=busDayConvFixed,busDayConvFloat=busDayConvFloat,relevantFinancialCenters = relevantFinancialCenters,notional = notional, direction = direction, coupon = coupon,effDate = effDate,matDate = matDate)
})

setMethodS3("setDefaultUsingTenorAndForwardStart","SpecificIRS",function(this,currency = "usd",quoteType = "close",quoteSide = "mid",quoteConvention = "rate",instrument = "irs",
    indexFloat = "libor 3m",spreadFloat = 0,resetFreqFloat = "quarterly",payFreqFloat = "quarterly",
    dayCountFloat = "act/360",rateBasisFloat = "simple interest",payFreqFixed = "semi-annual",dayCountFixed = "30/360",rateBasisFixed = "simple interest",
    busDayConvFixed="Modified Following",busDayConvFloat="Modified Following",relevantFinancialCenters=c("nyb","lnb"),notional = 1000000,direction = NULL,coupon = NULL,
    tradeDate=NULL, tenor=NULL, forwardStart = NULL, holidayList = NULL,...)
{   
  if(!is.null(tenor)){
        tenorList <- c("18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y", "25y", "30y", "40y")
        assert(any(tenor == tenorList), paste(tenor, "is not a valid IRS tenor"))
  }
  
  forwardStartList <- c("spot","1w","1m","3m","6m","9m","1y","2y","3y","4y","5y","6y", "7y", "8y", "9y", "10y", "12y", "15y", "20y")
  assert(any(forwardStart == forwardStartList),paste(forwardStart,"is not a valid forward start"))
  date.list <- parseSimpleTenor(forwardStart) 
  
  effDate <- as.POSIXct(getFincadDateAdjust(as.POSIXct(tradeDate),"d",2,holidayList))
  effDate <- as.POSIXct(getFincadDateAdjust(effDate,date.list$unit,date.list$numUnits,holidayList))
  
  date.list <- parseSimpleTenor(tenor)
  matDate <- as.POSIXct(getFincadDateAdjust(as.POSIXct(effDate),date.list$unit,date.list$numUnits,holidayList))

  this$setDefault(currency = currency,quoteSide = quoteSide,quoteType = quoteType,quoteConvention = quoteConvention,instrument = instrument,
        indexFloat = indexFloat,spreadFloat = spreadFloat,resetFreqFloat = resetFreqFloat,payFreqFloat = payFreqFloat,
        dayCountFloat = dayCountFloat,rateBasisFloat = rateBasisFloat,payFreqFixed = payFreqFixed,dayCountFixed = dayCountFixed,
        rateBasisFixed = rateBasisFixed,busDayConvFixed=busDayConvFixed,busDayConvFloat=busDayConvFloat,relevantFinancialCenters = relevantFinancialCenters,notional = notional, 
        direction = direction, coupon = coupon,effDate = effDate,matDate = matDate)
})
 
setMethodS3("getMaturityDate","SpecificIRS",function(this,...)
{
  return(this$.matDate)
})

setMethodS3("getEffectiveDate","SpecificIRS",function(this,...)
{
  return(this$.effDate)
})

setMethodS3("getCoupon","SpecificIRS",function(this,...)
{
  return(this$.coupon)
})

setMethodS3("getNotional","SpecificIRS",function(this,...)
{
  return(this$.notional)
})

setMethodS3("getDirection","SpecificIRS",function(this,...)
{
  return(this$.direction)
})

setMethodS3("parseForwardStart","SpecificIRS",function(this,forwardStart,...)
{
  if (forwardStart=="spot") {
    unit <- "d"
    num.units <- 0
  }
  else {
    unit <- substr(forwardStart,nchar(forwardStart),nchar(forwardStart))
    num.units <- as.numeric(substr(forwardStart,1,nchar(forwardStart)-1))
  }
  return(list(unit=unit,num.units=num.units))
})

setMethodS3("summary", "SpecificIRS", function(this,...)
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
    cat("Day count (fixed): ", this$getDayCountFixed(), "\n","\n")
    cat("Business Day Convention (fixed):",this$getBusDayConvFixed(),"\n")
    cat("Business Day Convention (float):",this$getBusDayConvFloat(),"\n")
    cat("Relevant Financial Centers (Bus Days):",this$getRelevantFinancialCenters(),"\n")
    cat("Coupon: ", this$getCoupon(),"\n")
    cat("Direction: ", this$getDirection(),"\n")
    cat("Notional: ",this$getNotional(),"\n")
    cat("EffDate: ", as.character(this$getEffectiveDate()),"\n")
    cat("MatDate: ", as.character(this$getMaturityDate()),"\n")    
})