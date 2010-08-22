constructor("SwapCache", function() {
    this <- extend(RObject(), "SwapCache",
        .last.swap.update = as.POSIXlt("1970/01/01"),
        .live.swap.tenors = c(2:10, 20, 30),
        .live.swap.string.tenors = c("2y","3y","4y","5y","6y","7y","8y","9y","10y","20y","30y"),
        .listeners = list()
    )
    failUnless(inStaticConstructor(this), "SwapCache can only be called as static")
    this
})

method("swapDefinitions", "SwapCache", function(static, ...) {
    lazy(
        static$.swap.definitions,
        sapply(static$.live.swap.tenors, function(t) SeriesDefinition("MARKETDATA", squish("irs_usd_rate_", t, "y"), "LastPrice"), simplify=FALSE),
        log=FALSE
    )
})

method("lastUpdateTime", "SwapCache", function(static, ...) {
    static$.last.swap.update
})

method("setLastUpdateTime", "SwapCache", function(static, t = Sys.time(), ...) {
    static$.last.swap.update <- t
})

method("needsUpdate", "SwapCache", function(static, ...) {
    secondsSinceUpdate <- difftime(Sys.time(), static$.last.swap.update, units="secs")
    secondsSinceUpdate > 3600
})

method("rates", "SwapCache", function(static, inputs, ...) {
    if(static$needsUpdate()) {
        static$.swap.rates <- sapply(inputs$fetchAll(static$swapDefinitions()), value) / 100
        static$setLastUpdateTime()
        for(callback in static$.listeners)
            callback()
    }
    failUnless(length(static$.swap.rates) > 0, "no swap rates available!")
    static$.swap.rates
})

method("clear", "SwapCache", function(static, ...) {
    static$.swap.rates <- NULL
    static$.last.swap.update = as.POSIXlt("1970/01/01")
})

method("data", "SwapCache", function(static, inputs, ...) {
    builder <- SwapCurveBuilder(ccy = "usd")
    builder$getSmoothedDiscountFactors(curveDate = Sys.time(),cashRates = NULL,cashTenors = NULL,swapRates = static$rates(inputs),swapTenors = static$.live.swap.string.tenors)[,-3]
})

method("onUpdate", "SwapCache", function(static, callback, ...) {
    static$.listeners <- appendSlowly(static$.listeners, callback)
})