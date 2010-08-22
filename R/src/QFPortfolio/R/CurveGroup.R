constructor("CurveGroup", function(name = NULL, verbose=FALSE) {
    this <- extend(RObject(), "CurveGroup", .name = name, .verbose=verbose)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, name="character", verbose='logical')
    this
})

method("curve", "CurveGroup", function(this, dir, extension = "bin", range = NULL, ...) {
    wmpf <- JGroups$GROUPS()$rWeighting_by_String(this$.name)
    this$.wmpfToCurve(wmpf, dir, extension, range)
})

method("curveList", "CurveGroup", function(this, dir, extension = "bin", range = NULL, interval=Interval$DAILY, ...) {
	wmpf <- JGroups$GROUPS()$rWeighting_by_String(this$.name)
	curves <- this$.wmpfToWeightedCurves(wmpf, dir, extension, range, interval=interval)$unscaledCurves()
	names(curves) <- wmpf$filenames()
	return(curves)
})

method("checkRangeType", "CurveGroup", function(this,rangeType = NULL, ...) {
	if(!is.null(rangeType))assert(rangeType %in% c('in-sample','out-sample','live','backtest'))
})

method(".wmpfToWeightedCurves", "CurveGroup", function(static, wmpf, dir, extension = "bin", range = NULL, rangeType = NULL, interval=Interval$DAILY, ...) {	
	needs(interval="Interval?")
    files <- paste(dir, "/", wmpf$filenames(), ".", extension, sep="")
	validCurveIndex <- NULL
	curves <- list()	
	for (i in sequence(length(files))){
		if(!is.null(rangeType)){			
			range <- SystemDB$tradingRangeForLiveMSIVPV(msivName = wmpf$msivs()[i],pvName = wmpf$pvs()[i])			
			if(rangeType == 'backtest' && !is.null(range)) range <- Range$before(range$.start)		
		}
		if(!(is.null(range) && !is.null(rangeType))){			
			validCurveIndex <- c(validCurveIndex,i)
			if (static$.verbose) print(squish("loading msivpv: ",files[i]))
			curves[[i]] <- ZooCurveLoader$fromFile(files[i], range = range)
			if (!is.null(interval)) curves[[i]] <- curves[[i]]$withInterval(interval)
		}		
	}	
	if(is.null(validCurveIndex))return(NULL)
    return(WeightedCurves(static$cut(wmpf$markets()[validCurveIndex], curves[validCurveIndex]), wmpf$weights()[validCurveIndex]))	
})

method(".wmpfToCurve", "CurveGroup", function(static, wmpf, dir, extension = "bin", range = NULL,rangeType = NULL, interval=Interval$DAILY, ...) {
	weightedCurve <- static$.wmpfToWeightedCurves(wmpf, dir, extension, range, rangeType, interval=interval)
	if(is.null(weightedCurve)) return(NULL)
	return(weightedCurve$curve())
})

method("childCurves", "CurveGroup", function(this, dir, extension = "bin", range = NULL, rangeType = NULL, interval=Interval$DAILY, ...) {
	this$checkRangeType(rangeType)
	children <- JGroups$GROUPS()$members_by_String(this$.name)
	res <- japply(children$iterator(), JGroup(), JGroups$GROUPS()$rWeighting_by_Group)
	res <- lapply(res, function(wmpf) this$.wmpfToCurve(wmpf, dir, extension, range, rangeType, interval=interval))
	names(res) <- japply(children$iterator(), JGroup(), name)	
	res[!sapply(res,is.null)]
})

method("childNames", "CurveGroup", function(this, ...){
	children <- JGroups$GROUPS()$members_by_String(this$.name)
	as.character(japply(children$iterator(), JGroup(), name))
})

method("weights", "CurveGroup", function(this, ...) {
    children <- JGroups$GROUPS()$members_by_String(this$.name)
    res <- as.vector(japply(children$iterator(), JGroup(), weight), mode="numeric")
    names(res) <- japply(children$iterator(), JGroup(), name)
    res
})

method("cut", "CurveGroup", function(static, markets, curves, ...) { 
    checkLength(markets, length(curves))
    lapply(seq_along(markets), function(i) { 
        market <- markets[[i]]
        curve <- curves[[i]]
        jMarket <- JSymbol$by_String(market)
        if (jMarket$hasPeriods()){
            starts <- jMarket$starts()
            ends <- jMarket$ends()
            static$cutCurve(curve, starts, ends, market)
        } else {
            curve
        }
    })
})

method("cutCurve", "CurveGroup", function(static, curve, starts, ends, market, ...) { 
    needs(curve = "PositionEquityCurve", starts = "list(JDate)", ends = "list(JDate)", market="character")
    checkLength(starts, length(ends))
    starts <- lapply(starts, as.POSIXct)
    ends <- lapply(ends, as.POSIXct)
    ends <- lapply(ends, makeEndInclusive)
    curve <- curve$as.zoo()
    zoos <- lapply(seq_along(starts), function(i) window(curve, start=starts[[i]], end=ends[[i]]))
    cutZoo <- do.call(rbind, zoos)
    zooLoader <- ZooCurveLoader(cutZoo, squish("only active points for ", market))
    PositionEquityCurve(zooLoader)
})

method(".wmpf", "CurveGroup", function(this, ...){
	lazy(this$...wmpf, JGroups$GROUPS()$rWeighting_by_String(this$.name), log=FALSE)
})

method("markets", "CurveGroup", function(this, unique=TRUE, ...){
	needs(unique='logical')
	res <- this$.wmpf()$markets()
	if (unique) res <- unique(res)
	return(res)
})

method("systems", "CurveGroup", function(this, unique=TRUE, ...){
	needs(unique='logical')
	res <- this$.wmpf()$systems()
	if (unique) res <- unique(res)
	return(res)
})

method("intervals", "CurveGroup", function(this, unique=TRUE, ...){
needs(unique='logical')
	res <- this$.wmpf()$intervals()
	if (unique) res <- unique(res)
	return(res)
})

method("pvs", "CurveGroup", function(this, unique=TRUE, ...){
	needs(unique='logical')
	res <- this$.wmpf()$pvs()
	if (unique) res <- unique(res)
	return(res)
})

method("versions", "CurveGroup", function(this, unique=TRUE, ...){
	needs(unique='logical')
	res <- this$.wmpf()$versions()
	if (unique) res <- unique(res)
	return(res)
})

method("msivs", "CurveGroup", function(this, unique=TRUE, ...){
	needs(unique='logical')
	res <- this$.wmpf()$msivs()
	if (unique) res <- unique(res)
	return(res)
})

method('filenames', 'CurveGroup', function(this, ...){
	return(this$.wmpf()$filenames())
})

method('msivpvs', 'CurveGroup', function(this, ...){
	return(this$.wmpf()$filenames())
})

method("totalWeights", "CurveGroup", function(this, ...){
	return(this$.wmpf()$weights())	
})

method('smash', 'CurveGroup', function(this, ...){
	return(data.frame(	system 		= this$systems(unique=FALSE),
						interval	= this$intervals(unique=FALSE),
						version		= this$versions(unique=FALSE),
						pv			= this$pvs(unique=FALSE),
						market 		= this$markets(unique=FALSE),
						msiv		= this$msivs(unique=FALSE),
						msivpv		= this$msivpvs()))
})
