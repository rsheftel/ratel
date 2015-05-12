setConstructorS3("FXForwardRateTri", function(FXForwardObj=NULL,fxNotional=NULL,startDate=NULL,endDate=NULL,tsdb=NULL,overUnder=NULL,DataLoad=NULL,
  initialEquity=NULL, lastRollInfo=NULL,...)
{
        this <- extend(RObject(), "FXForwardRateTri",
        .FXForwardObj = FXForwardObj,
		.fxNotional = fxNotional,
		.overUnder = overUnder,
		.lastRollInfo = lastRollInfo,
		.startDate = startDate,
        .endDate = endDate,
        .tsdb = tsdb,
        .FXCalcSetup = NULL
        )
        
        constructorNeeds(this,tsdb = "TimeSeriesDB", FXForwardObj = "FXForwardGeneric")
        
        if(!inStaticConstructor(this))
        {
          startDate <- as.POSIXct(startDate)
          endDate <- as.POSIXct(endDate)
          this$.FXCalcSetup <- FXCalcSetup(FXCurr = FXForwardObj$getFXCurrency(),startDate = startDate,endDate = endDate, tsdb=tsdb,
			  DataLoad = DataLoad,initialEquity = initialEquity,lastRollInfo = lastRollInfo, tenor = FXForwardObj$getTenor())	
      	  this$.tenor <- this$.FXForwardObj$getTenor()  
	  }
        this
})


setMethodS3("shared","FXForwardRateTri",function(this,...)
{
  return(this$.FXCalcSetup)                                                                                                                                     
})  
setMethodS3("sharedData","FXForwardRateTri",function(this,...)
{
  return(this$shared()$dataLoad())
})


#####################################################
# Calculation 
#####################################################

setMethodS3("loadForwardUsingRollInfo","FXForwardRateTri",function(this,...)
{
	if (!this$doesRollInfoExist()) this$shared()$setLastRollInfo(this$calcRollInfoForForward())
		initialDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["rollDate"]))
		settleDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["settleDate"]))
		rate <- as.numeric(this$shared()$getLastRollInfo()["rate"])
		expiryDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["expiryDate"]))	
		settleDateList <- list(expiryDate = expiryDate, settleDate = settleDate)
		if (!this$isForwardDataGood(initialDate,rate,expiryDate,settleDate)) return(NULL)
		else return(this$formForwardObj(initialDate,settleDateList,rate))
})

setMethodS3("createNewForward","FXForwardRateTri",function(this,...){
		roll.info <- this$calcRollInfoForForward()
		if (is.null(roll.info)) return(NULL) 
		this$shared()$setLastRollInfo(roll.info) 
		return(this$loadForwardUsingRollInfo())		
})

setMethodS3("formForwardObj","FXForwardRateTri",function(this,initialDate,settleDateList,rate,...){
		needs(initialDate="POSIXct",settleDateList="list(POSIXct)",rate="numeric")
		temp.fxfwd <- FXForwardSpecific$initSpecificFromGeneric(this$.FXForwardObj,initialDate,
			settleDateList = settleDateList, rate = rate, fxNotional = this$.fxNotional, direction = "buy") 
		return(temp.fxfwd)
})
 
setMethodS3("priceCurrent","FXForwardRateTri",function(this,currFXForward,...)
{
	rate <- as.numeric(this$sharedData()$getRateData()[this$shared()$getCurrentDate(),"spot"])
	if (is.null(rate) || is.na(rate)) return(NULL)
	interpList <- this$shared()$determineInterpolationScheme(expiry = currFXForward$getExpiryDate(), 
		maxTenor = which(FXTenor$getTenors()==this$.FXForwardObj$getTenor()))		
	if (is.null(interpList$interpIndex))
	{
		cat("Problem in computation. Interpolation Failed!","\n")
		return(NULL)
	}
# We do no have "spot" repo rates...if interp = 1 then we need to use last available
	if (interpList$interpIndex > 1) {		
		overRepoRate <- this$shared()$getInterpolatedData(interpList, this$sharedData()$getOverRepoData())
    	underRepoRate <- this$shared()$getInterpolatedData(interpList, this$sharedData()$getUnderRepoData())
	}
	else if (interpList$interpIndex == 1) {
		overRepoRate <- as.numeric(this$sharedData()$getOverRepoData()[this$shared()$getCurrentDate(),2])
		underRepoRate <- as.numeric(this$sharedData()$getUnderRepoData()[this$shared()$getCurrentDate(),2])
	}
#	if (!this$checkData(rate,overRepoRate,underRepoRate)) return(NULL)
  # Repo Data is continuously compounded...need to convert. Pricer needs Actual/360, annual compounding.
    temp.startDate <- this$shared()$getCurrentDate()
    temp.settleDate <- currFXForward$getSettleDate()
	regDates <- list(expiryDate = as.POSIXct(as.numeric(this$sharedData()$getExpiryData()[this$shared()$getCurrentDate(),"spot"])), 
		settleDate = as.POSIXct(as.numeric(this$sharedData()$getSettleData()[this$shared()$getCurrentDate(),"spot"])))
	spotSettleDate <- regDates$settleDate             
	if (overRepoRate < 0) overRepoRate <- 0
	if (underRepoRate < 0) underRepoRate <- 0
    temp.overRepoRate <- fincad("aaConvert_cmpd2",d_e = spotSettleDate, d_t = temp.settleDate, rate_from = overRepoRate, rate_basis_from = 6, acc_from = 3, rate_basis_to = 1, acc_to = 2)
    temp.underRepoRate <- fincad("aaConvert_cmpd2",d_e = spotSettleDate, d_t = temp.settleDate, rate_from = underRepoRate, rate_basis_from = 6, acc_from = 3, rate_basis_to = 1, acc_to = 2)
	
  	pricer <- FXForwardPricer()
  	pricer$setTerms(currFXForward, this$shared()$getCurrentDate(),spotRate = rate, overRepoRate = temp.overRepoRate, underRepoRate = temp.underRepoRate, this$.holidayList) 
  	res <- pricer$getPrice(this$.overUnder)
	return(res)

})


