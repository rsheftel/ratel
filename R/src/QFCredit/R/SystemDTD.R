setConstructorS3("SystemDTD", function(...)
{
	library(QFFixedIncome)
	library(QFEquity)
    extend(RObject(), "SystemDTD",
        .transformationName = NULL,
        .volExpiry = NULL,
        .optionType = NULL,
        .fairValues = NULL,
        .richCheaps = NULL,
        .loessInput = NULL,
        .cdsData = NULL,
        .irsData = NULL,
    	.sharePricesData = NULL,
    	.volatilityData = NULL,
    	.sharesOutstandingData = NULL,
    	.bloombergLiabilitiesData = NULL,	
        .dataDTD = NULL,
        .dataA = NULL,
        .dataSigmaA = NULL,
        .useConstrOptim = NULL,
        .updateTSDB = NULL
    )
})

setMethodS3("tickerUniverse", "SystemDTD", function(this,...)
{
	sectorTab <- read.csv(system.file("credit_data","dtd_universe.csv", package = "QFCredit"), sep = ",", header = FALSE)
	as.character(sectorTab[,1])
})

setMethodS3("runDTD","SystemDTD",function(this, transformationName = "dtd_1.0",volExpiry, optionType, tickerList, tenor,
    updateTSDB,useConstrOptim,refDate = NULL, startDate = NULL, endDate = NULL,...)
{    
    this$.cdsData <- SingleNameCDS$multipleGenericSeries(tickerList,'spread',startDate=startDate,endDate=endDate)	
	this$.assetValueData <- this$prepareSeedData(SingleNameCDS$multipleAssetSeries(tickerList,startDate=refDate,endDate=endDate))
	this$.assetVolData <- this$prepareSeedData(SingleNameCDS$multipleAssetVolSeries(tickerList,startDate=refDate,endDate=endDate))
    this$.irsData <- IRSDataLoader$getIRS(startDate,endDate)
    this$.sharePricesData <- SingleNameCDS$multipleAdjPriceSeries(tickerList,startDate=startDate,endDate=endDate)
    this$.volatilityData <- SingleNameCDS$multipleVolSeries(tickerList,startDate=startDate,endDate=endDate)
    this$.volExpiry <- volExpiry
    this$.optionType <- optionType
    this$.sharesOutstandingData <- SingleNameCDS$multipleSharesOutstandingSeries(tickerList,startDate=startDate,endDate=endDate)
    this$.bloombergLiabilitiesData <- SingleNameCDS$multipleLiabilitySeries(tickerList,startDate=startDate,endDate = endDate)
	this$.tenor <- tenor
	this$.tickerList <- tickerList
	if(!is.null(startDate))this$.startDate <- as.POSIXlt(startDate)
	if(!is.null(endDate))this$.endDate <- as.POSIXlt(endDate)

    needs(updateTSDB = "logical")
    this$.updateTSDB <- updateTSDB
    
    needs(useConstrOptim = "logical")
    this$.useConstrOptim <- useConstrOptim    
    
    needs(transformationName = "character")
    this$.transformationName <- transformationName    
    if(is.null(this$.cdsData) || is.null(this$.irsData) || is.null(this$.sharePricesData) || is.null(this$.volatilityData)
        || is.null(this$.sharesOutstandingData) || is.null(this$.bloombergLiabilitiesData)
    )throw("SystemDTD$setTerms: no valid data to run DTD")

    this$runMultipleTickers()
})

setMethodS3("prepareSeedData", "SystemDTD", function(this,seedZoo,...)
{	
	if(is.null(seedZoo))return(seedZoo)
	res <- getZooDataFrame(lag(na.locf(seedZoo),-1),colnames(seedZoo))	
	res
})

setMethodS3("filterDTD", "SystemDTD", function(this,result,...)
{
	if(all(is.na(result$dtdOutputDataDTD))) return(result)
	bools <- result$dtdOutputDataDTD <= -10
	result$dtdOutputDataDTD[bools] <- NA
	result$dtdOutputDataA[bools] <- NA
	result$dtdOutputDataSigmaA[bools] <- NA	
	result
})

