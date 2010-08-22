constructor("Pair", function(
    seriesY,seriesX,
    triY = NULL,triX = NULL,

    # the multiplier of a time series X is defined by PnL = dX * multiplier    
    multiplierY = NULL,multiplierX = NULL,
    
    # We have tri_daily_normX(t) <- tri_daily_normY(t) * hedge(t-1)
    # so going from one normalization to the other only makes sense if the hedge is relatively constant
    normalizeToY = TRUE,
    
    # we can assign acceptable hedge boundaries to a pair (min/max)
    hedgeMinY = NULL,hedgeMaxY = NULL,
    hedgeMinX = NULL,hedgeMaxX = NULL,
    
    # Assuming hedge coefficient of t-1 if t not available? (cf. getLinearCombinationDailyTri)
    holdCoefficients = TRUE,
    
...){                      
                       
   this <- extend(PairModels(),"Pair")
   
   if(!inStaticConstructor(this)){
        this$.seriesY = getZooDataFrame(seriesY,"seriesY")
        this$.seriesX = getZooDataFrame(seriesX,"seriesX")
        this$.triY = triY
        this$.triX = triX
        this$.multiplierY = multiplierY
        this$.multiplierX = multiplierX        
        if(!is.null(this$.triY))this$.triY = getZooDataFrame(this$.triY,"triY")
        else this$.triY = getZooDataFrame(this$.seriesY,"triY")
        if(!is.null(this$.triX)) this$.triX = getZooDataFrame(this$.triX,"triX")
        else this$.triX = getZooDataFrame(this$.seriesX,"triX")                
        if(!is.null(multiplierY))this$.multiplierY = getZooDataFrame(multiplierY,"multiplierY")
        else this$.multiplierY <- getZooDataFrame(zoo(1,index(this$.triY)),"multiplierY")
        if(!is.null(multiplierX))this$.multiplierX = getZooDataFrame(multiplierX,"multiplierX")
        else this$.multiplierX <- getZooDataFrame(zoo(1,index(this$.triX)),"multiplierX")
        
        # normalization
        
        needs(normalizeToY = "logical")
        this$.normalizeToY = normalizeToY
        
        # constant hedge rule
        
        needs(holdCoefficients = "logical")
        this$.holdCoefficients = holdCoefficients

        checkListNames <- c("hedgeMinY","hedgeMinX","hedgeMaxY","hedgeMaxX")
        for (i in 1:NROW(checkListNames)){
            value <- eval(parse(text = checkListNames[i]))
            if(!is.null(value)){
                needs(value,"numeric")
                eval(parse(text = paste("this$.",checkListNames[i],"=",value,sep = "")))
            }
        }
   }
   
   this
})

method("getSlopeHedgeRatios", "Pair", function(this,slope,...)
{   # Result TRIs are long Y (long residuals)
    m <- na.omit(merge(seriesY = this$.seriesY,seriesX = this$.seriesX,slope = slope,multiplierY = this$.multiplierY,multiplierX = this$.multiplierX))
    if(!this$.normalizeToY) m <- m[as.numeric(m[,"slope"])!=0]
    if(NROW(m)==0)return(NULL)

    if(this$.normalizeToY){ # dY = slope * dX and multiplierY dY + hedge * multiplierX dX = 0 => hedge = - slope * multiplierY/multiplierX
        res <- merge(hedgeY = zoo(1,index(m)),hedgeX = - m[,"slope"] * m[,"multiplierY"]/m[,"multiplierX"])
    }else{ # dY = slope * dX and multiplierY dY * hedge + multiplierX dX = 0 => hedge = - (multiplierX/multiplierY)/slope
        res <- merge(hedgeY = -(m[,"multiplierX"]/m[,"multiplierY"])/m[,"slope"],hedgeX = zoo(1,index(m)))        
    }
    return(this$checkHedgeBoundaries(res))
})

method("getEqualExposureHedgeRatios", "Pair", function(this,slope,predictedY,predictedX,...)
{   # Result TRIs are long Y (long residuals)
    m <- na.omit(merge(
        seriesY = this$.seriesY,seriesX = this$.seriesX,        
        predictedY = predictedY,predictedX = predictedX,                        
        multiplierY = this$.multiplierY,multiplierX = this$.multiplierX,
        slope = slope
    ))
    if(NROW(m)==0)return(NULL)

    if(this$.normalizeToY){ # |(PredictedY - ActualY) * multiplierY| = hedge * |(PredictedX - ActualX) * multiplierX|
        res <- merge(hedgeY = zoo(1,index(m)),hedgeX = - sign(m[,"slope"]) * abs( (m[,"multiplierY"]* (m[,"seriesY"] - m[,"predictedY"])) / (m[,"multiplierX"]* (m[,"seriesX"] - m[,"predictedX"])) ))
    }else{
        fail("Not implemented yet")
    }
    return(this$checkHedgeBoundaries(res))
})
                                     
