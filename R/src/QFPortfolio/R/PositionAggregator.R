# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

constructor("PositionAggregator", function(aggregateCurves = NULL, curveDirectory = NULL, groupName = NULL, interval = Interval$DAILY) {
		this <- extend(RObject(), "PositionAggregator", 
			.curveDirectory = curveDirectory,
			.aggregateCurves = aggregateCurves,
			.groupName = groupName			
		)
		
		if(inStaticConstructor(this)) return(this)
		constructorNeeds(this, aggregateCurves = 'AggregateCurves?',curveDirectory = "character?", groupName = 'character?')
		if(!is.null(this$.curveDirectory)){ 
			this$.curveDirectory = addSlashToDirectory(curveDirectory)		
			failIf(is.empty(dir(this$.curveDirectory)), "curveDirectory in PositionAggregator is either empty or doesn't exist")
			failIf(is.null(groupName), 'must supply a groupName if using CurveDirectory in PositionAggregator')
			ac <- AggregateCurves(groupName)
			ac$loadCurves(this$.curveDirectory, interval=interval)
			this$.aggregateCurves <- ac
		}
		failIf(is.null(this$aggregateCurves()$atomicCurves()), 'no aggregateCurves loaded in PositionAggregator') 
		this
})

method('aggregateCurves', 'PositionAggregator', function(this, ...){
		this$.aggregateCurves	
})

method('groupedPositions', 'PositionAggregator', function(this, aggregationLevels = 'market', verbose = TRUE, ...){
		needs(aggregationLevels = 'character', verbose = 'logical')
		if(verbose) print('grouping Positions')
		groupedCurves <- this$aggregateCurves()$atomicCurves(aggregationLevels)
		positions <- list()
		for(market in names(groupedCurves)){
			if(verbose) print(market)			
			atomicPositions <- lapply(groupedCurves[[market]], function(x) x$position())
			if(length(groupedCurves[[market]]) == 1) {positions <- appendSlowly(positions, atomicPositions[[1]]);next();}
			groupedPositions <- do.call(merge, atomicPositions)
			positions <- appendSlowly(positions, zoo(rowSums(groupedPositions, na.rm = TRUE), order.by = index(groupedPositions)))
		}			
		names(positions) <- names(groupedCurves)
		positions
})

method('groupedPositionRisk', 'PositionAggregator', function(this, groupedPositions, lookbackRange = 60, variance = FALSE, verbose = TRUE, ...){
		needs(groupedPositions = 'list(zoo)', lookbackRange = 'numeric', verbose = 'logical')
		if(verbose) print('grouping Risk')
		groupedRisk <- list() 
		for(market in names(groupedPositions)){
			if(verbose) print(market)
			triSeries <- Symbol(market)$series()[,'close']
			TRIrisk <- getHistoricalVolatility(triSeries, window = lookbackRange, logChanges = FALSE, dt = 1, method = 'standard_deviation')
			combinedData <- merge(groupedPositions[[market]], TRIrisk)
			combinedData[,'TRIrisk'] <- na.locf(combinedData[,'TRIrisk'], rev = TRUE, na.rm = FALSE)
			risk <- combinedData[,'TRIrisk'] * SystemDB$bigPointValue(market)
			if(variance) risk <- risk^2
			groupedRisk <- appendSlowly(groupedRisk, risk * groupedPositions[[market]])
		}			
		names(groupedRisk) <- names(groupedPositions)
		groupedRisk
})

method('riskBySector', 'PositionAggregator', function(this, aggregationLevels = 'market', lookbackRange = 60, reportVariance = FALSE, absoluteValue = TRUE, 
			groupedPositionList = NULL, groupedRisk = NULL, verbose = TRUE, ...){
		needs(aggregationLevels = 'character', lookbackRange = 'numeric', reportVariance = 'logical', verbose = 'logical', groupedPositionList = 'list(zoo)?', groupedRisk = 'list(zoo)?')
		failIf(!absoluteValue && !reportVariance, 'if absoluteValue is FALSE, then cannot report sqrts in PositionAggregator$riskBySector')
		if(is.null(groupedPositionList)) groupedPositionList <- this$groupedPositions(aggregationLevels, verbose)
		if(is.null(groupedRisk)) groupedRisk <- this$groupedPositionRisk(groupedPositionList, lookbackRange, variance = TRUE, verbose = verbose)
		sectorTable <- SystemDB$sectors(names(groupedRisk))
		sectorNames <- unlist(lapply(names(groupedRisk), function(x) as.character(sectorTable[sectorTable$Name == x, 'Sector'])))
		output <- list()
		for(sector in unique(sectorNames)){
			print(sector)
			singleSectorRisk <- groupedRisk[sectorNames == sector]
			if(length(singleSectorRisk)== 1) {output <- appendSlowly(output, singleSectorRisk[[1]]);next();}
			singleSectorRisk <- do.call(merge, singleSectorRisk)
			if(sector == 'FX') singleSectorRisk <- this$.processFXZoo(singleSectorRisk)
			output <- appendSlowly(output, zoo(rowSums(singleSectorRisk, na.rm = TRUE), order.by = index(singleSectorRisk)))
		}
		names(output) <- unique(sectorNames)
		output <- do.call(merge, output)
		if(absoluteValue) output <- abs(output)
		if(!reportVariance) output <- sqrt(output)
		output 
})

method('.processFXZoo', 'PositionAggregator', function(this, FXZoo, ...){
		needs(FXZoo = 'zoo')
		
		if(NCOL(FXZoo) == 1) return(FXZoo)
		for(market in colnames(FXZoo)) {
			multiplier <- NA
			if(rightStr(market, 3) == '.1C') next;
			currencyPair <- leftStr(market, 6)
			for(baseCurrency in c('USD', 'JPY', 'CHF', 'EUR')){
				multiplier <- this$.setMultiplier(currencyPair, baseCurrency)
				if(!is.na(multiplier)) break;
			}
			failIf(is.na(multiplier), "Not recognized currency pair in PositionAggregator$.processFXZoo") 
			if(rightStr(market, 3) == '.P1') multiplier <- multiplier * -1
			FXZoo[,market] <- FXZoo[,market] * multiplier
		}		
		FXZoo
})

method('.setMultiplier', 'PositionAggregator', function(this, currencyPair, lowRiskCurrency, ...){
		if(leftStr(currencyPair, 3) == lowRiskCurrency) return (-1)
		if(rightStr(currencyPair, 3) == lowRiskCurrency) return(1)
		return (NA)		
})

