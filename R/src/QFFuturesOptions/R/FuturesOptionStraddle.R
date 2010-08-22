# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################



constructor("FuturesOptionStraddle", function(underlying = NULL){
	this <- extend(RObject(), "FuturesOptionStraddle", .underlying = underlying)
	constructorNeeds(this, underlying = 'character')
	if(inStaticConstructor(this)) return(this)
				
	this
	})

method('underlying', 'FuturesOptionStraddle', function(this, ...){
	this$.underlying	
})

method('underlyingContract', 'FuturesOptionStraddle', function(this,...){
	Contract(this$underlying(), 'Comdty')		
})

method('runHistoricalPrices', 'FuturesOptionStraddle', function(this, start, end, rollLogic, optionFrequency = 'quarterly', source = 'internal', base = 100, ...){
	needs(rollLogic = 'function')
	assert(optionFrequency %in% c('quarterly', 'monthly'))
	print('getting Available Dates')
	availableDates <- this$getAvailableDates(start, end)
	print('getting RollData')
	optionData <- this$getRollDates(availableDates, rollLogic, optionFrequency)
	print('getting ATM Strikes')
	strikes <- this$getHistoricalStrikesForRollDates(optionData)
	optionData <- cbind(optionData, strikes)
	print('downloading option prices')
	optionData <- this$getHistoricalPrices(optionData)
	straddles <- optionData[,'callPx'] + optionData[,'putPx']
	optionData <- cbind(optionData, straddles)
	print('getting Expiry Dates')
	optionData <- this$getExpiryDates(optionData)
	print('getting Swap Rates')
	rates <- this$loadSwapRates(start, end, source)
	print('getting Discount Rates')
	discountRate <- this$getDiscountRates(rates, optionData)
	optionData <- cbind(optionData, discountRate)
	print('getting Underlying Futures Prices')
	optionData <- this$underlyingFuturesPrices(optionData)
	print('getting Underlying Futures Changes')
	optionData <- this$underlyingFuturesChanges(optionData)
	optionData <- this$calcDeltas(optionData)
	optionData <- this$calculateReturns(optionData, base)
	optionData
})

method('getAvailableDates', 'FuturesOptionStraddle', function(this, start=NULL, end=NULL, ...){
	ticker <- toupper(this$underlying())
	dataSeries <- Symbol(paste(ticker, '.1C', sep = ''))$series()
	if(is.null(start)) start <- first(index(dataSeries))
	if(is.null(end)) end <- last(index(dataSeries))
	index(Range(start,end)$cut(dataSeries))
})

method('getRollDates', 'FuturesOptionStraddle', function(this, availableDates, rollLogic, optionFrequency, ...){
	rollDateZoo <- rollLogic(currentDate = availableDates[1],
							 nextDate = availableDates[2],
							 lastDate = NULL,
							 optionFrequency = optionFrequency,
							 currentStrike = NULL,
							 currentDelta = NULL)
	for(currIndex in 2:(length(availableDates)-1)){
		rollDateZoo <- rbind(rollDateZoo,rollLogic(currentDate = availableDates[currIndex],
											 	   nextDate = availableDates[currIndex+1],
											 	   lastDate = availableDates[currIndex-1],
											 	   optionFrequency = optionFrequency,
											 	   currentStrike = NULL,
											 	   currentDelta = NULL))
	}	
	currIndex <- length(availableDates)
	rollDateZoo <- rbind(rollDateZoo, rollLogic(currentDate = availableDates[currIndex],
												nextDate = as.POSIXct(as.Date(availableDates[currIndex])+1),
												lastDate = availableDates[currIndex - 1],
												optionFrequency = optionFrequency,
												currentStrike = NULL,
												currentDelta = NULL))
	index(rollDateZoo) <- as.POSIXct(paste(as.character(index(rollDateZoo)), " 15:00:00", sep = ""))
	rollDateZoo
})

method('getHistoricalStrikesForRollDates', 'FuturesOptionStraddle', function(this,rollDates,...){
	assert('optionMonth' %in% colnames(rollDates), 'optionMonth must be in the RollDates for FuturesOptionStraddle$getHistoricalStrikesForRollDates')
	strikeZoo <- zoo(NA, index(rollDates))
	contract <- Contract(this$underlying(), 'Comdty')
	
	strikeZoo[1] <- this$getNextStrike(rollDates, 1, contract)
	
	
	
	for(currIndex in 2:(length(index(rollDates))-1)){
		if(as.numeric(rollDates[currIndex,'optionMonth']) != as.numeric(rollDates[currIndex - 1, 'optionMonth'])) 
			strikeZoo[currIndex] <- this$getNextStrike(rollDates, currIndex, contract)
		#ROLLS MONTHLY NO MATTER WHAT!!!!
		if(as.POSIXlt(index(rollDates)[currIndex])$mon != as.POSIXlt(index(rollDates)[currIndex+1])$mon)
			strikeZoo[currIndex] <- this$getNextStrike(rollDates, currIndex, contract)	
	}	
	strikeZoo
})

