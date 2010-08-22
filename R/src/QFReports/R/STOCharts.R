# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################
constructor("STOCharts", function(sto = NULL, stoDir=NULL, stoName=NULL, cube = NULL){
		this <- extend(RObject(), "STOCharts", .sto = sto, .stoDir = stoDir, .stoName = stoName, .cube = cube)
		constructorNeeds(this)
		if(inStaticConstructor(this)) return(this)
		if(is.null(this$.sto)) this$.sto <- STO(this$.stoDir, this$.stoName, calculateMetrics = FALSE)
		this$.dirName <- this$.sto$dirname()
		if(is.null(this$.cube)) this$.cube <- this$.sto$metrics()
		return(this)	
})

method('cube', 'STOCharts', function(this,...){
	this$.cube
})

method('underlyingSTO', 'STOCharts', function(this, ...){
	this$.sto
})

method('dirName', 'STOCharts', function(this, ...){
	this$.dirName	
})

### Sample usage
#  sto <- STO('v:/Market Systems/General Market Systems/RSITargets', '20090115_in', calculateMetrics = FALSE)
#  stc <- STOCharts(sto)
#  stc$runContourCharts('HalfLife', c('EntryLevel','ExitLevel'), QKRatio, '_test', 
#						 filteredParams = sto$parameters()$filter(HalfLife > 25), zRange = c(-3,3))

method('runContourCharts', 'STOCharts', function(this, axis1Names, axis2Names, metrics, fileNameExtension = NULL, aggFunction = mean, 
												 msivList = this$underlyingSTO()$msivs(),  filteredParams = NULL, zRange = NULL, ...){
	needs(axis1Names = 'character', axis2Names = 'character', metrics = 'Metric|list', aggFunction = 'function', fileNameExtension = 'character?', 
		  msivList = 'list', filteredParams = 'RunFilter?')
	axisNames <- cbind(axis1Names, axis2Names)
	failIf((length(metrics) != 1 && length(metrics) != NROW(axisNames)), 'number of Metrics must be 1 or same as number of axes in STOCharts')
	
	if(is.null(filteredParams)) filteredParams <- RunFilter$with("ALL", this$underlyingSTO()$.runs)
	
	metricList <- metrics
	if(length(metrics) == 1) metricList <- lapply(parse(text = rep(as.character(metrics), NROW(axisNames))), eval)

	for(port in msivList){
		stringMsiv <- strsplit(port$as.character(), "_")[[1]][[4]]
		outputFileName <- paste(stringMsiv, fileNameExtension, '.pdf', sep = '')
		
		print(stringMsiv)
		
		pdf(file = paste(this$dirName(), outputFileName, sep = ''), paper = 'special', width = 10, height = 10)
		for(rowNum in 1:NROW(axisNames)){
			axis1 <- axisNames[rowNum,1]
			axis2 <- axisNames[rowNum,2]
			metric <- metricList[[rowNum]] 

			currSurface <- this$underlyingSTO()$surface(port, axis1, axis2, metric, aggFunction, metric.cube = this$cube(), filter = filteredParams)	
			currSurface$plotContour(paste(as.character(metric), ' - ', stringMsiv, sep = ''), zRange = zRange)
		}
		dev.off()
	}	
})

# USAGE
# stc <- STOCharts(sto)
# stc$downloadMetricCsvs()

method('downloadMetricCsvs', 'STOCharts', function(this, msivList = this$underlyingSTO()$msivs(), ...){
	needs(msivList = 'list')
	
	params <- this$underlyingSTO()$parameters()
	metricList <- this$cube()$availableMetrics()
	
	for(port in msivList){
		print(port)
		metricsData <- data.frame(
			sapply(metricList, function(x) this$cube()$values(x, port), simplify = FALSE),
			row.names = this$underlyingSTO()$runNumbers())
		names(metricsData) <- c(sapply(metricList, function(x) x$as.character()))
		rowFind <- match(row.names(metricsData), params$.data[,1])
		Data <- cbind(metricsData, params$.data[rowFind,-1])
		write.csv(Data, paste(this$dirName(), port, '.csv', sep = ''), row.names = TRUE)
	}	
})

####################################################################################################
#    Static Plot methods
####################################################################################################

method('plotPortfolioSingleMsivMultipleRuns', 'STOCharts', function(static,msivs,weights,runs,sto.curves,...){	
	pm <- ParameterizedMsivs(msivs = msivs, weights = weights, runs = runs)
	plot(pm$curve(sto.curves)$equity())
})

method("plotEquitySingleMsivSingleRun","STOCharts" ,function (static, curve.cube, msiv, run, ...){
#plot an equity curve for a single msiv and a single run
	windows()
	equityCurve <- curve.cube$curve(msiv,run)$equity()
	title <- paste(as.character(msiv),' run= ',run)
	simpleZooPlot(equityCurve, "Dates", "Equity", title)
})

method("plotEquityMultipleMsivsSingleRun", "STOCharts", function (static, curve.cube, plotMsivs, run, ...){
#plot equity curve multiple msivs for a single run
	windows()
	title <- paste('Multiple MSIV run= ',run)
	equityZoo <- STOUtils$mergeMultipleMsivsSingleRun(curve.cube, plotMsivs, run)
	simpleZooPlot(equityZoo, "Dates", "Equity", title)	
})

