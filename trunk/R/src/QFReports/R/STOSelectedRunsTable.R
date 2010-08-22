constructor("STOSelectedRunsTable", function(){
		this <- extend(RObject(), "STOSelectedRunsTable")
		constructorNeeds(this)
		if(inStaticConstructor(this)) return(this)
		return(this)
})

method('createFiles', 'STOSelectedRunsTable', function(static, sto, msiv, textFilter = NULL, runNums = NULL, filePath, 
													   metricList = NULL, cube = NULL, ...){
		needs(sto = 'STO', msiv = 'MSIV', textFilter = 'character?', runNums = 'integer|numeric?',
			 filePath = 'character', metricList = 'list?', cube = 'MetricCube?')
		 
		failIf(is.null(textFilter) && is.null(runNums), 'Either a textFilter or runNums must be passed to STOSelectedRunsTable$createFiles')
		
		print(msiv$as.character())
		if(is.null(runNums)) runNums <- STOSelectedRunsTable$chooseRuns(sto, textFilter)$runs
			
		headerTable <- STOSelectedRunsTable$createHeaderTable(sto, runNums)
		dataTable <- STOSelectedRunsTable$createTable(sto, msiv, runNums, metricList = metricList, cube = cube)
		
		hwu <- HWriterUtils(filePath, pdf = FALSE)
		
		hwrite(paste('MSIV =', msiv$as.character()), hwu$connection(), br = TRUE)
		hwrite('SPECIFIED RUN REPORT', hwu$connection(), br = TRUE)
		hwrite(paste('STO Directory =', sto$dirname()), hwu$connection(), br = TRUE)
		hwrite(hwu$dataTable(headerTable, rowNames = FALSE), hwu$connection())
		hwrite(hwu$dataTable(dataTable, formatNumbers = FALSE), hwu$connection())
		
		hwu$closeConnection()					
})


method('createTable', 'STOSelectedRunsTable', function(static, sto, msiv, runNums, metricList = NULL, cube = NULL, ...){
		needs(sto = 'STO', msiv = 'MSIV', runNums = 'integer|numeric', metricList = 'list?', cube = 'MetricCube?')
		if(is.null(cube)) cube <- sto$metrics()
		if(is.null(metricList)) metricList <- cube$availableMetrics()
		
		output <- sapply(runNums, function(x) cube$oneValue(metricList[[1]], msiv, as.numeric(x)))
		output <- c(min(output, na.rm = TRUE), max(output, na.rm = TRUE), mean(output, na.rm = TRUE), sd(output, na.rm = TRUE), output)
		output <- STOSelectedRunsTable$formatRow(output)
		names(output) <- c('Min','Max','Ave','SD',paste('Run#',runNums))
		
		if(length(metricList) == 1) {row.names(output) = metricList[[1]]$as.character(); return(output)}
		
		for(met in 2:length(metricList)){
			newData <- sapply(runNums, function(x) cube$oneValue(metricList[[met]], msiv, as.numeric(x)))
			newData <- c(min(newData, na.rm = TRUE), max(newData, na.rm = TRUE), mean(newData, na.rm = TRUE), sd(newData, na.rm = TRUE), newData)
			newData <- STOSelectedRunsTable$formatRow(newData)
			names(newData) <- c('Min','Max','Ave','SD',paste('Run#',runNums))
			output <- rbind(output, newData)
		}
		row.names(output) <- sapply(metricList, function(x) x$as.character())
		output
		
	})

method('createHeaderTable', 'STOSelectedRunsTable', function(static, sto, runNums, ...){
		needs(sto = 'STO', runNums = 'integer|numeric?')
		
		runNums <- as.numeric(runNums)
		output <- sto$parameters()$data()[runNums,]
})


method('chooseRuns', 'STOSelectedRunsTable', function(static, sto, textFilter, ...){
		needs(sto = 'STO', textFilter = 'character')
		params <- sto$parameters()
		
		filter = eval(parse(text = squish("params$filter(", textFilter, ")")))
		
		list(filter = filter, runs = filter$runs())		
})

method('chainFilters', 'STOSelectedRunsTable', function(static, filterList, ...){
		needs(filterList = 'list|character')
		if(is.character(filterList)) filterList <- as.list(filterList)
		
		output <- filterList[[1]]
		if(length(filterList) == 1) return(output)
		
		for(pos in 2:length(filterList)) output <- squish(output, " & ", filterList[[pos]])
		output 		
})

method('formatNums', 'STOSelectedRunsTable', function(static, x, threshold = 1000, ...){
		ifelse(abs(x) > threshold, format(x, dig = 1, big.mark = ',', scientific = FALSE), format(x, dig = 3 , scientific = FALSE))
})

method('formatRow', 'STOSelectedRunsTable', function(static, x, threshold = 1000, ...){
		data.frame(t(STOSelectedRunsTable$formatNums(x)))	
})

#####################################################################################################
####   Scatter plots of Metric values against parameter values of selected runs
####   Please NOTE: These functions output a pdf, so do not currently have a test available
#####################################################################################################

method('createGraphsMultipleMsivs', 'STOSelectedRunsTable', function(static, sto, runNums, fileHeader, msivList = NULL, metricList = NULL, cube = NULL, ...){
		needs(sto = 'STO', msivList = 'list?', runNums = 'numeric', fileHeader = 'character', metricList = 'list?', cube = 'MetricCube?')
		if(is.null(msivList)) msivList <- sto$msivs()
		for(msiv in msivList)
			STOSelectedRunsTable$createGraphsOneMsiv(sto, msiv, runNums, squish(fileHeader, msiv$as.character(), '.pdf'), metricList, cube)	
})

method('createGraphsOneMsiv', 'STOSelectedRunsTable', function(static, sto, msiv, runNums, fileName, metricList = NULL, cube = NULL, ...){
		needs(sto = 'STO', msiv = 'MSIV', runNums = 'numeric', fileName = 'character', metricList = 'list?', cube = 'MetricCube?')
		if(is.null(cube)) cube <- sto$metrics()
		if(is.null(metricList)) metricList = cube$availableMetrics()
		
		pdf(file = fileName, paper = 'special', width = 10, height = 10)
		parameterValues <- sto$parameters()$data()[runNums,]
		parameterNames <- colnames(parameterValues)[2:NCOL(parameterValues)]
		for(param in parameterNames){
			for(met in metricList){
				y <- sapply(runNums, function(x) cube$oneValue(met, msiv, as.numeric(x)))
				plot(parameterValues[,param], y, main = squish(msiv$as.character(), ' - ', met$as.character(), ' vs ', param), xlab = param, ylab = '')
			}
		}
		dev.off()
})
