# test functions for Interpolate
# 
# Author: rsheftel
###############################################################################

library(QFMath)


test.qf.interpolate <- function(){
	
	checkSame(4, qf.interpolate(4, c(1,2,3,4), c(1,2,3,4)))
	checkSame(35, qf.interpolate(4.5, c(2,3,4,5), c(10,20,30,40)))
	checkSame(10, qf.interpolate(-99, c(2,3,4,5), c(10,20,30,40)))
	checkSame(40, qf.interpolate(99, c(2,3,4,5), c(10,20,30,40)))
	checkSame(30, qf.interpolate("fsdf", c(1,2,3), c(10,20,30)))
	checkTrue(is.na(qf.interpolate(2.5, c(1,2,3), c(10,20))))
	checkTrue(is.na(qf.interpolate(4.5, c(2,3,4,5), c(10,20,30,40,50,60,70))))
	checkSame(42.5, qf.interpolate(4.5, c(2,3,4,5), c(10,20,NA,50)))
	checkSame(42.5, qf.interpolate(4.5, c(2,3,NA,5), c(10,20,40,50)))
	checkSame(20, qf.interpolate(2.5, c(NA,3,4,5), c(NA,20,40,50)))
	checkSame(25, qf.interpolate(3.25, c(NA,3,4,5), c(NA,20,40,50)))
	checkSame(40, qf.interpolate(4.5, c(2,3,4,NA), c(10,20,40,NA)))
}


test.qf.interpolateXY <- function(){
	
	xVec <- c(1,2,3,4)
	yVec <- c(5,6,7,8)
	zMat <- matrix(NA, nrow=3, ncol=3)
	zMat <- xVec%*%t(yVec)
	
	checkSame(10, qf.interpolateXY(6,-99, yVec, xVec, zMat))
	checkSame(16, qf.interpolateXY(6,99, yVec, xVec, zMat))
	checkSame(6, qf.interpolateXY(-99,2, yVec, xVec, zMat))
	checkSame(24, qf.interpolateXY(99,2, yVec, xVec, zMat))
	checkSame(16.25, qf.interpolateXY(6.5,2.5, yVec, xVec, zMat))
	checkSame(5, qf.interpolateXY(-99,-99, yVec, xVec, zMat))
	
}
