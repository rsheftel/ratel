getFincadDateAdjust <- function(startDate, unit, NumUnits,holidayList,adjRule = ifelse(NumUnits >= 0, 2, 3),...)
{      
    acceptableList <- c("d","w","m","y")
    assert(any(unit == acceptableList),paste(unit," is not an acceptable increment"))
    assert(class(NumUnits)=="numeric")
    
    if (unit == "d") AUnits = 1
    if (unit == "w") AUnits = 3
    if (unit == "m") AUnits = 4
    if (unit == "y") AUnits = 5

    # Note:  This is too slow if you supply the complete holiday list.
    # Optimization - run once without holiday list.  
    # Then, if counting "market days", use the holidays after the start, and up to one year after the no-holiday list.
    # If counting anything else, use this no holiday date as the start and a month later as the end.
    # If, for some bizarre reason, the adjusted date comes back later than this "end point", print a warning and use the whole list.

    lookupAdjustedDate <- function(holidays){
        fincad.to.POSIXct(fincad("aaDateAdjust",
            d_to_adj = startDate,
            num_units = NumUnits,
            adj_units = AUnits,
            d_rul = adjRule,
            hl = as.matrix(as.character(holidays))))
    }

    noHolidayDate <- lookupAdjustedDate(c())
    if(is.null(holidayList)) return(noHolidayDate)
   if(unit == "d") {
        if(NumUnits >= 0) {
            holidayEndDate <- noHolidayDate + 86400 * 366
            holidayStartDate <- as.POSIXct(startDate)
        } else {
            holidayStartDate <- noHolidayDate - 86400 * 366
            holidayEndDate <- as.POSIXct(startDate)
        }
    } else {
        if(NumUnits >= 0) {
            holidayEndDate <- noHolidayDate + 86400 * 32
            holidayStartDate <- noHolidayDate
        } else {
            holidayStartDate <- noHolidayDate - 86400 * 32
            holidayEndDate <- noHolidayDate
        }
    }
        
    holidayList <- as.POSIXct(holidayList)
    shortenedHolidayList <- holidayList[holidayList >= holidayStartDate & holidayList <= holidayEndDate]
    if(length(shortenedHolidayList) == 0) return(noHolidayDate)

    shortenedHolidayDate <- lookupAdjustedDate(shortenedHolidayList)
    if(shortenedHolidayDate > holidayEndDate)
        return(lookupAdjustedDate(holidayList))

    return(shortenedHolidayDate)
}

getDaysBetween <- function(startDate, endDate)
{
	temp.startDate <- as.POSIXct(startDate)
	temp.endDate <- as.POSIXct(endDate)
	result <- as.numeric(difftime(endDate, startDate, units = "d"))
	return(result)
}

getBusinessDaysBetween <- function(startDate, endDate, center) {
    needs(startDate="character|POSIXt", endDate="character|POSIXt", center="character")
    if(startDate > endDate)return(-JDates$businessDaysBetween_by_Date_Date_String(as.JDate(as.POSIXct(endDate)), as.JDate(as.POSIXct(startDate)), center))
    JDates$businessDaysBetween_by_Date_Date_String(as.JDate(as.POSIXct(startDate)), as.JDate(as.POSIXct(endDate)), center)
}



getSystemDBFXMarketName <- function(fxCurr,tenor,putCall,toUpper = TRUE, SystemNumber) {
    assert(any(class(fxCurr)=="FXCurr"))
#    assert(FXTenor$checkTenor(tenor))
    assert(any(SystemNumber==c(1,2))) 
    over <- fxCurr$over()
    under <- fxCurr$under()
    putCallList <- c("call","put")
    assert(any(putCall == putCallList),paste(putCall," is not a valid optionType"))
      
    if (putCall=="call") optionType <- paste("C",SystemNumber,sep="")
    else if (putCall=="put") optionType <- paste("P",SystemNumber,sep="")
    
    market_name <-  paste(over,under,tenor,"TRI.",optionType,sep = "")
    
    if (toUpper) return(toupper(market_name))
    else return(market_name)
}

getELDateFromRDate <- function(dates){
    
    elTimeStamps <- getELTimeFromPOSIXlt(dates)
    dates <- as.character(as.Date(dates))
    
    years <- as.numeric(substr(dates,1,4))
    months <- as.numeric(substr(dates,6,7))
    days <- as.numeric(substr(dates,9,10))

    res <- (years - 1900) * 10000  + (months * 100) + days
    res <- res + elTimeStamps / 10000
    return(res)
}

getELTimeFromPOSIXlt<- function(date){
    date <- as.POSIXlt(date)
    minutes <- date$min
    hours <- date$hour
    
    .bool <- minutes < 10 
    minutes[.bool] <- paste("0",minutes[.bool],sep= "")

    .bool <- hours < 10 
    hours[.bool] <- paste("0",hours[.bool],sep= "")
    
    res <- paste(hours,minutes,sep = "")
    return(as.numeric(res))
}

