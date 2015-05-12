setConstructorS3("DTDRichCheapCalculator", function(...)
{
	library(QFFixedIncome)
    extend(RObject(), "DTDRichCheapCalculator",
        .transformationName = NULL,
        .rateTable = NULL,
        .spreadInfo = NULL,
        .dtdinfo = NULL,
        .tenor = NULL,
        .time = NULL,
        .coefficients = NULL,
        .fairValues = NULL,
        .MINDATAPTS = NULL,
        .baseDate = NULL,
        .matDate = NULL,
        .richCheaps = NULL,
        .rsqs = NULL,
        .deltas = NULL,
        .hedgeType = NULL,  # "price" or "value"
		.dtdShift = NULL,
		.minAcceptableDTD = NULL,
		.maxAcceptableDTD = NULL,
		.thresholdOAS = NULL,
		.DTDshift = NULL,
		.loessfun = NULL,
        .updateTSDB = NULL,
        .tsdb = TimeSeriesDB(),
		.searchDTDs = NULL,
		.testPredictions = NULL,
		.minScaleFactor = 0.01
    )
})

setMethodS3("initialize", "DTDRichCheapCalculator", function(this, transformationName,dtdinfo, 
															 spreadinfo, rateTable, tenor, hedgeType, 
															 minAcceptableDTD = -4, maxAcceptableDTD = 4, thresholdOAS = 0.15, ...)
{    
    print("initializing")
    
    assert(ncol(dtdinfo) == ncol(spreadinfo), "DTDRichCheapCalculator needs the number of columns in the spread input to equal the number of columns in the DTD input")
    tenorList <- c("6m","1y","2y","3y","4y","5y","7y","10y","15y","20y","30y")
    assert(any(tenor == tenorList), "DTDRichCheapCalculator needs a correct tenor for CDS")
    assert(any(tenor == colnames(rateTable)), "DTDRichCheapCalculator needs a correct tenor in the rate input")
    assert(any(hedgeType == c("price","value")), "DTDRichCheapCalculator needs a correct hedging methodology")
    
    this$.MINDATAPTS <- 50           # definition of the minimum data pts necessary to run the regression    
    this$.tenor <- tenor
    this$.rateTable <- rateTable
    this$.time <- characterToNumericTenor(this$.tenor)
    this$.baseDate <- as.POSIXct(Sys.Date())    # Start Date to use when pricing options(Fincad requires a start and end date
    this$.matDate <- seq(from = this$.baseDate,by = paste(this$.time*12,"month"),length=2)[2]
    this$.hedgeType <- hedgeType              
    this$.spreadInfo <- spreadinfo  
    this$.dtdinfo <- dtdinfo
	this$.dtdShift <- 0
	this$.minAcceptableDTD <- minAcceptableDTD
	this$.maxAcceptableDTD <- maxAcceptableDTD
	this$.thresholdOAS <- thresholdOAS           
    
    needs(transformationName = "character")
    this$.transformationName <- transformationName      
})

setMethodS3("calcLoessCoefficients", "DTDRichCheapCalculator", function(this,updateTSDB = FALSE,...)
{
    print("getting Loess Coefficients")

    rowNum <- nrow(this$.spreadInfo)
    
    this$.coefficients <- zoo(matrix(nrow = rowNum, ncol = 5), order.by = index(this$.spreadInfo))
    
    for(i in 1:rowNum)
    {
        todayRate <- this$.rateTable[index(this$.spreadInfo[i,]),this$.tenor]/100
        spreadData <- this$.spreadInfo[i,]
        dtdData <- this$.dtdinfo[index(this$.spreadInfo[i,]),]
        print(index(this$.spreadInfo[i,]))
        this$.coefficients[i,] <- this$createDTDDataFrame(dtdData, spreadData, todayRate)
    }
    
    index(this$.coefficients) <- index(this$.spreadInfo)
    colnames(this$.coefficients) <- c("DTDMedian","ScalingFactor","VolMedian", "DTDShift","OASZero")
    
    if(updateTSDB)this$updateCoefficientsInTSDB()
})

