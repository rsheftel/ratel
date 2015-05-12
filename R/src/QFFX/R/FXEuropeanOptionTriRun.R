FXEuropeanOptionTriRun <- function(FXCurr = NULL, tenorList = NULL, putCallList=NULL,startDate = NULL, 
  endDate = NULL, tsdb = NULL, writeToTSDB = FALSE, overUnder = "over",rebalPeriod = "2w", rebalDelta=0.15,BackPopulate=FALSE,...)
{
	#Date range for data request
	initialDate <- startDate
   	finalDate <- endDate
   	quoteside <- c("mid")
	dataLoad <- FXDataLoad(FXCurr,initialDate,finalDate,tsdb,tenor="2y")
	
	if (!any(startDate==index(dataLoad$getVolData()))) {
	   	newStartDate <- as.character(index(dataLoad$getVolData())[1])
	   	cat("Warning: Requested start date:",startDate," Actual Start Date:",newStartDate,"\n")
		startDate <- newStartDate 
   	}
   	if (!any(endDate==index(dataLoad$getVolData()))) {
	   	newEndDate <- as.character(index(dataLoad$getVolData())[length(index(dataLoad$getVolData()))])
	   	cat("Warning: Requested end date:",endDate," Actual end date:",newEndDate,"\n")
	   	endDate <- newEndDate
	}
  	quote.convention <- "tri_local_ccy"
   	fxNotional <- FXNotional(0.001,"over")
   	ccy.pair <- squish(FXCurr$over(), FXCurr$under())
   	rebal_type <- squish(rebalPeriod,(rebalDelta*100),"d")
   	for (i in 1:(length(tenorList))){
    	for (optionType in putCallList){
    		tenor <- tenorList[i]
    		fwd.generic <- FXForwardGeneric(FXCurr,quoteside,tenor)
			time.series.name <- paste(ccy.pair, tenor, optionType, quote.convention, rebal_type,sep = "_")
			time.series.source <- "internal"
			option.generic <- FXEuropeanOptionGeneric(fwd.generic,expiry = tenor, putCall = optionType)
   	 		
			lastRollInfo <- NULL
			initialEquity <- 0
    		
			if (BackPopulate==FALSE) {
      			start.date.info <- FXOptionRollInfo$getLastEquityAndRollInfo(currencyPair=FXCurr, tenor = tenor, 
					putCall=optionType,obsDate = startDate, tsdb = tsdb,rebalPeriod = rebalPeriod, rebalDelta=rebalDelta)
      			initialEquity <- start.date.info$tri
      			lastRollDate <- as.numeric(start.date.info$lastRollDate)
				strike <- as.numeric(start.date.info$strike)
				expiryDate <- as.numeric(start.date.info$expiryDate)
				settleDate <- as.numeric(start.date.info$settleDate)
      			cat("Last Roll Date:",as.character(as.POSIXct(lastRollDate)),"\n")
				cat("Equity:",initialEquity,"\n")
				cat("Strike:",strike,"\n")
				cat("Expiry:",as.character(as.POSIXct(expiryDate)),"\n")
				cat("Settle Date:",as.character(as.POSIXct(settleDate)),"\n")
				lastRollInfo <- data.frame(lastRollDate,strike,expiryDate,settleDate)
				colnames(lastRollInfo) <- c("rollDate","strike","expiryDate","settleDate")
			}
			fxhisttri <- FXEuropeanOptionTri(FXOptionObj=option.generic, fxNotional=fxNotional, startDate=startDate, 
				endDate=endDate, tsdb=tsdb, DataLoad=dataLoad, overUnder=overUnder,initialEquity=initialEquity, 
				lastRollInfo=lastRollInfo)
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
				
				rollPopulation <- function(tsdbRollName,rollData) {
					roll.time.series.name <- paste(ccy.pair, tenor, optionType, "tri", rebal_type,tsdbRollName,sep = "_")
					if (BackPopulate==TRUE) tsdb$purgeTimeSeries(name=roll.time.series.name, data.source=time.series.source)
					cat("Writing to DB:",roll.time.series.name,"\n")
					tsMatrix <- array(list(NULL),dim=c(1,1),dimnames = list(roll.time.series.name, time.series.source))
					tsMatrix[[1,1]] <- zoo(as.numeric(coredata(rollInfo[,rollData])),order.by=index(rollInfo))
					fx.data <- tsdb$writeTimeSeries(tsMatrix)
				}
				
				rollPopulation(tsdbRollName="lastrolldate",rollData="rollDate")
				rollPopulation(tsdbRollName="strike",rollData="strike")
				rollPopulation(tsdbRollName="expirydate",rollData="expiryDate")
				rollPopulation(tsdbRollName="settledate",rollData="settleDate")
				
				
  			}
 			if (writeToTSDB==FALSE) return(merge(pnl,rollInfo))
   		}
	}
}
