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
calculator <- IRSSpreadTri(ccy = "usd",dataSource = "internal",dataTimeStamp = Close$NY.irs,
        bondIssuer = "us_treasury",bondSector = "government",exportSource = "internal")

for (i in 1:NROW(tenorList)){

    #tsdb$purgeTimeSeries(paste("irs_usd_spread_",tenorList[i],"_1c_tri",sep = ""),data.source = "internal")
    #tsdb$purgeTimeSeries(paste("irs_usd_spread_",tenorList[i],"_1c_tri_daily",sep = ""),data.source = "internal")    

    result <- calculator$updateSpreadTriByTenor(swapTenor = tenorList[i],modified = "1c",couponType = "otr",updateTSDB = TRUE,startDate = as.character(dateList$refBusinessDate),endDate = NULL)
    if(is.null(result))quit(save="no", status = -1)
}                                                                       

quit(save="no", status=0)