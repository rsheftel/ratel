# AggregateCurves Class
# 
# Author: rsheftel
###############################################################################

constructor("AggregateCurves", function(groupName=NULL, verbose=TRUE){
	this <- extend(RObject(), "AggregateCurves", .groupName=groupName, .verbose=verbose)
	constructorNeeds(this, groupName='character', verbose='logical')
	if (inStaticConstructor(this)) return(this)
	this$.initializeSmash()
	this$weights(1)
	return(this)
})

method('.initializeSmash', 'AggregateCurves', function(this, ...){
	this$.smash <- CurveGroup(this$.groupName)$smash()
	failIf(NROW(this$.smash) == 0, 'bad groupName in AggregateCurves')
})

method('groupName', 'AggregateCurves', function(this, ...){
	return(this$.groupName)	
})

method('smash', 'AggregateCurves', function(this, ...){
	return(this$.smash)	
})

method('weights', 'AggregateCurves', function(this, weights=NULL, ...){
	needs(weights='data.frame|numeric|integer')
	if (!is.data.frame(weights)){
		this$.smash$weight <- weights
		return(silent=TRUE)
	}
	origSmash <- this$.smash
	this$.smash$weight <- NULL
	this$.smash <- merge(this$.smash, weights, all=TRUE)
	rowsToRestore <- this$.smash[is.na(this$.smash$weight),]
	rowsToRestore$weight <- NULL
	restoreRows <- merge(origSmash, rowsToRestore, all=FALSE)
	this$.smash <- rbind(this$.smash[!is.na(this$.smash$weight),], restoreRows)
})

method('loadCurves', 'AggregateCurves', function(this, curvesDirectory, curvesExtension='bin', interval=Interval$DAILY, ...){
	if(rightStr(curvesDirectory,1)!='/') curvesDirectory <- squish(curvesDirectory,'/')
	this$.curves <- CurveGroup(this$.groupName)$curveList(curvesDirectory, curvesExtension, interval=interval)
})

method('atomicCurves', 'AggregateCurves', function(this, aggregationLevels=NULL, ...){
	needs(aggregationLevels='character?')
	if (is.null(aggregationLevels)) return(this$.curves)

	detailSmash <- this$smash()
	groups <- unique(detailSmash[aggregationLevels])
	groups$id <- apply(groups,1,function(x) paste(x, collapse="_"))
	curves  <- list()
	
	for (x in 1:NROW(groups)){
		id <- as.character(groups$id[[x]])
		lookupValues <- as.data.frame(groups[x,aggregationLevels])
		colnames(lookupValues) <- aggregationLevels
		msivpvs <- as.character(merge(lookupValues,detailSmash)$msivpv)
		curves[[id]] <- this$.curves[msivpvs]
	}
	return(curves)
})

method('weightedCurves', 'AggregateCurves', function(this, aggregationLevels=NULL, ...){
	needs(aggregationLevels='character?')
	atomicCurves <- this$atomicCurves(aggregationLevels)
	if (is.null(aggregationLevels)){
		weights <- as.numeric(this$.smash[match(names(atomicCurves),as.character(this$.smash$msivpv)),'weight'])
		return(WeightedCurves(atomicCurves, weights))
	}
	curves <- list()
	for (id in names(atomicCurves)){
		subCurves <- atomicCurves[[id]]
		subMSIVPVs <- names(subCurves)
		weights <- as.numeric(this$.smash[match(subMSIVPVs, as.character(this$.smash$msivpv)),'weight'])
		curves[[id]] <- WeightedCurves(subCurves, weights)
	}
	return(curves)
})

method('addFactors', 'AggregateCurves', function(this, factors, factorFrame, ...){
	needs(factors='character', factorFrame='data.frame')
	for (factor in factors) this$.smash[[factor]] <- NULL
	this$.smash <- merge(this$.smash, factorFrame, all=TRUE)
})
