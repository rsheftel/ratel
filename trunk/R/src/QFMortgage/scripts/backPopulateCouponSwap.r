
#Create and back populate the following:

# Coupon Swap Actual
# Coupon Swap Model


backPopulateCouponSwap <- function(){

    library(QFMortgage)

    programs <- c('fncl','fnci')
  
#    if (isWindows()) path <- "V:/TSDB_Upload/Today/"
#        else path <- "/data/TSDB_upload/Today/"

    path <- 'h:/CpnSwap/Compare/'
	modelName <- 'compare' #"qfmodel_couponSwapModel_1.5"
    dv01.source <- 'internal'
	knotPoints <- seq(-3,3,1) #seq(-3,3,0.5)
		
    runStartDate <- as.POSIXct('1995-01-01')
    runEndDate <- as.POSIXct('2009-05-15')
    dataStartDate <- seq(runStartDate,length=2,by='-1000 DSTday')[2]

    
    for (program in programs){
    
        cpns <- TBA$couponVector(program,'all')
        print(paste('start for ',program))

        #Setup the object
        cpnSwap <- TBACouponSwap(program=program, couponVector=cpns, forwardDays=45, modelName=modelName)

        #Just to get the date zoo
        print("Getting historical dates")
        cpnSwap$setStartEndDates(startDate=dataStartDate,endDate=runEndDate)
        cpnSwap$setRawDataFromTSDB()
        cpnSwap$generateActualData()
        dateRange.tba <- zoo(1,as.POSIXct(format(cpnSwap$.dateTimes.vector,"%Y-%m-%d")))
        dateRange.slope <- zoo(1,as.POSIXct(format(index(cpnSwap$.swapRate10yZoo),"%Y-%m-%d")))
        dateRange <- index(merge(dateRange.tba, dateRange.slope, all=FALSE))
        
        #set up the cache history
        print("Getting Historical cache")
        cpnSwap$setHistoryCacheFromTSDB(couponDv01.source=dv01.source)

        #Set up storage arrays
        
        cpnSwapActual <- NULL
        cpnSwapModel <- NULL
        cpnSwapDV01 <- NULL
        cpnSwapHedgeRatio <- NULL
		cpnSwapWeightedRolls <- NULL
        
        if (length(dateRange) >= 1){
            for (counter in (1:length(dateRange))){

                endDate <- dateRange[counter]
                if ((endDate >= runStartDate) & (endDate <= runEndDate)){
                    print(paste('Date :',endDate))
                    startDate <- seq(endDate,length=2,by='-1000 DSTday')[2]
                    cpnSwap$setStartEndDates(startDate=startDate,endDate=endDate)
                    cpnSwap$setRawDataFromCache()
                    cpnSwap$setCouponDv01FromCache()
					cpnSwap$setRollDataFromCache()
                    cpnSwap$generateActualData()
                    cpnSwap$setKnotPoints(knotPoints=knotPoints)
                    cpnSwap$setStartEndDatesForCoupons()
                    cpnSwap$generateWeights()
                    cpnSwap$vectorizeMatrix()
                    cpnSwap$filterVectors()
                    cpnSwap$fitModel()
                
                    results <- cpnSwap$calculateResultsForBackfill()
                
                    cpnSwapActual <- rbind(cpnSwapActual,results$couponSwap.actual)
                    results$couponSwap.model[is.na(results$couponSwap.actual)] <- NA
                    cpnSwapModel  <- rbind(cpnSwapModel,results$couponSwap.model)
                    results$couponSwap.dv01[is.na(results$couponSwap.actual)] <- NA
                    cpnSwapDV01 <- rbind(cpnSwapDV01,results$couponSwap.dv01)
                    results$couponSwap.hedgeRatio[is.na(results$couponSwap.actual)] <- NA
                    cpnSwapHedgeRatio <- rbind(cpnSwapHedgeRatio,results$couponSwap.hedgeRatio)
					results$couponSwap.weightedRolls[is.na(results$couponSwap.actual)] <- NA
					cpnSwapWeightedRolls <- rbind(cpnSwapWeightedRolls,results$couponSwap.weightedRolls)
                }
            }
            write.csv(cpnSwapActual, squish(path,'history_CpnSwap_Actual_',program,'.csv'),row.names=TRUE, quote=FALSE)
            write.csv(cpnSwapModel, squish(path,'history_CpnSwap_Model_',program,'.csv'),row.names=TRUE, quote=FALSE)
            write.csv(cpnSwapDV01, squish(path,'history_CpnSwap_dv01_',program,'.csv'),row.names=TRUE, quote=FALSE)
            write.csv(cpnSwapHedgeRatio, squish(path,'history_CpnSwap_HedgeRatio_',program,'.csv'),row.names=TRUE, quote=FALSE)
			write.csv(cpnSwapWeightedRolls, squish(path,'history_CpnSwap_WgtRolls_',program,'.csv'),row.names=TRUE, quote=FALSE)
            print ('done writing zoo')
        }
    }
}
