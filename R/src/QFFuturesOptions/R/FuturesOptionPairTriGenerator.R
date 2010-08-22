# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


constructor('FuturesOptionPairTriGenerator', function(underlying = NULL, type = NULL, rollLogic = NULL, optionFrequency = NULL, width = NULL){
	this <- extend(RObject(), 'FuturesOptionPairTriGenerator', .underlying = underlying, 
		.type = type, .rollLogic = rollLogic, .optionFrequency = optionFrequency, .width = width)
	constructorNeeds(this, underlying = 'character', rollLogic = 'function', optionFrequency = 'character', width = 'numeric?')
	if(inStaticConstructor(this)) return(this)
	assert(type %in% c('straddle','strangle','straddleStrangle'))
	assert(optionFrequency %in% c('monthly', 'quarterly'))
	
	this	
})

method('underlying', 'FuturesOptionPairTriGenerator', function(this, ...){
	this$.underlying
})

method('underlyingContract', 'FuturesOptionPairTriGenerator', function(this, ...){
	Contract(this$underlying(), 'Comdty')	
})

method('type' , 'FuturesOptionPairTriGenerator', function(this,...){
	this$.type
})

method('rollLogic', 'FuturesOptionPairTriGenerator', function(this, ...){
	this$.rollLogic
})

method('optionFrequency', 'FuturesOptionPairTriGenerator', function(this,...){
	this$.optionFrequency	
})

method('width', 'FuturesOptionPairTriGenerator', function(this,...){
	this$.width	
})

method('getAvailableDates', 'FuturesOptionPairTriGenerator', function(this,start = NULL, end = NULL, source = 'financialcalendar', financialCenter = 'nyb',...){
	ticker <- toupper(this$underlying())
	dataSeries <- Symbol(paste(ticker, '.1C', sep = ''))$series()
	if(is.null(start)) start <- first(index(dataSeries))
	if(is.null(end)) end <- last(index(dataSeries))
	returnDates <- index(Range(start, end)$cut(dataSeries))
	holidays <- HolidayDataLoader$getHolidays(source, financialCenter)
	returnDates[!(returnDates %in% holidays)]
})

method('dateSplit', 'FuturesOptionPairTriGenerator', function(this, currentDate, ...){
	output <- as.POSIXlt(currentDate)
	list(month = output$mon+1, year = output$year + 1900, day = output$mday)		
})

method('initializeUnderlyings', 'FuturesOptionPairTriGenerator', function(this, currentDate, ...){
	optionInfo <- this$rollLogic()(currentDate = currentDate, 
								   nextDate = NULL,
								   lastDate = NULL,
								   optionFrequency = this$optionFrequency(),
								   currentStrike = NULL,
								   currentDelta = NULL,
								   underlyingFuture = this$underlyingContract(),
								   width = this$width())

	underlyingSpecificContract <- SpecificContract(this$underlying(), optionInfo$underlyingMonth, optionInfo$underlyingYear)							   
	options <- this$options(underlyingSpecificContract, optionInfo$optionMonth, optionInfo$optionYear, optionInfo$strike)
	
	return(list(underlyingContract = underlyingSpecificContract, options = options, centralStrike = optionInfo$strike))
})

