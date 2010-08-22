constructor("JExchangesTable", function(jobj = NULL) {
    extend(JObject(), "JExchangesTable", .jobj = jobj)
})

method("by", "JExchangesTable", enforceRCC = TRUE, function(static, ...) {
    JExchangesTable(jNew("systemdb/metadata/ExchangesTable"))
})

method("insert_by_String_double_double", "JExchangesTable", enforceRCC = TRUE, function(static, exchange = NULL, defaultBigPointValue = NULL, defaultSlippage = NULL, ...) {
    jCall("systemdb/metadata/ExchangesTable", "V", "insert", the(exchange), theNumeric(defaultBigPointValue), theNumeric(defaultSlippage))
})

method("insert_by_String_double_double_String_int", "JExchangesTable", enforceRCC = TRUE, function(static, name = NULL, defaultBigPointValue = NULL, defaultSlippage = NULL, defaultClose = NULL, defaultCloseOffset = NULL, ...) {
    jCall("systemdb/metadata/ExchangesTable", "V", "insert", the(name), theNumeric(defaultBigPointValue), theNumeric(defaultSlippage), the(defaultClose), theInteger(defaultCloseOffset))
})

method("EXCHANGES", "JExchangesTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JExchangesTable$...EXCHANGES, JExchangesTable(jobj = jField("systemdb/metadata/ExchangesTable", "Lsystemdb/metadata/ExchangesTable;", "EXCHANGES")), log = FALSE)
})

