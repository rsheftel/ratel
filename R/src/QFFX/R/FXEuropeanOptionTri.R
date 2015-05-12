setConstructorS3("FXEuropeanOptionTri", function(FXOptionObj=NULL,fxNotional=NULL,startDate=NULL,
		endDate=NULL,tsdb=NULL,overUnder=NULL,DataLoad=NULL,initialEquity=NULL, lastRollInfo=NULL,...)
{
        this <- extend(RObject(), "FXEuropeanOptionTri",
        .FXOptionObj = FXOptionObj,
        .putCall = NULL,
        .fxNotional = fxNotional,
        .overUnder = overUnder,
		.lastRollInfo = lastRollInfo,
		.startDate = startDate,
		.endDate = endDate,
		.FXCalcSetup = NULL,
		.expiry = NULL
        )    
        constructorNeeds(this,tsdb = "TimeSeriesDB", FXOptionObj = "FXEuropeanOptionGeneric")
        if(!inStaticConstructor(this))
        {
          this$.FXCalcSetup <- FXCalcSetup(FXCurr = FXOptionObj$getFXCurrency(),startDate=startDate,endDate=endDate,tsdb=tsdb,
			  DataLoad=DataLoad,initialEquity=initialEquity, lastRollInfo=lastRollInfo, tenor = FXOptionObj$getFXForwardGeneric()$getTenor())
		  this$.putCall <- this$.FXOptionObj$getPutCall()
		  this$.expiry <- this$.FXOptionObj$getExpiry()
		}
        this
})

setMethodS3("shared","FXEuropeanOptionTri",function(this,...){
	return(this$.FXCalcSetup)
})

setMethodS3("sharedData","FXEuropeanOptionTri",function(this,...){
		return(this$shared()$dataLoad())
})

#####################################################
# Calculation 
#####################################################

setMethodS3("loadOptionUsingRollInfo","FXEuropeanOptionTri",function(this,...)
{
	if (!this$doesRollInfoExist()) this$shared()$setLastRollInfo(this$calcRollInfoForOTROption())
	initialDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["rollDate"]))
	settleDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["settleDate"]))
	strike <- as.numeric(this$shared()$getLastRollInfo()["strike"])
	expiryDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["expiryDate"]))	
 	settleDateList <- list(expiryDate = expiryDate, settleDate = settleDate)
	if (!this$isOptionDataGood(initialDate,strike,expiryDate,settleDate)) return(NULL)
	else return(this$formOptionObj(initialDate,settleDateList,strike))
 })

setMethodS3("createNewOption","FXEuropeanOptionTri",function(this,...){
		roll.info <- this$calcRollInfoForOTROption()
		if (is.null(roll.info)) return(NULL) 
		this$shared()$setLastRollInfo(roll.info) 
		return(this$loadOptionUsingRollInfo())		
	})

setMethodS3("formOptionObj","FXEuropeanOptionTri",function(this,initialDate,settleDateList,strike,...){
	needs(initialDate="POSIXct",settleDateList="list(POSIXct)",strike="numeric")
	temp.fxfwd <- FXForwardSpecific$initSpecificFromGeneric(this$.FXOptionObj$getFXForwardGeneric(),initialDate,
		settleDateList = settleDateList, rate = strike, fxNotional = this$.fxNotional, direction = "buy") 
	return(FXEuropeanOptionSpecific(temp.fxfwd, strike = strike, expiryDate = temp.fxfwd$getExpiryDate(),
			direction = "buy", putCall = this$.putCall))
	})

