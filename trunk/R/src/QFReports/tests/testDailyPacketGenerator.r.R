# TODO: Add comment
# 
# Author: dhorowitz
###############################################################################


library(QFReports)




testDailyPacketGeneratorConstructor <- function(){
	dateList <- lapply(c('2008-07-18', '2008-07-18', '2008-07-17'), as.POSIXct)
	dpg <- DailyPacketGenerator(dateList = dateList)
	checkSame(dpg$.dateClose, as.POSIXct('2008-07-18'))
	checkSame(dpg$.datePrevious, as.POSIXct('2008-07-17'))
	checkSame(dpg$.pastDates, lapply(c('2008-07-17','2008-07-11','2008-06-19'),as.POSIXct))
	target <- list(as.character(dateList[[2]]), as.character(dateList[[3]]), '', '1d', '5d', '20d', '30d Zscore', '90d Zscore',
					 '30d SD', '90d SD', '30d %ile', '90d %ile')
	checkSame(dpg$.rowNames, target)	
}

testDailyPacketGeneratorRun <- function(){
	dateList <- lapply(c('2008-07-18', '2008-07-18', '2008-07-17'), as.POSIXct)
	output <- DailyPacketGenerator(dateList= dateList)$run()
	checkSame(output[[1]][1,2], '3.62')
	checkSame(output[[1]][5,5], '22')
	checkSame(output[[2]][5,3], '-6')
	checkSame(output[[2]][7,2], '1.01')
	checkSame(output[[3]][7,4], '2.77')
	#checkSame(output[[4]][7,4], '-0.4')  ## Vol stuff removed until data restored
	#checkSame(output[[4]][8,5], '0.46')
}




