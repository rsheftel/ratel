setConstructorS3("EuropeanOptionPricer", function(...)
{
    extend(RObject(), "EuropeanOptionPricer",
        .europeanOptionObj = NULL,
        .underlyingPrice = NULL,
        .rate30360Semi = NULL,
        .rate30360Ann = NULL,
        .volLn = NULL,
        .settleDate = NULL,
        .d1 = NULL,
        .d2 = NULL,
        .holidayList = NULL,
        .output = NULL
    )
})

setMethodS3("getFincadPrice", "EuropeanOptionPricer", function(this,europeanOptionObj,underlyingPrice, rate30360Semi,volLn, settleDate, ...)
{
    assert(is.numeric(underlyingPrice) && underlyingPrice > 0, paste(underlyingPrice, "is not a valid underlying price for EuropeanOptionPricer."))
    assert(any(class(europeanOptionObj) == "EuropeanOptionSpecific"), paste(europeanOptionObj, "is not a valid Option for EuropeanOptionPricer."))
    assert(is.numeric(rate30360Semi) && rate30360Semi > 0, paste(rate30360Semi, "is not a valid discounting rate for EuropeanOptionPricer."))
    assert(is.numeric(volLn) && volLn > 0, paste(volLn, "is not a valid vol for EuropeanOptionPricer."))
    assert(as.POSIXct(europeanOptionObj$.expiryDate) > as.POSIXct(settleDate), cat("The settle date must be before the expiration date of the option in EuropeanOptionPricer."))
    
    this$.europeanOptionObj <- europeanOptionObj
    this$.underlyingPrice <- underlyingPrice
    this$.rate30360Semi <- rate30360Semi
    this$.volLn <- volLn
    this$.settleDate <- as.POSIXlt(settleDate)
    
    this$.rate30360Ann <- fincad("aaConvert_cmpd",
                                    freq_from = 2,  #from semi-annual
                                    freq_to = 1,    #to annual
                                    rate_from = this$.rate30360Semi)
    
    if((this$.europeanOptionObj)$.putCall == "call") optionType = 1
    if((this$.europeanOptionObj)$.putCall == "put") optionType = 2
    
    result = fincad("aaBSG",
                        price_u = this$.underlyingPrice,
                        ex = (this$.europeanOptionObj)$.strike,
                        d_exp = (this$.europeanOptionObj)$.expiryDate,
                        d_v = this$.settleDate,
                        vlt = this$.volLn,
                        rate_ann = this$.rate30360Ann,
                        cost_hldg = (this$.europeanOptionObj)$.dividendAnn,
                        option_type = optionType,
                        stat = data.frame(1:8),
                        acc_rate = 4,           # 30/360
                        acc_cost_hldg = 4)      # 30/360
    
     row.names(result) <- c("fair value","delta","gamma","theta","vega","rho of rate",
            "rho of holding cost","prob of exercise")
     colnames(result) <- c("Fincad output")
     this$.output <- result
     
     # d1 and d2 separate calcs for the DTD functions
     holiday <- "2001-12-31"  ###### NEED TO ADD HOLIDAY LIST FILE HERE
     rateContinuousCompound <- fincad("aaConvert_cmpd",freq_from = 1, freq_to = 6, rate_from = this$.rate30360Ann)
     timeToExpiry <- fincad("aaDateCount", d_e = as.POSIXct(this$.settleDate),
                                           d_t = as.POSIXct((this$.europeanOptionObj)$.expiryDate),
                                           adj_units = 5,
                                           d_rul = 1,
                                           hl = holiday)
     this$.d1 = (log(this$.underlyingPrice / (this$.europeanOptionObj)$.strike) +
                (rateContinuousCompound + (this$.volLn ^ 2) / 2 ) * timeToExpiry) /
                (this$.volLn * sqrt(timeToExpiry))
     this$.d2 = this$.d1 - this$.volLn * sqrt(timeToExpiry)
                                           
     
})

setMethodS3("summary","EuropeanOptionPricer",function(this,...)
{
    if(any(class(this$.europeanOptionObj) == "EuropeanOptionEquity")) cat(paste("Underlying ticker:",this$.europeanOptionObj$.equityObj$.ticker),"\n")
    cat(paste("Settle date:",this$.settleDate),"\n")
    cat(paste("Underlying Price:", this$.underlyingPrice), "\n")
    cat(paste("Lognormal Vol:", this$.volLn),"\n\n")
    cat("Specific Option:\n\n")
    if(!is.null(this$.europeanOptionObj))   (this$.europeanOptionObj)$summary()
    cat("\n")


# this info needs to be updated for options when we are looking at real data
#    cat("CDS source:",this$.cdsSource,"\n")
#    cat("CDS Term Structure:","\n","\n")
#    print(this$.cdsTermStructure)
#    cat("\n")
#    cat("IRS source:",this$.irsSource,"\n")
#    cat("Interest rate curve:","\n","\n")
#    print(this$.irsData)
#    cat("\n")
#    cat(paste("Flat curve pricing:",this$.isFlat,sep = " "),"\n")


    cat("Output:","\n","\n")
    print(this$.output)
    cat("\n")
})


    
    
    