method("plotEquitySingleMsivMultipleRuns", "STOCharts", function (static, curve.cube, msiv, runs, ...){
#plot equity curve single msiv multiple runs
	windows()
	title <- paste(as.character(msiv),' runs= ',paste(runs,collapse=","))
	simpleZooPlot(STOUtils$mergeSingleMsivMultipleRuns(curve.cube, msiv, runs), "Dates", "Equity", title)
})

method("plotEquitySingleMsivSingleRunPanelByCalendar","STOCharts" ,function (static, curve.cube, msiv, run, ...){
#plot an equity curve for a single msiv and a single run
	equityCurve <- curve.cube$curve(msiv,run)$equity()
	plotZooPanelByCalendar(equityCurve)
})

####################################################################################################
#    In-Out Sample stiched - Static Plot methods
####################################################################################################
method("runEquitySingleMsivRunStitchInOutSample", "STOCharts", function(static, curve.cube.inSample, curve.cube.outSample, msiv.inSample, msiv.outSample, run, ...){
	equities <- list()
	equities$inSample <- curve.cube.inSample$curve(msiv.inSample,run)$equity()
	equities$outSample <- curve.cube.outSample$curve(msiv.outSample,run)$equity()
	equities
})

method("plotEquityMsivRunStitchInOutSample", "STOCharts", function(static, curve.cube.inSample, curve.cube.outSample, msiv.inSample, msiv.outSample, run, title=NULL, ...){
	needs(title="character?")
	equities <- static$runEquitySingleMsivRunStitchInOutSample(curve.cube.inSample, curve.cube.outSample, msiv.inSample, msiv.outSample, run)
	if(is.null(title)) title <- paste(as.character(msiv.inSample),' run= ',run,' In/Out-Sample')
	plotStitchedZoos(equities, title=title, ylab="Equity", overlapRule = "usePrior")
})

method("plotEquityMultipleMsivRunStitchInOutSample", "STOCharts", function(static, curve.cube.inSample, curve.cube.outSample, msiv.inSample, msiv.outSample, runVector, title=NULL, ...){
	needs(runVector = 'numeric',title="character?")
	equityCurves <- lapply(runVector, function(run) 
		{output = static$runEquitySingleMsivRunStitchInOutSample(curve.cube.inSample, curve.cube.outSample, msiv.inSample, msiv.outSample, run)
			output <- getZooDataFrame(stitchZoos(output, backAdjusted = TRUE, overlapRule = 'usePrior')$stitchedZoo)
			colnames(output) = run; output})
	equityCurvesMerged <- do.call(merge, equityCurves)
	if(is.null(title)) title <- msiv.inSample$as.character() 
	simpleZooPlot(equityCurvesMerged, "Dates", "Equity", title = title)
	
	splitDate <- stitchZoos(static$runEquitySingleMsivRunStitchInOutSample(curve.cube.inSample,curve.cube.outSample, msiv.inSample, msiv.outSample, runVector[[1]]))$splitDates
	abline(v = splitDate)
})

method("pdfMultipleContours", "STOCharts", function(static, sto, axis1, axis2, metric, aggregation.function = mean, fileLocation, filter = NULL, msivList = NULL, ...){
	needs(sto = 'STO', axis1 = 'character', axis2 = 'character', metric = 'Metric', fileLocation = 'character')
	cube <- sto$metrics()
	
	if(is.null(msivList)) msivList <- sto$msivs()
	
	surfaceList <- lapply(msivList, function(currMsiv) sto$surface(currMsiv, axis1, axis2, metric, aggregation.function, filter = RunFilter$with("ALL", sto$.runs), metric.cube = cube))
	
	pdf(file = squish(fileLocation, "/", sto$id(), ".", metric$as.character(), ".Contours.pdf"), paper = "special", width = 10, height = 10)
	for(i in 1:length(surfaceList)) surfaceList[[i]]$plotContour(squish(msivList[[i]]$as.character(), " - ", metric$as.character()))
	dev.off()
})

####################################################################################################
#    Correlation Plot methods
####################################################################################################

method("plotCorrelationPanel", "STOCharts", function(static, curve.cube, msivs, runs, removeNA=TRUE, ...){
	#reomveNA is either TRUE, in which case all rows with NA are deleted, or if a number these will replace the NAs
	needs(curve.cube="CurveCube", msivs="MSIV|list(MSIV)", runs="numeric|integer", removeNA="logical|numeric|integer")
	if((length(msivs)>1)&&(length(runs)>1)){
		equityZoo <- STOUtils$mergeMsivsAcrossRuns(curve.cube, msivs, runs)
		colnames(equityZoo) <- c(sapply(msivs,function(msiv) msiv$market()),'Average')
	}else{ 
		equityZoo <- STOUtils$msivsRunsToZoo(curve.cube, msivs, runs)
	}
	equityDF <- data.frame(equityZoo)
	if (removeNA==TRUE){equityDF <- na.omit(equityDF)}
	else equityDF[is.na(equityDF)] <- removeNA 
	plotCorrelationPanel(equityDF)
})

