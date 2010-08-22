## Test file for the LiqInjSectorETFs object
library(QFPairsTrading)

source(system.file("testHelper.r", package = "QFPairsTrading"))

this <- LiqInjSectorETFs(pairName = "XLEXLF")

pairName <- "XLEXLF"
version <- "1.0"
transformationName <- "liqinj"
analyticsSource <- "internal"
principal <- 0
strategyPath <- "Market Systems/Linked Market Systems/Equities/LiqInjSectorETFs/"

window = 20
updateTSDB = FALSE
generatePDF = FALSE

date <- as.POSIXct(trunc(Sys.time(), "days"))
holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(date,"d",-25,holidays)

test.LiqInjSectorETFs.constructor <- function(){    
    checkSame(this$.systemHelper$.pairName,pairName)
    checkSame(this$.systemHelper$.version,version)
    checkSame(this$.systemHelper$.transformationName,transformationName)
    checkSame(this$.systemHelper$.analyticsSource,analyticsSource)
    checkSame(this$.systemHelper$.principal,principal)
    checkSame(this$.systemHelper$.pdfPath,squish(dataDirectory(),strategyPath,"XLEXLF/PDF/"))                         
    checkSame(this$.systemHelper$.stoPath,squish(dataDirectory(),strategyPath,"XLEXLF/STO/"))           
    checkSame(this$.systemHelper$.pairPath,squish(dataDirectory(),strategyPath,"XLEXLF/"))                       
    checkSame(this$.systemHelper$.strategyPath,squish(dataDirectory(),strategyPath))                           
    assert("SystemPairsTrading" %in% class(this$.systemHelper))
    assert("TimeSeriesDB" %in% class(this$.systemHelper$.tsdb))
}

test.LiqInjSectorETFs.getBidAsk <- function(){
	checkSame(LiqInjSectorETFs$getBidAsk(),0.02)
}

test.LiqInjSectorETFs.getSecurityId <- function(){
	checkSame(LiqInjSectorETFs$getSecurityId("XLE"),110011)
	checkSame(LiqInjSectorETFs$getSecurityId("XLB"),110007)
}

test.LiqInjSectorETFs.main <- function(){
	
	underlyingData <- getTestData('xle.xlf.data.csv')
	betaData <- getTestData('xle.xlf.beta.20.no.intercept.percent.data.csv')
	testResult <- getTestData('testLiqInjSectorETFs.csv')
		
	# short term
	result <- this$runShortTerm(startDate,window = window,updateTSDB = FALSE,generatePDF = FALSE,
		underlyingData = Range('2008-12-16','2009-01-22')$cut(underlyingData)
	)

	checkZoos(result$beta,Range('2009-01-15','2009-01-22')$cut(betaData))
	
	# long term
	result <- this$runLongTerm(window = 130,betaBand = 0.2,updateTSDB = FALSE,generatePDF = FALSE,
		underlyingData = Range('2008-01-22','2009-01-22')$cut(underlyingData),
		shortBeta = Range('2008-01-22','2009-01-22')$cut(betaData)
	)
	checkZoos(result$combBeta,testResult[,1])
	checkZoos(result$hedge,testResult[,2])
	checkZoos(result$tc,testResult[,3])
	checkZoos(result$tri,testResult[,4])
	checkZoos(result$dailyTri,testResult[-1,5])
}
