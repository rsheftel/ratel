#! /tp/bin/Rscript --no-site-file --no-init-file

rm(list = ls())                   
library(Live)                   

args <- commandArgs()
args <- args[-(1:match("--args", args))]

if(NROW(args)==0){
    dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")
}else{
    dateList <- getRefLastNextBusinessDates(lastBusinessDate = args,holidaySource = "financialcalendar", financialCenter = "nyb")
}

omitForCondition <- function(obj,condition){
	if(any(condition)){
		obj[condition] <- NA
		obj <- na.omit(obj)
	}
	return(obj)
}

tsdb <- TimeSeriesDB()
ids <- EquityDataLoader$getSecurityIDsUniverse()
for (type in c("open","high","low","close")){
	print(squish("Working on ",type))

	matrix <- tsdb$retrieveTimeSeriesByAttributeList(attributes = list(
	    security_id = ids,
	    quote_type = type,
	    quote_convention = "price",
	    quote_side = "mid",
	    instrument = "equity"
	), data.source = "ivydb", start = dateList$lastBusinessDate, end = dateList$lastBusinessDate) 
	for(i in 1:NROW(matrix)){			
		matrix[[i]] <- omitForCondition(matrix[[i]],as.numeric(matrix[[i]]) == 0)
		if(type == "close")
			if(is.numeric(matrix[[i]]))matrix[[i]] <- abs(matrix[[i]])
		if(type %in% c("high","low"))
			matrix[[i]] <- omitForCondition(matrix[[i]],as.numeric(matrix[[i]]) < 0)
	}
	TSDataLoader$loadMatrixInTSDB(matrix,Close$NY.equity,"internal")
	cat(squish("\nEquity ",type," prices were updated successfully!\n"))
}

quit(save="no", status = 0)