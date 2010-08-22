## Test file for the SystemPairsTrading object
library(QFPairsTrading)

pairName <- "ESIG"
version <- "1.0"
transformationName <- "rolling_regression"
analyticsSource <- "internal"
dataTimeStamp <- "15:00:00"
principal <- 0
strategyPath <- "temp/"
this <- SystemPairsTrading(pairName,transformationName,version,strategyPath)

test.SystemPairsTrading.constructor <- function(){
    checkSame(this$.pairName,pairName)
    checkSame(this$.version,version)
    checkSame(this$.transformationName,transformationName)
    checkSame(this$.analyticsSource,analyticsSource)
    checkSame(this$.dataTimeStamp,dataTimeStamp)
    checkSame(this$.principal,principal)                    
    checkSame(this$.strategyPath,squish(dataDirectory(),strategyPath))
    checkSame(this$.pairPath,squish(dataDirectory(),strategyPath,"ESIG/"))    
    checkSame(this$.pdfPath,squish(dataDirectory(),strategyPath,"ESIG/PDF/"))
    checkSame(this$.asciiPath,squish(dataDirectory(),strategyPath,"ESIG/ASCII/"))    
    checkSame(this$.stoPath,squish(dataDirectory(),strategyPath,"ESIG/STO/"))        
    assert("TimeSeriesDB" %in% class(this$.tsdb))
    
    shouldBomb(SystemPairsTrading(TRUE,transformationName,version,strategyPath))
    shouldBomb(SystemPairsTrading(pairName,TRUE,version,strategyPath))
    shouldBomb(SystemPairsTrading(pairName,transformationName,TRUE,strategyPath))    
    shouldBomb(SystemPairsTrading(pairName,transformationName,version,TRUE))
    shouldBomb(SystemPairsTrading(pairName,transformationName,version,strategyPath,TRUE))
    shouldBomb(SystemPairsTrading(pairName,transformationName,version,strategyPath,"internal",TRUE))
    shouldBomb(SystemPairsTrading(pairName,transformationName,version,strategyPath,"internal","15:00:00","junk"))                        
    
    destroyDirectory(squish(dataDirectory(),"temp/ESIG"),TRUE)
}

test.SystemPairsTrading.getPairMarketName <- function(){
    pairName <- "ESIG"
    version <- "1.0"
    result <- "PTT10.ESIG"
    checkSame(result,this$getPairMarketName(pairName,version)) 
}

test.SystemPairsTrading.Undefined.Methods <- function(){
    shouldBomb(this$getData())
}