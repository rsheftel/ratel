# test of HypothesisTest.R class
# 
# Author: RSheftel
###############################################################################

library(QFMath)

test.signTest <- function(){
	
	vect.1 <- c(-1,1,-1,1,-1,1,-1,1,-1,1)
	vect.2 <- c(-5,7,-8,-6,8,4,-3,5,0,9)
	test.matrix <- cbind(vect.1,vect.2)
	
	checkSame(0.7, HypothesisTests$signTest(test.matrix)$estimate[[1]])
	checkSame(0.7, HypothesisTests$signTest(zoo(test.matrix))$estimate[[1]])
	checkSame(0.7, HypothesisTests$signTest(data.frame(test.matrix))$estimate[[1]])
	
	checkSame(0.34375, HypothesisTests$signTest(test.matrix)$p.value)
	
	shouldBomb(HypothesisTests$signTest(cbind(vect.1,vect.1,vect.2)))
}

test.ksTest <- function(){
	x <- c(-1,1,-1,1,-1,1,-1,1,-1,1)
	y <- c(-5,7,-8,-6,8,4,-3,5,0,9)
	res <- HypothesisTests$ksTest(x,y,'two.sided')
	checkSame(res,data.frame(PValue = 0.1641,NumObsX = 10,NumObsY = 10,NumObsXDivNumObsY = 100))
	res <- HypothesisTests$ksTest(x[1:5],y,'two.sided')
	checkSame(res,data.frame(PValue = 0.3752,NumObsX = 5,NumObsY = 10,NumObsXDivNumObsY = 50))
	shouldBomb(HypothesisTests$ksTest(x,y,'junk'))
	shouldBomb(HypothesisTests$ksTest(zoo(x),zoo(y),'two.sided'))
}