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

tenorList <- TermStructure$irs

calculator <- IRSTri()

for (i in 1:NROW(tenorList)){

    print(tenorList[i])

    #tsdb$purgeTimeSeries(paste("irs_usd_tri",tenorList[i],"mid",sep = "_"),data.source = "internal")
    #tsdb$purgeTimeSeries(paste("irs_usd_tri_daily",tenorList[i],"mid",sep = "_"),data.source = "internal")
    #tsdb$purgeTimeSeries(paste("irs_usd_dv01",tenorList[i],"mid",sep = "_"),data.source = "internal")    
    #tsdb$purgeTimeSeries(paste("irs_usd_convexity",tenorList[i],"mid",sep = "_"),data.source = "internal")        

    result <- IRSTri$run(
        startDate = as.character(dateList$refBusinessDate),
        endDate = NULL,
        ccy = "usd",
        financialCenterFixed = "nyb",financialCenterFloat = "lnb",
        tenorFixed = tenorList[i],tenorFloat = "3m",
        instrumentFixed = "irs",instrumentFloat = "libor",
        sourceFixed = "internal",sourceFloat = "internal",
        timeStampFixed = "15:00:00",timeStampFloat = "15:00:00",
        termStructureFixed = TermStructure$irs,termStructureFloat = TermStructure$libor,
        unitSettFixed = "d",numUnitsSettFixed = 2,
        unitSettFloat = "d",numUnitsSettFloat = 2,
        freqFixed = 2,accFixed = 4, # this is coming from the fincad sw_331 table: 30/360
        freqFloat = 4,accFloat = 2, # this is coming from the fincad sw_331 table: actual/360
        updateTSDB = TRUE
    )
}     

if(!is.null(result)){
    quit(save="no", status = 0)
}else{
    quit(save="no", status=-1)
}
