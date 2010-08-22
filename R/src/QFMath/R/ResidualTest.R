setConstructorS3("ResidualTest", function(...)
{
    extend(RObject(), "ResidualTest",
        .testName = NULL,
        .residuals = NULL,
        .result = NULL
    )
})

setMethodS3("augmentedDickeyFuller","ResidualTest",function(this,residuals,lags,k,pValueThreshold,...)
{
    # !!!this test is designed for residuals of OLS regressions!!! See Hamilton p.528 Case 1 for details
    # if residuals is an AR(p), lags = p - 1 
    # no trend/no drift since we test OLS residuals

    # check inputs
        
    assert(any(class(residuals)=="numeric"),paste(residuals,"is not numeric"))
    this$.residuals <- residuals
    
    assert(class(lags)=="numeric" && (lags >= 0) && ((lags+2) < NROW(residuals)),paste(lags,"is not valid"))
    
    assert(any(k==c(1,2,3,4,5)),paste(k,"is not a valid k parameter"))
    
    assert(any(pValueThreshold==c(0.01,0.025,0.05,0.075,0.1,0.125,0.15)),paste(pValueThreshold,"is not a valid pValue threshold"))
    
    # algorithm
    
    lags <- lags + 1
    z <- diff(residuals)
    n <- length(z)
    x <- embed(z,lags)
    z.diff <- x[,1]
    z.lag.1 <- residuals[lags:n]

    if (lags > 1) {
        z.diff.lag = x[, 2:lags]
        result <- lm(z.diff ~ z.lag.1 - 1 + z.diff.lag)

        pValues <- coef(summary(result))[,4]

        # this loop reduces the number of lag using student tests
        
        for (i in 2:lags){
            if(is.na(pValues[i])){
                result <- this$augmentedDickeyFuller(residuals = residuals, lags = (lags -2),k = k,pValueThreshold = pValueThreshold)
                return(result)
            }
            if(pValues[i]>pValueThreshold){
                result <- this$augmentedDickeyFuller(residuals = residuals, lags = (lags -2),k = k,pValueThreshold = pValueThreshold)
                return(result)
            }
        }
        tau <- coef(summary(result))[1,3]
        teststat <- as.matrix(tau)
    }else{
        result <- lm(z.diff ~ z.lag.1 - 1)
        tau <- coef(summary(result))[1,3]
        teststat <- as.matrix(tau)
    }

    # see Hamilton Table B.9 case 1 (given for n = 500)
        
    if (k==1)(cvals <- t(c(-3.39,-3.05,-2.76,-2.58,-2.45,-2.35,-2.26)))
    if (k==2)(cvals <- t(c(-3.84,-3.55,-3.27,-3.11,-2.99,-2.88,-2.79)))
    if (k==3)(cvals <- t(c(-4.3,-3.99,-3.74,-3.57,-3.44,-3.35,-3.26)))
    if (k==4)(cvals <- t(c(-4.67,-4.38,-4.13,-3.95,-3.81,-3.71,-3.61)))
    if (k==5)(cvals <- t(c(-4.99,-4.67,-4.40,-4.25,-4.14,-4.04,-3.94)))
	
    colnames(cvals) <- c("1%","2.5%","5%","7.5%","10%","12.5%","15%")
    rownames(cvals) <- "tau1"
    colnames(teststat) <- "tau1"
    rownames(teststat) <- "statistic"

    test.result <- cvals >= teststat[1]
    resultInteger <- 0
    for(i in seq_len(7)) {
        if(test.result[i]) {
            resultInteger <- 8 - i
            break
        }
    }

    if(resultInteger>0){
        resultCharacter <- colnames(cvals)[8-resultInteger]
    }else{
        resultCharacter <- "Not stationnary"
    }
    
    
    this$.testName <- "Augmented Dickey Fuller Test (JB)"
    this$.result <- list(integer = resultInteger,character = resultCharacter)
    this$.result
})

setMethodS3("summary","ResidualTest",function(this,...)
{
    cat("Test name:",this$.testName,"\n")    
    if(!is.null(this$.result))this$.result
})
