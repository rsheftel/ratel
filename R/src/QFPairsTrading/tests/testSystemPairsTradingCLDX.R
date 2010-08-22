## Test file for the SystemPairsTradingCLDX object
library(QFPairsTrading)

pairName <- "DXCL"
dataTimeStamp <- "14:30:00"
this <- SystemPairsTradingCLDX(pairName = "DXCL",dataTimeStamp = dataTimeStamp)

version <- "1.0"
transformationName <- "rolling_regression"
analyticsSource <- "internal"
principal <- 0
strategyPath <- "Market Systems/Linked Market Systems/FX/StrategyCLDX/"

window = 20
tcLead = 10
tcLag = 5
updateTSDB = FALSE
generatePDF = FALSE

holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(Sys.time(),"d",-25,holidays)

test.SystemPairsTradingCLDX.constructor <- function(){
    checkSame(this$.systemHelper$.pairName,pairName)
    checkSame(this$.leadMarket,"crudeFuturesMarketValue")
    checkSame(this$.lagMarket,"dollarFuturesMarketValue")    
    checkSame(this$.systemHelper$.version,version)
    checkSame(this$.systemHelper$.transformationName,transformationName)
    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
    checkSame(this$.systemHelper$.principal,principal)
    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"DXCL/PDF/"))                      
    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"DXCL/STO/"))          
    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"DXCL/"))                       
    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
    assert("SystemPairsTrading" %in% class(this$.systemHelper))
    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
    shouldBomb(SystemPairsTradingCLDX(pairName = "junk"))
}