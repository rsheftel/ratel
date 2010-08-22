## Test file for the IRSTri object
library(QFFixedIncome)

test.taylorPriceChange <- function(){

    calculator <- IRSTri()

    result <- calculator$taylorPriceChange(
        rate =  4.97038,
        rateChange =  -0.03752,
        settlementDate = "2007-11-06",
        maturityDate = "2017-11-06",
        freq = 2,
        acc = 4
    )

    checkEquals(result, list(priceChange = 0.293381544092703,dv01 = 7.805496654767127,convexity = 0.7377864795284634))
}

test.getAccrual <- function(){

    calculator <- IRSTri()

    result <- calculator$getAccrual(
        rate = 5.0079,
        start = "2007-11-05",
        end = "2007-11-06",
        acc = 4
    )

    checkEquals(result,  0.01391083333333333)
}

test.IRSTri <- function(){
    calculator <- IRSTri()
    checkEquals(calculator$.linlast, 2)
    checkEquals(calculator$.exdays, 0)
    checkEquals(calculator$.principal, 100)
    checkEquals(calculator$.holidaySource, "financialcalendar")
    checkEquals(calculator$.triSource, "internal")
    tsdb <- calculator$.tsdb
    needs(tsdb = "TimeSeriesDB?")
}

test.getCurves <- function(){
    calculator <- IRSTri()

    result <- calculator$getCurves("2008-01-16","2008-01-17","usd","irs","libor",TermStructure$irs,TermStructure$libor,"internal","internal","15:00:00","15:00:00")

    fixedCurve <- getTermStructureForTimeSeries("irs_usd_rate_tenor_mid",TermStructure$irs,"internal","2008-01-16","2008-01-17")
    floatCurve <- getTermStructureForTimeSeries("libor_usd_rate_tenor",TermStructure$libor,"internal","2008-01-16","2008-01-17")
    fixedCurve <- make.zoo.daily(fixedCurve, "15:00:00")
    floatCurve <- make.zoo.daily(floatCurve, "15:00:00")

    checkEquals(result,list(fixedCurve = fixedCurve,floatCurve = floatCurve))
}

test.cleanCurves <- function(){
    calculator <- IRSTri()

    curveList <- calculator$getCurves("2008-01-16","2008-01-17","usd","irs","libor",TermStructure$irs,TermStructure$libor,"internal","internal","15:00:00","15:00:00")
    curveListRef <- curveList

    # no change made
    result <- calculator$cleanCurves(curveList,"5y","3m")
    target <- list(fixedCurve = curveList$fixedCurve,floatCurve = curveList$floatCurve)

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))

    # no float on the first date
    curveList <- curveListRef
    curveList$floatCurve[1,"3m"] <- NA
    result <- calculator$cleanCurves(curveList,"5y","3m")
    target <- list(fixedCurve = curveList$fixedCurve[-1,],floatCurve = curveList$floatCurve[-1,])

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))

    # no fixed tenor on the first date
    curveList <- curveListRef
    curveList$fixedCurve[1,"5y"] <- NA
    result <- calculator$cleanCurves(curveList,"5y","3m")
    target <- list(fixedCurve = curveList$fixedCurve[-1,],floatCurve = curveList$floatCurve[-1,])

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))

    # no fixed tenor on the second date
    curveList <- curveListRef
    curveList$fixedCurve[2,"5y"] <- NA
    result <- calculator$cleanCurves(curveList,"5y","3m")
    target <- list(fixedCurve = curveList$fixedCurve[-2,],floatCurve = curveList$floatCurve[-2,])

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))

    # no float tenor on the second date

    curveList <- curveListRef
    curveList$floatCurve[2,"3m"] <- NA
    result <- calculator$cleanCurves(curveList,"5y","3m")
    curveList$floatCurve[2,] <- curveList$floatCurve[1,]
    target <- list(fixedCurve = curveList$fixedCurve,floatCurve = curveList$floatCurve)

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))

    # extra float curve

    curveList <- calculator$getCurves("2008-01-16","2008-01-18","usd","irs","libor",TermStructure$irs,TermStructure$libor,"internal","internal","15:00:00","15:00:00")
    curveListRef <- curveList
    curveList$fixedCurve <- curveList$fixedCurve[-2,]
    result <- calculator$cleanCurves(curveList,"5y","3m")
    target <- list(fixedCurve = curveList$fixedCurve[-2,],floatCurve = curveList$floatCurve[-2,])

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))

    # extra fixed curve

    curveList <- curveListRef
    curveList$floatCurve <- curveList$floatCurve[-2,]
    result <- calculator$cleanCurves(curveList,"5y","3m")
    curveList <- curveListRef
    curveList$floatCurve[2,] <- curveList$floatCurve[1,]
    target <- list(fixedCurve = curveList$fixedCurve,floatCurve = curveList$floatCurve)

    assert(index(result$fixedCurve)==index(target$fixedCurve))
    assert(all(target$fixedCurve==result$fixedCurve))
    assert(index(result$floatCurve)==index(target$floatCurve))
    assert(all(target$floatCurve==result$floatCurve))
}

