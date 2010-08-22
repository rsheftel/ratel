# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


constructor('FuturesOptionRollLogic', function(){
		this <- extend(RObject(), 'FuturesOptionRollLogic')
		if(inStaticConstructor(this)) return(this)
		
		this
})

method('abstract', 'FuturesOptionRollLogic', function(this, 
													currentDate,
													nextDate,
													lastDate,
													optionFrequency,
													currentStrike,
													currentDelta,
													...){
	### Not an actual function
	### simply an abstract function that defines the inputs for a FuturesOptionRollLogic function
})

method('jpMorgan', 'FuturesOptionRollLogic', function(this, currentDate, nextDate, lastDate = NULL, optionFrequency='quarterly',
						    						  currentStrike = NULL, currentDelta=NULL,...){
	needs(currentStrike = 'numeric?', currentDelta = 'numeric?')
	assert(optionFrequency == 'quarterly', "FuturesOptionRollLogic$jpMorgan is only implemented for quarterly optionFrequency")
	assert(as.Date(nextDate) > as.Date(currentDate), "nextDate must be after currentDate in FuturesOptionRollLogic")
	nextYearFlag <- FALSE
	
	nextDateQuarter <- trunc((as.POSIXlt(nextDate)$mon + 2)/3) + 1
	if(nextDateQuarter == 5) {nextDateQuarter <- 1; nextYearFlag <- TRUE}
		
	optionMonth <- nextDateQuarter * 3
	underlyingMonth <- nextDateQuarter * 3
	
	
	if(nextYearFlag == TRUE) underlyingYear <- as.POSIXlt(nextDate)$year + 1900 + 1 else underlyingYear <- as.POSIXlt(nextDate)$year + 1900
	return(zoo(data.frame(optionMonth = optionMonth, optionYear = underlyingYear,
						  underlyingMonth = underlyingMonth, underlyingYear = underlyingYear), order.by = currentDate))
})			

method('straddleStrangle', 'FuturesOptionRollLogic', function(this, currentDate, nextDate, lastDate = NULL, optionFrequency='quarterly',
															  currentStrike = NULL, currentDelta = NULL, underlyingFuture = NULL, width,
															  tolerance = 0.8, ...){
	needs(currentStrike = 'numeric?', currentDelta = 'numeric?', width = 'numeric', tolerance = 'numeric')
	
	# currently just implemented for quarterly
	assert(optionFrequency == 'quarterly')

	#initialize the dataStream with at-the-money strike
	if(is.null(lastDate)) {
		return(this$nextStraddleStrangleData(currentDate,optionFrequency, underlyingFuture))				
	}		
	
	if(this$underlyingDatesQuarterly(currentDate)$underlyingMonth != underlyingFuture$expiryMonth())
		return(this$nextStraddleStrangleData(currentDate, optionFrequency, underlyingFuture$generic()))
	
	if(abs(as.numeric(underlyingFuture$historicalPrices()[as.POSIXct(paste(currentDate, ' 15:00:00',sep =''))]) - currentStrike) < (width*tolerance)) return(NULL)

	data <- this$nextStraddleStrangleData(currentDate, optionFrequency, underlyingFuture$generic())
	data	  
})				

method('nextStraddleStrangleData', 'FuturesOptionRollLogic', function(this, currentDate, optionFrequency = 'quarterly', underlyingFuture,...){
	underlyingDates <- this$underlyingDatesQuarterly(currentDate)
	currentDate <- as.POSIXct(currentDate)
	futOpt <- FuturesOption(underlying = underlyingFuture,
		optionType = 'call',
		optionExpiryMonth = underlyingDates$optionMonth,
		optionExpiryYear = underlyingDates$optionYear,
		underlyingExpiryMonth = underlyingDates$underlyingMonth,
		underlyingExpiryYear = underlyingDates$underlyingYear)
	strike <- futOpt$atTheMoneyStrike(currentDate)
	return(list(optionMonth = underlyingDates$optionMonth,
			optionYear = underlyingDates$optionYear,
			underlyingMonth = underlyingDates$underlyingMonth,
			underlyingYear = underlyingDates$underlyingYear,
			strike = strike))						
})

method('underlyingDatesQuarterly', 'FuturesOptionRollLogic', function(this, currentDate, ...){
	currentDate <- as.POSIXlt(currentDate)	
	nextYearFlag <- FALSE
	dateQuarter <- trunc((currentDate$mon + 2)/3) + 1
	if(dateQuarter == 5) {dateQuarter <- 1; nextYearFlag <- TRUE}
	
	underlyingMonth <- dateQuarter * 3
	if(nextYearFlag == TRUE) underlyingYear <- currentDate$year + 1900 + 1 else underlyingYear <- currentDate$year + 1900
	
	optionMonth <- dateQuarter * 3
	optionYear <- underlyingYear
	
	return(list(underlyingMonth = underlyingMonth, underlyingYear = underlyingYear, optionMonth = optionMonth, optionYear = optionYear))		
})


