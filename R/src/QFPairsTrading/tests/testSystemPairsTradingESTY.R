## Test file for the SystemPairsTradingESTY object
library(QFPairsTrading)

this <- SystemPairsTradingESTY(dataTimeStamp = "16:15:00")

pairName <- "ESTY"
version <- "1.0"
transformationName <- "rolling_regression"
analyticsSource <- "internal"
dataTimeStamp <- "16:15:00"
principal <- 0
strategyPath <- "Market Systems/Linked Market Systems/Equities/StrategyESTY/"

window = 20
tcES = 12.5 # One tick 
tcTY = 15.7 # One tick
updateTSDB = FALSE
updateASCII = FALSE
generatePDF = FALSE

date <- as.POSIXct(trunc(Sys.time(), "days"))
holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(date,"d",-25,holidays)

test.SystemPairsTradingESTY.constructor <- function(){
    
    checkSame(this$.systemHelper$.pairName,pairName)
    checkSame(this$.systemHelper$.version,version)
    checkSame(this$.systemHelper$.transformationName,transformationName)
    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
    checkSame(this$.systemHelper$.dataTimeStamp,dataTimeStamp)
    checkSame(this$.systemHelper$.principal,principal)
    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"ESTY/PDF/"))           
    checkSame(this$.systemHelper$.asciiPath,squish(dataDirectory(),strategyPath,"ESTY/ASCII/"))           
    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"ESTY/STO/"))           
    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"ESTY/"))                       
    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
    assert("SystemPairsTrading" %in% class(this$.systemHelper))
    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
}

test.SystemPairsTradingESTY.getData <- function(){

    colNames <- c("equityFuturesMarketValue","treasuryFuturesMarketValue")

    data <- this$getData(startDate = startDate)
    checkShape(data[NROW(data),], 1, 2, colnames = colNames)
    
    shouldBomb(this$getData(startDate = TRUE))
}

test.SystemPairsTradingESTY.run <- function(){

    shouldBomb(this$run(TRUE,20,1.25,1.25,FALSE,FALSE,TRUE))
    shouldBomb(this$run(NULL,TRUE,1.25,1.25,FALSE,FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,TRUE,1.25,FALSE,FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,1.25,TRUE,FALSE,FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,1.25,1.25,"junk",FALSE,TRUE))    
    shouldBomb(this$run(NULL,20,1.25,1.25,FALSE,"junk",TRUE))            
    shouldBomb(this$run(NULL,20,1.25,1.25,FALSE,FALSE,"junk"))                
 
    #result <- this$run(startDate,window,tcES,tcTY,updateTSDB,updateASCII,generatePDF)
    #checkShape(result$pairResult[NROW(result$pairResult),],1,16)
    #assert(class(result$tc) == "zoo" & NROW(result$tc) >= 1)    
    #assert(class(result$delta) == "zoo" & NROW(result$delta) >= 1)         
}          