test.run.taylor <- function(){

    # several dates

    calculator <- IRSTri()

    result <- IRSTri$run(
        startDate = "2007-11-05",
        endDate = "2007-11-09",
        ccy = "usd",
        financialCenterFixed = "nyb",financialCenterFloat = "nyb",
        tenorFixed = "10y",tenorFloat = "3m",
        instrumentFixed = "irs",instrumentFloat = "libor",
        sourceFixed = "internal",sourceFloat = "internal",
        timeStampFixed = "15:00:00",timeStampFloat = "15:00:00",
        termStructureFixed = TermStructure$irs,termStructureFloat = TermStructure$libor,
        unitSettFixed = "d",numUnitsSettFixed = 2,
        unitSettFloat = "d",numUnitsSettFloat = 2,
        freqFixed = 2,accFixed = 4,
        freqFloat = 4,accFloat = 2,
        updateTSDB = FALSE
    )

    dates <- c("2007-11-06","2007-11-07","2007-11-08","2007-11-09")
    dates <- as.POSIXct(paste(dates,"15:00:00",sep = " "))
    target <- getZooDataFrame(zoo(c(-0.38328350837002084, -0.01856586336716216, 0.31939568835311161, 0.48226993727610978),dates))

    checkEquals(result$zooTriDaily, target)

    dates <- c("2007-11-06","2007-11-07","2007-11-08","2007-11-09")
    dates <- as.POSIXct(paste(dates,"15:00:00",sep = " "))
    target <- getZooDataFrame(zoo(c(7.53929604823868, 7.52086308549050, 7.51992106601993, 7.53473675952874),dates))

    checkEquals(result$zooDv01, target)

    dates <- c("2007-11-06","2007-11-07","2007-11-08","2007-11-09")
    dates <- as.POSIXct(paste(dates,"15:00:00",sep = " "))
    target <- getZooDataFrame(zoo(c(0.7344625277811172, 0.7319317095068272, 0.7318025474247573, 0.7338360433203623),dates))

    checkEquals(result$zooConvexity, target)

    # one date

    calculator <- IRSTri()

    result <- IRSTri$run(
        startDate = "2007-11-05",
        endDate = "2007-11-06",
        ccy = "usd",
        financialCenterFixed = "nyb",financialCenterFloat = "nyb",
        tenorFixed = "10y",tenorFloat = "3m",
        instrumentFixed = "irs",instrumentFloat = "libor",
        sourceFixed = "internal",sourceFloat = "internal",
        timeStampFixed = "15:00:00",timeStampFloat = "15:00:00",
        termStructureFixed = TermStructure$irs,termStructureFloat = TermStructure$libor,
        unitSettFixed = "d",numUnitsSettFixed = 2,
        unitSettFloat = "d",numUnitsSettFloat = 2,
        freqFixed = 2,accFixed = 4,
        freqFloat = 4,accFloat = 2,
        updateTSDB = FALSE
    )

    dates <- c("2007-11-06")
    dates <- as.POSIXct(paste(dates,"15:00:00",sep = " "))
    target <- getZooDataFrame(zoo(c(-0.38328350837002084),dates))

    checkEquals(result$zooTriDaily, target)

    dates <- c("2007-11-06")
    dates <- as.POSIXct(paste(dates,"15:00:00",sep = " "))
    target <- getZooDataFrame(zoo(c(7.53929604823868),dates))

    checkEquals(result$zooDv01, target)

    dates <- c("2007-11-06")
    dates <- as.POSIXct(paste(dates,"15:00:00",sep = " "))
    target <- getZooDataFrame(zoo(c(0.7344625277811172),dates))

    checkEquals(result$zooConvexity, target)
}