setMethodS3("priceCurrent","FXEuropeanOptionTri",function(this,currFXOption,...)
{
	interpList <- this$shared()$determineInterpolationScheme(expiry = currFXOption$getExpiryDate(), 
		maxTenor = which(FXTenor$getTenors()==this$.expiry))
  	if (is.null(interpList$interpIndex))
  	{
		cat("Problem in computation. Interpolation Failed!","\n")
		return(NULL)
  	}
  	rate <- as.numeric(this$sharedData()$getRateData()[this$shared()$getCurrentDate(),"spot"])
  	vol <- this$shared()$getInterpolatedData(interpList, this$sharedData()$getVolData())
  	overRepoRate <- this$shared()$getInterpolatedData(interpList, this$sharedData()$getOverRepoData())
  	underRepoRate <- this$shared()$getInterpolatedData(interpList, this$sharedData()$getUnderRepoData())
  	if (!this$checkData(rate,vol,overRepoRate,underRepoRate)) 
  	{
    	cat("Warning: Data is not sufficient to calculate option for ",as.character(this$shared()$getCurrentDate()),"\n")
    	cat("Rate:",rate," Vol:",vol," OverRepoRate:",overRepoRate," UnderRepoRate:",underRepoRate,"\n")
    	return(NULL)
  	}
 # Repo Data is continuously compounded...need to convert. Passing to pricer needs Actual/360, annualized.
  	temp.startDate <- this$shared()$getCurrentDate()
  	temp.settleDate <- currFXOption$getSettleDate()
 
  	regDates <- list(expiryDate = as.POSIXct(as.numeric(this$sharedData()$getExpiryData()[this$shared()$getCurrentDate(),"spot"])), 
  		settleDate = as.POSIXct(as.numeric(this$sharedData()$getSettleData()[this$shared()$getCurrentDate(),"spot"])))
  	spotSettleDate <- regDates$settleDate             
  
  	temp.overRepoRate <- fincad("aaConvert_cmpd2",d_e = spotSettleDate, d_t = temp.settleDate, rate_from = overRepoRate, rate_basis_from = 6, acc_from = 1, rate_basis_to = 1, acc_to = 2)
  	temp.underRepoRate <- fincad("aaConvert_cmpd2",d_e = spotSettleDate, d_t = temp.settleDate, rate_from = underRepoRate, rate_basis_from = 6, acc_from = 1, rate_basis_to = 1, acc_to = 2)
  
  	if (temp.overRepoRate < 0) temp.overRepoRate <- 0
  	if (temp.underRepoRate < 0) temp.underRepoRate <- 0
  
  	pricer <- FXEuropeanOptionPricer()
  	pricer$setTerms(currFXOption, this$shared()$getCurrentDate(),spotRate = rate, spotSettleDate = spotSettleDate, volLn = vol, overRepoRate = temp.overRepoRate, 
    	underRepoRate = temp.underRepoRate, holidayDates = this$.holidayDates) 
  	return(pricer$getPrice(this$.overUnder)) 
 })

 
 setMethodS3("computeEquityCurve","FXEuropeanOptionTri",function(this,rebalPeriod=NULL, rebalDelta=NULL,...)
	 {
		 rebalPeriodList <- c("1d","1w","2w","1m")
		 failUnless(any(rebalPeriod == rebalPeriodList),squish(rebalPeriod," is not a valid Rebal Period"))
		 rebalDeltaList <- c(0.10,0.15,0.20)
		 failUnless(any(rebalDelta == rebalDeltaList),squish(rebalDelta," is not a valid Rebal Delta"))
		 
		 this$shared()$setCurrentDate(this$.startDate)
		 currFXOptionObj <- this$loadOptionUsingRollInfo()
		 if (is.null(this$shared()$getRollHistory())) this$shared()$addRollToHistory()
		 currPriceInfo <- this$priceCurrent(currFXOptionObj)
		 
		 for (i in (match(this$shared()$getCurrentDate(),index(this$sharedData()$getVolData()))+1):
			 length(index(this$sharedData()$getVolData()))) {              
			 this$shared()$setCurrentDate(index(this$sharedData()$getVolData()[i,]))
			 if (this$hasOptionExpired(currFXOptionObj)) {
				 currFXOptionObj <- this$createNewOption()
				 currPriceInfo <- this$priceCurrent(currFXOptionObj)
				 cat("Failure due to option expiring, creating new Option",as.character(this$shared()$getCurrentDate()),"\n")
			 } 
			 newPriceInfo <-  this$priceCurrent(currFXOptionObj)
			 if (is.null(newPriceInfo)) {
				 cat("Problem computing option price","\n")
				 newPriceInfo <- currPriceInfo
			 }
			 this$shared()$computePnL(newPriceInfo[1],currPriceInfo[1],currFXOptionObj$getFXForward()$getFXNotional()$getNotional())
			 if (this$checkNeedToRebalance(newPriceInfo,currFXOptionObj, rebalPeriod,rebalDelta)){
				 temp.option <- this$createNewOption()
				 if (!is.null(temp.option)) currFXOptionObj <- temp.option 	 
				 temp.option.info <- this$priceCurrent(currFXOptionObj)    
				 if (!is.null(temp.option.info)) newPriceInfo <- temp.option.info
				 else newPriceInfo <- currPriceInfo
			 }     
			 currPriceInfo <- newPriceInfo
			 this$shared()$addRollToHistory()
		 }                  
	})
	  
###########################################################
#   Methods for Rebalance Logic
###########################################################
  
setMethodS3("checkNeedToRebalance","FXEuropeanOptionTri",function(this,priceResults,currFXOption,rebalPeriod,rebalDelta,...)
{
  # Needs to be called AFTER being priced for that day.
	if (is.null(priceResults)) return(FALSE)
    if (this$checkForExpiration(currFXOption$getExpiryDate(), this$shared()$getCurrentDate())) return(TRUE)
    if (this$checkDelta(priceResults[2],rebalDelta)) return(TRUE) 
    if (this$checkForRollDate(this$shared()$getCurrentDate(),as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["rollDate"])),rebalPeriod)) return(TRUE)
	return(FALSE)
 })
 
setMethodS3("checkDelta","FXEuropeanOptionTri",function(this,delta,rebalDelta,...){
	if ((abs(delta) > (0.5 + rebalDelta))||(abs(delta) < (0.5 - rebalDelta))) return(TRUE)
	return(FALSE)
}) 