method('getNewUnderlyings', 'FuturesOptionPairTriGenerator', function(this, currentDate, underlyingContracts, lastStrike, ...){
	liborInterpolator <- LiborInterpolator()
	if(this$type() == 'straddle' || this$type() == 'strangle'){
		if(is.empty(underlyingContracts$options$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[currentDate,])) 
				leg <- 'lower' else leg <- 'upper'
		
		newStrike <- this$getNewUnderlyingsStrike(underlyingContracts$options$optionLeg(leg),leg)
		option <- underlyingContracts$options$optionLeg(leg)
		underlyingSpecificContract <- SpecificContract(this$underlying(), option$underlyingExpiryMonth(), option$underlyingExpiryYear())
		newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		
		if(is.empty(newOptions$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		if(is.empty(newOptions$optionLeg(leg)$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),])) 	{
			newStrike <- this$getNewUnderlyingsStrike(newOptions$optionLeg(leg),leg)
			newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(),option$optionExpiryYear(), newStrike)
		}
		
		if(is.empty(newOptions$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate, '15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		if(is.empty(newOptions$optionLeg(leg)$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate, '15:00:00')),])){
			newStrike <- this$getNewUnderlyingsStrike(newOptions$optionLeg(leg), leg)
			newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		}		
		
		if(is.empty(newOptions$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate, '15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		if(is.empty(newOptions$optionLeg(leg)$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate, '15:00:00')),])){
			newStrike <- this$getNewUnderlyingsStrike(newOptions$optionLeg(leg), leg)
			newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		}			
	}		
	if(this$type() == 'straddleStrangle'){
		if(is.empty(underlyingContracts$options[[1]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			optionNum <- 1 else optionNum <- 2
		if(is.empty(underlyingContracts$options[[optionNum]]$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		
		newStrike  <- this$getNewUnderlyingsStrike(underlyingContracts$options[[optionNum]]$optionLeg(leg),leg)
		option <- underlyingContracts$options[[optionNum]]$optionLeg(leg)
		strikeDistance <- newStrike - option$strike()
		newStrike <- underlyingContracts$centralStrike + strikeDistance
		underlyingSpecificContract <- SpecificContract(this$underlying(), option$underlyingExpiryMonth(), option$underlyingExpiryYear())
		newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		
		if(is.empty(newOptions[[1]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			optionNum <- 1 else optionNum <- 2
		if(is.empty(newOptions[[optionNum]]$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		if(is.empty(newOptions[[optionNum]]$optionLeg(leg)$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),])){
			oldCentralStrike <- newStrike
			newStrike <- this$getNewUnderlyingsStrike(newOptions[[1]]$optionLeg(leg),leg)
			option <- newOptions[[optionNum]]$optionLeg(leg)
			strikeDistance <- newStrike - option$strike()
			newStrike <- oldCentralStrike + strikeDistance
			underlyingSpecificContract <- SpecificContract(this$underlying(), option$underlyingExpiryMonth(), option$underlyingExpiryYear())
			newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		}
		
		if(is.empty(newOptions[[1]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			optionNum <- 1 else optionNum <- 2
		if(is.empty(newOptions[[optionNum]]$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		if(is.empty(newOptions[[optionNum]]$optionLeg(leg)$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),])){
			oldCentralStrike <- newStrike
			newStrike <- this$getNewUnderlyingsStrike(newOptions[[1]]$optionLeg(leg),leg)
			option <- newOptions[[optionNum]]$optionLeg(leg)
			strikeDistance <- newStrike - option$strike()
			newStrike <- oldCentralStrike + strikeDistance
			underlyingSpecificContract <- SpecificContract(this$underlying(), option$underlyingExpiryMonth(), option$underlyingExpiryYear())
			newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		}		
		if(is.empty(newOptions[[1]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			optionNum <- 1 else optionNum <- 2
		if(is.empty(newOptions[[optionNum]]$optionLeg('lower')$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),]))
			leg <- 'lower' else leg <- 'upper'
		if(is.empty(newOptions[[optionNum]]$optionLeg(leg)$deltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(currentDate,'15:00:00')),])){
			oldCentralStrike <- newStrike
			newStrike <- this$getNewUnderlyingsStrike(newOptions[[1]]$optionLeg(leg),leg)
			option <- newOptions[[optionNum]]$optionLeg(leg)
			strikeDistance <- newStrike - option$strike()
			newStrike <- oldCentralStrike + strikeDistance
			underlyingSpecificContract <- SpecificContract(this$underlying(), option$underlyingExpiryMonth(), option$underlyingExpiryYear())
			newOptions <- this$options(underlyingSpecificContract, option$optionExpiryMonth(), option$optionExpiryYear(), newStrike)
		}				
	}	
	print(newStrike)	
	return(list(underlyingContract = underlyingSpecificContract, options = newOptions, centralStrike = newStrike))
})

method('getNewUnderlyingsStrike', 'FuturesOptionPairTriGenerator', function(this, option,leg,...){
	availableStrikes <- option$availableStrikes()
	if(option$strike() %in% availableStrikes) strikePos <- match(option$strike(), availableStrikes)
	if(!(option$strike() %in% availableStrikes))
		strikePos <- match(option$strike(), sort(c(option$strike(),availableStrikes))) - 1
	if(strikePos > length(availableStrikes)/2)
		strikePos <- strikePos - 1 else strikePos <- strikePos + 1
	newStrike <- availableStrikes[[strikePos]]
	newStrike
})

method('options', 'FuturesOptionPairTriGenerator', function(this, underlyingSpecificContract, optionMonth, optionYear, strike, ...){
	attributes <- underlyingSpecificContract$attributes()
	if(this$type() == 'straddle') return(FuturesOptionPair(underlying = this$underlying(), 
														   type = 'straddle',
														   optionMonth = optionMonth,
														   optionYear = optionYear,
														   underlyingMonth = attributes$future_month,
														   underlyingYear = attributes$future_year, 
														   centralStrike = strike, 
														   width = NULL))
	if(this$type() == 'strangle') return(FuturesOptionPair(underlying = this$underlying(),
														   type = 'strangle',
														   optionMonth = optionMonth,
														   optionYear = optionYear,
														   underlyingMonth = attributes$future_month,
														   underlyingYear = attributes$future_year,
														   centralStrike = strike,
														   width = this$width()))
	if(this$type() == 'straddleStrangle') return(list(FuturesOptionPair(underlying = this$underlying(), 
																		type = 'straddle',
																		optionMonth = optionMonth,
																		optionYear = optionYear,
																		underlyingMonth = attributes$future_month,
																		underlyingYear = attributes$future_year,
																		centralStrike = strike, 
																		width = NULL),
												      FuturesOptionPair(underlying = this$underlying(), 
																		type = 'strangle', 
																		optionMonth = optionMonth,
																		optionYear = optionYear,
																		underlyingMonth = attributes$future_month,
																		underlyingYear = attributes$future_year,
																		centralStrike = strike, 
																		width = this$width())))
})

method('runDailyHedgedReturns', 'FuturesOptionPairTriGenerator', function(this, start = NULL, end = NULL, strangleWeight = 1,holidaySource = 'financialcalendar', financialCenter = 'nyb',...){
	datesToRun <- this$getAvailableDates(start, end, holidaySource, financialCenter)
	
	# get Libor Interpolator
	liborInterpolator <- LiborInterpolator()
	
	outputZoo <- zoo(NA, datesToRun)
	currentDate <- as.POSIXct(index(outputZoo[1]))
	underlyingContracts <- this$initializeUnderlyings(currentDate)
	#outputZoo[1] <- this$calculateHedgedReturns(currentDate, underlyingContracts)
	
	lastDate <- as.POSIXct(currentDate)		
	
		
	for(dateNum in 2:length(index(outputZoo))){
		print(as.POSIXct(index(outputZoo[dateNum])))
		newOptionData <- this$rollLogic()(currentDate = as.POSIXct(index(outputZoo[dateNum])), nextDate = NULL, lastDate = as.POSIXct(lastDate),
			optionFrequency = this$optionFrequency(), currentStrike = underlyingContracts$centralStrike,
			underlyingFuture = underlyingContracts$underlyingContract,
			width = this$width())
		
		if(!is.null(newOptionData)) underlyingContracts <- this$initializeUnderlyings(as.POSIXct(index(outputZoo[dateNum])))
		
		print(underlyingContracts$centralStrike)
		if(length(underlyingContracts$options) == 1){
			
			currentData <- underlyingContracts$options$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(index(outputZoo[dateNum]),"15:00:00"))]
			if(is.empty(currentData)) {
				underlyingContracts <- this$getNewUnderlyings(as.POSIXct(paste(index(outputZoo[dateNum]),"15:00:00")), underlyingContracts, underlyingContracts$centralStrike)
				currentData <- underlyingContracts$options$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(index(outputZoo[dateNum]),"15:00:00"))] 
			}
			outputZoo[dateNum] <- currentData
		} 
		if(length(underlyingContracts$options) == 2){
			currentData <- underlyingContracts$options[[1]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(index(outputZoo[dateNum]),"15:00:00"))] -
							      (strangleWeight * underlyingContracts$options[[2]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(index(outputZoo[dateNum]),"15:00:00"))])
			if(is.empty(currentData)){
				underlyingContracts <- this$getNewUnderlyings(as.POSIXct(index(outputZoo[dateNum])), underlyingContracts, underlyingContracts$centralStrike)
				currentData <- underlyingContracts$options[[1]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(index(outputZoo[dateNum]),"15:00:00"))] -
								  (strangleWeight * underlyingContracts$options[[2]]$pairDeltaHedgedReturns(liborInterpolator = liborInterpolator)[as.POSIXct(paste(index(outputZoo[dateNum]),'15:00:00'))])	
			}				  
			outputZoo[dateNum] <- currentData				  
		}
		
		lastDate <- index(outputZoo[dateNum])
	}		
	outputZoo	
})

method('readTable', 'FuturesOptionPairTriGenerator', function(this, fileName, ...){
	input <- read.table(fileName, header = TRUE, sep = ',', row.names =1)
	zoo(input, order.by = row.names(input))		
})

method('writeTable', 'FuturesOptionPairTriGenerator', function(this, optionData, fileName, ...){
	write.table(optionData, fileName, row.names = index(optionData), sep = ',', col.names = TRUE, append = FALSE)	
})














