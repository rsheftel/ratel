# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


testThresholdList <- function(){
	target1 <- list(QAnnualizedNetProfit = '>0',
		QTotalTrades = '>50',
		QKRatio = '>0.4',
		QConditionalTenPercentileCalmarRatio = '>0.3',
		QAverageTrade = '>10000',
		QWinningTradesPct = '>0.50'
	)
	
	target2 <- list(QAnnualizedNetProfit = '>0',
		QTotalTrades = '>50',
		QKRatio = '>0.3',
		QConditionalTenPercentileCalmarRatio = '>0.4',
		QAverageTrade = '>10000',
		QWinningTradesPct = '>0.40'
	)
	
	target3 <- list(Metric$fetch('QAnnualizedNetProfit'),
					Metric$fetch('QTotalTrades'),
					Metric$fetch('QKRatio'),
					Metric$fetch('QConditionalTenPercentileCalmarRatio'),
					Metric$fetch('QAverageTrade'),
					Metric$fetch('QWinningTradesPct')
	)			
	
	shouldBomb(ThresholdList$list('junk'))
	
	checkSame(target1, ThresholdList$list('Carry'))
	checkSame(target1, ThresholdList$list('Oscillation'))
	checkSame(target1, ThresholdList$list('Portfolio'))
	checkSame(target2, ThresholdList$list('Trend'))
	checkSame(target3, ThresholdList$metricList('Carry'))
}
