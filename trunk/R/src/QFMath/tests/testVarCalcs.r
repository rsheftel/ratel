library(QFMath)


 testHistVar <- function() {
  
  list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10"))

  list.values.a <- c(1.2,2.5,3.3,4.7,5.0,6.1,7.3,8.3,9.2,2.1)
  list.values.b <- c(5.2,6.4,7.7,8.5,9.3,10.3,11.2,12.05,13.1,3.3)
  list.values.c <- c(3.005,4.8,5.2,6.3,7.0,8.9,9.9,10.6,11.6005,2.01)

  test.zoo <- zoo(data.frame(one=list.values.a,two=list.values.b,three=list.values.c),order.by=list.dates)

  Answer <- 19.395
  
  weights <- c(3.7,2,2.5)
  results <- histVaR(test.zoo,weights,alpha=0.1)
  checkEquals(round(results[[1]],3),Answer)
  
  weights <- weights/2
  results <- histVaR(test.zoo,weights,alpha=0.1)
  checkEquals(round(results[[1]],4),Answer/2)
}

testHistDownsideVaR <- function() {
	list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10","2008-01-11",
			"2008-01-12","2008-01-13","2008-01-14"))
	Answer <- -11
	list.values <- c(2,1,0,-1,-2,-3,-4,-5,-6,-7,-8,-9,-10,-11)
	
	weights <- c(1)
	test.zoo <- zoo(data.frame(one=list.values),order.by=list.dates)
	results <- histDownsideVaR(test.zoo,weights,alpha=0.1)
	checkEquals(round(results[[1]],3),Answer)
}

testExpectedShortFallVaR <- function() {
  list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10"))
  Answer <- 19.395
  list.values.a <- c(1.2,2.5,3.3,4.7,5.0,6.1,7.3,8.3,9.2,2.1)
  list.values.b <- c(5.2,6.4,7.7,8.5,9.3,10.3,11.2,12.05,13.1,3.3)
  list.values.c <- c(3.005,4.8,5.2,6.3,7.0,8.9,9.9,10.6,11.6005,2.01)
  weights <- c(3.7,2,2.5)
  test.zoo <- zoo(data.frame(one=list.values.a,two=list.values.b,three=list.values.c),order.by=list.dates)
  results <- expectedShortFallVaR(test.zoo,weights,alpha=0.1)
  checkEquals(round(results[[1]],3),Answer)
}

testExpectedShortFallDownsideVaR <- function() {
	list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10","2008-01-11",
			"2008-01-12","2008-01-13","2008-01-14"))
	Answer <- -11
	list.values <- c(2,1,0,-1,-2,-3,-4,-5,-6,-7,-8,-9,-10,-11)
	
	weights <- c(1)
	test.zoo <- zoo(data.frame(one=list.values),order.by=list.dates)
	results <- expectedShortFallDownsideVaR(test.zoo,weights,alpha=0.1)
	checkEquals(round(results[[1]],3),Answer)
}


testParametricVar <- function()
{
	list.dates <- as.POSIXct(c("2008-01-01","2008-01-02","2008-01-03","2008-01-04","2008-01-05","2008-01-06","2008-01-07","2008-01-08","2008-01-09","2008-01-10"))
	
	list.values.a <- c(1.2,2.5,3.3,4.7,5.0,6.1,7.3,8.3,9.2,2.1)
	list.values.b <- c(5.2,6.4,7.7,8.5,9.3,10.3,11.2,12.05,13.1,3.3)
	list.values.c <- c(3.005,4.8,5.2,6.3,7.0,8.9,9.9,10.6,11.6005,2.01)
	
	test.zoo <- zoo(data.frame(one=list.values.a,two=list.values.b,three=list.values.c),order.by=list.dates)
	weights <- weights <- c(3.7,2,2.5)
	totalZoo <- apply(t(t(test.zoo) * weights), 1, sum)
	s <- skewness(totalZoo)
	m <- mean(totalZoo)
	v <- sqrt(var(totalZoo))
	Answer <- round(as.numeric(m + 1/6 * s * v),3)
	results <- parametricVaR(test.zoo,weights,alpha=0.5)
	checkEquals(results[[1]],Answer)
}

testSkewness <- function() {
  list.values.a <- c(-5,-4,-3,-2,-1,0,1,2,3,4,5)
  Answer <- 0
  checkSame(round(as.numeric(skewness(list.values.a)),5),Answer)
  
  list.values.b <- c(-5,-4,-3,-2,-1,0,1,2,3,4,100)
  Answer <- 3.81577
  checkSame(round(as.numeric(skewness(list.values.b,method="fisher")),5),Answer)
  
  Answer <- 2.42674
  checkSame(round(as.numeric(skewness(list.values.b,method="moment")),5),Answer)
}

testKurtosis <- function() {
	list.values.a <- c(-5,-4,-3,-2,-1,0,1,2,3,4,5)
	Answer <- -1.52893
	checkSame(round(as.numeric(kurtosis(list.values.a)),5),Answer)
	
	list.values.b <- c(-5,-4,-3,-2,-1,0,1,2,3,4,100)
	Answer <- 4.39182
	checkSame(round(as.numeric(kurtosis(list.values.b)),5),Answer)
}

  