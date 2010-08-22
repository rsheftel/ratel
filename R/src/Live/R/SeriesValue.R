constructor("SeriesValue", function(value = NULL, changed = NULL, ...) {
    this <- extend(RObject(), "SeriesValue", .value = value, .changed = changed)
    constructorNeeds(this, value="numeric|POSIXt", changed="logical")
    this
})

method("as.character", "SeriesValue", function(this, ...) {
    squish(this$.value, ":", this$.changed)
})

method("value", "SeriesValue", function(this, ...) {
    this$.value
})


method("changed", "SeriesValue", function(this, ...) {
    this$.changed
})
