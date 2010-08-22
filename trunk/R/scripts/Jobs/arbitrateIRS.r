# USD Interest Rate Swap Arbitration

library(GSFAnalytics)
library(QFFixedIncome)

dataDateTime <- dateTimeFromArguments(commandArgs(), hour=15)
dataDate <- as.POSIXct(strptime(dataDateTime,"%Y-%m-%d"))

tsdbGet <- function(tsdb, tsdbTicker, tsdbSource, tsdbDate){
    tsdbReturn <- as.numeric(tsdb$retrieveOneTimeSeriesByName(tsdbTicker, tsdbSource, tsdbDate, tsdbDate))
    if(NROW(tsdbReturn)==0)
        tsdbReturn <- NA
    return(tsdbReturn)
}

tenorVector <- c('18m','2y','3y','4y','5y','6y','7y','8y','9y','10y','11y','12y','15y','20y','25y','30y','40y','50y')

tsdbTickers <- paste('irs_usd_rate_',tenorVector,'_mid',sep="")

tsdb <- TimeSeriesDB()

#prepare the values vectors
valuesJpmorgan <- rep(NA,length=length(tsdbTickers))
valuesBloomberg <- rep(NA,length=length(tsdbTickers))

#Get data from tsdb
for (countTicker in 1:length(tsdbTickers)){
    valuesJpmorgan[[countTicker]] <- tsdbGet(tsdb, tsdbTickers[countTicker], 'jpmorgan', dataDate)
    valuesBloomberg[[countTicker]] <- tsdbGet(tsdb, tsdbTickers[countTicker], 'bloomberg_CMN3', dataDate)
}

#Arbitrate
valuesInternal <- rep(NA,length=length(tsdbTickers))

#first rate
valuesInternal[1] <- ifelse(is.numeric(valuesJpmorgan[1]), valuesJpmorgan[1], valuesBloomberg[1])

#All higher rates
for (countTicker in 2:length(tsdbTickers)){
    if (!is.na(valuesJpmorgan[countTicker]))
        valuesInternal[countTicker] <- valuesJpmorgan[countTicker]
    else
        valuesInternal[countTicker] <- valuesInternal[countTicker-1] + (valuesBloomberg[countTicker] - valuesBloomberg[countTicker-1])
}

#Now do the error checking prior to upload
failIf(any(is.na(valuesInternal)),"Some Swap rates are set to NA, cannot upload")
failIf(any(valuesInternal > 100),"Some Swap rates are > 100, cannot upload")
failIf(any(valuesInternal < -1),"Some Swap rates are < -1, cannot upload")

uploadZooToTsdb(zoo(t(valuesInternal),dataDateTime), tsdbNames=tsdbTickers, tsdbSources='internal', uploadMethod='direct', 
																									uploadFilename=squish('SwapRates_internal_',format(dataDate,"%Y%m%d")))
