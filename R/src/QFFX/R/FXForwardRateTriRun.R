FXForwardRateTriRun <- function(FXCurr = NULL, tenorList = NULL,startDate = NULL, 
	endDate = NULL, tsdb = NULL, writeToTSDB = FALSE, overUnder = "over",rebalPeriod = "1d",BackPopulate=FALSE,close=NULL,...)
{
	#Date range for data request
	quoteside <- c("mid")
	initialDate <- startDate
	finalDate <- endDate
	dataLoad <- FXDataLoad(FXCurr,initialDate,finalDate,tsdb,tenor="2y",close=close)
	if (!any(startDate==index(dataLoad$getRateData()))) {
		newStartDate <- as.character(index(dataLoad$getRateData())[1])
		cat("Warning: Requested start date:",startDate," Actual Start Date:",newStartDate,"\n")
		startDate <- newStartDate 
	}
	if (!any(endDate==index(dataLoad$getRateData()))) {
		newEndDate <- as.character(index(dataLoad$getRateData())[length(index(dataLoad$getRateData()))])
		cat("Warning: Requested end date:",endDate," Actual end date:",newEndDate,"\n")
		endDate <- newEndDate
	}
	quote.convention <- "tri_local_ccy"
	if (!is.null(close)) quote.convention <- squish(quote.convention,close)
	fxNotional <- FXNotional(0.001,"over")
	ccy.pair <- squish(FXCurr$over(), FXCurr$under())
	rebal_type <- rebalPeriod
	for (i in 1:(length(tenorList))){
			tenor <- tenorList[i]
			if (tenor == "spot") {
				fwd.generic <- FXForwardGeneric(FXCurr,quoteside,tenor="1w")
				time.series.name <- squish(ccy.pair,"_",tenor,"_",quote.convention)
				rebalPeriod = "1d"
			}
			else {
				fwd.generic <- FXForwardGeneric(FXCurr,quoteside,tenor)
			    time.series.name <- squish(ccy.pair,"_",tenor,"_",quote.convention,"_",rebal_type)
			}
			time.series.source <- "internal"
			
			lastRollInfo <- NULL
			initialEquity <- NULL
			
			if (BackPopulate==FALSE) {
				start.date.info <- getLastEquityAndRollInfoForFXForwardTri(FXCurr=FXCurr, tenor = tenor,obsDate = startDate, 
					tsdb = tsdb,rebalPeriod = rebalPeriod)
				initialEquity <- start.date.info$tri
				lastRollDate <- as.numeric(start.date.info$lastRollDate)
				rate <- as.numeric(start.date.info$rate)
				expiryDate <- as.numeric(start.date.info$expiryDate)
				settleDate <- as.numeric(start.date.info$settleDate)
				cat("Last Roll Date:",as.character(as.POSIXct(lastRollDate)),"\n")
				cat("Equity:",initialEquity,"\n")
				cat("Rate:",rate,"\n")
				cat("Expiry:",as.character(as.POSIXct(expiryDate)),"\n")
				cat("Settle Date:",as.character(as.POSIXct(settleDate)),"\n")
				lastRollInfo <- data.frame(lastRollDate,rate,expiryDate,settleDate)
				colnames(lastRollInfo) <- c("rollDate","rate","expiryDate","settleDate")
			}
			fxhisttri <- FXForwardRateTri(FXForwardObj=fwd.generic, fxNotional=fxNotional, startDate=startDate, endDate=endDate, tsdb=tsdb, 
				DataLoad=dataLoad, overUnder=overUnder,initialEquity=initialEquity, lastRollInfo=lastRollInfo)
			fxhisttri$computeEquityCurve(rebalPeriod=rebalPeriod,rebalDelta=rebalDelta)
			pnl <- fxhisttri$shared()$getCurrPnL()  
			rollInfo <- fxhisttri$shared()$getRollHistory()
			if (writeToTSDB==TRUE){
				if (BackPopulate==TRUE) tsdb$purgeTimeSeries(name=time.series.name, data.source=time.series.source)
				cat("Writing to DB:",time.series.name,"\n")
				tsdb <- TimeSeriesDB()
				tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(time.series.name, time.series.source))
				tsMatrix[[1,1]] <- pnl
				fx.data <- tsdb$writeTimeSeries(tsMatrix)
				
				saveRollData <- function(series.suffix, internal.name) {
					if (tenor == "spot") roll.time.series.name <- squish(ccy.pair,"_", tenor, "_tri_",series.suffix)
					else roll.time.series.name <- squish(ccy.pair,"_", tenor, "_tri_",rebal_type,"_",series.suffix)
					if (BackPopulate==TRUE) tsdb$purgeTimeSeries(name=roll.time.series.name, data.source=time.series.source)
					cat("Writing to DB:",roll.time.series.name,"\n")
					tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(roll.time.series.name, time.series.source))
					tsMatrix[[1,1]] <- zoo(as.numeric(coredata(rollInfo[,internal.name])),order.by=index(rollInfo))
					
					fx.data <- tsdb$writeTimeSeries(tsMatrix)
				}
				saveRollData("lastrolldate","rollDate")
				saveRollData("rate","rate")
				saveRollData("expirydate","expiryDate")
				saveRollData("settledate","settleDate")	
			}
			if (writeToTSDB==FALSE) return(merge(pnl,rollInfo))
	}
}

getLastEquityAndRollInfoForFXForwardTri <- function(FXCurr = NULL, tenor = NULL,obsDate = NULL, 
	tsdb = NULL,rebalPeriod = "1d",...)
{
	quote.convention <- "tri_local_ccy"
	ccy.pair <- squish(FXCurr$over(), FXCurr$under())
	rebal_type <- rebalPeriod
	
	if (tenor=="spot") time.series.name <- squish(ccy.pair,"_",tenor,"_", quote.convention)
	else time.series.name <- squish(ccy.pair,"_", tenor,"_",quote.convention,"_",rebal_type)
	
	time.series.source <- "internal"
	tri <- tsdb$retrieveOneTimeSeriesByName(name=time.series.name,data.source=time.series.source,start=obsDate,end=obsDate)
	
	quote.convention <- "tri"
	getData <- function(dataType) {
		if (tenor == "spot") time.series.name <- paste(ccy.pair, tenor, quote.convention,dataType,sep = "_")
		else time.series.name <- paste(ccy.pair, tenor, quote.convention, rebal_type,dataType,sep = "_")
		return(tsdb$retrieveOneTimeSeriesByName(name=time.series.name,data.source=time.series.source,start=obsDate,end=obsDate))
	}
	lastRollDate <- getData("lastrolldate")
	rate <- getData("rate")
	expiryDate <- getData("expirydate")
	settleDate <- getData("settledate") 
	
	return(list(tri=tri,lastRollDate=lastRollDate,rate=rate,expiryDate=expiryDate,settleDate=settleDate))
}
