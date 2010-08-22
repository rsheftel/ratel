constructor("JMsivTable", function(jobj = NULL) {
    extend(JObject(), "JMsivTable", .jobj = jobj)
})

method("by", "JMsivTable", enforceRCC = TRUE, function(static, ...) {
    JMsivTable(jNew("systemdb/metadata/MsivTable"))
})

method("insert_by_String_Siv", "JMsivTable", enforceRCC = TRUE, function(this, market = NULL, siv = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(market), .jcast(siv$.jobj, "systemdb.metadata.Siv"))
})

method("exists_by_Siv_String", "JMsivTable", enforceRCC = TRUE, function(this, siv = NULL, market = NULL, ...) {
    jCall(this$.jobj, "Z", "exists", .jcast(siv$.jobj, "systemdb.metadata.Siv"), the(market))
})

method("siv_by_Row", "JMsivTable", enforceRCC = TRUE, function(this, r = NULL, ...) {
    JSiv(jobj = jCall(this$.jobj, "Lsystemdb/metadata/Siv;", "siv", .jcast(r$.jobj, "db.Row")))
})

method("matches_by_Siv", "JMsivTable", enforceRCC = TRUE, function(this, siv = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "matches", .jcast(siv$.jobj, "systemdb.metadata.Siv")))
})

method("systemMatches_by_String", "JMsivTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "systemMatches", the(system)))
})

method("nameMatches_by_StringColumn", "JMsivTable", enforceRCC = TRUE, function(this, msivName = NULL, ...) {
    JClause(jobj = jCall(this$.jobj, "Ldb/clause/Clause;", "nameMatches", .jcast(msivName$.jobj, "db.columns.StringColumn")))
})

method("forName_by_String", "JMsivTable", enforceRCC = TRUE, function(this, msiv = NULL, ...) {
    JMsivRow(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MsivTable/MsivRow;", "forName", the(msiv)))
})

method("markets_by_String", "JMsivTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "markets", the(system)))
})

method("symbols_by_String", "JMsivTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "symbols", the(system)))
})

method("allSivs", "JMsivTable", enforceRCC = TRUE, function(this, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "allSivs"))
})

method("sivs_by_String", "JMsivTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "sivs", the(system)))
})

method("msiv_by_String_Siv", "JMsivTable", enforceRCC = TRUE, function(this, market = NULL, siv = NULL, ...) {
    JMsivRow(jobj = jCall(this$.jobj, "Lsystemdb/metadata/MsivTable/MsivRow;", "msiv", the(market), .jcast(siv$.jobj, "systemdb.metadata.Siv")))
})

method("msivs_by_String", "JMsivTable", enforceRCC = TRUE, function(this, system = NULL, ...) {
    JList(jobj = jCall(this$.jobj, "Ljava/util/List;", "msivs", the(system)))
})

method("MSIVS", "JMsivTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JMsivTable$...MSIVS, JMsivTable(jobj = jField("systemdb/metadata/MsivTable", "Lsystemdb/metadata/MsivTable;", "MSIVS")), log = FALSE)
})

