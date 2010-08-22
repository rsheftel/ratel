# DailyPacketGenerator.r
#
# This program creates the daily data packet, and places it as a PDF in the outputFile location.
# Currently supported: IRS, Credit, Mtge, Swaption Vol
#
# 
# Author: dhorowitz
# Created: 8/5/08
# Last updated: 
###############################################################################
constructor("DailyPacketGenerator", function(dateList = getRefLastNextBusinessDates(holiday = 'financialcalendar', financialCenter = 'nyb'),
	daysAgoList = c(1,5,20), zScoreDays = c(30,90), outputFile = NULL){
	this <- extend(RObject(), "DailyPacketGenerator", .dateList = dateList, .daysAgoList = daysAgoList, .outputFile = outputFile)
	if(inStaticConstructor(this)) return (this)
	constructorNeeds(this, dateList = "list", daysAgoList = 'numeric', outputFile = 'character?')
		
	#set up initial variables
	this$.dateClose <- as.POSIXct(dateList[[2]])
	this$.datePrevious <- as.POSIXct(dateList[[3]])
	this$.pastDates <- lapply(daysAgoList, function(x) businessDaysAgo(days = x, date = this$.dateClose))
	this$.zScoreDates <- lapply(zScoreDays, function(x) businessDaysAgo(days = x, date = this$.dateClose))
	this$.rowNames <- c(as.character(this$.dateClose), as.character(this$.datePrevious), "",
					  lapply(daysAgoList, function(x) squish(x,"d")),
					  lapply(zScoreDays, function(x) squish(x, 'd Zscore')),
					  lapply(zScoreDays, function(x) squish(x, 'd SD')),
					  lapply(zScoreDays, function(x) squish(x, 'd %ile'))) 
		 
	return(this)
})						

method("run", "DailyPacketGenerator", function(this, ...){

	printList <- list()

	printList$'Interest Rate Swaps' <- this$buildIRSTable()
	printList$'Credit' <- this$buildCreditTable()
	printList$'Agency Mortgages' <- this$buildMortgageTable()
	#printList$'Swaption Vol (in daily bps)' <- this$buildVolatilityTable()
	#currencyTable <- this$buildCurrencyTable()
	
	printList	
})

method("buildIRSTable", "DailyPacketGenerator", function(this, ...){
	tickerNameList <- c('18m', '2y', '3y', '4y', '5y', '6y', '7y', '8y', '9y', '10y', '12y', '15y', '20y', '25y', '30y', '40y')	
	tickerList <- lapply(tickerNameList, function(x) paste('irs','usd', 'rate', x, 'mid', sep = "_"))
	sourceList <- rep('internal', length(tickerList))

	irsTable <- this$.buildGenericTable(tickerList, sourceList)
	
	irsTable[1:2,] <- round(irsTable[1:2,], 2)
	
	lastPastDateRow <- 3 + length(this$.pastDates) - 1
	irsTable[3:lastPastDateRow,] <- round(irsTable[3:lastPastDateRow,]*100,0)
	
	numZScoreDays <- length(this$.zScoreDates)
	startSDRow <- lastPastDateRow + numZScoreDays + 1
	irsTable[startSDRow:(startSDRow + numZScoreDays - 1),] <- round(irsTable[startSDRow:(startSDRow + numZScoreDays - 1),]*100,0) 
	
	irsTable <- this$.formatTable(irsTable, tickerNameList)
	irsTable	
		
})

method("buildCreditTable", "DailyPacketGenerator", function(this, ...){
	tickerList <- list('cdx-na-ig_market_spread_5y_otr',
					'cdx-na-ig-hvol_market_spread_5y_otr',
					'itraxx-eur_market_spread_5y_otr')
	tickerNameList <- c('IG','HVol','iTraxx Main')
	sourceList <- rep('internal', length(tickerList))

	creditTable <- this$.buildGenericTable(tickerList, sourceList)		
		
	lastPastDateRow <- 3 + length(this$.pastDates) - 1
	creditTable[1:lastPastDateRow,] <- round(creditTable[1:lastPastDateRow,] * 10000,1) # in bps
	
	numZScoreDays <- length(this$.zScoreDates)
	startSDRow <- lastPastDateRow + numZScoreDays + 1
	creditTable[startSDRow:(startSDRow+numZScoreDays-1),] <- round(creditTable[startSDRow:(startSDRow+numZScoreDays-1),] * 10000, 1)
	
	creditTable <- this$.formatTable(creditTable, tickerNameList)
	creditTable
})

