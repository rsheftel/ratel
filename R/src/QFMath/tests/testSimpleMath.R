library(QFMath)

test.roundToNearest <- function(){
	shouldBomb(roundToNearest(TRUE,1))
	shouldBomb(roundToNearest(1,TRUE))	
	
	checkSame(roundToNearest(3,0.25),3)
	checkSame(roundToNearest(3.1,0.25),3)
	checkSame(roundToNearest(3.125,0.25),3)	
	checkSame(roundToNearest(3.2,0.25),3.25)
	checkSame(roundToNearest(3.3,0.25),3.25)
	checkSame(roundToNearest(3.25,0.25),3.25)			
}

test.lowerPartialMoment <- function(){
	shouldBomb(lowerPartialMoment(c('a','b'),1,0))
	shouldBomb(lowerPartialMoment(1:10,'v',0))
	
	dataset <- -5:5
	checkSame(round(lowerPartialMoment(dataset,1,0),6),1.363636)
	checkSame(round(lowerPartialMoment(dataset,2,0),6),5)
	checkSame(round(lowerPartialMoment(dataset,1,3),6),3.272727)
	checkSame(round(lowerPartialMoment(dataset,2,-3),6),0.454545)
}

test.semiDeviation <- function(){
	shouldBomb(semiDeviation(1:10,0,'wrong'))
	checkSame(semiDeviation(-5:5,moment=2,0,'downside'), 3.3166247903554)
	checkSame(semiDeviation(10:-8,moment=2,2,'upside'), 5.04975246918104)
	checkSame(semiDeviation(10:-8,moment=1,0,'upside'), 5.5)
}