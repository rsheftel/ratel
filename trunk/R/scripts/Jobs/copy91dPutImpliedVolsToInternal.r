#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(fincad)                   
library(GSFCore)                  
library(RUnit)
library(RJDBC)                 
library(GSFAnalytics)
library(QFEquity)
library(QFFixedIncome)
library(QFCredit)

args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
    dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else{
    dateList <- getRefLastNextBusinessDates(lastBusinessDate = args,holidaySource = "financialcalendar", financialCenter = "nyb")
}

########################################################################

# get data from "ivydb"
    
matrix <- TimeSeriesDB()$retrieveTimeSeriesByAttributeList(attributes = list(
        security_id = EquityDataLoader$getSecurityIDsUniverse(),
        expiry = "91d",
        option_type = "put",
        quote_type = "close",
        quote_convention = "vol_ln",
        quote_side = "mid",
        strike = "atm",
        instrument = "std_equity_option"
), data.source = "ivydb", start = dateList$lastBusinessDate, end = dateList$lastBusinessDate)

# Ivydb -99.99 removal (case where vol surface interpolation was not possible)

if(!is.null(matrix)){
	for(i in 1:NROW(matrix)){
		if(any(as.numeric(matrix[[i]]) == -99.99)){
			matrix[[i]][as.numeric(matrix[[i]]) == -99.99] <- NA
			matrix[[i]] <- na.omit(matrix[[i]])
		}
		if(any(as.numeric(matrix[[i]]) <=0)){
			matrix[[i]][as.numeric(matrix[[i]]) <=0] <- NA
			matrix[[i]] <- na.omit(matrix[[i]])
		}
	}
}

TSDataLoader$loadMatrixInTSDB(matrix,Close$NY.equity,"internal")

cat("\nEquity implied vols were updated successfully!\n")
quit(save="no", status = 0)