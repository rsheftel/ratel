# 
# Author: rsheftel
###############################################################################


programs <- c('fncl','fnci')

for (program in programs){
	coupons.active <- format(TBA$couponVector(program,'active'),nsmall=1)
	
	actuals <- list()
	itm <- list()
	cc <- TimeSeriesDB$retrieveOneTimeSeriesByName(paste(program,'cc','30d','yield',sep="_"),'internal')
	
	for (cpnHigh in coupons.active[2:length(coupons.active)]){
		cpnLow <- format(as.numeric(cpnHigh)-0.5, nsmall=1)
		actual <- TimeSeriesDB$retrieveOneTimeSeriesByName(paste(program,cpnHigh,cpnLow,'45d','price',sep="_"),'internal')
		actuals[[cpnHigh]] <- TBA$cut(actual,program,as.numeric(c(cpnHigh,cpnLow)))
		itm[[cpnHigh]] <- as.numeric(cpnHigh)-cc
	}
	actual.zoo <- do.call(merge,actuals)
	itm.zoo <- do.call(merge,itm) 
} 

range.x <- c(min(na.omit(as.vector(itm.zoo))), max(na.omit(as.vector(itm.zoo))))
range.y <- c(min(na.omit(as.vector(actual.zoo))), max(na.omit(as.vector(actual.zoo))))
plot(range.x,range.y)

for (x in 1:NCOL(actual.zoo)){
	points(itm.zoo[,x],actual.zoo[,x],col=x)
}
title(program)
