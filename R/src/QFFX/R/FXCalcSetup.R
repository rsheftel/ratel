setConstructorS3("FXCalcSetup", function(FXCurr=NULL,startDate=NULL,endDate=NULL,tsdb=NULL,DataLoad=NULL,initialEquity=NULL, 
		lastRollInfo=NULL,tenor=NULL,...)
{
        this <- extend(RObject(), "FXCalcSetup",
        .FXCurr = FXCurr,
        .startDate = startDate,
        .endDate = endDate,
        .tsdb = tsdb,
        .currDate = startDate,
        .runningPnL = 0,
        .currPnL = NULL,
        .rollHistory = NULL,
        .DataLoad = NULL,
        .initialEquity = initialEquity,
        .lastRollInfo = lastRollInfo,
		.tenor = tenor
        )
        constructorNeeds(this,tsdb = "TimeSeriesDB")
        if(!inStaticConstructor(this))
        	{
          	this$.startDate <- as.POSIXct(startDate)
          	this$.endDate <- as.POSIXct(endDate)
          	if(this$.startDate > this$.endDate) throw("FXCalcSetup: start date must be less than end date")
			if (is.null(this$.initialEquity)) this$.initialEquity <- 0.0
          	this$.currPnL <- zoo(as.numeric(this$.initialEquity),as.POSIXct(this$.startDate))
		  	if (!is.null(this$.lastRollInfo)) this$.rollHistory <- zoo(this$.lastRollInfo,this$.startDate)
			this$.runningPnL <- this$.initialEquity
			if (is.null(DataLoad)) 
			  this$.DataLoad <- FXDataLoad(this$.FXCurr,startDate,endDate,tsdb,tenor)
		  	else this$.DataLoad <- DataLoad
          	this$.DataLoad$filterRateAndVol()
	  }
   this
})

setMethodS3("getCurrentDate","FXCalcSetup",function(this,...)
{
  return(this$.currDate)
})

setMethodS3("setCurrentDate","FXCalcSetup",function(this, currDate,...)
{
  currDate <- as.POSIXct(currDate)
  failUnless(grep(as.character(currDate),index(this$.DataLoad$getVolData())),squish("Cannot Set Curr Date to ", 
		  as.character(currDate)," since not in Dataset"))
  this$.currDate <- currDate
})

setMethodS3("dataLoad","FXCalcSetup",function(this,...)
{
  return(this$.DataLoad)
})  

setMethodS3("setLastRollInfo","FXCalcSetup",function(this, lastRollInfo,...)
{
  this$.lastRollInfo <- lastRollInfo
})

setMethodS3("getLastRollInfo","FXCalcSetup",function(this,...)
{
  return(this$.lastRollInfo)
})

setMethodS3("getInterpolatedData","FXCalcSetup",function(this,interpList, dataSet,...)
{
   lowerPt  <-  as.numeric(dataSet[this$getCurrentDate(),interpList$interpIndex])
   upperPt  <-  as.numeric(dataSet[this$getCurrentDate(),interpList$interpIndex+1])
   w1 <- (interpList$daysTwo/(interpList$daysOne+interpList$daysTwo))
   w2 <- (interpList$daysOne/(interpList$daysOne+interpList$daysTwo))
   res <- lowerPt*w1 + upperPt*w2
   return(res)
})

setMethodS3("getCurrPnL","FXCalcSetup",function(this,...)
{
  return(this$.currPnL)
})

setMethodS3("getRollHistory","FXCalcSetup",function(this,...)
{
  return(this$.rollHistory)
})

setMethodS3("determineInterpolationScheme","FXCalcSetup",function(this,expiry,maxTenor,...)
{
# We know that the Dataloader is loading in all of the tenors...whether they exist or not is another matter.
  tenorList <- FXTenor$getTenors()
  tenor.Dates <- c(this$getCurrentDate())
  temp.func <- function(tenor) {
	  temp.res <- list(expiryDate = as.numeric(this$dataLoad()$getExpiryData()[this$getCurrentDate(),tenor]), 
  			settleDate = as.numeric(this$dataLoad()$getSettleData()[this$getCurrentDate(),tenor]))
  	  temp.res$expiryDate <- as.POSIXct(temp.res$expiryDate)
  	  temp.res$settleDate <- as.POSIXct(temp.res$settleDate)
  	  return(temp.res)
	}
	res <- temp.func(tenorList[maxTenor])
 	end.date <- res$expiryDate
	for (i in maxTenor-1:1){
        res <-  temp.func(tenorList[i])
		beg.date <- res$expiryDate
		daysOne <- getDaysBetween(beg.date,expiry)
        daysTwo <- getDaysBetween(expiry,end.date)
        if ((daysOne>=0) && (daysTwo>=0))
        {
          res <- list(interpIndex = i, daysOne = daysOne, daysTwo = daysTwo)
          return(res)
        }
        end.date <- beg.date
  	}
  	result <- list(interpIndex = NULL, daysOne = NULL, daysTwo = NULL)
  	print(result)
	return(result)
})
 

setMethodS3("addRollToHistory","FXCalcSetup",function(this,...)
{
	tempZoo <- zoo(this$.lastRollInfo,this$getCurrentDate())
	if (is.null(this$.rollHistory)||is.na(this$.rollHistory)) this$.rollHistory <- tempZoo
	else this$.rollHistory <- rbind(this$.rollHistory,tempZoo)
	
})
   
setMethodS3("computePnL","FXCalcSetup",function(this, todayPrice, yestPrice, notional,...)
{
	unitSize = 1000000
	if ((!is.null(todayPrice)) && (!is.null(yestPrice)))
	{ 
		this$.runningPnL <- this$.runningPnL +  notional * unitSize * (todayPrice - yestPrice)
	}
	temp.zoo <- zoo(this$.runningPnL,as.POSIXct(this$getCurrentDate()))
		this$.currPnL <- rbind(this$.currPnL,temp.zoo)	
	})

setMethodS3("check","FXCalcSetup",function(this,data,...)
{
		tolerance <- -0.0025
		if ((data < tolerance) || is.na(data) || is.null(data)) return(FALSE)
		return(TRUE)
})