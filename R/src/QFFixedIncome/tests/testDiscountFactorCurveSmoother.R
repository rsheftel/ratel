## Test file for the DiscountFactorCurveSmoother class
library(QFFixedIncome)

test.DiscountFactorCurveSmoother.Constructor <- function()
{
    dfCurve <- read.csv(system.file("testdata","testfileSwapCurveBuilderCalcDiscountFactors.csv", package = "QFFixedIncome"), sep = ",", header = FALSE)
    colnames(dfCurve) <- c("date","df","spot")
    dfCurve <- dfCurve[,-3]
    
    sm <- DiscountFactorCurveSmoother(dfCurve = dfCurve)

    checkEquals(sm$.dfCurve,dfCurve)
    checkEquals(sm$.smoothingFreq,3)
    checkEquals(sm$.method,1)
    checkEquals(sm$.acc,2)
    checkEquals(sm$.freq,1)
    
    result <- sm$getSmoothedDiscountFactorCurve()

    target <- read.csv(system.file("testdata","testfileDiscountFactorCurveSmoother.csv", package = "QFFixedIncome"), sep = ",", header = FALSE)
    colnames(target) <- c("date","df","spot")

    target[,1] <- as.POSIXct(target[,1])
    checkEquals(result,target)
}