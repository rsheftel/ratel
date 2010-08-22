library(Live)

.setUp <- function() {
    CdsIndexTriTransformation$clearCache()
    CdsIndexTriTransformation$setDate(as.POSIXct("2007/11/30"))
}

inputs <- list(
    Spread = "CDS:||:CDXNAIGHVOL_snrfor_usd_xr_5y:||:LastSpread:||:227:||:TRUE",
    Swap2y = "MARKETDATA:||:irs_usd_rate_2y:||:LastPrice:||:3.948:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_3y:||:LastPrice:||:3.976:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_4y:||:LastPrice:||:4.087:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_5y:||:LastPrice:||:4.204:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_6y:||:LastPrice:||:4.314:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_7y:||:LastPrice:||:4.409:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_8y:||:LastPrice:||:4.492:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_9y:||:LastPrice:||:4.562:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_10y:||:LastPrice:||:4.626:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_20y:||:LastPrice:||:4.899:||:TRUE",
    "MARKETDATA:||:irs_usd_rate_30y:||:LastPrice:||:4.93:||:TRUE"
)

stripTimestamps <- function(outputs) { 
    outputs[ -grep(":Timestamp:", outputs) ]
}

testClosingData <- function() {
    trans <- CdsIndexTriTransformation("cdx-na-ig-hvol", "CDXNAIGHVOL_snrfor_usd_xr")
    trans$setDate(as.POSIXct("2007/11/30"))
    trans$initialize()
    checkSame(25763.8574067778, trans$closingMV())
}

testUpdate <- function() {
    trans <- CdsIndexTriTransformation("cdx-na-ig-hvol", "CDXNAIGHVOL_snrfor_usd_xr")
    trans$setDate(as.POSIXct(Sys.time()))
    outputs <- trans$update(inputs)
    expectedOutputs <- list(
        "CDSTRI:||:CDXNAIGHVOL_snrfor_usd_xr_5y:||:LastMV:||:whatever",
        "CDSTRI:||:CDXNAIGHVOL_snrfor_usd_xr_5y:||:LastTRI:||:whatever"
    )

    checkSame(the(strsplit(expectedOutputs[[1]], ':||:', fixed=TRUE))[-4],the(strsplit(outputs[[1]], ':||:', fixed=TRUE))[-4])
    checkSame(the(strsplit(expectedOutputs[[2]], ':||:', fixed=TRUE))[-4],the(strsplit(outputs[[2]], ':||:', fixed=TRUE))[-4])    
}
