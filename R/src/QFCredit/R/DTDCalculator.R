setConstructorS3("DTDCalculator", function(...)
{
    extend(RObject(), "DTDCalculator",
        .liability = NULL,  # in millions of $$
        .shares = NULL,  # in millions of shares
        .s = NULL,       # share price
        .mktCap = NULL,  # in millions of $$
        .sigmaE = NULL,  # sigmaE is express in decimal form (.35, rather than 35)
        .time = NULL,    # in years
        .rate = NULL,    # continuosly compounded rate in decimal form
        .sigmaA = NULL,  # sigmaA is expressed in decimal form
        .a = NULL,       # asset value in millions of $$
        .fvLiab = NULL,
        .d1 = NULL,
        .d2 = NULL,       # distance to default (DTD)
        .useConstrOptim = NULL # constraint optimizer switch
    )
})

setMethodS3("runDTD","DTDCalculator",function(this,seedSigmaA,seedAsset,liability,shares, s, sigmaE, time, rate,useConstrOptim,...)
{
    # test the quality of the inputs
    assert(is.numeric(liability), "Liability must be numeric in DTD Calculator")
    assert(is.numeric(shares), "Number of shares must be numeric in DTD Calculator")
    assert(is.numeric(s), "Stock price must be numeric in DTD Calculator")
    assert(is.numeric(sigmaE), "Equity Vol must be numeric in DTD Calculator")
    assert(is.numeric(time), "Time must be numeric in DTD Calculator")
    assert(is.numeric(rate), "Rate must be numeric in DTD Calculator")
    assert(class(useConstrOptim) == "logical", "useConstrOptim must be logical")    
    assert(liability > 0, "Liability must be > 0 in DTD Calculator")
    assert(shares > 0, "Number of shares must be > 0 in DTD Calculator")
    assert(s > 0, "Stock Price must be > 0 in DTD Calculator")
    assert(sigmaE > 0, "Equity Vol must be > 0 in DTD Calculator")
    assert(time > 0, "Time must be > 0 in DTD Calculator")
    assert(rate > 0, "Rate must be > 0 in DTD Calculator")
	
	print(squish('Sigma A: ',seedSigmaA))
	print(squish('Asset: ',seedAsset))
     
    this$.mktCap <- shares * s
    this$.shares <- shares
    this$.s <- s
    this$.sigmaE <- sigmaE
    this$.time <- time
    this$.useConstrOptim <- useConstrOptim
    
    rate <- rate
    this$.rate <- fincad("aaConvert_cmpd",
                        freq_to = 6,
                        rate_from = rate,
                        freq_from = 2)	
    this$.liability <- liability
    this$.fvLiab <- liability * exp(this$.rate * time)
	
	seed <- c(seedSigmaA,seedAsset)
	optiBase <- optim(seed, this$dtdCalc, control = list(reltol = .Machine$double.eps))
	d1Base <- this$.d1; d2Base <- this$.d2
	
	if(seedSigmaA == 0.5 && seedAsset == 1 && ((this$.d2 > 5) || (this$.d2 < -2) || sigmaE > 0.75)){
		print("Opti Constraint")
		optiOne <- constrOptim(c(0.5,1),this$dtdCalc,ui = diag(2),ci = c(0,0),outer.iterations = 100,control = list(reltol = .Machine$double.eps),grad = NULL)
		d1One <- this$.d1; d2One <- this$.d2
		optiTwo <- constrOptim(c(.5, this$.mktCap + this$.fvLiab),this$dtdCalc,ui = diag(2),ci = c(0,0),outer.iterations = 100,control = list(reltol = .Machine$double.eps),grad = NULL)
		d1Two <- this$.d1; d2Two <- this$.d2
		if(abs(optiBase$value) < min(abs(optiTwo$value),abs(optiOne$value))){
			aData <- optiBase$par
			this$.d1 <- d1Base; this$.d2 <- d2Base
		}else if(abs(optiOne$value) < abs(optiTwo$value)){	
			aData <- optiOne$par
			this$.d1 <- d1One; this$.d2 <- d2One
		}else{
			aData <- optiTwo$par
			this$.d1 <- d1Two; this$.d2 <- d2Two
		}	
	}else{
		aData <- optiBase$par
	}
		
    this$.sigmaA <- aData[1]
    this$.a <- aData[2]
    return(c(this$.d2,this$.a, this$.sigmaA))
})

setMethodS3("dtdCalc", "DTDCalculator", function(this,assetInfo,...)
{
    sigmaA <- assetInfo[1]
    A <- assetInfo[2]
    this$.d2 <- (log(A) + this$.time * (this$.rate - (sigmaA ^ 2) / 2) - log(this$.fvLiab)) / (sigmaA * sqrt(this$.time))
    this$.d1 <- this$.d2 + sigmaA * sqrt(this$.time)
    sigmaATest <- (this$.mktCap * this$.sigmaE) / (A * pnorm(this$.d1))
    mktCapTest <- A * pnorm(this$.d1) - exp(-this$.rate * this$.time) * this$.fvLiab * pnorm(this$.d2)
    return((sigmaA - sigmaATest)^2 + ((this$.mktCap - mktCapTest)/this$.mktCap)^2)
})
