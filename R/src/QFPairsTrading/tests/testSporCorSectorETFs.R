## Test file for the SporCorSectorETFs object
library(QFPairsTrading)

source(system.file("testHelper.r", package = "QFPairsTrading"))

this <- SporCorSectorETFs(pairName = "XLEXLF")

pairName <- "XLEXLF"
version <- "1.0"
transformationName <- "sporcor"
analyticsSource <- "internal"
principal <- 0
strategyPath <- "Market Systems/General Market Systems/SporCor/"

window = 20
updateTSDB = FALSE
generatePDF = FALSE

date <- as.POSIXct(trunc(Sys.time(), "days"))
holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(date,"d",-25,holidays)

test.SporCorSectorETFs.constructor <- function(){    
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

test.SporCorSectorETFs.getBidAsk <- function(){
	checkSame(SporCorSectorETFs$getBidAsk(),0.02)
}

test.SporCorSectorETFs.getSecurityId <- function(){
	checkSame(SporCorSectorETFs$getSecurityId("XLE"),110011)
	checkSame(SporCorSectorETFs$getSecurityId("XLB"),110007)
}

test.SporCorSectorETFs.main <- function(){
	
	underlyingData <- getTestData('xle.xlf.data.csv')
	betaData <- getTestData('xle.xlf.beta.20.no.intercept.data.csv')
	testResult <- getTestData('testSporCorSectorETFs.csv')
		
	# short term
	result <- this$runShortTerm(startDate,window = window,updateTSDB = FALSE,generatePDF = FALSE,
		underlyingData = Range('2008-12-16','2009-01-22')$cut(underlyingData)
	)
	values <- c(2.72557174357707, 2.58628159224365,1.91454945759099,2.04505380898899, 2.03229365007405)
	dates <- as.POSIXct(c('2009-01-15','2009-01-16','2009-01-20','2009-01-21','2009-01-22')) 
	checkZoos(result$beta,zoo(values,dates))
	
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
