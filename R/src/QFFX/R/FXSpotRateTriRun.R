FXSpotRateTriRun <- function(FXCurr = NULL,startDate = NULL, 
	endDate = NULL, tsdb = NULL, writeToTSDB = FALSE, overUnder = "over",BackPopulate=FALSE,close=NULL,...)
{
	#Date range for data request
	initialDate <- startDate
	finalDate <- endDate
	dataLoad <- FXDataLoad(FXCurr,initialDate,finalDate,tsdb,tenor="1m",close=close)
	
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
	tenor <- "spot"
	if (!is.null(close)) quote.convention <- squish(quote.convention,close)
	fxNotional <- FXNotional(0.0001,"over")
	ccy.pair <- squish(FXCurr$over(), FXCurr$under())
	time.series.name <- squish(ccy.pair,"_",tenor,"_",quote.convention)
	time.series.source <- "internal"
	initialEquity <- 200
			
	if (BackPopulate==FALSE) {
		eq.info <- getLastEquity(FXCurr=FXCurr, tenor = tenor,obsDate = startDate, tsdb = tsdb)
		initialEquity <- as.numeric(eq.info)
		cat("Equity:",initialEquity,"\n")
	}
	
	fxhisttri <- FXSpotRateTri(FXCurr=FXCurr, fxNotional=fxNotional, startDate=startDate, endDate=endDate, tsdb=tsdb, 
				DataLoad=dataLoad, overUnder=overUnder,initialEquity=initialEquity)
	fxhisttri$computeEquityCurve()
	pnl <- fxhisttri$.currPnL  
	
	if (writeToTSDB==TRUE){
		if (BackPopulate==TRUE) tsdb$purgeTimeSeries(name=time.series.name, data.source=time.series.source)
		cat("Writing to DB:",time.series.name,"\n")
		tsdb <- TimeSeriesDB()
		tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(time.series.name, time.series.source))
		tsMatrix[[1,1]] <- pnl
		fx.data <- tsdb$writeTimeSeries(tsMatrix)
	}
	else return(pnl)
}

getLastEquity <- function(FXCurr = NULL, tenor = NULL,obsDate = NULL, tsdb = NULL,...)
{
	quote.convention <- "tri_local_ccy"
	ccy.pair <- squish(FXCurr$over(), FXCurr$under())
	time.series.name <- squish(ccy.pair,"_",tenor,"_", quote.convention)
	time.series.source <- "internal"
	tri <- tsdb$retrieveOneTimeSeriesByName(name=time.series.name,data.source=time.series.source,start=obsDate,end=obsDate)
	return(tri)
}