method('getNextStrike', 'FuturesOptionStraddle', function(this, rollDates, currIndex, contract, ...){
	assert('optionMonth' %in% colnames(rollDates), 'optionMonth must be in the RollDates for FuturesOptionStraddle$getNextStrike')	
	assert('optionYear' %in% colnames(rollDates), 'optionYear must be in the RollDates for FuturesOptionStraddle$getNextStrike')
	assert('underlyingMonth' %in% colnames(rollDates), 'underlyingMonth must be in the RollDates for FuturesOptionStraddle$getNextStrike')
	assert('underlyingYear' %in% colnames(rollDates), 'underlyingYear must be in the RollDates for FuturesOptionStraddle$getNextStrike')
	
	futOpt <- FuturesOption(contract, 'call', as.numeric(rollDates[currIndex,'optionMonth']), as.numeric(rollDates[currIndex,'optionYear']), 
							as.numeric(rollDates[currIndex, 'underlyingMonth']), as.numeric(rollDates[currIndex,'underlyingYear']))
	futOpt$atTheMoneyStrike(index(rollDates)[currIndex])	
})

method('getHistoricalPrices', 'FuturesOptionStraddle', function(this, optionData, ...){
	assert('optionMonth' %in% colnames(optionData), 'optionMonth must be in the RollDates for FuturesOptionStraddle$getHistoricalPrices')
	assert('optionYear' %in% colnames(optionData), 'optionYear must be in the RollDates for FuturesOptionStraddle$getHistoricalPrices')
	assert('underlyingMonth' %in% colnames(optionData), 'underlyingMonth must be in the RollDates for FuturesOptionStraddle$getHistoricalPrices')
	assert('underlyingYear' %in% colnames(optionData), 'underlyingYear must be in the RollDates for FuturesOptionStraddle$getHistoricalPrices')
	assert('strikes' %in% colnames(optionData), 'strikes must be in the RollDates for FuturesOptionStraddle$getHistoricalPrices')
	
	callPx <- zoo(NA, index(optionData))
	putPx <- zoo(NA, index(optionData))
	for(currIndex in seq_along(index(optionData))){
		print(index(optionData[currIndex]))
		if(is.na(optionData[currIndex,'strikes']) && length(currentCall[index(optionData)[currIndex]]) > 0 && length(currentPut[index(optionData)[currIndex]]) > 0){
			callPx[currIndex] <- as.numeric(currentCall[index(optionData)[currIndex]])
			putPx[currIndex] <- as.numeric(currentPut[index(optionData)[currIndex]])
		}
		else {
			
			if(is.na(optionData[currIndex,'strikes'])){
				currDate <- index(optionData[currIndex])
				futOpt <- FuturesOption(this$underlyingContract(), 'call', as.numeric(optionData[currDate, 'optionMonth']), as.numeric(optionData[currDate, 'optionYear']),
							as.numeric(optionData[currDate, 'underlyingMonth']), as.numeric(optionData[currDate, 'underlyingYear']))
				optionData[currIndex,'strikes'] <- futOpt$atTheMoneyStrike(currDate)
			} 
				
			optionExpiryMonth <- as.numeric(optionData[currIndex, 'optionMonth'])
			optionExpiryYear <- as.numeric(optionData[currIndex, 'optionYear'])
			underlyingExpiryMonth <- as.numeric(optionData[currIndex, 'underlyingMonth'])
			underlyingExpiryYear <- as.numeric(optionData[currIndex, 'underlyingYear'])
			strike <- as.numeric(optionData[currIndex, 'strikes'])
				
			currentCallOption <- FuturesOption(this$underlyingContract(),'call', optionExpiryMonth, optionExpiryYear, underlyingExpiryMonth, underlyingExpiryYear)
			currentPutOption <- FuturesOption(this$underlyingContract(),'put', optionExpiryMonth, optionExpiryYear, underlyingExpiryMonth, underlyingExpiryYear)
			callOptionStrikes <- currentCallOption$availableStrikes(optionExpiryMonth, optionExpiryYear)
			putOptionStrikes <- currentPutOption$availableStrikes(optionExpiryMonth, optionExpiryYear)
			
			while(!(strike %in% putOptionStrikes)){
				strike <- strike - 0.5
				optionData[currIndex, 'strikes'] <- strike
			}
			
			while(!(strike %in% callOptionStrikes)){
				strike <- strike + 0.5
				optionData[currIndex, 'strikes'] <- strike
			}
			
			currentPut <- currentPutOption$historicalPrices(strike=strike)
			## Next 2 IF blocks are for if the at-the-money strikes don't have all of the data
			if(!(index(optionData[currIndex,]) %in% index(currentPut))){
				strike <- putOptionStrikes[match(strike, putOptionStrikes)-1]
				currentPut <- currentPutOption$historicalPrices(strike=strike)
				currentCall <- currentCallOption$historicalPrices(strike=strike)
				optionData[currIndex, 'strikes'] <- strike	
			}
			currentCall <- currentCallOption$historicalPrices(strike=strike)
			if(!(index(optionData[currIndex,]) %in% index(currentCall))) {
				strike <- callOptionStrikes[match(strike, callOptionStrikes) + 1]
				currentCall <- currentCallOption$historicalPrices(strike=strike)
				currentPut <- currentPutOption$historicalPrices(strike=strike)
				optionData[currIndex, 'strikes'] <- strike	
			}
		
			callPx[currIndex] <- as.numeric(currentCall[index(optionData[currIndex])])
			putPx[currIndex] <- as.numeric(currentPut[index(optionData[currIndex])])
		}
	}	
	cbind(optionData,callPx,putPx)
})

