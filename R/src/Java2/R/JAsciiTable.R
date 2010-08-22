constructor("JAsciiTable", function(jobj = NULL) {
    extend(JObject(), "JAsciiTable", .jobj = jobj)
})

method("by", "JAsciiTable", enforceRCC = TRUE, function(static, ...) {
    JAsciiTable(jNew("systemdb/data/AsciiTable"))
})

method("dataSource_by_String", "JAsciiTable", enforceRCC = TRUE, function(this, x0 = NULL, ...) {
    JHistoricalDailyData(jobj = jCall(this$.jobj, "Lsystemdb/data/HistoricalDailyData;", "dataSource", the(x0)))
})

method("dataSource_by_String", "JAsciiTable", enforceRCC = TRUE, function(this, name = NULL, ...) {
    JAsciiRow(jobj = jCall(this$.jobj, "Lsystemdb/data/AsciiTable/AsciiRow;", "dataSource", the(name)))
})

method("insert_by_String_String_boolean_double", "JAsciiTable", enforceRCC = TRUE, function(this, name = NULL, filename = NULL, isDaily = NULL, priceMultiplier = NULL, ...) {
    jCall(this$.jobj, "V", "insert", the(name), the(filename), theLogical(isDaily), theNumeric(priceMultiplier))
})

method("SYSTEM_ASCII", "JAsciiTable", enforceRCC = FALSE, function(static, ...) {
    lazy(JAsciiTable$...SYSTEM_ASCII, JAsciiTable(jobj = jField("systemdb/data/AsciiTable", "Lsystemdb/data/AsciiTable;", "SYSTEM_ASCII")), log = FALSE)
})

