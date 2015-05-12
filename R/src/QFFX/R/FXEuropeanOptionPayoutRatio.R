setConstructorS3("FXEuropeanOptionPayoutRatio", function(FXOptionObj=NULL,startDate=NULL,endDate=NULL,tsdb=NULL,DataLoad=NULL,...)
{
        this <- extend(RObject(), "FXEuropeanOptionPayoutRatio",
        .FXOptionObj = FXOptionObj,
        .startDate = startDate,
        .endDate = endDate,
        .tsdb = tsdb,
        .DataLoad = DataLoad,
        .FXHist = NULL,
        .payoutRatio = NULL        
        )
# Note: this uses many of the same         
        constructorNeeds(this,tsdb = "TimeSeriesDB", FXOptionObj = "FXEuropeanOptionGeneric")
        
        if(!inStaticConstructor(this))
        {
          this$.startDate <- as.POSIXct(startDate)
          this$.endDate <- as.POSIXct(endDate)
          this$.FXHist <- FXEuropeanOptionTri(this$.FXOptionObj,fxNotional=FXNotional(100,"over"),this$.startDate,this$.endDate,this$.tsdb,"over",this$.DataLoad)    
      	  this$.putCall <- this$.FXOptionObj$getPutCall()  
	  }
         
         this
})

setMethodS3("getPayoutRatio","FXEuropeanOptionPayoutRatio",function(this,...)
{
  return(this$.payoutRatio)
})

setMethodS3("computePayoutRatio","FXEuropeanOptionPayoutRatio",function(this,...)
{
    for (i in 1:length(index(this$.FXHist$shared()$dataLoad()$getVolData()))){  
      	this$.FXHist$shared()$setCurrentDate(index(this$.FXHist$shared()$dataLoad()$getVolData()[i,]))
      	fwdRate <- this$.FXHist$shared()$dataLoad()$getRateData()[this$.FXHist$shared()$getCurrentDate(),this$.FXOptionObj$getExpiry()][[1]]
      	spotRate <-  this$.FXHist$shared()$dataLoad()$getRateData()[this$.FXHist$shared()$getCurrentDate(),"spot"][[1]]
      	rateDiff <- (spotRate - fwdRate)/(spotRate)
		if (!this$checkData(fwdRate=fwdRate,spotRate=spotRate)) poR <- 0
		else {
			if ((rateDiff < 0) && (this$.putCall == "call")) poR = 0
      		else if ((rateDiff > 0) && (this$.putCall == "put")) poR = 0
      		else
        		{
          		currFXOptionObj <- this$.FXHist$createNewOption()
          		currPriceInfo <- this$.FXHist$priceCurrent(currFXOptionObj)
          		if (!this$checkData(currPrice=currPriceInfo[1])) poR <- 0
          		else 
          			{
            			if (this$.putCall=="call") poR <- rateDiff/currPriceInfo[1]
            			else poR <- -rateDiff/currPriceInfo[1]
          			}
        		}
        		temp.zoo <- zoo(poR,as.character(this$.FXHist$shared()$getCurrentDate()))
        		this$.payoutRatio <- rbind(this$.payoutRatio,temp.zoo)
      		}
    	}
		index(this$.payoutRatio) <- as.POSIXct(index(this$.payoutRatio))
})

setMethodS3("checkData","FXEuropeanOptionPayoutRatio",function(this,fwdRate=0.0001,spotRate=0.00001,currPrice=0,...)
{
  if ((fwdRate < 0) || is.na(fwdRate) || is.null(fwdRate)) return(FALSE)
  if ((spotRate < 0) || is.na(spotRate) || is.null(spotRate)) return(FALSE)
  if (is.null(currPrice) || is.na(currPrice)) return(FALSE)
  return(TRUE)
})
    
