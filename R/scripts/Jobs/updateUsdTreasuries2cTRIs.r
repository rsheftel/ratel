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

calculator <- BondTri(couponTypeLead = "1o",couponTypeLag = "2o",modified = "2c")

for (i in 1:NROW(tenorList)){

    result <- calculator$run(
        startDate = as.character(dateList$refBusinessDate),
        endDate = NULL,
        ccy = "usd",
        financialCenter = "nyb",
        data.source = "internal",
        maturity = tenorList[i],
        sector = "government",
        issuer = "us_treasury",
        timeStamp = "15:00:00",
        unitSett = "d",numUnitsSett = 1, # Bonds settle t+1, repo is t+0
        accBond = 3, # bond accrues act/act
        accRepo = 2, # repo accrues act/360
        fincadCountryCode = "US",
        updateTSDB = TRUE
    )
}

if(!is.null(result)){
    quit(save="no", status = 0)
}else{
    quit(save="no", status=-1)
}