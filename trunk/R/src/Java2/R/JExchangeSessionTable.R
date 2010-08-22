constructor("JExchangeSessionTable", function(jobj = NULL) {
    extend(JObject(), "JExchangeSessionTable", .jobj = jobj)
})

method("by", "JExchangeSessionTable", enforceRCC = TRUE, function(static, ...) {
    JExchangeSessionTable(jNew("systemdb/metadata/ExchangeSessionTable"))
})

method("defaultSession_by_String_String", "JExchangeSessionTable", enforceRCC = TRUE, function(this, market = NULL, name = NULL, ...) {
    JMarketSession(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MarketSession;", "defaultSession", the(market), the(name)))
})

method("deleteIfExists_by_String_String", "JExchangeSessionTable", enforceRCC = TRUE, function(this, exchange = NULL, name = NULL, ...) {
    jCall(this$.jobj, "V", "deleteIfExists", the(exchange), the(name))
})

method("delete_by_String_String", "JExchangeSessionTable", enforceRCC = TRUE, function(this, exchange = NULL, name = NULL, ...) {
    jCall(this$.jobj, "V", "delete", the(exchange), the(name))
})

method("update_by_String_String_String_int", "JExchangeSessionTable", enforceRCC = TRUE, function(this, exchange = NULL, session = NULL, close = NULL, closeOffset = NULL, ...) {
    jCall(this$.jobj, "V", "update", the(exchange), the(session), the(close), theInteger(closeOffset))
})

method("insert_by_String_String_String_String_int", "JExchangeSessionTable", enforceRCC = TRUE, function(this, exchange = NULL, session = NULL, open = NULL, close = NULL, closeOffset = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(exchange), the(session), the(open), the(close), theInteger(closeOffset))
})

method("EXCHANGE_SESSION", "JExchangeSessionTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JExchangeSessionTable$...EXCHANGE_SESSION, JExchangeSessionTable(jobj = jField("systemdb/metadata/ExchangeSessionTable", "Lsystemdb/metadata/ExchangeSessionTable;", "EXCHANGE_SESSION")), log = FALSE)
})