getHistoricalVolatility <- function(zooTRI,window = 60, dt = 1/252, method = "standard_deviation",initEWMA = NULL, logChanges = TRUE){
	zooTRI <- na.omit(zooTRI)
    needs(zooTRI = "zoo");
	if(logChanges) assert(all(zooTRI > 0),"The TRI series should always be positive")
    assert(dt > 0,"dt should be positive")
    assert(window > 0,"window should be positive")
    assert(any(method %in% c("standard_deviation","EWMA")),"window should be positive")        
    if(logChanges) ui <- na.omit(log(zooTRI/lag(zooTRI,-1)))
	else ui <- na.omit(diff(zooTRI, 1))
    n <- NROW(ui)
    if(n < window)return(NULL)
    if(method == "standard_deviation"){
        resZoo <- ui[window:n]    
        for (i in window:n)resZoo[i - window + 1] <- sd(ui[(i - window + 1):i])
    }else if(method == "EWMA"){
        resZoo <- sqrt(getEWMA(zoo(as.vector(ui*ui),index(ui)),halfLife = window,init = initEWMA))
    }
    return(resZoo/sqrt(dt))
}

getEWMA <- function(series,halfLife = 120, init = NULL){
    assert(halfLife > 0,"halfLife should be positive")               
    n <- NROW(series)
    res <- series
    if(n<2)return(res)
    if(!is.null(init))res[1] <- init
    lambda <- 0.5^(1/halfLife)
    for (i in 2:n)res[i] <- (1-lambda) * res[i] + lambda * res[i-1]
    return(res)
}      

getZScore <- function(zoo)return((zoo-mean(zoo))/sd(zoo))

parseSimpleTenor <- function(tenor)
{
# This assumes that the tenor is expressed as "10y" or "9m", etc. It also checks for "spot".
  assert(any(tenor==c("spot","1w","2w","1m","2m","3m","6m","9m","1y","18m", "2y", "3y", "4y", "5y", "6y", "7y", "8y", "9y", 
    "10y", "12y", "15y", "20y", "25y", "30y", "40y")))
  if (tenor=="spot") {
    unit <- "d"
    numUnits <- 0
  }
  else {
    unit <- substr(tenor,nchar(tenor),nchar(tenor))
    numUnits <- as.numeric(substr(tenor,1,nchar(tenor)-1))
  }
  return(list(unit=unit,numUnits=numUnits))
}

getSubList = function(myList,nbSteps,step){

    needs(nbSteps = "numeric",step = "numeric")
    assert(nbSteps >= step && step >= 1 && length(myList) > 0)
    
    n <- length(myList)
    
    f <-floor(n/nbSteps)
    
    b1 <- (step - 1) * f + 1
    b2 <- step * f
    
    if(step == nbSteps)return(myList[b1:n])
    return(myList[b1:b2])
}

rightStr <- function(fullString, numChars) substr(fullString, nchar(fullString) - numChars + 1, nchar(fullString))

leftStr <- function(fullString, numChars) substr(fullString, 1, numChars)

midStr <- function(fullString, startNum, numChars) substr(fullString, startNum, startNum + numChars - 1)

dateTimeFromArguments <- function(arguments, hour=NULL){
	arguments <- arguments[-(1:match("--args", arguments))]
	
	if(NROW(arguments)==0){
		dataDate <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb", switchTime="15:00:00")$lastBusinessDate
	}else
		dataDate <- first(arguments)
	
	dataDate <- as.POSIXlt(dataDate)
	if (!is.null(hour)) dataDate$hour <- hour
	dataDateTime <- as.POSIXct(dataDate)
	return(dataDateTime)
}

removeCommasFromNumber <- function(x, returnAsNumeric = TRUE) {
	output <- squish(strsplit(as.character(x), ','))
	if(returnAsNumeric) output <- as.numeric(output)
	output
}

removeCommasFromDataFrame <- function(x, returnAsNumeric = TRUE) {
	needs(x = 'data.frame')
	output <- data.frame()
	for(row in 1:NROW(x)) 
		for(col in 1:NCOL(x)) output[row,col] <- removeCommasFromNumber(x[row,col], returnAsNumeric)
	row.names(output) <- row.names(x)
	names(output) <- names(x)
	colnames(output) <- colnames(x)
	output
}

mergeDataFrames <- function(a,b){
	needs(a = 'data.frame',b = 'data.frame')
	if(equals(sort(colnames(a)),sort(colnames(b)))){
		rbind(a,b)
	}else{
		cnames <- unique(c(colnames(a),colnames(b)))	
		g <- function(h,cnames){
			missingcol <- !cnames %in% colnames(h)
			if(all(missingcol == FALSE)) return(h)			
			l <- list()
			length(l) <- length(cnames[missingcol])
			names(l) <- cnames[missingcol]
			for(i in 1:length(l)) l[[i]] <- rep(NA,NROW(h))
			data.frame(h,l,stringsAsFactors = FALSE)
		}
		rbind(g(a,cnames),g(b,cnames))
	}
}


addSlashToDirectory <- function(directoryName){
	needs(directoryName = 'character')
	if(rightStr(directoryName,1) != '/') directoryName <- squish(directoryName, '/')
	directoryName
}