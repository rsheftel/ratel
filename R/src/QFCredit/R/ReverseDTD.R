constructor('ReverseDTD', function(startDate = NULL, endDate = NULL, tickerList = SystemDTD$tickerUniverse(), tenor = '5y',ccy = 'usd', tier = 'snrfor', ...){
		this <- extend(RObject(), 'ReverseDTD')
		if(inStaticConstructor(this)) return(this)
		constructorNeeds(this, tickerList = 'character', tenor = 'character', ccy = 'character', tier = 'character', startDate = 'character|POSIXct?', endDate = 'character|POSIXct?')
		this$.fvs <- SingleNameCDS$multipleFairValuesSeries(tickerList, startDate = startDate, endDate = endDate)
		this$.dtds <- SingleNameCDS$multipleDTDSeries(tickerList, startDate = startDate, endDate = endDate)
		this$.oas <- SingleNameCDS$multipleGenericSeries(tickerList, 'spread', startDate = startDate, endDate = endDate) * 10000
		this$.stocks <- SingleNameCDS$multipleClosingPrices(tickerList, startDate = startDate, endDate = endDate)
		this$.shares <- SingleNameCDS$multipleSharesOutstandingSeries(tickerList, startDate = startDate, endDate = endDate)
		this$.sigmaA <- SingleNameCDS$multipleAssetVolSeries(tickerList, startDate = startDate, endDate = endDate)
		this$.assets <- SingleNameCDS$multipleAssetValues(tickerList, startDate = startDate, endDate = endDate)
		this$.testColumnNamesAndData()
		this$.DTDPrime <- data.frame()
		this$.normMktCapZoo <- data.frame()
		this$.resids <- data.frame()
		this$.richCheaps <- data.frame()
		this
})

method('multipleDayRichCheaps', 'ReverseDTD', function(this, maximumRichCheap = 100, ...){
		if(!is.empty(this$.richCheaps)) return(this$.richCheaps)
		this$.DTDPrime <- data.frame()
		this$.normMktCapZoo <- data.frame()
		this$.resids <- data.frame()
		this$.richCheaps <- data.frame()
		for(thisDate in index(this$.fvs)){
			print('running DTDPrime')
			this$.DTDPrime <- rbind(this$.DTDPrime, this$singleDayDTDPrime(thisDate))
			this$.normMktCapZoo <- rbind(this$.normMktCapZoo, this$singleDayNormalizedMarketCaps(thisDate))
		}		
		this$.DTDPrime <- this$.cleanUpZoo(this$.DTDPrime)
		this$.normMktCapZoo <- this$.cleanUpZoo(this$.normMktCapZoo)

		for(thisDate in index(this$.fvs)) {
			print('running resids')
			this$.resids <- rbind(this$.resids, this$singleDayResids(thisDate))
		}
		this$.resids <- this$.cleanUpZoo(this$.resids)
		this$.richCheaps <- data.frame()
		this$.tempRichCheaps <- zoo(matrix(exp(as.numeric(this$.resids)), nrow = NROW(this$.resids), ncol = NCOL(this$.resids)), order.by = index(this$.resids))
		colnames(this$.tempRichCheaps) <- colnames(this$.resids)
		for(thisDate in index(this$.tempRichCheaps)){
			thisDate <- as.POSIXct(thisDate)
			print(thisDate)
			this$.richCheaps <- rbind(this$.richCheaps,this$.stocks[thisDate,] - this$.tempRichCheaps[thisDate,] * this$.sigmaA[thisDate,] * this$.assets[thisDate,] / this$.shares[thisDate,])
		}	
		this$.richCheaps[this$.richCheaps > maximumRichCheap] <- NA
		this$.richCheaps[this$.richCheaps < -maximumRichCheap] <- NA
		index(this$.richCheaps) <- as.POSIXct(index(this$.richCheaps))
		this$.richCheaps		
})

method('singleDayResids', 'ReverseDTD', function(this, thisDate, ...){
		thisDate <- as.POSIXct(thisDate)
		print(thisDate)
		currentData <- data.frame(DTDPrime = as.numeric(this$.DTDPrime[thisDate,]), normMktCap = as.numeric(this$.normMktCapZoo[thisDate,]))
		currentLm <- lm(normMktCap ~ DTDPrime, currentData, na.action = na.exclude)
		currentResid <- resid(currentLm)
		zoo(t(currentResid), order.by = thisDate)		
})

