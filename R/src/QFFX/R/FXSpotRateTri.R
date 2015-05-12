setConstructorS3("FXSpotRateTri", function(FXCurr=NULL,fxNotional=NULL,startDate=NULL,endDate=NULL,tsdb=NULL,overUnder=NULL,DataLoad=NULL,
  initialEquity=NULL,...)
{
        this <- extend(RObject(), "FXSpotRateTri",
        .FXCurr = FXCurr,
		.fxNotional = fxNotional,
		.overUnder = overUnder,
		.startDate = startDate,
        .endDate = endDate,
        .tsdb = tsdb,
        .DataLoad = NULL,
		.initialEquity = initialEquity
        )
        
        constructorNeeds(this,tsdb = "TimeSeriesDB", FXCurr = "FXCurr")
        
        if(!inStaticConstructor(this))
        {
          this$.startDate <- as.POSIXct(startDate)
          this$.endDate <- as.POSIXct(endDate)
		  if (is.null(this$.initialEquity)) this$.initialEquity <- 0.0
		  this$.currPnL <- zoo(as.numeric(this$.initialEquity),as.POSIXct(this$.startDate))
		  this$.runningPnL <- this$.initialEquity
		  if (is.null(DataLoad)) 
			  this$.DataLoad <- FXDataLoad(this$.FXCurr,startDate,endDate,tsdb,"spot")
		  else this$.DataLoad <- DataLoad 
	  	}
       this
})

setMethodS3("computePnL","FXSpotRateTri",function(this, todayDate, todayPrice, yestPrice, notional,...)
{
		unitSize = 1000000
		if ((!is.null(todayPrice)) && (!is.null(yestPrice))) 
			this$.runningPnL <- this$.runningPnL +  notional * unitSize * (todayPrice - yestPrice)
		temp.zoo <- zoo(this$.runningPnL,as.POSIXct(todayDate))
		this$.currPnL <- rbind(this$.currPnL,temp.zoo)	
})

#####################################################
# Calculation 
#####################################################
 
setMethodS3("priceNewSpot","FXSpotRateTri",function(this,oldDate,newDate,...)
{
	newRate <- as.numeric(this$.DataLoad$getRateData()[newDate,"spot"])
	oldRate <- as.numeric(this$.DataLoad$getRateData()[oldDate,"spot"])
	
	if (!this$checkDataOK(oldRate)) {
		cat("Problem with old spot rate:",as.character(oldDate)," Data:",oldRate,"\n") 
		return(NULL)
	}
	if (!this$checkDataOK(newRate)) {
		cat("Problem with new spot rate:",as.character(newDate)," Data:",newRate,"\n") 
		return(NULL)
	}
	
	if (this$.overUnder == "over") price <- (1 - oldRate/newRate)
	else price <- (1-newRate/oldRate)
	
	repoData <- this$getRepoData(oldDate)
	if (is.null(repoData)) return(NULL)
	
	numberDays <- this$getAccrualDays(oldDate,newDate)
	effectiveRate <- as.numeric(repoData["overRepoRate"]) - as.numeric(repoData["underRepoRate"])
	if (this$.overUnder == "under") effectiveRate <- effectiveRate * -1
	
	effectiveCarry <- effectiveRate * numberDays/360
	res <- price + effectiveCarry
	return(res)

})

setMethodS3("getAccrualDays","FXSpotRateTri",function(this,oldDate,newDate,...)
{
	firstSettleDate <- as.POSIXct(as.numeric(this$.DataLoad$getSettleData()[oldDate,"spot"]))
	secondSettleDate <- as.POSIXct(as.numeric(this$.DataLoad$getSettleData()[newDate,"spot"]))
	weekSettleDate <- as.POSIXct(as.numeric(this$.DataLoad$getSettleData()[oldDate,2]))
	
	firstAccrualPeriod <- getDaysBetween(firstSettleDate,weekSettleDate)
	secondAccrualPeriod <- getDaysBetween(secondSettleDate,weekSettleDate)
	accrualDays <- firstAccrualPeriod - secondAccrualPeriod
	return(accrualDays)
	
})
setMethodS3("getRepoData","FXSpotRateTri",function(this,obsDate,...)
{
	overRepoRate <- this$.DataLoad$getOverRepoData()[obsDate,2]
	underRepoRate <- this$.DataLoad$getUnderRepoData()[obsDate,2]
	if (overRepoRate < 0) overRepoRate <- 0
	if (underRepoRate < 0) underRepoRate <- 0
	
	if (!this$checkDataOK(overRepoRate)) 
	{
		cat("Problem with overRepoRate:",as.character(obsDate)," Data:",overRepoRate,"\n")
		return(NULL)
	}
	if (!this$checkDataOK(underRepoRate))
	{
		cat("Problem with underRepoRate:",as.character(obsDate), "Data:",underRepoRate,"\n")
		return(NULL)
	} 
# Repo Data is continuously compounded...need to convert. Pricer needs Actual/360, annual compounding.
	spotSettleDate <- as.POSIXct(as.numeric(this$.DataLoad$getSettleData()[obsDate,"spot"]))             
	firstSettleDate <- as.POSIXct(as.numeric(this$.DataLoad$getSettleData()[obsDate,2]))
	
	
	
	temp.overRepoRate <- fincad("aaConvert_cmpd2",d_e = spotSettleDate, d_t = firstSettleDate, rate_from = overRepoRate, rate_basis_from = 6, acc_from = 3, rate_basis_to = 1, acc_to = 2)
	temp.underRepoRate <- fincad("aaConvert_cmpd2",d_e = spotSettleDate, d_t = firstSettleDate, rate_from = underRepoRate, rate_basis_from = 6, acc_from = 3, rate_basis_to = 1, acc_to = 2)	
	res <- list(overRepoRate=as.numeric(temp.overRepoRate),underRepoRate=as.numeric(temp.underRepoRate))
	return(res)
})

setMethodS3("computeEquityCurve","FXSpotRateTri",function(this,...)
{
	oldDate <- this$.startDate
	for (i in (match(oldDate,index(this$.DataLoad$getRateData()))+1):
		length(index(this$.DataLoad$getVolData()))) {   
		newDate <- index(this$.DataLoad$getRateData()[i,])
		newValue <-  this$priceNewSpot(oldDate,newDate)
		if (is.null(newValue)) {
			cat("Problem computing TRI for ",as.character(newDate)," using last TRI","\n")
			newValue <- 0
		}
		this$computePnL(newDate,newValue,0,this$.fxNotional$getNotional())
		oldDate <- newDate
     }
})

setMethodS3("checkDataOK","FXSpotRateTri",function(this,dataToCheck,...)
{
	if (is.null(dataToCheck) || is.na(dataToCheck) || (dataToCheck < 0)) return(FALSE)
	return(TRUE)	
})