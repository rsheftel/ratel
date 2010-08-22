constructor("PortfolioSensitivity", function(groupName=NULL, curvesDir = NULL, outputDir = NULL, verbose=TRUE){
		this <- extend(RObject(), "PortfolioSensitivity", .groupName=groupName, .curvesDir = curvesDir, .outputDir = outputDir, .verbose=verbose)
		constructorNeeds(this, groupName='character', curvesDir = 'character', outputDir = 'character', verbose='logical')
		if (inStaticConstructor(this)) return(this)
		this$.curveGroup <- CurveGroup(groupName)
		this$.childCurves <- this$.curveGroup$childCurves(curvesDir)
		this$.outputDir <- addSlashToDirectory(outputDir)
		return(this)
})

method('shiftWeights', 'PortfolioSensitivity', function(this, metricList, perturbation, saveFile = FALSE,...){
		needs(metricList = 'list(Metric)', perturbation = 'numeric', saveFile = 'logical')
		baseWeights <- data.frame(t(this$.curveGroup$weights()), row.names = '')
		shiftUp <- this$.shiftWeightsOnce(metricList, perturbation)
		shiftDn <- this$.shiftWeightsOnce(metricList, -perturbation)
		outputTableLevel <- (shiftUp - shiftDn) / 2
		outputTableLevel[1,] <- shiftUp[1,]
		outputTablePercent <- outputTableLevel
		for(i in 2:NROW(outputTablePercent)) outputTablePercent[i,] <- (outputTablePercent[i,] / outputTablePercent[1,]) * 100
		if(saveFile) 
			this$writeFile('Sensitivity to Changes in Levels of Weights', 
						list(baseWeights, outputTableLevel, outputTablePercent), 
						list('Base Weights', 
							squish('Absolute Change from ', perturbation, ' move up (calculated as average of up and down)'),
							squish('Percentage Change from ', perturbation, ' move up (calculated as average of up and down)')),
						'LevelSensitivities.html')
		list(level = outputTableLevel, percent = outputTablePercent)
})

method('shiftRisk', 'PortfolioSensitivity', function(this, metric, shifts, changes = TRUE, saveFile = FALSE, ...){
		needs(metric = 'Metric', shifts = 'numeric', changes = 'logical', saveFile = 'logical')
		baseData <- data.frame(lapply(this$.childCurves, function(x) x$metric(metric)), row.names = '')
		baseData <- baseData * this$.curveGroup$weights()
		baseData <- baseData / sum(baseData)
		baseResult <- WeightedCurves(this$.childCurves, this$.curveGroup$weights())$curve()$metric(metric)
		baseResultTable <- data.frame(baseResult, row.names = '')
		names(baseResultTable) <- metric$as.character()
		
		output <- data.frame()
		for(sector in names(baseData)){
			output <- rbind(output, 
				data.frame(t(sapply(shifts,
							function(x) this$.calcSingleRiskPoint(metric, baseData, x, sector)))))										 
		}
		colnames(output) <- shifts
		row.names(output) <- names(baseData)
		if(changes)  output <- output - baseResult
		if(saveFile) this$writeFile(squish('Sensitivities to Changes in %ge of Metric Risk -- ', metric$as.character()), 
						list(baseData, baseResultTable, output), 
						list('Base Risk Percentages', 'Metric at Base Weights', squish('Metric Changes from shifts in Risks')),
						squish('RiskShiftSensitivities', metric$as.character(), '.html'))		
		output
})

method('writeFile', 'PortfolioSensitivity', function(this, mainTitle, data, titles, fileName, ...){
		needs(data = 'data.frame|list(data.frame)', fileName = 'character', titles = 'character|list(character)')
		hwu <- HWriterUtils(squish(this$.outputDir, fileName), pdf = FALSE)
		hwrite(mainTitle, hwu$connection(), style = list('font-size: 20pt; font-weight: bold'))
		hwu$addLine(2)
		for(i in 1:length(data)){
			hwrite(titles[[i]], hwu$connection(), )
			hwrite(hwu$dataTable(data[[i]]), hwu$connection())
			hwu$addLine(2)
		}	
		hwu$closeConnection()
})

method('.calcMetrics', 'PortfolioSensitivity', function(this, metricList, weights, rowName, ...){
		needs(metricList = 'list(Metric)', weights = 'numeric', rowName = 'character')
		output <- lapply(metricList, function(x) WeightedCurves(this$.childCurves, weights)$curve()$metric(x))
		names(output) <- unlist(lapply(metricList, function(x) x$as.character()))
		data.frame(output, row.names = rowName)
})

method('.calcSingleRiskPoint', 'PortfolioSensitivity', function(this, metric, baseData, shift, sectorName, ...){
		needs(metric = 'Metric', baseData = 'data.frame', shift = 'numeric', sectorName = 'character')
		if(this$.verbose) print(squish('Working on shifting ', sectorName, ' by ', shift))
		newWeights <- this$.curveGroup$weights()
		if(baseData[[sectorName]] == 0) return(NA)
		newWeights[sectorName] <- newWeights[[sectorName]] + shift / baseData[[sectorName]] * newWeights[[sectorName]]
		if(newWeights[[sectorName]] <= 0) return (NA)
		WeightedCurves(this$.childCurves, newWeights)$curve()$metric(metric)
})

method('.shiftWeightsOnce', 'PortfolioSensitivity', function(this, metricList, perturbation, ...){
		needs(metricList = 'list(Metric)', perturbation = 'numeric')
		baseWeights <- this$.curveGroup$weights()
		output <- this$.calcMetrics(metricList, baseWeights, 'Base Weights')
		for(sector in names(baseWeights)){
			if(this$.verbose) print(squish('Working on ', sector))
			newWeights <- baseWeights
			newWeights[sector] <- newWeights[sector] * (1 + perturbation)
			rowLabel <- squish(sector, ' shifted ', perturbation)
			output <- rbind(output, this$.calcMetrics(metricList, newWeights, rowLabel))
		}		
		output
})


