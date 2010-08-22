# Tests for CommonMarketFactors
# 
# Author: rsheftel
###############################################################################

library(QFPortfolio)

test.Constructor <- function(){
	CMF <- CommonMarketFactors()
	checkSame(CMF$.factors$names,c("10ySwapRate","CDX.IG5Y","5ySwapSpread","3m5yBpVolDaily","2y10yBpVolDaily"))
	CMF <- CommonMarketFactors(FALSE)
	checkSame(CMF$.factors$names,NULL)
}

test.loadFactors <- function(){
	fileDir <- squish(system.file("testdata", package="QFPortfolio"),'/CommonMarketFactors/')	
	CMF <- CommonMarketFactors(addStandardFactors=FALSE)
	CMF$addFactors('RE.TEST','RE.TEST.TY.1C','','systemdb')
	CMF$addFactors('FileTest','irs_usd_rate_10y_mid','internal',squish(fileDir,'standardFactors.csv'))
	CMF$loadFactors(stripTimes=TRUE)
	checkSame(CMF$getFactorZoo('RE.TEST')[[100]],31.21875)
	checkSame(CMF$getFactorZoo('FileTest')[[100]],11.03)
}

test.statistics <- function(){
	fileDir <- squish(system.file("testdata", package="QFPortfolio"),'/CommonMarketFactors/')
	CMF <- CommonMarketFactors(addStandardFactors=FALSE)
	
	standard.names <- c('10ySwapRate','CDX.IG5Y','5ySwapSpread','3m5yBpVolDaily','2y10yBpVolDaily')
	standard.tickers <- c('irs_usd_rate_10y_mid','cdx-na-ig_market_spread_5y_otr','irs_usd_spread_5y_1n','swaption_usd_3m5y_atm_payer_vol_bp_daily_mid','swaption_usd_2y10y_atm_payer_vol_bp_daily_mid')
	standard.sources <- c('internal','internal','internal','jpmorgan','jpmorgan')
	standard.containers <- rep(squish(fileDir,'standardFactors.csv'),length(standard.names))
	CMF$addFactors(standard.names, standard.tickers, standard.sources, standard.containers, standard.containers)	
	CMF$loadFactors()

	studyData <- TSDataLoader$getDataByName(squish(fileDir,'fncl_6.5_price.csv'),'fncl_6.5_1n_price','internal')
	CMF$analysisVariable(studyData,'FNCL 6.5s')

	stats.matrix <- CMF$statisticsForFactors()
	checkSame(stats.matrix,round(read.csv(squish(fileDir,'statsResults.csv')),3))
}

test.addExternalFactors <- function(){
	CMF <- CommonMarketFactors(addStandardFactors=FALSE)
    addData <- TimeSeriesDB()$retrieveOneTimeSeriesByName("irs_usd_rate_30y_mid",data.source = "internal")	
	CMF$addExternalFactors("irs10Y",addData)
    assert(class(CMF$.external$zoo) == "zoo")
    checkSame(last(CMF$.factors$names),"irs10Y")
    checkSame(last(CMF$.factors$sources),"external")
    checkSame(last(CMF$.factors$tickers),"irs10Y")
	CMF$loadFactors()
	checkSameLooking(addData, CMF$getFactorZoo('irs10Y'))
}

