constructor("JMarketTable", function(jobj = NULL) {
    extend(JObject(), "JMarketTable", .jobj = jobj)
})

method("by", "JMarketTable", enforceRCC = TRUE, function(static, ...) {
    JMarketTable(jNew("systemdb/metadata/MarketTable"))
})

method("setSlippageCalculator_by_String_String", "JMarketTable", enforceRCC = TRUE, function(this, name = NULL, calculator = NULL, ...) {
    jCall(this$.jobj, "V", "setSlippageCalculator", the(name), the(calculator))
})

method("insert_by_String_String_Double_String_Integer", "JMarketTable", enforceRCC = TRUE, function(this, name = NULL, exchange = NULL, slippage = NULL, closeTime = NULL, processCloseOrdersOffsetSeconds = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), the(exchange), .jcast(slippage$.jobj, "java.lang.Double"), the(closeTime), .jcast(processCloseOrdersOffsetSeconds$.jobj, "java.lang.Integer"))
})

method("insert_by_String_Double", "JMarketTable", enforceRCC = TRUE, function(this, name = NULL, slippage = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), .jcast(slippage$.jobj, "java.lang.Double"))
})

method("slippageCalculator_by_String", "JMarketTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "Ljava/lang/String;", "slippageCalculator", the(name))
})

method("fixedSlippage_by_String", "JMarketTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "D", "fixedSlippage", the(name))
})

method("bigPointValue_by_String", "JMarketTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    jCall(this$.jobj, "D", "bigPointValue", the(name))
})

method("MARKET", "JMarketTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JMarketTable$...MARKET, JMarketTable(jobj = jField("systemdb/metadata/MarketTable", "Lsystemdb/metadata/MarketTable;", "MARKET")), log = FALSE)
})

