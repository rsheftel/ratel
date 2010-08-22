filter.zoo.byTime <- function(z, time, tz = "") {
    needs(z = "zoo", time = "character", tz = "character")
    assert(tz == "", "time zone functionality currently disabled")
    t <- strptime(time, "%H:%M:%S", tz)
    dates <- as.POSIXlt(convert.timeZone(index(z), tz))
    z[with(dates, hour == t$hour & min == t$min & sec == t$sec), drop=FALSE]
}

filter.zoo.byTimeRange <- function(z, timeStart = NULL,timeEnd = NULL, tz = "") {
	needs(z = "zoo", timeStart = "character?",timeEnd = "character?", tz = "character")
	assert(tz == "", "time zone functionality currently disabled")	
	getTStamps <- function(x){strptime(rightStr(as.character(index(x)),8), "%H:%M:%S", tz)}
	getTSE <- function(x,timeStamp){rep(strptime(timeStamp, "%H:%M:%S", tz),NROW(x))}
	if(all(is.na(getTStamps(z))))return(z)
	if(!is.null(timeStart))z <- z[getTStamps(z) >= getTSE(z,timeStart)]
	if(!is.null(timeEnd))z <- z[getTStamps(z) <= getTSE(z,timeEnd)]
	if(NROW(z)==0)return(NULL)
	z
}

filter.zoo.byDate <- function(z,date) {
	needs(z = "zoo",date = 'POSIXt|character')
	z[as.Date(index(z)) %in% as.Date(date)]
}

daily.zoo.forTime <- function(z, timeClose, tz = "") {
	needs(z = "zoo", timeClose = "character", tz = "character")
	assert(tz == "", "time zone functionality currently disabled")
	checkShape(getZooDataFrame(z),cols = 1)
	zClose <- filter.zoo.byTime(z,timeClose,tz)
	if(NROW(zClose)==0)return(NULL)
	m <- merge(z,zClose)
	index(m) <- index(z)
	.m <- m[index(strip.times.zoo(m)) %in% index(strip.times.zoo(na.omit(m)))]	
	.m <- na.locf(.m)	
	.m[,1]-.m[,2]
}

filter.zoo.byIntersection <- function(base, ...) {
    needs(base = "zoo")
    others <- list(...)
    indices <- rep(TRUE, length(index(base)))
    for(i in others)
        indices <- indices & index(base) %in% index(i)
    base[indices, drop=FALSE]
}

strip.times.zoo <- function(z, tz = "") {
    needs(z = "zoo", tz = "character")
    assert(tz == "", "time zone functionality currently disabled")
    fix.row.names <- (
        !is.null(rownames(z)) && 
        nrow(z) != 0 && 
        nrow(z) == length(rownames(z)) && 
        rownames(z)[1] == as.character(index(z)[1])
    )
    index(z) <- as.POSIXct(trunc(convert.timeZone(index(z), tz), "days"))
    if(fix.row.names)
        rownames(z) <- as.character(index(z))
    z
}

make.zoo.daily <- function(z, time, tz = "") {
    needs(z = "zoo", time = "character", tz = "character")
    assert(tz == "", "time zone functionality currently disabled")
    strip.times.zoo(filter.zoo.byTime(z, time, tz), tz)
}
    

timeZone <- function(z) {
    needs(z = "zoo")
    assert(inherits(index(z), "POSIXt"), squish("index of zoo is not a POSIXt was ", class(index(z))))
    attr(index(z), "tzone")
}

convert.timeZone <- function(dates, tz) UseMethod("convert.timeZone", dates)

convert.timeZone.POSIXct <- function(dates, tz) {
    needs(dates = "POSIXct", tz = "character")
    assert(tz == "", "time zone functionality currently disabled")
    assert(is.null(attr(dates, "tzone")) || attr(dates, "tzone") == tz, "time zone functionality currently disabled")
    if(!is.null(attr(dates, "tzone")) && attr(dates, "tzone") == tz) return(dates)
    as.POSIXct(as.POSIXlt(dates, tz))
}

convert.timeZone.POSIXlt <- function(dates, tz) {
    needs(dates = "POSIXlt", tz = "character")
    assert(tz == "", "time zone functionality currently disabled")
    assert(is.null(attr(dates, "tzone")) || attr(dates, "tzone") == tz, "time zone functionality currently disabled")
    if(!is.null(attr(dates, "tzone")) && attr(dates, "tzone") == tz) return(dates)
    newDates <- as.POSIXlt(as.POSIXct(dates), tz)
    # For some bizarre reason, as.POSIXlt() sets the tz attribute to a list of time zones (ie. c("EST5EDT", "EST", "EDT")).  This is ugly.
    assert(attr(newDates, "tzone")[1] == tz)
    attr(newDates, "tzone") <- tz
    newDates
}

