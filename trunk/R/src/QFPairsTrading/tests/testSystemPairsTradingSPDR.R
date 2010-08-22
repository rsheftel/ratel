## Test file for the SystemPairsTradingSPDR object
library(QFPairsTrading)

#this <- SystemPairsTradingSPDR(pairName = "XLEXLF",dataTimeStamp = "16:00:00")
#
#pairName <- "XLEXLF"
#version <- "1.0"
#transformationName <- "rolling_regression"
#analyticsSource <- "internal"
#dataTimeStamp = "16:00:00"
#principal <- 0
#strategyPath <- "Market Systems/Linked Market Systems/Equities/StrategySPDR/"
#
#window = 20
#updateTSDB = FALSE
#updateASCII = FALSE
#generatePDF = FALSE
#
#date <- as.POSIXct(trunc(Sys.time(), "days"))
#holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
#startDate <- getFincadDateAdjust(date,"d",-25,holidays)
#
#test.SystemPairsTradingSPDR.constructor <- function(){    
#    checkSame(this$.systemHelper$.pairName,pairName)
#    checkSame(this$.systemHelper$.version,version)
#    checkSame(this$.systemHelper$.transformationName,transformationName)
#    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
#    checkSame(this$.systemHelper$.dataTimeStamp,dataTimeStamp)
#    checkSame(this$.systemHelper$.principal,principal)
#    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"XLEXLF/PDF/"))           
#    checkSame(this$.systemHelper$.asciiPath,squish(dataDirectory(),strategyPath,"XLEXLF/ASCII/"))           
#    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"XLEXLF/STO/"))           
#    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"XLEXLF/"))                       
#    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
#    assert("SystemPairsTrading" %in% class(this$.systemHelper))
#    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
#}
#
#test.SystemPairsTradingSPDR.getData <- function(){
#
#    colNames <- c("XLF.tri","XLF.close","XLE.tri","XLE.close")
#    data <- this$getData(startDate = startDate)
#    checkShape(data[NROW(data),], 1, 4, colnames = colNames)
#    
#    shouldBomb(this$getData(startDate = TRUE))
#}
#test.SystemPairsTradingSPDR.getTC <- function(){
#	checkSame(SystemPairsTradingSPDR$getTC(),0.01)
#}
#
#test.SystemPairsTradingSPDR.getSecurityId <- function(){
#	checkSame(SystemPairsTradingSPDR$getSecurityId("XLE"),110011)
#	checkSame(SystemPairsTradingSPDR$getSecurityId("XLB"),110007)
#}
#
#