method('underlyingFuturesPrices', 'FuturesOptionStraddle', function(this, optionData, source = 'bloomberg', ...){
	assert('underlyingMonth' %in% colnames(optionData), 'underlyingMonth must be in the RollDates for FuturesOptionStraddle$underlyingFuturesPrices')
	assert('underlyingYear' %in% colnames(optionData), 'underlyingYear must be in the RollDates for FuturesOptionStraddle$underlyingFuturesPrices')
			
	underlyingFutures <- zoo(NA, index(optionData))
	underlyingFuturesData <- TimeSeriesDB()$retrieveTimeSeriesByAttributeList(this$underlyingFuturesAttributes(optionData[1,'underlyingMonth'], optionData[1,'underlyingYear']), data.source = source)[[1]]
	underlyingFutures[1] <- as.numeric(underlyingFuturesData[index(optionData)[1]])
	for(rowNum in 2:length(index(optionData))){
		currDate <- index(optionData)[rowNum]
		print(currDate)
		lastDate <- index(optionData)[rowNum - 1]
		if(as.numeric(optionData[currDate, 'underlyingMonth']) != as.numeric(optionData[lastDate, 'underlyingMonth']))
			underlyingFuturesData <- TimeSeriesDB()$retrieveTimeSeriesByAttributeList(this$underlyingFuturesAttributes(optionData[rowNum, 'underlyingMonth'], optionData[rowNum,'underlyingYear']), data.source = source)[[1]]
		underlyingFutures[rowNum] <- as.numeric(underlyingFuturesData[currDate])
	}
	cbind(optionData, underlyingFutures)		
})

method('underlyingFuturesChanges', 'FuturesOptionStraddle', function(this, optionData, source = 'bloomberg', ...){
	assert('underlyingMonth' %in% colnames(optionData), 'underlyingMonth must be in the RollDates for FuturesOptionStraddle$underlyingFuturesChanges')
	assert('underlyingYear' %in% colnames(optionData), 'underlyingYear must be in the RollDates for FuturesOptionStraddle$underlyingFuturesChanges')
		
	underlyingFuturesChanges <- zoo(NA, index(optionData))
	for(rowNum in 2:nrow(optionData)){
		print(index(optionData)[rowNum])
		if(as.numeric(optionData[rowNum,'underlyingMonth']) == as.numeric(optionData[rowNum-1, 'underlyingMonth'])){
			underlyingFuturesChanges[rowNum] <- as.numeric(optionData[rowNum,'underlyingFutures']) - as.numeric(optionData[rowNum-1, 'underlyingFutures'])
			next()
		}
		oldFutures <- TimeSeriesDB()$retrieveTimeSeriesByAttributeList(this$underlyingFuturesAttributes(optionData[rowNum-1,'underlyingMonth'], optionData[rowNum-1,'underlyingYear']), data.source = source)[[1]]
		underlyingFuturesChanges[rowNum] <- as.numeric(oldFutures[index(optionData)[rowNum]]) - as.numeric(optionData[rowNum-1, 'underlyingFutures'])		
	}
	cbind(optionData, underlyingFuturesChanges)
})

