rm(list = ls())                   
library(fincad)                   
library(GSFCore)                  
library(RUnit)
library(RJDBC)                 
library(QFCredit)
library(QFEquity)
library(QFFixedIncome)
library(QFFuturesOptions)
library(QFMath)


args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
	dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else
{
	dateList <- getRefLastNextBusinessDates(lastBusinessDate = args[1], holidaySource = "financialcalendar", financialCenter = "nyb")
}

startDate <- as.POSIXlt(dateList$refBusinessDate)
if(startDate$mon < 3) startDate$year <- startDate$year - 1
startDate$mon <- (startDate$mon - 3) %% 12
startDate <- as.POSIXct(startDate)


lastDateFormatted <- as.POSIXct(paste(dateList$lastBusinessDate, ' 15:00:00', sep = ''))
refDateFormatted <- as.POSIXct(paste(dateList$refBusinessDate, ' 15:00:00', sep = ''))


#contractList <- c('fv', 'ty', 'us')
contractList <- c('ty')

for(contractName in contractList){
	straddle <- FuturesOptionPairTriGenerator(contractName,'straddle',FuturesOptionRollLogic$straddleStrangle, 'quarterly',2)
	contractOutput <- straddle$runDailyHedgedReturns(startDate,dateList$lastBusinessDate)
	
	lastDateReturn <- as.numeric(contractOutput[as.POSIXct(dateList$lastBusinessDate)])
	
	timeSeriesName <- paste(contractName, '_frontquarterly_straddle_tri_daily_mid', sep = '')
	
	oldSeries <- TimeSeriesDB$retrieveTimeSeriesByName(timeSeriesName, 'internal')[[1]]
	lastOldValue <- last(oldSeries)
	
	oldSeriesLastDate <- index(oldSeries)[length(oldSeries)]
	checkEquals(refDateFormatted, oldSeriesLastDate)
	
	newTRIValue <- lastOldValue + lastDateReturn	
	uploadZoo <- zoo(newTRIValue, order.by = lastDateFormatted) 
	TimeSeriesDB$writeOneTimeSeriesByName(uploadZoo, timeSeriesName, 'internal')
}


for(contractName in contractList){
	straddle <- FuturesOptionPairTriGenerator(contractName,'straddleStrangle',FuturesOptionRollLogic$straddleStrangle, 'quarterly',2)
	contractOutput <- straddle$runDailyHedgedReturns(startDate,dateList$lastBusinessDate, strangleWeight = 0.50)
	
	lastDateReturn <- as.numeric(contractOutput[as.POSIXct(dateList$lastBusinessDate)])
	
	timeSeriesName <- paste(contractName, '_frontquarterly_straddlestrangle_tri_daily_mid_2width_0.50weight', sep = '')
	
	oldSeries <- TimeSeriesDB$retrieveTimeSeriesByName(timeSeriesName, 'internal')[[1]]
	lastOldValue <- last(oldSeries)
	
	oldSeriesLastDate <- index(oldSeries)[length(oldSeries)]
	checkEquals(refDateFormatted, oldSeriesLastDate)
	
	newTRIValue <- lastOldValue + lastDateReturn	
	uploadZoo <- zoo(newTRIValue, order.by = lastDateFormatted) 
	TimeSeriesDB$writeOneTimeSeriesByName(uploadZoo, timeSeriesName, 'internal')
}