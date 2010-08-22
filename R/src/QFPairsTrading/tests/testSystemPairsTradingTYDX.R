## Test file for the SystemPairsTradingTYDX object
library(QFPairsTrading)

pairName <- "DXTY"
this <- SystemPairsTradingTYDX(pairName = pairName)

version <- "1.0"
transformationName <- "rolling_regression"
analyticsSource <- "internal"
principal <- 0
strategyPath <- "Market Systems/Linked Market Systems/FX/StrategyTYDX/"

window = 20
tcLead = 15.625
tcLag = 5
updateTSDB = FALSE
updateASCII = FALSE
generatePDF = FALSE

holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(Sys.time(),"d",-25,holidays)

test.SystemPairsTradingTYDX.constructor <- function(){
    
    checkSame(this$.systemHelper$.pairName,pairName)
    checkSame(this$.leadMarket,"treasuryFuturesMarketValue")
    checkSame(this$.lagMarket,"dollarFuturesMarketValue")    
    checkSame(this$.systemHelper$.version,version)
    checkSame(this$.systemHelper$.transformationName,transformationName)
    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
    checkSame(this$.systemHelper$.principal,principal)
    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"DXTY/PDF/"))           
    checkSame(this$.systemHelper$.asciiPath,squish(dataDirectory(),strategyPath,"DXTY/ASCII/"))           
    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"DXTY/STO/"))           
    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"DXTY/"))                       
    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
    assert("SystemPairsTrading" %in% class(this$.systemHelper))
    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
    shouldBomb(SystemPairsTradingTYDX(pairName = "junk"))
}

test.SystemPairsTradingTYDX.getData <- function(){

    #colNames <- c("dollarFuturesMarketValue","treasuryFuturesMarketValue")
    #data <- this$getData(startDate = startDate)
    #checkShape(data[NROW(data),], 1, 2, colnames = colNames)
    
    #shouldBomb(this$getData(startDate = TRUE))
}

test.SystemPairsTradingTYDX.run <- function(){

    #shouldBomb(this$run(TRUE,20,1.25,1.25,FALSE,FALSE,TRUE))
    #shouldBomb(this$run(NULL,TRUE,1.25,1.25,FALSE,FALSE,TRUE))    
    #shouldBomb(this$run(NULL,20,TRUE,1.25,FALSE,FALSE,TRUE))    
    #shouldBomb(this$run(NULL,20,1.25,TRUE,FALSE,FALSE,TRUE))    
    #shouldBomb(this$run(NULL,20,1.25,1.25,"junk",FALSE,TRUE))    
    #shouldBomb(this$run(NULL,20,1.25,1.25,FALSE,"junk",TRUE))            
    #shouldBomb(this$run(NULL,20,1.25,1.25,FALSE,FALSE,"junk"))                
 
    #result <- this$run(startDate,window,tcLag,tcLead,updateTSDB,updateASCII,generatePDF)
    #checkShape(result$pairResult[NROW(result$pairResult),],1,16)
    #assert(class(result$tc) == "zoo" & NROW(result$tc) >= 1)    
    #assert(class(result$delta) == "zoo" & NROW(result$delta) >= 1)           
}