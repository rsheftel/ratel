# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


constructor("FuturesOptionPair",function(underlying = NULL, type = NULL, optionMonth = NULL, optionYear = NULL, underlyingMonth = NULL, underlyingYear = NULL,
			centralStrike = NULL, width = NULL){
	this <- extend(RObject(), "FuturesOptionPair", .underlying = underlying, .type = type, .optionMonth = optionMonth, .optionYear = optionYear,
				.underlyingMonth = underlyingMonth, .underlyingYear = underlyingYear)
	constructorNeeds(this, underlying = 'character', type = 'character', centralStrike = 'numeric', width = 'numeric?',
					 optionMonth = 'numeric', optionYear = 'numeric', underlyingMonth = 'numeric', underlyingYear = 'numeric')
	if(inStaticConstructor(this)) return(this)
	assert(type %in% c('straddle', 'strangle'))
	if(type == 'straddle'){
		this$.lowerStrike = centralStrike
		this$.upperStrike = centralStrike
	}
	if(type == 'strangle'){
		this$.lowerStrike = centralStrike - width
		this$.upperStrike = centralStrike + width
	}
	
	this$.lowerLeg <- FuturesOption(this$underlyingContract(), optionType = 'put', this$optionMonth(), this$optionYear(), 
									this$underlyingMonth(), this$underlyingYear(), strike = this$strike('lower'))
	this$.upperLeg <- FuturesOption(this$underlyingContract(), optionType = 'call', this$optionMonth(), this$optionYear(),
									this$underlyingMonth(), this$underlyingYear(), strike = this$strike('upper'))
	this	
})

method('underlying', 'FuturesOptionPair', function(this, ...){
	this$.underlying
})

method('type', 'FuturesOptionPair', function(this, ...){
	this$.type
})

method('underlyingContract', 'FuturesOptionPair', function(this, ...){
	Contract(this$underlying(), 'Comdty')
})

method('strike', 'FuturesOptionPair', function(this, side = NULL,...){
	assert(side %in% c('lower', 'upper'))
	if(side == 'lower') return(this$.lowerStrike)
	this$.upperStrike
})

method('optionMonth', 'FuturesOptionPair', function(this, ...){
	this$.optionMonth
})

method('optionYear', 'FuturesOptionPair', function(this,...){
	this$.optionYear	
})

method('underlyingMonth', 'FuturesOptionPair', function(this,...){
	this$.underlyingMonth		
})

method('underlyingYear', 'FuturesOptionPair', function(this,...){
	this$.underlyingYear		
})

method('optionLeg', 'FuturesOptionPair', function(this, side = NULL,...){
	assert(side %in% c('lower', 'upper'))
	if(side == 'lower') return(this$.lowerLeg)
	this$.upperLeg	
})

method('mergedPrices', 'FuturesOptionPair', function(this, start=NULL, end = NULL, ...){
	if(is.empty(this$optionLeg('lower')$historicalPrices(start,end))) return(NULL)
	if(is.empty(this$optionLeg('upper')$historicalPrices(start,end))) return(NULL)
	merge(this$optionLeg('lower')$historicalPrices(start,end), this$optionLeg('upper')$historicalPrices(start,end), all = FALSE)	
})

method('mergedDeltaHedgedReturns', 'FuturesOptionPair', function(this, start = NULL, end = NULL, liborInterpolator = LiborInterpolator(),...){
	if(is.empty(this$optionLeg('lower')$deltaHedgedReturns(start,end,liborInterpolator))) return(NULL)
	if(is.empty(this$optionLeg('upper')$deltaHedgedReturns(start,end,liborInterpolator))) return(NULL)
	merge(this$optionLeg('lower')$deltaHedgedReturns(start, end, liborInterpolator),
		  this$optionLeg('upper')$deltaHedgedReturns(start, end, liborInterpolator), all = TRUE)	
})

method('pairDeltaHedgedReturns', 'FuturesOptionPair', function(this, start = NULL, end = NULL, liborInterpolator = LiborInterpolator(), ...){
	if(is.null(this$.pairDeltaHedgedReturns)){
	mergedReturns <- this$mergedDeltaHedgedReturns(start, end, liborInterpolator)
	mergedPrices <- this$mergedPrices(start,end, liborInterpolator)
	if(is.empty(mergedReturns) || is.empty(mergedPrices)) return(NULL)
	
	# check to make sure that the delta hedging is always for the same number of days in both the lower and upper leg
	goodDates1 <- na.omit(merge(mergedPrices[,1],lag(mergedPrices[,1],-1)))
	goodDates2 <- na.omit(merge(mergedPrices[,2],lag(mergedPrices[,2],-1)))
	goodDates <- index(merge(goodDates1, goodDates2, all=FALSE))
	
	mergedReturns <- mergedReturns[goodDates,]
	this$.pairDeltaHedgedReturns <- mergedReturns[,1] + mergedReturns[,2]		
	}
	this$.pairDeltaHedgedReturns
})