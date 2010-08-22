#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(Live) 

args <- commandArgs()
args <- args[-(1:match("--args", args))]
dateList <- getRefLastNextBusinessDates(lastBusinessDate = args,holidaySource = "financialcalendar", financialCenter = "nyb")

########################################################################

destination <- 'team'
yellowKeysAllowed <- Contract$yellowKeys()
rollMessage <- NULL; updateMessage <- NULL; yellowKeyMessage <- NULL; bloombergRootMessage <- NULL; ricRootMessage <- NULL

#  Get list of all continuous contracts in SystemDB
allMarkets <- SystemDB$getContinuousContracts()
marketNames <- allMarkets[!allMarkets %in% SystemDB$getContinuousContracts('ts')]

# Set the time stamp value that will be saved on every record in SystemDB
timeStamp <- as.character(dateList$lastBusinessDate)

# Get rolling information for eurodollar packs and bundles (this is a special case)
shortRateFutures <- ShortRateFuture(contract='ed', rollDaysPriorToExpiry=5)
adjustedList <- shortRateFutures$adjustedList(10,dateList$lastBusinessDate)
shortRateFuturesMarkets <- ShortRateFuture$markets()

# Get rolling information for GFUT symbols
gfutFrame <- Contract$gfutYearMonthFrame()

for(i in 1:NROW(marketNames)){
	print(i)
	canUpdate <- TRUE
	marketTicker <- marketNames[i]	
	# Keep track of what is currently in SystemDB 
	dbBloombergTicker <- SystemDB$getFrontBloombergTicker(marketTicker)
	dbTSDBTicker <- SystemDB$getFrontTSDBTicker(marketTicker)
	dbYellowKey <- SystemDB$getYellowKey(marketTicker)
	dbBloombergRoot <- SystemDB$getBloombergRoot(marketTicker)
	dbRICRoot <- SystemDB$getRICRoot(marketTicker)
	dbYearMonth <- rightStr(dbTSDBTicker,6)
	isInDB <- NROW(dbBloombergTicker) > 0	
	# Delete existing record
	SystemDB$deleteMarketTicker(marketTicker)			
	# Get contract name from market 
	contractName <- Contract$nameFromMarket(marketTicker)	
	
	if(marketTicker %in% shortRateFuturesMarkets){ # ED markets
		tsdbTicker <- shortRateFutures$frontDataForMarket(marketTicker,adjustedList)	
		lastYearMonth <- rightStr(tsdbTicker,6)		
	}else if(marketTicker %in% gfutFrame[,'marketName']){ # GFUT markets
		tsdbTicker <- gfutFrame[gfutFrame[,'marketName'] == marketTicker,'field']		
		lastYearMonth <- rightStr(tsdbTicker,6)
	}else{ # Others (CSI and internal roll logics) 
		yearMonthZoo <- ModifiedContract(marketTicker)$specificYearMonths()	
		if(all(as.POSIXct(c(dateList$refBusinessDate,dateList$lastBusinessDate)) %in% index(yearMonthZoo))){
			lastYearMonth <- yearMonthZoo[as.POSIXct(dateList$lastBusinessDate)][[1]]
			refYearMonth <- yearMonthZoo[as.POSIXct(dateList$refBusinessDate)][[1]]
			tsdbTicker <- squish(contractName,lastYearMonth)			
		}else{
			updateMessage <- paste(updateMessage,marketTicker,sep = '\n')			
			canUpdate <- FALSE
		}
	}	
	if(canUpdate){
		# Yellow Key
		isValidYellowKey <- dbYellowKey %in% Contract$yellowKeys()
		if(NROW(isValidYellowKey) == 0 || !isValidYellowKey){
			dbYellowKey <- 'Comdty'
			yellowKeyMessage <- paste(yellowKeyMessage,marketTicker,sep = '\n')			
		}		
		# Ric Root
		if(is.na(dbRICRoot) || NROW(dbRICRoot) == 0){
			dbRICRoot <- toupper(contractName)
			ricRootMessage <- paste(ricRootMessage,marketTicker,sep = '\n')							
		}		
		# Construct bloomberg ticker
		if(is.na(dbBloombergRoot) || NROW(dbBloombergRoot) == 0){
			dbBloombergRoot <- toupper(List$bloombergRootFromContractName(contractName))
			bloombergRootMessage <- paste(bloombergRootMessage,marketTicker,sep = '\n')							
		}
		bloomberTicker <- List$bloombergFromYearMonth(dbBloombergRoot,as.character(lastYearMonth))
		
		# Insert new record in SystemDB
		SystemDB$insertMarketTicker(marketTicker,bloomberTicker,dbYellowKey,tsdbTicker,timeStamp,dbBloombergRoot,dbRICRoot)
		
		# Email if ticker changed
		if(isInDB){
			if(bloomberTicker != dbBloombergTicker){
				rollMessage <- paste(rollMessage,squish(marketTicker,' rolls from ',dbBloombergTicker,' to ',bloomberTicker),sep = '\n')									
			}
		}
	}
}

# Send EMails
if(!is.null(yellowKeyMessage)) Mail$notification("Continuous futures yellow key assigned to 'Comdty'",yellowKeyMessage)$sendTo(destination)
if(!is.null(bloombergRootMessage)) Mail$notification("Continuous futures bloomberg roots assigned",bloombergRootMessage)$sendTo(destination)
if(!is.null(ricRootMessage)) Mail$notification("Continuous futures bloomberg RIC roots assigned",ricRootMessage)$sendTo(destination)
if(!is.null(updateMessage)) Mail$problem(squish('Continuous futures not updated for ',as.character(dateList$lastBusinessDate)),updateMessage)$sendTo(destination)
if(!is.null(rollMessage)) Mail$notification(squish('Continuous futures rolls for ',as.character(dateList$lastBusinessDate)),rollMessage)$sendTo(destination)