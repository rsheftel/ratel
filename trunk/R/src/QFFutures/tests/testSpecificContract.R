# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


library(QFFutures)

testSpecificContract <- function(){
	shouldBomb(SpecificContract(12,12,2008))
	shouldBomb(SpecificContract('ty','ty',2008))
	shouldBomb(SpecificContract('ty',12,'ty'))
	
	this <- SpecificContract('ty',12,2008)
	
	target <- list(instrument = 'futures', future_month = 12, future_year = 2008, quote_side = 'mid', quote_type = 'close', quote_convention = 'price', contract = 'ty')
	result <- this$attributes()
	checkSame(target, result)
	
	target <- 113.546875
	historicalPrices <- this$historicalPrices(start = '2008-10-01', end = '2008-10-20')
	result <- as.numeric(historicalPrices[as.POSIXct('2008-10-10 15:00:00'),])
	checkSame(target, result)
	
	target <- -1.40625
	result <- as.numeric(this$dailyChanges()[as.POSIXct('2008-10-09 15:00:00'),])
	checkSame(target, result)
}
