## Test file for the SystemPairsTradingSectorETFs object
library(QFPairsTrading)

this <- SystemPairsTradingSectorETFs(pairName = "XLEXLF")

pairName <- "XLEXLF"
version <- "1.1"
transformationName <- "rolling_regression"
analyticsSource <- "internal"
principal <- 0
strategyPath <- "Market Systems/Linked Market Systems/Equities/StrategySectorETFs/"

window = 20
updateTSDB = FALSE
updateASCII = FALSE
generatePDF = FALSE

date <- as.POSIXct(trunc(Sys.time(), "days"))
holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(date,"d",-25,holidays)

test.SystemPairsTradingSectorETFs.constructor <- function(){    
    checkSame(this$.systemHelper$.pairName,pairName)
    checkSame(this$.systemHelper$.version,version)
    checkSame(this$.systemHelper$.transformationName,transformationName)
    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
    checkSame(this$.systemHelper$.principal,principal)
    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"XLEXLF/PDF/"))           
    checkSame(this$.systemHelper$.asciiPath,squish(dataDirectory(),strategyPath,"XLEXLF/ASCII/"))           
    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"XLEXLF/STO/"))           
    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"XLEXLF/"))                       
    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
    assert("SystemPairsTrading" %in% class(this$.systemHelper))
    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
}

test.SystemPairsTradingSectorETFs.getData <- function(){
    colNames <- c("XLF.tri","XLF.close","XLE.tri","XLE.close")
    data <- this$getData(startDate = startDate)
    checkShape(data[NROW(data),], 1, 4, colnames = colNames)
    
    shouldBomb(this$getData(startDate = TRUE))
}

test.SystemPairsTradingSectorETFs.getTC <- function(){
	checkSame(SystemPairsTradingSectorETFs$getTC(),0.02)
}

test.SystemPairsTradingSectorETFs.getSecurityId <- function(){
	checkSame(SystemPairsTradingSectorETFs$getSecurityId("XLE"),110011)
	checkSame(SystemPairsTradingSectorETFs$getSecurityId("XLB"),110007)
}


