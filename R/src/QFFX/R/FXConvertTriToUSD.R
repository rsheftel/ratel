setConstructorS3("FXConvertTriToUSD", function(localTRI=NULL,targetTRI=NULL,...)
{
        this <- extend(RObject(), "FXConvertTriToUSD",
        .tsdb = NULL,
        .fxCurr = NULL,
		.localTRI = localTRI,
		.targetTRI = targetTRI
        )
        
        if(!inStaticConstructor(this))
        {
          	this$.localTRI <- localTRI
			this$.targetTRI <- targetTRI
			this$.tsdb <- TimeSeriesDB()
		  	this$.fxCurr <- FXCurr$setByPairs(substr(localTRI,0,3),substr(localTRI,4,6))
        }
        this
})

setMethodS3("convertTriToUSD","FXConvertTriToUSD",function(this,localTRI = localTRI, targetTRI = targetTRI, writeToTSDB=TRUE,...)
{    
    CurrInst <- FXConvertTriToUSD(localTRI, targetTRI)
    if (isCrossUSDDenominated(CurrInst$.fxCurr)) zoo.result <- CurrInst$getTimeSeries(ts = CurrInst$.localTRI)
    else
    {
      tri_res <- CurrInst$getTimeSeries(ts = CurrInst$.localTRI)
      convertCcy <- ReturnCorrectFXPair(CurrInst$.fxCurr$over(),"usd")
      spotRates <- CurrInst$getSpotRates(convertCcy)
      if (convertCcy$over()=="usd") InvertFlag <- TRUE
      else InvertFlag <- FALSE
      
      spotRates <- filter.zoo.byIntersection(spotRates,tri_res)
      tri_res <- filter.zoo.byIntersection(tri_res,spotRates)
      
      dates <- index(spotRates)
      data_tri <- data.frame(tri_res)
      data_spot <- data.frame(spotRates)
      
      colnames(data_tri) <- "tri"
      colnames(data_spot) <- "spot"
      
      norm_func <- function(a,b) { return(a*b) }
      invert_func <- function(a,b) {return(a/b) }
      
      if (InvertFlag==TRUE) result <- mapply(invert_func,data_tri$tri,data_spot$spot)
      else result <- mapply(norm_func,data_tri$tri,data_spot$spot)
      zoo.result <- (getZooDataFrame(zoo(result,dates)))       
    }
	if (writeToTSDB) CurrInst$.tsdb$writeOneTimeSeriesByName(ts=zoo.result,name=CurrInst$.targetTRI,data.source="internal")
    return(zoo.result)
})

    
setMethodS3("getSpotRates","FXConvertTriToUSD",function(this,fxCurr,startDate=NULL,endDate=NULL,...)
{
  quote_side <- "mid"
  ccy <- paste(fxCurr$over(),fxCurr$under(),sep="")
  data.source <- "internal"
  tenor <- "spot"
  quote_type <- "rate"
  time_series_name <- paste(ccy,tenor,quote_type,quote_side,sep="_")
  result <- this$.tsdb$retrieveOneTimeSeriesByName(name=time_series_name,data.source=data.source,start=startDate,end=endDate)
  result <- make.zoo.daily(result, "15:00:00")
  return(result)
})

setMethodS3("getTimeSeries","FXConvertTriToUSD",function(this,ts = NULL,startDate=NULL,endDate=NULL,...)
{
	data.source <- "internal"
	result <- this$.tsdb$retrieveOneTimeSeriesByName(name=ts,data.source=data.source,start=startDate,end=endDate)
})
  
  