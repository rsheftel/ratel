## Test file for the SystemPairsTradingESIG object
library(QFPairsTrading)

this <- SystemPairsTradingESIG()

pairName <- "ESIG"
version <- "1.0"
transformationName <- "rolling_regression"
analyticsSource <- "internal"
dataTimeStamp <- "15:00:00"
principal <- 0
strategyPath <- "Market Systems/Linked Market Systems/Credit/StrategyESIG/"

window = 20
tcBps = 1.25
useCash = FALSE
updateTSDB = FALSE
updateASCII = FALSE
generatePDF = FALSE

date <- as.POSIXct(trunc(Sys.time(), "days"))
holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(date,"d",-25,holidays)

test.SystemPairsTradingESIG.constructor <- function(){
    
    checkSame(this$.systemHelper$.pairName,pairName)
    checkSame(this$.systemHelper$.version,version)
    checkSame(this$.systemHelper$.transformationName,transformationName)
    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
    checkSame(this$.systemHelper$.dataTimeStamp,dataTimeStamp)
    checkSame(this$.systemHelper$.principal,principal)
    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"ESIG/PDF/"))           
    checkSame(this$.systemHelper$.asciiPath,squish(dataDirectory(),strategyPath,"ESIG/ASCII/"))           
    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"ESIG/STO/"))           
    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"ESIG/"))                       
    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
    assert("SystemPairsTrading" %in% class(this$.systemHelper))
    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
}

test.SystemPairsTradingESIG.getData <- function(){

    colNames <- c("creditTri","creditSpread","creditDv01","equityFuturesMarketValue")
    # cash
    data.cash <- this$getData(startDate = startDate,useCash = TRUE)
    checkShape(data.cash[NROW(data.cash),], 1, 4, colnames = colNames)
    # CDX
    data.cdx <- this$getData(startDate = startDate,useCash = FALSE)
    checkShape(data.cdx[NROW(data.cdx),], 1, 4, colnames = colNames)
    checkSame(first(data.cash)==first(data.cdx),FALSE)
    
    shouldBomb(this$getData(startDate = TRUE,useCash = TRUE))
    shouldBomb(this$getData(startDate = startDate,useCash = "no"))
}

test.SystemPairsTradingESIG.run <- function(){

    shouldBomb(this$run(TRUE,20,1.25,FALSE,FALSE,TRUE))
    shouldBomb(this$run(NULL,TRUE,1.25,FALSE,FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,TRUE,FALSE,FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,1.25,"junk",FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,1.25,FALSE,"junk",TRUE))            
    shouldBomb(this$run(NULL,20,1.25,FALSE,FALSE,"junk"))                
 
    result <- this$run(startDate,window,tcBps,useCash,updateTSDB,updateASCII,generatePDF)
    checkShape(result$pairResult[NROW(result$pairResult),],1,16)
    assert(class(result$pScore) == "zoo" & NROW(result$pScore) >= 1)    
    assert(class(result$delta) == "zoo" & NROW(result$delta) >= 1)          
}