setMethodS3("updateCoefficientsInTSDB", "DTDRichCheapCalculator", function(this,...)
{
    if(length(na.omit(this$.coefficients)) == 0)return(FALSE)
    
    quote_types <- c("dtd_median","scaling_factor","vol_median", 'dtd_shift','oas_zero')
    
    for(i in 1:NROW(quote_types)){
        tsName <- paste(this$.transformationName,quote_types[i],sep = "_")
        if(!(this$.tsdb)$timeSeriesExists(tsName)){                    
                (this$.tsdb)$createTimeSeries(
                    tsName,
                    attributes = list(transformation =  this$.transformationName,transformation_output = quote_types[i])
                )
        }
        tsArray <- array(list(NULL),dim = c(1,1),dimnames = list(tsName, "internal"))
        tsArray[[1,1]] <- getZooDataFrame(na.omit(this$.coefficients[,i]))
        (this$.tsdb)$writeTimeSeries(tsArray)
    }
    return(TRUE)
})

setMethodS3("calcRichCheap", "DTDRichCheapCalculator", function(this, transformationName,dtdinfo, spreadinfo, rateTable, tenor, hedgeType,updateTSDB = FALSE,...)
{   
    this$initialize(transformationName,dtdinfo, spreadinfo, rateTable, tenor, hedgeType)
    
    this$calcLoessCoefficients(updateTSDB = updateTSDB)
    
    print("getting fair values")
    
    tickNames <- colnames(this$.dtdinfo)
    dateList <- index(this$.dtdinfo)
    
    this$.fairValues <- t(sapply(as.character(dateList), this$runSingleDayFV, tickers = tickNames, dtdinfo = this$.dtdinfo, rateTable = this$.rateTable))
    
    this$.fairValues <- zoo(this$.fairValues, order.by = as.POSIXct(row.names(this$.fairValues)))
    
    this$.richCheaps <- t(sapply(as.character(dateList), this$runSingleDayRC, tickers = tickNames, dtdinfo = this$.dtdinfo))                               
    
    this$.fairValues <- zoo(this$.fairValues, order.by = as.POSIXct(row.names(this$.fairValues)))
    this$.richCheaps <- zoo(this$.richCheaps, order.by = as.POSIXct(row.names(this$.richCheaps)))
    
    if(updateTSDB)this$updateRichCheapsInTSDB()
    
    this$.rsqs <- this$calcRSQs()

    this$.deltas <- this$calcDeltas(
		dateList,tickNames,this$.spreadInfo,updateTSDB = updateTSDB)
    
    return(TRUE)
})

setMethodS3("updateRichCheapsInTSDB", "DTDRichCheapCalculator", function(this,...)
{
    colNamesSpreads <- colnames(this$.spreadInfo)    
    func <- function(tickerName){
        myZoo <- this$.richCheaps[,tickerName]
        if(length(na.omit(myZoo))==0)return(FALSE)		
		cds <- SingleNameCDS(tickerName)
		cds$uploadGeneric('rich_cheap',na.omit(myZoo) * 10000,purgeTimeSeries = FALSE)    
        return(TRUE)
    }    
    res <- sapply(colNamesSpreads,func)
})

setMethodS3("createDTDDataFrame", "DTDRichCheapCalculator", function(this, dtdData, spreadData, rate, returnCoefficients = TRUE, ...)
{
    dtdInput <- data.frame(cbind(as.numeric(spreadData), as.numeric(dtdData)))
	colnames(dtdInput) <- c("OAS","DTD")
	dtdInput <- dtdInput[!is.na(dtdInput[,1]),]
	dtdInput <- dtdInput[!is.na(dtdInput[,2]),]
	dtdInput <- dtdInput[dtdInput[,2] > this$.minAcceptableDTD,]
	dtdInput <- dtdInput[dtdInput[,2] < this$.maxAcceptableDTD,]
	
	if(!returnCoefficients) return(dtdInput)
	
	if(NROW(dtdInput) > this$.MINDATAPTS)
        return(this$runDTDLoess(dtdInput,rate,this$.time))         
    else return(c(NA,NA,NA,NA,NA))   
})

setMethodS3("getOASZero", "DTDRichCheapCalculator", function(this, oasdata,dtddata,nNames = 10,...)			
{	
	m <- order(dtddata)[1:nNames]
	.oasdata <- oasdata[m]
	.dtddata <- dtddata[m]		
	predict(lm(.oasdata ~ .dtddata),newdata = data.frame(.dtddata=0))		
})
		
