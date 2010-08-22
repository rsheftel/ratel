library(STO)
source(system.file("testHelper.r", package = "STO"))

test.constructor <- function(){
	bah <- BuyAndHold()
	checkInherits(bah, "BuyAndHold")
}

test.buyAndHoldMetric <- function(){
    target <- 1.552135199821794
    output <- BuyAndHold$buyAndHoldMetric(CalmarRatio, tsdbName = 'ty_frontquarterly_straddle_tri_daily_mid', start = '2007-01-01', end = '2007-12-31')
    checkEquals(target, output)
    
    data <- TimeSeriesDB$retrieveTimeSeriesByName('ty_frontquarterly_straddle_tri_daily_mid','internal')[[1]]
    output <- BuyAndHold$buyAndHoldMetric(CalmarRatio, dataZoo = data, start = '2007-01-01', end = '2007-12-31')
    checkEquals(target, output)
}

test.sellAndHoldMetric <- function(){
	target <- -0.666672703414006
	output <- BuyAndHold$sellAndHoldMetric(CalmarRatio, tsdbName = 'ty_frontquarterly_straddle_tri_daily_mid', start = '2007-01-01', end = '2007-12-31')
	checkEquals(target, output)
		
	data <- TimeSeriesDB$retrieveTimeSeriesByName('ty_frontquarterly_straddle_tri_daily_mid','internal')[[1]]
	output <- BuyAndHold$sellAndHoldMetric(CalmarRatio, dataZoo = data, start = '2007-01-01', end = '2007-12-31')
	checkEquals(target, output)
}
