# BuyAndHold and SellAndHold
# 
# Author: rsheftel
###############################################################################

constructor("BuyAndHold", function(){
	this <- extend(RObject(), "BuyAndHold")
	if (inStaticConstructor(this)) return(this)
	return(this)
})

########################################################################################################################################
#   Buy and Hold Methods
########################################################################################################################################

method('buyAndHoldMetric', 'BuyAndHold', function(static, metric, dataZoo = NULL, tsdbName = NULL, start = NULL, end = NULL, source = 'internal', ...){
	needs(tsdbName = 'character?', metric = 'Metric', source = 'character', dataZoo = 'zoo?', start = 'character?', end = 'character?')
	
	if(is.null(dataZoo)) needs(tsdbName = 'character')
	if(is.null(dataZoo)) dataZoo <- TimeSeriesDB$retrieveTimeSeriesByName(tsdbName, data.source = source)[[1]]
	
	equityCurve <- ZooCurveLoader$fromEquity(dataZoo, 'dataZoo')
	
	if(is.null(start) || is.null(end)) return(equityCurve$metric(metric))
	equityCurve$withRange(Range(start = start, end = end))$metric(metric)    
})

method('sellAndHoldMetric', 'BuyAndHold', function(static, metric, dataZoo = NULL, tsdbName = NULL, start = NULL, end = NULL, source = 'internal', ...){
	needs(tsdbName = 'character?', metric = 'Metric', source = 'character', dataZoo = 'zoo?', start = 'character?', end = 'character?')
	
	if(is.null(dataZoo)) needs(tsdbName = 'character')
	if(is.null(dataZoo)) dataZoo <- TimeSeriesDB$retrieveTimeSeriesByName(tsdbName, data.source = source)[[1]]
	
	dataZoo <- getCumTriFromDailyTri(-diff(dataZoo))
	
	equityCurve <- ZooCurveLoader$fromEquity(dataZoo, 'dataZoo')
	
	if(is.null(start) || is.null(end)) return(equityCurve$metric(metric))
	equityCurve$withRange(Range(start = start, end = end))$metric(metric)	
})
