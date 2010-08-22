constructor("JRBarData", function(jobj = NULL) {
    extend(JObject(), "JRBarData", .jobj = jobj)
})

method("by", "JRBarData", enforceRCC = TRUE, function(static, ...) {
    JRBarData(jNew("systemdb/data/RBarData"))
})

method("dates", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...dates, jField(this$.jobj, "[J", "dates"), log = FALSE)
})

method("volume", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...volume, jField(this$.jobj, "[J", "volume"), log = FALSE)
})

method("openInterest", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...openInterest, jField(this$.jobj, "[J", "openInterest"), log = FALSE)
})

method("low", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...low, jField(this$.jobj, "[D", "low"), log = FALSE)
})

method("high", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...high, jField(this$.jobj, "[D", "high"), log = FALSE)
})

method("close", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...close, jField(this$.jobj, "[D", "close"), log = FALSE)
})

method("open", "JRBarData", enforceRCC = FALSE, function(this, ...) {
    lazy(JRBarData$...open, jField(this$.jobj, "[D", "open"), log = FALSE)
})

