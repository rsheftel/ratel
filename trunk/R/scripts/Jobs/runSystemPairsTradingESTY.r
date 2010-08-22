#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())
library(Live)

args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
    dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else{
    dateList <- getRefLastNextBusinessDates(lastBusinessDate = args,holidaySource = "financialcalendar", financialCenter = "nyb")
}

########################################################################

holidays <- HolidayDataLoader$getHolidays(source = "financialcalendar",financialCenter = "nyb")
startDate <- getFincadDateAdjust(dateList$lastBusinessDate,"d",-25,holidays)

this <- SystemPairsTradingESTY(dataTimeStamp = "16:15:00")

result <- this$run(
    startDate = startDate,
    window = 20,
    tcES = 12.5,
    tcTY = 15.7,
    updateTSDB = TRUE,
    updateASCII = FALSE,
    generatePDF = TRUE
)

assert(as.Date(last(index(result$pairResult))) == as.Date(dateList$lastBusinessDate))

if(is.null(result)) quit(save="no", status=-1)