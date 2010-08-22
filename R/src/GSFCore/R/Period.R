constructor("Period", function(type = NULL, count = NULL) {
    this <- extend(RObject(), "Period", .type = type, .count = count)
    if(inStaticConstructor(this)) return(this)
    constructorNeeds(this, type="character", count = "numeric")
    this
})

method("months", "Period", function(this, count, ...) {
    needs(count="numeric")
    Period(type="months", count)
})

method("days", "Period", function(this, count, ...) {
    needs(count="numeric")
    Period(type="days", count)
})

method("advance", "Period", function(this, from, ...) { 
    needs(from="POSIXct")
    f <- match.fun(squish(this$.type, "Ahead_by_int_Date.JDates"))
    as.POSIXct(f("JDates", this$.count, as.JDate(from)))
}) 

method("rewind", "Period", function(this, from, ...) { 
    needs(from="POSIXct")
    f <- match.fun(squish(this$.type, "Ago_by_int_Date.JDates"))
    as.POSIXct(f("JDates", this$.count, as.JDate(from)))
}) 