setMethodS3("runDTDLoess", "DTDRichCheapCalculator", function(this, dtdInput, rate, time, ...)
{
    # first column of dtdinfo is OAS, 2nd column is DTD
    oasdata <- dtdInput$OAS
    dtddata <- dtdInput$DTD
    loessfun <- loess(oasdata~dtddata, span = 0.9, na.action = na.exclude)
	this$.loessfun <- loessfun
	this$.testPredictions <- NULL		# this is an optimization for running the deltas
	this$.searchDTDs <- NULL			# this is an optimization for running the deltas
	
	dataSet <- dtddata < 0 & oasdata < this$.thresholdOAS
	.oasdata <- na.omit(oasdata[dataSet])
	.dtddata <- na.omit(dtddata[dataSet])
	
	if(NROW(.oasdata)==0){
		print('all valid dtds > 0')
		this$.DTDshift <- 0
		oasZero <- this$getOASZero(oasdata,dtddata) # linear extrapolation		
	}else{
		print('some dtds < 0')
		this$.DTDshift <- max(this$.minAcceptableDTD,min(.dtddata))		
		oasZero <- predict(loessfun,min(.dtddata))		
	}
	this$.DTDshift <- abs(this$.DTDshift)
	
    dtdMedian <- median(dtddata, na.rm = TRUE)
	oasMedian <- predict(loessfun, dtdMedian)
	dtdMedian <- dtdMedian + this$.DTDshift

    rate <- fincad("aaConvert_cmpd", freq_to = 1,rate_from = rate,freq_from = 2)    
    scalingFactor <- oasZero / (dtdMedian * exp(-rate*time))
	scalingFactor <- max(scalingFactor,this$.minScaleFactor)
     
	if(is.na(oasMedian) == FALSE && is.na(scalingFactor) == FALSE)
    {
        volMedian <- fincad("aaBSG_iv", price_u = dtdMedian,
                                        ex = dtdMedian,
                                        d_exp = this$.matDate,
                                        d_v = this$.baseDate,
                                        price = oasMedian / scalingFactor,
                                        rate_ann = rate,
                                        cost_hldg = 0,
                                        option_type = 2,
                                        acc_rate = 4,
                                        acc_cost_hldg = 4)
        
        coefficients <- c(dtdMedian - this$.DTDshift, scalingFactor, volMedian, this$.DTDshift,oasZero)
    }

    else coefficients <- c(NA,NA,NA,NA,NA)
    
    return(coefficients)	
})

setMethodS3("getLoessFV", "DTDRichCheapCalculator", function(this, dtd, loessCoeff, rate, ...)
{
    dtdMedian <- loessCoeff[[1]]
    scalingFactor <- loessCoeff[[2]]
    volMedian <- loessCoeff[[3]]
	dtdShift <- loessCoeff[[4]]
	dtd <- as.numeric(dtd) + dtdShift
    rate <- fincad("aaConvert_cmpd", freq_to = 1,            # option pricer requires annual compounding
                                     rate_from = rate,
                                     freq_from = 2)          # swaps are quoted semi-annual
 
    if(dtd > 0) loessFV <- scalingFactor * fincad("aaBSG",
                                           price_u = dtd,
                                           ex = dtdMedian + dtdShift,
                                           d_exp = this$.matDate,
                                           d_v = this$.baseDate,
                                           vlt = volMedian,
                                           rate_ann = rate,
                                           cost_hldg = 0,    # COULD ADD DIVIDEND HERE
                                           option_type = 2,  # Put
                                           stat = 1,         # Price
                                           acc_rate = 4,     # 30/360
                                           acc_cost_hldg = 4)
                                                     
    if(dtd <= 0) loessFV <- predict(this$.loessfun, dtd - dtdShift)
    
    return(loessFV)
})

setMethodS3("runSingleTickerFV", "DTDRichCheapCalculator", function(this, ticker, currDate, dtdinfo, currRate, currCoeff, ...)
{
    if(is.na(dtdinfo[1,ticker]) || is.na(currCoeff[1,1]) || is.na(currRate)) return(NA)
    else return(this$getLoessFV(dtdinfo[1,ticker], currCoeff[1,], as.numeric(currRate/100)))
})

setMethodS3("runSingleDayFV", "DTDRichCheapCalculator", function(this,currDate,tickers,dtdinfo,rateTable,...)
{
    print(as.POSIXct(currDate))
    currDate <- as.POSIXct(currDate)
    return(sapply(tickers, this$runSingleTickerFV, currDate = currDate, dtdinfo = dtdinfo[currDate,], currRate = rateTable[currDate,this$.tenor], currCoeff = this$.coefficients[currDate,]))
})   
  
setMethodS3("calcRSQs", "DTDRichCheapCalculator", function(this, ...)
{
    dateList <- index(this$.spreadInfo)
    print("Running RSQs")
    return(sapply(as.character(dateList), this$calcSingleRSQ))
})

