constructor("WeightedCurves", function(curves = NULL, weights = rep(1.0, length(curves))) {
    this <- extend(RObject(), "WeightedCurves", .curves = curves, .weights = weights)
    constructorNeeds(this, curves="list(PositionEquityCurve)", weights="Weights|numeric")
    if(inStaticConstructor(this)) return(this)
    if (is.numeric(weights))
        this$.weights <- Weights(weights)     
    this$.weights$requireParallelTo(curves)
    this
})

method(".zoos", "WeightedCurves", function(this, ...) {
    lapply(this$.curves, as.zoo)
})

method("curve", "WeightedCurves", function(this, ...) {
    zoos <- this$scaledZoos()
    zoos <- c(zoos, all = TRUE, fill = 0, retclass = "list")
    zoos <- do.call(merge, zoos)
    z <- sumZoos(zoos)
    colnames(z) <- c("pnl", "position")
    z[, "position"] <- NA
    PositionEquityCurve(ZooCurveLoader(z, "weighted"))
})

method("scaledZoos", "WeightedCurves", function(this, ...) {
    lazy(this$...scaledZoos, this$.weights$scale(this$.zoos()), log=FALSE)
})

method("scaledCurves", "WeightedCurves", function(this, ...) {
	lapply(this$scaledZoos(), function(z) PositionEquityCurve(ZooCurveLoader(z, "weighted")))
})

method("unscaledCurves", "WeightedCurves", function(this, ...) {
	this$.curves
})

method("weights", "WeightedCurves", function(this, ...) {
    this$.weights$weights()
})

method("oneZooUnweighted", "WeightedCurves", function(this, ...) {
    pnls <- lapply(this$.zoos(), function(z) z[,"pnl"])
    args <- c(pnls, list(fill = 0))
    do.call(merge.zoo, args)
})