setMethodS3("checkForExpiration","FXEuropeanOptionTri",function(this,expiryDate,currentDate,...){
	timeToExpiry <- as.numeric(difftime(expiryDate, currentDate,units = "days"))
	if (timeToExpiry < 2){ 
		return(TRUE)
	}
	return(FALSE)		
}) 

setMethodS3("checkForRollDate","FXEuropeanOptionTri",function(this,currentDate,lastRollDate,rebalPeriod,...){
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

#################################################
#Checking Data
#################################################  

setMethodS3("checkIfDataOK","FXEuropeanOptionTri",function(this,...)
{
	rate <- as.numeric(this$sharedData()$getRateData()[this$shared()$getCurrentDate(),this$.expiry])
	vol <-  as.numeric(this$sharedData()$getVolData()[this$shared()$getCurrentDate(),this$.expiry])
	underRepoRate <- as.numeric(this$sharedData()$getOverRepoData()[this$shared()$getCurrentDate(),this$.expiry])
	overRepoRate <- as.numeric(this$sharedData()$getUnderRepoData()[this$shared()$getCurrentDate(),this$.expiry])
	if (!this$checkData(rate,vol,overRepoRate,underRepoRate)) {
		cat("Warning: Data is not sufficient to calculate option for ",as.character(this$shared()$getCurrentDate()),"\n")
		cat("Rate:",rate," Vol:",vol," OverRepoRate:",overRepoRate," UnderRepoRate:",underRepoRate,"\n")
		return(FALSE)
	}
	return(TRUE)
})
       
setMethodS3("checkData","FXEuropeanOptionTri",function(this,rate,vol,underRepoRate,overRepoRate,...)
{
  if (!(this$shared()$check(rate)&&this$shared()$check(vol)&&
		  this$shared()$check(underRepoRate)&&this$shared()$check(overRepoRate))) return(FALSE)
  return(TRUE)
})

setMethodS3("hasOptionExpired","FXEuropeanOptionTri",function(this,currentFXOption,...) {
		expiryDate <- currentFXOption$getExpiryDate()
		if (expiryDate <= this$shared()$getCurrentDate()) return(TRUE)
		else return(FALSE)
})
################################################
#Managing Roll Data
################################################

setMethodS3("getNewOptionStrike","FXEuropeanOptionTri",function(this,obsDate,...){
		needs(obsDate="POSIXct")
		return(as.numeric(this$sharedData()$getRateData()[obsDate,this$.expiry]))	
	})

setMethodS3("getNewOptionSettle","FXEuropeanOptionTri",function(this,obsDate,...) {
		needs(obsDate="POSIXct")
		return(as.POSIXct(as.numeric(this$sharedData()$getSettleData()[obsDate,this$.expiry]))) 
})

setMethodS3("getNewOptionExpiry","FXEuropeanOptionTri",function(this,obsDate,...) {
		needs(obsDate="POSIXct")
		return(as.POSIXct(as.numeric(this$sharedData()$getExpiryData()[obsDate,this$.expiry])))
})

setMethodS3("calcRollInfoForOTROption","FXEuropeanOptionTri",function(this,...){
		obsDate <- as.POSIXct(this$shared()$getCurrentDate())
		rollInfo <- data.frame(as.numeric(obsDate),this$getNewOptionStrike(obsDate),as.numeric(this$getNewOptionExpiry(obsDate)),as.numeric(this$getNewOptionSettle(obsDate)))
		colnames(rollInfo) <- c("rollDate","strike","expiryDate","settleDate")
		rownames(rollInfo) <- obsDate
		if (!this$isOptionDataGood(rollInfo[,"rollDate"],rollInfo[,"strike"],rollInfo[,"expiryDate"],rollInfo[,"settleDate"])) return(NULL)
		else return(rollInfo)
	})

setMethodS3("doesRollInfoExist","FXEuropeanOptionTri",function(this,...){
		if (is.null(this$shared()$getLastRollInfo())) return(FALSE)
		rollDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["rollDate"]))
		strike <- as.numeric(this$shared()$getLastRollInfo()["strike"])
		expiryDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["expiryDate"]))
		settleDate <- as.POSIXct(as.numeric(this$shared()$getLastRollInfo()["settleDate"]))
		if (!this$isOptionDataGood(rollDate,strike,expiryDate,settleDate)) return(FALSE)
		else return(TRUE)
	})  

setMethodS3("isOptionDataGood","FXEuropeanOptionTri",function(this,rollDate,strike,expiryDate,settleDate,...){
		if (is.null(rollDate)||is.na(rollDate)) return(FALSE)
		if (is.null(strike)||is.na(strike)||(strike<0)) return(FALSE)
		if (is.null(expiryDate)||is.na(expiryDate)) return(FALSE)
		if (is.null(settleDate)||is.na(settleDate)) return(FALSE) 
		else return(TRUE)			
	})