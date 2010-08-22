# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


constructor("PortfolioRunManager", function(){
		this <- extend(RObject(), "PortfolioRunManager")
		if (inStaticConstructor(this)) return(this)
		return(this)
})

method("outputListCreator", "PortfolioRunManager", function(this, portfolioRun, source = 'optimal', curves = 'cut', slip = 'slipped', ...){
		needs(portfolioRun = 'PortfolioRun')
		
		if(!portfolioRun$.optimized) {print('portfolioRun not optimized');return(NULL)}
		portfolioEquity <- portfolioRun$portfolioCurveEquity(source = source, curves = curves, slip = slip)
		names(index(portfolioEquity)) <- NULL
		
		systemNames <- colnames(portfolioRun$childCurvesEquity())
				
		metricData <- portfolioRun$.metrics[source][[1]][curves][[1]][slip][[1]]['Portfolio'][[1]]
		
		constraintDF <- this$getConstraintsDataFrame(portfolioRun)
		
		outputDataFrame <- data.frame(portfolioRun$name(),
			portfolioRun$startDate(),
			portfolioRun$endDate(), 
			t(portfolioRun$seeds()),
			t(as.numeric(metricData)),
			constraintDF,
			t(as.numeric(portfolioRun$.weights$min)),
			t(as.numeric(portfolioRun$.weights$max)),
			t(as.numeric(portfolioRun$.weights$optimal))
			
			)		
			
		names(outputDataFrame) <- c('RunName', 'StartDate', 'EndDate',
			paste('Seeds.', systemNames, sep = ''),
			names(metricData),
			names(constraintDF),
			paste('Weights.Min.', systemNames, sep = ''),
			paste('Weights.Max.', systemNames, sep = ''),
			paste('Weights.Optimal.', systemNames, sep = '')
			)
		
		outputList <- list(outputData = outputDataFrame, portfolioEquity = portfolioEquity)
		
		return(outputList)
})

method('getConstraintsDataFrame', 'PortfolioRunManager', function(this, portfolioRun, ...){
	constraints <- portfolioRun$constraintListDF()
	output <- constraints[[1]]
	names(output) <- paste('Con1.', names(output), sep = '')
	if(length(constraints) == 1) return(output)
	
	for(conNum in 2:length(constraints)){
		oldNames <- names(output)
		newDF <- constraints[[conNum]]
		newNames <- paste('Con', conNum, '.', names(newDF), sep = '')
		output <- data.frame(output, newDF)
		names(output) <- c(oldNames, newNames)
	}
	return(output)
})

method('saveRun', 'PortfolioRunManager', function(this, portfolioRun, file, ...){
	needs(portfolioRun = 'PortfolioRun', file = 'character')
	
	outputList <- this$outputListCreator(portfolioRun)
	dput(outputList, file)		
})

method('getRun', 'PortfolioRunManager', function(this, file, ...){
	needs(file = 'character')
	
	dget(file)	
})

method('mergeSavedRuns', 'PortfolioRunManager', function(this, oldRunData, runFileName, ...){
	needs(oldRunData = 'list', runFileName = 'character')
	
	output <- list()
	newRun <- this$getRun(runFileName)
	output$outputData <- mergeDataFrames(oldRunData$outputData, newRun$outputData)
	rownames(output$outputData) <- NULL
	output$portfolioEquity <- merge(oldRunData$portfolioEquity, newRun$portfolioEquity, all = TRUE)
	colnames(output$portfolioEquity) <- output$outputData[,'RunName']
	return(output)
})