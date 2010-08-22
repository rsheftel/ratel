library(QFPortfolio)

source(system.file("testHelper.r", package = "STO"))

sto <- STO(stoDirectory(), "SimpleSTOTemplate")
curve.cube <- sto$curves()
curves <- lapply(sto$msivs(), function(msiv) curve.cube$curve(msiv, 1))

testWeightedCurves <- function() {
    wc <- WeightedCurves(curves, c(1,1,1))
    aggregate <- wc$curve()
    checkSameLooking(aggregate$pnl(), c(112, 222, -334))
    checkTrue(all(is.na(aggregate$position())))

    shouldBombMatching(WeightedCurves(curves, c(1, 1)), "expected length of items was 2 but got 3")
}

test.scaledAndUnscaledCurves <- function(){
	wc <- WeightedCurves(curves, c(2,3,4))
	sc <- wc$scaledCurves()
	uc <- wc$unscaledCurves()
	
	checkSame(sc[[1]]$pnl(), uc[[1]]$pnl() * 2)
	checkSame(sc[[2]]$pnl(), uc[[2]]$pnl() * 3)
	checkSame(sc[[3]]$pnl(), uc[[3]]$pnl() * 4)
}

testWeightedCurvesWithTime <- function() {
    wc <- WeightedCurves(curves, Weights(zoo(matrix(1:6, nrow=2), order.by=as.POSIXct(c("2005-11-10", "2005-11-15")))))
    aggregate <- wc$curve()
    checkSameLooking(aggregate$pnl(), c(356, 710, -1400))
}

testWeights <- function() {
    w <- Weights(c(1,2,3))
    dates <- as.POSIXct(c("2008-01-01", "2008-02-01", "2008-03-01"))
    input <- list(
        zoo(c(3,2,1), order.by = dates), # * 1     
        zoo(c(6,5,4), order.by = dates), # * 3 
        zoo(c(9,8,7), order.by = dates)  # * 5
    )
    expected <- list(
        zoo(c(3,2,1), order.by = dates),
        zoo(c(12,10,8), order.by = dates),
        zoo(c(27,24,21), order.by = dates)
    )
    checkSame(expected, w$scale(input))
    
    w <- Weights(zoo(matrix(c(1:6), nrow = 2), order.by = dates[-2]))
    # 1 3 5  1/1/2008
    # 2 4 6  3/1/2008 
    
    expected <- list(
        zoo(c(3,2,2), order.by = dates),
        zoo(c(18,15,16), order.by = dates),
        zoo(c(45,40,42), order.by = dates)
    )
    checkSame(expected, w$scale(input))
    
}

test.scaledCurves <- function(){
	
}
