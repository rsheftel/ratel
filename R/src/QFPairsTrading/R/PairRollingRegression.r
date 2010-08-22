constructor("PairRollingRegression", function(...){
    this <- extend(RObject(),"PairRollingRegression",
        .builder = NULL
    )
})

method("initBuilder", "PairRollingRegression", function(this,
    window,storeIn,
    generatePDF = FALSE,pathPDF = "u:/Market Systems/",mfrowPDF = c(3,3),
	weighting = NULL
,...){	
    builder <- RollingRegressionBuilder(
        window,
        generatePDF = generatePDF,
        pathPDF = pathPDF,
        mfrowPDF = mfrowPDF,
		weighting = weighting
    )
    return(builder)
})

method("checkHedgeMethod", "PairRollingRegression", function(this,hedgeMethod
,...){
    assert(hedgeMethod %in% c("slope","equalExposure"))
})

method("mergedSeries", "PairRollingRegression", function(this,...){
	na.omit(merge(this$.seriesY,this$.seriesX))
})

method("mergedSeriesAndCoefficients", "PairRollingRegression", function(this,outputObj,...){
	na.omit(merge(outputObj$.coefficients,this$.seriesY,this$.seriesX))
})

method("runRollingRegression", "PairRollingRegression", function(this,window,constant = TRUE,storeIn = "LT",...){
    # Y = alpha + beta X
    # dY = slope * dX => slope = beta
    builder <- this$initBuilder(window,storeIn,...)
    cNames <- "beta"; if(constant)cNames <- c("alpha","beta")
    stringFormula <- "dataZoo[,1]~ -1 + dataZoo[,2]"; if(constant)stringFormula <- "dataZoo[,1]~ dataZoo[,2]" 
    outputObj <- (builder)$runLinearModel(dataZoo = this$mergedSeries(),stringFormula = stringFormula)
    m <- this$mergedSeriesAndCoefficients(outputObj)
    slope <- m[,1]; if(constant)slope <- m[,2]
    this$returnRollingRegressionResults(outputObj,slope,cNames,window,this$getSlopeHedgeRatios(slope),storeIn)
})

method("runLogChangesVsLevelsRollingRegression", "PairRollingRegression", function(this,window,constant = TRUE,storeIn = "ST",...){
    # d(log(Y)) = alpha + beta * X <=> dY/Y = alpha + beta * X
    # dY = slope * dX => slope = beta * Y
    builder <- this$initBuilder(window,storeIn,...)
    cNames <- "beta"; if(constant)cNames <- c("alpha","beta")
    stringFormula <- "dataZoo[,1]~ -1 + dataZoo[,2]"; if(constant)stringFormula <- "dataZoo[,1]~ dataZoo[,2]" 
    outputObj <- (builder)$runLinearModel(dataZoo = na.omit(merge(diff(log(this$.seriesY)),this$.seriesX)),stringFormula = stringFormula)
    m <- this$mergedSeriesAndCoefficients(outputObj)
    slope <- m[,1] * m[,2]; if(constant)slope <- m[,2] * m[,3]
    this$returnRollingRegressionResults(outputObj,slope,cNames,window,this$getSlopeHedgeRatios(slope),storeIn)
})

method("runLogRollingRegression", "PairRollingRegression", function(this,window,constant = TRUE,storeIn = "LT",hedgeMethod = "slope",...){
    # log(Y) = [alpha] + beta * log(X) <=> Y = [exp(alpha)] * X^beta
    # dY = slope * dX => slope = [exp(alpha)] * beta * X^(beta-1)
    # PredictedY = exp([alpha] + beta * log(X))
    # PredictedX = exp((log(Y) - [alpha]) / beta)
    this$checkHedgeMethod(hedgeMethod)
    builder <- this$initBuilder(window,storeIn,...)
    cNames <- "beta"; if(constant)cNames <- c("alpha","beta")
    stringFormula <- "dataZoo[,1]~ -1 + dataZoo[,2]"; if(constant)stringFormula <- "dataZoo[,1]~ dataZoo[,2]" 
    outputObj <- (builder)$runLinearModel(dataZoo = log(this$mergedSeries()),stringFormula = stringFormula)
    m <- this$mergedSeriesAndCoefficients(outputObj)
    slope <- m[,1] * m[,3]^(m[,1]-1); if(constant)slope <- exp(m[,1]) * m[,2] * m[,4]^(m[,2]-1)
    
    if(hedgeMethod == "slope"){
        hedgeCoefficients <- this$getSlopeHedgeRatios(slope)
    }else{
        predictedY <- exp(m[,1] * log(m[,3])); if(constant)predictedY <- exp(m[,1] + m[,2] * log(m[,4]))
        predictedX <- exp(log(m[,2])/m[,1]); if(constant)predictedX <- exp((log(m[,3]) - m[,1])/m[,2])
        hedgeCoefficients <- this$getEqualExposureHedgeRatios(slope,predictedY,predictedX)
    }
    this$returnRollingRegressionResults(outputObj,slope,cNames,window,hedgeCoefficients,storeIn)
})

