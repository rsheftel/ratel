## Test file for the CDS object

library(QFCredit)

testDTDCalculator <- function()
{   
    # tests good inputs
        
    dtdCalc <- DTDCalculator()
    
    output <- dtdCalc$runDTD(0.5,1,10000,1000,5,.1,5,0.05,FALSE)
    output <- round(output,2)
    
    checkEquals(5.4, output[1])
    checkEquals(15000, output[2])
    checkEquals(0.03, output[3])
    
    checkEquals(10000, dtdCalc$.liability)
    checkEquals(1000, dtdCalc$.shares)
    checkEquals(5, dtdCalc$.s)
    checkEquals(5000, dtdCalc$.mktCap)
    checkEquals(0.1, dtdCalc$.sigmaE)
    checkEquals(5, dtdCalc$.time)
    checkEquals(.0494, round(dtdCalc$.rate, 4))
    checkEquals(0.0333, round(dtdCalc$.sigmaA, 4))
    checkEquals(15000, round(dtdCalc$.a,0))
    checkEquals(12801, round(dtdCalc$.fvLiab,0))
    checkEquals(5.48, round(dtdCalc$.d1, 2))
    checkEquals(5.40, round(dtdCalc$.d2,2))  
    
    dtdCalc <- DTDCalculator()
    
    output <- dtdCalc$runDTD(0.5,1,10000,1000,5,.3,5,0.05,FALSE)
    output <- round(output,2)
    
    checkEquals(1.62, output[1])
    checkEquals(14952.81, output[2])
    checkEquals(0.1, output[3])
    
    checkEquals(10000, dtdCalc$.liability)
    checkEquals(FALSE, dtdCalc$.useConstrOptim)
    checkEquals(1000, dtdCalc$.shares)
    checkEquals(5, dtdCalc$.s)
    checkEquals(5000, dtdCalc$.mktCap)
    checkEquals(0.3, dtdCalc$.sigmaE)
    checkEquals(5, dtdCalc$.time)
    checkEquals(.0494, round(dtdCalc$.rate, 4))
    checkEquals(.1036, round(dtdCalc$.sigmaA, 4))
    checkEquals(14953, round(dtdCalc$.a,0))
    checkEquals(12801, round(dtdCalc$.fvLiab,0))
    checkEquals(1.85, round(dtdCalc$.d1, 2))
    checkEquals(1.62, round(dtdCalc$.d2,2))  

    rm(dtdCalc)
}