observations.zoo <- function(observations) {
	needs(observations = "JObservations")
	zoo(matrix(observations$values()), order.by=POSIXct.from.millis(observations$timesMillis()))
}

column <- function(...) UseMethod("column")

column.default <- function(z, col) {
    needs(z = "zoo|data.frame|matrix|array", col = "character|numeric|integer")
    z[, col]
}

sumZoos <- function(zoos) {
    needs(zoos = "list")
    accumulate('+', zoos)
}

chronToPosixZoo <- function(z){
    needs(z = "zoo"); assert(all(class(index(z)) %in% c("chron","dates","times")))
    index(z) <- as.POSIXct(paste(
        as.character(as.Date(index(z))),
        paste(hours(index(z)),minutes(index(z)),seconds(index(z)),sep = ":")
    ,sep = " "))
    z
}

changeZooFrequency = function(zooObj,freq){
    # For yearly/monthly, this will take the last date of each month/year
    # For weekly, this will take Fridays
    
    needs(zooObj = "zoo")
    if(!any(freq %in% c("yearly","monthly","weekly")))throw("This is not a valid frequency!")
    copyZoo <- zooObj # This is to ensure that we don't change the format of the zoo object
    
    index(zooObj) <- as.character(index(zooObj))    
    index.years <- as.POSIXlt(index(zooObj))$year
    index.months <- months(as.POSIXct(index(zooObj)))
    index.weeks <- weekdays(as.POSIXlt(index(zooObj)))
    
    if(freq == "weekly"){
        sub <- subset(copyZoo,index.weeks == "Friday")    
        if(NROW(sub) == 0)return(NULL)
        res <- sub
    }else{   
        if(freq == "monthly")index <- index.months
        if(freq == "yearly")index <- index.years
    
        l <- NULL
        n <- NROW(index)
        if(NROW(index)==0)return(NULL)
        bool <- FALSE
        for(i in 1:(n-1)){
            bool <- (
                index[i]!=index[i+1]
                || ifelse(freq != "yearly",index.years[i]!=index.years[i+1],FALSE)
            )
            if(bool)l <- c(l,i)
        }        
        res <- copyZoo[l]
    }
    
    # In all cases, the very last day of the zoo is included    
    if(!any(last(index(copyZoo)) %in% index(res)))res <- rbind(res,copyZoo[NROW(copyZoo)])    
    return(res)
}

getZooDataFrame <- function(zooObj,colNames = NULL,...) {
    assert(class(zooObj)=="zoo",paste(zooObj,"should be a zoo object"))

    columnNames <- colnames(zooObj)
    result <- zoo(data.frame(zooObj),order.by = as.POSIXct(rownames(data.frame(zooObj))))
    if(!is.null(columnNames))colnames(result) <- columnNames 
    if(!is.null(colNames))colnames(result) <- colNames 
   	result
}

merge.zoo.null <- function(zoo1,zoo2,...) {
    assert(is.null(zoo1) || inherits(zoo1, "zoo"), paste("zoo 1 was not zoo but was", class(zoo1)))
    assert(is.null(zoo2) || inherits(zoo2, "zoo"), paste("zoo 2 was not zoo but was", class(zoo2)))
    if (is.null(zoo1)) return(zoo2)
    if (is.null(zoo2)) return(zoo1)
    getZooDataFrame(merge.zoo(zoo1, zoo2))
}

exportZooInTradeStationASCII <- function(zooObj,filePath,...)
{
    zooObj <- getZooDataFrame(zooObj)
    assert(dim(zooObj)[2]==1,paste(zooObj,"should be a vector"))

    dates <- index(zooObj)
    data <- data.frame(zooObj)

    options(scipen = 50)

    tsFile <- na.omit(data.frame(
        Date = dates,
        Open = round(data,10),
        High = round(data,10),
        Low = round(data,10),
        Close = round(data,10)
    ))
    colnames(tsFile) <- c("Date","Open","High","Low","Close")

    write.table(tsFile,file = filePath,sep = ",",col.names = TRUE,row.names = FALSE,quote = FALSE)
}

zooForDates <- function(inputZoo, dates, remove.na=TRUE, ...)
{
	needs(inputZoo="zoo", dates="POSIXct")
	
	mergedZoo <- merge(inputZoo, zoo(NA,dates), all=FALSE)
	mergedZoo <- merge(mergedZoo, zoo(NA,dates), all=TRUE)
	
	outputZoo <- mergedZoo[,1:NCOL(inputZoo)]
	if (remove.na) outputZoo <- outputZoo[!is.na(outputZoo)]
	
	return(outputZoo)
}

