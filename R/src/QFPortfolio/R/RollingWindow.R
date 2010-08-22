constructor("RollingWindow", function(period = NULL) {
    this <- extend(RObject(), "RollingWindow", .period = period)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, period="Period")
    this
})

method("preceding", "RollingWindow", function(this, date, ...) {
    needs(date="POSIXt")
    Range(this$.period$rewind(date), date)
})

method("following", "RollingWindow", function(this, date, ...) {
    needs(date="POSIXt")
    Range(date, this$.period$advance(date))
})




