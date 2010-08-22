constructor("RegressionOutputs", function(regressionDates = NULL,nbFactors = NULL)
{
    this <- extend(RObject(), "RegressionOutputs")  
    if(!inStaticConstructor(this)){
        regressionDates <- as.POSIXct(regressionDates)
        z <- zoo(as.matrix(NA),regressionDates)
        zFactors <- zoo(matrix(NA,ncol = as.numeric(nbFactors)),regressionDates)     
        this$.r2Adj <- z
        this$.r2 <- z
        this$.residual <- z
        this$.sd <- z
        this$.zScore <- z
		this$.qScore <- z
		this$.strike <- z
		this$.impliedVol <- z
		this$.maturity <- z
		this$.richCheapBps <- z
		this$.delta <- z
		this$.gamma <- z
		this$.vega <- z
		this$.coefficients <- zFactors
        this$.pValues <- zFactors
        this$.factorRank <- zFactors 
    }
    this
})

method("storeOneDate", "RegressionOutputs", function(this,int,variableName,variableValue,...){
    output <- squish('this$',variableName)
    output.eval <- eval(parse(text=output))
    assert(!is.null(output.eval),"invalid regression internal variable")
    assert(int <= NROW(output.eval))
    for (i in 1:NROW(variableValue))eval(parse(text=squish(output,"[int,i] <- ",variableValue[i])))
})     