setMethodS3("calcSingleRSQ", "DTDRichCheapCalculator", function(this, currDate, ...)
{
    print(as.POSIXct(currDate))
    if(is.na(match(as.POSIXct(currDate), index(this$.spreadInfo))) || is.na(match(as.POSIXct(currDate), index(this$.fairValues))))
        return(NA)
    else 
    {
    print(as.POSIXct(currDate))
    spreaddata <- this$.spreadInfo[as.POSIXct(currDate),]
    fvs <- this$.fairValues[as.POSIXct(currDate),]
    n <- min(length(na.omit(as.numeric(spreaddata))), length(na.omit(as.numeric(fvs))))
    output <- 1 - (n / (n - 2)) * mean(as.numeric((spreaddata - fvs) ^ 2), na.rm = T) / var(as.numeric(spreaddata), na.rm = T)
    return(output)
    }
})
  
setMethodS3("calcDeltas", "DTDRichCheapCalculator", function(this,dateList,tickerList,spreadInfo,updateTSDB = FALSE,...)
{
    print("Running Deltas")
    
    func <- function(currDate){
		print(as.POSIXct(currDate))
        currDate <- as.POSIXct(currDate)        
        output <- sapply(tickerList, this$calcSingleDelta, currDate = currDate,
				currRate = this$.rateTable[currDate,this$.tenor],
				currCoeff = this$.coefficients[currDate,],
				currSpread = spreadInfo[currDate,])	
        return(output)
    }
                                                
    result <- t(sapply(as.character(dateList),func))
    
    if(updateTSDB){    
    
        print("updating Deltas in TSDB")
        
        updateFunc <- function(currTicker){			
			cds <- SingleNameCDS(currTicker)
			cds$uploadGeneric(
				squish('dtd_delta_',rightStr(this$.transformationName,3)),				
				na.omit(zoo(result[,currTicker],rownames(result))),
				purgeTimeSeries = FALSE
			)
        }
        sapply(as.character(tickerList),updateFunc)
    }
    return(result)
})



setMethodS3("calcSingleDelta", "DTDRichCheapCalculator", function(this, currTicker, currDate, currRate, currCoeff, currSpread, loessfun = this$.loessfun, ...)
{
	currDate <- as.POSIXct(currDate)
    
    currRate <- as.numeric(currRate) / 100
    currRate <- fincad("aaConvert_cmpd", freq_to = 1,rate_from = currRate,freq_from = 2)
    dtdMedian <- currCoeff[[1]]
    scalingFactor <- currCoeff[[2]]
    volMedian <- currCoeff[[3]]
	dtdShift <- currCoeff[[4]]
	oasZero <- currCoeff[[5]]
	
	if(all(is.na(c(dtdMedian,scalingFactor,volMedian,dtdShift))))return(NA)
    
    if(this$.hedgeType == "value") 
        currDTD <- as.numeric(this$.dtdinfo[currDate, currTicker])
    if(this$.hedgeType == "price") 
    {
        currOAS <- as.numeric(currSpread[1,currTicker])
		if(is.na(currOAS)) return(NA)
        adjOAS <- currOAS / scalingFactor        
        currDTD <- NA
        
        if(!(is.na(currOAS) || is.na(dtdMedian) || is.na(volMedian))){
            if(currOAS < (oasZero - .001) && !is.na(currOAS) && !is.na(dtdMedian) && !is.na(volMedian)){
                currDTD <- try(fincad("aaBSG_iu", price = adjOAS,
                                      ex = dtdMedian + dtdShift,
                                      d_exp = this$.matDate,
                                      d_v = this$.baseDate,
                                      vlt = volMedian,
                                      rate_ann = currRate,
                                      cost_hldg = 0,
                                      option_type = 2),TRUE)
                if(class(currDTD)== "try-error")currDTD <- NA
			}
		
            if(currOAS >= (oasZero - .001) && !is.na(currOAS)){
               	# Find the shift of DTD to make the threshold OAS where DTD = 0
				if(is.null(this$.searchDTDs))
					this$.searchDTDs <- seq(this$.maxAcceptableDTD, this$.minAcceptableDTD, -0.005)
				searchDTDs <- this$.searchDTDs
				if(is.null(this$.testPredictions))
					this$.testPredictions <- sapply(searchDTDs, function(x) predict(loessfun, x))
				
				testPredictions <- this$.testPredictions
				#clean out NAs in both directions
				testPredictions <- na.locf(na.locf(testPredictions, na.rm = FALSE), rev = TRUE)
				for(i in length(testPredictions):2)
					 if(testPredictions[i-1] > testPredictions[i]) testPredictions[i-1] <- testPredictions[i]
				
				zeroLocation <- findInterval(currOAS, testPredictions)
				
				currDTD <- searchDTDs[zeroLocation] 
			}
        }
    }
    
    if((currDTD + dtdShift)>0 && !is.na(currDTD) && !is.na(dtdMedian) && !is.na(volMedian))
        currDelta <- scalingFactor * fincad("aaBSG", price_u = currDTD + dtdShift,
                                                ex = dtdMedian + dtdShift,
                                                d_exp = this$.matDate,
                                                d_v = this$.baseDate,
                                                vlt = volMedian,
                                                rate_ann = currRate,
                                                cost_hldg = 0,
                                                option_type = 2,
                                                stat = 2, 
                                                acc_rate = 4,
                                                acc_cost_hldg = 4)
    if((currDTD + dtdShift) <= 0 && !is.na(currDTD)) {
		deltaShift <- 0.01
		highDelta <- min(currDTD + deltaShift, 0)
		lowDelta <- highDelta - 2*deltaShift
        currDelta <- (predict(loessfun, highDelta) - predict(loessfun, lowDelta)) / (highDelta - lowDelta)
	}
	
    if(is.na(currDTD) || is.na(dtdMedian) || is.na(volMedian)) currDelta <- NA
    
    return(currDelta)
})
    