method('underlyingFuturesAttributes', 'FuturesOptionStraddle', function(this, futuresMonth, futuresYear, ...){
	list(instrument = 'futures',
		future_month = as.numeric(futuresMonth),
		future_year = as.numeric(futuresYear),
		quote_side = 'mid',
		quote_convention = 'price',
		contract = this$underlying())		
})

method('calcDeltas', 'FuturesOptionStraddle', function(this, optionData, ...){
	assert('optionMonth' %in% colnames(optionData), 'optionMonth must be in the RollDates for FuturesOptionStraddle$calcDeltas')
	assert('optionYear' %in% colnames(optionData), 'optionYear must be in the RollDates for FuturesOptionStraddle$calcDeltas')

	deltaCall <- zoo(NA, index(optionData))
	deltaPut <- zoo(NA, index(optionData))
	for(rowNum in 1:nrow(optionData)){
		print(index(optionData[rowNum,]))
		currDate <- index(optionData[rowNum,])
		optionMaturity <- as.character(as.Date(as.POSIXct(as.numeric(optionData[rowNum, 'expiryDates']))))
		strike <- as.numeric(na.locf(optionData[,'strikes'])[rowNum])
		disc <- as.numeric(optionData[rowNum, 'discountRate'])/100
		underlyingPrice <- as.numeric(optionData[rowNum, 'underlyingFutures'])
		callPx <- as.numeric(optionData[rowNum, 'callPx'])
		putPx <- as.numeric(optionData[rowNum, 'putPx'])
		deltaCall[rowNum] <- FuturesOptionCalculator$calcDelta(currDate, underlyingPrice, disc, optionMaturity, 'call', strike, 'calendar', 'price', price = callPx)
		deltaPut[rowNum] <- FuturesOptionCalculator$calcDelta(currDate, underlyingPrice, disc, optionMaturity, 'put', strike, 'calendar', 'price', price = putPx) 
	}	
	cbind(optionData, deltaCall, deltaPut)
})

method('readTable', 'FuturesOptionStraddle', function(this, fileName, ...){
	input <- read.table(fileName, header = TRUE, sep = ',', row.names = 1)
	zoo(input, order.by=row.names(input))	
})

method('writeTable', 'FuturesOptionStraddle', function(this, optionData, fileName, ...){
	write.table(optionData, fileName, row.names = index(optionData), sep = ',', col.names = TRUE, append = FALSE)	
})

