## Test file for the ResidualTest object

library(QFMath)

testResidualTest <- function()
{
    # test bad inputs
    
    testSample <- ResidualTest()
    
    residuals <- rnorm(100, mean=0, sd=1)

    shouldBomb(testSample$augmentedDickeyFuller())
    shouldBomb(testSample$augmentedDickeyFuller(residuals = TRUE,lags = 5,k = 1,pValueThreshold = 0.05))
    shouldBomb(testSample$augmentedDickeyFuller(residuals = residuals,lags = -1,k = 1,pValueThreshold = 0.05))
    shouldBomb(testSample$augmentedDickeyFuller(residuals = residuals,lags = 5,k = 0,pValueThreshold = 0.05))
    shouldBomb(testSample$augmentedDickeyFuller(residuals = residuals,lags = 5,k = 1,pValueThreshold = 0.5))


    # test good inputs

    testSample <- ResidualTest()
    
    residuals <- rnorm(100, mean=0, sd=1)
    
    checkEquals(list(integer = 7,character = "1%"),
    	    testSample$augmentedDickeyFuller(residuals = residuals,lags = 5,k = 1,pValueThreshold = 0.05))
    checkEquals(testSample$.testName,"Augmented Dickey Fuller Test (JB)")
    	    	
    rm(testSample)
}