method('singleDayDTDPrime', 'ReverseDTD', function(this, thisDate, ...){
		thisDate <- as.POSIXct(thisDate)
		print(thisDate)
		sortedFVs <- as.numeric(this$.fvs[thisDate,])[this$fairValueOrder(thisDate)]
		sortedDTDs <- as.numeric(this$.dtds[thisDate,])[this$fairValueOrder(thisDate)]
		DTDPrimes <- sapply(as.numeric(this$.oas[thisDate,]), function(x) qf.interpolate(x, sortedFVs, sortedDTDs))
		zoo(t(DTDPrimes), order.by = thisDate)		
})

method('singleDayNormalizedMarketCaps', 'ReverseDTD', function(this, thisDate, ...){
		thisDate <- as.POSIXct(thisDate)
		failIf(!(as.POSIXct(thisDate) %in% index(this$.stocks)), 'thisDate not found in availableDates in ReverseDTD$singleDayNormalizedMarketCaps()')		
		mktCap <- as.numeric(this$.stocks[thisDate,]) * as.numeric(this$.shares[thisDate,])
		normMktCap <- mktCap / (as.numeric(this$.sigmaA[thisDate,]) * as.numeric(this$.assets[thisDate,]))
		zoo(t(normMktCap), order.by = thisDate)
})

method('rsqs', 'ReverseDTD', function(this, ...){
		output <- c()
		for(rowNum in 1:NROW(this$.normMktCapZoo)) 
			output <- c(output, cor(as.numeric(this$.DTDPrime[rowNum,]), as.numeric(this$.normMktCapZoo[rowNum,]), use = 'complete.obs')^2)
		zoo(output, order.by = index(this$.normMktCapZoo))			
})

method('fairValueOrder', 'ReverseDTD', function(this, thisDate, ...){
		thisDate <- as.POSIXct(thisDate)
	 	order(as.numeric(this$.fvs[thisDate,]))		
})

method('plot', 'ReverseDTD', function(this, thisDate, pdfLocation = NULL, ...){
		thisDate <- as.POSIXct(thisDate)
		if(!is.null(pdfLocation)) pdf(pdfLocation, paper = 'special', height = 10, width = 10)
		plot(as.numeric(this$.DTDPrime[thisDate,]), as.numeric(this$.normMktCapZoo[thisDate,]), xlab = 'DTD Prime', ylab = 'Normalized Mkt Cap', main = thisDate)
		if(!is.null(pdfLocation)) dev.off()
})

method('.testColumnNamesAndData', 'ReverseDTD', function(this, ...){
		failIf(any(colnames(this$.fvs) != colnames(this$.dtds)), 'Column Names do not match in ReverseDTD')
		failIf(any(colnames(this$.dtds) != colnames(this$.oas)), 'Column Names do not match in ReverseDTD')	
		failIf(any(colnames(this$.dtds) != colnames(this$.stocks)), 'Column Names do not match in ReverseDTD')
		failIf(any(colnames(this$.dtds) != colnames(this$.shares)), 'Column Names do not match in ReverseDTD')
		failIf(any(colnames(this$.dtds) != colnames(this$.sigmaA)), 'Column Names do not match in ReverseDTD')
		failIf(any(colnames(this$.dtds) != colnames(this$.assets)), 'Column Names do not match in ReverseDTD')
		failIf(is.null(this$.fvs), 'no data found for FVs in ReverseDTD')
		failIf(is.null(this$.dtds), 'no data found for DTDs in ReverseDTD')
		failIf(is.null(this$.oas), 'no data found for OASs in ReverseDTD')
		failIf(is.null(this$.stocks), 'no data found for stocks in ReverseDTD')
		failIf(is.null(this$.shares), 'no data found for shares outstanding in ReverseDTD')
		failIf(is.null(this$.sigmaA), 'no data found for sigmaAs in ReverseDTD')
		failIf(is.null(this$.assets), 'no data found for asset values in ReverseDTD')
})

method('.cleanUpZoo', 'ReverseDTD', function(this, baseZoo, ...){
		needs(baseZoo = 'zoo')
		index(baseZoo) <- index(this$.fvs)
		colnames(baseZoo) <- colnames(this$.fvs)
		baseZoo		
})




