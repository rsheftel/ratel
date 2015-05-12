constructor("DTDAdjustedTri", function(...)
{
    extend(RObject(),"DTDAdjustedTri",
        .indexTickers = c("cdx-na-ig","cdx-na-ig-hvol"),
        .tenor = "5y",
        .dataSource = "internal",
        .timeStamp = "15:00:00",
		.base = 100
    )
})

method("calcAdjustedCDSTRI", "DTDAdjustedTri", function(this, cdsTRI,indexTRI,cdsDelta,indexDelta,...)
{
    needs(cdsTRI = "zoo",indexTRI = "zoo",indexTRI = "zoo",cdsDelta = "zoo"); assert(NROW(cdsTRI)>1)
    checkEquals(index(cdsTRI),index(indexTRI))
    checkEquals(index(cdsTRI),index(cdsDelta))
    checkEquals(index(cdsTRI),index(indexDelta))
    
	returnCDSTRI <- diff(cdsTRI)
	returnINDEXTRI <- diff(indexTRI)
	deltaRatio <- lag(cdsDelta/indexDelta,-1)
	adjustedReturns <- returnCDSTRI - deltaRatio * returnINDEXTRI   
	adjustedTRI <- getCumTriFromDailyTri(adjustedReturns,this$.base)
        
    return(adjustedTRI)
})



method("calcCombinedAdjustedCDSTRI", "DTDAdjustedTri", function(this,
    adjustedTri1,adjustedTri2,
    cdsSpreads,avgIndexSpread
,...){
    # Rule based on last close spread levels
    
    needs(adjustedTri1 = "zoo",adjustedTri2 = "zoo",cdsSpreads = "zoo",avgIndexSpread = "zoo"); assert(NROW(adjustedTri1)>1)
    checkEquals(index(adjustedTri1),index(adjustedTri2))
    checkEquals(index(adjustedTri1),index(cdsSpreads))
    checkEquals(index(adjustedTri1),index(avgIndexSpread))
    
	hedgeTriDaily <- diff(merge(adjustedTri1,adjustedTri2))
    mergeSpreads <- merge(cdsSpreads,avgIndexSpread)
   
    m <- merge(hedgeTriDaily,lag(mergeSpreads,-1))

    combinedAdjustedDailyTri <- zoo(0,index(m))

    for(j in 1:NROW(m)) combinedAdjustedDailyTri[j] <- ifelse(m[j,3] < m[j,4],m[j,1],m[j,2])
    
    return(getCumTriFromDailyTri(combinedAdjustedDailyTri,this$.base))
})



method("updateAdjustedCDSTRI", "DTDAdjustedTri", function(this, tickerList,hedge = "cdx-na-ig-hvol",
    deltaQuoteType = "dtd_option_fit_delta",outputQuoteType = "dtd_option_fit_aig",...)
{
    # Loading index data since inception
 
	tsName <- squish(hedge,'_',deltaQuoteType,'_',this$.tenor,'_otr')
	mergedIndexData.1 <- getMergedTimeSeries(tsdb = TimeSeriesDB(),tsName = tsName,dataSource = this$.dataSource)
	mergedIndexData.2 <- getMergedTimeSeries(tsdb = TimeSeriesDB(),tsName = paste(hedge,"_tri_daily_",this$.tenor,"_otr",sep = ""),dataSource = this$.dataSource,filter = this$.timeStamp)
    mergedIndexData <- na.omit(merge(mergedIndexData.1,mergedIndexData.2))
    indexDelta <- mergedIndexData[,1]; indexTRI <- mergedIndexData[,2]
    
    # Looping through tickers

    for (i in 1:NROW(tickerList)){

        # Checking for currentData and get the reference TRI

		cds <- SingleNameCDS(tickerList[i])
        currentData <- cds$genericSeries(outputQuoteType,dataSource = this$.dataSource)
		     
        # Getting cds delta and TRI

        cdsDelta <- cds$genericSeries(deltaQuoteType,dataSource = this$.dataSource)
				
		cdsTRI <- cds$genericSeries('tri')		
        
        if(!is.null(cdsDelta) && !is.null(cdsTRI)){
            mergeData <- getZooDataFrame(na.omit(merge(cdsTRI,indexTRI,cdsDelta,indexDelta)))
        }else{
            mergeData <- NULL
        }
        if((NROW(mergeData) > 1 && NCOL(mergeData) ==4)){        
            res <- this$calcAdjustedCDSTRI(mergeData[,1],mergeData[,2],mergeData[,3],mergeData[,4])			
			cds$uploadGeneric(
				outputQuoteType,				
				na.omit(res),
				purgeTimeSeries = TRUE
			)		
            print(paste("Updated: ",tickerList[i],sep = ""))
        }else{
            print(paste("Did not update: ",tickerList[i],sep = ""))
        }
    }
})

method("updateCombinedAdjustedCDSTRI", "DTDAdjustedTri", function(this, tickerList,
    deltaQuoteType = "dtd_option_fit_delta",inputQuoteType = c("dtd_option_fit_aig","dtd_option_fit_ahv"),outputQuoteType = "dtd_option_fit_acb",...)
{
    # Calc avgIndexSpread
    tsNameIndexSpreads <- sapply(this$.indexTickers,function(x){paste(x,"_market_spread_",this$.tenor,"_otr",sep = "")})
    dataIndexSpreads <- getMergedTimeSeries(TimeSeriesDB(),tsNameIndexSpreads,this$.dataSource)
    avgIndexSpread <- (dataIndexSpreads[,1] + dataIndexSpreads[,2])/2
    avgIndexSpread <- make.zoo.daily(avgIndexSpread,this$.timeStamp)

    for (i in 1:NROW(tickerList)){
    
        # Checking for currentData and get the reference TRI

		cds <- SingleNameCDS(tickerList[i])
		currentData <- cds$genericSeries(outputQuoteType,dataSource = this$.dataSource)					
        
        # Get single name spreads and adjusted TRIs
        
		cds <- SingleNameCDS(ticker = tickerList[i])
		cdsSpreads <- cds$genericSeries('spread')		
		
		adjustedTri1 <- cds$genericSeries(inputQuoteType[1],dataSource = this$.dataSource)
		adjustedTri2 <- cds$genericSeries(inputQuoteType[2],dataSource = this$.dataSource)		
   
        if(!is.null(adjustedTri1) && !is.null(adjustedTri2) && !is.null(cdsSpreads)){
            mergeData <- getZooDataFrame(na.omit(merge(adjustedTri1,adjustedTri2,cdsSpreads,avgIndexSpread)))
        }else{
            mergeData <- NULL
        }
        if((NROW(mergeData) > 1 && NCOL(mergeData) ==4)){        
            combinedAdjustedTri <- this$calcCombinedAdjustedCDSTRI(mergeData[,1],mergeData[,2],mergeData[,3],mergeData[,4])			
			cds$uploadGeneric(
				outputQuoteType,				
				na.omit(combinedAdjustedTri),
				purgeTimeSeries = TRUE
			)
            print(paste("Updated: ",tickerList[i],sep = ""))
        }else{
            print(paste("Did not update: ",tickerList[i],sep = ""))
        }
    }
})