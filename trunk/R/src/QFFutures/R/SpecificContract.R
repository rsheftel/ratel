# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


constructor('SpecificContract', function(underlyingTicker = NULL, expiryMonth = NULL, expiryYear = NULL){
	this <- extend(RObject(), 'SpecificContract', .underlyingTicker = underlyingTicker, .expiryMonth = expiryMonth, .expiryYear = expiryYear)
	constructorNeeds(this, underlyingTicker = 'character', expiryMonth = 'numeric', expiryYear = 'numeric')
	if(inStaticConstructor(this)) return(this)
	
	this	
})

method('attributes', 'SpecificContract', function(this, ...){
	list(instrument = 'futures', 
		future_month = this$.expiryMonth,
		future_year = this$.expiryYear,
		quote_side = 'mid',
		quote_type = 'close',
		quote_convention = 'price',
		contract = this$.underlyingTicker)	
})

method('generic', 'SpecificContract', function(this,...){
	Contract(this$.underlyingTicker, 'Comdty')	
})

method('underlyingTicker', 'SpecificContract', function(this,...){
	this$.underlyingTicker		
})

method('expiryMonth', 'SpecificContract', function(this,...){
	this$.expiryMonth		
})

method('expiryYear', 'SpecificContract', function(this,...){
	this$.expiryYear		
})

method('historicalPrices', 'SpecificContract', function(this, start = NULL, end = NULL, source = 'internal', ...){
	if((!is.null(start) && !is.null(this$.start) && start != this$.start) 
		|| (!is.null(end) && !is.null(this$.end) && end != this$.end)
		|| is.null(this$.historicalPrices)){
		this$.start <- start
		this$.end <- end
		this$.historicalPrices <- TimeSeriesDB()$retrieveTimeSeriesByAttributeList(this$attributes(), start = start, end = end, data.source = source)[[1]]
	}	
	this$.historicalPrices
})

method('dailyChanges', 'SpecificContract', function(this, start = NULL, end = NULL, source = 'internal',...){
	diff(this$historicalPrices(start = start, end = end, source = source))	
})