setMethodS3("computeEquityCurve","FXForwardRateTri",function(this,rebalPeriod=NULL,...)
{
    rebalPeriodList <- c("1d","1w","2w","1m")
    assert(any(rebalPeriod == rebalPeriodList),paste(rebalPeriod,"is not a valid Rebal Period"))   
    this$shared()$setCurrentDate(this$.startDate)
    currFXForwardObj <- this$loadForwardUsingRollInfo()
    currPrice <- this$priceCurrent(currFXForwardObj)    
	
	if (is.null(this$shared()$getRollHistory())) this$shared()$addRollToHistory()
	currPrice <- this$priceCurrent(currFXForwardObj)
	for (i in (match(this$shared()$getCurrentDate(),index(this$sharedData()$getRateData()))+1):
		length(index(this$sharedData()$getVolData()))) {   
		this$shared()$setCurrentDate(index(this$sharedData()$getVolData()[i,]))
		if (this$hasForwardExpired(currFXForwardObj)) {
			cat("Forward has expired...creating new","\n")
			currFXForwardObj <- this$createNewForward()
			currPriceInfo <- this$priceCurrent(currFXForwardObj)
			cat("Failure due to forward expiring, creating new Forward",as.character(this$shared()$getCurrentDate()),"\n")
		} 
		newPrice <-  this$priceCurrent(currFXForwardObj)
		if (is.null(newPrice))
      	{
			cat("Problem computing forward price on ",as.character(this$shared()$getCurrentDate()),"\n")
			newPrice <- currPrice
		}
		this$shared()$computePnL(newPrice,currPrice,currFXForwardObj$getFXNotional()$getNotional())
		if (this$checkNeedToRebalance(currFXForwardObj,rebalPeriod)){
			temp.forward <- this$createNewForward()
		 	if (!is.null(temp.forward)) currFXForwardObj <- temp.forward 	 
		  	temp.forward.price <- this$priceCurrent(currFXForwardObj)    
		  	if (!is.null(temp.forward.price)) newPrice <- temp.forward.price
		  	else newPrice <- currPrice
	  	}     
	  	currPrice <- newPrice
	  	this$shared()$addRollToHistory()
       
     }
                      
})

#################################################
#Checking Data
#################################################  

setMethodS3("checkIfDataOK","FXForwardRateTri",function(this,...)
	{
		rate <- as.numeric(this$sharedData()$getRateData()[this$shared()$getCurrentDate(),this$.tenor])
		underRepoRate <- as.numeric(this$sharedData()$getOverRepoData()[this$shared()$getCurrentDate(),this$.tenor])
		overRepoRate <- as.numeric(this$sharedData()$getUnderRepoData()[this$shared()$getCurrentDate(),this$.tenor])
		if (!this$checkData(rate,overRepoRate,underRepoRate)) {
			cat("Warning: Data is not sufficient to calculate forward for ",as.character(this$shared()$getCurrentDate()),"\n")
			cat("Rate:",rate," OverRepoRate:",overRepoRate," UnderRepoRate:",underRepoRate,"\n")
			return(FALSE)
		}
		return(TRUE)
	})

