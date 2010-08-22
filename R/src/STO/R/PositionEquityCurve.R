constructor("PositionEquityCurve", function(loader = NULL, interval = NULL, range = NULL) {
    this <- extend(RObject(), "PositionEquityCurve", 
        .loader = loader, 
        ...requiredDates = NULL,
        .interval = interval,
        .range = range
    )
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, loader = "RObject?", interval="Interval?", range="Range?")  
    this
})

method("withRange", "PositionEquityCurve", function(this, range, ...) {
    needs(range="Range")        
    PositionEquityCurve(this$.loader, this$.interval, range)
})


method("withInterval", "PositionEquityCurve", function(this, interval, ...) {
    needs(interval="Interval")        
    PositionEquityCurve(this$.loader, interval, this$.range)
})

method("as.character", "PositionEquityCurve", function(this, ...) {
    squish("(", this$.loader$curveName(), ")")
})

method(".data", "PositionEquityCurve", function(this, ...) {
    z <- this$.loader$curveZoo()
    z <- this$.applyRange(z)
    z <- this$.applyInterval(z)
    z
})

method(".applyInterval", "PositionEquityCurve", function(this, z, ...) {
    if(is.null(this$.interval)) return(z)
    positions <- this$.interval$collapse(z, last)[, "position"]
    z <- this$.interval$collapse(z, sum)
    z[, "position"] <- positions
    z
})

method(".applyRange", "PositionEquityCurve", function(this, z, ...) {
    if(is.null(this$.range)) return(z)
    this$.range$cut(z)
})

method("metric", "PositionEquityCurve", function(this, metric, msiv = getStaticInstance(MSIV), ...) {
    needs(metric="Metric", msiv="MSIV")
    getMetricValues <- function(metric, msiv) {
        this$metric(metric, msiv)
    }
    getCurves <- function(msiv) list(this)
    metric$calculate(msiv, getMetricValues, getCurves)
})

method("equity", "PositionEquityCurve", function(this, ...) { cumsum(this$pnl()) })
method("pnl", "PositionEquityCurve", function(this, ...) { column(this$.data(), "pnl") })
method("position", "PositionEquityCurve", function(this, ...) { column(this$.data(), "position") })
method("dates", "PositionEquityCurve", function(this, ...) { index(this$.data()) })

method("writeBin", "PositionEquityCurve", function(class, z, file, ...) {
    needs(z="zoo", file="character|connection")
    data <- vector("numeric", 3 * nrow(z))
    everyThirds <- 1:nrow(z)*3
    data[everyThirds - 2] <- as.vector(index(z))
    data[everyThirds - 1] <- z[, "pnl"]
    data[everyThirds    ] <- z[, "position"]
    writeBin(data, file)
})

method("requireDates", "PositionEquityCurve", function(this, dates, ...) {
    needs(dates="POSIXct")
    this$...requiredDates <- dates
})

method("requireShape", "PositionEquityCurve", function(this, ...) {
    checkShape(this$.data(), ...)
})

method("as.zoo", "PositionEquityCurve", function(this, ...) {
    this$.data()
})

method("toCode", "PositionEquityCurve", function(this, ...) {
    conn <- textConnection("z", open="w", local=TRUE)
    dput(this$as.zoo(), conn)
    squish("PositionEquityCurve(ZooCurveLoader(", z, ", '", this$.loader$curveName(), "'))")
})

method("covers", "PositionEquityCurve", function(this, range, ...) {
    needs(range="Range")
    range$coveredBy(this$dates())      
})

normalizeIndices <- function(curve, dates) {
    z <- curve$as.zoo()
    new.array <- array(0, c(length(dates), 2), list(NULL, c("pnl", "position")))
    indices <- match(index(z), dates)
    new.array[indices,] <- coredata(z)
    zoo(new.array, order.by = dates)
}


checkPnl <- function(curve, expected) {
    checkSame(as.vector(pnl(curve)), expected)
}

checkEquity <- function(curve, expected) {
    checkSame(as.vector(equity(curve)), expected)
}

