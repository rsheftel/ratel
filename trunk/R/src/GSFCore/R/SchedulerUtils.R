isBusinessDate <- function(date,holidayList){
    nextBusinessDate <- getFincadDateAdjust(startDate = date,unit = "d",NumUnits = 1,holidayList = holidayList)
    date == getFincadDateAdjust(startDate = nextBusinessDate,unit = "d",NumUnits = -1,holidayList = holidayList)
}

getRefLastNextBusinessDates <- function(lastBusinessDate = NULL, holidaySource = "financialcalendar", financialCenter = "nyb",switchTime = "16:00:00"){

    # This can be used for Rscript jobs, the function returns the last business date (t-1) and the ref business date (t-2)
    # for a given holiday calendar
    
    # If the function runs between 16pm and midnight, the lastBusinessDate is considered to be today (only if today is a business date)

    todayDate <- as.POSIXlt(as.character(Sys.Date()))
    if(!is.null(lastBusinessDate))
        todayDate <- lastBusinessDate
    needs(switchTime = "character")

    holidayList <- HolidayDataLoader$getHolidays(
        startDate = as.Date(todayDate) - 90,
        endDate = as.Date(todayDate) + 90,
        source = holidaySource,financialCenter = financialCenter
    )

    if(is.null(lastBusinessDate)){
        lastBusinessDate <- getFincadDateAdjust(
            startDate = todayDate,
            unit = "d",
            NumUnits = -1,
            holidayList = holidayList
        )

        limitSup <-  as.POSIXlt(paste(todayDate," 23:59:59",sep = ""))
        limitInf <-  as.POSIXlt(paste(todayDate,switchTime,sep = " "))
        currentTime <- as.POSIXlt(as.character(Sys.time()))
        if(currentTime <= limitSup && currentTime >= limitInf && isBusinessDate(todayDate,holidayList)){        
            lastBusinessDate <- as.POSIXlt(as.character(todayDate))
        }else{
            lastBusinessDate <- as.POSIXlt(as.character(lastBusinessDate))
        }
    }else{
        lastBusinessDate <- as.POSIXlt(as.character(lastBusinessDate))
    }
    
    refBusinessDate <- getFincadDateAdjust(
        startDate = lastBusinessDate,
        unit = "d",
        NumUnits = -1,
        holidayList = holidayList
    )
    refBusinessDate <- as.POSIXlt(as.character(refBusinessDate))
    
    nextBusinessDate <- getFincadDateAdjust(
        startDate = lastBusinessDate,
        unit = "d",
        NumUnits = 1,
        holidayList = holidayList
    )
    nextBusinessDate <- as.POSIXlt(as.character(nextBusinessDate))

    assert(lastBusinessDate <= todayDate)
    assert(lastBusinessDate < nextBusinessDate)
    assert(refBusinessDate < lastBusinessDate)

    cat("\n----------------------------------------\n")
    cat("\nNext Business date: ",as.character(nextBusinessDate),"\n",sep = "")
    cat("\nLast Business date: ",as.character(lastBusinessDate),"\n",sep = "")
    cat("\nRef Business date: ",as.character(refBusinessDate),"\n",sep = "")
    cat("\n----------------------------------------\n")

    return(list(nextBusinessDate = nextBusinessDate,lastBusinessDate = lastBusinessDate, refBusinessDate = refBusinessDate))
}