method("checkHedgeBoundaries", "Pair", function(this,res,...)
{
    if(!is.null(this$.hedgeMinY))res <- res[res[,"hedgeY"] > this$.hedgeMinY]
    if(!is.null(this$.hedgeMinX))res <- res[res[,"hedgeX"] > this$.hedgeMinX]    
    if(!is.null(this$.hedgeMaxY))res <- res[res[,"hedgeY"] < this$.hedgeMaxY]
    if(!is.null(this$.hedgeMaxX))res <- res[res[,"hedgeX"] < this$.hedgeMaxX]
    res
})

method("plotSeries","Pair",function(this,...){
    plot(na.omit(merge(this$.seriesY,this$.seriesX)),main = "",col = 1:2)
})

method("plotTris","Pair",function(this,...){
    plot(na.omit(merge(this$.triY,this$.triX)),main = "",col = 1:2)
})

method("scatterPlotSeries","Pair",function(this,...){
    m  <- na.omit(merge(this$.seriesY,this$.seriesX))
    plot(m[,1],m[,2],main = "",col = 1:2,xlab = colnames(this$.seriesX),ylab = colnames(this$.seriesY))
})
    
method("calculateZooFromHedgeRatios", "Pair", function(this, hedgeRatios, stripTimes=FALSE, ...){
#Returns a zoo that is the hedged results from the seriesY and seriesX.
#The hedgeRatios are applied to the SeriesX series
	needs(hedgeRatios = "zoo", stripTimes = "logical")
	
    merged.XY <- this$stripAndMerge(stripTimes)
    
	if(stripTimes) hedgeRatios <- strip.times.zoo(hedgeRatios)
	
	if(!(last(index(merged.XY))%in%index(hedgeRatios)))
		hedgeRatios <- c(hedgeRatios,zoo(NULL,last(index(merged.XY))))
	
	mergedSeries <- merge(merged.XY, hedgeRatios, all=FALSE)
	
	if(NROW(mergedSeries) < 2) return("Not enough dates to calculate")
	
	seriesY.change <- as.vector(diff(mergedSeries[,1]))
	seriesX.change <- as.vector(diff(mergedSeries[,2]))
	
	hedgeRatios.aligned <- as.vector(mergedSeries[1:(NROW(mergedSeries)-1),3])
	
	dates <- as.POSIXct(index(mergedSeries)[2:NROW(mergedSeries)])
	
	this$.zooFromHedgeRatios$firstDate <- first(index(mergedSeries))
	diffSeries <- seriesY.change - (hedgeRatios.aligned * seriesX.change)
	
	returnZoo <- zoo(diffSeries,dates)
	
	return(returnZoo)
})


method("getFirstDateFromZooFromHedgeRatios", "Pair", function(this, ...){
	return(this$.zooFromHedgeRatios$firstDate)	
})

method("constantHedgeRatioOfMarketValues", "Pair", function(this, hedgeRatio = 1.0, stripTimes = FALSE, ...){
    # hedges Y wth X, using a fixed hedgeRatio of hedgeRatio market value of X
    # for each market value of Y
    
    needs(hedgeRatio = "numeric")
    
    merged.XY <- this$stripAndMerge(stripTimes)
    
    marketValueHedgeRatioZoo <- zoo(as.numeric(merged.XY[,1] / merged.XY[,2]) * hedgeRatio, index(merged.XY))
    return(this$calculateZooFromHedgeRatios(marketValueHedgeRatioZoo, stripTimes))    
})

method("stripAndMerge", "Pair", function(this, stripTimes = FALSE, ...){

    needs(stripTimes = "logical")
    
    if(stripTimes){
		seriesY <- strip.times.zoo(this$.seriesY)
		seriesX <- strip.times.zoo(this$.seriesX)
		return(merge(seriesY, seriesX, all = FALSE))
	}
	return(merge(this$.seriesY, this$.seriesX, all = FALSE))	
})

method("constantHedgeRatioOfNotional", "Pair", function(this, hedgeRatio = 1.0, stripTimes = TRUE, ...){
    # hedges Y with X, using a fixed hedgeRatio of hedgeRatio notional units of X
    # for each notional unit of Y
    
    needs(hedgeRatio = "numeric")
        
    hedgeRatioZoo <- getZooDataFrame(zoo(hedgeRatio, index(this$.seriesY)))
        
    return(this$calculateZooFromHedgeRatios(hedgeRatios = hedgeRatioZoo, stripTimes = stripTimes))
})
