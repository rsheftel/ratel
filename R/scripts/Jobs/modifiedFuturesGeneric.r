library(Live)

dateList <- getRefLastNextBusinessDates(holidaySource = "financialcalendar", financialCenter = "nyb")

cleanAndUploadModifiedFutures <- function(tsName,data,sourceSource){
	TimeSeriesDB()$purgeTimeSeries(tsName,sourceSource)
	TimeSeriesDB()$writeOneTimeSeriesByName(data,tsName,sourceSource)
}

failIfNoUpdate <- function(z,name){
	assert(as.Date(as.character(last(index(z)))) == as.Date(dateList$lastBusinessDate),squish("No update for: ",name))
}

# CL (Crude Oil)

cl <- Contract('cl','Comdty',monthlyCycle = 1:12)
cl$loadRawData(startDate = '2008-10-01',dataSource = 'bloomberg',timeStampFilter = '14:30:00',quote_type = 'last')
clModified <- cl$continuousRatio(1,Roll(rollMethod = function(x)daysToExpiry(x,1)))
cleanAndUploadModifiedFutures('cl.1c_price_last',clModified,'internal')
failIfNoUpdate(clModified,'cl.1c_price_last')

# DX (USD Index)

dx <- Contract('dx','Curncy')
dx$loadRawData(startDate = '2008-10-01',dataSource = 'bloomberg',timeStampFilter = '14:30:00',quote_type = 'last')
dxModified <- dx$continuousRatio(1,Roll(rollMethod = function(x)daysToExpiry(x,2)))
cleanAndUploadModifiedFutures('dx.1c_price_last',dxModified,'internal')
failIfNoUpdate(dxModified,'dx.1c_price_last')