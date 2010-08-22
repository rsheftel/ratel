# USD LIBOR Arbitration
# 2/26/08 - Initial version (ryan sheftel)

library(GSFAnalytics)
library(QFFixedIncome)

dataDateTime <- dateTimeFromArguments(commandArgs(), hour=15)
dataDate <- as.POSIXct(strptime(dataDateTime,"%Y-%m-%d"))

tenorVector <- c('on','1w','2w','1m','2m','3m','4m','5m','6m','7m','8m','9m','10m','11m','12m')

tsdbTickers <- paste('libor_usd_rate_',tenorVector,sep="")

tsdb <- TimeSeriesDB()

#prepare the values vectors
valuesJpmorgan <- rep(NA,length=length(tsdbTickers))
valuesBloomberg <- rep(NA,length=length(tsdbTickers))

#Get data from tsdb
for (countTicker in 1:length(tsdbTickers)){
    valuesJpmorgan[[countTicker]] <- tsdb$retrieveOneTimeSeriesByName(tsdbTickers[countTicker], 'jpmorgan', dataDate,dataDate)
    valuesBloomberg[[countTicker]] <- tsdb$retrieveOneTimeSeriesByName(tsdbTickers[countTicker], 'bloomberg_BBAM', dataDate,dataDate)
}

#Arbitrate
valuesInternal <- rep(NA,length=length(tsdbTickers))

#ovenight rate
valuesInternal[1] <- ifelse(is.numeric(valuesBloomberg[1]), valuesBloomberg[1], valuesJpmorgan[1])

#All higher rates
for (countTicker in 2:length(tsdbTickers)){
    valuesInternal[countTicker] <- ifelse(is.numeric(valuesBloomberg[countTicker]), valuesBloomberg[countTicker], valuesJpmorgan[countTicker])
}

#Now do the error checking prior to upload
failIf(any(is.na(valuesInternal)),"Some LIBOR rates are set to NA, cannot upload")
failIf(any(valuesInternal > 100),"Some LIBOR rates are > 100, cannot upload")
failIf(any(valuesInternal < -1),"Some LIBOR rates are < -1, cannot upload")

uploadZooToTsdb(zoo(t(valuesInternal),dataDateTime), tsdbNames=tsdbTickers, tsdbSources='internal', uploadMethod='direct', 
														uploadFilename=squish('LIBOR_USD_internal_',format(dataDate,"%Y%m%d")))
