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

########################################################################

tenorList <- TermStructure$us_treasury
calculator <- IRSSpread(ccy = "usd",swapInstrument = "irs",swapSource = "internal",swapTimeStamp = Close$NY.irs,
        bondInstrument = "bond",bondIssuer = "us_treasury",bondSector = "government",bondTimeStamp = Close$NY.irs,bondSource = "internal")

for (i in 1:NROW(tenorList)){

    # tsdb$purgeTimeSeries(paste("irs_usd_spread_",tenorList[i],"_1c",sep = ""),data.source = "internal")
    # tsdb$purgeTimeSeries(paste("irs_usd_spread_",tenorList[i],"_1n",sep = ""),data.source = "internal")    

    result.1 <- calculator$updateSpreadSeriesByTenor(swapTenor = tenorList[i],bondYieldType = "otr",modified = "1n",startDate = as.character(dateList$refBusinessDate),endDate = NULL)
    result.2 <- calculator$updateSpreadSeriesByTenor(swapTenor = tenorList[i],bondYieldType = "1c",modified = "1c")
    if(is.null(result.1) || is.null(result.2))quit(save="no", status = -1)
}

quit(save="no", status=0)