setMethodS3("runMultipleTickers", "SystemDTD", function(this,...)
{
    dtdOutputDataDTD <- NULL                                                                             
    dtdOutputDataA <- NULL
    dtdOutputDataSigmaA <- NULL
        
    successArray <- array(FALSE,dim =c(NROW(this$.tickerList),3),dimnames  = list(this$.tickerList,c("a","dtd","sga")))

    for(i in 1:length(this$.tickerList)){

        ticker <- as.character(this$.tickerList[i])

    	result <- this$runSingleDTD(ticker = ticker)
		
		result <- this$filterDTD(result)

        if(this$.updateTSDB){			
            successArray[i,1] <- this$updateTSDB(quote_type = "dtd_asset",ticker = ticker,zooObj = result[[2]])              
            successArray[i,2] <- this$updateTSDB(quote_type = "dtd",ticker = ticker,zooObj = result[[1]])              
            successArray[i,3] <- this$updateTSDB(quote_type = "dtd_sigma",ticker = ticker,zooObj = result[[3]])
        }
    	
        dtdOutputDataDTD = c(dtdOutputDataDTD,list(result[[1]]))
        dtdOutputDataA = c(dtdOutputDataA,list(result[[2]]))
        dtdOutputDataSigmaA = c(dtdOutputDataSigmaA,list(result[[3]]))               
    }

    this$.dataDTD <- getZooDataFrame(do.call(merge.zoo,dtdOutputDataDTD))
    this$.dataA <- getZooDataFrame(do.call(merge.zoo,dtdOutputDataA))
    this$.dataSigmaA <- getZooDataFrame(do.call(merge.zoo,dtdOutputDataSigmaA))
    
    return(successArray)
})

setMethodS3("updateTSDB", "SystemDTD", function(this,quote_type,ticker,zooObj,toUpper = FALSE,...)
{
	cds <- SingleNameCDS(ticker)
	cds$uploadGeneric(quote_type,na.omit(zooObj),purgeTimeSeries = FALSE)	
	TRUE
})

setMethodS3("runSingleDTD", "SystemDTD", function(this, ticker, ...)
{	
    numTenor <- characterToNumericTenor(this$.tenor)    

    # merge all of the data for a given ticker
			
    tickerData <- merge(		
		this$.cdsData[,ticker],
		this$.irsData[,this$.tenor],
		this$.volatilityData[,ticker],
		this$.sharesOutstandingData[,ticker],
		this$.sharePricesData[,ticker],
		this$.bloombergLiabilitiesData[,ticker]		
	,all = FALSE)	

    tickerData <- getZooDataFrame(tickerData)    

    dtdObject <- DTDCalculator()        

    #calc DTDs for every day for that ticker

    output <- mapply(
	    this$getDTDHistory,
        rate = tickerData[,2],
        sigmaE = tickerData[,3],
        shares = tickerData[,4],
        s = tickerData[,5],
        liability = tickerData[,6],
        effDate = index(tickerData[,2]),
        MoreArgs = list(time = numTenor,obj = dtdObject,ticker = ticker)
    )
      	
	dtdOutputDataDTD = getZooDataFrame(zoo(output[1,],order.by = as.POSIXct(index(tickerData[,1]))))
    colnames(dtdOutputDataDTD) <- ticker
	dtdOutputDataA = getZooDataFrame(zoo(output[2,],order.by = as.POSIXct(index(tickerData[,1]))))
    colnames(dtdOutputDataA) <- ticker
	dtdOutputDataSigmaA = getZooDataFrame(zoo(output[3,],order.by = as.POSIXct(index(tickerData[,1]))))
    colnames(dtdOutputDataSigmaA) <- ticker

    return(list(
	    dtdOutputDataDTD = dtdOutputDataDTD,
	    dtdOutputDataA = dtdOutputDataA,
	    dtdOutputDataSigmaA = dtdOutputDataSigmaA
	))
}) 

setMethodS3("getDTDHistory", "SystemDTD", function(this, obj,rate, sigmaE, shares, s, liability, effDate, time,ticker,...)
{
	print(effDate)
	
	if(is.null(this$.seedAsset) || is.null(this$.seedSigmaA)){
		# If first calc, het last values from historical
		getSeedFromHistorical <- function(dataZoo){			
			as.numeric(dataZoo[effDate,ticker])			
		}
		this$.seedAsset <- try(getSeedFromHistorical(this$.assetValueData),TRUE)
		this$.seedSigmaA <- try(getSeedFromHistorical(this$.assetVolData),TRUE)		
	}
	
	if(NROW(na.omit(this$.seedAsset))==0)this$.seedAsset <- 1
	if(NROW(na.omit(this$.seedSigmaA))==0)this$.seedSigmaA <- 0.5	
	
    ifelse(any(is.na(c(rate, sigmaE, shares, s, liability, effDate, time))) ||
        any(c(rate,sigmaE,shares,s,liability, effDate, time) == 0),
            output.getDTDs <- c(NA,NA,NA),      # 3 NA's because DTDCalculator returns 3 fields
            output.getDTDs <- obj$runDTD(
				seedSigmaA = this$.seedSigmaA,
				seedAsset = this$.seedAsset,
                rate = rate,
                sigmaE = sigmaE,
                shares = shares,
                s = s,
                liability = liability,                
                time = time,
                useConstrOptim = this$.useConstrOptim
    ))
   
	this$.seedAsset <- output.getDTDs[2]
	this$.seedSigmaA <- output.getDTDs[3]
	
    return(output.getDTDs)
})