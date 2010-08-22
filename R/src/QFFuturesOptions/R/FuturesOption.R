#FuturesOption object
#
# 
# Author: dhorowitz
# Last updated: 8/20/08
###############################################################################


constructor("FuturesOption", function(underlying = NULL, optionType = NULL, optionExpiryMonth=NULL, optionExpiryYear=NULL, 
									underlyingExpiryMonth=NULL, underlyingExpiryYear=NULL, strike = NULL){
		this <- extend(RObject(), "FuturesOption", .underlying = underlying, .optionType = optionType, .optionExpiryMonth = optionExpiryMonth,
						.optionExpiryYear = optionExpiryYear, .underlyingExpiryMonth = underlyingExpiryMonth, .underlyingExpiryYear = underlyingExpiryYear , .strike = strike)
		constructorNeeds(this, underlying = "Contract", optionExpiryMonth = 'numeric', optionExpiryYear = 'numeric',
						  underlyingExpiryYear = 'numeric', strike = 'numeric?')
		if(inStaticConstructor(this)) return(this)
		assert(this$.optionType %in% c('put', 'call'), 'optionType must be put or call in FuturesOption')
		
		this$.underlyingName <- this$.underlying$name()
		this$.underlyingCycle <- this$.underlying$monthlyCycle()
		if(is.null(underlyingExpiryMonth)) this$.underlyingExpiryMonth <- this$underlyingExpiryMonthCalculator()
		
		this
})

method('underlyingName', "FuturesOption", function(this, ...){
	this$.underlyingName
})

method('optionType', 'FuturesOption', function(this, ...){
	this$.optionType		
})

method('underlyingCycle', 'FuturesOption', function(this,...){
	this$.underlyingCycle		
})

method('underlying', 'FuturesOption', function(this,...){
	this$.underlying	
})

method('optionExpiryMonth', 'FuturesOption', function(this, ...){
	this$.optionExpiryMonth	
})

method('optionExpiryYear', 'FuturesOption', function(this,...){
	this$.optionExpiryYear		
})

method('strike', 'FuturesOption', function(this, ...){
	this$.strike	
})

method('underlyingExpiryMonthCalculator', 'FuturesOption', function(this, optionExpiryMonth = this$optionExpiryMonth(),...){
	needs(optionExpiryMonth = 'numeric')
	cycleIndex <- apply(outer(optionExpiryMonth, this$underlyingCycle(),'>'), 1, sum) + 1
	this$underlyingCycle()[cycleIndex]	
})

method('underlyingExpiryMonth', 'FuturesOption', function(this,...){
	this$.underlyingExpiryMonth	
})

method('underlyingExpiryYear', 'FuturesOption', function(this, ...){
	this$.underlyingExpiryYear	
})

method('attributeList', 'FuturesOption', function(this, currentMonth = this$optionExpiryMonth(), optionExpiryYear = this$optionExpiryYear(), ...){
	needs(optionExpiryYear = 'numeric')
	list( 
		option_month = currentMonth,
		contract = this$underlyingName(),
		quote_convention = 'price',
		quote_type = 'close',
		future_month = this$underlyingExpiryMonth(),
		expiry = 'actual',
		option_contract = this$underlyingName(),
		option_type = this$optionType(),
		quote_side = 'mid',
		option_year = optionExpiryYear,
		instrument = 'futures_option'		
	)		
})


method('availableStrikes', 'FuturesOption', function(this, currentMonth = this$optionExpiryMonth(), optionExpiryYear = this$optionExpiryYear(), source = 'internal', ...){
	if(is.null(this$.availableStrikes)){
		series <- TimeSeriesDB$lookupTimeSeriesByAttributeList(this$attributeList(currentMonth, optionExpiryYear), data.source = source)
		this$.availableStrikes <- sort(unique(as.numeric(TimeSeriesDB$lookupAttributesForTimeSeries(series, attribute = 'strike'))))
	}
	this$.availableStrikes
})

method('expiryDates', 'FuturesOption', function(this, currentMonth = this$optionExpiryMonth(), optionExpiryYear = this$optionExpiryYear(),source = 'internal', ...){
	if(is.null(this$.expiryDate)) {	
	series <- TimeSeriesDB$lookupTimeSeriesByAttributeList(this$attributeList(currentMonth, optionExpiryYear), data.source = source)[1]
	this$.expiryDate <- as.Date(as.character(TimeSeriesDB$lookupAttributesForTimeSeries(series)[,'expiry_date']))
	}
	this$.expiryDate
})