setMethodS3("checkData","FXForwardRateTri",function(this,rate,underRepoRate,overRepoRate,...)
	{
		if (!(this$shared()$check(rate)&&
				this$shared()$check(underRepoRate)&&this$shared()$check(overRepoRate))) return(FALSE)
		return(TRUE)
	})

setMethodS3("hasForwardExpired","FXForwardRateTri",function(this,currentFXForward,...) {
		expiryDate <- currentFXForward$getExpiryDate()
		if (expiryDate <= this$shared()$getCurrentDate()) return(TRUE)
		else return(FALSE)
	})

###########################################################
#   Methods for Rebalance Logic
###########################################################

setMethodS3("checkNeedToRebalance","FXForwardRateTri",function(this,currFXForward,rebalPeriod,...)
	{
		if (this$checkForExpiration(currFXForward$getExpiryDate(), this$shared()$getCurrentDate())) return(TRUE)
		if (this$checkForRollDate(this$shared()$getCurrentDate(),as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["rollDate"])),rebalPeriod)) return(TRUE)
		return(FALSE)
	})


setMethodS3("checkForExpiration","FXForwardRateTri",function(this,expiryDate,currentDate,...){
		timeToExpiry <- as.numeric(difftime(expiryDate, currentDate,units = "days"))
		if (timeToExpiry < 2){ 
			return(TRUE)
		}
		return(FALSE)		
	}) 

setMethodS3("checkForRollDate","FXForwardRateTri",function(this,currentDate,lastRollDate,rebalPeriod,...){
		needs(currentDate="POSIXct",lastRollDate="POSIXct")
		t_periods <- list("d" = "day", "w" ="week", "m" = "month")
		rebal_unit <- substr(rebalPeriod,2,2)
		rebal_unit <- as.character(t_periods[rebal_unit])
		rebal_num <- as.numeric(substr(rebalPeriod,1,1))
		if (rebal_unit == "month")
		{
			rebal_unit <- "week"
			rebal_num <- rebal_num * 4
		}
		timeSinceLastRoll <- as.numeric(difftime(currentDate,lastRollDate,units = rebal_unit))
		if (timeSinceLastRoll >= rebal_num) return(TRUE)
		return(FALSE)  
	}) 

    

################################################
#Managing Roll Data
################################################

setMethodS3("getNewForwardRate","FXForwardRateTri",function(this,obsDate,...){
		needs(obsDate="POSIXct")
		return(as.numeric(this$sharedData()$getRateData()[obsDate,this$.tenor]))	
	})

setMethodS3("getNewForwardRateSettle","FXForwardRateTri",function(this,obsDate,...) {
		needs(obsDate="POSIXct")
		return(as.POSIXct(as.numeric(this$sharedData()$getSettleData()[obsDate,this$.tenor]))) 
	})

setMethodS3("getNewForwardRateExpiry","FXForwardRateTri",function(this,obsDate,...) {
		needs(obsDate="POSIXct")
		return(as.POSIXct(as.numeric(this$sharedData()$getExpiryData()[obsDate,this$.tenor])))
	})

setMethodS3("calcRollInfoForForward","FXForwardRateTri",function(this,...){
		obsDate <- as.POSIXct(this$shared()$getCurrentDate())
		rollInfo <- data.frame(as.numeric(obsDate),this$getNewForwardRate(obsDate),as.numeric(this$getNewForwardRateExpiry(obsDate)),as.numeric(this$getNewForwardRateSettle(obsDate)))
		colnames(rollInfo) <- c("rollDate","rate","expiryDate","settleDate")
		rownames(rollInfo) <- obsDate
		if (!this$isForwardDataGood(rollInfo[,"rollDate"],rollInfo[,"rate"],rollInfo[,"expiryDate"],rollInfo[,"settleDate"])) return(NULL)
		else return(rollInfo)
	})

setMethodS3("doesRollInfoExist","FXForwardRateTri",function(this,...){
		if (is.null(this$shared()$getLastRollInfo())) return(FALSE)
		rollDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["rollDate"]))
		rate <- as.numeric(this$shared()$getLastRollInfo()["rate"])
		expiryDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["expiryDate"]))
		settleDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["settleDate"]))
		if (!this$isForwardDataGood(rollDate,rate,expiryDate,settleDate)) return(FALSE)
		else return(TRUE)
	})  

setMethodS3("isForwardDataGood","FXForwardRateTri",function(this,rollDate,rate,expiryDate,settleDate,...){
		if (is.null(rollDate)||is.na(rollDate)) return(FALSE)
		if (is.null(rate)||is.na(rate)||(rate<0)) return(FALSE)
		if (is.null(expiryDate)||is.na(expiryDate)) return(FALSE)
		if (is.null(settleDate)||is.na(settleDate)) return(FALSE) 
		else return(TRUE)			
	})	                                                                      