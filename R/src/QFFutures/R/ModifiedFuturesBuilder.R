constructor("ModifiedFuturesBuilder", function(contract,rollObj,...){
    this <- extend(RObject(), "ModifiedFuturesBuilder")    
    if(inStaticConstructor(this)) return(this)
    this$.contract <- contract
    this$.rollObj <- rollObj
	this$.initialized <- FALSE
	this$.rollSchedule <- data.frame(rollDate = NULL,oldTicker = NULL,newTicker = NULL)
    constructorNeeds(this,contract = "Contract",rollObj = "Roll")
    this
})

method("backAdjustedSeries","ModifiedFuturesBuilder",function(this,nth,adjustmentMethod,...){
	assert(adjustmentMethod %in% c("simple","none","ratio")); needs(nth = "numeric|integer")
    startDate <- first(index(this$.contract$.rawData)); endDate <- last(index(this$.contract$.rawData))
    this$.dates <- sort(as.character(index(this$.contract$.rawData)),TRUE)
	this$.rollDates <- this$.contract$rollDatesInWindow(startDate,endDate,this$.rollObj)
	sapply(this$.dates,this$runOneDate,nth,adjustmentMethod)
	this$runAdjustmentLogic(this$.contract$.rawData,adjustmentMethod,NROW(this$.rollSchedule))    
})

method("runOneDate","ModifiedFuturesBuilder",function(this,date,nth,adjustmentMethod,...){            
    if(!this$.initialized){
		this$.lastTicker <- this$getNextTheoraticalTicker(date,nth); this$.lastDate <- date
		if(!is.null(this$.rollDates))this$.nextRollDate <- last(this$.rollDates)
		this$isValidTicker(this$.lastTicker)
		this$.initialized <- TRUE
    }
	currentTicker <- this$.lastTicker
	if(!is.null(this$.nextRollDate)){
		if(as.Date(this$.lastDate) <= as.Date(this$.nextRollDate)){
			theoraticalTicker <- this$getNextTheoraticalTicker(date,nth)
			if(this$isValidRoll(theoraticalTicker,date)){
				this$.nextRollDate <- this$getNextRollDate()
				currentTicker <- theoraticalTicker
				if(date == first(this$.dates)){
					# First Date is Roll Date 
					if(NROW(this$.dates) > 1)currentTicker <- this$getNextTheoraticalTicker(second(this$.dates),nth)
					else currentTicker <- currentTicker  
					this$.rollSchedule <- rbind(this$.rollSchedule,
						data.frame(rollDate = this$.lastDate,oldTicker = currentTicker,newTicker = theoraticalTicker,stringsAsFactors = FALSE)
					)
				}else{
					# Standard Case
					this$.rollSchedule <- rbind(this$.rollSchedule,
							data.frame(rollDate = this$.lastDate,oldTicker = currentTicker,newTicker = this$.lastTicker,stringsAsFactors = FALSE)
					)	
				}
				cat(squish("Rolling on: ",as.character(this$.lastDate),"\n"))
			}
		}
	}
    this$.lastTicker <- currentTicker; this$.lastDate <- date;
})

method("runAdjustmentLogic","ModifiedFuturesBuilder",function(this,data,adjustmentMethod,nbRolls,...){
	if(nbRolls == 0) return(getZooDataFrame(data[,this$.lastTicker]))
	rollDates <- this$.rollSchedule[,"rollDate"]
	frontTickers <- this$.rollSchedule[,"newTicker"]
	backTickers <- this$.rollSchedule[,"oldTicker"]
	modifiedSeries <- data[index(data) >= as.POSIXct(first(rollDates)),first(frontTickers)]
	for(rollStep in 1:nbRolls){
		if(rollStep == nbRolls){
			rangeBool <- index(data) <= as.POSIXct(rollDates[rollStep])
			zooToAdd <- data[rangeBool,backTickers[rollStep]]
		}else{
			rangeBool <- index(data) >= as.POSIXct(rollDates[rollStep+1]) & index(data) <= as.POSIXct(rollDates[rollStep])
			zooToAdd <- data[rangeBool,frontTickers[rollStep+1]]
		}
		zooToAdd <- this$adjust(firstN(zooToAdd,NROW(zooToAdd)-1),adjustmentMethod,first(modifiedSeries),last(zooToAdd))
		modifiedSeries <- rbind(modifiedSeries,zooToAdd)
	}
	getZooDataFrame(modifiedSeries)
})

method("adjust","ModifiedFuturesBuilder",function(this,zooToAdd,adjustmentMethod,lastModifiedData,refData,...){
	if(adjustmentMethod == "simple"){
		zooToAdd + lastModifiedData - refData
	}else if(adjustmentMethod == "ratio"){
		zooToAdd * lastModifiedData / refData
	}else if(adjustmentMethod == "none"){
		zooToAdd        
	}
})

method("isValidTicker","ModifiedFuturesBuilder",function(this,ticker,...){
	if(is.null(ticker))fail("Ticker is NULL (Contract$.numCycles probably needs to be increased)!!!")
	if(!(ticker %in% colnames(this$.contract$.rawData))) fail(squish(ticker," is not included in raw underlying data!!!"))
})


method("isValidRollData","ModifiedFuturesBuilder",function(this,currentTicker,date,...){
	(!is.na(this$.contract$.rawData[as.POSIXct(this$.lastDate),currentTicker])
	& !is.na(this$.contract$.rawData[as.POSIXct(this$.lastDate),this$.lastTicker])
	& !is.na(this$.contract$.rawData[as.POSIXct(date),currentTicker]))
})

method("isValidRoll","ModifiedFuturesBuilder",function(this,currentTicker,date,...){
	this$isValidTicker(currentTicker)
	this$isValidRollData(currentTicker,date)
})

method("rollSchedule","ModifiedFuturesBuilder",function(this,...){
	this$.rollSchedule
})

method("getNextRollDate","ModifiedFuturesBuilder",function(this,...){
	bool <- as.Date(this$.rollDates) < as.Date(this$.lastDate)
	if(any(bool))return(last(this$.rollDates[bool]))
	else return(NULL)
})

method("getNextTheoraticalTicker","ModifiedFuturesBuilder",function(this,date,nth,...){
	this$.contract$nth("ticker",nth,this$.rollObj,date)
})