setMethodS3("runSingleDayRC", "DTDRichCheapCalculator", function(this, currDate, tickers, dtdinfo, ...)
{
    print(as.POSIXct(currDate))
    currDate <- as.POSIXct(currDate)
    return(sapply(tickers, this$runSingleTickerRC, currDate = as.POSIXct(currDate), dtdinfo = dtdinfo[currDate,], currSpread = this$.spreadInfo[currDate,], currFairValues = this$.fairValues[currDate,]))
})

setMethodS3("runSingleTickerRC", "DTDRichCheapCalculator", function(this, ticker, currDate, dtdinfo, currSpread, currFairValues, ...)
{
    if(is.na(match(as.POSIXct(currDate), index(this$.fairValues))) || is.na(match(as.POSIXct(currDate), index(this$.spreadInfo)))) return(NA)
    if(is.na(dtdinfo[1,ticker]) || is.na(currFairValues[1,ticker])) return(NA)
        
    rc <- as.numeric(currSpread[1,ticker]) - as.numeric(currFairValues[1,ticker])
                                                          
    return(rc)
})

setMethodS3('plotDailyGraph', 'DTDRichCheapCalculator', function(this, dtdInput, coefficients, rate, fileName = NULL, plotTitle = NULL, ...){
	predictedOAS <- sapply(dtdInput$DTD, function(x) this$getLoessFV(x, coefficients, rate))
	lineData <- cbind(dtdInput$DTD, predictedOAS)
	lineData <- lineData[order(lineData[,1]),]
	if(!is.null(fileName)) pdf(fileName,paper="special",width=10,height=10)
	plot(dtdInput$DTD, dtdInput$OAS, main = plotTitle)
	lines(lineData)
	if(!is.null(fileName)) dev.off()
})

setMethodS3("getLoessFVExcel", "DTDRichCheapCalculator", function(this, dtd, oasMedian, scalingFactor, volMedian, dtdShift, rate, time, ...)
{
    assert(class(time) == "numeric", "getLoessFVExcel requires a numeric time > 0")
    assert(time > 0, "getLoessFVExcel requires a numeric time > 0")
    assert(class(rate) == "numeric", "getLoessFVExcel requires a numeric rate > 0")
    assert(rate > 0, "getLoessFVExcel requires a numeric time > 0")
    assert(class(oasMedian) == "numeric", "getLoessFVExcel requires a numeric oasMedian > 0")
    assert(oasMedian > 0, "getLoessFVExcel requires a numeric oasMedian > 0")
    assert(class(scalingFactor) == "numeric", "getLoessFVExcel requires a numeric scalingFactor > 0")
    assert(scalingFactor > 0, "getLoessFVExcel requires a numeric scalingFactor > 0")
    assert(class(volMedian) == "numeric", "getLoessFVExcel requires a numeric volMedian > 0")
    assert(volMedian > 0, "getLoessFVExcel requires a numeric volMedian > 0")
        
    this$.time <- time
    this$.baseDate <- as.POSIXct("2008-02-28")
    this$.matDate <- seq(from = this$.baseDate, by = paste(this$.time*12, "month"), length = 2)[2]
    loessCoeff <- c(oasMedian, scalingFactor, volMedian, dtdShift)
    
    fairValue <- this$getLoessFV(dtd, loessCoeff, rate)
    return(fairValue)
})