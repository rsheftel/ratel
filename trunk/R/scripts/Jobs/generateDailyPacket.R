#
# author: dhorowitz
############################################################

library(GSFCore)
library(QFReports)
library(hwriter)


#Get date from command line, or use logic if not available
arguments <- commandArgs()
arguments <- arguments[-(1:match("--args", arguments))]

if(NROW(arguments)==0){
	dataDate <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else
{
	dataDate <- getRefLastNextBusinessDates(lastBusinessDate = arguments, holidaySource = "financialcalendar", financialCenter = "nyb")
}

dpg <- DailyPacketGenerator(dateList = dataDate)
output <- dpg$run()
fileDirectory <- paste(dataDirectory(), 'DailyReports/', sep = '')
fileName <- paste(format(dpg$.dateClose, "%Y%m%d"), '.html', sep = '')

dpg$createHTML(output, fileDirectory, fileName, sep = '')

email <- Mail$notification('Daily Data Packet',paste('Report available in ', fileDirectory, '/', fileName, '\n\n', sep = ''))

email$attachFile(paste(fileDirectory,fileName, sep = '/'))

email$sendTo('team')

on.exit(emailer$reset())

rm(dpg)
rm(output)
rm(fileDirectory)
rm(fileName)