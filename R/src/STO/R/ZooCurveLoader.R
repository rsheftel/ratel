constructor("ZooCurveLoader", function(z = NULL, name = NULL) {
    this <- extend(RObject(), "ZooCurveLoader", .z = z, .name = name)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, z="zoo", name="character")
    checkSame(colnames(z), c("pnl", "position"))
    this
})

method("curveZoo", "ZooCurveLoader", function(this, ...) {
    this$.z
})

method("curveName", "ZooCurveLoader", function(this, ...) {
    this$.name
})

method("fromEquity", "ZooCurveLoader", function(class, equity, name, ...) {
    needs(equity="zoo")
    pos <- zoo(NA, index(equity))
    pos.zoo <- cbind(pnl(equity), pos)
    colnames(pos.zoo) <- c("pnl", "position")
    class$fromPnl(pos.zoo, name)
})

method("fromPnl", "ZooCurveLoader", function(class, z, name, ...) {
    needs(z="zoo")
    PositionEquityCurve(ZooCurveLoader(z, name))
})

method("fromFile", "ZooCurveLoader", function(class, filename, interval=NULL, range=NULL, ...) {
    c <- PositionEquityCurve(CurveFileLoader(filename), interval, range)
    PositionEquityCurve(ZooCurveLoader(c$as.zoo(), filename))
})