method("buildMortgageTable", "DailyPacketGenerator", function(this, ...){
	tickerList <- list('fncl_cc_30d_yield', 
				  	list('fncl_cc_30d_yield', 'irs_usd_rate_10y_mid'),
					list('fncl_cc_30d_yield', 'bond_government_usd_10y_1c_yield'),
					'fncl_cc_30d_libor_oas')
					
				
	tickerNameList <- c('FN CC 30y yld', 'FN CC 30y yld vs 10y IRS', 'FN CC 30y yld vs 10y Tsy', 'FN CC 30y OAS (JP08)')
	
	sourceList <- c(rep('internal',3), 'model_jpmorgan_bondStudio2008')
		
	mortgageTable <- this$.buildGenericTable(tickerList, sourceList)
	
	mortgageTable <- round(mortgageTable, 2)
		
	mortgageTable <- this$.formatTable(mortgageTable, tickerNameList)
	mortgageTable
})

method("buildCurrencyTable", "DailyPacketGenerator", function(this, ...){
		
})

method("buildVolatilityTable", "DailyPacketGenerator", function(this, ...){
	tickerList <- list('swaption_usd_1m10y_atm_payer_vol_bp_daily_mid',
					'swaption_usd_3m10y_atm_payer_vol_bp_daily_mid',
					'swaption_usd_6m10y_atm_payer_vol_bp_daily_mid',
					'swaption_usd_1y10y_atm_payer_vol_bp_daily_mid',
					'swaption_usd_5y5y_atm_payer_vol_bp_daily_mid',
					'swaption_usd_10y10y_atm_payer_vol_bp_daily_mid')
				#	'swaption_usd_3y7y_atm_payer_vol_bp_daily_mid')
		
	tickerNameList <- c('1m x10', '3m x10', '6m x10', '1x10', '5x5', '10x10')
			#, '3x7')
	sourceList <- c(rep('jpmorgan',length(tickerList)))
	
	volTable <- this$.buildGenericTable(tickerList, sourceList)
		
	volTable <- round(volTable, 2)
	
	volTable <- this$.formatTable(volTable, tickerNameList)
	volTable	
})

method(".buildGenericTable", "DailyPacketGenerator", function(this, tickerList, sourceList = list('internal'), ...){
	tickerOutput <- this$runTickerInfo(tickerList, sourceList)
	outputTable <- tickerOutput$dataTable
	outputTable <- this$addChangesRows(outputTable, tickerOutput$combinedTickerInfo)
	outputTable <- this$addZScoreRows(outputTable, tickerOutput$combinedTickerInfo)
	outputTable <- this$addSDRows(outputTable, tickerOutput$combinedTickerInfo)
	outputTable <- this$addPercentileRows(outputTable, tickerOutput$combinedTickerInfo)
	outputTable	
})

method("runTickerInfo", "DailyPacketGenerator", function(this, tickerList, sourceList = list('internal'), ...){
	dataTable <- NULL
	combinedTickerInfo <- NULL
	output <- list()
	for(tickerNum in seq_along(tickerList)){
		if(class(tickerList[[tickerNum]]) == 'list') currentTickerInfo <- this$.runSpread(tickerList[[tickerNum]], sourceList[[tickerNum]]) 
		else currentTickerInfo <- strip.times.zoo(TimeSeriesDB()$retrieveTimeSeriesByName(tickerList[[tickerNum]], sourceList[[tickerNum]])[[1]])
		currentTicker <- rbind(data.frame(currentTickerInfo[this$.dateClose,]), data.frame(currentTickerInfo[this$.datePrevious,]))
		dataTable <- cbind(dataTable, currentTicker[,1])
		combinedTickerInfo <- cbind(combinedTickerInfo, currentTickerInfo[,1])
	}
	output$dataTable <- data.frame(dataTable)
	index(combinedTickerInfo) <- as.POSIXct(index(combinedTickerInfo))
	output$combinedTickerInfo <- combinedTickerInfo
	output
})

