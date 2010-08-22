#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())
library(fincad)
library(GSFCore)
library(RUnit)
library(RJDBC)
library(QFCredit)
library(QFEquity)
library(QFFixedIncome)

########################################################################

tenorList <- TermStructure$us_treasury
calculator <- BondModifiedSeries()

for (i in 1:NROW(tenorList)){
    result <- calculator$updateContinuousYieldsSeriesInTSDB(tenorList[i],front = "otr",back = "1o",modified = "1c",adjustmentType = "ratio")
    if(!result)quit(save="no", status = -1)
}

quit(save="no", status=0)