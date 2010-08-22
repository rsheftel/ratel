constructor("JMarket", function(jobj = NULL) {
    extend(JObject(), "JMarket", .jobj = jobj)
})

method("by_String", "JMarket", enforceRCC = TRUE, function(static, name = NULL, ...) {
    JMarket(jNew("systemdb/metadata/Market", the(name)))
})

method("fixedSlippage", "JMarket", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "D", "fixedSlippage")
})

method("exchange", "JMarket", enforceRCC = TRUE, function(this, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "exchange")
})

method("contractSize_by_String", "JMarket", enforceRCC = TRUE, function(static, name = NULL, ...) {
    jCall("systemdb/metadata/Market", "D", "contractSize", the(name))
})