method(".runSpread", "DailyPacketGenerator", function(this, tickers, sourceName = 'internal', ...){
	needs(tickers = 'list')
	dataList <- lapply(tickers, function(x) TimeSeriesDB()$retrieveTimeSeriesByName(x, sourceName)[[1]])
	dataCombined <- merge(dataList[[1]], dataList[[2]], all = FALSE)
	currentTickerInfo <- strip.times.zoo(dataCombined[,1] - dataCombined[,2])
	currentTickerInfo
})

method("addBlankRow", "DailyPacketGenerator", function(this, inputTable, afterRow, ...){
	return(rbind(inputTable[1:afterRow,], "", inputTable[(afterRow + 1):NROW(inputTable),]))
})


method("addChangesRows", "DailyPacketGenerator", function(this, inputTable, historicalData, ...){
	closingLevels <- as.numeric(historicalData[this$.dateClose,])
	
	for(pastDate in this$.pastDates)
		inputTable <- rbind(inputTable, closingLevels - as.numeric(historicalData[pastDate,]))
	
	inputTable			
})

method("addZScoreRows", "DailyPacketGenerator", function(this, inputTable, historicalData, ...){
	closeIndex <- match(this$.dateClose, index(historicalData))
	ZScoreIndexes <- lapply(this$.zScoreDates, function(x) match(x, index(historicalData)))
	historicalData <- data.frame(historicalData)
	
	for(zScoreIndex in ZScoreIndexes){
		newData <- round((historicalData[closeIndex,] - mean(historicalData[zScoreIndex:closeIndex,], na.rm = TRUE)) / sd(historicalData[zScoreIndex:closeIndex,], na.rm=TRUE),2)
		colnames(newData) <- colnames(inputTable)
		inputTable <- rbind(inputTable, newData)
	}
		
	inputTable	
})

method("addSDRows", "DailyPacketGenerator", function(this, inputTable, historicalData, ...){
	closeIndex <- match(this$.dateClose, index(historicalData))
	sdIndexes <- lapply(this$.zScoreDates, function(x) match(x, index(historicalData)))
	historicalData <- data.frame(historicalData)
	
	for(sdIndex in sdIndexes){
		newData <- sd(historicalData[sdIndex:closeIndex,], na.rm=TRUE)
		inputTable <- rbind(inputTable, newData)
	}	
	
	inputTable
})

method("addPercentileRows", "DailyPacketGenerator", function(this, inputTable, historicalData, ...){
	closeIndex <- match(this$.dateClose, index(historicalData))
	pctIndexes <- lapply(this$.zScoreDates, function(x) match(x, index(historicalData)))
	historicalData <- data.frame(historicalData)
	
	for(pctIndex in pctIndexes){
		newData <- NULL
		for(colNum in 1:ncol(historicalData)){
			newData <- c(newData, ecdf(historicalData[pctIndex:closeIndex,colNum])(historicalData[closeIndex,colNum]))
		}
		inputTable <- rbind(inputTable, round(newData * 100,0))
	}	
	
	inputTable
})

method(".formatTable", "DailyPacketGenerator", function(this, inputTable, tickerNameList, ...){
	inputTable <- this$addBlankRow(inputTable, afterRow = 2)
	row.names(inputTable) <- this$.rowNames
	names(inputTable) <- tickerNameList
	inputTable	
})

method("createHTML", "DailyPacketGenerator", function(this, tableList, outputDirectory, outputFileName, ...){
	needs(outputDirectory = 'character')
	needs(outputFileName = 'character')
	
	p <- openPage(outputFileName, dirname = outputDirectory)

	hwrite(paste("Daily Data Packet -", as.character(this$.dateClose), 'Close'), page = p, center = TRUE, br = TRUE, 
		   style = 'font-weight: bold; font-size: 30pt; text-decoration: underline')
	hwrite('', page = p, br = TRUE)
	
	for(tableNum in seq_along(tableList)){
		hwrite(names(tableList[tableNum]), page = p, center = TRUE, br = TRUE, style = 'font-weight:bold')
		hwrite(tableList[[tableNum]], page = p, center = TRUE, br = TRUE, border = 2, cellpadding = 10, row.bgcolor = list('lightblue'))
		hwrite('', page = p, br = TRUE)
	}		
	
	closePage(p)
})