method('calculateReturns', 'FuturesOptionStraddle', function(this, optionData, base, ...){
	assert('underlyingMonth' %in% colnames(optionData), 'underlyingMonth must be in the RollDates for FuturesOptionStraddle$calcReturns')
	assert('underlyingYear' %in% colnames(optionData), 'underlyingYear must be in the RollDates for FuturesOptionStraddle$calcReturns')
	assert('optionMonth' %in% colnames(optionData), 'optionMonth must be in the RollDates for FuturesOptionStraddle$calcReturns')
	assert('optionYear' %in% colnames(optionData), 'optionYear must be in the RollDates for FuturesOptionStraddle$calcReturns')
	assert('strikes' %in% colnames(optionData), 'strikes must be in the RollDates for FuturesOptionStraddle$calcReturns')
	assert('straddles' %in% colnames(optionData), 'straddles must be in the RollDates for FuturesOptionStraddle$calcReturns')
			
	index(optionData) <- as.POSIXct(index(optionData))
	dailyReturn <- zoo(NA, index(optionData))
	dailyReturn[1] <- base			
	for(indexNum in 2:length(index(optionData))){
		print(index(optionData)[indexNum])
		if(is.na(optionData[indexNum, 'strikes'])){
			dailyReturn[indexNum] <- (as.numeric(optionData[indexNum, 'straddles']) - as.numeric(optionData[indexNum - 1, 'straddles']) 
							- ((as.numeric(optionData[indexNum-1,'deltaCall']) + as.numeric(optionData[indexNum-1,'deltaPut'])) * as.numeric(optionData[indexNum,'underlyingFuturesChanges']))
							+ as.numeric(dailyReturn[indexNum - 1]))
			next()
		}
		
		optionExpiryMonth <- as.numeric(optionData[indexNum - 1, 'optionMonth'])
		optionExpiryYear <- as.numeric(optionData[indexNum - 1, 'optionYear'])
		underlyingExpiryMonth <- as.numeric(optionData[indexNum - 1, 'underlyingMonth'])
		underlyingExpiryYear <- as.numeric(optionData[indexNum - 1, 'underlyingYear'])
		strike <- as.numeric(na.locf(optionData[,'strikes'])[indexNum - 1])
		currentCallOption <- FuturesOption(this$underlyingContract(),'call', optionExpiryMonth, optionExpiryYear, underlyingExpiryMonth, underlyingExpiryYear)
		currentPutOption <- FuturesOption(this$underlyingContract(),'put', optionExpiryMonth, optionExpiryYear, underlyingExpiryMonth, underlyingExpiryYear)
		callPx <- currentCallOption$historicalPrices(strike=strike)
		putPx <- currentPutOption$historicalPrices(strike=strike)
		if(length(as.numeric(callPx[index(optionData)[indexNum]])) > 0 && length(as.numeric(putPx[index(optionData)[indexNum]])) > 0){
			currStraddle <- as.numeric(callPx[index(optionData)[indexNum]]) + as.numeric(putPx[index(optionData)[indexNum]])
			dailyReturn[indexNum] <- (currStraddle - as.numeric(optionData[indexNum - 1, 'straddles']) 
							- ((as.numeric(optionData[indexNum-1,'deltaCall']) + as.numeric(optionData[indexNum-1,'deltaPut'])) * as.numeric(optionData[indexNum,'underlyingFuturesChanges']))	
							+ as.numeric(dailyReturn[indexNum - 1]))
		}
		else dailyReturn[indexNum] <- dailyReturn[indexNum - 1]		
	}
	cbind(optionData, dailyReturn)	
})

method('loadSwapRates', 'FuturesOptionStraddle', function(this, start = NULL, end = NULL, source = 'internal', ...){
	getTermStructureForTimeSeries('libor_usd_rate_tenor', TermStructure$libor, startDate = start, endDate = end, source = source, lookFor = 'tenor')		
})

method('buildFincadTable', 'FuturesOptionStraddle', function(this, curveDate, rates, tenors = TermStructure$libor, ccy = 'usd', holidayCenter = 'nyb', holidaySource = 'financialcalendar', ...){
	scb <- SwapCurveBuilder(ccy = ccy, holidayCenter = holidayCenter, holidaySource = holidaySource)
	fincadTable <- scb$buildFincadTable(curveDate, rates, tenors, TRUE)
	daysFromEffDate <- as.numeric(as.Date(fincadTable[,'term_date']) - as.Date(fincadTable[,'eff_date']))	
	cbind(fincadTable, daysFromEffDate)
})

method('getInterpolatedDiscountRate', 'FuturesOptionStraddle', function(this, fincadTable, expiryDate, ...){
	daysToExpiry <- as.numeric(as.Date(expiryDate) - as.Date(fincadTable[1,'eff_date']))
	qf.interpolate(daysToExpiry, fincadTable[,'daysFromEffDate'], fincadTable[,'rate'])
})

method('getExpiryDates', 'FuturesOptionStraddle', function(this, optionData, ...){
	expiryDates <- zoo(NULL, index(optionData))
	for(rowNum in 1:nrow(optionData)){
		futOption <- FuturesOption(this$underlyingContract(), 'call', as.numeric(optionData[rowNum,'optionMonth']), as.numeric(optionData[rowNum,'optionYear']),
								   as.numeric(optionData[rowNum,'underlyingMonth']), as.numeric(optionData[rowNum, 'underlyingYear']))
		expiryDates[rowNum] <- as.POSIXct(futOption$expiryDates())
	}
	cbind(optionData, expiryDates)			
})

method('getDiscountRates', 'FuturesOptionStraddle', function(this, rateTable, optionData, ...){
	discountRates <- zoo(NULL, index(optionData))
	for(rowNum in 1:nrow(optionData)){
		currDate <- index(optionData[rowNum,])
		expirationDate <- as.POSIXct(as.numeric(optionData[rowNum, 'expiryDates']))
		rates <- as.numeric(rateTable[currDate,])
		fincadTable <- this$buildFincadTable(currDate, rates)
		discountRates[rowNum] <- this$getInterpolatedDiscountRate(fincadTable, expirationDate)
	}	
	na.locf(discountRates)
})
