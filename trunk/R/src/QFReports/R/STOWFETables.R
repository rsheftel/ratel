constructor("STOWFETables", function(){
		this <- extend(RObject(), "STOWFETables")
		constructorNeeds(this)
		if(inStaticConstructor(this)) return(this)
		return(this)
})

method('createValidationTables', 'STOWFETables', function(static, stoIn, stoOut, msivIn, msivOut = NULL, cubeIn = NULL, cubeOut = NULL, metricList = NULL, 
														  thresholdList = NULL, fileLocation = NULL, ...){
	needs(stoIn = 'STO', stoOut = 'STO', msivIn = 'MSIV', msivOut = 'MSIV?', cubeIn = 'MetricCube?',
		  cubeOut = 'MetricCube?', metricList = 'list?', thresholdList = 'list?', fileLocation = 'character?')
	  	
	  	if(is.null(msivOut)) msivOut <- msivIn
		if(is.null(cubeIn)) {print('getting In-sample MetricCube'); cubeIn <- stoIn$metrics()}
		if(is.null(cubeOut)) {print('getting Out-sample MetricCube'); cubeOut <- stoOut$metrics()}
		if(is.null(metricList)) metricList <- cubeIn$availableMetrics()
		if(is.null(thresholdList)) thresholdList <- STOValidationTable(stoIn)$defaultThresholds()
		
		print(squish('running ',msivIn$as.character(), ' Validation Tables...'))
		validTableIn <- STOValidationTable(stoIn)$createTable(msivIn, metricList, thresholdList, cubeIn)
		validTableOut <- STOValidationTable(stoOut)$createTable(msivOut, metricList, thresholdList, cubeOut)
		
		validTableOut <- validTableOut[row.names(validTableIn),]
		
		tempValidTableIn <- removeCommasFromDataFrame(validTableIn)
		tempValidTableOut <- removeCommasFromDataFrame(validTableOut)
		
		diffTable <- tempValidTableOut - tempValidTableIn
		diffTable <- STOWFETables$formatTable(diffTable, 4)
		diffTable[,'Threshold'] <- validTableIn[,'Threshold']
			
		STOWFETables$createFile(diffTable, stoIn, msivIn, 'Diff', fileLocation = fileLocation)
		
		diffTable <- round(tempValidTableOut / tempValidTableIn * 100, 0)	
		diffTable <- STOWFETables$formatTable(diffTable, 4)
		diffTable[,'Threshold'] <- validTableIn[,'Threshold']

		STOWFETables$createFile(diffTable, stoIn, msivIn, 'PercentDiff', fileLocation = fileLocation)		 		
})

method('createSelectedRunsTables', 'STOWFETables', function(static, stoIn, stoOut, msivIn, runNumsIn, runNumsOut = NULL, msivOut = NULL, 
														cubeIn = NULL, cubeOut = NULL, metricList = NULL, thresholdList = NULL, 
														fileLocation = NULL, ...){
		needs(stoIn = 'STO', stoOut = 'STO', msivIn = 'MSIV', runNumsIn = 'integer|numeric',runNumsOut = 'integer|numeric?',msivOut = 'MSIV?',
			  cubeIn = 'MetricCube?', cubeOut = 'MetricCube?', metricList = 'list?', thresholdList = 'list?', fileLocation = 'character?')  

		 if(is.null(msivOut)) msivOut <- msivIn
		 if(is.null(cubeIn)) {print('getting In-sample MetricCube'); cubeIn <- stoIn$metrics()}
		 if(is.null(cubeOut)) {print('getting Out-sample MetricCube'); cubeOut <- stoOut$metrics()}
		 if(is.null(metricList)) metricList <- cubeIn$availableMetrics()
		 if(is.null(thresholdList)) thresholdList <- STOValidationTable(stoIn)$defaultThresholds()
		 if(is.null(runNumsOut)) runNumsOut <- runNumsIn
		 
		 print(squish('running ', msivIn$as.character(), ' Selected Runs Tables...'))
		 selectedRunsIn <- STOSelectedRunsTable$createTable(stoIn, msivIn, runNumsIn, metricList, cubeIn)
		 selectedRunsOut <- STOSelectedRunsTable$createTable(stoOut, msivOut, runNumsOut, metricList, cubeOut)
		 
		 selectedRunsIn <- removeCommasFromDataFrame(selectedRunsIn)
		 selectedRunsOut <- removeCommasFromDataFrame(selectedRunsOut)
		 
		 diffTable <- selectedRunsOut - selectedRunsIn
		 diffTable <- STOWFETables$formatTable(diffTable, NCOL(diffTable))
		 
		 STOWFETables$createFile(diffTable, stoIn, msivIn, 'SelectedRunsDiff', runNumsIn, fileLocation = fileLocation)
		 
		 diffTable <- round(selectedRunsOut / selectedRunsIn * 100, 0)
		 diffTable <- STOWFETables$formatTable(diffTable, NCOL(diffTable))
		 
		 STOWFETables$createFile(diffTable, stoIn, msivIn, 'SelectedRunsPercentDiff', runNumsIn, fileLocation = fileLocation)    														
})

method('createFile', 'STOWFETables', function(static, diffTable, sto, msiv, fileExtension = NULL, selectedRuns = NULL, fileLocation = NULL, ...){
		needs(diffTable = 'data.frame', sto = 'STO', msiv = 'MSIV', fileExtension = 'character?', selectedRuns = 'integer|numeric?')
		
		if(is.null(fileLocation)) fileLocation <- sto$dirname() 
		fileLocation <- addSlashToDirectory(fileLocation)
		
		fileLocation <- squish(fileLocation, msiv$as.character(), fileExtension)
		hwu <- HWriterUtils(fileLocation, pdf = FALSE)
		
		hwrite(squish('Walk-Forward Efficiency Chart - ', fileExtension), hwu$connection(), br = TRUE)
		hwrite(squish('MSIV = ', msiv$as.character()), hwu$connection(), br = TRUE)
		hwrite(squish('STO Directory = ', sto$dirname()), hwu$connection(), br = TRUE)
		if(!is.null(selectedRuns)) hwrite(STOSelectedRunsTable$createHeaderTable(sto, selectedRuns), hwu$connection(), br = TRUE)
		hwrite(hwu$dataTable(diffTable, formatNumbers = FALSE), hwu$connection())
		hwu$closeConnection()		
})

method('formatTable', 'STOWFETables', function(static, diffTable, nCol,...){
		for(col in 1:nCol)
			for(row in 1:NROW(diffTable)){
				thisCell <- as.numeric(diffTable[row,col])
				ifelse(abs(thisCell) > 1000, 
					diffTable[row,col] <- format(thisCell, dig = 1, big.mark = ',', scientific = FALSE), 
					diffTable[row,col] <- format(thisCell, dig = 3 , scientific = FALSE))
			}
		diffTable
})
