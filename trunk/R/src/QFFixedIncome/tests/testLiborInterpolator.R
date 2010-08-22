# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################

library(QFMath)
library(QFFixedIncome)

testLiborInterpolator <- function(){
	start <- '2008-10-01'
	end <- '2008-10-20'
	this <- LiborInterpolator(start = start, end = end)
	
	target <- 1.99625
	result <- as.numeric(this$liborTable()[3,1])
	checkSame(target, result)
	
	target <- 4.11625
	result <- as.numeric(this$buildFincadTable('2008-10-02')[3,'rate'])
	checkSame(target,result)
	
	target <- 4.09859375
	result <- this$interpolatedRate('2008-10-02','2008-11-30')
	checkSame(target,result)	
}