method('historicalPrices', 'FuturesOption', function(this,  start = NULL, end = NULL, strike=this$strike(),currentMonth = this$optionExpiryMonth(), optionExpiryYear = this$optionExpiryYear(), source = 'internal', ...){
	needs(optionExpiryYear = 'numeric', strike = 'numeric')
	
	if(!(strike %in% this$availableStrikes())) return(NULL)
	if((!is.null(start) && !is.null(this$.start) && start != this$.start)
		|| (!is.null(end) && !is.null(this$.end) && end != this$.end)
		|| is.null(this$.historicalPrices)){
		this$.start <- start
		this$.end <- end
		this$.historicalPrices <- TimeSeriesDB()$retrieveTimeSeriesByAttributeList(attributes = c(this$attributeList(currentMonth, optionExpiryYear), strike = strike),
		 data.source = source, start = start, end = end)[[1]]
 		}		
	this$.historicalPrices
})

method('historicalUnderlyingPrices', 'FuturesOption', function(this, start = NULL, end = NULL,...){
	if(is.null(this$.underlyingPrices)){
		underlyingContract <- SpecificContract(this$underlyingName(), this$underlyingExpiryMonth(), this$underlyingExpiryYear())	
		this$.underlyingPrices <- underlyingContract$historicalPrices(start = start, end = end)
	}	
	this$.underlyingPrices
})

method('historicalChanges', 'FuturesOption', function(this, strike = this$strike(), start = NULL, end = NULL, currentMonth = this$optionExpiryMonth(), optionExpiryYear = this$optionExpiryYear(), source = 'internal', ...){
	needs(optionExpiryYear = 'numeric', strike = 'numeric')
	diff(this$historicalPrices(start, end, strike, currentMonth, optionExpiryYear, source))
})

method('atTheMoneyStrike', 'FuturesOption', function(this, currentDate, currentMonth = this$optionExpiryMonth(), optionExpiryYear = this$optionExpiryYear(), source = 'internal', ...){
	needs(currentMonth = 'numeric')
	availableStrikeList <- this$availableStrikes(currentMonth, optionExpiryYear, source)
	currentUnderlyingPrice <- as.numeric(this$historicalUnderlyingPrices()[as.POSIXct(paste(currentDate,"15:00:00"))])
	distanceFromStrike <- abs(currentUnderlyingPrice - availableStrikeList)
	availableStrikeList[match(min(distanceFromStrike), distanceFromStrike)]		
})

method('historicalDeltas', 'FuturesOption', function(this, start = NULL, end = NULL, minimumDaysToExpiry = 10, liborInterpolator = LiborInterpolator(), ...){
	if(is.null(this$.historicalDeltas)){
		historicalPrices <- this$historicalPrices(start, end)
		underlyingPrices <- this$historicalUnderlyingPrices(start, end)
		expirationDate <- as.character(this$expiryDates())
		strike <- this$strike()
		historicalPrices <- merge(historicalPrices, underlyingPrices, all = FALSE)
		deltaZoo <- zoo(NA, index(historicalPrices))
		discountRate <- liborInterpolator$interpolatedRate(as.POSIXct(index(historicalPrices[1])), expirationDate)/100
		
		for(dateNum in 1:(length(index(historicalPrices))-1)){
			currentDate <- as.POSIXct(index(historicalPrices[dateNum]))

			newDiscountRate <- liborInterpolator$interpolatedRate(currentDate, expirationDate)/100
			if(!is.na(newDiscountRate)) discountRate <- newDiscountRate
			
			if(as.numeric(as.Date(expirationDate) - as.Date(currentDate)) < minimumDaysToExpiry){
				deltaZoo[dateNum] <- NA
				next()
			}
			
			tryCatch({deltaZoo[dateNum] <- FuturesOptionCalculator$calcDelta(valueDate = currentDate,
									underlyingPrice = as.numeric(historicalPrices[currentDate,2]),
									disc = discountRate,
									expiry = expirationDate,
									optionType = this$optionType(),
									strike = this$strike(),
									dateCountConvention = 'calendar',
									haveValue = 'price',
									price = as.numeric(historicalPrices[currentDate,1]));},
									error = function(e) {deltaZoo[dateNum] <- NA;})
		}
		this$.historicalDeltas <- deltaZoo
	}
	this$.historicalDeltas
})

method('deltaHedgedReturns', 'FuturesOption', function(this, start = NULL, end = NULL, liborInterpolator = LiborInterpolator(),...){
	if(is.null(this$.deltaHedgedReturns)){
		if(is.empty(this$historicalPrices(start = start, end = end))) return(NULL)
		if(length(this$historicalPrices(start = start, end = end)) == 1) return(NULL)

		dataZoo <- merge(diff(this$historicalPrices(start=start,end=end)), 
				   		 diff(this$historicalUnderlyingPrices(start = start, end = end)),
						 lag(this$historicalDeltas(start = start, end = end, liborInterpolator = liborInterpolator),-1), all = FALSE)
		this$.deltaHedgedReturns <- dataZoo[,1] - dataZoo[,2] * dataZoo[,3]
	}
	this$.deltaHedgedReturns
})