method("runChangesRollingRegression", "PairRollingRegression", function(this,window,constant = TRUE,storeIn = "ST",lag = 1,...){
    # dY = alpha + beta * dX
    # dY = slope * dX => slope = beta
    builder <- this$initBuilder(window,storeIn,...)
    cNames <- "beta"; if(constant)cNames <- c("alpha","beta")
    stringFormula <- "dataZoo[,1]~ -1 + dataZoo[,2]"; if(constant)stringFormula <- "dataZoo[,1]~ dataZoo[,2]"
    outputObj <- builder$runLinearModel(dataZoo = diff(this$mergedSeries(),lag = lag),stringFormula = stringFormula)
    m <- this$mergedSeriesAndCoefficients(outputObj)
    slope <- m[,1];  if(constant)slope <- m[,2]
    this$returnRollingRegressionResults(outputObj,slope,cNames,window,this$getSlopeHedgeRatios(slope),storeIn)
})

method("runLogChangesRollingRegression", "PairRollingRegression", function(this,window,constant = TRUE,storeIn = "ST",...){
    # d(log(Y)) = beta * d(log(X)) <=> dY/Y = beta * dX/X
    # dY = slope * dX => slope = beta * Y/X
    builder <- this$initBuilder(window,storeIn,...)
    cNames <- "beta"; if(constant)cNames <- c("alpha","beta")
    stringFormula <- "dataZoo[,1]~ -1 + dataZoo[,2]"; if(constant)stringFormula <- "dataZoo[,1]~ dataZoo[,2]"
    outputObj <- builder$runLinearModel(dataZoo = diff(log(this$mergedSeries())),stringFormula = stringFormula)
    m <- this$mergedSeriesAndCoefficients(outputObj)
    slope <- m[,1] * m[,2] / m[,3];  if(constant)slope <- m[,2] * m[,3] / m[,4]
    this$returnRollingRegressionResults(outputObj,slope,cNames,window,this$getSlopeHedgeRatios(slope),storeIn)
})

method("runPercentChangesRollingRegression", "PairRollingRegression", function(this,window,constant = TRUE,storeIn = "ST",lag = 1,...){	
	# dY/Y = beta * dX/X
	# dY = slope * dX => slope = beta * Y/X	
	builder <- this$initBuilder(window,storeIn,...)
	calcReturn <- function(z,lag){diff(z,lag = lag)/lag(z,-lag)}	
	cNames <- "beta"; if(constant)cNames <- c("alpha","beta")
	stringFormula <- "dataZoo[,1]~ -1 + dataZoo[,2]"; if(constant)stringFormula <- "dataZoo[,1]~ dataZoo[,2]"
	outputObj <- builder$runLinearModel(dataZoo = calcReturn(this$mergedSeries(),lag),stringFormula = stringFormula)
	m <- this$mergedSeriesAndCoefficients(outputObj)
	slope <- m[,1] * m[,2] / m[,3];  if(constant)slope <- m[,2] * m[,3] / m[,4]
		
	this$returnRollingRegressionResults(outputObj,slope,cNames,window,this$getSlopeHedgeRatios(slope),storeIn)
})

method("returnRollingRegressionResults", "PairRollingRegression", function(this,outputObj,slope,coeffNames,window,hedgeCoefficients,storeIn,...)
{
    dailyTri <- getLinearCombinationDailyTri(zooDailyTris = diff(merge(this$.triY,this$.triX)),zooHedgeCoefficients = hedgeCoefficients,holdCoefficients = this$.holdCoefficients)
    result <- getZooDataFrame(merge(
        outputObj$.zScore,outputObj$.residual,outputObj$.sd,outputObj$.r2Adj,outputObj$.r2,outputObj$.pValues,outputObj$.coefficients,
        slope,hedgeCoefficients,dailyTri,getCumTriFromDailyTri(dailyTri,baseTri = 0),outputObj$.factorRank
    ))
    colnames(result) <- c("zScore","residual","sd","r2Adj","r2",paste(coeffNames,".pVal",sep = ""),
        coeffNames,"slope","hedgeY","hedgeX","dailyTri","tri",paste(coeffNames,".factorRank",sep = ""))
    
    eval(parse(text = paste("this$.",storeIn,".",window," = result",sep = "")))
})

method("generateCSV", "PairRollingRegression", function(this,pathPDF,storeIn,window,...)
{
    data <- merge(this$getModelResults(storeIn,window),this$.triY,this$.triX)
    write.csv(data.frame(data),paste(pathPDF,resultString,"/data.csv",sep = ""))
})

method("getTriChangesZoo","PairRollingRegression",function(this,resultString,diffSeq = c(1,5,10,15,30,60,90,180),...)
{    
    result <- eval(parse(text = squish("this$.",resultString)))
    assert(max(diffSeq) <= NROW(result[,"tri"])-1)
    triChangesList <- NULL; colNameVec <- NULL
    for(k in 1:NROW(diffSeq)){
        triChangesList[[k]] <- lag(result[,"tri"],diffSeq[k]) - result[,"tri"]
        colNameVec <- c(colNameVec,squish("tri.diff.",diffSeq[k]))
    }
    triChangesZoo <- do.call(merge,triChangesList)
    colnames(triChangesZoo) <- c(colNameVec)
    return(triChangesZoo)
})

method("getModelResults","PairRollingRegression",function(this,storeIn,window,...)
{   
    needs(storeIn = "character",window = "numeric")
    result <- eval(parse(text = squish("this$.",storeIn,".",window)))
    failIf(is.null(result),"No rolling regression in memory for this name and this window")
    result
})