# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################



constructor("ThresholdList", function(){
		this <- extend(RObject(), "ThresholdList")
		constructorNeeds(this)
		if(inStaticConstructor(this)) return(this)
		return(this)
})

method('list', 'ThresholdList', function(static, type, ...){
		needs(type = 'character')
		assert(type %in% c('Carry', 'Director', 'Event', 'MBS Specific', 'Oscillation', 'Portfolio', 'Test', 'Trend'), 'unimplemented Type in ThresholdList')
		
		if(type %in% c('Director', 'Event', 'MBS Specific', 'Test')) return(squish('Type ', type, ' does not have list defined for ThresholdList'))
		
		if(type == 'Carry') return(ThresholdList()$carry())
		if(type == 'Oscillation') return(ThresholdList()$oscillation())
		if(type == 'Portfolio') return(ThresholdList()$portfolio())
		if(type == 'Trend') return(ThresholdList()$trend())
})

method('metricList', 'ThresholdList', function(static, type, ...){
		lapply(names(ThresholdList()$list(type)), Metric$fetch)	
})

method('carry', 'ThresholdList', function(static, ...){
		return(ThresholdList()$oscillation())
})

method('oscillation', 'ThresholdList', function(static, ...){
		return(list(QAnnualizedNetProfit = '>0',
		 QTotalTrades = '>50',
		 QKRatio = '>0.4',
		 QConditionalTenPercentileCalmarRatio = '>0.3',
		 QAverageTrade = '>10000',
		 QWinningTradesPct = '>0.50'
	 	))
})

method('portfolio', 'ThresholdList', function(static, ...){
		return(ThresholdList()$oscillation())
})

method('trend', 'ThresholdList', function(static, ...){
		return(list(QAnnualizedNetProfit = '>0',
				QTotalTrades = '>50',
				QKRatio = '>0.3',
				QConditionalTenPercentileCalmarRatio = '>0.4',
				QAverageTrade = '>10000',
				QWinningTradesPct = '>0.40'
		))
})