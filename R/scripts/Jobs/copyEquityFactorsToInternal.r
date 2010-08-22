#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(fincad)                   
library(GSFCore)                  
library(RUnit)
library(RJDBC)        
library(QFEquity)
library(QFFixedIncome)
library(QFCredit)

###########################################################################################################################

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
        quote_type = "close",
        quote_convention = "total_return_factor",
        quote_side = "mid",
        instrument = "equity"
), data.source = "ivydb", start = dateList$lastBusinessDate, end = dateList$lastBusinessDate)
    
# basic arbitration

if(!is.null(matrix)){
    r <- rownames(matrix)        
    tycoTsName <- "ivydb_111283_total_return_factor_mid"
    m <- match(tycoTsName,r)
    ivydbValue <- matrix[[m]][[1]]
    if(!is.null(ivydbValue)){
        if(ivydbValue < 4.22977)matrix[[m]][[1]] <- 4.22977
    }
}

TSDataLoader$loadMatrixInTSDB(matrix,Close$NY.equity,"internal")

cat("\nEquity factors were updated successfully!\n")
quit(save="no", status = 0)