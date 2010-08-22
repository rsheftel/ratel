## Test file for the RegressionOutputs object
library(QFPairsTrading)

regressionDates <- c("2001-01-01","2001-01-02","2001-01-03")
nbFactors = 2

test.RegressionOutputs.constructor <- function(){
    this <- RegressionOutputs(regressionDates,nbFactors)
    checkEquals(this$.r2Adj,zoo(as.matrix(NA),as.POSIXct(regressionDates)))
    checkEquals(this$.pValues,zoo(matrix(NA,ncol = nbFactors),as.POSIXct(regressionDates)))
    shouldBomb(RegressionOutputs(TRUE,nbFactors))
    shouldBomb(RegressionOutputs(regressionDates,"bad"))
}

test.RegressionOutputs.storeOneDate <- function(){
    this <- RegressionOutputs(regressionDates,nbFactors)

    # check r2
    this$storeOneDate(1,".r2",0.5)
    checkEquals(this$.r2,zoo(as.matrix(c(0.5,NA,NA)),as.POSIXct(regressionDates)))
    shouldBomb(this$storeOneDate(1,".doesNotExist",0.5))
    shouldBomb(this$storeOneDate(4,".r2",0.5))

    # check coefficients
    this$storeOneDate(1,".coefficients",c(3,4))
    checkEquals(this$.coefficients,zoo(matrix(c(3,NA,NA,4,NA,NA),ncol = nbFactors),as.POSIXct(regressionDates)))
}
