constructor("JMarketSessionTable", function(jobj = NULL) {
    extend(JObject(), "JMarketSessionTable", .jobj = jobj)
})

method("by", "JMarketSessionTable", enforceRCC = TRUE, function(static, ...) {
    JMarketSessionTable(jNew("systemdb/metadata/MarketSessionTable"))
})

method("removeCloseOffset_by_String_String", "JMarketSessionTable", enforceRCC = TRUE, function(this, string = NULL, day = NULL, ...) {
    jCall(this$.jobj, "V", "removeCloseOffset", the(string), the(day))
})

method("delete_by_String_String", "JMarketSessionTable", enforceRCC = TRUE, function(this, market = NULL, name = NULL, ...) {
    jCall(this$.jobj, "V", "delete", the(market), the(name))
})

method("forName_by_String_String", "JMarketSessionTable", enforceRCC = TRUE, function(this, marketName = NULL, sessionName = NULL, ...) {
    JMarketSession(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MarketSession;", "forName", the(marketName), the(sessionName)))
})

method("update_by_String_String_String_int", "JMarketSessionTable", enforceRCC = TRUE, function(this, market = NULL, session = NULL, close = NULL, closeOffset = NULL, ...) {
    jCall(this$.jobj, "V", "update", the(market), the(session), the(close), theInteger(closeOffset))
})

method("insert_by_String_String_String_String_int", "JMarketSessionTable", enforceRCC = TRUE, function(this, market = NULL, session = NULL, open = NULL, close = NULL, closeOffset = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(market), the(session), the(open), the(close), theInteger(closeOffset))
})

method("insert_by_String_String_String_String_int_String", "JMarketSessionTable", enforceRCC = TRUE, function(this, market = NULL, session = NULL, open = NULL, close = NULL, closeOffset = NULL, timeZone = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(market), the(session), the(open), the(close), theInteger(closeOffset), the(timeZone))
})

method("SESSION", "JMarketSessionTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JMarketSessionTable$...SESSION, JMarketSessionTable(jobj = jField("systemdb/metadata/MarketSessionTable", "Lsystemdb/metadata/MarketSessionTable;", "SESSION")), log = FALSE)
})

