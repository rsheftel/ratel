#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(Live) 

args <- commandArgs()
args <- args[-(1:match("--args", args))]

########################################################################

ids <- EquityDataLoader$getSecurityIDsUniverse()

nbSteps = as.numeric(args[1])
step = as.numeric(args[2])

print("Close")
result <- EquityDataLoader$calcAdjClosePrices(ids,nbSteps,step)
if(!result) quit(save="no", status = -1)
print("Open")
result <- EquityDataLoader$calcAdjOpenPrices(ids,nbSteps,step)
if(!result) quit(save="no", status = -1)
print("High")
result <- EquityDataLoader$calcAdjHighPrices(ids,nbSteps,step)
if(!result) quit(save="no", status = -1)
print("Low")
result <- EquityDataLoader$calcAdjLowPrices(ids,nbSteps,step)
if(!result) quit(save="no", status = -1)