importZooFromTradeStationFile <- function(fileName){
    needs(fileName = "character")
    tsData = read.csv(fileName,stringsAsFactors = FALSE)
    dates <- as.character(as.Date(tsData[,1],format = "%m/%d/%Y"))
    timeStamps <- sapply(
        as.character(tsData[,2]),
        function(x){
            if(nchar(x) > 2)squish(substr(x,1,nchar(x)-2),":",substr(x,nchar(x)-1,nchar(x)),":00")
            else squish("00:",x,":00")
        }
    )
    zoo(tsData[,-c(1,2)],as.POSIXct(paste(dates,timeStamps,sep = " ")))
}

overlap <- function(...) any(duplicated(unlist(...)))
indiciesOverlap <- function(...) overlap(lapply(list(...),index))

stitchZoos <- function(listOfZoos, backAdjusted=FALSE, overlapRule='notAllowed'){
#overlapRule options:
#			notAllowed  - all zoos must have non-overlapping dates
#			usePrior	- use all of the prior zoo, then add the next one. the cut is last(prior)
#			useLatter	- use all of the latter zoo, so the cut is first(latter)
	
	needs(listOfZoos="list", backAdjusted="logical", overlapRule='character')
	
	listOfZoos.count <- max(index(listOfZoos))
	if (listOfZoos.count < 2) return('#Error : Must provide at least 2 zoos')
	if (overlap(lapply(listOfZoos,index)) && overlapRule=='notAllowed') return('#Error : Zoos cannot have overlapping dates')
	
	splitDates <- list()
	filteredZoos <- list()
	
	if (overlapRule=="useLatter"){
		filteredZoos[[listOfZoos.count]] <- listOfZoos[[listOfZoos.count]]
		for (count in (listOfZoos.count-1):1){
			splitDates[[count]] <- first(index(listOfZoos[[count+1]]))
			priorZoo <- Range$before(splitDates[[count]])$cut(listOfZoos[[count]])
			if (backAdjusted){
				overlapDate <- first(index(filteredZoos[[count+1]]))
				if (NROW(listOfZoos[[count]][overlapDate]) != 0){
					priorZoo <- priorZoo + (first(filteredZoos[[count+1]]) - listOfZoos[[count]][overlapDate][[1]])
				}
				else{
					priorZoo <- priorZoo + (first(filteredZoos[[count+1]]) - last(priorZoo))
				}
			}
			filteredZoos[[count]] <- priorZoo
		}
	}
	else{
		filteredZoos[[1]] <- listOfZoos[[1]]
		for (count in 1:(listOfZoos.count-1)){
			splitDates[[count]] <- last(index(listOfZoos[[count]]))
			nextZoo <- Range$after(splitDates[[count]])$cut(listOfZoos[[count+1]])
			if (backAdjusted){
				overlapDate <- last(index(filteredZoos[[count]]))
				if (NROW(listOfZoos[[count+1]][overlapDate]) != 0){
					nextZoo <- nextZoo + (last(filteredZoos[[count]]) - listOfZoos[[count+1]][overlapDate][[1]])
				}
				else{
					nextZoo <- nextZoo + (last(filteredZoos[[count]]) - first(nextZoo))					
				}
			}
			filteredZoos[[count+1]] <- nextZoo
		}
	}
	
	stitchedZoo <- do.call(rbind,filteredZoos)
	return(list(stitchedZoo=stitchedZoo,splitDates=splitDates))
}

is.empty <- function(x) (length(x) == 0 || is.null(x))


setZooTimes <- function(thisZoo, hour=15, minute=0, second=0){
	thisIndex <- as.POSIXlt(index(thisZoo))
	thisIndex$hour <- hour
	thisIndex$min <- minute
	thisIndex$sec <- second
	index(thisZoo) <- as.POSIXct(thisIndex)
	return(thisZoo)
}
lastN <- function(zooObj,Num){
	needs(zooObj = "zoo",Num = "numeric")
	failIf(Num <= 0 | Num > NROW(zooObj))
	condition <- (NROW(zooObj)-Num+1):NROW(zooObj)
	if(NCOL(zooObj) > 1) zooObj[condition,]
	else zooObj[condition]
}

firstN <- function(zooObj,Num){
	needs(zooObj = "zoo",Num = "numeric")
	failIf(Num <= 0 | Num > NROW(zooObj))
	condition <- 1:Num
	if(NCOL(zooObj) > 1) zooObj[condition,]
	else zooObj[condition]
}
