# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

constructor("STOValidationTable", function(sto = NULL){
		this <- extend(RObject(), "STOValidationTable", .sto = sto)
		constructorNeeds(this, sto = 'STO')
		if(inStaticConstructor(this)) return(this)
		return(this)
})

method('createFiles', 'STOValidationTable', function(this, msiv, filePath, metricList = NULL, thresholdList = this$defaultThresholds(), cube = this$.sto$metrics(), csvFilePath = NULL, filteredParams = NULL, ...){
		needs(filePath = 'character', csvFilePath = 'character?', filteredParams = 'RunFilter?')
		
		print(msiv$as.character())	
		dataTable <- this$createTable(msiv, metricList, thresholdList, cube, csvFilePath, filteredParams)
		hwu <- HWriterUtils(filePath, pdf = FALSE)
		
		hwrite(paste('MSIV =', msiv$as.character()), hwu$connection(), br = TRUE)
		hwrite(paste('STO Directory =', this$.sto$dirname()), hwu$connection(), br = TRUE)
		hwrite(hwu$dataTable(STOValidationTable(this$.sto)$createTotalRunsTable(filteredParams), colNames = FALSE), hwu$connection())
		hwrite(hwu$dataTable(dataTable, formatNumbers = FALSE), hwu$connection())
		
		hwu$closeConnection()					
})	


method('createTable', 'STOValidationTable', function(this, msiv, metricList = NULL, thresholdList = this$defaultThresholds(), cube = this$.sto$metrics(), csvFilePath = NULL, filteredParams = NULL, ...){
		needs(msiv = 'MSIV', metricList = 'list?', thresholdList = 'list', cube = 'MetricCube', csvFilePath = 'character?', filteredParams = 'RunFilter?')
				
		### thresholdList must be a named list -- names are the metrics as characters
		
		params <- this$.sto$parameters()
		totalMetricList <- cube$availableMetrics()
		if(is.null(filteredParams)) chosenRuns <- params$runs()
		else chosenRuns <-  filteredParams$runs()
			
		if(is.null(metricList)) metricList <- totalMetricList
		
		metricsData <- data.frame(
				sapply(totalMetricList, function(x) cube$values(x, msiv), simplify = FALSE),
				row.names = this$.sto$runNumbers())
		names(metricsData) <- c(sapply(totalMetricList, function(x) x$as.character()))
		rowFind <- match(row.names(metricsData), chosenRuns)
		rowFind <- rowFind[!is.na(rowFind)]
		Data <- cbind(metricsData[rowFind,], params$.data[rowFind,-1])
		
		if(!is.null(csvFilePath)) write.csv(Data, csvFilePath) 
			
		totalRuns <- NROW(Data)
		
		metricList <- sapply(metricList, function(x) x$as.character())
		
		maxList <- sapply(metricList, function(x) max(Data[,x], na.rm=TRUE))
		maxList <- sapply(maxList, this$formatNums)
		minList <- sapply(metricList, function(x) min(Data[,x], na.rm = TRUE))
		minList <- sapply(minList, this$formatNums)
		meanList <- sapply(metricList, function(x) mean(Data[,x], na.rm = TRUE))
		meanList <- sapply(meanList, this$formatNums)
		sdList <- sapply(metricList, function(x) sd(Data[,x], na.rm = TRUE))
		sdList <- sapply(sdList, this$formatNums)
		
		output <- data.frame(cbind(maxList, minList, meanList, sdList))
		
		thresholdForTable <- sapply(metricList, function(x) thresholdList[[match(x, names(thresholdList))]])
		output <- cbind(output, as.character(thresholdForTable))
				
		passedList <- sapply(metricList, function(x) {if(is.null(thresholdList[x][[1]])) return(NA); sum(eval(parse(text = paste('Data[,"', x, '"]', thresholdList[x], sep=''))))}) 
		output <- data.frame(cbind(output, passedList))
		output <- data.frame(cbind(output, round(passedList / totalRuns * 100,1)))
		row.names(output) <- metricList
		
		colnames(output) <- c('Max','Min','Average','StDev','Threshold', '# of Runs', '% of Runs')	
		
		thresholdRowNums <- !sapply(match(row.names(output), names(thresholdList)), is.na)
		thresholdRows <- output[thresholdRowNums,]
		output <- rbind(thresholdRows, output[!thresholdRowNums,])	
		output
})

method('createTotalRunsTable', 'STOValidationTable', function(this, filteredParams = NULL, ...){
		needs(filteredParams = 'RunFilter?')
		if(is.null(filteredParams))	numRuns <- this$.sto$parameters()$rowCount()
		else numRuns <- length(filteredParams$runs())
		output <- data.frame(numRuns)
		row.names(output) <- 'Total Runs'
		output		
})

method('formatNums', 'STOValidationTable', function(this, x, threshold = 1000, ...){
	ifelse(abs(x) > threshold, format(x, dig = 1, big.mark = ',', scientific = FALSE), format(x, dig = 3 , scientific = FALSE))
})

method('defaultThresholds', 'STOValidationTable', function(this,...){
	list(QAnnualizedNetProfit = '>0',
		 QTotalTrades = '>50',
		 QKRatio = '>0.3',
		 QCalmarRatio = '>0.3',
		 QAverageTrade = '>10000',
		 QWinningTradesPct = '>0.50'
		 )	
})