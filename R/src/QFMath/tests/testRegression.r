# test for Regression Class
# 
# Author: rsheftel
###############################################################################


library(QFMath)

test.exponentialWeights <- function(){
	
	result.1 <- c(1.0000,0.99424,0.98851,0.98282,0.97716,0.97153,0.96594,0.96037,0.95484,0.94934)
	checkSame(result.1, round(Regression$exponentialWeights(10,120),5))
	
	result.2 <- c(result.1[1:5],rep(0,5))
	checkSame(result.2, round(Regression$exponentialWeights(10,120,5),5))
	
}