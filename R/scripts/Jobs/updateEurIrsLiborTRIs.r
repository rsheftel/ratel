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
    dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "lnb")
}else{                                       
    dateList <- getRefLastNextBusinessDates(lastBusinessDate = args,holidaySource = "financialcalendar", financialCenter = "lnb")
}

########################################################################

tenorList <- TermStructure$irs[-16]

calculator <- IRSTri()

for (i in 1:NROW(tenorList)){

    print(tenorList[i])

    result <- IRSTri$run(
        startDate = as.character(dateList$refBusinessDate),
        endDate = NULL,
        ccy = "eur",
        financialCenterFixed = "lnb",financialCenterFloat = "lnb",
        tenorFixed = tenorList[i],tenorFloat = "3m",
        instrumentFixed = "irs",instrumentFloat = "libor",
        sourceFixed = "internal",sourceFloat = "internal",
        timeStampFixed = "11:00:00",timeStampFloat = "11:00:00",
        termStructureFixed = tenorList,termStructureFloat = TermStructure$libor,
        unitSettFixed = "d",numUnitsSettFixed = 2,
        unitSettFloat = "d",numUnitsSettFloat = 2,
        freqFixed = 1,accFixed = 4, # this is coming from the fincad sw_331 table: 30/360
        freqFloat = 4,accFloat = 2, # this is coming from the fincad sw_331 table: actual/360
        updateTSDB = TRUE
    )
}                

if(!is.null(result)){
    quit(save="no", status = 0)
}else{
    quit(save="no", status=-1)
}
