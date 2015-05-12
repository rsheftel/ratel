## Test file for the BondIndex object
library(QFCredit)
library(GSFCore)

this <- BondIndex(sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00")
ticker <- "lehman_us_credit"
indexList <- c("lehman_us_credit_intermediate","lehman_us_credit_long")
startDate = "2008-01-28"
endDate = "2008-02-01"
rawTriDates <- as.POSIXct(c("2008-01-28 15:00:00","2008-01-29 15:00:00","2008-01-30 15:00:00","2008-01-31 15:00:00"))
interpolatedDates <- as.POSIXct(c("2008-01-28 15:00:00","2008-01-29 15:00:00","2008-01-30 15:00:00","2008-01-31 15:00:00","2008-02-01 15:00:00"))
weightDates <- as.POSIXct(c("2008-01-28 15:00:00","2008-01-29 15:00:00","2008-01-30 15:00:00"))
rawTri <- zoo(c(1685.22,1688.21,1690.05,1700.51),rawTriDates)

test.BondIndex.constructor <- function(){   
    assert(class(this$.tsdb) %in% "TimeSeriesDB")
    checkEquals(this$.instrument,"bond_index")
    checkEquals(this$.quote_type,"close")
    checkEquals(this$.originalSource,"lehman")    
    checkEquals(this$.analyticsSource,"internal")
    checkEquals(this$.sector,"credit")
    checkEquals(this$.issuer,"all")
    checkEquals(this$.ccy,"usd")
    checkEquals(this$.maturity,"all")
    checkEquals(this$.rating,"all")
    checkEquals(this$.dataTimeStamp,"15:00:00")
    checkEquals(this$.principal,100)
    
    # Should Bombs
    shouldBomb(BondIndex(sector = "invalid",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00"))    
    shouldBomb(BondIndex(sector = "credit",issuer ="invalid",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "15:00:00"))
    shouldBomb(BondIndex(sector = "credit",issuer ="all",ccy = "invalid",maturity = "all",rating = "all",dataTimeStamp = "15:00:00"))    
    shouldBomb(BondIndex(sector = "credit",issuer ="all",ccy = "usd",maturity = "invalid",rating = "all",dataTimeStamp = "15:00:00"))    
    shouldBomb(BondIndex(sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "invalid",dataTimeStamp = "15:00:00"))    
    shouldBomb(BondIndex(sector = "credit",issuer ="all",ccy = "usd",maturity = "all",rating = "all",dataTimeStamp = "invalid"))
}

test.BondIndex.getInterpolatedZoo <- function(){  
    termStructure <- TermStructure$us_treasury
    termStructureZoo <- getTermStructureForTimeSeries("bond_government_usd_maturity_otr_yield",termStructure,"internal",startDate,endDate,lookFor = "maturity")
    maturityZoo <- (this$.tsdb)$retrieveTimeSeriesByName("lehman_us_credit_maturity",data.source = "internal",start = startDate,end = endDate)[[1]]

    # base case
    result <- this$getInterpolatedZoo(termStructure,termStructureZoo,maturityZoo)    
    target <- getZooDataFrame(zoo(c(3.587677852419935,3.659825014175560,3.733888173541108,3.644017236891113,3.605014767986201),interpolatedDates))
    colnames(target) <- "interpolated"
    checkSame(target,result)
    # maturity date missing
    result <- this$getInterpolatedZoo(termStructure,termStructureZoo,maturityZoo[-3,])
    checkSame(target[-3,],result)
    # termStructure date missing
    result <- this$getInterpolatedZoo(termStructure,termStructureZoo[-3,],maturityZoo)
    checkSame(target[-3,],result)
    # check 10y tenor missing
    termStructureZoo[2,3] = NA
    result <- this$getInterpolatedZoo(termStructure,termStructureZoo,maturityZoo,requiredTenor = "10y")
    checkSame(target[-2,],result)
    
    shouldBomb(this$getInterpolatedZoo(termStructure,termStructureZoo,maturityZoo,requiredTenor = "junk"))
}

test.BondIndex.getMarketValueWeights <- function(){  
    subIndexList <- indexList
    result <- this$getMarketValueWeights(subIndexList,startDate = "2008-01-28",endDate = "2008-01-30")
    target <- getZooDataFrame(zoo(t(matrix(c(0.7461533225831118,0.2538466774168882,0.7462300694314510,
        0.2537699305685490,0.7460141369267147,0.2539858630732852),nrow = 2,ncol = 3)),weightDates))
    colnames(target) <- c("lehman_us_credit_intermediate_market_value","lehman_us_credit_long_market_value") 
    checkSame(target,result)
    
    shouldBomb(this$getMarketValueWeights("lehman_us_credit",startDate = "2008-01-28",endDate = "2008-01-30"))
    shouldBomb(this$getMarketValueWeights(c("junk","lehman_us_credit"),startDate = "2008-01-28",endDate = "2008-01-30"))
    shouldBomb(this$getMarketValueWeights(subIndexList,startDate = TRUE,endDate = "2008-01-30"))
    shouldBomb(this$getMarketValueWeights(subIndexList,startDate = "2008-01-30",endDate = TRUE))
}

test.BondIndex.getWeightedSeries <- function(){
    startDate = "2008-01-28"
    endDate = "2008-01-30"   
    weightsZoo <- this$getMarketValueWeights(indexList,startDate = startDate,endDate = endDate) 
    seriesZoo <- spread.data <- na.omit(getMergedTimeSeries(this$.tsdb,paste(indexList,"adjusted_spread",sep = "_"),"internal",startDate,endDate))
    result <- this$getWeightedSeries(seriesZoo,weightsZoo)
    target <- getZooDataFrame(zoo(c(2.089988243209432,2.057413575196845,2.049470170533142),weightDates))
    colnames(target) <- colnames(result)
    #checkEquals(result,target)
    
    shouldBomb(this$getWeightedSeries(seriesZoo[,-1],weightsZoo))
    shouldBomb(this$getWeightedSeries(seriesZoo,weightsZoo[,-1]))    
}

test.BondIndex.arbitrateRawData <- function(){
    result <- BondIndex$arbitrateRawData("lehman_us_credit",startDate =  businessDaysAgo(1,Sys.Date(),"nyb"),endDate = NULL)
    #checkSame(result,TRUE)
}