test.getDailyZooSeries <- function(){

    calculator <- IRSTri()

    curveList <- calculator$getCurves("2007-11-05","2007-11-09","usd","irs","libor",TermStructure$irs,TermStructure$libor,"internal","internal","15:00:00","15:00:00")
    curve <- curveList$fixedCurve
    tenor <- "5y"
    termStructure <- TermStructure$irs
    unitSett <- "d"
    numUnitsSett <- 2
    freq <- 2
    acc <- 4
    holidayList <- HolidayDataLoader$getHolidaysToTenor("financialcalendar","nyb",tenorNumeric = 1,startDate = "2007-11-06",endDate = "2007-11-07")

    result <- calculator$getDailyZooSeries(curve,tenor,termStructure,unitSett,numUnitsSett,freq,acc,holidayList)

    dates <- c("2007-11-06","2007-11-07","2007-11-08","2007-11-09")
    target <- getZooDataFrame(zoo(c(-0.1731633267119933,0.1219893938392340,0.3911708768485915, 0.2685141540667279),dates))

    checkEquals(result$zooTriDaily, target)

    dates <- c("2007-11-06","2007-11-07","2007-11-08","2007-11-09")
    target <- getZooDataFrame(zoo(c(4.412785850841906, 4.407921376488237, 4.410708927972647, 4.419340782708034),dates))

    checkEquals(result$zooDv01, target)

    dates <- c("2007-11-06","2007-11-07","2007-11-08","2007-11-09")
    target <- getZooDataFrame(zoo(c(0.2289417781653981, 0.2285682787496768, 0.2287822928923050, 0.2294452918984439),dates))

    checkEquals(result$zooConvexity, target)

}


test.csvFileHardCheck <- function(){
	result <- IRSTri$run(
		startDate = '1993-03-30',
		endDate = '1994-01-01',
		ccy = "usd",
		financialCenterFixed = "nyb",financialCenterFloat = "lnb",
		tenorFixed = '30y',tenorFloat = "3m",
		instrumentFixed = "irs",instrumentFloat = "libor",
		sourceFixed = "internal",sourceFloat = "internal",
		timeStampFixed = "15:00:00",timeStampFloat = "15:00:00",
		termStructureFixed = TermStructure$irs,termStructureFloat = TermStructure$libor,
		unitSettFixed = "d",numUnitsSettFixed = 2,
		unitSettFloat = "d",numUnitsSettFloat = 2,
		freqFixed = 2,accFixed = 4,
		freqFloat = 4,accFloat = 2,
		updateTSDB = FALSE
	)
	target <- read.csv(system.file("testdata","test30yTriDv01Convex.csv", package = "QFFixedIncome"), sep = ",", header = FALSE)	
	checkZoo <- function(z1,z2){
		checkSame(getZooDataFrame(z1,'a'),getZooDataFrame(z2,'a'))		
	}
	zooTriDaily <- zoo(as.numeric(target[,2]),as.POSIXct(target[,1]))
	zooDv01 <- zoo(as.numeric(target[,3]),as.POSIXct(target[,1]))
	zooConvexity <- zoo(as.numeric(target[,4]),as.POSIXct(target[,1]))
	checkZoo(result$zooTriDaily,zooTriDaily)
	checkZoo(result$zooDv01,zooDv01)
	checkZoo(result$zooConvexity,zooConvexity)
}