library("GSFCore")
library("GSFAnalytics")

test.getRefLastNextBusinessDates <- function() {
    
    currentDate <- as.POSIXlt(as.character(Sys.Date()))

    holidayList <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")

    oneBackDate <- getFincadDateAdjust(
            startDate = currentDate,
            unit = "d",
            NumUnits = -1,
            holidayList = holidayList
    )
    twoBackDate <- getFincadDateAdjust(
            startDate = currentDate,
            unit = "d",
            NumUnits = -2,
            holidayList = holidayList
    )
    
    # case where no ref/last date inputs
    
    dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
    
    limitSup <-  as.POSIXlt(paste(currentDate," 23:59:59",sep = ""))
    limitInf <-  as.POSIXlt(paste(currentDate," 16:00:00",sep = ""))
    currentTime <- as.POSIXlt(as.character(Sys.time()))
    if(currentTime <= limitSup && currentTime >= limitInf && isBusinessDate(currentDate,holidayList)){
        checkSame(as.POSIXlt(as.character(oneBackDate)),dateList$refBusinessDate)
        checkSame(as.POSIXlt(as.character(currentDate)),dateList$lastBusinessDate)        
    }else{
        checkSame(as.POSIXlt(as.character(twoBackDate)),dateList$refBusinessDate)
        checkSame(as.POSIXlt(as.character(oneBackDate)),dateList$lastBusinessDate)
    }
    checkSame(as.POSIXlt(as.character(getFincadDateAdjust(startDate = dateList$lastBusinessDate,unit = "d",NumUnits = 1,holidayList = holidayList))),dateList$nextBusinessDate)
    
    # case where last given
    
    dateList <- getRefLastNextBusinessDates(lastBusinessDate = "2008-01-02",holidaySource = "financialcalendar", financialCenter = "nyb")
    checkSame(as.POSIXlt("2007-12-31"),dateList$refBusinessDate)
    checkSame(as.POSIXlt("2008-01-02"),dateList$lastBusinessDate)
    checkSame(as.POSIXlt("2008-01-03"),dateList$nextBusinessDate)
    
    # case week end

    dateList <- getRefLastNextBusinessDates(lastBusinessDate = "2008-01-07",holidaySource = "financialcalendar", financialCenter = "nyb")
    checkSame(as.POSIXlt("2008-01-04"),dateList$refBusinessDate)
    checkSame(as.POSIXlt("2008-01-07"),dateList$lastBusinessDate)    
    checkSame(as.POSIXlt("2008-01-08"),dateList$nextBusinessDate)    
    
    # ShouldBomb
    
    shouldBomb(getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb",switchTime = TRUE))
}

test.isBusinessDate <- function() {
    holidayList <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
    
    checkSame(isBusinessDate("2008-01-01",holidayList),FALSE)
    checkSame(isBusinessDate("2008-01-02",holidayList),TRUE)
    checkSame(isBusinessDate("2008-01-03",holidayList),TRUE)
    checkSame(isBusinessDate("2008-01-04",holidayList),TRUE)
    checkSame(isBusinessDate("2008-01-05",holidayList),FALSE)    
    checkSame(isBusinessDate("2008-01-06",holidayList),FALSE)
    checkSame(isBusinessDate("2008-01-07",holidayList),TRUE)
}
