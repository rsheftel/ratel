#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(QFMortgage) 

args <- commandArgs()
args <- args[-(1:match("--args", args))]

destination <- 'team'

sixDaysFromNow <- businessDaysAgo(-6,args)
fiveDaysFromNow <- businessDaysAgo(-5,args)

fnclsixDaysFromNow <- TBA$frontSettle('fncl',sixDaysFromNow)
fnclfiveDaysFromNow <- TBA$frontSettle('fncl',fiveDaysFromNow)
fncisixDaysFromNow <- TBA$frontSettle('fnci',sixDaysFromNow)
fncifiveDaysFromNow <- TBA$frontSettle('fnci',fiveDaysFromNow)

message <- NULL
if(fnclsixDaysFromNow != fnclfiveDaysFromNow)
	message <- squish('FNCL: front settlement date changes on ',as.character(sixDaysFromNow)) 

if(fncisixDaysFromNow != fncifiveDaysFromNow)
	message <-paste(message,squish('FNCI: front settlement date changes on ',as.character(sixDaysFromNow)),sep = '\n')

# Send EMails
if(!is.null(message))Mail$notification("TBA Roll",message)$sendTo(destination)