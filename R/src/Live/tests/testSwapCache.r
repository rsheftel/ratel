library(Live)

seriesWithValues <- c(
    "MARKETDATA:||:irs_usd_rate_2y:||:LastPrice:||:3.948:||:TRUE",
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

.setUp <- function() { 
    SwapCache$clear()
}

testCache <- function() {
    checkTrue(SwapCache$needsUpdate())
    inputs <- Transformation$parseInputs(seriesWithValues)    
    expected <- c(
        0.03948, 0.03976, 0.04087, 0.04204, 0.04314, 0.04409, 0.04492, 0.04562, 0.04626, 
        0.04899, 0.0493
    )
    checkSame(SwapCache$rates(inputs), expected)
    newSeriesWithValues <- seriesWithValues
    newSeriesWithValues[[1]] <- "MARKETDATA:||:irs_usd_rate_2y:||:LastPrice:||:12.345:||:TRUE"
    checkSame(SwapCache$rates(inputs), expected)

    checkFalse(SwapCache$needsUpdate())
    SwapCache$setLastUpdateTime(SwapCache$lastUpdateTime() - 3601)
    checkTrue(SwapCache$needsUpdate())
}

testCacheCantBeInstance <- function() { 
    shouldBombMatching(SwapCache(), "can only be called as static")
}

testCallback <- function() {
    callbackCalled <- FALSE
    callback <- function() { assign("callbackCalled", TRUE, env=parent.env(environment())) }
    SwapCache$onUpdate(callback)

    SwapCache$rates(Transformation$parseInputs(seriesWithValues))
    checkTrue(callbackCalled)

    callbackCalled <- FALSE
    SwapCache$rates(Transformation$parseInputs(seriesWithValues))
    checkFalse(callbackCalled)

    SwapCache$setLastUpdateTime(SwapCache$lastUpdateTime() - 3601)
    callbackCalled <- FALSE
    SwapCache$rates(Transformation$parseInputs(seriesWithValues))
    checkTrue(callbackCalled)
}
