#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())
library(fincad)
library(GSFCore)
library(RUnit)
library(RJDBC)
library(QFCredit)
library(QFEquity)
library(QFFixedIncome)

args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
    dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else{
    dateList <- getRefLastNextBusinessDates(lastBusinessDate = args,holidaySource = "financialcalendar", financialCenter = "nyb")
}

startDate <- as.character(dateList$refBusinessDate)
endDate <- as.character(dateList$lastBusinessDate)
tenor <- "on"
updateTSDB <- TRUE

this <- LiborTri(ccy = "usd",dataTimeStamp = "15:00:00",unitSett = "d",numUnitsSett = 1,acc = 2) # act/360
result <- this$run(startDate,endDate,tenor,updateTSDB)

quit(save="no", status = 0)