constructor("JTick", function(jobj = NULL) {
    extend(JObject(), "JTick", .jobj = jobj)
})

method("by_double_long_double_double_double_Date", "JTick", enforceRCC = TRUE, function(static, lastPrice = NULL, lastVolume = NULL, open = NULL, high = NULL, low = NULL, time = NULL, ...) {
    JTick(jNew("systemdb/data/Tick", theNumeric(lastPrice), theLong(lastVolume), theNumeric(open), theNumeric(high), theNumeric(low), .jcast(time$.jobj, "java.util.Date")))
})

method("toString", "JTick", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "toString")
})

method("fields", "JTick", enforceRCC = TRUE, function(this, ...) {
    JFields(jobj = jCall(this$.jobj, "Lsystemdb/data/Fields;", "fields"))
})

method("time", "JTick", enforceRCC = FALSE, function(this, ...) {
    lazy(JTick$...time, JDate(jobj = jField(this$.jobj, "Ljava/util/Date;", "time")), log = FALSE)
})

method("low", "JTick", enforceRCC = FALSE, function(this, ...) {
    lazy(JTick$...low, jField(this$.jobj, "D", "low"), log = FALSE)
})

method("high", "JTick", enforceRCC = FALSE, function(this, ...) {
    lazy(JTick$...high, jField(this$.jobj, "D", "high"), log = FALSE)
})

method("open", "JTick", enforceRCC = FALSE, function(this, ...) {
    lazy(JTick$...open, jField(this$.jobj, "D", "open"), log = FALSE)
})

method("volume", "JTick", enforceRCC = FALSE, function(this, ...) {
    lazy(JTick$...volume, jField(this$.jobj, "J", "volume"), log = FALSE)
})

method("last", "JTick", enforceRCC = FALSE, function(this, ...) {
    lazy(JTick$...last, jField(this$.jobj, "D", "last"), log = FALSE)
})

